package ru.alamics.sso.e2e.common;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.restassured.RestAssured;
import jakarta.ws.rs.core.UriBuilder;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.ClassRule;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
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
import java.sql.Connection;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.testcontainers.utility.MountableFile.forHostPath;

@EnabledIfEnvironmentVariable(named = Tests.ENABLE_VARIABLE, matches = "true")
@ExtendWith(Tests.WaitOnFailExtension.class)
@Slf4j
public abstract class Tests {
    static final String ENABLE_VARIABLE = "ERTH_SSO_E2E_ENABLED";

    static final Path BASEDIR = Paths.get("..").toAbsolutePath().normalize();

    static final boolean IS_LOW_MEMORY = "true".equals(System.getenv().get("ERTH_SSO_E2E_LOW_MEMORY"));

    static final String MARIA_DB_HOST = "mariadb";

    static final String KEYCLOAK_HOST = "keycloak";

    static final String MOCKSERVER_HOST = "mockserver";

    @ClassRule
    public static final Network NETWORK = Network.newNetwork();

    @SuppressWarnings("resource")
    public static final MariaDBContainer<?> MARIA_DB = new MariaDBContainer<>(DockerImageName.parse("mariadb:10.11"))
            .withNetwork(NETWORK)
            .withNetworkAliases(MARIA_DB_HOST);

    public static final KeycloakContainer KEYCLOAK = new KeycloakContainer("quay.io/keycloak/keycloak:25.0.2")
            .dependsOn(MARIA_DB)
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

    @SuppressWarnings("resource")
    static final GenericContainer<?> SMTP = new GenericContainer<>(DockerImageName.parse("rnwood/smtp4dev:v3"))
            .withNetwork(NETWORK)
            .withExposedPorts(25);

    static final MockServerContainer MOCK_SERVER =
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

    static {
        if (!Boolean.TRUE.toString().equals(System.getenv(ENABLE_VARIABLE))) {
            try {
                initialize();
                log.info("Tests initialized");
            } catch (Exception e) {
                throw new ExceptionInInitializerError(e);
            }
        }
    }

    static void initialize() {
        List<GenericContainer<?>> containers = List.of(
                MARIA_DB, KEYCLOAK, KEYCLOAK_CONFIG_CLI, SMTP, MOCK_SERVER
        );
        containers.forEach(GenericContainer::start);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> containers.forEach(GenericContainer::stop)));

        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();

        String mockServerUrl = "http://" + MOCKSERVER_HOST + ":" + MOCK_SERVER.getExposedPorts().get(0);

        executeSQL(connection -> {
            for (Map.Entry<String, String> entry : Map.of(
                    "cities.url", mockServerUrl + "/cities/domains"
            ).entrySet()) {
                var statement = connection.prepareStatement("update APP_PROPERTIES set VALUE = ? where NAME = ?");
                statement.setString(1, entry.getValue());
                statement.setString(2, entry.getKey());
                statement.execute();
            }

            for (Map.Entry<String, String> entry : Map.of(
                    "urlDaDataRequestLocationIp", mockServerUrl + "/dadata/suggestions/api/4_1/rs/iplocate/address"
            ).entrySet()) {
                var statement = connection.prepareStatement("update SETTINGS set VALUE = ? where EXT_ID = ?");
                statement.setString(1, entry.getValue());
                statement.setString(2, entry.getKey());
                statement.execute();
            }


            for (var user : TestsUsers.values()) {
                var userId = getUserId(user);
                var statement = connection.prepareStatement(
                        "insert into CUSTOMER(id, name, update_time) values(?, 'tester', now())"
                );
                statement.setLong(1, user.getTomsId());
                statement.execute();
                statement = connection.prepareStatement(
                        "insert into USER_POST(id, user_id, toms_id, dmp_id, role_id) " +
                                "values (uuid(), ?, ?, uuid(), 1)"
                );
                statement.setString(1, userId);
                statement.setLong(2, user.getTomsId());
                statement.execute();
            }

            for (var client : TestsClients.values()) {
                var clientId = getClientId(client);
                var statement = connection.prepareStatement(
                        "INSERT INTO MAIN_REDIRECT_URIS (CLIENT_ID, URI) VALUES (?, 'http://localhost')"
                );
                statement.setString(1, clientId);
                statement.execute();
            }
        });
    }

    @SneakyThrows
    public static <T> T executeSQL(SQLFunction<T> function) {
        @Cleanup var connection = MARIA_DB.createConnection("");
        return function.apply(connection);
    }

    @SneakyThrows
    public static void executeSQL(SQLConsumer consumer) {
        @Cleanup var connection = MARIA_DB.createConnection("");
        consumer.consume(connection);
    }

    public static String getUserId(TestsUsers user) {
        return executeSQL(connection -> {
            var statement = connection.prepareStatement("select id from USER_ENTITY where USERNAME = ? and REALM_ID = ?");
            statement.setString(1, user.getUsername());
            statement.setString(2, user.getRealm().getId());
            var rs = statement.executeQuery();
            Assertions.assertTrue(rs.next());
            return rs.getString(1);
        });
    }

    public static String getClientId(TestsClients client) {
        return executeSQL(connection -> {
            var statement = connection.prepareStatement("select id from CLIENT where CLIENT_ID = ? and REALM_ID = ?");
            statement.setString(1, client.getClientId());
            statement.setString(2, client.getRealm().getId());
            var rs = statement.executeQuery();
            Assertions.assertTrue(rs.next());
            return rs.getString(1);
        });
    }

    @FunctionalInterface
    public interface SQLFunction<T> {
        T apply(Connection statement) throws Exception;
    }

    @FunctionalInterface
    public interface SQLConsumer {
        void consume(Connection statement) throws Exception;
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
}
