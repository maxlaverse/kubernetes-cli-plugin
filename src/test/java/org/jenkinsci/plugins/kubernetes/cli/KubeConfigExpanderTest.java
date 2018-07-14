package org.jenkinsci.plugins.kubernetes.cli;

import hudson.EnvVars;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class KubeConfigExpanderTest {
    @Test
    public void testExpander() throws Exception {
        KubeConfigExpander expander = new KubeConfigExpander("a-file-path");
        EnvVars initialEnv = new EnvVars();
        expander.expand(initialEnv);
        assertEquals("a-file-path", initialEnv.get("KUBECONFIG"));
    }

    @Test
    public void testExpanderOverride() throws Exception {
        KubeConfigExpander expander = new KubeConfigExpander("a-file-path");
        EnvVars initialEnv = new EnvVars();
        initialEnv.put("KUBECONFIG", "a-wrong-path");
        expander.expand(initialEnv);
        assertEquals("a-file-path", initialEnv.get("KUBECONFIG"));
    }
}
