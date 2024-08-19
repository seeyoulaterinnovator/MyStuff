package ru.alamics.sso.keycloak.auth.form.new_auth.newAuthReqActions;

import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.UserModel;
import ru.alamics.sso.keycloak.auth.form.new_auth.SsoUtil;

@Slf4j
public class EmailReqAction implements RequiredActionProvider {

    public static final String PROVIDER_ID = "email_sender";

    private static final String BLANK_PAGE = "blank-page.ftl";

    @Override
    public void evaluateTriggers(RequiredActionContext context) {
    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        UserModel user = context.getUser();
        if (user.getFirstAttribute("phone") == null) {
            context.form().setInfo("Ваш E-mail успешно подтверждён!");
        } else {
            context.form().setInfo("На указанный E-mail отправлена инструкция для подтверждения данных.");
        }
        log.info("called requiredActionChallenge");
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
