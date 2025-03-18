package ru.alamics.sso.tools;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agroal.api.AgroalDataSource;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.commons.configuration.ClassAllowList;
import org.infinispan.jboss.marshalling.commons.GenericJBossMarshaller;
import org.keycloak.models.session.PersistentAuthenticatedClientSessionAdapter;
import org.keycloak.models.session.PersistentUserSessionAdapter;
import org.keycloak.models.sessions.infinispan.changes.SessionEntityWrapper;
import org.keycloak.models.sessions.infinispan.entities.AuthenticatedClientSessionEntity;
import org.keycloak.models.sessions.infinispan.entities.AuthenticatedClientSessionStore;
import org.keycloak.models.sessions.infinispan.entities.UserSessionEntity;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.IntStream;

@QuarkusMain
@Slf4j
public class SessionMigration implements QuarkusApplication {
    @Inject
    AgroalDataSource dataSource;

    final ObjectMapper mapper = new ObjectMapper();

    @Override
    public int run(String... args) {
        @Cleanup var cacheManager = getCacheManager();

        RemoteCache<?, SessionEntityWrapper<UserSessionEntity>> userSessionCache
                = cacheManager.getCache("sessions");
        int userSessionCount = userSessionCache.size();
        log.info("User session count: {}", userSessionCount);
        List<UserSessionEntity> userSessions = new ArrayList<>(userSessionCount);
        userSessionCache.forEach((key, wrapper) -> {
            userSessions.add(wrapper.getEntity());
            if(userSessions.size() % 1000 == 0) {
                log.info("Read user session count: {} ({}%)", userSessions.size(),
                        userSessions.size() * 100.0 / userSessionCount);
            }
        });

        batch("User session save", userSessions, this::saveUserSessions, 1000);

        RemoteCache<?, SessionEntityWrapper<AuthenticatedClientSessionEntity>> clientSessionCache
                = cacheManager.getCache("clientSessions");
        int clientSessionCount = clientSessionCache.size();
        log.info("Client session count: {}", clientSessionCount);
        List<AuthenticatedClientSessionEntity> clientSessions = new ArrayList<>(clientSessionCount);
        clientSessionCache.forEach((key, wrapper) -> {
            clientSessions.add(wrapper.getEntity());
            if(clientSessions.size() % 1000 == 0) {
                log.info("Read client session count: {} ({}%)", clientSessions.size(),
                        clientSessions.size() * 100.0 / clientSessionCount);
            }
        });

        batch("Client session save", clientSessions, (sessions) -> this.saveClientSessions(userSessions, sessions), 1000);

        return 0;
    }

