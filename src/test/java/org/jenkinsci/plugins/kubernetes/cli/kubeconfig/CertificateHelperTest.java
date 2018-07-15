package org.jenkinsci.plugins.kubernetes.cli.kubeconfig;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CertificateHelperTest {
    @Test
    public void testWrapCertificate() throws Exception {
        String wrapperCertificate = CertificateHelper.wrapCertificate("a-certificate");
        assertEquals("-----BEGIN CERTIFICATE-----\na-certificate\n-----END CERTIFICATE-----", wrapperCertificate);
    }

    @Test
    public void testWrapAlreadyWrappedCertificate() throws Exception {
        String wrapperCertificate = CertificateHelper.wrapCertificate("-----BEGIN CERTIFICATE-----\na-certificate\n-----END CERTIFICATE-----");
        assertEquals("-----BEGIN CERTIFICATE-----\na-certificate\n-----END CERTIFICATE-----", wrapperCertificate);
    }

    @Test
    public void testWrapPrivateKey() throws Exception {
        String wrapperCertificate = CertificateHelper.wrapPrivateKey("a-key");
        assertEquals("-----BEGIN PRIVATE KEY-----\na-key\n-----END PRIVATE KEY-----", wrapperCertificate);
    }

    @Test
    public void testWrapAlreadyWrappedPrivateKey() throws Exception {
        String wrapperCertificate = CertificateHelper.wrapPrivateKey("-----BEGIN PRIVATE KEY-----\na-key\n-----END PRIVATE KEY-----");
        assertEquals("-----BEGIN PRIVATE KEY-----\na-key\n-----END PRIVATE KEY-----", wrapperCertificate);
    }
}
