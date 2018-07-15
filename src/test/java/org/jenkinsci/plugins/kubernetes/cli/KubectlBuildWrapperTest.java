package org.jenkinsci.plugins.kubernetes.cli;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.Domain;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

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
    public void testFreeStyleProject() throws Exception {
        CredentialsProvider.lookupStores(r.jenkins).iterator().next().addCredentials(Domain.global(), secretCredential());

        FreeStyleProject p = r.createFreeStyleProject();

        KubectlBuildWrapper bw = new KubectlBuildWrapper();
        bw.credentialsId = secretCredential().getId();
        p.getBuildWrappersList().add(bw);


        FreeStyleBuild b = p.scheduleBuild2(0).waitForStart();
        assertNotNull(b);
        r.assertBuildStatus(Result.SUCCESS, r.waitForCompletion(b));
        r.assertLogContains("kubectl configuration cleaned up", b);
    }

}
