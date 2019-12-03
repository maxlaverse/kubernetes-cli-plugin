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
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class MultiKubectlBuildStep extends Step {
    final transient public List<KubectlCredential> kubectlCredentials;

    @DataBoundConstructor
    public MultiKubectlBuildStep(List<KubectlCredential> credentials) {
        if (credentials == null || credentials.size() == 0) {
            throw new RuntimeException("Credentials list cannot be empty");
        }
        this.kubectlCredentials = credentials;
    }

    @Override
    public final StepExecution start(StepContext context) throws Exception {
        return new GenericBuildStep(this.kubectlCredentials, context);
    }

    @Extension
    public static class DescriptorImpl extends StepDescriptor {

        /**
         * {@inheritDoc}
         */
        @Override
        public String getDisplayName() {
            return "Configure Kubernetes CLI (kubectl) with multiple credentials";
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getFunctionName() {
            return "withMultiKubeConfigs";
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

    }
}
