package ru.alamics.sso.keycloak.registration.userpost;

import javassist.NotFoundException;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import ru.alamics.sso.keycloak.mapper.DataMapper;
import ru.alamics.sso.keycloak.registration.mapper.UserModelUserMapper;
import ru.alamics.sso.registration.dto.ExternalSystemRoleDto;
import ru.alamics.sso.registration.dto.ExternalSystemRoleRequest;
import ru.alamics.sso.registration.dto.UserPostRequest;
import ru.alamics.sso.registration.dto.UserPostResponse;
import ru.alamics.sso.registration.model.MessageConstants;
import ru.alamics.sso.registration.model.User;
import ru.alamics.sso.registration.service.UserPostService;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.EntityManager;
import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;

import static ru.alamics.sso.registration.model.FormConstants.*;
import static ru.alamics.sso.registration.model.UserConstants.*;

@Slf4j
public class UserPostCheckProvider implements FormAction {
    private static final Long ROLE_ID = 1L;     //Соотаветсвует ЛПР
    private UserPostService userPostService;

    public UserPostCheckProvider() {
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

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        List<FormMessage> errors = new ArrayList<>();

        if (context.getUser() == null) {
            context.error(Errors.INVALID_REGISTRATION);
            context.validationError(formData,
                    List.of(new FormMessage("Регистрация временно недоступна, попробуйте повторить попытку позже")));
            return;
        }
        UserPostRequest userPostRequest = DataMapper.toUserPostRequest(context);
        if (userPostRequest.getUserId() == null || userPostRequest.getUserId().isBlank() ||
                userPostRequest.getTomsId() == null || userPostRequest.getTomsId().isBlank()) {
            errors.add(new FormMessage("Регистрация временно недоступна, попробуйте повторить попытку позже"));
        }

        if (!errors.isEmpty()) {
            context.error(Errors.INVALID_REGISTRATION);
            context.validationError(formData, errors);
        } else {
            context.success();
        }
    }

    @Override
    public void success(FormContext context) {
        UserPostRequest userPostRequest = DataMapper.toUserPostRequest(context);
        userPostRequest.setRoleId(ROLE_ID);
        try {
            UserPostResponse userPost = userPostService.save(userPostRequest);
            for (ExternalSystemRoleDto systemRoleDto : userPostService.getExternalSystemRoles()) {
                ExternalSystemRoleRequest systemRole = new ExternalSystemRoleRequest();
                systemRole.setUserPostId(userPost.getId());
                systemRole.setSystemRoleId(systemRoleDto.getId());
                userPostService.addSystemRole(systemRole);
            }
        } catch (NotFoundException e) {
            log.error(e.getMessage(), e);
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
