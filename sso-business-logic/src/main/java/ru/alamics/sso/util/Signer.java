package ru.alamics.sso.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSTypedData;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import ru.alamics.sso.util.esia.PrivateKeyFactory;
import ru.alamics.sso.util.esia.X509CertificateFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class Signer {
    private static KeyPair kp;
    private static X509CertificateHolder certHolder;

    private static X509CertificateHolder getCert() {
        return Signer.certHolder;
    }

    private static PrivateKey getPrivateKey() {
        return Signer.kp.getPrivate();
    }

    public static String signString(String data) {
        if (kp == null || certHolder == null)
            initKeys();
        String encoded = null;
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        List<X509CertificateHolder> certList = new ArrayList<>();
        CMSTypedData msg = new CMSProcessableByteArray(data.getBytes());

        certList.add(getCert()); // Adding the X509 Certificate
        try {
            JcaCertStore certs = new JcaCertStore(certList);

            CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
            // Initializing the the BC's Signer
            ContentSigner shaSigner = new JcaContentSignerBuilder("SHA256withRSA")
                    .setProvider("BC").build(getPrivateKey());

            gen.addSignerInfoGenerator(new JcaSignerInfoGeneratorBuilder(
                    new JcaDigestCalculatorProviderBuilder().setProvider("BC")
                            .build()).build(shaSigner, getCert()));

            gen.addCertificates(certs);

            CMSSignedData sigData = gen.generate(msg, false);
            sigData.getSignerInfos();

            encoded = Base64.encodeBase64URLSafeString(sigData.getEncoded());
        } catch (Exception e) {
            log.info("error sign string{} {}", data, e.toString());
        }

        return encoded;

    }

//    private static final Path keyPath = Paths.get(Signer.class.getClassLoader().getResource("cert/cert.key").getFile()); //путь к приватоному ключу ЕСИА
//    private static final Path certPath = Paths.get(Signer.class.getClassLoader().getResource("cert/cert.csr").getFile()); //путь к сертификату ЕСИА

    private static synchronized void initKeys() {
        try {
            InputStream privatePath = Signer.class.getClassLoader().getResourceAsStream("cert/private.pem");
            InputStream publicPath = Signer.class.getClassLoader().getResourceAsStream("cert/public.pem");
//            Path keyPath = Paths.get(privatePath); //путь к приватоному ключу ЕСИА
//            Path certPath = Paths.get(publicPath); //путь к сертификату ЕСИА

            kp = PrivateKeyFactory.generateKeyPair(privatePath);
            certHolder = X509CertificateFactory.generateHolder(publicPath);
        } catch (IOException | CertificateException e) {
            log.error(e.getMessage());
        }
    }
}
