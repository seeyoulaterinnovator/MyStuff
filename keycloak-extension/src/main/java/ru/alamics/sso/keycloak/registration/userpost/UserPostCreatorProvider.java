package ru.alamics.sso.keycloak.registration.userpost;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import ru.alamics.sso.keycloak.facade.CachedUserPostFacade;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.registration.FoundUserPostException;
import ru.alamics.sso.registration.dto.UserPostRequest;
import ru.alamics.sso.user.mapper.UserMapper;
import ru.alamics.sso.util.validator.NotValidException;

import javax.ws.rs.NotFoundException;

@Slf4j
public class UserPostCreatorProvider implements FormAction {
    public static final Long ROLE_ID = 1L;     //Соотаветсвует ЛПР
    private final CachedUserPostFacade cachedUserPostFacade;

    public UserPostCreatorProvider() {
        this.cachedUserPostFacade = Lookup.lookup(CachedUserPostFacade.class);
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
            userPostRequest.setSelected(true);

            try {
                cachedUserPostFacade.addUserPostAndSystemRole(userPostRequest);
            } catch (NotFoundException | FoundUserPostException | NotValidException e) {
                log.error(e.getMessage(), e);
            }
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
