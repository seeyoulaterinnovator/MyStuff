package ru.alamics.sso.keycloak.registration.phone;

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
import ru.alamics.sso.registration.model.User;

import javax.persistence.EntityManager;
import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static ru.alamics.sso.registration.model.FormConstants.FIELD_EMAIL;
import static ru.alamics.sso.registration.model.FormConstants.USER_ATTRIBUTES_PHONE;
import static ru.alamics.sso.registration.model.UserConstants.ATTR_PHONE_NAME;

public class PhoneCheckProvider implements FormAction {

    private final EntityManager em;

    public PhoneCheckProvider(EntityManager em) {
        this.em = em;
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
                .phone(formData.getFirst(USER_ATTRIBUTES_PHONE))
                .build();

        if (user.getPhone() == null || user.getPhone().isBlank()) {
            formData.remove(USER_ATTRIBUTES_PHONE);
            context.getEvent().detail("Phone", user.getPhone());
            errors.add(new FormMessage(USER_ATTRIBUTES_PHONE, "Телефон должен быть заполнен"));
        } else {
            Long count = em.createQuery("select count(u.id) from UserAttributeEntity u " +
                    "where u.name = 'phone' and u.value like '%' || :phone || '%'", Long.class)
                    .setParameter("phone", user.getPhone())
                    .getSingleResult();

            if (count > 0) {
                formData.remove(USER_ATTRIBUTES_PHONE);
                context.getEvent().detail("Phone", user.getPhone());
                errors.add(new FormMessage(USER_ATTRIBUTES_PHONE, "Пользователь с таким телефоном уже существует"));
            }
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
        user.setAttribute(ATTR_PHONE_NAME, Collections.singletonList(formData.getFirst(USER_ATTRIBUTES_PHONE)));
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
