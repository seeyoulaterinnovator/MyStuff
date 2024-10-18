package ru.alamics.sso.e2e;

import jakarta.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import ru.alamics.sso.e2e.common.Tests;
import ru.alamics.sso.e2e.common.TestsClients;
import ru.alamics.sso.e2e.common.TestsEnabled;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@TestsEnabled
public class ClientTests extends Tests {
    TestsClients client = TestsClients.APP;

    @Test
    void logon() {
        given()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formParam("grant_type", "client_credentials")
                .formParam("client_id", client.getClientId())
                .formParam("client_secret", client.getClientSecret())
                .pathParam("realm", client.getRealm().getId())
                .baseUri(getKeycloakUrl())
                .post("/realms/{realm}/protocol/openid-connect/token")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body("access_token", notNullValue());
    }

    /**
     * see ru.alamics.sso.keycloak.oidc.TokenEndpointInterceptor
     */
    @Test
    void legacyInvalidLogon() {
        given()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formParam("grant_type", "client_credentials")
                .formParam("client_id", client.getClientId())
                .formParam("client_id", client.getClientId()) // duplicate
                .formParam("client_secret", client.getClientSecret())
                .pathParam("realm", client.getRealm().getId())
                .baseUri(getKeycloakUrl())
                .post("/realms/{realm}/protocol/openid-connect/token")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body("access_token", notNullValue());
    }

    @Test
    void legacyInvalidLogonFail() {
        given()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formParam("grant_type", "client_credentials")
                .formParam("client_id", client.getClientId())
                .formParam("client_id", client.getClientId() + "1") // collision
                .formParam("client_secret", client.getClientSecret())
                .pathParam("realm", client.getRealm().getId())
                .baseUri(getKeycloakUrl())
                .post("/realms/{realm}/protocol/openid-connect/token")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body("error", is("invalid_request"))
                .body("error_description", is("duplicated parameter"));
    }
}
