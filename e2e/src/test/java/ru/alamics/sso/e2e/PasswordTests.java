package ru.alamics.sso.e2e;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import ru.alamics.sso.e2e.common.Tests;
import ru.alamics.sso.e2e.common.TestsClients;
import ru.alamics.sso.e2e.common.TestsEnabled;
import ru.alamics.sso.e2e.common.TestsUsers;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static ru.alamics.sso.e2e.common.TestsUtils.*;

@TestsEnabled
@Slf4j
public class PasswordTests extends Tests {
    final TestsUsers user = TestsUsers.PASSWORD_TESTER;
//    final TestsUsers user2 = TestsUsers.TESTER;

    final String userId = getUserId(user);

    @BeforeEach
    void tearDown() {
        clearRequiredActions(user);
    }

    @Test
    @Order(1)
    void updatePassword() {
        var client = TestsClients.APP;
        var newPassword = user.getPassword() + "!";

        addRequiredAction(user, "UPDATE_PASSWORD");

        var logonPage = given()
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", client.getRedirectUri())
                .queryParam("client_id", client.getClientId())
                .baseUri(getKeycloakUrl())
                .pathParam("realm", client.getRealm().getId())
                .get("/realms/{realm}/protocol/openid-connect/auth")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract();

        var logonUrl = Jsoup.parse(logonPage.body().asString()).body().select("#loginForm").attr("action");

        assertNotNull(logonUrl);
        assertNotEquals("", logonUrl);
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
                .statusCode(HttpStatus.SC_OK)
                .extract();

        var updateUrl = Jsoup.parse(updatePage.body().asString()).body().select("#loginUpdatePasswordForm").attr("action");

        assertNotNull(updateUrl);
        assertNotEquals("", updateUrl);
        log.info("update URL: {}", updateUrl);

        var logonPage2 = given()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formParam("password-new", newPassword)
                .cookies(logonPage.cookies())
                .redirects().follow(true)
                .post(updateUrl)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_MOVED_TEMPORARILY)
                .extract()
                .header(HttpHeaders.LOCATION);

        var code = getQueryParameter(logonPage2, "code");
        assertNotNull(code);

        var accessToken = given()
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
                .jsonPath().getString("access_token");

        assertNotNull(accessToken);

       given()
                .cookies(logonPage.cookies())
                .redirects().follow(false)
                .baseUri(getKeycloakUrl())
                .pathParam("realm", client.getRealm().getId())
                .post("/realms/{realm}/protocol/openid-connect/logout")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract();

       var logonPage3 = given()
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", client.getRedirectUri())
                .queryParam("client_id", client.getClientId())
                .baseUri(getKeycloakUrl())
                .pathParam("realm", client.getRealm().getId())
                .get("/realms/{realm}/protocol/openid-connect/auth")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract();

       var logonUrl2 = Jsoup.parse(logonPage3.body().asString()).body().select("#loginForm").attr("action");

       assertNotNull(logonUrl2);
       assertNotEquals("", logonPage2);

