package org.jenkinsci.plugins.kubernetes.cli;

import com.cloudbees.plugins.credentials.CredentialsMatcher;
import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.common.StandardCertificateCredentials;
import com.cloudbees.plugins.credentials.common.StandardCredentials;
import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Item;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.tasks.BuildWrapperDescriptor;
import hudson.util.ListBoxModel;
import jenkins.tasks.SimpleBuildWrapper;
import org.jenkinsci.plugins.kubernetes.cli.kubeconfig.KubeConfigWriter;
import org.jenkinsci.plugins.kubernetes.cli.kubeconfig.KubeConfigWriterFactory;
import org.jenkinsci.plugins.kubernetes.credentials.TokenProducer;
import org.jenkinsci.plugins.plaincredentials.FileCredentials;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MultiKubectlBuildWrapper extends SimpleBuildWrapper {
    final transient public List<KubectlCredential> kubectlCredentials;

    @DataBoundConstructor
    public MultiKubectlBuildWrapper(List<KubectlCredential> credentials) {
        if (credentials == null || credentials.size() == 0) {
            throw new RuntimeException("Credentials list cannot be empty");
        }
        this.kubectlCredentials = credentials;
    }

    @Override
    public void setUp(Context context, Run<?, ?> build,
                      FilePath workspace,
                      Launcher launcher,
                      TaskListener listener,
                      EnvVars initialEnvironment) throws IOException, InterruptedException {

        List<String> configFiles = new ArrayList<String>();
        for(KubectlCredential cred: this.kubectlCredentials) {
            KubeConfigWriter kubeConfigWriter = KubeConfigWriterFactory.get(
                    cred.serverUrl,
                    cred.credentialsId,
                    cred.caCertificate,
                    cred.clusterName,
                    cred.contextName,
                    cred.namespace,
                    workspace,
                    launcher,
                    build);

            configFiles.add(kubeConfigWriter.writeKubeConfig());
        }

        // Remove it when the build is finished
        context.setDisposer(new CleanupDisposer(configFiles));

        // Set environment for the kubectl calls to find the configuration
        String configFileList = String.join(File.pathSeparator, configFiles);
        context.env(KubeConfigWriter.ENV_VARIABLE_NAME, configFileList);
    }

    @Extension
    public static class DescriptorImpl extends BuildWrapperDescriptor {
        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Configure Kubernetes CLI (kubectl) with multiple credentials";
        }
    }

    public static class CleanupDisposer extends Disposer {

        private static final long serialVersionUID = 1L;
        private List<String> filesToBeRemoved;

        public CleanupDisposer(List<String > files) {
            this.filesToBeRemoved = files;
        }

        @Override
        public void tearDown(Run<?, ?> build,
                             FilePath workspace,
                             Launcher launcher,
                             TaskListener listener) throws IOException, InterruptedException {
            for(String file : filesToBeRemoved) {
                workspace.child(file).delete();
            }
            listener.getLogger().println("kubectl configuration cleaned up");
        }
    }
}
