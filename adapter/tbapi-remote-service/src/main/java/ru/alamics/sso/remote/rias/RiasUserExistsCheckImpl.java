package ru.alamics.sso.remote.rias;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.registration.phone.HashGenerator;
import ru.alamics.sso.registration.rias.exception.RiasCheckException;
import ru.alamics.sso.registration.rias.port.RiasApiService;
import ru.alamics.sso.remote.rias.model.RiasCheckStatus;
import ru.alamics.sso.remote.rias.model.RiasData;
import ru.alamics.sso.util.Util;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Slf4j
@Stateless(name = "RiasApiService")
@NoArgsConstructor
public class RiasUserExistsCheckImpl implements RiasApiService {
    private static final String RIAS_API_URI = "riasApi.uri";
    private static final String CLIENT_NAME = "riasApi.client.name";
    private static final String CLIENT_SALT = "riasApi.client.salt";

    private static final ResteasyClientBuilder clientBuilder = new ResteasyClientBuilder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS);

    private static final ResteasyClient client = clientBuilder.build();

    @Resource(lookup = "java:global/domru-sso/ApplicationProperties")
    private ApplicationProperties properties;

    private URI uri;

    public RiasUserExistsCheckImpl(URI uri) {
        this.uri = uri;
    }

    public RiasUserExistsCheckImpl(ApplicationProperties properties, URI uri) {

        this.properties = properties;
        this.uri = uri;
    }

    @PostConstruct
    private void init() {
        uri = URI.create(properties.getProperty(RIAS_API_URI));
    }

    public boolean checkParam(String param) throws RiasCheckException {
        LocalDateTime dateTime = LocalDateTime.now();

        String timestamp = dateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        String clientSecret = param + timestamp + properties.getProperty(CLIENT_NAME) + properties.getProperty(CLIENT_SALT);

        String secretHash = HashGenerator.getSecretHash(clientSecret);

        String paramsV = "check_profile_data";
        String namesV = Util.encodeUTF8("data_for_check$c,timestamp,client,client_secret");
        String valuesV = Util.encodeUTF8(param + "," + timestamp + "," + properties.getProperty(CLIENT_NAME) + "," + secretHash);

        RiasData response;

        try {
            response = client.target(uri)
                    .queryParam("params", paramsV)
                    .queryParam("param_names_arr$c", namesV)
                    .queryParam("param_values_arr$c", valuesV)
                    .request(MediaType.APPLICATION_XML)
                    .get(RiasData.class);

            log.info("response result {}, status {}, message {}", response.getResult(), response.getStatus(), response.getMessages());

        } catch (ProcessingException | WebApplicationException wae) {
            throw new RiasCheckException(wae);
        }

        if (response.getStatus() == RiasCheckStatus.DATA_NOT_FOUND)
            throw new RiasCheckException(response.getMessages().getCode() + ": " + response.getMessages().getText());

        else if (response.getStatus() == RiasCheckStatus.DATA_FOUND)
            if (response.getResult().getCheckProfileData() == RiasCheckStatus.DATA_NOT_FOUND)
                return false;

            else if (response.getResult().getCheckProfileData() == RiasCheckStatus.DATA_FOUND)
                return true;

            else if (response.getResult().getCheckProfileData() == RiasCheckStatus.EMPTY_DATA_FOR_CHECK)
                throw new RiasCheckException("Checked data is empty: check_profile_data = " + response.getResult().getCheckProfileData());

            else if (response.getResult().getCheckProfileData() == RiasCheckStatus.INVALID_DATA_FOR_CHECK)
                throw new RiasCheckException("Checked data is invalid: check_profile_data = " + response.getResult().getCheckProfileData());

            else
                throw new RiasCheckException("Bad response data: check_profile_data = " + response.getResult().getCheckProfileData());

        else throw new RiasCheckException("Bad response status: status = " + response.getStatus());

    }
}
