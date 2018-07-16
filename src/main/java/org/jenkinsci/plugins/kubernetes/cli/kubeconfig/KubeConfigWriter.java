package org.jenkinsci.plugins.kubernetes.cli.kubeconfig;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardCertificateCredentials;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import hudson.AbortException;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import hudson.util.QuotedStringTokenizer;
import hudson.util.Secret;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.plugins.kubernetes.credentials.TokenProducer;
import org.jenkinsci.plugins.plaincredentials.FileCredentials;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

/**
 * @author Max Laverse
 */
public class KubeConfigWriter {
    public static final String ENV_VARIABLE_NAME = "KUBECONFIG";

    private static final String KUBECTL_BINARY = "kubectl";
    private static final String USERNAME = "cluster-admin";
    private static final String DEFAULT_CONTEXTNAME = "k8s";
    private static final String CLUSTERNAME = "k8s";

    private final String serverUrl;
    private final String credentialsId;
    private final String caCertificate;
    private final String contextName;
    private final FilePath workspace;
    private final Launcher launcher;
    private final Run<?, ?> build;

    public KubeConfigWriter(@Nonnull String serverUrl, @Nonnull String credentialsId,
                            @Nonnull String caCertificate, @Nonnull String contextName, FilePath workspace, Launcher launcher, Run<?, ?> build) {
        this.serverUrl = serverUrl;
        this.credentialsId = credentialsId;
        this.caCertificate = caCertificate;
        this.workspace = workspace;
        this.launcher = launcher;
        this.build = build;
        this.contextName = contextName;
    }

    /**
     * Write a configuration file for kubectl to disk.
     *
     * @return path to configfile
     * @throws IOException          on file operations
     * @throws InterruptedException on file operations
     */
    public String writeKubeConfig() throws IOException, InterruptedException {
        if (!workspace.exists()) {
            launcher.getListener().getLogger().println("creating missing workspace to write kubeconfig");
            workspace.mkdirs();
        }

        FilePath configFile = workspace.createTempFile(".kube", "config");

        final StandardCredentials credentials = getCredentials(build);
        if (credentials == null) {
            throw new AbortException("No credentials defined to setup Kubernetes CLI");
        } else if (credentials instanceof FileCredentials) {
            setRawKubeConfig(configFile, (FileCredentials) credentials);
            if (wasContextProvided()) {
                useContext(configFile.getRemote(), this.contextName);
            }
            if(this.wasServerUrlProvided()){
                launcher.getListener().getLogger().println("the serverUrl will be ignored as a raw kubeconfig file was provided");
            }
        } else {
            setCluster(configFile.getRemote());
            setCredentials(configFile.getRemote(), credentials);
            setContext(configFile.getRemote(), this.getContextNameOrDefault());
            useContext(configFile.getRemote(), this.getContextNameOrDefault());
        }


        return configFile.getRemote();
    }

    /**
     * Set the whole kube configuration file from a FileCredentials.
     *
     * @throws IOException          on file operations
     * @throws InterruptedException on file operations
     */
    private void setRawKubeConfig(FilePath configFile, FileCredentials credentials) throws IOException, InterruptedException {
        OutputStream output = configFile.write();
        IOUtils.copy(credentials.getContent(), output);
        output.close();
    }

    /**
     * Set the cluster section of the kube configuration file.
     *
     * @throws IOException          on file operations
     * @throws InterruptedException on file operations
     */
    private void setCluster(String configFile) throws IOException, InterruptedException {
        String tlsConfigArgs;
        Set<String> filesToBeRemoved = newHashSet();

        if (caCertificate == null || caCertificate.isEmpty()) {
            tlsConfigArgs = " --insecure-skip-tls-verify=true";
        } else {
            // Write certificate on disk
            FilePath caCrtFile = workspace.createTempFile("cert-auth", "crt");
            caCrtFile.write(CertificateHelper.wrapCertificate(caCertificate), null);
            filesToBeRemoved.add(caCrtFile.getRemote());

            tlsConfigArgs = " --embed-certs=true --certificate-authority=" + caCrtFile.getRemote();
        }

        try {
            int status = launcher.launch()
                    .envs(String.format("KUBECONFIG=%s", configFile))
                    .cmdAsSingleString(String.format("%s config set-cluster %s --server=%s %s",
                            KUBECTL_BINARY,
                            CLUSTERNAME,
                            serverUrl,
                            tlsConfigArgs))
                    .stdout(launcher.getListener())
                    .join();
            if (status != 0) throw new IOException("Failed to add kubectl cluster (exit code  " + status + ")");
        } finally {
            for (String tempFile : filesToBeRemoved) {
                workspace.child(tempFile).delete();
            }
        }
    }

