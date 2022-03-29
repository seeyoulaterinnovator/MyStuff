package ru.alamics.sso.keycloak.user.resource.session;

import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import ru.alamics.sso.keycloak.rest.BaseResourceProvider;

public class CustomSessionsProvider implements BaseResourceProvider<CustomSessions> {
    private final KeycloakSession session;


    public CustomSessionsProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public CustomSessions getResource() {
        try {
            AdminPermissionEvaluator auth = initAuthByWorkingRealm(this.session);
            return new CustomSessions(session, auth);
        } catch (Exception e) {
            throw new RuntimeException("Something wrong with context");
        }

    }
}
