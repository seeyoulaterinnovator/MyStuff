package ru.alamics.sso.keycloak.interceptor;

import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.ext.Provider;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.status.StatusService;

@Provider
@PreMatching
public class DevHeaderInterceptor implements ContainerResponseFilter {
    @Inject
    ApplicationProperties properties;

    @Inject
    StatusService statusService;

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        if("true".equals(properties.getProperty("dev.headersEnabled"))) {
            responseContext.getHeaders().add("X-NodeName", statusService.getNodeName());
        }
    }
}
