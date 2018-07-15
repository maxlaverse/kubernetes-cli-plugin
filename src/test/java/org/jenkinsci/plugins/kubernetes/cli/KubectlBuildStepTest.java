package org.jenkinsci.plugins.kubernetes.cli;

import com.cloudbees.hudson.plugins.folder.Folder;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.Domain;
import hudson.model.Result;
import org.jenkinsci.plugins.workflow.cps.CpsFlowDefinition;
import org.jenkinsci.plugins.workflow.job.WorkflowJob;
import org.jenkinsci.plugins.workflow.job.WorkflowRun;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

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
        CredentialsProvider.lookupStores(folder).iterator().next().addCredentials(Domain.global(), usernamePasswordCredential());

        WorkflowJob p = folder.createProject(WorkflowJob.class, "testScopedCredentials");
        p.setDefinition(new CpsFlowDefinition(loadResource("mockedKubectl.groovy"), true));
        WorkflowRun b = p.scheduleBuild2(0).waitForStart();
        assertNotNull(b);
        r.assertBuildStatusSuccess(r.waitForCompletion(b));
    }

    @Test
    public void testMissingScopedCredentials() throws Exception {
        Folder folder = new Folder(r.jenkins.getItemGroup(), "test-folder");
        CredentialsProvider.lookupStores(folder).iterator().next().addCredentials(Domain.global(), usernamePasswordCredential());

        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "testMissingScopedCredentials");
        p.setDefinition(new CpsFlowDefinition(loadResource("mockedKubectl.groovy"), true));
        WorkflowRun b = p.scheduleBuild2(0).waitForStart();
        assertNotNull(b);
        r.assertBuildStatus(Result.FAILURE, r.waitForCompletion(b));
        r.assertLogContains("ERROR: No credentials found for id \"cred1234\"", b);
    }

    @Test
    public void testSecretWithSpace() throws Exception {
        CredentialsProvider.lookupStores(r.jenkins).iterator().next().addCredentials(Domain.global(), secretCredentialWithSpace());

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
        CredentialsProvider.lookupStores(r.jenkins).iterator().next().addCredentials(Domain.global(), usernamePasswordCredentialWithSpace());

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
        CredentialsProvider.lookupStores(r.jenkins).iterator().next().addCredentials(Domain.global(), usernamePasswordCredentialWithSpace());

        WorkflowJob p = r.jenkins.createProject(WorkflowJob.class, "testUsernamePasswordWithSpace");
        p.setDefinition(new CpsFlowDefinition(loadResource("mockedKubectl.groovy"), true));
        WorkflowRun b = p.scheduleBuild2(0).waitForStart();
        assertNotNull(b);
        r.assertBuildStatusSuccess(r.waitForCompletion(b));
        r.assertLogContains("kubectl configuration cleaned up", b);
    }
}
