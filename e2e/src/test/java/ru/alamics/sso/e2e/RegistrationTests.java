package ru.alamics.sso.e2e;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;
import ru.alamics.sso.e2e.common.Tests;
import ru.alamics.sso.e2e.common.TestsClients;
import ru.alamics.sso.e2e.common.TestsEnabled;

import java.time.Duration;
import java.util.HashMap;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static ru.alamics.sso.e2e.common.TestsUtils.*;

@TestsEnabled
@Slf4j
public class RegistrationTests extends Tests {
    final TestsClients client = TestsClients.APP;

    final String email = randomEmail();

    final String phone = randomPhone();

    @Test
    void register() {
        var logonPage = given()
                .queryParam("response_type", "code")
                .queryParam("client_id", client.getClientId())
                .queryParam("redirect_uri", client.getRedirectUri())
                .baseUri(getKeycloakUrl())
                .pathParam("realm", client.getRealm().getId())
                .get("/realms/{realm}/protocol/openid-connect/auth")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract();

        var cookies = new HashMap<>(logonPage.cookies());

        var registrationPagePath = Jsoup.parse(logonPage.body().asString())
                .body()
                .select("a[href*='login-actions/registration']")
                .attr("href");

        assertNotNull(registrationPagePath);
        assertNotEquals("", registrationPagePath);

        var registrationPage = given()
                .cookies(cookies)
                .get(resolveKeycloakPath(registrationPagePath))
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract();

        cookies.putAll(registrationPage.cookies());

        var registrationFormUrl = Jsoup.parse(registrationPage.body().asString())
                .body()
                .select("form[action*='/login-actions/registration']")
                .attr("action");

        assertNotNull(registrationFormUrl);
        assertNotEquals("", registrationFormUrl);

        var emptyReqActionPageUrl = given()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formParam("firstName", "-")
                .formParam("lastName", "-")
                .formParam("email", email)
                .formParam("phone", phone.replaceAll("7(\\d{3})(\\d{3})(\\d{2})(\\d{2})", "+7 ($1) $2-$3-$4"))
                .formParam("username", email)
                .cookies(cookies)
                .redirects().follow(false)
                .post(resolveKeycloakPort(registrationFormUrl))
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_MOVED_TEMPORARILY)
                .extract()
                .header(HttpHeaders.LOCATION);

        assertNotNull(emptyReqActionPageUrl);
        assertNotEquals("", emptyReqActionPageUrl);

        var emptyReqActionPage = given()
                .cookies(logonPage.cookies())
                .get(resolveKeycloakPort(emptyReqActionPageUrl))
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract();

        cookies.putAll(emptyReqActionPage.cookies());

        var emailSenderActionPageUrl = Jsoup.parse(emptyReqActionPage.body().asString())
                .body()
                .select("form[action*='/login-actions/required-action']")
                .attr("action");

        assertNotNull(emailSenderActionPageUrl);
        assertNotEquals("", emailSenderActionPageUrl);

        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofSeconds(5))
                .until(() -> getMessageCount(email) >= 1);

        var emailSenderActionPage = given()
                .cookies(cookies)
                .get(resolveKeycloakPort(emailSenderActionPageUrl))
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract();

        cookies.putAll(emailSenderActionPage.cookies());

        var smsFormUrl1 = Jsoup.parse(emailSenderActionPage.body().asString())
                .body()
                .select("#totpForm")
                .attr("action");

        assertNotNull(smsFormUrl1);
        assertNotEquals("", smsFormUrl1);

        var smsCode1 = getLastSmsCode(phone);

        assertNotNull(smsCode1);

        var logonRedirectUrl1 = given()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formParam("smscode", smsCode1)
                .cookies(cookies)
                .redirects().follow(false)
                .post(resolveKeycloakPort(smsFormUrl1))
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_MOVED_TEMPORARILY)
                .extract()
                .header(HttpHeaders.LOCATION);

        assertNotNull(logonRedirectUrl1);

        var code1 = getQueryParameter(logonRedirectUrl1, "code");

        assertNotNull(code1);

        getAccessTokenByCode(code1);

        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofSeconds(5))
                .until(() -> getMessageCount(email) >= 2);

        var message = getLastMessageHtml(email);

        assertNotNull(message);

        var confirmEmailPageUrl = message.body().select("a[href*='/login-actions/action-token']").attr("href");

        assertNotNull(confirmEmailPageUrl);
        assertNotEquals("", confirmEmailPageUrl);

        var confirmEmailPage = given()
                .redirects().follow(false)
                .get(resolveKeycloakPort(confirmEmailPageUrl))
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_MOVED_TEMPORARILY)
                .extract();

        cookies = new HashMap<>(confirmEmailPage.cookies());

        var phoneVerificatorSmsActionPageUrl = confirmEmailPage.header(HttpHeaders.LOCATION);

        assertNotNull(phoneVerificatorSmsActionPageUrl);

        var phoneVerificatorSmsActionPage = given()
                .cookies(cookies)
                .get(resolveKeycloakPort(phoneVerificatorSmsActionPageUrl))
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract();

        cookies.putAll(phoneVerificatorSmsActionPage.cookies());

        var smsFormUrl2 = Jsoup.parse(phoneVerificatorSmsActionPage.body().asString())
                .body()
                .select("#totpForm")
                .attr("action");

        assertNotNull(smsFormUrl2);
        assertNotEquals("", smsFormUrl2);

        var smsCode2 = getLastSmsCode(phone);

        assertNotNull(smsCode2);

        var passwordFormPage = given()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formParam("smscode", smsCode2)
                .cookies(cookies)
                .post(resolveKeycloakPort(smsFormUrl2))
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract();

        cookies.putAll(passwordFormPage.cookies());

        var passwordFormUrl = Jsoup.parse(passwordFormPage.body().asString())
                .body()
                .select("#loginUpdatePasswordForm")
                .attr("action");

        assertNotNull(passwordFormUrl);
        assertNotEquals("", passwordFormUrl);

        var logonRedirectUrl2 = given()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formParam("password-new", "password")
                .cookies(cookies)
                .redirects().follow(false)
                .post(resolveKeycloakPort(passwordFormUrl))
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_MOVED_TEMPORARILY)
                .extract()
                .header(HttpHeaders.LOCATION);

        assertNotNull(logonRedirectUrl2);

        var code2 = getQueryParameter(logonRedirectUrl2, "code");

        assertNotNull(code2);

        getAccessTokenByCode(code2);

        assertTrue(getRequiredActions(email, client.getRealm()).isEmpty());

        assertEquals(phone, getUserAttribute(email, client.getRealm(), "phone"));

        assertNotNull(getUserAttribute(email, client.getRealm(), "phone_validated_on"));
    }

    void getAccessTokenByCode(String code) {
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
    }
}
