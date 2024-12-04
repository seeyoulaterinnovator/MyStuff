package ru.alamics.sso.keycloak.registration.validate;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.authentication.forms.RegistrationPage;
import org.keycloak.authentication.forms.RegistrationPassword;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.policy.PasswordPolicyManagerProvider;
import org.keycloak.policy.PolicyError;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.validation.Validation;

import jakarta.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;
@Slf4j
public class RegistrationCustomPassword extends RegistrationPassword {

    private static final String DISPLAY_NAME = "Password Custom Validation";

    @Override
    public String getDisplayType() {
        return DISPLAY_NAME;
    }

    @Override
    public void validate(ValidationContext context) {
        log.info("Password Custom Validation");
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        List<FormMessage> errors = new ArrayList<>();
        context.getEvent().detail(Details.REGISTER_METHOD, "form");
        if (Validation.isBlank(formData.getFirst(RegistrationPage.FIELD_PASSWORD))) {
            errors.add(new FormMessage(RegistrationPage.FIELD_PASSWORD, Messages.MISSING_PASSWORD));
        }
        if (formData.getFirst(RegistrationPage.FIELD_PASSWORD) != null) {
            PolicyError err = context.getSession().getProvider(PasswordPolicyManagerProvider.class).validate(context.getRealm().isRegistrationEmailAsUsername() ? formData.getFirst(RegistrationPage.FIELD_EMAIL) : formData.getFirst(RegistrationPage.FIELD_USERNAME), formData.getFirst(RegistrationPage.FIELD_PASSWORD));
            if (err != null)
                errors.add(new FormMessage(RegistrationPage.FIELD_PASSWORD, err.getMessage(), err.getParameters()));
        }

        if (errors.size() > 0) {
            context.error(Errors.INVALID_REGISTRATION);
            formData.remove(RegistrationPage.FIELD_PASSWORD);
            context.validationError(formData, errors);
            return;
        } else {
            context.success();
        }
    }
}