    @Transactional
    void saveUserSessions(List<UserSessionEntity> sessions) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "insert into OFFLINE_USER_SESSION (USER_SESSION_ID, USER_ID, REALM_ID, CREATED_ON, " +
                            "OFFLINE_FLAG, DATA, LAST_SESSION_REFRESH, BROKER_SESSION_ID, VERSION) " +
                            "values (?, ?, ?, ?, ?, ?, ?, ?, ?);"
            )) {
                for (UserSessionEntity session : sessions) {
                    log.trace("User session {}: {}", session.getId(), session);
                    statement.setString(1, session.getId()); // USER_SESSION_ID
                    statement.setString(2, session.getUser()); // USER_ID
                    statement.setString(3, session.getRealmId()); // REALM_ID
                    statement.setInt(4, session.getStarted()); // CREATED_ON
                    statement.setString(5, "0"); // OFFLINE_FLAG
                    // DATA
                    statement.setString(6, mapper.writeValueAsString(UserSessionData.builder()
                            .brokerSessionId(session.getBrokerSessionId())
                            .brokerUserId(session.getBrokerUserId())
                            .ipAddress(session.getIpAddress())
                            .authMethod(session.getAuthMethod())
                            .rememberMe(session.isRememberMe())
                            .started(session.getStarted())
                            .notes(session.getNotes())
                            .state(session.getState() != null ? session.getState().toString() : null)
                            .loginUsername(session.getLoginUsername())
                            .build()));
                    statement.setInt(7, session.getLastSessionRefresh()); // LAST_SESSION_REFRESH
                    statement.setString(8, session.getBrokerSessionId()); // BROKER_SESSION_ID
                    statement.setInt(9, 0); // VERSION
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    void saveClientSessions(List<UserSessionEntity> allUserSessions, List<AuthenticatedClientSessionEntity> sessions) {
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(
                    "insert into OFFLINE_CLIENT_SESSION (USER_SESSION_ID, CLIENT_ID, OFFLINE_FLAG, TIMESTAMP, " +
                            "DATA, CLIENT_STORAGE_PROVIDER, EXTERNAL_CLIENT_ID, VERSION)" +
                            " values (?, ?, ?, ?, ?, ?, ?, ?);"
            )) {
                for (AuthenticatedClientSessionEntity session : sessions) {
                    log.trace("Client session {}: {}", session.getId(), session);
                    UserSessionEntity userSession = allUserSessions.stream()
                            .filter(s -> s.getAuthenticatedClientSessions().keySet().stream()
                                    .anyMatch(k -> s.getAuthenticatedClientSessions().get(k).equals(session.getId())))
                            .findAny()
                            .orElse(null);
                    if (userSession == null) {
                        log.info("User session not found for client session {}", session.getId());
                        continue;
                    }
                    statement.setString(1, userSession.getId()); // USER_SESSION_ID
                    statement.setString(2, session.getClientId()); // CLIENT_ID
                    statement.setString(3, "0"); // OFFLINE_FLAG
                    statement.setInt(4, session.getTimestamp()); // TIMESTAMP
                    // DATA
                    statement.setString(5, mapper.writeValueAsString(ClientSessionData.builder()
                            .authMethod(session.getAuthMethod())
                            .redirectUri(session.getRedirectUri())
                            .notes(session.getNotes())
                            .action(session.getAction())
                            .build()));
                    statement.setString(6, "local"); // CLIENT_STORAGE_PROVIDER
                    statement.setString(7, "local"); // EXTERNAL_CLIENT_ID
                    statement.setInt(8, 0); // VERSION
                    statement.addBatch();
                }
                statement.executeBatch();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    <T> void batch(String message, List<T> items, Consumer<List<T>> consumer, int count) {
        AtomicInteger consumed = new AtomicInteger();
        IntStream.range(0, (items.size() + count - 1) / count)
                .mapToObj(i -> items.subList(i * count, Math.min(items.size(), (i + 1) * count)))
                .forEach(batch -> {
                    log.info("{}, count: {} ({}%)", message, consumed.get(), consumed.get() * 100.0 / items.size());
                    consumer.accept(batch);
                    consumed.addAndGet(batch.size());
                });
    }

    RemoteCacheManager getCacheManager() {
        ClassAllowList classAllowList = new ClassAllowList();
        classAllowList.addClasses(
                UUID.class,
                SessionEntityWrapper.class,
                SessionEntityWrapper.ExternalizerImpl.class,
                UserSessionEntity.class,
                UserSessionEntity.ExternalizerImpl.class,
                AuthenticatedClientSessionStore.class,
                AuthenticatedClientSessionStore.ExternalizerImpl.class,
                AuthenticatedClientSessionEntity.class,
                AuthenticatedClientSessionEntity.ExternalizerImpl.class
        );

        GenericJBossMarshaller marshaller = new GenericJBossMarshaller();
        marshaller.initialize(classAllowList);

        ConfigurationBuilder builder = new ConfigurationBuilder()
                .batchSize(64)
                .socketTimeout(30_000)
                .addServer()
                .host(System.getenv("KC_CACHE_REMOTE_HOST"))
                .port(Integer.parseInt(System.getenv("KC_CACHE_REMOTE_PORT")))
                .security()
                .authentication()
                .username(System.getenv("KC_CACHE_REMOTE_USERNAME"))
                .password(System.getenv("KC_CACHE_REMOTE_PASSWORD"))
                .clientIntelligence(ClientIntelligence.BASIC)
                .marshaller(marshaller);
        builder.remoteCache("sessions")
                .configuration("<distributed-cache name=\"sessions\">" +
                        "<encoding><key media-type=\"application/x-java-object\"/>" +
                        "<value media-type=\"application/x-java-object\"/>" +
                        "</encoding>" +
                        "</distributed-cache>")
                .marshaller(marshaller);
        builder.remoteCache("clientSessions")
                .configuration("<distributed-cache name=\"clientSessions\">" +
                        "<encoding><key media-type=\"application/x-java-object\"/>" +
                        "<value media-type=\"application/x-java-object\"/>" +
                        "</encoding>" +
                        "</distributed-cache>")
                .marshaller(marshaller);
        return new RemoteCacheManager(builder.build());
    }

    /**
     * @see PersistentUserSessionAdapter
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    static class UserSessionData {
        @JsonProperty("brokerSessionId")
        String brokerSessionId;

        @JsonProperty("brokerUserId")
        String brokerUserId;

        @JsonProperty("ipAddress")
        String ipAddress;

        @JsonProperty("authMethod")
        String authMethod;

        @JsonProperty("rememberMe")
        boolean rememberMe;

        @JsonProperty("started")
        int started;

        @JsonProperty("notes")
        Map<String, String> notes;

        @JsonProperty("state")
        String state;

        @JsonProperty("loginUsername")
        String loginUsername;
    }

    /**
     * @see PersistentAuthenticatedClientSessionAdapter
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    static class ClientSessionData {
        @JsonProperty("authMethod")
        String authMethod;

        @JsonProperty("redirectUri")
        String redirectUri;

        @JsonProperty("notes")
        Map<String, String> notes;

        @JsonProperty("action")
        String action;
    }
}
