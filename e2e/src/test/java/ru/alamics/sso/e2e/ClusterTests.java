package ru.alamics.sso.e2e;

import io.restassured.path.json.JsonPath;
import io.restassured.response.ExtractableResponse;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.RepeatedTest;
import ru.alamics.sso.e2e.common.TestsClients;
import ru.alamics.sso.e2e.common.TestsUsers;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static ru.alamics.sso.e2e.common.TestsUtils.getQueryParameter;

/**
 * see docker-compose-local-cluster.yml
 */
//@org.junit.jupiter.api.Disabled
public class ClusterTests {
    String keycloakUrl = "http://localhost:8080/auth";

    TestsClients client = TestsClients.APP;

    TestsUsers user = TestsUsers.TESTER;

    @RepeatedTest(value = 100)
    void test() {
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

        getTokensByCodeAndLogout(code);
    }

    ExtractableResponse<?> getLogonPage() {
        return given()
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", client.getRedirectUri())
                .queryParam("client_id", client.getClientId())
                .queryParam("scope", "openid")
                .pathParam("realm", client.getRealm().getId())
                .baseUri(keycloakUrl)
                .get("/realms/{realm}/protocol/openid-connect/auth")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract();
    }

    void getTokensByCodeAndLogout(String code) {
        JsonPath jsonPath = given()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formParam("grant_type", "authorization_code")
                .formParam("redirect_uri", client.getRedirectUri())
                .formParam("code", code)
                .auth().preemptive().basic(client.getClientId(), client.getClientSecret())
                .pathParam("realm", client.getRealm().getId())
                .baseUri(keycloakUrl)
                .post("/realms/{realm}/protocol/openid-connect/token")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .contentType(MediaType.APPLICATION_JSON)
                .extract()
                .body()
                .jsonPath();

        var accessToken = jsonPath.getString("access_token");

        var idToken = jsonPath.getString("id_token");

        assertNotNull(accessToken);
        assertNotNull(idToken);

        logout(idToken);
    }

    void logout(String idToken) {
        given()
                .queryParam("id_token_hint", idToken)
                .queryParam("post_logout_redirect_uri", client.getRedirectUri())
                .baseUri(keycloakUrl)
                .redirects().follow(false)
                .pathParam("realm", client.getRealm().getId())
                .get("/realms/{realm}/protocol/openid-connect/logout")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_MOVED_TEMPORARILY);
    }
}
