package ru.alamics.sso.remote.rias;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.specimpl.ResteasyUriBuilder;
import ru.alamics.sso.registration.rias.port.RiasApiService;
import ru.alamics.sso.remote.rias.model.RiasData;

import javax.ejb.Stateless;
import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Stateless(name = "RiasApiService")
public class RiasUserExistsCheckImpl implements RiasApiService {

    private final ResteasyClient client = new ResteasyClientBuilder()
            .build();

    private MessageDigest digest;

    private final URI uri;

    public RiasUserExistsCheckImpl() {
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e);
        }
        uri = new ResteasyUriBuilder()
                .scheme("https")
                .host("hq-dev.db.ertelecom.ru")
                .port(443)
                .path("/cgi-bin/ppo/excells/web_cabinet.get_info_unauth")
                .build();
    }

    public RiasUserExistsCheckImpl(URI uri) {
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage(), e);
        }
        this.uri = uri;
    }

    private static final String CLIENT_NAME = "SSO";
    private static final String CLIENT_SALT = "W2NHAYTWrfEG9fDw2MAt2TuuM7VK2K7H";

    public boolean checkParam(String param) {

        LocalDateTime dateTime = LocalDateTime.now();

        String timestamp = dateTime.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        String clientSecret = param + "||" + timestamp + "||"+ CLIENT_NAME + "||" + CLIENT_SALT;

        byte[] hash = this.digest.digest(clientSecret.getBytes(StandardCharsets.UTF_8));

        String secretHash = bytesToHex(hash);

        RiasData response = client.target(uri)
                .queryParam("params", "check_profile_data")
                .queryParam("param_names_arr$c", "data_for_check$c,timestamp,client,client_secret")
                .queryParam("param_values_arr$c", param + "," + timestamp + "," + CLIENT_NAME + "," + secretHash)
                .request(MediaType.APPLICATION_XML)
                .get(RiasData.class);

        if (response.getStatus() == 0)
            throw new RuntimeException(response.getMessages().getCode() + ": " + response.getMessages().getText());
        else if (response.getStatus() == 1)
            if (response.getResult().getCheckProfileData() == 0)
                return false;
            else if (response.getResult().getCheckProfileData() == 1)
                return true;
            else if (response.getResult().getCheckProfileData() == -1)
                throw new RuntimeException("Checked data is empty: check_profile_data = " + response.getResult().getCheckProfileData());
            else if (response.getResult().getCheckProfileData() == -2)
                throw new RuntimeException("Checked data is invalid: check_profile_data = " + response.getResult().getCheckProfileData());
            else
                throw new RuntimeException("Bad response data: check_profile_data = " + response.getResult().getCheckProfileData());
        else throw new RuntimeException("Bad response status: status = " + response.getStatus());

    }



    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }



}
