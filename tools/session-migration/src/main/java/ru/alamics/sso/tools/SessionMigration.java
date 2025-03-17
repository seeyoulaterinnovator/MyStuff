package ru.alamics.sso.tools;

import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.transaction.Transactional;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.commons.configuration.ClassAllowList;
import org.infinispan.jboss.marshalling.commons.GenericJBossMarshaller;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.models.sessions.infinispan.changes.SessionEntityWrapper;
import org.keycloak.models.sessions.infinispan.entities.AuthenticatedClientSessionEntity;
import org.keycloak.models.sessions.infinispan.entities.UserSessionEntity;

import java.util.UUID;

@QuarkusMain
@Slf4j
public class SessionMigration implements QuarkusApplication {
    @Override
    public int run(String... args) {
        @Cleanup var cacheManager = getCacheManager();
        RemoteCache<?, UserSessionModel> userSessionCache = cacheManager.getCache("sessions");
        RemoteCache<?, AuthenticatedClientSessionModel> clientSessionCache = cacheManager.getCache("sessions");
        log.info("User session count: {}", userSessionCache.size());
        log.info("Client session count: {}", clientSessionCache.size());
        userSessionCache.forEach((id, session) -> {
            try {
                log.info("User session {}: {}", id, session);
                save(session);
            } catch (Exception e) {
                log.error("Save user session {} failed: {}", id, e.getMessage(), e);
            }
        });
        clientSessionCache.forEach((id, session) -> {
            try {
                log.info("Client session {}: {}", id, session);
                save(session);
            } catch (Exception e) {
                log.error("Save client session {} failed: {}", id, e.getMessage(), e);
            }
        });
        return 0;
    }

    @Transactional
    void save(UserSessionModel session) {
        // TODO
    }

    @Transactional
    void save(AuthenticatedClientSessionModel session) {
        // TODO
    }

    RemoteCacheManager getCacheManager() {
        ConfigurationBuilder builder = new ConfigurationBuilder()
                .addServer()
                .host(System.getenv("KC_CACHE_REMOTE_HOST"))
                .port(Integer.parseInt(System.getenv("KC_CACHE_REMOTE_PORT")))
                .security()
                .authentication()
                .username(System.getenv("KC_CACHE_REMOTE_USERNAME"))
                .password(System.getenv("KC_CACHE_REMOTE_PASSWORD"))
                .clientIntelligence(ClientIntelligence.BASIC);
        ClassAllowList classAllowList = new ClassAllowList();
        classAllowList.addClasses(
                UUID.class,
                SessionEntityWrapper.class,
                SessionEntityWrapper.ExternalizerImpl.class,
                UserSessionEntity.class,
                UserSessionEntity.ExternalizerImpl.class,
                AuthenticatedClientSessionEntity.class,
                AuthenticatedClientSessionEntity.ExternalizerImpl.class
        );
        classAllowList.addClasses(".*");
        GenericJBossMarshaller marshaller = new GenericJBossMarshaller();
        marshaller.initialize(classAllowList);
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
                        "</distributed-cache>");
        return new RemoteCacheManager(builder.build());
    }
}
