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
//        1)try return false что будет если вернуть тру или фолс при каком результате происходит логин,
//        2)проверить логи с юзером которого нет в риасе

        if (user == null) {
            ClientModel cm = context.getAuthenticationSession().getClient();
//            for example cm = "b2b" if iframe
            log.info("find user by rias: " + cm.getClientId());
            boolean checkInRiasIfNotFound = context.getRealm().getAttribute("checkInRiasIfNotFound", false);
            if (checkInRiasIfNotFound && Util.isFrame(context.getSession()) && (B2B_ID.equals(cm.getClientId()) || DMP_ID.equals(cm.getClientId()))) {
                String withCity = context.getHttpRequest().getDecodedFormParameters().getFirst(FormConstants.WITH_CITY);
                log.info("withCity = " + withCity);
                if (Util.isEmpty(withCity) || !withCity.equals("TRUE")) {
                    //ilya547 попадает сюда(нет в риасе)
                    log.info("Rias isSuccessCheckUser,  if (Util.isEmpty(withCity) || !withCity.equals(TRUE))");
                    context.form().setAttribute(FormConstants.WITH_CITY, "TRUE");
                    context.form().setAttribute("showModal", "TRUE");
                    context.challenge(context.form().createLogin());
                    return true;
                } else if (!checkAuthRias(context, CHOOSE_REDIRECT_TO_LK_FORM)) {
                    log.info("Rias isSuccessCheckUser,  else if (!checkAuthRias(context, CHOOSE_REDIRECT_TO_LK_FORM))");
                    context.getEvent().error(Errors.USER_NOT_FOUND);
                    context.form().setAttribute("showModal", "FALSE");
                    context.form().setAttribute(FormConstants.WITH_CITY, "TRUE");
                    context.failureChallenge(AuthenticationFlowError.INVALID_USER, challenge(context, Messages.INVALID_USER));
                    return true;
                }
                log.info("Rias isSuccessCheckUser, return false");
                return false;
            }
            String defaultClientRealm = settingsService.getSettingsStringValue(SettingConstants.DEFAULT_REALM_CLIENT_ID, context.getRealm().getName());
            boolean ret = (!defaultClientRealm.equals(cm.getClientId()) && !CONSOLE_ID.equals(cm.getClientId())) || !checkAuthRias(context, REDIRECT_TO_RIAS_FORM);
            //выводится если креды одинаковые и город НН
            log.info("Rias isSuccessCheckUser, boolean ret = " + ret);//ret = false

            return (!defaultClientRealm.equals(cm.getClientId()) && !CONSOLE_ID.equals(cm.getClientId())) || !checkAuthRias(context, REDIRECT_TO_RIAS_FORM);
        }
        log.info("Rias isSuccessCheckUser, return true");

        return true;
    }


    private boolean checkAuthRias(AuthenticationFlowContext context, String form) {

        log.info("call check auth RIAS");
        return true;

////        try return false что будет если вернуть тру или фолс
//        MultivaluedMap<String, String> formData = context.getHttpRequest().getDecodedFormParameters();
//        String username = formData.getFirst(FormConstants.FIELD_USERNAME);
//        String password = formData.getFirst(FormConstants.FIELD_PASSWORD);
////        city = выбранному городу(смотрел по логам)
//        String city = formData.getFirst(FormConstants.FIELD_CITY);
//
//        log.info("RIAS auth, got city = " + city);
//
//        if (Validation.isBlank(city)) {
//            city = "yar";
//            log.info("RIAS auth, got city = yar");
//        }
//
//        String domain = null;
//        CityMigration cm = CitiesResource.getCityMigrationByCity(city);
//        if (cm != null) {
//            domain = cm.getDomain();
//            log.info("RIAS auth, domain = " + domain);
//        }
////        авторизация риас
//        RiasLogin riasLogin = riasService.loginUser(domain, username, password);
//
//        if (riasLogin != null) {
//            log.info("Rias Login - success");
//            if (riasLogin.getAccess_token() != null) {
//                String redirectTo = properties.getProperty(RIAS_REDIRECT_PROPERTY);
//                if (redirectTo == null)
//                    //попадаем если креды одинаковые и город НН
//                    redirectTo = "https://lkb2b.dom.ru/login";
//                    log.info("Rias auth, redirectTo = " + redirectTo);
//                //город и домен = НН(не пусто)
//                if (!Validation.isBlank(city)) {
//                    //попадаем если креды одинаковые и город НН
//                    redirectTo += "?citydomain=" + city;
//                    log.info(redirectTo += "?citydomain=" + city);
//                    //redirectTo = https://lkb2b.dom.ru/login?citydomain=nn?citydomain=nn
//                }
//                log.info("Redirecting to {}", redirectTo); //Redirecting to https://lkb2b.dom.ru/login?citydomain=nn?citydomain=nn
//
//
//                String redirectHeader = riasLogin.getAccess_token();
//
//                Response challenge = context.form()
//                        .setAttribute("redirectTo", redirectTo)
//                        .setAttribute("redirectHeader", redirectHeader)
//                        .setAttribute("loginToB2B", settingsService.getSettingsStringValue(LOGIN_TO_B2B, context.getRealm().getId()))
//                        .setAttribute("enter", settingsService.getSettingsStringValue(ENTER, context.getRealm().getId()))
//                        .setAttribute("footer", settingsService.getSettingsStringValue(FOOTER, context.getRealm().getId()))
//                        .setAttribute("backToMainPage", settingsService.getSettingsStringValue(BACK_TO_MAIN_PAGE, context.getRealm().getId()))
//                        .setAttribute("phoneConst", settingsService.getSettingsStringValue(PHONE_CONST, context.getRealm().getId()))
//                        .setAttribute("homePage", settingsService.getSettingsStringValue(HOME_PAGE, context.getRealm().getId()))
//                        .setAttribute("phoneConstLink", settingsService.getSettingsStringValue(PHONE_CONST_LINK, context.getRealm().getId()))
//                        .createForm(form);
//
//                context.challenge(challenge);
//                //лог выводится, если креды одинаковые и город НН
//                log.info("Rias checkAuthRias, return true");
//                return true;
//            }
//        }
//        log.info("Rias checkAuthRias, return false");
//        return false;
    }
}

