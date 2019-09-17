package ru.alamics.sso.remote.rias;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.specimpl.ResteasyUriBuilder;
import ru.alamics.sso.registration.phone.HashGenerator;
import ru.alamics.sso.registration.rias.exception.RiasCheckException;
import ru.alamics.sso.registration.rias.port.RiasApiService;
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

@Slf4j
@Stateless(name = "RiasApiService")
public class RiasUserExistsCheckImpl implements RiasApiService {

    private static final ResteasyClientBuilder clientBuilder = new ResteasyClientBuilder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS);

    private static final ResteasyClient client = clientBuilder.build();

    private final URI uri;

    public RiasUserExistsCheckImpl() {
        uri = new ResteasyUriBuilder()
                .scheme("https")
                .host("hq-dev.db.ertelecom.ru")
                .port(443)
                .path("/cgi-bin/ppo/excells/web_cabinet.get_info_unauth")
                .build();
    }

    public RiasUserExistsCheckImpl(URI uri) {
        this.uri = uri;
    }

    private static final String CLIENT_NAME = "SSO";
    private static final String CLIENT_SALT = "W2NHAYTWrfEG9fDw2MAt2TuuM7VK2K7H";

    public boolean checkParam(String param) throws RiasCheckException
    {
        LocalDateTime dateTime = LocalDateTime.now();

        String timestamp = dateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        String clientSecret = param + timestamp + CLIENT_NAME + CLIENT_SALT;

        String secretHash = HashGenerator.getSecretHash(clientSecret);

        String paramsV = "check_profile_data";
        String namesV = URLEncoder.encode("data_for_check$c,timestamp,client,client_secret", StandardCharsets.UTF_8);
        String valuesV = URLEncoder.encode(param + "," + timestamp + "," + CLIENT_NAME + "," + secretHash, StandardCharsets.UTF_8);


        // TODO Entity<RiasData> => response.close() ?
        RiasData response;

        try {
            response = client.target(uri)
                    .queryParam("params", paramsV)
                    .queryParam("param_names_arr$c", namesV)
                    .queryParam("param_values_arr$c", valuesV)
                    .request(MediaType.APPLICATION_XML)
                    .get(RiasData.class);

        } catch (ProcessingException | WebApplicationException wae) {
            throw new RiasCheckException(wae);
        }

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

    }
}
