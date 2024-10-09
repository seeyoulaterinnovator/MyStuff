package ru.alamics.sso.config;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.property.ApplicationProperties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Stream;

@Dependent
@Slf4j
public class SslConfig {
    @Inject
    ApplicationProperties applicationProperties;

    @Produces
    @Named("rias")
    public SSLContext getRiasContext() throws Exception {
        return getContext("rias.ssl.relaxed", "rias.ssl.thumbprints");
    }

    @Produces
    @Named("riasLogin")
    public SSLContext getRiasLoginContext() throws Exception {
        return getContext("riasLogin.ssl.relaxed", "riasLogin.ssl.thumbprints");
    }

    @Produces
    @Named("tbapiRegistration")
    public SSLContext getTbapiRegistrationContext() throws Exception {
        return getContext("tbapi.registration.ssl.relaxed", "tbapi.registration.ssl.thumbprints");
    }

    @Produces
    @Named("tbapiCustomer")
    public SSLContext getTbapiCustomerContext() throws Exception {
        return getContext("tbapi.customer.ssl.relaxed", "tbapi.customer.ssl.thumbprints");
    }

    SSLContext getContext(String relaxedProperty, String thumbprintsProperty) throws Exception {
        var context = SSLContext.getInstance("SSL");
        context.init(
                null,
                new TrustManager[] { getTrustManager(relaxedProperty, thumbprintsProperty) },
                new SecureRandom()
        );
        return context;
    }

    TrustManager getTrustManager(String relaxedProperty, String thumbprintsProperty) throws Exception {
        var trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init((KeyStore) null);

        var trustManagers = Arrays.stream(trustManagerFactory.getTrustManagers())
                .filter(X509TrustManager.class::isInstance)
                .map(X509TrustManager.class::cast)
                .toList();

        return new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String type) {
                throw new UnsupportedOperationException();
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String type) throws CertificateException {
                if(!Boolean.TRUE.toString().equals(applicationProperties.getProperty(relaxedProperty))) {
                    if(!checkTrusted(chain, getThumbprints(thumbprintsProperty))) {
                        for (X509TrustManager trustManager : trustManagers) {
                            trustManager.checkServerTrusted(chain, type);
                        }
                    }
                }
            }
        };
    }

    List<String> getThumbprints(String thumbprintsProperty) {
        var value = applicationProperties.getProperty(thumbprintsProperty);
        if(value == null || value.isBlank()) {
            return List.of();
        } else {
            return Stream.of(value.split("[,;]")).map(String::trim).map(String::toLowerCase).toList();
        }
    }

    boolean checkTrusted(X509Certificate[] chain, List<String> trustedThumbprints) throws CertificateException{
        if(chain == null || chain.length == 0) throw new CertificateException();

        try {
            var thumbprint = getThumbprint(chain[0]);
            for(var trustedThumbprint : trustedThumbprints) {
                if(thumbprint.equals(trustedThumbprint)) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.debug(e.getMessage(), e);
        }

        return false;
    }

    @SneakyThrows({NoSuchAlgorithmException.class, CertificateEncodingException.class})
    String getThumbprint(Certificate certificate) {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(certificate.getEncoded()));
    }
}
