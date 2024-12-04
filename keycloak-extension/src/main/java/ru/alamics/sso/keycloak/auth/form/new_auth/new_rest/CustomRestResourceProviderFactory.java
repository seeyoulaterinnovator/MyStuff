package ru.alamics.sso.keycloak.auth.form.new_auth.new_rest;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.Config;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.services.resource.RealmResourceProvider;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import ru.alamics.sso.keycloak.rest.BaseResourceProvider;
import ru.alamics.sso.keycloak.rest.BaseResourceProviderFactory;

@Slf4j
public class CustomRestResourceProviderFactory implements BaseResourceProviderFactory, BaseResourceProvider {

    private static final String PROVIDER_ID = "api-v1";

    private KeycloakSession session;
    private AdminPermissionEvaluator auth;

    @Override
    public String getId() {
        return PROVIDER_ID;
    }

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        this.auth = this.initAuth(session, true);
        this.session = session;
        return this;
    }

    @Override
    public Object getResource() {
        return new CustomNewRestResource(session, this.auth);
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public void close() {
    }
}
