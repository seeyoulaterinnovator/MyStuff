package ru.alamics.sso.keycloak.auth.form.new_auth.newAuthReqActions;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.sessions.AuthenticationSessionModel;
import ru.alamics.sso.keycloak.auth.form.new_auth.SsoUtil;

import javax.ws.rs.core.Response;
@Slf4j
public class EmailReqAction implements RequiredActionProvider {

    public static final String PROVIDER_ID = "email_sender";

    private static final String BLANK_PAGE = "blank-page.ftl";

    @Override
    public void evaluateTriggers(RequiredActionContext context) {
    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        context.form().setInfo("Проверка На указанный E-mail отправлена инструкция для подтверждения данных");
//       гипотеза: этот метод вызывается при регистрации через сайт, проверка с помощью добавления логирования
        log.info(" is call ! !");
        SsoUtil.sendEmailVer(context);
        context.challenge(createForm(context));
    }

    @Override
    public void processAction(RequiredActionContext context)  {
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