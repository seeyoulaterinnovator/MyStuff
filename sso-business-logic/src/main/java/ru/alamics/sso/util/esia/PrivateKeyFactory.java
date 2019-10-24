package ru.alamics.sso.util.esia;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.PrivateKey;

final public class PrivateKeyFactory {
    private static final JcaPEMKeyConverter CONVERTER = new JcaPEMKeyConverter();

    static PrivateKey generate(Path path) throws IOException {
        try (Reader in = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            PEMParser parser = new PEMParser(in);

            Object keyPair = parser.readObject();
            if (!(keyPair instanceof PEMKeyPair)) {
                throw new IllegalStateException(String.format("%s contains an artifact that is not a key pair: %s", path, keyPair));
            }

            PrivateKeyInfo privateKeyInfo = ((PEMKeyPair) keyPair).getPrivateKeyInfo();
            if (privateKeyInfo == null) {
                throw new IllegalStateException(String.format("%s does not contain a private key", path));
            }

            return CONVERTER.getPrivateKey(privateKeyInfo);
        }
    }

    public static KeyPair generateKeyPair(InputStream is) throws IOException {
        try (Reader in = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            PEMParser parser = new PEMParser(in);

            Object keyPair = parser.readObject();
            if (!(keyPair instanceof PEMKeyPair)) {
                throw new IllegalStateException(String.format(" contains an artifact that is not a key pair: %s", keyPair));
            }

            return CONVERTER.getKeyPair((PEMKeyPair) keyPair);
        }
    }
}
