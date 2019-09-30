package ru.alamics.sso.remote.call;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.internal.ClientInvocationBuilder;
import org.jboss.resteasy.plugins.providers.StringTextStar;
import org.jboss.resteasy.specimpl.ResteasyUriBuilder;
import ru.alamics.sso.registration.phone.exception.PhoneCallException;
import ru.alamics.sso.registration.phone.port.PhoneCallerRemoteService;
import ru.alamics.sso.util.Util;

import javax.ejb.Stateless;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Slf4j
@Stateless(name = "PhoneCaller")
public class PhoneCallerRemoteServiceImpl implements PhoneCallerRemoteService {

    private static final String LOGIN_SSO_CALL = "LoGSsOIn:1LoGSsOPaSsERTH777@";
    private static final String host_perm = LOGIN_SSO_CALL + "call-auth.cc-perm.ertelecom.ru";
    private static final String host_voronezh = LOGIN_SSO_CALL + "call-auth.cc-voronezh.ertelecom.ru";

    private static final ResteasyClientBuilder clientBuilder = new ResteasyClientBuilder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS);

    private static final ResteasyClient client = clientBuilder.build();

    private static final URI uriVoronezh = new ResteasyUriBuilder()
            .scheme("http")
            .host(host_voronezh)
            .path("sso_call.php")
            .build();
    private static final URI uriPerm = new ResteasyUriBuilder()
            .scheme("http")
            .host(host_perm)
            .path("sso_call.php")
            .build();

    @Override
    public String call(String phone, int count) throws PhoneCallException {

        String response = null;
        try {
            // TODO почему то не звонил воронеж
            //response = getCode(uriVoronezh, phone, count);
            //if (isNull(response)) {
                response = getCode(uriPerm, Util.getCleanUserPhone(phone), count);
            //}
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
        return field == null || field.isBlank();
    }
}
