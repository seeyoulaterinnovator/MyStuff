package ru.alamics.sso.remote.rias;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.jboss.resteasy.specimpl.ResteasyUriBuilder;
import ru.alamics.sso.registration.phone.HashGenerator;
import ru.alamics.sso.registration.rias.exception.RiasCheckException;
import ru.alamics.sso.remote.rias.model.RiasData;

import javax.ejb.Stateless;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

//@Slf4j
//@Stateless(name = "RiasLoginService")
public class RiasUserLoginImpl {

    private static final ResteasyClientBuilder clientBuilder = new ResteasyClientBuilder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS);

    private static final ResteasyClient client = clientBuilder.build();

    private final URI uri;

    public RiasUserLoginImpl() {
        uri = new ResteasyUriBuilder()
                .scheme("https")
                .host("testing2.db.ertelecom.ru")
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

    public static boolean loginUser(String username, String password) throws RiasCheckException
    {
        System.out.println("qwer");

        LocalDateTime dateTime = LocalDateTime.now();

        String timestamp = dateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        String clientSecret = CLIENT_NAME + GRANT_TYPE + username + password + timestamp + CLIENT_SALT;

        String secretHash = HashGenerator.getSecretHashMD5(clientSecret);

        String usernameV = URLEncoder.encode(username, StandardCharsets.UTF_8);

        // TODO Entity<RiasData> => response.close() ?
        String response;

        URI uri = new ResteasyUriBuilder()
                .scheme("https")
                .host("perm-dev.db.ertelecom.ru")
                .port(443)
                .path("/cgi-bin/ppo/es_webface/open_auth.authorize_password")
                .build();

        try {
            ResteasyWebTarget wt = client.target(uri)
                    .queryParam("client_id", CLIENT_NAME)
                    .queryParam("grant_type", GRANT_TYPE)
                    .queryParam("username", usernameV)
                    .queryParam("timestamp$c", timestamp)
                    .queryParam("client_secret", secretHash);

            System.out.println(wt.getUri());

            response = wt.request(MediaType.APPLICATION_XML)
                    .get(String.class);

        } catch (ProcessingException | WebApplicationException wae) {
            throw new RiasCheckException(wae);
        }

        //log.info("login response: " + response);
        System.out.println("login response: " + response);

        /*
        if (response.getStatus() == 0)
            throw new RiasCheckException(response.getMessages().getCode() + ": " + response.getMessages().getText());

        else if (response.getStatus() == 1)
            if (response.getResult().getCheckProfileData() == 0)
                return false;

            else if (response.getResult().getCheckProfileData() == 1)
                return true;

            else if (response.getResult().getCheckProfileData() == -1)
                throw new RiasCheckException("Checked data is empty: check_profile_data = " + response.getResult().getCheckProfileData());

            else if (response.getResult().getCheckProfileData() == -2)
                throw new RiasCheckException("Checked data is invalid: check_profile_data = " + response.getResult().getCheckProfileData());

            else
                throw new RiasCheckException("Bad response data: check_profile_data = " + response.getResult().getCheckProfileData());

        else throw new RiasCheckException("Bad response status: status = " + response.getStatus());
        */


        return false;
    }

    public static void main(String[] args) {

        try {
            loginUser("70000004129", "4129");
        } catch (RiasCheckException e) {
            e.printStackTrace();
        }
    }
}
