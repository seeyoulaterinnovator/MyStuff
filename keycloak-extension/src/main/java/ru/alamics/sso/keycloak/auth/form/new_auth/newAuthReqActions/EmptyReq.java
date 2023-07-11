package ru.alamics.sso.keycloak.auth.form.new_auth.newAuthReqActions;

import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.forms.login.LoginFormsProvider;

import javax.ws.rs.core.Response;

public class EmptyReq implements RequiredActionProvider {

    public static final String PROVIDER_ID = "empty_req";

    private static final String EMPTY_PAGE = "empty-page.ftl";

    public final static String CLIENT_B2B = "b2b";

    @Override
    public void evaluateTriggers(RequiredActionContext context) {
    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        if (context.getAuthenticationSession().getClient().getClientId().equals(CLIENT_B2B)) {
            context.challenge(createForm(context));
        } else {
            context.success();
        }
    }

    @Override
    public void processAction(RequiredActionContext context) {
        context.success();
    }

    private Response createForm(RequiredActionContext context) {
        LoginFormsProvider form = context.form();
        return form.createForm(EMPTY_PAGE);
    }

    @Override
    public void close() {

    }
}
