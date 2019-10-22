package ru.alamics.sso.keycloak.auth;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.specimpl.MultivaluedMapImpl;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.authentication.Authenticator;
import org.keycloak.authentication.authenticators.browser.AbstractUsernameFormAuthenticator;
import org.keycloak.events.Details;
import org.keycloak.events.Errors;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.*;
import org.keycloak.models.utils.KeycloakModelUtils;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.services.ServicesLogger;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.validation.Validation;
import ru.alamics.sso.keycloak.cities.CitiesResource;
import ru.alamics.sso.keycloak.cities.model.CityMigration;
import ru.alamics.sso.registration.model.FormConstants;
import ru.alamics.sso.registration.rias.RiasService;
import ru.alamics.sso.registration.rias.model.RiasLogin;
import ru.alamics.sso.registration.service.UserFindService;
import ru.alamics.sso.util.Util;

import javax.persistence.EntityManager;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static ru.alamics.sso.registration.model.UserConstants.AUTH_FORM_SUCCESS;

@Slf4j
public class AuthMailPhoneForm extends AbstractUsernameFormAuthenticator implements Authenticator {

    // TODO
    private final static String LKB2B_ID = "lkb2b";
    private final static String CONSOLE_ID = "security-admin-console";

    private final EntityManager em;
    private final RiasService riasService;
    private final UserFindService userFindService;

    public AuthMailPhoneForm(EntityManager em, RiasService riasService, UserFindService userFindService) {
        this.em = em;
        this.riasService = riasService;
        this.userFindService = userFindService;
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        if (formData.containsKey("cancel")) {
            context.cancelLogin();
            return;
        }
        if (!validateForm(context, formData)) {
            return;
        }
        context.getAuthenticationSession().setAuthNote(AUTH_FORM_SUCCESS, "1");
        context.success();
    }

    protected boolean validateForm(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        return validateUserAndPassword(context, formData);
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        MultivaluedMap<String, String> formData = new MultivaluedMapImpl<>();
        String loginHint = context.getAuthenticationSession().getClientNote(OIDCLoginProtocol.LOGIN_HINT_PARAM);

        String rememberMeUsername = AuthenticationManager.getRememberMeUsername(context.getRealm(), context.getHttpRequest().getHttpHeaders());

        if (loginHint != null || rememberMeUsername != null) {
            if (loginHint != null) {
                formData.add(AuthenticationManager.FORM_USERNAME, loginHint);
            } else {
                formData.add(AuthenticationManager.FORM_USERNAME, rememberMeUsername);
                formData.add("rememberMe", "on");
            }
        }
        Response challengeResponse = challenge(context, formData);
        context.challenge(challengeResponse);
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    protected Response challenge(AuthenticationFlowContext context, MultivaluedMap<String, String> formData) {
        LoginFormsProvider forms = context.form();

        if (formData.size() > 0) forms.setFormData(formData);

        return forms.createLogin();
    }


    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        // never called
        return true;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {
        // never called
    }

    @Override
    public void close() {

    }

    // -------------

    private boolean checkAuthRias(AuthenticationFlowContext context) {
        log.info("check auth RIAS");

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();

        String username = formData.getFirst(FormConstants.FIELD_USERNAME);
        String password = formData.getFirst(FormConstants.FIELD_PASSWORD);
        var city = formData.getFirst(FormConstants.FIELD_CITY);

        log.info("RIAS auth, got city = " + city);

        if (Validation.isBlank(city)) {
            city = "perm-dev"; // TODO с фронта не приходит город
        }

        String domain = null;
        CityMigration cm = CitiesResource.getCityMigrationByCity(city);
        if (cm != null) {
            domain = cm.getDomain();
        }

        RiasLogin riasLogin = riasService.loginUser(domain, username, password);
        if (riasLogin != null) {

            if (riasLogin.getAccess_token() != null) {

                var uriLoc = UriBuilder.fromPath("https://master.b2b-lk.web.t2.ertelecom.ru/login"); //"https://lkb2b.domru.ru/login");

                if (!Validation.isBlank(city)) {
                    uriLoc.queryParam("citydomain", city);
                }

                Response response = Response.seeOther(uriLoc.build())
                        .header("btoken", riasLogin.getAccess_token())
                        .build();

                log.debug("Redirecting to {}", uriLoc.build());
                context.forceChallenge(response);

                return true;
            }
        }

        return false;
    }

    @Override
    public boolean validateUserAndPassword(AuthenticationFlowContext context, MultivaluedMap<String, String> inputData) {
        String username = inputData.getFirst(AuthenticationManager.FORM_USERNAME);
        if (username == null) {
            context.getEvent().error(Errors.USER_NOT_FOUND);
            Response challengeResponse = challenge(context, Messages.INVALID_USER);
            context.failureChallenge(AuthenticationFlowError.INVALID_USER, challengeResponse);
            return false;
        }

        // remove leading and trailing whitespace
        username = username.trim();

        context.getEvent().detail(Details.USERNAME, username);
        context.getAuthenticationSession().setAuthNote(AbstractUsernameFormAuthenticator.ATTEMPTED_USERNAME, username);

        UserModel user = null;
        try {
            log.info("find user casual");
            user = KeycloakModelUtils.findUserByNameOrEmail(context.getSession(), context.getRealm(), username);

            if (user == null) {
                log.info("find user by phone");
                user = Util.getUserAdapter(context.getSession(), userFindService.getUserByPhone(context.getRealm(), username));
            }

            log.info("user is " + user);
            if (user != null) {
                log.info(user.getId());
            }
            if (user == null) {

                ClientModel cm = context.getAuthenticationSession().getClient();

                log.info("find user by rias: " + cm.getClientId());

                if (cm != null && (LKB2B_ID.equals(cm.getClientId()) || CONSOLE_ID.equals(cm.getClientId())) && checkAuthRias(context)) {
                    return false;
                }
            }

        } catch (ModelDuplicateException mde) {
            ServicesLogger.LOGGER.modelDuplicateException(mde);

            // Could happen during federation import
            if (mde.getDuplicateFieldName() != null && mde.getDuplicateFieldName().equals(UserModel.EMAIL)) {
                setDuplicateUserChallenge(context, Errors.EMAIL_IN_USE, Messages.EMAIL_EXISTS, AuthenticationFlowError.INVALID_USER);
            } else {
                setDuplicateUserChallenge(context, Errors.USERNAME_IN_USE, Messages.USERNAME_EXISTS, AuthenticationFlowError.INVALID_USER);
            }

            return false;
        }

        if (invalidUser(context, user)) {
            return false;
        }

        if (!validatePassword(context, user, inputData)) {
            return false;
        }

        if (!enabledUser(context, user)) {
            return false;
        }

        String rememberMe = inputData.getFirst("rememberMe");
        boolean remember = rememberMe != null && rememberMe.equalsIgnoreCase("on");
        if (remember) {
            context.getAuthenticationSession().setAuthNote(Details.REMEMBER_ME, "true");
            context.getEvent().detail(Details.REMEMBER_ME, "true");
        } else {
            context.getAuthenticationSession().removeAuthNote(Details.REMEMBER_ME);
        }
        context.setUser(user);
        return true;
    }


}
