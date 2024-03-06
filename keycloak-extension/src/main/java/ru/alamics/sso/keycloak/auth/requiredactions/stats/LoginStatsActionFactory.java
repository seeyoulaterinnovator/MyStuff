package ru.alamics.sso.keycloak.auth.requiredactions.stats;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.Config;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.auth.requiredactions.AbstractRequiredActionFactory;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.registration.service.AuthorisedUsersService;
import ru.alamics.sso.stats.LoginHistory;

@Slf4j
public class LoginStatsActionFactory extends AbstractRequiredActionFactory {

    private final AuthorisedUsersService authorisedUsersService;
    private static final String PROVIDER_ID = "login_stats_recordings";
    private static final String RECORD_LOGIN_STATISTICS_ACTION = "Record Login Statistics Action";

    public LoginStatsActionFactory(AuthorisedUsersService authorisedUsersService) {
        this.authorisedUsersService = authorisedUsersService;
    }

    @Override
    public String getDisplayText() {
        return RECORD_LOGIN_STATISTICS_ACTION;
    }

    @Override
    public RequiredActionProvider create(KeycloakSession session) {
        return new LoginStatsRecording(Lookup.lookup(LoginHistory.class), authorisedUsersService);
    }

    @Override
    public void init(Config.Scope config) {
        log.debug("Creating IdM Keycloak extension {}:", this);
    }

    @Override
    public String getId() {
        return PROVIDER_ID;
    }
}
