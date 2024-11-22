package ru.alamics.sso.config;

import io.quarkus.arc.impl.Reflections;
import io.quarkus.dev.appstate.ApplicationStateNotification;
import io.quarkus.vertx.http.runtime.VertxHttpRecorder;
import io.smallrye.config.ConfigValue;
import io.vertx.core.http.HttpHeaders;
import io.vertx.ext.web.Router;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.keycloak.quarkus.runtime.configuration.Configuration;

@ApplicationScoped
@Slf4j
public class RouterConfig {
    public void configureIndexRedirect(@Observes Router relativeRouter) {
        ConfigValue port = Configuration.getConfig().getConfigValue("kc.http-port");
        ConfigValue path = Configuration.getConfig().getConfigValue("kc.http-relative-path");

        if (port == null || path == null || path.getValue().replaceAll("/", "").isBlank()) return;

        new Thread(() -> {
            try {
                ApplicationStateNotification.waitForApplicationStart();
                Object rootHandler = Reflections.readField(VertxHttpRecorder.class, "rootHandler", null);
                Object rootDelegate = Reflections.readField(rootHandler.getClass(), "val$delegate", rootHandler);
                try {
                    rootDelegate = Reflections.readField(rootDelegate.getClass(), "val$old", rootDelegate);
                } catch (Exception e) {
                    log.trace(e.getMessage(), e);
                }
                Router rootRouter = (Router) Reflections.readField(rootDelegate.getClass(), "val$root", rootDelegate);
                rootRouter.errorHandler(HttpStatus.SC_NOT_FOUND, event -> {
                    if (event.request().path().equals("/")) {
                        event.response()
                                .setStatusCode(HttpStatus.SC_MOVED_TEMPORARILY)
                                .putHeader(HttpHeaders.LOCATION, path.getValue())
                                .end();
                    } else {
                        event.next();
                    }
                });
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }).start();
    }
}
