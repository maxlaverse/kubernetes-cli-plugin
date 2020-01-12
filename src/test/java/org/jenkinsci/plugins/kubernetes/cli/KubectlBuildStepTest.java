package org.jenkinsci.plugins.kubernetes.cli;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.CredentialsStore;
import com.cloudbees.plugins.credentials.domains.Domain;
import hudson.model.Result;
import hudson.util.ListBoxModel;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Max Laverse
 */
public class KubectlBuildStepTest extends KubectlTestBase {

    @Rule
    public JenkinsRule r = new JenkinsRule();

    @Before
    public void addFakeSlave() throws Exception {
        r.jenkins.addNode(getFakeSlave(r));
        r.jenkins.setNumExecutors(0);
    }

    @Test
    public void testScopedCredentials() throws Exception {
        Folder folder = new Folder(r.jenkins.getItemGroup(), "test-folder");
        CredentialsProvider.lookupStores(folder).iterator().next().addCredentials(Domain.global(), usernamePasswordCredential(CREDENTIAL_ID));

        WorkflowJob p = folder.createProject(WorkflowJob.class, "testScopedCredentials");
        p.setDefinition(new CpsFlowDefinition(loadResource("kubectlMocked.groovy"), true));
        WorkflowRun b = p.scheduleBuild2(0).waitForStart();
        assertNotNull(b);
        waitForResult(b, Result.SUCCESS);
    }

    @Test
    public void testMissingScopedCredentials() throws Exception {
        Folder folder = new Folder(r.jenkins.getItemGroup(), "test-folder");
        CredentialsProvider.lookupStores(folder).iterator().next().addCredentials(Domain.global(), usernamePasswordCredential(CREDENTIAL_ID));

        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "testMissingScopedCredentials");
        p.setDefinition(new CpsFlowDefinition(loadResource("kubectlMocked.groovy"), true));
        WorkflowRun b = p.scheduleBuild2(0).waitForStart();
        assertNotNull(b);
        waitForResult(b, Result.FAILURE);
        r.assertLogContains("ERROR: Unable to find credentials with id 'cred1234'", b);
    }

    @Test
    public void testSecretWithSpace() throws Exception {
        CredentialsProvider.lookupStores(r.jenkins).iterator().next().addCredentials(Domain.global(), secretCredentialWithSpace(CREDENTIAL_ID));

        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "testSecretWithSpace");
        p.setDefinition(new CpsFlowDefinition(loadResource("kubectlMocked.groovy"), true));
        WorkflowRun b = p.scheduleBuild2(0).waitForStart();
        assertNotNull(b);
        waitForResult(b, Result.SUCCESS);
        r.assertLogNotContains("with-space", b);
        r.assertLogNotContains("s3cr3t", b);
    }

    @Test
    public void testUsernamePasswordWithSpace() throws Exception {
        CredentialsProvider.lookupStores(r.jenkins).iterator().next().addCredentials(Domain.global(), usernamePasswordCredentialWithSpace(CREDENTIAL_ID));

        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "testUsernamePasswordWithSpace");
        p.setDefinition(new CpsFlowDefinition(loadResource("kubectlMocked.groovy"), true));
        WorkflowRun b = p.scheduleBuild2(0).waitForStart();
        assertNotNull(b);
        waitForResult(b, Result.SUCCESS);
        r.assertLogNotContains("with-passwordspace", b);
        r.assertLogNotContains("s3cr3t", b);
    }

    @Test
    public void testKubeConfigDisposed() throws Exception {
        CredentialsProvider.lookupStores(r.jenkins).iterator().next().addCredentials(Domain.global(), usernamePasswordCredentialWithSpace(CREDENTIAL_ID));

        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "testCleanupOnFailure");
        p.setDefinition(new CpsFlowDefinition(loadResource("kubectlMockedFailure.groovy"), true));
        WorkflowRun b = p.scheduleBuild2(0).waitForStart();
        assertNotNull(b);
        waitForResult(b, Result.FAILURE);
        r.assertLogContains("kubectl configuration cleaned up", b);
    }

    @Test
    public void testCredentialNotProvided() throws Exception {
        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "testWithEmptyCredentials");
        p.setDefinition(new CpsFlowDefinition(loadResource("kubectlMockedWithEmptyCredential.groovy"), true));
        WorkflowRun b = p.scheduleBuild2(0).waitForStart();
        assertNotNull(b);
        waitForResult(b, Result.FAILURE);
        r.assertLogContains("ERROR: Unable to find credentials with id ''", b);
    }

    @Test
    public void testUnsupportedCredential() throws Exception {
        CredentialsProvider.lookupStores(r.jenkins).iterator().next().addCredentials(Domain.global(), unsupportedCredential(CREDENTIAL_ID));

        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "testWithUnsupportedCredentials");
        p.setDefinition(new CpsFlowDefinition(loadResource("kubectlMocked.groovy"), true));
        WorkflowRun b = p.scheduleBuild2(0).waitForStart();
        assertNotNull(b);
        waitForResult(b, Result.FAILURE);
        r.assertLogContains("ERROR: Unsupported credentials type org.jenkinsci.plugins.kubernetes.cli.utils.UnsupportedCredential", b);
    }

    @Test
    public void testListedCredentials() throws Exception {
        CredentialsStore store = CredentialsProvider.lookupStores(r.jenkins).iterator().next();
        store.addCredentials(Domain.global(), usernamePasswordCredential("1"));
        store.addCredentials(Domain.global(), secretCredential("2"));
        store.addCredentials(Domain.global(), fileCredential("3"));
        store.addCredentials(Domain.global(), certificateCredential("4"));
        store.addCredentials(Domain.global(), tokenCredential("5"));

        KubectlBuildStep.DescriptorImpl d = new KubectlBuildStep.DescriptorImpl();
        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "testUsernamePasswordWithSpace");

        ListBoxModel s = d.doFillCredentialsIdItems(p.asItem(), "");
        assertEquals(6, s.size());
    }

    @Test
    public void testInvalidCertificate() throws Exception {
        CredentialsProvider.lookupStores(r.jenkins).iterator().next().addCredentials(Domain.global(), brokenCertificateCredential(CREDENTIAL_ID));

        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "testWithBrokenCertificate");
        p.setDefinition(new CpsFlowDefinition(loadResource("kubectlMocked.groovy"), true));
        WorkflowRun b = p.scheduleBuild2(0).waitForStart();
        assertNotNull(b);
        waitForResult(b, Result.FAILURE);
        r.assertLogContains("ERROR: Uninitialized keystore", b);
    }

    @Test
    public void testPlainKubeConfig() throws Exception {
        CredentialsProvider.lookupStores(r.jenkins).iterator().next().addCredentials(Domain.global(), fileCredential(CREDENTIAL_ID));

        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "testWithFileCertificateAndClusterName");
        p.setDefinition(new CpsFlowDefinition(loadResource("kubectlMockedWithCluster.groovy"), true));
        WorkflowRun b = p.scheduleBuild2(0).waitForStart();
        assertNotNull(b);
        waitForResult(b, Result.SUCCESS);
        r.assertLogNotContains("kubectl, config, set-cluster", b);
    }

    private void waitForResult(WorkflowRun b, Result result) throws Exception {
        r.assertBuildStatus(result, r.waitForCompletion(b));

        // Because else Jenkins would not always show all the logs
        Thread.sleep(1000);
    }

}
