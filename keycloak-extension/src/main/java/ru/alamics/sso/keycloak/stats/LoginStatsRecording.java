package ru.alamics.sso.keycloak.stats;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.Config;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionFactory;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.stats.LoginHistory;

import javax.ejb.EJB;
import java.util.Objects;

@Slf4j
public class LoginStatsRecording implements RequiredActionProvider, RequiredActionFactory {

    @EJB
    private LoginHistory loginHistory;

    private static final String PROVIDER_ID = "login_stats_recordings";
    private static final String RECORD_LOGIN_STATISTICS_ACTION = "Record Login Statistics Action";
    private static final LoginStatsRecording INSTANCE = new LoginStatsRecording();

    @Override
    public String getDisplayText () {
        return RECORD_LOGIN_STATISTICS_ACTION;
    }

    @Override
    public void evaluateTriggers (RequiredActionContext context) {
        final String DEBUG_STR = "evaluateTriggers";
        var user = context.getUser();
        Objects.requireNonNull(user);
        log.debug("{}: username={}", DEBUG_STR, user.getUsername());

        recordRecentLogin(user);
    }

    @Override
    public void requiredActionChallenge (RequiredActionContext context) {
        context.success();
    }

    @Override
    public void processAction (RequiredActionContext context) {

    }

    @Override
    public RequiredActionProvider create (KeycloakSession session) {
        return INSTANCE;
    }

    @Override
    public void init (Config.Scope config) {
        log.debug("Creating IdM Keycloak extension {}:", this);
    }

    @Override
    public void postInit (KeycloakSessionFactory factory) {

    }

    @Override
    public void close () {

    }

    @Override
    public String getId () {
        return PROVIDER_ID;
    }

    private void recordRecentLogin(UserModel model) {
        UserEntity entity = new UserEntity();
        entity.setId(model.getId());

        loginHistory.create(entity);
    }
}
