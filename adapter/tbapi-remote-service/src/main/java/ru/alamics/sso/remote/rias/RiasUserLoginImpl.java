package ru.alamics.sso.remote.rias;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import jakarta.annotation.Resource;
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
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.registration.phone.HashGenerator;
import ru.alamics.sso.registration.rias.exception.RiasCheckException;
import ru.alamics.sso.registration.rias.model.RiasLogin;
import ru.alamics.sso.registration.rias.port.RiasLoginService;
import ru.alamics.sso.util.Util;

import java.io.InputStream;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ApplicationScoped
@Named("RiasLoginService")
@Slf4j
public class RiasUserLoginImpl implements RiasLoginService {

    private static final HttpClient client;
    static {
        try {
            client = HttpClients.custom()
                    .setDefaultRequestConfig(RequestConfig.custom()
                            .setConnectTimeout(3_000)
                            .setSocketTimeout(10_000)
                            .build())
                    .build();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static final XmlMapper mapper = new XmlMapper();

    private static final String AUTH_SCHEME = "riasLogin.scheme";
    private static final String AUTH_DEF_CITY = "riasLogin.defCity";
    private static final String AUTH_DOMAIN = "riasLogin.domain";
    private static final String AUTH_PORT = "riasLogin.port";
    private static final String AUTH_PATH = "riasLogin.path";
    private static final String CLIENT_NAME = "riasLogin.client.name";
    private static final String CLIENT_SALT = "riasLogin.client.salt";
    private static final String GRANT_TYPE = "riasLogin.grantType";
    @Resource(lookup = "java:global/domru-sso/ApplicationProperties")
    private ApplicationProperties properties;

    public RiasUserLoginImpl() {
    }

    public RiasLogin loginUser(String domain, String username, String password) throws RiasCheckException {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        String clientSecret = properties.getProperty(CLIENT_NAME) + properties.getProperty(GRANT_TYPE) + username + password + timestamp +
                properties.getProperty(CLIENT_SALT);
        String secretHash = HashGenerator.getSecretHashMD5(clientSecret);

        String usernameV = Util.encodeUTF8(username);

        RiasLogin result;

        // {"refresh_token":"0000022d-703b1793-cea5-8e2a-e053-4794c26df2c1", "access_token":"m6qj36yhmrmm3xu6p067fu9h0aw8h2"}
        // {"error":"INVALID_CLIENT", "error_description":"Не найден договор"}
        // {"error":"INVALID_CLIENT", "error_description":"Пожалуйста, проверьте правильность введенных данных: логина, пароля и города"}
        // {"error":"UNAUTHORIZED_CLIENT", "error_description":"Данный тип авторизации не поддерживается для заданного клиента."}

        try {
            URI uri = UriBuilder.newInstance()
                    .scheme(properties.getProperty(AUTH_SCHEME))
                    .host(String.format("%s.%s", domain == null ? properties.getProperty(AUTH_DEF_CITY) : domain, properties.getProperty(AUTH_DOMAIN)))
                    .port(properties.getPropertyInt(AUTH_PORT))
                    .path(properties.getProperty(AUTH_PATH))
                    .build();

            HttpGet request = new HttpGet(UriBuilder.fromUri(uri)
                    .queryParam("client_id", properties.getProperty(CLIENT_NAME))
                    .queryParam("grant_type", properties.getProperty(GRANT_TYPE))
                    .queryParam("username", usernameV)
                    .queryParam("timestamp$c", timestamp)
                    .queryParam("client_secret", secretHash)
                    .build());
            request.setHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML);

            HttpResponse response = client.execute(request);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                try(InputStream stream = response.getEntity().getContent()) {
                    result = mapper.readValue(stream, RiasLogin.class);
                }
            } else {
                throw new RiasCheckException(response.getStatusLine().getReasonPhrase());
            }
        } catch (Exception e) {
                throw new RiasCheckException(e);
        }

        log.info("login response: " + result);

        return result;
    }
}