        var logonRedirectUrl = given()
                //.header("Referer", logonPageUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formParam("username", user.getUsername())
                .formParam("password", newPassword)
                .formParam("loginPasswordButton", "")
                .cookies(logonPage3.cookies())
                .redirects().follow(false)
                .post(logonUrl2)
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_MOVED_TEMPORARILY)
                .extract()
                .header(HttpHeaders.LOCATION);

        var code2 = getQueryParameter(logonRedirectUrl, "code");
        assertNotNull(code2);

        var accessToken2 = given()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formParam("grant_type", "authorization_code")
                .formParam("redirect_uri", client.getRedirectUri())
                .formParam("code", code2)
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
                .jsonPath().getString("access_token");

        assertNotNull(accessToken2);
    }

    @Test
    @Order(2)
    void sendLoginAndResetPassword() {
        var user = TestsUsers.PASSWORD_TESTER;

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .auth().oauth2(getAdminCliAccessToken())
                .baseUri(getKeycloakUrl())
                .body(List.of(userId))
                .pathParam("realm", user.getRealm().getId())
                .post("/realms/{realm}/users-toms/send/login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofSeconds(5))
                .until(() -> getMessageCount(user) > 0);

        assertEquals("Ваш логин для входа в Личный кабинет", getLastMessageSubject(user));

        assertFalse(getRequiredActions(user).contains("UPDATE_PASSWORD"));

        clearMailbox();

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .auth().oauth2(getAdminCliAccessToken())
                .baseUri(getKeycloakUrl())
                .body(List.of(userId))
                .pathParam("realm", user.getRealm().getId())
                .post("/realms/{realm}/users-toms/credential/reset-with-send-login")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofSeconds(5))
                .until(() -> getMessageCount(user) > 0);

        assertEquals("Ваш пароль для входа в Личный кабинет сброшен", getLastMessageSubject(user));

        assertTrue(getRequiredActions(user).contains("UPDATE_PASSWORD"));
    }

    @Test
    @Order(3)
    void blockPasswordByJob() {
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .auth().oauth2(getAdminCliAccessToken())
                .baseUri(getKeycloakUrl())
                .body(Map.of(
                        "id", "9e4f8fb6-5425-11ec-bf63-0242ac130002",
                        "extId", "timerIntervalDurationProperty",
                        "name", "Timer schedule",
                        "realmId", "master",
                        "value", "5",
                        "unit", "SECONDS",
                        "type", "REALM"
                ))
                .put("/realms/master/settings/9e4f8fb6-5425-11ec-bf63-0242ac130002")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);

        jdbi().useHandle(handle -> handle.execute("update CREDENTIAL set CREATED_DATE = 0 where USER_ID = ?", userId));

        log.info("User {} credential updated", user.getUsername());

        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofSeconds(5))
                .until(() -> jdbi().withHandle(handle ->
                        handle.createQuery("select ENABLED from USER_ENTITY where ID = ?")
                                .bind(0, userId)
                                .mapTo(Boolean.class)
                                .findFirst()
                                .orElseThrow())
                );

        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofSeconds(5))
                .until(() -> getMessageCount(user) > 0);

        assertEquals("Истек срок жизни пароля", getLastMessageSubject(user));
    }

    @Test
    @Order(4)
    void deletePassword() {
        var user = TestsUsers.PASSWORD_TESTER;

        var userId = given()
                .contentType(MediaType.APPLICATION_JSON)
                .auth().oauth2(getAdminCliAccessToken())
                .baseUri(getKeycloakUrl())
                .pathParams(Map.of(
                        "realm", user.getRealm().getId(),
                        "realm2", user.getRealm().getId(),
                        "email", user.getUsername()
                ))
                .get("/realms/{realm}/users-info/search?searchRealm={realm2}&first=0&max=11&searchEmail={email}")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .contentType(MediaType.APPLICATION_JSON)
                .extract()
                .body()
                .jsonPath().getString("results['users-info'][0].id");

        var userCredentialId = given()
                .contentType(MediaType.APPLICATION_JSON)
                .auth().oauth2(getAdminCliAccessToken())
                .baseUri(getKeycloakUrl())
                .pathParams(Map.of(
                        "realm", user.getRealm().getId(),
                        "userId", userId
                ))
                .get("/admin/realms/{realm}/users/{userId}/credentials/")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .contentType(MediaType.APPLICATION_JSON)
                .extract()
                .body()
                .jsonPath().getString("[0].id");

        given()
                .contentType(MediaType.APPLICATION_JSON)
                .auth().oauth2(getAdminCliAccessToken())
                .baseUri(getKeycloakUrl())
                .pathParams(Map.of(
                        "realm", user.getRealm().getId(),
                        "userId", userId,
                        "userCredentialId", userCredentialId
                ))
                .delete("/realms/{realm}/users-toms/users/{userId}/credentials/{userCredentialId}")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_NO_CONTENT);

        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofSeconds(5))
                .until(() -> getMessageCount(user) > 0);

        assertEquals("Обновление Вашей учетной записи", getLastMessageSubject(user));

        assertTrue(getRequiredActions(user).contains("UPDATE_PASSWORD"));

        clearMailbox();
    }

}
