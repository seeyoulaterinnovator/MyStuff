package ru.alamics.sso.keycloak.auth.requiredactions.stats;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.sessions.AuthenticationSessionModel;
import ru.alamics.sso.auth_n_regi.AuthOrRegTypeNotFoundException;
import ru.alamics.sso.registration.model.User;
import ru.alamics.sso.registration.service.AuthorisedUsersService;
import ru.alamics.sso.stats.LoginHistory;

import java.util.Objects;

import static ru.alamics.sso.keycloak.auth.form.new_auth.SsoUtil.getAuthOrRegType;

@Slf4j
public class LoginStatsRecording implements RequiredActionProvider {

    private final LoginHistory loginHistoryService;
    private final AuthorisedUsersService authorisedUsersService;

    public LoginStatsRecording(LoginHistory loginHistoryService, AuthorisedUsersService authorisedUsersService) {
        this.loginHistoryService = loginHistoryService;
        this.authorisedUsersService = authorisedUsersService;
    }

    @Override
    public void evaluateTriggers(RequiredActionContext context) {
        final String DEBUG_STR = "evaluateTriggers";
        UserModel user = context.getUser();
        Objects.requireNonNull(user);
        log.debug("{}: username={}", DEBUG_STR, user.getUsername());
        log.info("evaluateTriggers");
        try {
            recordRecentLogin(user);
        } catch (AuthOrRegTypeNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        context.success();
    }

    @Override
    public void processAction(RequiredActionContext context) {
    }

    private void recordRecentLogin(UserModel model) throws AuthOrRegTypeNotFoundException {
        UserEntity entity = new UserEntity();
        entity.setId(model.getId());
        log.info("!recordRecentLogin!, UserEntity entity is : " + entity);
        loginHistoryService.create(entity);
//        String clientId = authSession.getClient().getClientId();
        String clientId = "AAA";
//        String clientId = model.get;
        authorisedUsersService.saveSuccessfulAuth(User.builder().build(), entity.getRealmId(), clientId, getAuthOrRegType((AuthenticationSessionModel) model));
    }

    @Override
    public void close() {

    }
}
