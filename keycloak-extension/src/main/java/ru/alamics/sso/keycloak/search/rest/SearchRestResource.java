package ru.alamics.sso.keycloak.search.rest;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resources.admin.AdminAuth;

import javax.ws.rs.Path;

public class SearchRestResource {

    private KeycloakSession session;
    private AdminAuth auth;

    public SearchRestResource(KeycloakSession session, AdminAuth auth) {
        this.session = session;
        this.auth = auth;
    }

    @Path("")
    public SearchResource getSearchResource() {
        return new SearchResource(session);
    }
}