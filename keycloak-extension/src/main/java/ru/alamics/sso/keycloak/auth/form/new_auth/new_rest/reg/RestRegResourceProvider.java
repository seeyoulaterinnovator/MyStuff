package ru.alamics.sso.keycloak.auth.form.new_auth.new_rest.reg;

import lombok.RequiredArgsConstructor;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.rest.BaseResourceProvider;

@RequiredArgsConstructor
public class RestRegResourceProvider implements BaseResourceProvider<RestRegResource> {

    private final KeycloakSession session;

    @Override
    public RestRegResource getResource() {
        return new RestRegResource(session);
    }

    @Override
    public void close() {

    }
}
