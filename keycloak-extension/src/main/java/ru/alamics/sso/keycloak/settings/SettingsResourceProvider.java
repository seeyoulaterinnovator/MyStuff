package ru.alamics.sso.keycloak.settings;

import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.rest.BaseResourceProvider;
import ru.alamics.sso.settings.SettingsService;

public class SettingsResourceProvider implements BaseResourceProvider<SettingsResource> {

    private final KeycloakSession session;

    public SettingsResourceProvider(KeycloakSession session) {
        this.session = session;
    }

    @Override
    public SettingsResource getResource() {
        initAuthByWorkingRealm(session).realm().requireManageRealm();
        SettingsService service = Lookup.lookup(SettingsService.class);
        return new SettingsResource(session, service);
    }
}
