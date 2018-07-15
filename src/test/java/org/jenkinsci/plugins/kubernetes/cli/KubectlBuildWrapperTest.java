package org.jenkinsci.plugins.kubernetes.cli;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.domains.Domain;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import hudson.util.ListBoxModel;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Max Laverse
 */
public class KubectlBuildWrapperTest extends KubectlTestBase {

    @Rule
    public JenkinsRule r = new JenkinsRule();

    @Before
    public void addFakeSlave() throws Exception {
        r.jenkins.addNode(getFakeSlave(r));
        r.jenkins.setNumExecutors(0);
    }

    @Test
    public void testEnvVariablePresent() throws Exception {
        CredentialsProvider.lookupStores(r.jenkins).iterator().next().addCredentials(Domain.global(), secretCredential(CREDENTIAL_ID));

        FreeStyleProject p = r.createFreeStyleProject();

        KubectlBuildWrapper bw = new KubectlBuildWrapper();
        bw.credentialsId = CREDENTIAL_ID;
        p.getBuildWrappersList().add(bw);


        FreeStyleBuild b = p.scheduleBuild2(0).waitForStart();
        assertNotNull(b);
        r.assertBuildStatus(Result.SUCCESS, r.waitForCompletion(b));
        r.assertLogContains("KUBECONFIG=" + b.getWorkspace() + File.separator + ".kube", b);
    }

    @Test
    public void testKubeConfigDisposed() throws Exception {
        CredentialsProvider.lookupStores(r.jenkins).iterator().next().addCredentials(Domain.global(), secretCredential(CREDENTIAL_ID));

        FreeStyleProject p = r.createFreeStyleProject();

        KubectlBuildWrapper bw = new KubectlBuildWrapper();
        bw.credentialsId = CREDENTIAL_ID;
        p.getBuildWrappersList().add(bw);


        FreeStyleBuild b = p.scheduleBuild2(0).waitForStart();
        assertNotNull(b);
        r.assertBuildStatus(Result.SUCCESS, r.waitForCompletion(b));
        r.assertLogContains("kubectl configuration cleaned up", b);
    }

    @Test
    public void testListedCredentials() throws Exception {
        CredentialsStore store = CredentialsProvider.lookupStores(r.jenkins).iterator().next();
        store.addCredentials(Domain.global(), usernamePasswordCredential("1"));
        store.addCredentials(Domain.global(), secretCredential("2"));
        store.addCredentials(Domain.global(), fileCredential("3"));
        store.addCredentials(Domain.global(), certificateCredential("4"));
        store.addCredentials(Domain.global(), tokenCredential("5"));

        KubectlBuildWrapper.DescriptorImpl d = new KubectlBuildWrapper.DescriptorImpl();
        FreeStyleProject p = r.createFreeStyleProject();

        ListBoxModel s = d.doFillCredentialsIdItems(p.asItem(), "");
        assertEquals(6, s.size());
    }
}
