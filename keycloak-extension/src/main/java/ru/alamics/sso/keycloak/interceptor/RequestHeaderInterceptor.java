package ru.alamics.sso.keycloak.interceptor;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
@PreMatching
public class RequestHeaderInterceptor implements ContainerRequestFilter {
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String name = "X-Forwarded-Proto";
        String value = System.getenv("APP_FORWARDED_PROTO_HEADER");
        if (value != null && !value.isBlank()
                && requestContext.getHeaders().keySet().stream().noneMatch(name::equalsIgnoreCase)) {
            requestContext.getHeaders().putSingle(name, value);
        }
    }
}
