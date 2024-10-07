package ru.alamics.sso.e2e;

import io.restassured.response.ExtractableResponse;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import ru.alamics.sso.e2e.common.Tests;
import ru.alamics.sso.e2e.common.TestsClients;
import ru.alamics.sso.e2e.common.TestsEnabled;
import ru.alamics.sso.e2e.common.TestsUsers;

import java.net.URI;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static ru.alamics.sso.e2e.common.TestsUtils.*;

@TestsEnabled
@Slf4j
public class LogonTests extends Tests {
    final TestsClients client = TestsClients.APP;

    final TestsUsers user = TestsUsers.TESTER;

    @Test
    void logonByPassword() {
        var logonPage = getLogonPage();

        var logonUrl = Jsoup.parse(logonPage.body().asString())
                .body()
                .select("#loginForm")
                .attr("action");

        assertNotNull(logonUrl);

        var logonRedirectUrl = given()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formParam("username", user.getUsername())
                .formParam("password", user.getPassword())
                .formParam("loginPasswordButton", "")
                .cookies(logonPage.cookies())
                .redirects().follow(false)
                .post(logonUrl)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_MOVED_TEMPORARILY)
                .extract()
                .header(HttpHeaders.LOCATION);

        assertNotNull(logonRedirectUrl);

        var code = getQueryParameter(logonRedirectUrl, "code");

        assertNotNull(code);

        getAccessTokenByCode(code);
    }

    @Test
    void logonBySms() {
        var logonPage = getLogonPage();

        var smsLogonPagePath1 = Jsoup.parse(logonPage.body().asString())
                .body()
                .select("form:has(#smsLoginButton)")
                .attr("action");

        assertNotNull(smsLogonPagePath1);

        var smsLogonPage1 = given()
                .cookies(logonPage.cookies())
                .baseUri(KEYCLOAK.getAuthServerUrl().replaceFirst(KEYCLOAK.getContextPath(), ""))
                .get(smsLogonPagePath1)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract();

        var smsLogonFormUrl1 = Jsoup.parse(smsLogonPage1.body().asString()).select("#loginForm").attr("action");

        assertNotNull(smsLogonFormUrl1);

        var smsLogonFormUri1 = URI.create(smsLogonFormUrl1);
        smsLogonFormUri1 = UriBuilder.fromUri(smsLogonFormUri1).port(KEYCLOAK.getFirstMappedPort()).build();

        var smsLogonPage2 = given()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formParam("username", getUserPhone(user))
                .formParam("smsButton", "")
                .cookies(logonPage.cookies())
                .redirects().follow(false)
                .post(smsLogonFormUri1)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract();

        var smsLogonFormUrl2 = Jsoup.parse(smsLogonPage2.body().asString()).select("#totpe").attr("action");

        assertNotNull(smsLogonFormUrl2);

        String smsCode = getLastSmsCode(getUserPhone(user));

        assertNotNull(smsCode);

        var smsLogonFormUri2 = URI.create(smsLogonFormUrl2);
        smsLogonFormUri2 = UriBuilder.fromUri(smsLogonFormUri2).port(KEYCLOAK.getFirstMappedPort()).build();

        var logonRedirectUrl = given()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formParam("smscode", smsCode)
                .cookies(logonPage.cookies())
                .redirects().follow(false)
                .post(smsLogonFormUri2)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_MOVED_TEMPORARILY)
                .extract()
                .header(HttpHeaders.LOCATION);

        assertNotNull(logonRedirectUrl);

        var code = getQueryParameter(logonRedirectUrl, "code");

        assertNotNull(code);

        getAccessTokenByCode(code);
    }

    ExtractableResponse<?> getLogonPage() {
        return given()
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
    }

    void getAccessTokenByCode(String code) {
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
                .statusCode(HttpStatus.SC_OK)
                .contentType(MediaType.APPLICATION_JSON)
                .extract()
                .body()
                .jsonPath().getString("access_token");

        assertNotNull(accessToken);
    }
}
