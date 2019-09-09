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

import javax.ejb.Stateless;
import javax.ws.rs.BadRequestException;
import java.net.URI;
import java.util.regex.Pattern;

@Slf4j
@Stateless(name = "PhoneCaller")
public class PhoneCallerRemoteServiceImpl implements PhoneCallerRemoteService {
    private static final String LOGIN_SSO_CALL = "LoGSsOIn:1LoGSsOPaSsERTH777@";
    private static final String host_perm = LOGIN_SSO_CALL + "call-auth.cc-perm.ertelecom.ru";
    private static final String host_voronezh = LOGIN_SSO_CALL + "call-auth.cc-voronezh.ertelecom.ru";
    private final ResteasyClient client = new ResteasyClientBuilder().build();
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
        phone = phone.replaceAll("[^\\d]", "");
        try {
            response = getCode(uriVoronezh, phone, count);
            if (isNull(response)) {
                response = getCode(uriPerm, phone, count);
            }
        }catch (BadRequestException e) {
            if (HttpStatus.SC_BAD_REQUEST == e.getResponse().getStatus()) {
                throw new PhoneCallException("Incorrect phone number");
            }
        }

        if (isNull(response)) {
            throw new PhoneCallException("Невозможно выполнить звонок");
        }
        return response;
    }

    private String getCode(URI uri, String phone, int count) {
        log.info(String.format("call to number: %s, count: %d", phone, count));

        ClientInvocationBuilder builder = (ClientInvocationBuilder) client.register(StringTextStar.class)
                .target(uri)
                .queryParam("num", phone)
                .queryParam("retry", count)
                .request();
        String code = builder.get(String.class);
        return code != null ? code.replaceAll("\\n", "") : "";
    }

    private boolean isNull(String field) {
        return field == null || field.isBlank();
    }
}
