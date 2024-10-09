package ru.alamics.sso.config;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.property.ApplicationProperties;

import javax.net.ssl.*;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
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
    public HostnameVerifier getHostnameVerifier() {
        var hostnameVerifier = HttpsURLConnection.getDefaultHostnameVerifier();
        return (hostname, session) -> {
            try {
                String thumbprint = getThumbprint(session.getPeerCertificates()[0]);
                if(Arrays.stream(SslContextKind.values()).anyMatch(kind -> getThumbprints(kind).contains(thumbprint))) {
                    return true;
                }
            } catch (Exception e) {
                log.debug(e.getMessage(), e);
            }
            return hostnameVerifier.verify(hostname, session);
        };
    }

    @Produces
    @Named("rias")
    public SSLContext getRiasContext() throws Exception {
        return getContext(SslContextKind.RIAS);
    }

    @Produces
    @Named("riasLogin")
    public SSLContext getRiasLoginContext() throws Exception {
        return getContext(SslContextKind.RIAS_LOGIN);
    }

    @Produces
    @Named("tbapiRegistration")
    public SSLContext getTbapiRegistrationContext() throws Exception {
        return getContext(SslContextKind.TBAPI_REGISTRATION);
    }

    @Produces
    @Named("tbapiCustomer")
    public SSLContext getTbapiCustomerContext() throws Exception {
        return getContext(SslContextKind.TBAPI_CUSTOMER);
    }

    @Produces
    @Named("cities")
    public SSLContext getCitiesContext() throws Exception {
        return getContext(SslContextKind.CITIES);
    }

    @Produces
    @Named("dadata")
    public SSLContext getDaDataContext() throws Exception {
        return getContext(SslContextKind.DA_DATA);
    }

    @Produces
    @Named("smsSender")
    public SSLContext getSmsSenderContext() throws Exception {
        return getContext(SslContextKind.SMS_SENDER);
    }

    SSLContext getContext(SslContextKind kind) throws Exception {
        var context = SSLContext.getInstance("SSL");
        context.init(null, new TrustManager[] { getTrustManager(kind) }, new SecureRandom());
        return context;
    }

    TrustManager getTrustManager(SslContextKind kind) throws Exception {
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
                if(!Boolean.TRUE.toString().equals(applicationProperties.getProperty(kind.getRelaxedProperty()))) {
                    if(!checkTrusted(chain, getThumbprints(kind))) {
                        for (X509TrustManager trustManager : trustManagers) {
                            trustManager.checkServerTrusted(chain, type);
                        }
                    }
                }
            }
        };
    }

    List<String> getThumbprints(SslContextKind kind) {
        var value = applicationProperties.getProperty(kind.getThumbprintsProperty());
        if(value == null || value.isBlank()) {
            return List.of();
        } else {
            return Stream.of(value.split("[,;]")).map(String::trim).map(String::toLowerCase).toList();
        }
    }

    boolean checkTrusted(X509Certificate[] chain, List<String> trustedThumbprints) throws CertificateException {
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
