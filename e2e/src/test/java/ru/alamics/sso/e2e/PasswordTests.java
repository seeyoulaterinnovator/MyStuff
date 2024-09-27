package ru.alamics.sso.e2e;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import ru.alamics.sso.e2e.common.*;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@TestsEnabled
@Slf4j
public class PasswordTests extends Tests {
    @AfterEach
    void tearDown() {
        executeSQL(connection -> {
            connection.createStatement().execute("delete from USER_REQUIRED_ACTION");
        });
    }

    @Test
    void updatePassword() {
        var client = TestsClients.APP;
        var user = TestsUsers.TESTER;

        String userId = getUserId(user);

        executeSQL(connection -> {
            var statement = connection.prepareStatement(
                    "insert into USER_REQUIRED_ACTION (USER_ID, REQUIRED_ACTION) values (?, 'UPDATE_PASSWORD')"
            );
            statement.setString(1, userId);
            statement.execute();
        });

        var logonPage = given()
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", client.getRedirectUri())
                .queryParam("client_id", client.getClientId())
                .baseUri(KEYCLOAK.getAuthServerUrl())
                .pathParam("realm", client.getRealm().getId())
                .get("/realms/{realm}/protocol/openid-connect/auth")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract();

        var logonUrl = Jsoup.parse(logonPage.body().asString()).body().select("#loginForm").attr("action");

        assertNotNull(logonUrl);
        log.info("Logon URL: {}", logonUrl);

        var resetUrl = given()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formParam("username", user.getUsername())
                .formParam("password", user.getPassword())
                .formParam("loginPasswordButton", "")
                .cookies(logonPage.cookies())
                .redirects().follow(false)
                .post(logonUrl)
                .then()
                .assertThat()
                .log()
                .all()
                .statusCode(HttpStatus.SC_MOVED_TEMPORARILY)
                .extract()
                .header(HttpHeaders.LOCATION);

        log.info("reset URL: {}", resetUrl);

        var updatePage = given()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .cookies(logonPage.cookies())
                .redirects().follow(false)
                .get(resetUrl)
                .then()
                .assertThat()
                .log()
                .all()
                .statusCode(HttpStatus.SC_OK)
                .extract();

        var updateUrl = Jsoup.parse(updatePage.body().asString()).body().select("#loginUpdatePasswordForm").attr("action");

        var logonPage2 = given()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formParam("password-new", user.getPassword())
                .cookies(logonPage.cookies())
                .redirects().follow(true)
                .post(updateUrl)
                .then()
                .assertThat()
                .log()
                .all()
                .statusCode(HttpStatus.SC_MOVED_TEMPORARILY)
                .extract()
                .header(HttpHeaders.LOCATION);

        var code = Arrays.stream(URI.create(logonPage2).getRawQuery().split("&"))
                .filter(p -> p.split("=")[0].equals("code")).map(p -> p.split("=")[1])
                .map(v -> URLDecoder.decode(v, StandardCharsets.UTF_8))
                .findFirst().orElse(null);

        assertNotNull(code);

        var accessToken = given()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formParam("grant_type", "authorization_code")
                .formParam("redirect_uri", client.getRedirectUri())
                .formParam("code", code)
                .auth().preemptive().basic(client.getClientId(), client.getClientSecret())
                .baseUri(KEYCLOAK.getAuthServerUrl())
                .pathParam("realm", client.getRealm().getId())
                .post("/realms/{realm}/protocol/openid-connect/token")
                .then()
                .assertThat()
                .log()
                .all()
                .statusCode(HttpStatus.SC_OK)
                .contentType(MediaType.APPLICATION_JSON)
                .extract()
                .body()
                .jsonPath().getString("access_token");

        assertNotNull(accessToken);

       given()
                .cookies(logonPage.cookies())
                .redirects().follow(false)
                .baseUri(KEYCLOAK.getAuthServerUrl())
                .pathParam("realm", client.getRealm().getId())
                .post("/realms/{realm}/protocol/openid-connect/logout")
                .then()
                .assertThat()
                .log()
                .all()
                .statusCode(HttpStatus.SC_OK)
                .extract();

       var logonPage3 = given()
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", client.getRedirectUri())
                .queryParam("client_id", client.getClientId())
                .baseUri(KEYCLOAK.getAuthServerUrl())
                .pathParam("realm", client.getRealm().getId())
                .get("/realms/{realm}/protocol/openid-connect/auth")
                .then()
                .assertThat()
                .log()
                .all()
                .statusCode(HttpStatus.SC_OK)
                .extract();

       var logonUrl2 = Jsoup.parse(logonPage3.body().asString()).body().select("#loginForm").attr("action");

       assertNotNull(logonUrl2);

        var logonRedirectUrl = given()
                //.header("Referer", logonPageUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formParam("username", user.getUsername())
                .formParam("password", user.getPassword())
                .formParam("loginPasswordButton", "")
                .cookies(logonPage3.cookies())
                .redirects().follow(false)
                .post(logonUrl2)
                .then()
                .assertThat()
                .log()
                .all()
                .statusCode(HttpStatus.SC_MOVED_TEMPORARILY)
                .extract()
                .header(HttpHeaders.LOCATION);

        var code2 = TestsUtils.getQueryParameter(logonRedirectUrl, "code");
        assertNotNull(code2);

        var accessToken2 = given()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formParam("grant_type", "authorization_code")
                .formParam("redirect_uri", client.getRedirectUri())
                .formParam("code", code2)
                .auth().preemptive().basic(client.getClientId(), client.getClientSecret())
                .baseUri(KEYCLOAK.getAuthServerUrl())
                .pathParam("realm", client.getRealm().getId())
                .post("/realms/{realm}/protocol/openid-connect/token")
                .then()
                .assertThat()
                .log()
                .all()
                .statusCode(HttpStatus.SC_OK)
                .contentType(MediaType.APPLICATION_JSON)
                .extract()
                .body()
                .jsonPath().getString("access_token");

        assertNotNull(accessToken2);
    }
}
