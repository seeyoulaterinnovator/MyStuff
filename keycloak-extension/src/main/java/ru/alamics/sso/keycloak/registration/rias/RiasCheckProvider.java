package ru.alamics.sso.keycloak.registration.rias;

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
import org.keycloak.services.messages.Messages;
import ru.alamics.sso.keycloak.registration.mapper.UserModelUserMapper;
import ru.alamics.sso.registration.model.MessageConstants;
import ru.alamics.sso.registration.model.User;
import ru.alamics.sso.registration.rias.RiasService;
import ru.alamics.sso.util.Util;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;

import static ru.alamics.sso.registration.model.FormConstants.FIELD_EMAIL;
import static ru.alamics.sso.registration.model.FormConstants.FIELD_PHONE;

public class RiasCheckProvider implements FormAction {

    public static final String RIAS_REJECTED = "rias.rejected";
    private final RiasService riasService;

    public RiasCheckProvider(RiasService riasService) {
        this.riasService = riasService;
    }

    @Override
    public void buildPage(FormContext context, LoginFormsProvider form) {

    }

    @Override
    public void validate(ValidationContext context) {

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        List<FormMessage> errors = new ArrayList<>();

        context.getEvent().detail(Details.REGISTER_METHOD, "form");
        String eventError = Errors.INVALID_REGISTRATION;

        User user = User.builder()
                .email(formData.getFirst(FIELD_EMAIL))
                .phone(formData.getFirst(FIELD_PHONE))
                .build();

        boolean emailCheck = riasService.checkEmail(user);

        boolean phoneCheck = riasService.checkPhone(user);

        if (emailCheck) {
            formData.remove(FIELD_EMAIL);
            context.getEvent().detail(Details.EMAIL, user.getEmail());
            errors.add(new FormMessage(FIELD_EMAIL, Messages.EMAIL_EXISTS));
        }

        if (phoneCheck) {
            formData.remove(FIELD_PHONE);
            context.getEvent().detail("Phone", user.getPhone());
            errors.add(new FormMessage(FIELD_PHONE, MessageConstants.PHONE_EXISTS));
        }

        if (!errors.isEmpty()) {
            context.error(eventError);
            context.validationError(formData, errors);
            context.getAuthenticationSession().setAuthNote(RIAS_REJECTED, Util.TRUE_STR);
        } else {
            context.success();
        }
    }

    @Override
    public void success(FormContext context) {
        UserModelUserMapper.fillAttributesFromContext(context.getUser(), context.getHttpRequest());
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
