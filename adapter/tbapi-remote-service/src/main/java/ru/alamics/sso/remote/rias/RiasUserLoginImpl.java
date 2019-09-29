package ru.alamics.sso.remote.rias;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.specimpl.ResteasyUriBuilder;
import ru.alamics.sso.registration.phone.HashGenerator;
import ru.alamics.sso.registration.rias.exception.RiasCheckException;
import ru.alamics.sso.registration.rias.model.RiasLogin;
import ru.alamics.sso.registration.rias.port.RiasLoginService;

import javax.ejb.Stateless;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Slf4j
@Stateless(name = "RiasLoginService")
public class RiasUserLoginImpl implements RiasLoginService {

    private static final ResteasyClientBuilder clientBuilder = new ResteasyClientBuilder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS);

    private static final ResteasyClient client = clientBuilder.build();

    private final URI uri;

    public RiasUserLoginImpl() {
        uri = new ResteasyUriBuilder()
                .scheme("https")
                .host("perm-dev.db.ertelecom.ru")
                .port(443)
                .path("/cgi-bin/ppo/es_webface/open_auth.authorize_password")
                .build();
    }

    public RiasUserLoginImpl(URI uri) {
        this.uri = uri;
    }

    private static final String CLIENT_NAME = "SSO";
    private static final String CLIENT_SALT = "W2NHAYTWrfEG9fDw2MAt2TuuM7VK2K7H";

    private static final String GRANT_TYPE = "password";

    public RiasLogin loginUser(String username, String password) throws RiasCheckException
    {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        String clientSecret = CLIENT_NAME + GRANT_TYPE + username + password + timestamp + CLIENT_SALT;
        String secretHash = HashGenerator.getSecretHashMD5(clientSecret);

        String usernameV = URLEncoder.encode(username, StandardCharsets.UTF_8);

        Response response = null;
        RiasLogin result = null;

        // {"refresh_token":"0000022d-703b1793-cea5-8e2a-e053-4794c26df2c1", "access_token":"m6qj36yhmrmm3xu6p067fu9h0aw8h2"}
        // {"error":"INVALID_CLIENT", "error_description":"Не найден договор"}
        // {"error":"INVALID_CLIENT", "error_description":"Пожалуйста, проверьте правильность введенных данных: логина, пароля и города"}
        // {"error":"UNAUTHORIZED_CLIENT", "error_description":"Данный тип авторизации не поддерживается для заданного клиента."}

        try {
            ResteasyWebTarget wt = client.target(uri)
                    .queryParam("client_id", CLIENT_NAME)
                    .queryParam("grant_type", GRANT_TYPE)
                    .queryParam("username", usernameV)
                    .queryParam("timestamp$c", timestamp)
                    .queryParam("client_secret", secretHash);

            //System.out.println(wt.getUri());

            response = wt.request(MediaType.APPLICATION_XML)
                    .get();

            result = response.readEntity(RiasLogin.class);

        } catch (ProcessingException | WebApplicationException wae) {
            throw new RiasCheckException(wae);
        } finally {
            if (response != null)
                response.close();
        }

        log.info("login response: " + response);
        //System.out.println("login response: " + result);

        return result;
    }
}
