package ru.alamics.sso.keycloak.stats;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.Config;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.stats.LoginHistory;

@Slf4j
public class LoginStatsActionFactory implements RequiredActionFactory {
    private static final String PROVIDER_ID = "login_stats_recordings";
    private static final String RECORD_LOGIN_STATISTICS_ACTION = "Record Login Statistics Action";

    @Override
    public String getDisplayText() {
        return RECORD_LOGIN_STATISTICS_ACTION;
    }

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        LoginHistory login = (LoginHistory) Lookup.lookup(LoginHistory.class);
        return new LoginStatsRecording(login);
    }

    @Override
    public void init(Config.Scope config) {
        log.debug("Creating IdM Keycloak extension {}:", this);
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {

    }

    @Override
    public void close() {

    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
