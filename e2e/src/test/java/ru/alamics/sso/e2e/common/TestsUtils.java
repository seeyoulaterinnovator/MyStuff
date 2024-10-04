package ru.alamics.sso.e2e.common;

import jakarta.ws.rs.core.MediaType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.http.HttpStatus;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static ru.alamics.sso.e2e.common.Tests.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class TestsUtils {
    private static final AtomicInteger EMAIL_COUNTER = new AtomicInteger(1);

    private static final AtomicInteger PHONE_COUNTER = new AtomicInteger(1);

    public static String getQueryParameter(String url, String parameter) {
        return Arrays.stream(URI.create(url).getRawQuery().split("&"))
                .filter(p -> p.split("=")[0].equals(parameter)).map(p -> p.split("=")[1])
                .map(v -> URLDecoder.decode(v, StandardCharsets.UTF_8))
                .findFirst().orElse(null);
    }

    public static String randomEmail() {
        return String.format(
                "tester%s%d@nomail.tld",
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")), EMAIL_COUNTER.getAndIncrement()
        );
    }

    public static String randomPhone() {
        return String.format(
                "8%s%02d",
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")), PHONE_COUNTER.getAndIncrement() % 100
        );
    }

    public static String getUserId(TestsUsers user) {
        return jdbi().withHandle(handle -> handle.createQuery(
                        "select id from USER_ENTITY where USERNAME = ? and REALM_ID = ?"
                )
                .bind(0, user.getUsername())
                .bind(1, user.getRealm().getId())
                .mapTo(String.class)
                .findFirst()
                .orElseThrow());
    }

    public static void clearRequiredActions(TestsUsers user) {
        jdbi().useHandle(handle -> handle.execute(
                "delete from USER_REQUIRED_ACTION where USER_ID = ?",
                getUserId(user)
        ));
    }

    public static void addRequiredAction(TestsUsers user, String action) {
        jdbi().useHandle(handle -> handle.execute(
                "insert into USER_REQUIRED_ACTION (USER_ID, REQUIRED_ACTION) values (?, ?)",
                getUserId(user), action
        ));
    }

    public static List<String> getRequiredActions(TestsUsers user) {
        return jdbi().withHandle(handle -> handle.createQuery(
                        "select REQUIRED_ACTION from USER_REQUIRED_ACTION where USER_ID = ?"
                )
                .bind(0, getUserId(user))
                .mapTo(String.class)
                .stream()
                .toList());
    }

    public static String getClientId(TestsClients client) {
        return jdbi().withHandle(handle -> handle.createQuery(
                        "select id from CLIENT where CLIENT_ID = ? and REALM_ID = ?"
                )
                .bind(0, client.getClientId())
                .bind(1, client.getRealm().getId())
                .mapTo(String.class)
                .findFirst()
                .orElseThrow());
    }

    public static String getAdminCliAccessToken(TestsUsers user) {
        return given()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formParam("client_id", "admin-cli")
                .formParam("grant_type", "password")
                .formParam("username", user.getUsername())
                .formParam("password", user.getPassword())
                .pathParam("realm", user.getRealm().getId())
                .baseUri(KEYCLOAK.getAuthServerUrl())
                .post("/realms/{realm}/protocol/openid-connect/token")
                .then()
                .assertThat()
                .log()
                .all()
                .statusCode(HttpStatus.SC_OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body("access_token", notNullValue())
                .extract()
                .body()
                .jsonPath().getString("access_token");
    }

    public static String getAdminCliAccessToken() {
        return getAdminCliAccessToken(TestsUsers.ADMIN);
    }

    public static void clearMailbox() {
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .auth().oauth2(getAdminCliAccessToken())
                .baseUri("http://localhost:" + SMTP.getMappedPort(80))
                .delete("/api/Messages/*")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    public static int getMessageCount(TestsUsers user) {
        return given()
                .contentType(MediaType.APPLICATION_JSON)
                .auth().oauth2(getAdminCliAccessToken())
                .baseUri("http://localhost:" + SMTP.getMappedPort(80))
                .queryParam("searchTerms", user.getUsername())
                .queryParam("page", 1)
                .queryParam("pageSize", 1)
                .get("/api/Messages")
                .then()
                .assertThat()
                .log()
                .all()
                .statusCode(HttpStatus.SC_OK)
                .contentType(MediaType.APPLICATION_JSON)
                .extract()
                .body()
                .jsonPath().getInt("rowCount");
    }

    public static String getLastMessageSubject(TestsUsers user) {
        return given()
                .contentType(MediaType.APPLICATION_JSON)
                .auth().oauth2(getAdminCliAccessToken())
                .baseUri("http://localhost:" + SMTP.getMappedPort(80))
                .queryParam("searchTerms", user.getUsername())
                .queryParam("sortColumn", "receivedDate")
                .queryParam("sortIsDescending", true)
                .queryParam("page", 1)
                .queryParam("pageSize", 1)
                .get("/api/Messages")
                .then()
                .assertThat()
                .log()
                .all()
                .statusCode(HttpStatus.SC_OK)
                .contentType(MediaType.APPLICATION_JSON)
                .extract()
                .body()
                .jsonPath().get("results[0].subject");
    }
}
