package ru.alamics.sso.keycloak.interceptor;

import io.quarkus.resteasy.reactive.server.runtime.QuarkusResteasyReactiveRequestContext;
import io.quarkus.runtime.util.ExceptionUtil;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.server.spi.ResteasyReactiveContainerRequestContext;

import java.io.IOException;

/**
 * Подавление внутренней ошибки KC25 при обновлении кэша realm после успешного удаления realm
 */
@Provider
@Slf4j
public class RealmDeleteFixFilter implements ContainerResponseFilter {
    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
        if(!(request.getUriInfo().getPath().matches("/admin/realms/\\w+") && request.getMethod().equals("DELETE"))) return;

        if (!(request instanceof ResteasyReactiveContainerRequestContext context1)) return;

        if (!(context1.getServerRequestContext() instanceof QuarkusResteasyReactiveRequestContext context2)) return;

        var originalException = context2.getThrowable();
        if (originalException == null) return;

        var exception = ExceptionUtil.getRootCause(originalException);

        if (exception.getStackTrace() == null || exception.getStackTrace().length == 0) return;

        var element = exception.getStackTrace()[0];

        if(!("org.keycloak.models.sessions.infinispan.events.AbstractAuthSessionClusterListener".equals(element.getClassName()))) return;

        if(!"Cannot invoke \"org.keycloak.models.sessions.infinispan.InfinispanAuthenticationSessionProvider.getCache()\" because \"provider\" is null".equals(exception.getMessage())) return;

        log.warn("Exception suppressed by KC over fix: {}", originalException.getMessage(), originalException);

        response.setStatus(200);
        response.setEntity("");
    }
}
