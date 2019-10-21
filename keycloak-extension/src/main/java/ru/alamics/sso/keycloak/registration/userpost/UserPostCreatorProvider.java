package ru.alamics.sso.keycloak.registration.userpost;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import ru.alamics.sso.registration.dto.UserPostRequest;
import ru.alamics.sso.registration.service.UserPostService;
import ru.alamics.sso.user.mapper.UserMapper;

import javax.naming.InitialContext;
import javax.naming.NamingException;

@Slf4j
public class UserPostCreatorProvider implements FormAction {
    public static final Long ROLE_ID = 1L;     //Соотаветсвует ЛПР
    private UserPostService userPostService;

    public UserPostCreatorProvider() {
        try {
            this.userPostService = (UserPostService) new InitialContext().lookup("java:global/domru-sso/" + UserPostService.class.getSimpleName());
        } catch (NamingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Something wrong with context");
        }
    }

    @Override
    public void buildPage(FormContext context, LoginFormsProvider form) {

    }

    @Override
    public void validate(ValidationContext context) {
        context.success();
    }

    @Override
    public void success(FormContext context) {
        UserPostRequest userPostRequest = UserMapper.toUserPostRequest(context);
        if (userPostRequest != null && userPostRequest.getTomsId() != null) {
            userPostRequest.setRoleId(ROLE_ID);
            userPostService.addUserPostAndSystemRole(userPostRequest);
        }
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }

    @Override
    public void close() {

    }
}
