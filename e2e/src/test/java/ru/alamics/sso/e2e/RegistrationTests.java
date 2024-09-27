package ru.alamics.sso.e2e;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.alamics.sso.e2e.common.Tests;
import ru.alamics.sso.e2e.common.TestsClients;
import ru.alamics.sso.e2e.common.TestsEnabled;
import ru.alamics.sso.e2e.common.TestsUtils;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// TODO
@Disabled
@TestsEnabled
@Slf4j
public class RegistrationTests extends Tests {
    @Test
    void register() {
        var client = TestsClients.APP;
        var email = TestsUtils.randomEmail();
        var phone = TestsUtils.randomPhone();

        var logonPage = given()
                .queryParam("response_type", "code")
                .queryParam("client_id", client.getClientId())
                .queryParam("redirect_uri", client.getRedirectUri())
                .baseUri(KEYCLOAK.getAuthServerUrl())
                .pathParam("realm", client.getRealm().getId())
                .get("/realms/{realm}/protocol/openid-connect/auth")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract();

        var formUrl = Jsoup.parse(logonPage.body().asString()).body().select("#registrationForm").attr("action");

        assertNotNull(formUrl);
        log.info("Form URL: {}", formUrl);

        var registrationUrl = given()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formParam("email", email)
                .formParam("phone", phone)
                .cookies(logonPage.cookies())
                .redirects().follow(false)
                .post(formUrl)
                .then()
                .assertThat()
                .log()
                .all()
                .statusCode(HttpStatus.SC_MOVED_TEMPORARILY)
                .extract()
                .header(HttpHeaders.LOCATION);

        assertNotNull(registrationUrl);
        log.info("Registration URL: {}", registrationUrl);

        // TODO
    }
}
