package ru.alamics.sso.e2e;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.restassured.RestAssured;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriBuilder;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.jsoup.Jsoup;
import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.testcontainers.utility.MountableFile.forHostPath;

// TODO
@EnabledIfEnvironmentVariable(named = "ERTH_SSO_E2E_ENABLED", matches = "true")
@ExtendWith(SmsSenderTestExample.WaitOnFailExtension.class)
@Testcontainers
@Slf4j
public class SmsSenderTestExample {
    static final Path BASEDIR = Paths.get("..").toAbsolutePath().normalize();

    static final String MARIA_DB_HOST = "mariadb";

    static final String KEYCLOAK_HOST = "keycloak";

    @ClassRule
    public static final Network NETWORK = Network.newNetwork();

    @SuppressWarnings({"resource"})
    @Container
    static final MariaDBContainer<?> MARIA_DB = new MariaDBContainer<>(DockerImageName.parse("mariadb:10.11"))
            .withNetwork(NETWORK)
            .withNetworkAliases(MARIA_DB_HOST);

    @Container
    static final KeycloakContainer KEYCLOAK = new KeycloakContainer("quay.io/keycloak/keycloak:25.0.2")
            .dependsOn(MARIA_DB)
            .withNetwork(NETWORK)
            .withNetworkAliases(KEYCLOAK_HOST)
            .withContextPath("/auth")
            //.withEnv("JAVA_OPTS", "-Xmx768m")
            .withEnv("KC_DB", "mariadb")
            .withEnv("KC_DB_URL_HOST", MARIA_DB_HOST)
            .withEnv("KC_DB_URL_PORT", "3306")
            .withEnv("KC_DB_URL_DATABASE", MARIA_DB.getDatabaseName())
            .withEnv("KC_DB_USERNAME", MARIA_DB.getUsername())
            .withEnv("KC_DB_PASSWORD", MARIA_DB.getPassword())
            .withEnv("KC_LOG_LEVEL", "INFO")
            .withEnv("KC_LOG_CONSOLE_COLOR", "true")
            .withEnv("KC_CACHE", "ispn")
            .withEnv("KC_CACHE_CONFIG_FILE", "cache-ispn-custom.xml")
            .withEnv("ERTH_SSO_E2E_ENABLED", "true")
            .withEnv("SITE", "STAGE4")
            .withCopyFileToContainer(
                    forHostPath(BASEDIR.resolve("volumes/keycloak/opt/keycloak/conf/cache-ispn-custom.xml")),
                    "/opt/keycloak/conf/cache-ispn-custom.xml"
            )
            .withCopyFileToContainer(
                    forHostPath(BASEDIR.resolve("build/keycloak-extension/libs/keycloak-extension-1.0.1-all.jar")),
                    "/opt/keycloak/providers/keycloak-extension.jar"
            )
            .withStartupTimeout(Duration.ofMinutes(5));

    @SuppressWarnings({"resource", "unused"})
    @Container
    static final GenericContainer<?> KEYCLOAK_CONFIG_CLI =
            new GenericContainer<>(DockerImageName.parse("adorsys/keycloak-config-cli:latest-25.0.1"))
                    .dependsOn(KEYCLOAK)
                    .withNetwork(NETWORK)
                    .waitingFor(Wait.forLogMessage(".*keycloak-config-cli ran in.*", 1))
                    .withEnv("KEYCLOAK_URL", UriBuilder.newInstance()
                            .scheme("http")
                            .host(KEYCLOAK_HOST)
                            .port(8080)
                            .path(KEYCLOAK.getContextPath())
                            .build().toString())
                    .withEnv("KEYCLOAK_USER", KEYCLOAK.getAdminUsername())
                    .withEnv("KEYCLOAK_PASSWORD", KEYCLOAK.getAdminPassword())
                    .withEnv("KEYCLOAK_AVAILABILITYCHECK_ENABLED", "true")
                    .withEnv("IMPORT_FILES_LOCATIONS", "/config/*")
                    .withCopyFileToContainer(
                            forHostPath(BASEDIR.resolve("volumes/keycloak-config-cli/config/e2e.json")),
                            "/config/e2e.json"
                    );

    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }

    static class WaitOnFailExtension implements TestWatcher, AfterAllCallback {
        boolean failed;

        @Override
        public void afterAll(ExtensionContext context) throws Exception {
            if(failed) {
                log.error("Waiting...");
                Thread.sleep(Duration.ofMinutes(15).toMillis());
            }
        }

        @Override
        public void testFailed(ExtensionContext context, Throwable cause) {
            failed = true;
        }
    }

    @BeforeAll
    static void initMariaDB() throws Exception {
        @Cleanup var connection = MARIA_DB.createConnection("");
        @Cleanup var statement = connection.createStatement();

        {
            @Cleanup var rs = statement.executeQuery("select ID, FILENAME, EXECTYPE " +
                    "from DATABASECHANGELOG_CUSTOMJPAE " +
                    "order by ORDEREXECUTED");
            while (rs.next()) {
                log.info("Changelog {} {} - {}", rs.getString(1), rs.getString(2), rs.getString(3));
            }
        }

        var tomsId = 10002408221L;
        var userIdSql = "select id from USER_ENTITY where username = 'tester@nomail.tld' and realm_id = 'e2e'";
        statement.execute(String.format(
                "insert into CUSTOMER(id, name, update_time) values(%d, 'tester', now())",
                tomsId
        ));
        statement.execute(String.format(
                "insert into USER_POST(id, user_id, toms_id, dmp_id, role_id) values (uuid(), (%s), %d, uuid(), 1)",
                userIdSql, tomsId
        ));
    }

//    @Test
    void test() {
        var realm = "e2e";
        var clientId = "app";
        var clientSecret = "secret";
        var username = "79026370011";
        var redirectUri = "http://localhost";

        var logonPage = given()
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", redirectUri)
                .queryParam("client_id", clientId)
                .baseUri(KEYCLOAK.getAuthServerUrl())
                .pathParam("realm", realm)
                .get("/realms/{realm}/protocol/openid-connect/auth")
                .then()
                .assertThat()
                .log()
                .all()
                .statusCode(HttpStatus.SC_OK)
                .extract();

        var logonUrl = Jsoup.parse(logonPage.body().asString()).body().select("#loginForm").attr("action");

        assertNotNull(logonUrl);
        log.info("Logon URL: {}", logonUrl);

        var logonRedirectUrl = given()
                //.header("Referer", logonPageUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formParam("username", username)
                .formParam("smsButton", "")
                .cookies(logonPage.cookies())
                .redirects().follow(false)
                .post(logonUrl)
                .then()
                .assertThat()
                .log()
                .all()
                .statusCode(HttpStatus.SC_OK)
                .extract()
                .header(HttpHeaders.LOCATION);

        assertNotNull(logonRedirectUrl);

        var code = Arrays.stream(URI.create(logonRedirectUrl).getRawQuery().split("&"))
                .filter(p -> p.split("=")[0].equals("code")).map(p -> p.split("=")[1])
                .map(v -> URLDecoder.decode(v, StandardCharsets.UTF_8))
                .findFirst().orElse(null);

        assertNotNull(code);

        var accessToken = given()
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .formParam("grant_type", "authorization_code")
                .formParam("redirect_uri", redirectUri)
                .formParam("code", code)
                .auth().preemptive().basic(clientId, clientSecret)
                .baseUri(KEYCLOAK.getAuthServerUrl())
                .pathParam("realm", realm)
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
    }
}
