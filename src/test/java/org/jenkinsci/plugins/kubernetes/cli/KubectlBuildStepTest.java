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
        p.setDefinition(new CpsFlowDefinition(loadResource("mockedKubectl.groovy"), true));
        WorkflowRun b = p.scheduleBuild2(0).waitForStart();
        assertNotNull(b);
        r.assertBuildStatusSuccess(r.waitForCompletion(b));
    }

    @Test
    public void testMissingScopedCredentials() throws Exception {
        Folder folder = new Folder(r.jenkins.getItemGroup(), "test-folder");
        CredentialsProvider.lookupStores(folder).iterator().next().addCredentials(Domain.global(), usernamePasswordCredential(CREDENTIAL_ID));

        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "testMissingScopedCredentials");
        p.setDefinition(new CpsFlowDefinition(loadResource("mockedKubectl.groovy"), true));
        WorkflowRun b = p.scheduleBuild2(0).waitForStart();
        assertNotNull(b);
        r.assertBuildStatus(Result.FAILURE, r.waitForCompletion(b));
        r.assertLogContains("ERROR: No credentials found for id \"cred1234\"", b);
    }

    @Test
    public void testSecretWithSpace() throws Exception {
        CredentialsProvider.lookupStores(r.jenkins).iterator().next().addCredentials(Domain.global(), secretCredentialWithSpace(CREDENTIAL_ID));

        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "testSecretWithSpace");
        p.setDefinition(new CpsFlowDefinition(loadResource("mockedKubectl.groovy"), true));
        WorkflowRun b = p.scheduleBuild2(0).waitForStart();
        assertNotNull(b);
        r.assertBuildStatusSuccess(r.waitForCompletion(b));
        r.assertLogNotContains("with-space", b);
        r.assertLogNotContains("s3cr3t", b);
    }

    @Test
    public void testUsernamePasswordWithSpace() throws Exception {
        CredentialsProvider.lookupStores(r.jenkins).iterator().next().addCredentials(Domain.global(), usernamePasswordCredentialWithSpace(CREDENTIAL_ID));

        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "testUsernamePasswordWithSpace");
        p.setDefinition(new CpsFlowDefinition(loadResource("mockedKubectl.groovy"), true));
        WorkflowRun b = p.scheduleBuild2(0).waitForStart();
        assertNotNull(b);
        r.assertBuildStatusSuccess(r.waitForCompletion(b));
        r.assertLogNotContains("with-passwordspace", b);
        r.assertLogNotContains("s3cr3t", b);
    }

    @Test
    public void testKubeConfigDisposed() throws Exception {
        CredentialsProvider.lookupStores(r.jenkins).iterator().next().addCredentials(Domain.global(), usernamePasswordCredentialWithSpace(CREDENTIAL_ID));

        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "testUsernamePasswordWithSpace");
        p.setDefinition(new CpsFlowDefinition(loadResource("mockedKubectl.groovy"), true));
        WorkflowRun b = p.scheduleBuild2(0).waitForStart();
        assertNotNull(b);
        r.assertBuildStatusSuccess(r.waitForCompletion(b));
        r.assertLogContains("kubectl configuration cleaned up", b);
    }

    @Test
    public void testCredentialNotProvided() throws Exception {
        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "testWithEmptyCredentials");
        p.setDefinition(new CpsFlowDefinition(loadResource("mockedKubectlWithEmptyCredential.groovy"), true));
        WorkflowRun b = p.scheduleBuild2(0).waitForStart();
        assertNotNull(b);
        r.assertBuildStatus(Result.FAILURE, r.waitForCompletion(b));
        r.assertLogContains("ERROR: No credentials defined to setup Kubernetes CLI", b);
    }

    @Test
    public void testUnsupportedCredential() throws Exception {
        CredentialsProvider.lookupStores(r.jenkins).iterator().next().addCredentials(Domain.global(), unsupportedCredential(CREDENTIAL_ID));

        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "testWithUnsupportedCredentials");
        p.setDefinition(new CpsFlowDefinition(loadResource("mockedKubectl.groovy"), true));
        WorkflowRun b = p.scheduleBuild2(0).waitForStart();
        assertNotNull(b);
        r.assertBuildStatus(Result.FAILURE, r.waitForCompletion(b));
        r.assertLogContains("ERROR: Unsupported Credentials type org.jenkinsci.plugins.kubernetes.cli.utils.UnsupportedCredential", b);
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
        p.setDefinition(new CpsFlowDefinition(loadResource("mockedKubectl.groovy"), true));
        WorkflowRun b = p.scheduleBuild2(0).waitForStart();
        assertNotNull(b);
        r.assertBuildStatus(Result.FAILURE, r.waitForCompletion(b));
        r.assertLogContains("ERROR: Uninitialized keystore", b);
    }
}
