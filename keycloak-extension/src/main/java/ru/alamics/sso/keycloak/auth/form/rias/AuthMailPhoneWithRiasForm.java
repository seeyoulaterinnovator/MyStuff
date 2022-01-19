package ru.alamics.sso.keycloak.auth.form.rias;

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
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.registration.model.FormConstants;
import ru.alamics.sso.registration.rias.RiasService;
import ru.alamics.sso.registration.rias.model.RiasLogin;
import ru.alamics.sso.registration.service.UserFindService;
import ru.alamics.sso.settings.SettingConstants;
import ru.alamics.sso.settings.SettingsService;
import ru.alamics.sso.util.Util;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import static ru.alamics.sso.registration.model.UserConstants.AUTH_FORM_SUCCESS;
import static ru.alamics.sso.settings.SettingConstants.*;

@Slf4j
public class AuthMailPhoneWithRiasForm extends AbstractUsernameFormAuthenticator implements Authenticator {

    private final static String RIAS_REDIRECT_PROPERTY = "riasLogin.redirect.url";
    // TODO
    private final static String B2B_ID = "b2b";
    private final static String DMP_ID = "dmp-kc-sit";
    private final static String CONSOLE_ID = "security-admin-console";

    private final static String REDIRECT_TO_RIAS_FORM = "redirect-to-rias.ftl";
    private final static String CHOOSE_REDIRECT_TO_LK_FORM = "redirect-to-lk.ftl";

    private final RiasService riasService;
    private final UserFindService userFindService;

    private final ApplicationProperties properties;
    private SettingsService settingsService;

    public AuthMailPhoneWithRiasForm(RiasService riasService, UserFindService userFindService) {
        this.riasService = riasService;
        this.userFindService = userFindService;

        this.properties = (ApplicationProperties) Lookup.lookup(ApplicationProperties.class);

        settingsService = (SettingsService) Lookup.lookup(SettingsService.class);
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

    private boolean checkAuthRias(AuthenticationFlowContext context, String form) {
        log.info("check auth RIAS");

        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String username = formData.getFirst(FormConstants.FIELD_USERNAME);
        String password = formData.getFirst(FormConstants.FIELD_PASSWORD);
        String city = formData.getFirst(FormConstants.FIELD_CITY);

        log.info("RIAS auth, got city = " + city);

        if (Validation.isBlank(city)) {
            city = "yar"; // TODO с фронта не приходит город
        }

        String domain = null;
        CityMigration cm = CitiesResource.getCityMigrationByCity(city);
        if (cm != null) {
            domain = cm.getDomain();
        }

        RiasLogin riasLogin = riasService.loginUser(domain, username, password);
        if (riasLogin != null) {

            if (riasLogin.getAccess_token() != null) {
                String redirectTo = properties.getProperty(RIAS_REDIRECT_PROPERTY);
                if (redirectTo == null)
                    redirectTo = "https://lkb2b.dom.ru/login";

                if (!Validation.isBlank(city)) {
                    redirectTo += "?citydomain=" + city;
                }
                log.info("Redirecting to {}", redirectTo);

                String redirectHeader = riasLogin.getAccess_token();

                Response challenge = context.form()
                        .setAttribute("redirectTo", redirectTo)
                        .setAttribute("redirectHeader", redirectHeader)
                        .setAttribute("loginToB2B", settingsService.getSettingsStringValue(LOGIN_TO_B2B, context.getRealm().getId()))
                        .setAttribute("enter", settingsService.getSettingsStringValue(ENTER, context.getRealm().getId()))
                        .setAttribute("footer", settingsService.getSettingsStringValue(FOOTER, context.getRealm().getId()))
                        .setAttribute("backToMainPage", settingsService.getSettingsStringValue(BACK_TO_MAIN_PAGE, context.getRealm().getId()))
                        .setAttribute("phoneConst", settingsService.getSettingsStringValue(PHONE_CONST, context.getRealm().getId()))
                        .setAttribute("homePage", settingsService.getSettingsStringValue(HOME_PAGE, context.getRealm().getId()))
                        .setAttribute("phoneConstLink", settingsService.getSettingsStringValue(PHONE_CONST_LINK, context.getRealm().getId()))
                        .createForm(form);

                context.challenge(challenge);

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
                boolean checkInRiasIfNotFound = context.getRealm().getAttribute("checkInRiasIfNotFound", false);
                if (checkInRiasIfNotFound && Util.isFrame(context.getSession()) && (B2B_ID.equals(cm.getClientId()) || DMP_ID.equals(cm.getClientId()))) {
                    String withCity = context.getHttpRequest().getDecodedFormParameters().getFirst(FormConstants.WITH_CITY);
                    if (Util.isEmpty(withCity) || !withCity.equals("TRUE")) {
                        context.form().setAttribute(FormConstants.WITH_CITY, "TRUE");
                        context.challenge(context.form().createLogin());
                    } else if (!checkAuthRias(context, CHOOSE_REDIRECT_TO_LK_FORM)) {
                        context.getEvent().error(Errors.USER_NOT_FOUND);
                        context.failureChallenge(AuthenticationFlowError.INVALID_USER, challenge(context, Messages.INVALID_USER));
                    }
                    return false;
                }
                String defaultClientRealm = settingsService.getSettingsStringValue(SettingConstants.DEFAULT_REALM_CLIENT_ID, context.getRealm().getName());
                if ((defaultClientRealm.equals(cm.getClientId()) || CONSOLE_ID.equals(cm.getClientId())) && checkAuthRias(context, REDIRECT_TO_RIAS_FORM)) {
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
