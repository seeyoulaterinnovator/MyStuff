package ru.alamics.sso.keycloak.auth.form.new_auth.newAuthReqActions;

import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.forms.login.LoginFormsProvider;
import ru.alamics.sso.keycloak.auth.form.new_auth.SsoUtil;
import javax.ws.rs.core.Response;
public class EmailReqAction implements RequiredActionProvider {

    public static final String PROVIDER_ID = "email_sender";

    private static final String BLANK_PAGE = "blank-page.ftl";

    @Override
    public void evaluateTriggers(RequiredActionContext context) {
    }
    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        context.form().setInfo("");
        SsoUtil.sendEmailVer(context);
        context.challenge(createForm(context));
    }
    @Override
    public void processAction(RequiredActionContext context) {
        context.success();
    }
    @Override
    public void close() {
    }

    private Response createForm(RequiredActionContext context) {
        LoginFormsProvider form = context.form();
        return form.createForm(BLANK_PAGE);
    }

}