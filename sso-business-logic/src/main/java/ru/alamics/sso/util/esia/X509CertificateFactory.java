package ru.alamics.sso.util.esia;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMParser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

final public class X509CertificateFactory {

    private static final JcaX509CertificateConverter CONVERTER = new JcaX509CertificateConverter();

    static List<X509Certificate> generate(Path path) throws IOException, CertificateException {
        List<X509Certificate> certificates = new ArrayList<>();

        try (Reader in = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            PEMParser parser = new PEMParser(in);

            Object certificate;
            while ((certificate = parser.readObject()) != null) {
                if (!(certificate instanceof X509CertificateHolder)) {
                    throw new IllegalStateException(String.format("%s contains an artifact that is not a certificate: %s", path, certificate));
                }

                certificates.add(CONVERTER.getCertificate((X509CertificateHolder) certificate));
            }
        }

        return certificates;
    }


    public static X509CertificateHolder generateHolder(InputStream is) throws IOException, CertificateException {
        List<X509Certificate> certificates = new ArrayList<>();

        try (Reader in = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            PEMParser parser = new PEMParser(in);

            Object certificate;
            while ((certificate = parser.readObject()) != null) {
                if (!(certificate instanceof X509CertificateHolder)) {
                    throw new IllegalStateException(String.format("contains an artifact that is not a certificate:", certificate));
                }

                return (X509CertificateHolder) certificate;
            }
        }

        return null;
    }

}