package org.jenkinsci.plugins.kubernetes.cli.kubeconfig;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.Run;
import org.jenkinsci.plugins.workflow.steps.StepContext;

import javax.annotation.Nonnull;
import java.io.IOException;

/**
 * @author Max Laverse
 */
public abstract class KubeConfigWriterFactory {
    public static KubeConfigWriter get(@Nonnull String serverUrl, @Nonnull String credentialsId,
                                       @Nonnull String caCertificate, @Nonnull String contextName, FilePath workspace, Launcher launcher, Run<?, ?> build) {
        return new KubeConfigWriter(serverUrl, credentialsId, caCertificate, contextName, workspace, launcher, build);
    }

    public static KubeConfigWriter get(@Nonnull String serverUrl, @Nonnull String credentialsId,
                                       @Nonnull String caCertificate, @Nonnull String contextName, StepContext context) throws IOException, InterruptedException {
        Run<?, ?> run = context.get(Run.class);
        FilePath workspace = context.get(FilePath.class);
        Launcher launcher = context.get(Launcher.class);
        return new KubeConfigWriter(serverUrl, credentialsId, caCertificate, contextName, workspace, launcher, run);
    }
}
