package ru.alamics.sso.keycloak.auth.requiredactions.stats;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.sessions.AuthenticationSessionModel;
import org.keycloak.storage.user.UserLookupProvider;
import ru.alamics.sso.auth_n_regi.AuthOrRegTypeNotFoundException;
import ru.alamics.sso.keycloak.registration.mapper.UserModelUserMapper;
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

    @SneakyThrows
    @Override
    public void evaluateTriggers(RequiredActionContext context) {
        final String DEBUG_STR = "evaluateTriggers";
        UserModel user = context.getUser();
        Objects.requireNonNull(user);
        log.debug("{}: username={}", DEBUG_STR, user.getUsername());
        log.info("evaluateTriggers");

        recordRecentLogin(user, context);
    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        context.success();
    }

    @Override
    public void processAction(RequiredActionContext context) {
    }

    private void recordRecentLogin(UserModel model, RequiredActionContext context) throws AuthOrRegTypeNotFoundException {
        User user = UserModelUserMapper.mapToUser(context.getUser());
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        UserEntity entity = new UserEntity();
        log.info("model.getUsername() is : " + model.getUsername());
        entity.setId(model.getId());
        log.info("!recordRecentLogin!, UserEntity entity.getUsername is : " + entity.getUsername());
        loginHistoryService.create(entity);
        String clientId = authSession.getClient().getClientId();
        int typeId = getAuthOrRegType(authSession);
//
            // сюда заходит при авторизации логопасс в МП
        authorisedUsersService.saveSuccessfulAuth(user, context.getRealm().getName(), clientId, typeId);
    }

    @Override
    public void close() {

    }
}
