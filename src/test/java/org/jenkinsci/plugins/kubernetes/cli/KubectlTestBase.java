package org.jenkinsci.plugins.kubernetes.cli;

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SecretBytes;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import com.cloudbees.plugins.credentials.impl.CertificateCredentialsImpl;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import hudson.model.Slave;
import hudson.util.Secret;
import org.apache.commons.compress.utils.IOUtils;
import org.jenkinsci.plugins.kubernetes.credentials.OpenShiftBearerTokenCredentialImpl;
import org.jenkinsci.plugins.plaincredentials.FileCredentials;
import org.jenkinsci.plugins.plaincredentials.impl.FileCredentialsImpl;
import org.jenkinsci.plugins.plaincredentials.impl.StringCredentialsImpl;
import org.jvnet.hudson.test.FakeLauncher;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.PretendSlave;

import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * @author Max Laverse
 */
public class KubectlTestBase {
    protected static final String CREDENTIAL_ID = "cred1234";
    protected static final String PASSPHRASE = "test";
    protected static final String USERNAME_WITH_SPACE = "bob with-userspace";
    protected static final String USERNAME = "bob";
    protected static final String PASSWORD = "s3cr3t";
    protected static final String PASSWORD_WITH_SPACE = "s3cr3t with-passwordspace";
    protected static final String CA_CERTIFICATE = "-----BEGIN CERTIFICATE-----\na-certificate\n-----END CERTIFICATE-----";
    protected static final String SERVER_URL = "https://localhost:6443";
    protected static final String KUBECTL_BINARY = "kubectl";

    protected String loadResource(String name) {
        try {
            return new String(IOUtils.toByteArray(getClass().getResourceAsStream(name)));
        } catch (Throwable t) {
            throw new RuntimeException("Could not read resource:[" + name + "].");
        }
    }

    protected String getResourceFile(String name) {
        return getClass().getResource(name).getFile();
    }

    protected Slave getFakeSlave(JenkinsRule r) throws Exception {
        PretendSlave slave = r.createPretendSlave(p -> {
            if (p.cmds().get(0).equals(KUBECTL_BINARY)) {
                String[] maskedCmd = getMaskedCmd(p.cmds(), p.masks());
                PrintStream ps = new PrintStream(p.stdout());
                ps.println("Call stubbed for: " + String.join(", ", maskedCmd) + " with env: " + String.join(",", p.envs()));

                return new FakeLauncher.FinishedProc(0);
            }
            return r.createLocalLauncher().launch(p);
        });
        slave.setLabelString("mocked-kubectl");
        return slave;
    }

    private String[] getMaskedCmd(List<String> cmds, boolean[] masks) {
        String[] strArr = cmds.toArray(new String[cmds.size()]);
        if (masks != null) {
            for (int i = 0; i < masks.length; i++) {
                if (masks[i] == true) {
                    strArr[i] = "****";
                }
            }
        }
        return strArr;
    }

    protected boolean kubectlPresent() {
        return Stream.of(System.getenv("PATH").split(Pattern.quote(File.pathSeparator)))
                .map(Paths::get)
                .map(p -> p.resolve(KUBECTL_BINARY))
                .filter(Files::exists)
                .anyMatch(Files::isExecutable);
    }

    protected BaseStandardCredentials secretCredential(String credentialId) {
        return new StringCredentialsImpl(CredentialsScope.GLOBAL, credentialId, "sample", Secret.fromString(PASSWORD));
    }

    protected BaseStandardCredentials secretCredentialWithSpace(String credentialId) {
        return new StringCredentialsImpl(CredentialsScope.GLOBAL, credentialId, "sample", Secret.fromString(PASSWORD_WITH_SPACE));
    }

    protected BaseStandardCredentials certificateCredential(String credentialId) {
        String storeFile = getResourceFile("/org/jenkinsci/plugins/kubernetes/cli/kubernetes.pkcs12");
        CertificateCredentialsImpl.KeyStoreSource keyStoreSource = new CertificateCredentialsImpl.FileOnMasterKeyStoreSource(storeFile);
        return new CertificateCredentialsImpl(CredentialsScope.GLOBAL, credentialId, "sample", PASSPHRASE, keyStoreSource);
    }

    protected BaseStandardCredentials usernamePasswordCredential(String credentialId) {
        return new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, credentialId, "sample", USERNAME, PASSWORD);
    }

    protected BaseStandardCredentials usernamePasswordCredentialWithSpace(String credentialId) {
        return new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, credentialId, "sample", USERNAME_WITH_SPACE, PASSWORD_WITH_SPACE);
    }

    protected FileCredentials fileCredential(String credentialId) throws UnsupportedEncodingException {
        return new FileCredentialsImpl(CredentialsScope.GLOBAL, credentialId, "sample", "file-name", SecretBytes.fromBytes("---\napiVersion: v1\nclusters:\n- cluster:\n  name: test-sample\n".getBytes("UTF-8")));
    }

    protected OpenShiftBearerTokenCredentialImpl tokenCredential(String credentialId) {
        return new OpenShiftBearerTokenCredentialImpl(CredentialsScope.GLOBAL, credentialId, "a-description", USERNAME, PASSWORD);
    }
}
