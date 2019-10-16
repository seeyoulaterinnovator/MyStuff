package ru.alamics.sso.keycloak.social.esia;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.codec.binary.Base64;
import org.keycloak.util.JsonSerialization;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DecodedToken {
    public String scope;
    @JsonProperty("urn:esia:sbj_id")
    public String userId;
    public String nbf;

    public static DecodedToken getDecoded(String encodedToken) throws IOException {
        String[] pieces = encodedToken.split("\\.");
        String b64payload = pieces[1];
        String jsonString = new String(Base64.decodeBase64(b64payload), StandardCharsets.UTF_8);
        return JsonSerialization.readValue(jsonString, DecodedToken.class);
    }
}
