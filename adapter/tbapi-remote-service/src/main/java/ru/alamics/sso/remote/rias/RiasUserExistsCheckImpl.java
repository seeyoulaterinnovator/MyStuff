package ru.alamics.sso.remote.rias;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Named;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.registration.phone.HashGenerator;
import ru.alamics.sso.registration.rias.exception.RiasCheckException;
import ru.alamics.sso.registration.rias.port.RiasApiService;
import ru.alamics.sso.remote.rias.model.RiasCheckStatus;
import ru.alamics.sso.remote.rias.model.RiasData;
import ru.alamics.sso.util.Util;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.InputStream;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
@Named("RiasApiService")
@Slf4j
public class RiasUserExistsCheckImpl implements RiasApiService {
    private static final String RIAS_API_URI = "riasApi.uri";
    private static final String CLIENT_NAME = "riasApi.client.name";
    private static final String CLIENT_SALT = "riasApi.client.salt";

    private final XmlMapper mapper = new XmlMapper();

    private final ApplicationProperties properties;

    private final HttpClient client;

    private final URI uri;

    public RiasUserExistsCheckImpl(
            ApplicationProperties properties,
            @Named("riasApiSSLContext") SSLContext sslContext,
            @Named("riasApiHostnameVerifier") HostnameVerifier hostnameVerifier
    ) {
        this.properties = properties;
        client = HttpClients.custom()
                .setSSLContext(sslContext)
                .setSSLHostnameVerifier(hostnameVerifier)
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setConnectTimeout(3_000)
                        .setConnectionRequestTimeout(3_000)
                        .setSocketTimeout(10_000)
                        .build())
                .build();
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

        HttpGet request = new HttpGet(UriBuilder.fromUri(uri)
                .queryParam("params", paramsV)
                .queryParam("param_names_arr$c", namesV)
                .queryParam("param_values_arr$c", valuesV)
                .build());
        request.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML);

        RiasData response;
        try {
            HttpResponse httpResponse = client.execute(request);
            try {
                if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    try (InputStream stream = httpResponse.getEntity().getContent()) {
                        response = mapper.readValue(stream, RiasData.class);
                        log.info("response result {}, status {}, message {}", response.getResult(), response.getStatus(), response.getMessages());
                    }
                } else {
                    throw new RiasCheckException(httpResponse.getStatusLine().getReasonPhrase());
                }
            } finally {
                EntityUtils.consume(httpResponse.getEntity());
            }
        } catch (Exception e) {
            throw new RiasCheckException(e);
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
