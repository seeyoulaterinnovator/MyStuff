package ru.alamics.sso.keycloak.cache;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import lombok.RequiredArgsConstructor;
import org.keycloak.connections.infinispan.InfinispanConnectionProvider;
import org.keycloak.events.admin.OperationType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resources.admin.AdminEventBuilder;

@RequiredArgsConstructor
public class CustomCacheResource {
    final KeycloakSession session;

    final AdminEventBuilder adminEvent;

    @Path("clear-customer-cache")
    @POST
    public void clearCustomerCache() {
        session.getProvider(InfinispanConnectionProvider.class).getCache("customer_cache").clear();
        adminEvent.operation(OperationType.ACTION).resourcePath(session.getContext().getUri()).success();
    }

    @Path("clear-user-post-cache")
    @POST
    public void clearUserPostCache() {
        session.getProvider(InfinispanConnectionProvider.class).getCache("user_post_cache").clear();
        adminEvent.operation(OperationType.ACTION).resourcePath(session.getContext().getUri()).success();
    }
}
