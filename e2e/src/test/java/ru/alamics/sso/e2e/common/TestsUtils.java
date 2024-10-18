package ru.alamics.sso.e2e.common;

import io.restassured.response.ExtractableResponse;
import jakarta.ws.rs.core.MediaType;
import lombok.AccessLevel;
import lombok.Cleanup;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.http.HttpStatus;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.mockserver.model.HttpRequest;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;
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

    @SneakyThrows
    public static Certificate getCertificate(String host, int port) {
        var context = SSLContext.getInstance("TLS");
        context.init(null, new TrustManager[] { new X509TrustManager() {
            @Override public void checkClientTrusted(X509Certificate[] chain, String type) { }
            @Override public void checkServerTrusted(X509Certificate[] chain, String type) { }
            @Override public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
        }}, null);
        @Cleanup var socket = (SSLSocket) context.getSocketFactory().createSocket(host, port);
        socket.startHandshake();
        return socket.getSession().getPeerCertificates()[0];
    }

    @SneakyThrows
    public static String getThumbprint(Certificate certificate) {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(certificate.getEncoded()));
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
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddhh")), PHONE_COUNTER.getAndIncrement() % 100
        );
    }

    public static String getUserId(String username, TestsRealms realm) {
        return jdbi().withHandle(handle -> handle.createQuery(
                        "select ID from USER_ENTITY where USERNAME = ? and REALM_ID = ?"
                )
                .bind(0, username)
                .bind(1, realm.getId())
                .mapTo(String.class)
                .findFirst()
                .orElseThrow());
    }

    public static String getUserId(TestsUsers user) {
        return getUserId(user.getUsername(), user.getRealm());
    }

    public static String getUserAttribute(String username, TestsRealms realm, String attribute) {
        return jdbi().withHandle(handle -> handle.createQuery(
                        "select VALUE from USER_ATTRIBUTE where USER_ID = ? and NAME = ?"
                )
                .bind(0, getUserId(username, realm))
                .bind(1, attribute)
                .mapTo(String.class)
                .findFirst()
                .orElseThrow());
    }

    public static String getUserAttribute(TestsUsers user, String attribute) {
        return getUserAttribute(user.getUsername(), user.getRealm(), attribute);
    }

    public static String getUserPhone(TestsUsers user) {
        return getUserAttribute(user, "phone");
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

    public static List<String> getRequiredActions(String username, TestsRealms realm) {
        return jdbi().withHandle(handle -> handle.createQuery(
                        "select REQUIRED_ACTION from USER_REQUIRED_ACTION where USER_ID = ?"
                )
                .bind(0, getUserId(username, realm))
                .mapTo(String.class)
                .stream()
                .toList());
    }

    public static List<String> getRequiredActions(TestsUsers user) {
        return getRequiredActions(user.getUsername(), user.getRealm());
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
        getSmtp4DevApi()
                .delete("/Messages/*")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK);
    }

    public static int getMessageCount(String email) {
        return getSmtp4DevApi()
                .queryParam("searchTerms", email)
                .queryParam("page", 1)
                .queryParam("pageSize", 1)
                .get("/Messages")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .contentType(MediaType.APPLICATION_JSON)
                .extract()
                .body()
                .jsonPath().getInt("rowCount");
    }

    public static int getMessageCount(TestsUsers user) {
        return getMessageCount(user.getUsername());
    }

    public static String getLastMessageSubject(String email) {
        return getLastMessage(email).body().jsonPath().get("results[0].subject");
    }

    public static String getLastMessageSubject(TestsUsers user) {
        return getLastMessageSubject(user.getUsername());
    }

    public static Document getLastMessageHtml(String email) {
        var messageId = getLastMessage(email).body().jsonPath().get("results[0].id");

        var html = getSmtp4DevApi()
                .pathParam("messageId", messageId)
                .get("/Messages/{messageId}/html")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .body().asString();

        return Jsoup.parse(html);
    }

    public static Document getLastMessageHtml(TestsUsers user) {
        return getLastMessageHtml(user.getUsername());
    }

    public static String getLastSmsCode(String phone) {
        var mockServerClient = getMockServerClient();
        var requests = mockServerClient.retrieveRecordedRequests(
                HttpRequest.request()
                        .withQueryStringParameter("to", phone)
                        .withPath("/sms-sender/sendsms")
        );
        if(requests.length > 0) {
            var request = requests[requests.length - 1];
            var text = request.getFirstQueryStringParameter("text");
            if(!text.isEmpty()) return text;
        }
        return null;
    }

    public static void setAppProperty(String name, String value) {
        jdbi().useHandle(handle -> {
            handle.execute("delete from APP_PROPERTIES where name = ?", name);
            handle.execute("insert into APP_PROPERTIES (NAME, VALUE) values (?, ?)", name, value);
        });
    }

    private static ExtractableResponse<?> getLastMessage(String email) {
        return getSmtp4DevApi()
                .queryParam("searchTerms", email)
                .queryParam("sortColumn", "receivedDate")
                .queryParam("sortIsDescending", true)
                .queryParam("page", 1)
                .queryParam("pageSize", 1)
                .get("/Messages")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .contentType(MediaType.APPLICATION_JSON)
                .extract();
    }
}
