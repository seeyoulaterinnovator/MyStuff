package ru.alamics.sso.keycloak.search.rest;

import jakarta.ws.rs.Path;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resources.admin.AdminAuth;

public class SearchRestResource {

    private KeycloakSession session;
    private AdminAuth adminAuth;

    public SearchRestResource(KeycloakSession session, AdminAuth adminAuth) {
        this.session = session;
        this.adminAuth = adminAuth;
    }

    @Path("")
    public SearchResource getSearchResource() {
        return new SearchResource(session, adminAuth);
    }
}
