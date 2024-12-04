package ru.alamics.sso.e2e;

import io.restassured.path.json.JsonPath;
import io.restassured.response.ExtractableResponse;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import ru.alamics.sso.e2e.common.Tests;
import ru.alamics.sso.e2e.common.TestsClients;
import ru.alamics.sso.e2e.common.TestsEnabled;
import ru.alamics.sso.e2e.common.TestsUsers;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static ru.alamics.sso.e2e.common.TestsUtils.*;

@TestsEnabled
@Slf4j
public class LogonTests extends Tests {
    final TestsClients client = TestsClients.APP;

    final TestsUsers user = TestsUsers.TESTER;

    String accessToken;

    String idToken;

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

        getTokensByCode(code);

        logout();
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
                .get(resolveKeycloakPath(smsLogonPagePath1))
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract();

        var smsLogonFormUrl1 = Jsoup.parse(smsLogonPage1.body().asString()).select("#loginForm").attr("action");

        assertNotNull(smsLogonFormUrl1);
        assertNotEquals("", smsLogonFormUrl1);

        var smsLogonPage2 = given()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formParam("username", getUserPhone(user))
                .formParam("smsButton", "")
                .cookies(logonPage.cookies())
                .redirects().follow(false)
                .post(resolveKeycloakPort(smsLogonFormUrl1))
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract();

        var smsLogonFormUrl2 = Jsoup.parse(smsLogonPage2.body().asString()).select("#totpe").attr("action");

        assertNotNull(smsLogonFormUrl2);
        assertNotEquals("", smsLogonFormUrl2);

        String smsCode = getLastSmsCode(getUserPhone(user));

        assertNotNull(smsCode);

        var logonRedirectUrl = given()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formParam("smscode", smsCode)
                .cookies(logonPage.cookies())
                .redirects().follow(false)
                .post(resolveKeycloakPort(smsLogonFormUrl2))
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_MOVED_TEMPORARILY)
                .extract()
                .header(HttpHeaders.LOCATION);

        assertNotNull(logonRedirectUrl);

        var code = getQueryParameter(logonRedirectUrl, "code");

        assertNotNull(code);

        getTokensByCode(code);

        legacyInvalidLogout();
    }

    ExtractableResponse<?> getLogonPage() {
        return given()
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", client.getRedirectUri())
                .queryParam("client_id", client.getClientId())
                .queryParam("scope", "openid")
                .baseUri(getKeycloakUrl())
                .pathParam("realm", client.getRealm().getId())
                .get("/realms/{realm}/protocol/openid-connect/auth")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract();
    }

    void getTokensByCode(String code) {
        JsonPath jsonPath = given()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formParam("grant_type", "authorization_code")
                .formParam("redirect_uri", client.getRedirectUri())
                .formParam("code", code)
                .auth().preemptive().basic(client.getClientId(), client.getClientSecret())
                .baseUri(getKeycloakUrl())
                .pathParam("realm", client.getRealm().getId())
                .post("/realms/{realm}/protocol/openid-connect/token")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .contentType(MediaType.APPLICATION_JSON)
                .extract()
                .body()
                .jsonPath();

        accessToken = jsonPath.getString("access_token");

        idToken = jsonPath.getString("id_token");

        assertNotNull(accessToken);
        assertNotNull(idToken);
    }

    void logout() {
        given()
                .queryParam("id_token_hint", idToken)
                .queryParam("post_logout_redirect_uri", client.getRedirectUri())
                .baseUri(getKeycloakUrl())
                .pathParam("realm", client.getRealm().getId())
                .redirects().follow(false)
                .get("/realms/{realm}/protocol/openid-connect/logout")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_MOVED_TEMPORARILY);
    }

    /**
     * see ru.alamics.sso.keycloak.oidc.LogoutEndpointInterceptor
     */
    void legacyInvalidLogout() {
        given()
                .queryParam("id_token_hint", accessToken)
                .queryParam("post_logout_redirect_uri", client.getRedirectUri())
                .baseUri(getKeycloakUrl())
                .pathParam("realm", client.getRealm().getId())
                .redirects().follow(false)
                .get("/realms/{realm}/protocol/openid-connect/logout")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_MOVED_TEMPORARILY);
    }
}
