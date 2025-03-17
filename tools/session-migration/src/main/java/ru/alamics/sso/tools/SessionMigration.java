package ru.alamics.sso.tools;

import io.quarkus.infinispan.client.Remote;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.infinispan.client.hotrod.RemoteCache;
import org.keycloak.models.AuthenticatedClientSessionModel;
import org.keycloak.models.UserSessionModel;

@QuarkusMain
@Slf4j
public class SessionMigration implements QuarkusApplication {
    @Inject
    @Remote("sessions")
    RemoteCache<String, UserSessionModel> userSessionCache;

    @Inject
    @Remote("clientSessions")
    RemoteCache<String, AuthenticatedClientSessionModel> clientSessionCache;

    @Override
    public int run(String... args) {
        log.info("User session count: {}", userSessionCache.size());
        log.info("Client session count{}", clientSessionCache.size());
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
}
