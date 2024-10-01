package ru.alamics.sso.e2e.common;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.restassured.RestAssured;
import jakarta.ws.rs.core.UriBuilder;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.junit.ClassRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.MockServerContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.testcontainers.utility.MountableFile.forHostPath;
import static ru.alamics.sso.e2e.common.TestsUtils.*;

@ExtendWith(Tests.TestWatcherExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
public abstract class Tests {
    static final String CONDITION_VARIABLE = "ERTH_SSO_E2E_ENABLED";

    static final Path BASEDIR = Paths.get("..").toAbsolutePath().normalize();

    static final boolean IS_ENABLED = Boolean.TRUE.toString().equals(System.getenv(CONDITION_VARIABLE));

    static final boolean IS_LOW_MEMORY = "true".equals(System.getenv().get("ERTH_SSO_E2E_LOW_MEMORY"));

    static final String NAME_PREFIX = "erth-sso-e2e";

    static final String MARIA_DB_HOST = "mariadb";

    static final String SMTP_HOST = "smtp4dev";

    static final String KEYCLOAK_HOST = "keycloak";

    static final String MOCKSERVER_HOST = "mockserver";

    @ClassRule
    public static final Network NETWORK = Network.newNetwork();

    @SuppressWarnings("resource")
    public static final MariaDBContainer<?> MARIA_DB = new MariaDBContainer<>(DockerImageName.parse("mariadb:10.11"))
            .withNetwork(NETWORK)
            .withNetworkAliases(MARIA_DB_HOST);

    @SuppressWarnings("resource")
    public static final GenericContainer<?> SMTP = new GenericContainer<>(DockerImageName.parse("rnwood/smtp4dev:v3"))
            .withNetworkAliases(SMTP_HOST)
            .withNetwork(NETWORK)
            .withExposedPorts(25, 80)
            .waitingFor(Wait.forListeningPort());

    public static final MockServerContainer MOCK_SERVER =
            new MockServerContainer(DockerImageName.parse("mockserver/mockserver:5.15.0"))
                    .withNetwork(NETWORK)
                    .withNetworkAliases(MOCKSERVER_HOST)
                    .waitingFor(Wait.forLogMessage(".*started on port.*", 1))
                    .withEnv("MOCKSERVER_INITIALIZATION_JSON_PATH", "/initialization.json")
                    .withEnv("MOCKSERVER_WATCH_INITIALIZATION_JSON", "true")
                    .withCopyFileToContainer(
                            forHostPath(BASEDIR.resolve("volumes/mockserver/initialization.json")),
                            "/initialization.json"
                    );

    public static final KeycloakContainer KEYCLOAK = new KeycloakContainer("quay.io/keycloak/keycloak:25.0.2")
            .dependsOn(MARIA_DB)
            .dependsOn(SMTP)
            .dependsOn(MOCK_SERVER)
            .withNetwork(NETWORK)
            .withNetworkAliases(KEYCLOAK_HOST)
            .withStartupTimeout(Duration.ofMinutes(5))
            .withContextPath("/auth")
            .withCreateContainerCmdModifier(cmd -> {
                if(IS_LOW_MEMORY) Objects.requireNonNull(cmd.getHostConfig()).withMemory(2048 * 1024 * 1024L);
            })
            .withEnv("JAVA_OPTS", value -> {
                if(IS_LOW_MEMORY) {
                    return value.orElse("") + " -Xms1536m";
                }
                return value.orElse("");
            })
            .withEnv("KC_DB", "mariadb")
            .withEnv("KC_DB_URL_HOST", MARIA_DB_HOST)
            .withEnv("KC_DB_URL_PORT", "3306")
            .withEnv("KC_DB_URL_DATABASE", MARIA_DB.getDatabaseName())
            .withEnv("KC_DB_USERNAME", MARIA_DB.getUsername())
            .withEnv("KC_DB_PASSWORD", MARIA_DB.getPassword())
            .withEnv("KC_LOG_LEVEL", "INFO")
            .withEnv("KC_LOG_CONSOLE_COLOR", "true")
            .withEnv("KC_CACHE", "ispn")
            .withEnv("LIQUIBASE_COMMAND_CHANGE_EXEC_LISTENER_CLASS",
                    "ru.alamics.sso.keycloak.migration.CustomChangeExecListener")
            .withEnv("KC_SPI_USER_PROVIDER", "customjpa")
            .withEnv("KC_CACHE_CONFIG_FILE", "cache-ispn-custom.xml")
            .withEnv("ERTH_SSO_E2E_ENABLED", "true")
            .withCopyFileToContainer(
                    forHostPath(BASEDIR.resolve("volumes/keycloak/opt/keycloak/conf/cache-ispn-custom.xml")),
                    "/opt/keycloak/conf/cache-ispn-custom.xml"
            )
            .withCopyFileToContainer(
                    forHostPath(BASEDIR.resolve("build/keycloak-extension/libs/keycloak-extension-1.0.1-all.jar")),
                    "/opt/keycloak/providers/keycloak-extension.jar"
            );

    @SuppressWarnings("resource")
    public static final GenericContainer<?> KEYCLOAK_CONFIG_CLI =
            new GenericContainer<>(DockerImageName.parse("adorsys/keycloak-config-cli:latest-25.0.1"))
                    .dependsOn(KEYCLOAK)
                    .withNetwork(NETWORK)
                    .withStartupTimeout(Duration.ofMinutes(2))
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
        if (IS_ENABLED) {
            try {
                initialize();
                log.info("Tests initialized");
            } catch (Exception e) {
                throw new ExceptionInInitializerError(e);
            }
        }
    }

    static void initialize() {
        var containers = List.of(MARIA_DB, SMTP, MOCK_SERVER, KEYCLOAK, KEYCLOAK_CONFIG_CLI);

        var namePrefix = NAME_PREFIX + "-" + System.currentTimeMillis() + "-";
        containers.forEach(container -> container.withCreateContainerCmdModifier(cmd  -> {
            cmd.withName(namePrefix + container.getDockerImageName()
                    .replaceAll("[^/]+/", "")
                    .replaceAll(":.*$", ""));
        }));
        containers.forEach(GenericContainer::start);

        Runtime.getRuntime().addShutdownHook(new Thread(() ->  containers.forEach(GenericContainer::stop)));

        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        int mockServerPort = MOCK_SERVER.getExposedPorts().get(0);
        String mockServerUrl = "http://" + MOCKSERVER_HOST + ":" + mockServerPort;

        jdbi().useHandle(handle -> {
            (new HashMap<String, String>() {{
                put("cities.url", mockServerUrl + "/cities/domains");
                put("tbapi.registration.ip", MOCKSERVER_HOST);
                put("tbapi.registration.host", MOCKSERVER_HOST);
                put("tbapi.registration.port", String.valueOf(mockServerPort));
                put("tbapi.registration.secure", "false");
                put("tbapi.registration.find.path", "/tbapi/api/v1/customerManagement/customerAccount");
                put("tbapi.customer.ip", MOCKSERVER_HOST);
                put("tbapi.customer.host", MOCKSERVER_HOST);
                put("tbapi.customer.port", String.valueOf(mockServerPort));
                put("tbapi.customer.secure", "false");
                put("tbapi.customer.find.path", "/tbapi/api/v1/customerManagement/customerAccounts/names");
            }}).forEach((key, value) -> {
                handle.execute("update APP_PROPERTIES set VALUE = ? where NAME = ?", value, key);
            });

            (new HashMap<String, String>() {{
                put("urlDaDataRequestLocationIp", mockServerUrl + "/dadata/suggestions/api/4_1/rs/iplocate/address");
            }}).forEach((key, value) -> {
                handle.execute("update SETTINGS set VALUE = ? where EXT_ID = ?", value, key);
            });

            for (var user : TestsUsers.values()) {
                handle.execute(
                        "insert into CUSTOMER(id, name, update_time) values(?, 'tester', now())",
                        user.getTomsId()
                );
                handle.execute(
                        "insert into USER_POST(id, user_id, toms_id, dmp_id, role_id) " +
                                "values (uuid(), ?, ?, uuid(), 1)",
                        getUserId(user), user.getTomsId()
                );
            }

            for (var client : TestsClients.values()) {
                handle.execute(
                        "insert into MAIN_REDIRECT_URIS (CLIENT_ID, URI) values (?, 'http://localhost')",
                        getClientId(client)
                );
            }
        });
    }

    protected static Jdbi jdbi() {
        return Jdbi.create(() -> MARIA_DB.createConnection(""));
    }

    @BeforeEach
    void tearDownMailBox() {
        clearMailbox();
    }

    static class TestWatcherExtension implements TestWatcher, AfterAllCallback {
        final AtomicBoolean failed = new AtomicBoolean(false);

        @Override
        public void testFailed(ExtensionContext context, Throwable cause) {
            failed.set(true);
        }

        @Override
        public void afterAll(ExtensionContext context) {
            if(failed.get()) {
                log.error("Waiting...");
                try {
                    Thread.sleep(Duration.ofMinutes(15).toMillis());
                } catch (Exception e) {
                    // skip
                }
            }
        }
    }
}
