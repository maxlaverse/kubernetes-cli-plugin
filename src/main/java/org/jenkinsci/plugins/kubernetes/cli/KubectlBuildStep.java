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
import hudson.model.Item;
import hudson.model.TaskListener;
import hudson.security.ACL;
import hudson.util.ListBoxModel;
import org.jenkinsci.plugins.kubernetes.cli.kubeconfig.KubeConfigWriter;
import org.jenkinsci.plugins.kubernetes.cli.kubeconfig.KubeConfigWriterFactory;
import org.jenkinsci.plugins.kubernetes.credentials.TokenProducer;
import org.jenkinsci.plugins.plaincredentials.FileCredentials;
import org.jenkinsci.plugins.plaincredentials.StringCredentials;
import org.jenkinsci.plugins.workflow.steps.*;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.*;

/**
 * @author Max Laverse
 */
public class KubectlBuildStep extends Step {

    @DataBoundSetter
    public String serverUrl;

    @DataBoundSetter
    public String credentialsId;

    @DataBoundSetter
    public String caCertificate;

    @DataBoundSetter
    public String contextName;

    @DataBoundSetter
    public String clusterName;

    @DataBoundSetter
    public String namespace;

    @DataBoundConstructor
    public KubectlBuildStep() {
    }

    @Override
    public final StepExecution start(StepContext context) throws Exception {
        KubectlCredential cred = new KubectlCredential();
        cred.serverUrl = this.serverUrl;
        cred.credentialsId = this.credentialsId;
        cred.caCertificate = this.caCertificate;
        cred.contextName = this.contextName;
        cred.clusterName = this.clusterName;
        cred.namespace = this.namespace;

        List<KubectlCredential> list = new ArrayList<KubectlCredential>();
        list.add(cred);

        return new GenericBuildStep(list, context);
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {
        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return "Configure Kubernetes CLI (kubectl)";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getFunctionName() {
            return "withKubeConfig";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean takesImplicitBlockArgument() {
            return true;
        }

        @Override
        public Set<? extends Class<?>> getRequiredContext() {
            return new HashSet<>();
        }

        public ListBoxModel doFillCredentialsIdItems(@Nonnull @AncestorInPath Item item, @QueryParameter String serverUrl) {
            return new StandardListBoxModel()
                    .includeEmptyValue()
                    .includeMatchingAs(
                            ACL.SYSTEM,
                            item,
                            StandardCredentials.class,
                            URIRequirementBuilder.fromUri(serverUrl).build(),
                            KubectlCredential.supportedCredentials);
        }
    }

}