    /**
     * Set the user section of the kube configuration file.
     *
     * @throws IOException          on file operations
     * @throws InterruptedException on file operations
     */
    private void setCredentials(String configFile, StandardCredentials credentials) throws IOException, InterruptedException {
        Set<String> tempFiles = newHashSet();

        String credentialsArgs;
        int sensitiveFieldsCount = 1;
        if (credentials instanceof TokenProducer) {
            credentialsArgs = "--token=\"" + ((TokenProducer) credentials).getToken(serverUrl, null, true) + "\"";
        } else if (credentials instanceof StringCredentials) {
            credentialsArgs = "--token=\"" + ((StringCredentials) credentials).getSecret() + "\"";
        } else if (credentials instanceof UsernamePasswordCredentials) {
            UsernamePasswordCredentials upc = (UsernamePasswordCredentials) credentials;
            credentialsArgs = "--username=\"" + upc.getUsername() + "\" --password=\"" + Secret.toString(upc.getPassword()) + "\"";
        } else if (credentials instanceof StandardCertificateCredentials) {
            sensitiveFieldsCount = 0;
            FilePath clientCrtFile = workspace.createTempFile("client", "crt");
            FilePath clientKeyFile = workspace.createTempFile("client", "key");
            CertificateHelper.extractFromCertificate((StandardCertificateCredentials) credentials, clientCrtFile, clientKeyFile);
            tempFiles.add(clientCrtFile.getRemote());
            tempFiles.add(clientKeyFile.getRemote());
            credentialsArgs = "--embed-certs=true --client-certificate=" + clientCrtFile.getRemote() + " --client-key="
                    + clientKeyFile.getRemote();
        } else {
            throw new AbortException("Unsupported Credentials type " + credentials.getClass().getName());
        }

        String[] cmds = QuotedStringTokenizer.tokenize(String.format("%s config set-credentials %s %s",
                KUBECTL_BINARY,
                USERNAME,
                credentialsArgs));

        int status = launcher.launch()
                .envs(String.format("KUBECONFIG=%s", configFile))
                .cmds(cmds)
                .masks(getMasks(cmds.length, sensitiveFieldsCount))
                .stdout(launcher.getListener())
                .join();
        if (status != 0) throw new IOException("Failed to add kubectl credentials (exit code  " + status + ")");

        for (String tempFile : tempFiles) {
            workspace.child(tempFile).delete();
        }

    }

    /**
     * Set the context section of the kube configuration file.
     *
     * @throws IOException          on file operations
     * @throws InterruptedException on file operations
     */
    private void setContext(String configFile, String contextName) throws IOException, InterruptedException {
        // Add the context
        int status = launcher.launch()
                .envs(String.format("KUBECONFIG=%s", configFile))
                .cmdAsSingleString(String.format("%s config set-context %s --cluster=%s --user=%s",
                        KUBECTL_BINARY,
                        contextName,
                        CLUSTERNAME,
                        USERNAME))
                .stdout(launcher.getListener())
                .join();
        if (status != 0) throw new IOException("Failed to add kubectl context (exit code  " + status + ")");
    }

    /**
     * Set the current context of the kube configuration file.
     *
     * @throws IOException          on file operations
     * @throws InterruptedException on file operations
     */
    private void useContext(String configFile, String contextName) throws IOException, InterruptedException {
        int status = launcher.launch()
                .envs(String.format("KUBECONFIG=%s", configFile))
                .cmdAsSingleString(String.format("%s config use-context %s",
                        KUBECTL_BINARY,
                        contextName))
                .stdout(launcher.getListener())
                .join();

        if (status != 0) throw new IOException("Failed to set kubectl current context (exit code  " + status + ")");
    }

    /**
     * Returns an array of mask to hide the last fields of a Jenkins launch command.
     */
    private boolean[] getMasks(int numberOfFields, int numberOfSensibleFields) {
        boolean[] masks = new boolean[numberOfFields];
        for (int i = 0; i < numberOfSensibleFields; i++) {
            masks[masks.length - 1 - i] = true;
        }
        return masks;
    }

    /**
     * Get the {@link StandardCredentials}.
     *
     * @return the credentials matching the {@link #credentialsId} or {@code null} is {@code #credentialsId} is blank
     * @throws AbortException if no {@link StandardCredentials} matching {@link #credentialsId} is found
     */
    private StandardCredentials getCredentials(Run<?, ?> build) throws AbortException {
        if (StringUtils.isBlank(credentialsId)) {
            return null;
        }
        StandardCredentials result = CredentialsProvider.findCredentialById(
                credentialsId,
                StandardCredentials.class,
                build,
                Collections.<DomainRequirement>emptyList());

        if (result == null) {
            throw new AbortException("No credentials found for id \"" + credentialsId + "\"");
        }
        return result;
    }

    /**
     * Return whether or not a contextName was provided
     *
     * @return true if a contextName was provided to the plugin.
     */
    private boolean wasContextProvided() {
        return this.contextName != null && !this.contextName.isEmpty();
    }

    /**
     * Return whether or not a serverUrl was provided
     *
     * @return true if a serverUrl was provided to the plugin.
     */
    private boolean wasServerUrlProvided() {
        return this.serverUrl != null && !this.serverUrl.isEmpty();
    }

    /**
     * Returns a contextName
     *
     * @return contextName if provided, else the default value.
     */
    private String getContextNameOrDefault() {
        if (!wasContextProvided()) {
            return DEFAULT_CONTEXTNAME;
        }
        return this.contextName;
    }

}
