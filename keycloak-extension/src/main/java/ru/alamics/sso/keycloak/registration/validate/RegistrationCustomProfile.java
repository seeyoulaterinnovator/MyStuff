package ru.alamics.sso.keycloak.registration.validate;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.ValidationContext;
import org.keycloak.authentication.forms.RegistrationPage;
import org.keycloak.authentication.forms.RegistrationProfile;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.models.utils.FormMessage;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.validation.Validation;

import javax.ws.rs.core.MultivaluedMap;
import java.util.ArrayList;
import java.util.List;

import static ru.alamics.sso.registration.model.FormConstants.FIELD_FIRST_NAME;
import static ru.alamics.sso.registration.model.FormConstants.FIELD_PHONE;
@Slf4j
public class RegistrationCustomProfile extends RegistrationProfile {

    private static final String DISPLAY_NAME = "Profile Custom Validation";

    @Override
    public String getDisplayType() {
        return DISPLAY_NAME;
    }

    @Override
    public void validate(ValidationContext context) {
        log.info("Profile Custom Validation");
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        List<FormMessage> errors = new ArrayList<>();

        context.getEvent().detail(Details.REGISTER_METHOD, "form");
        String eventError = Errors.INVALID_REGISTRATION;

        // empty is ok
        /*
        if (Validation.isBlank(formData.getFirst((RegistrationPage.FIELD_FIRST_NAME)))) {
            errors.add(new FormMessage(RegistrationPage.FIELD_FIRST_NAME, Messages.MISSING_FIRST_NAME));
        }
         */

        // empty is ok
        /*
        if (Validation.isBlank(formData.getFirst((RegistrationPage.FIELD_LAST_NAME)))) {
            errors.add(new FormMessage(RegistrationPage.FIELD_LAST_NAME, Messages.MISSING_LAST_NAME));
        }
        */
        formData.remove(RegistrationPage.FIELD_LAST_NAME);
        formData.add(RegistrationPage.FIELD_LAST_NAME, " ");
        formData.remove(FIELD_FIRST_NAME);
        formData.add(FIELD_FIRST_NAME, formData.getFirst(FIELD_PHONE));

        String email = formData.getFirst(Validation.FIELD_EMAIL);
        boolean emailValid = true;
        if (Validation.isBlank(email)) {
            errors.add(new FormMessage(RegistrationPage.FIELD_EMAIL, Messages.MISSING_EMAIL));
          //  context.getAuthenticationSession().setAuthNote("email_error", "email is missing");
            emailValid = false;
        } else if (!Validation.isEmailValid(email)) {
            context.getEvent().detail(Details.EMAIL, email);
            errors.add(new FormMessage(RegistrationPage.FIELD_EMAIL, Messages.INVALID_EMAIL));
           // context.getAuthenticationSession().setAuthNote("email_error", "email is not valid");
            emailValid = false;
        }

        if (emailValid && !context.getRealm().isDuplicateEmailsAllowed() && context.getSession().users().getUserByEmail(email, context.getRealm()) != null) {
            eventError = Errors.EMAIL_IN_USE;
            formData.remove(Validation.FIELD_EMAIL);
            context.getEvent().detail(Details.EMAIL, email);
         //   context.getAuthenticationSession().setAuthNote("dupl_email", "email already exists");
            errors.add(new FormMessage(RegistrationPage.FIELD_EMAIL, Messages.EMAIL_EXISTS));
        }

        if (errors.size() > 0) {
            context.error(eventError);
            context.validationError(formData, errors);
            return;

        } else {
            context.success();
        }
    }
}
