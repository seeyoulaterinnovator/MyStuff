package ru.alamics.sso.keycloak.auth.requiredactions.stats;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.stats.LoginHistory;

import java.util.Objects;

@Slf4j
public class LoginStatsRecording implements RequiredActionProvider {

    private final LoginHistory loginHistoryService;

    public LoginStatsRecording(LoginHistory loginHistoryService) {
        this.loginHistoryService = loginHistoryService;
    }

    @Override
    public void evaluateTriggers(RequiredActionContext context) {
        final String DEBUG_STR = "evaluateTriggers";
        UserModel user = context.getUser();
        Objects.requireNonNull(user);
        log.debug("{}: username={}", DEBUG_STR, user.getUsername());

        recordRecentLogin(user);
    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        context.success();
    }

    @Override
    public void processAction(RequiredActionContext context) {
    }

    private void recordRecentLogin(UserModel model) {
        UserEntity entity = new UserEntity();
        entity.setId(model.getId());
        log.info("!recordRecentLogin!, UserEntity entity is : " + entity);
        loginHistoryService.create(entity);
    }

    @Override
    public void close() {

    }
}
