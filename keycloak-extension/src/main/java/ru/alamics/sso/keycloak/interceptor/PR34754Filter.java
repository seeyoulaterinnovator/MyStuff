package ru.alamics.sso.keycloak.interceptor;

import io.quarkus.resteasy.reactive.server.runtime.QuarkusResteasyReactiveRequestContext;
import io.quarkus.runtime.util.ExceptionUtil;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.jboss.resteasy.reactive.server.spi.ResteasyReactiveContainerRequestContext;
import org.keycloak.cookie.CookieProvider;
import org.keycloak.cookie.CookieType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.managers.AuthenticationManager;

import java.io.IOException;

/**
 * <a href="https://github.com/keycloak/keycloak/pull/34754/files">Fix NullPointerException if no session attached when authenticating with identity cookie</a>
 */
@Provider
@Slf4j
public class PR34754Filter implements ContainerResponseFilter {
    @Context
    KeycloakSession keycloak;

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
        if (request instanceof ResteasyReactiveContainerRequestContext context1) {
            if (context1.getServerRequestContext() instanceof QuarkusResteasyReactiveRequestContext context2) {
                var exception = context2.getThrowable();
                if (exception != null) {
                    exception = ExceptionUtil.getRootCause(exception);
                    if (exception.getStackTrace() != null && exception.getStackTrace().length > 0) {
                        var element = exception.getStackTrace()[0];
                        if ("org.keycloak.services.managers.AuthenticationManager".equals(element.getClassName())
                                && "authenticateIdentityCookie".equals(element.getMethodName())
                                && ("Cannot invoke " +
                                "\"org.keycloak.models.UserSessionModel.setLastSessionRefresh(int)\" " +
                                "because the return value of " +
                                "\"org.keycloak.services.managers.AuthenticationManager$AuthResult.getSession()\"" +
                                " is null").equals(exception.getMessage())
                        ) {
                            log.warn("PR34754 error: {}", request.getUriInfo().getRequestUri());
                            if(!request.getUriInfo().getQueryParameters().containsKey("redirect")) {
                                var cookieProvider = keycloak.getProvider(CookieProvider.class);
                                cookieProvider.set(CookieType.IDENTITY, "", 0);
                                cookieProvider.set(CookieType.SESSION, "", 0);
                                response.setStatus(HttpStatus.SC_MOVED_TEMPORARILY);
                                response.getHeaders().clear();
                                response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN);
                                response.getHeaders().add(HttpHeaders.LOCATION,
                                        request.getUriInfo()
                                                .getRequestUriBuilder()
                                                .queryParam("redirect", "true")
                                                .build()
                                );
                                response.setEntity("");
                            }
                        }
                    }
                }
            }
        }
    }
}
