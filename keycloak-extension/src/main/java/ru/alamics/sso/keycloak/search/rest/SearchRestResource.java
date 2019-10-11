package ru.alamics.sso.keycloak.search.rest;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resources.admin.AdminAuth;

import javax.ws.rs.Path;

public class SearchRestResource {

    private KeycloakSession session;

    public SearchRestResource(KeycloakSession session) {
        this.session = session;
    }

    @Path("")
    public SearchResource getSearchResource() {
        return new SearchResource(session);
    }
}