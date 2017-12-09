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
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;

/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
public class KubectlBuildWrapper extends SimpleBuildWrapper {

    @DataBoundSetter
    public String serverUrl;

    @DataBoundSetter
    public String credentialsId;

    @DataBoundSetter
    public String caCertificate;

    @DataBoundConstructor
    public KubectlBuildWrapper() {
    }

    @Override
    public void setUp(Context context, Run<?, ?> build,
                      FilePath workspace,
                      Launcher launcher,
                      TaskListener listener,
                      EnvVars initialEnvironment) throws IOException, InterruptedException {
        KubeConfigWriter kubeConfigWriter = KubeConfigWriterFactory.get(
                this.serverUrl,
                this.credentialsId,
                this.caCertificate,
                workspace,
                launcher,
                build);

        // Write the kubeconfig file
        String configFile = kubeConfigWriter.writeKubeConfig();

        // Remove it when the build is finished
        context.setDisposer(new CleanupDisposer(configFile));

        // Set environment for the kubectl calls to find the configuration
        context.env(KubeConfigWriter.ENV_VARIABLE_NAME, configFile);
    }

    @Extension
    public static class DescriptorImpl extends BuildWrapperDescriptor {
        // List of supported credentials
        private static CredentialsMatcher matcher = CredentialsMatchers.anyOf(
                CredentialsMatchers.instanceOf(StandardUsernamePasswordCredentials.class),
                CredentialsMatchers.instanceOf(TokenProducer.class),
                CredentialsMatchers.instanceOf(StringCredentials.class),
                CredentialsMatchers.instanceOf(StandardCertificateCredentials.class)
        );

        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Setup Kubernetes CLI (kubectl)";
        }

        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item item, @QueryParameter String serverUrl) {
            return new StandardListBoxModel()
                    .includeEmptyValue()
                    .includeMatchingAs(
                            ACL.SYSTEM,
                            item,
                            StandardCredentials.class,
                            URIRequirementBuilder.fromUri(serverUrl).build(),
                            matcher);
        }
    }

    /**
     * @author Max Laverse
     */
    public static class CleanupDisposer extends Disposer {

        private static final long serialVersionUID = 1L;
        private String fileToBeRemoved;

        public CleanupDisposer(String file) {
            this.fileToBeRemoved = file;
        }

        @Override
        public void tearDown(Run<?, ?> build,
                             FilePath workspace,
                             Launcher launcher,
                             TaskListener listener) throws IOException, InterruptedException {
            workspace.child(fileToBeRemoved).delete();
            listener.getLogger().println("kubectl configuration cleaned up");
        }
    }
}
