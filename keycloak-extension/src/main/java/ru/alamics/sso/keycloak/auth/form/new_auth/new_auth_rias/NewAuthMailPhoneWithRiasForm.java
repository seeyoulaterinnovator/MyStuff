package ru.alamics.sso.keycloak.auth.form.new_auth.new_auth_rias;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.AuthenticationFlowError;
import org.keycloak.events.Errors;
import org.keycloak.models.*;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.resources.admin.UserResource;
import org.keycloak.services.validation.Validation;
import ru.alamics.sso.jpa.entity.UserLoginHistory;
import ru.alamics.sso.keycloak.auth.form.new_auth.NewAbstractAuthMailPhoneForm;
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

import static ru.alamics.sso.settings.SettingConstants.*;

@Slf4j
public class NewAuthMailPhoneWithRiasForm extends NewAbstractAuthMailPhoneForm {

    private final static String RIAS_REDIRECT_PROPERTY = "riasLogin.redirect.url";

    private final static String B2B_ID = "b2b";

    private final static String DMP_ID = "dmp-kc-sit";

    private final static String CONSOLE_ID = "security-admin-console";

    private final static String REDIRECT_TO_RIAS_FORM = "redirect-to-rias.ftl";

    private final static String CHOOSE_REDIRECT_TO_LK_FORM = "redirect-to-lk.ftl";

    private final RiasService riasService;

    private final ApplicationProperties properties;

    private final SettingsService settingsService;

    public NewAuthMailPhoneWithRiasForm(RiasService riasService, UserFindService userFindService, KeycloakSession session) {
        super(userFindService, session);
        this.riasService = riasService;

        this.properties = Lookup.lookup(ApplicationProperties.class);

        settingsService = Lookup.lookup(SettingsService.class);
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        // never called
        return true;
    }

    @Override
    public boolean isSuccessCheckUser(AuthenticationFlowContext context, UserModel user) {
//        эта проверка валит авторизацию
//        if (context.getUser().getId() != null) {
//            return true;
//        }

        if (user == null) {
            ClientModel cm = context.getAuthenticationSession().getClient();
//            for example cm = "b2b" if iframe
            log.info("find user by rias: " + cm.getClientId());
            boolean checkInRiasIfNotFound = context.getRealm().getAttribute("checkInRiasIfNotFound", false);
            if (checkInRiasIfNotFound && Util.isFrame(context.getSession()) && (B2B_ID.equals(cm.getClientId()) || DMP_ID.equals(cm.getClientId()))) {
                String withCity = context.getHttpRequest().getDecodedFormParameters().getFirst(FormConstants.WITH_CITY);
                if (Util.isEmpty(withCity) || !withCity.equals("TRUE")) {
                    context.form().setAttribute(FormConstants.WITH_CITY, "TRUE");
                    context.form().setAttribute("showModal", "TRUE");
                    context.challenge(context.form().createLogin());
                    return true;
                } else if (!checkAuthRias(context, CHOOSE_REDIRECT_TO_LK_FORM)) {
                    context.getEvent().error(Errors.USER_NOT_FOUND);
                    context.form().setAttribute("showModal", "FALSE");
                    context.form().setAttribute(FormConstants.WITH_CITY, "TRUE");
                    context.failureChallenge(AuthenticationFlowError.INVALID_USER, challenge(context, Messages.INVALID_USER));
                    return true;
                }
                return false;
            }
            String defaultClientRealm = settingsService.getSettingsStringValue(SettingConstants.DEFAULT_REALM_CLIENT_ID, context.getRealm().getName());
            return (!defaultClientRealm.equals(cm.getClientId()) && !CONSOLE_ID.equals(cm.getClientId())) || !checkAuthRias(context, REDIRECT_TO_RIAS_FORM);
        }
        return true;
    }


    private boolean checkAuthRias(AuthenticationFlowContext context, String form) {
        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
        String username = formData.getFirst(FormConstants.FIELD_USERNAME);
        String password = formData.getFirst(FormConstants.FIELD_PASSWORD);
//        city = выбранному городу(смотрел по логам)
        String city = formData.getFirst(FormConstants.FIELD_CITY);

        log.info("RIAS auth, got city = " + city);

        if (Validation.isBlank(city)) {
            city = "yar";
        }


        String domain = null;
        CityMigration cm = CitiesResource.getCityMigrationByCity(city);
        if (cm != null) {
            domain = cm.getDomain();
        }
//        авторизация риас
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
}

