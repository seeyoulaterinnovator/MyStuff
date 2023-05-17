package ru.alamics.sso.keycloak.auth.form.new_auth.new_rest;

import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import ru.alamics.sso.registration.dto.UserPostResponse;

import javax.ws.rs.core.MultivaluedMap;
import java.util.List;
import java.util.Map;

public interface RestAuthHelper {

    static boolean isRestRequestValid(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> forms = context.getHttpRequest().getDecodedFormParameters();
        final boolean isPassword = forms.containsKey("password");
        final boolean isTwoStep = forms.containsKey("viaPhoneOnly");

        if ((isPassword && isTwoStep) || (!isPassword && !isTwoStep)) {
            return false;
        }

        if (isTwoStep) {
            return forms.getFirst("viaPhoneOnly").equals("phone_verificator_sms") || forms.getFirst("viaPhoneOnly").equals("incoming_call_phone_verificator");
        }
        return true;
    }

    static void chooseYourDestiny(Map<String, String> entity, AuthenticationSessionModel authenticationSession) {
        if (authenticationSession.getAuthNote("error_code") != null) {
            entity.put("wrong code", "wrong sms or phone code");
        }
        if (authenticationSession.getAuthNote("unable_to_send") != null) {
            entity.put("service_error", authenticationSession.getAuthNote("unable_to_send"));
        }
        if (authenticationSession.getAuthNote("rest_post") != null) {
            entity.put("post_list", authenticationSession.getAuthNote("rest_post"));
        }
    }

    static void setEmailSetter(UserModel user) {
        if (!user.isEmailVerified()) user.addRequiredAction("email_sender");
    }

    static void setPostSelector(UserModel user, List<UserPostResponse> attributes) {
        if (!attributes.isEmpty() && attributes.size() > 1) user.addRequiredAction("rest_post_selector");
    }

    static void setSecondPhaseAuth(AuthenticationFlowContext context, UserModel user) {
        MultivaluedMap<String, String> forms = context.getHttpRequest().getDecodedFormParameters();
        String reqAction = forms.getFirst("viaPhoneOnly");
        user.addRequiredAction(reqAction);
        user.removeRequiredAction(reqAction.equals("phone_verificator_sms") ? "incoming_call_phone_verificator" : "phone_verificator_sms");
    }


}
