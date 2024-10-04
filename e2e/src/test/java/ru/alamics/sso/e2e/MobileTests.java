package ru.alamics.sso.e2e;

import jakarta.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.alamics.sso.e2e.common.Tests;
import ru.alamics.sso.e2e.common.TestsClients;
import ru.alamics.sso.e2e.common.TestsEnabled;
import ru.alamics.sso.e2e.common.TestsUsers;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static ru.alamics.sso.e2e.common.TestsUtils.*;

@TestsEnabled
public class MobileTests extends Tests {
    final TestsClients client = TestsClients.MOBILE_APP;

    final TestsUsers user = TestsUsers.MOBILE_TESTER;

    @BeforeEach
    void tearDown() {
        clearRequiredActions(user);
    }

    @Test
    void resetPassword() {
        addRequiredAction(user, "UPDATE_PASSWORD");

        var response = given()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formParam("grant_type", "password")
                .formParam("client_id", client.getClientId())
                .formParam("client_secret", client.getClientSecret())
                .formParam("username", user.getUsername())
                .formParam("password", user.getPassword())
                .baseUri(KEYCLOAK.getAuthServerUrl())
                .pathParam("realm", client.getRealm().getId())
                .post("/realms/{realm}/protocol/openid-connect/token")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body("execution", is("UPDATE_PASSWORD"))
                .extract();

        var tabId = response.jsonPath().getString("tab_id");
        assertNotNull(tabId);

        var accessCode = response.jsonPath().getString("access_code");
        assertNotNull(accessCode);

        var sessionState = response.jsonPath().getString("session_state");
        assertNotNull(sessionState);

        given()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formParam("grant_type", "password")
                .formParam("password-new", user.getPassword())
                .formParam("password-confirm", user.getPassword())
                .queryParam("tab_id", tabId)
                .queryParam("session_code", accessCode)
                .queryParam("auth_session_id", sessionState)
                .queryParam("client_id", client.getClientId())
                .queryParam("execution", "UPDATE_PASSWORD")
                .baseUri(KEYCLOAK.getAuthServerUrl())
                .pathParam("realm", client.getRealm().getId())
                .post("/realms/{realm}/login-actions/required-action")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body("access_token", notNullValue());

        assertFalse(getRequiredActions(user).contains("UPDATE_PASSWORD"));
    }
}
