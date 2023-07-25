package ru.alamics.sso.keycloak.auth.form.new_auth.new_rest.reg;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;
import ru.alamics.sso.keycloak.rest.BaseResourceProviderFactory;

public class RegistrationResourceProviderFactory implements BaseResourceProviderFactory {

    private static final String PROVIDER_ID = "rest-reg";

    @Override
    public RealmResourceProvider create(KeycloakSession session) {
        return new RestRegResourceProvider(session);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
