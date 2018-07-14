package org.jenkinsci.plugins.kubernetes.cli.kubeconfig;

import com.cloudbees.plugins.credentials.common.StandardCertificateCredentials;
import hudson.AbortException;
import hudson.FilePath;
import hudson.util.Secret;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

/**
 * @author Max Laverse
 */
public abstract class CertificateHelper {
    private static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";
    private static final String END_CERTIFICATE = "-----END CERTIFICATE-----";
    private static final String BEGIN_PRIVATE_KEY = "-----BEGIN PRIVATE KEY-----";
    private static final String END_PRIVATE_KEY = "-----END PRIVATE KEY-----";

    /**
     * Extract the private key a client certificate from a X509 certificate and write them to disk.
     *
     * @param certificatCredential Jenkins certificateCredential
     * @param clientCrtFile        path where to write of the certificate
     * @param clientKeyFile        path where to write of the private key
     * @throws IOException          lol
     * @throws InterruptedException on file operation
     */
    public static void extractFromCertificate(StandardCertificateCredentials certificatCredential,
                                              FilePath clientCrtFile,
                                              FilePath clientKeyFile) throws IOException, InterruptedException {
        try {
            KeyStore keyStore = certificatCredential.getKeyStore();
            String alias = keyStore.aliases().nextElement();
            X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);

            // Get private key using passphrase
            Key key = keyStore.getKey(alias, Secret.toString(certificatCredential.getPassword()).toCharArray());

            // Write certificate
            String encodedClientCrt = wrapCertificate(Base64.encodeBase64String(certificate.getEncoded()));
            clientCrtFile.write(encodedClientCrt, null);

            // Write private key
            String encodedClientKey = wrapPrivateKey(Base64.encodeBase64String(key.getEncoded()));
            clientKeyFile.write(encodedClientKey, null);
        } catch (KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException | CertificateEncodingException e) {
            throw new AbortException(e.getMessage());
        }
    }

    public static String wrapPrivateKey(String encodedBody) {
        return wrapWithMarker(BEGIN_PRIVATE_KEY, END_PRIVATE_KEY, encodedBody);
    }

    public static String wrapCertificate(String encodedBody) {
        return wrapWithMarker(BEGIN_CERTIFICATE, END_CERTIFICATE, encodedBody);
    }

    private static String wrapWithMarker(String begin, String end, String encodedBody) {
        if (encodedBody.startsWith(begin)) {
            return encodedBody;
        }
        return new StringBuilder(begin).append("\n")
                .append(encodedBody).append("\n")
                .append(end)
                .toString();
    }
}
