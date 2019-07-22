package ru.alamics.sso.keycloak.registration.rias;

import org.keycloak.authentication.FormAction;
import org.keycloak.authentication.FormContext;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.authentication.forms.RegistrationPage;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.validation.Validation;
import ru.alamics.sso.keycloak.registration.mapper.UserModelUserMapper;
import ru.alamics.sso.registration.model.User;
import ru.alamics.sso.registration.rias.RiasService;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RiasCheckProvider implements FormAction {

    public static final String USER_ATTRIBUTES_PHONE = "user.attributes.phone";
    private final RiasService riasService;
    private final UserModelUserMapper mapper;

    public RiasCheckProvider(RiasService riasService, UserModelUserMapper mapper) {
        this.riasService = riasService;
        this.mapper = mapper;
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


//        User user = mapper.mapToUser(context.getUser());
        User user = User.builder()
                .email(formData.getFirst(RegistrationPage.FIELD_EMAIL))
                .phone(formData.getFirst(USER_ATTRIBUTES_PHONE))
                .build();

        boolean emailCheck = riasService.checkEmail(user);

        boolean phoneCheck = riasService.checkPhone(user);

        if (emailCheck) {
            formData.remove(Validation.FIELD_EMAIL);
            context.getEvent().detail(Details.EMAIL, user.getEmail());
            errors.add(new FormMessage(RegistrationPage.FIELD_EMAIL, Messages.EMAIL_EXISTS));
        }

        if (phoneCheck) {
            formData.remove(USER_ATTRIBUTES_PHONE);
            context.getEvent().detail("Phone", user.getPhone());
            errors.add(new FormMessage(USER_ATTRIBUTES_PHONE, "Пользователь с таким телефоном уже существкет"));
        }

        if (!errors.isEmpty()) {
            context.error(eventError);
            context.validationError(formData, errors);
        } else {
            context.success();
        }
    }

    @Override
    public void success(FormContext context) {
        UserModel user = context.getUser();
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        user.setAttribute("phone", Collections.singletonList(formData.getFirst(USER_ATTRIBUTES_PHONE)));
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
