package ru.alamics.sso.remote.call;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.ClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.internal.ClientInvocationBuilder;
import org.jboss.resteasy.plugins.providers.StringTextStar;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.registration.phone.exception.PhoneCallException;
import ru.alamics.sso.registration.phone.port.PhoneCallerRemoteService;
import ru.alamics.sso.util.Util;

import java.net.URI;
import java.util.concurrent.TimeUnit;

@ApplicationScoped
@Named("PhoneCallerService")
@Slf4j
public class PhoneCallerRemoteServiceImpl implements PhoneCallerRemoteService {

    private static final String URI_PERM = "phoneCaller.uri.perm";
    private static final String URI_VORONEZH = "phoneCaller.uri.voronezh";

    private static final ResteasyClientBuilder clientBuilder = ((ResteasyClientBuilder) ClientBuilder.newBuilder())
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS);

    private static final ResteasyClient client = clientBuilder.build();

    @Resource(lookup = "java:global/domru-sso/ApplicationProperties")
    private ApplicationProperties properties;

    private URI uriPerm;
    private URI uriVoronezh;

    @PostConstruct
    private void init() {
        try {
            uriPerm = URI.create(properties.getProperty(URI_PERM));
        } catch (Exception e) {
            log.error("Fail uriPerm initialize", e);
        }
        try {
            uriVoronezh = URI.create(properties.getProperty(URI_VORONEZH));
        } catch (Exception e) {
            log.error("Fail uriVoronezh initialize", e);
        }
    }

    @Override
    public String callAndGetCode(String phone, int count) throws PhoneCallException {

        String response = null;
        try {
            if (uriVoronezh != null) {
                response = getCode(uriVoronezh, Util.getCleanUserPhone(phone), count);
            }
            if (isNull(response)) {
                response = getCode(uriPerm, Util.getCleanUserPhone(phone), count);
            }
        } catch (BadRequestException e) {
            log.error("Error", e);
            if (HttpStatus.SC_BAD_REQUEST == e.getResponse().getStatus()) {
                throw new PhoneCallException("Incorrect phone number", e);
            }
        } catch (ProcessingException | WebApplicationException wae) {
            log.error("Error", wae);
            throw new PhoneCallException("Ошибка при выполнении звонка", wae);
        }

        if (isNull(response)) {
            log.error("Empty response");
            throw new PhoneCallException("Невозможно выполнить звонок");
        }
        return response;
    }

    private String getCode(URI uri, String phone, int count) {

        log.info(String.format("Api %s, call to number: %s, count: %d", uri.getHost(), phone, count));

        ClientInvocationBuilder builder = (ClientInvocationBuilder) client.register(StringTextStar.class)
                .target(uri)
                .queryParam("num", phone)
                .queryParam("retry", count)
                .request();

        String code = builder.get(String.class);
        code = code != null ? code.replaceAll("\\n", "") : "";

        log.info(String.format("Api %s, code %s", uri.getHost(), code));

        return code;
    }

    private boolean isNull(String field) {
        return field == null || field.isEmpty();
    }
}
