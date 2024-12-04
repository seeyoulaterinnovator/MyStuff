package ru.alamics.sso.keycloak.auth;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.authenticators.broker.AbstractIdpAuthenticator;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.common.util.ObjectUtil;
import org.keycloak.forms.login.LoginFormsPages;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.forms.login.MessageType;
import org.keycloak.forms.login.freemarker.AuthenticatorConfiguredMethod;
import org.keycloak.forms.login.freemarker.FreeMarkerLoginFormsProvider;
import org.keycloak.forms.login.freemarker.LoginFormsUtil;
import org.keycloak.forms.login.freemarker.Templates;
import org.keycloak.forms.login.freemarker.model.*;
import org.keycloak.models.*;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.services.ErrorPage;
import org.keycloak.services.Urls;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.resources.LoginActionsService;
import org.keycloak.theme.FreeMarkerException;
import org.keycloak.theme.Theme;
import org.keycloak.theme.beans.LocaleBean;
import org.keycloak.utils.MediaType;
import ru.alamics.sso.client.ClientService;
import ru.alamics.sso.keycloak.auth.model.AuthType;
import ru.alamics.sso.keycloak.auth.model.SsoUrlBean;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.util.MiscUtil;
import ru.alamics.sso.registration.model.FormConstants;
import ru.alamics.sso.registration.service.UserFindService;
import ru.alamics.sso.settings.SettingConstants;
import ru.alamics.sso.settings.SettingsService;
import ru.alamics.sso.util.TraceUtil;
import ru.alamics.sso.util.Util;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;
import static org.keycloak.models.Constants.TAB_ID;
import static ru.alamics.sso.keycloak.auth.form.new_auth.new_rest.auth.RestAuthHelper.chooseYourDestiny;
import static ru.alamics.sso.keycloak.util.MiscUtil.notEmptySettingsValue;
import static ru.alamics.sso.registration.model.UserConstants.*;
import static ru.alamics.sso.settings.SettingConstants.*;
import static ru.alamics.sso.util.Util.CLIENT_B2B;

@Slf4j
public class SsoFreeMarkerLoginForm extends FreeMarkerLoginFormsProvider {

    private static final String REGISTRATION_ONLY_IN_FRAME_ATTRIBUTE = "registrationOnlyInFrame";

    private static final String AUTH_VIA_SMS = "loginViaSms";
    private static final String HIDDEN_CHAT = "hideChat";

    private static final String AUTH_VIA_EMAIL_OR_USERNAME_AND_PASSWORD = "loginViaEmailOrUsernameAndPassword";

    private static final String AUTH_VIA_PHONE_CALL = "loginViaPhoneCall";

    private static final String AUTH_NOTE_LAST_LOGIN_PHONE = "lastLoginPhone";

    private static final String AUTH_NOTE_LAST_LOGIN_USERNAME = "lastLoginUsername";

    private ClientService clientService = null;

    private SettingsService settingsService = null;

    private UserFindService userFindService = null;

    public SsoFreeMarkerLoginForm(KeycloakSession session) {
        super(session);
        putAttribute("redirectUrl", getRedirectUrl());
        putAttribute("hideRegistration", isHideRegistration());
        putAttribute("iframe", Util.isFrame(session));

        settingsService = Lookup.lookup(SettingsService.class);
        clientService = Lookup.lookup(ClientService.class);
        userFindService = Lookup.lookup(UserFindService.class);

        putAttribute("phoneConst", settingsService.getSettingsStringValue(PHONE_CONST, realm.getName()));
        putAttribute("phoneConstLink", settingsService.getSettingsStringValue(PHONE_CONST_LINK, realm.getName()));
        putAttribute("footer", settingsService.getSettingsStringValue(FOOTER, realm.getName()));
        putAttribute("homePage", settingsService.getSettingsStringValue(HOME_PAGE, realm.getName()));
    }

    @Override
    protected Response createResponse(LoginFormsPages page) {
        Response restResponse = createRestResponse();
        return restResponse != null
                ? restResponse
                : super.createResponse(page);
    }

    @Override
    protected void createCommonAttributes(Theme theme, Locale locale, Properties messagesBundle, UriBuilder baseUriBuilder, LoginFormsPages page) {
        URI baseUri = baseUriBuilder.build();
        if (accessCode != null) {
            baseUriBuilder.queryParam(LoginActionsService.SESSION_CODE, accessCode);
        }
        URI baseUriWithCodeAndClientId = baseUriBuilder.build();

        if (client != null) {
            putAttribute("client", new ClientBean(session, client));
        }

        if (realm != null) {
            putAttribute("realm", new RealmBean(realm));

            if (settingsService == null) {
                settingsService = Lookup.lookup(SettingsService.class);
            }

            List<IdentityProviderModel> identityProviders = realm.getIdentityProvidersStream().toList();
            identityProviders = LoginFormsUtil.filterIdentityProviders(identityProviders.stream(), session, context);
            if (Util.isFrame(session)) {
                identityProviders = identityProviders.stream().filter(
                        model -> {
                            String systems = model.getConfig().get("systems");
                            return systems != null &&
                                    Arrays.stream(systems.split(",")).anyMatch(str -> str.equals(client.getClientId()));
                        }
                ).collect(Collectors.toList());
            }
            putAttribute("social", new IdentityProviderBean(realm, session, identityProviders, baseUriWithCodeAndClientId));
            putAttribute("auth", new AuthenticationContextBean(context, page));

            //register page
            putAttribute("placeholderUsername", settingsService.getSettingsStringValue(PLACEHOLDER_USERNAME, realm.getName()));
            putAttribute("placeholderEmail", settingsService.getSettingsStringValue(PLACEHOLDER_EMAIL, realm.getName()));
            putAttribute("fullPlaceholderEmail", notEmptySettingsValue(
                    settingsService.getSettingsStringValue(FULL_PLACEHOLDER_EMAIL, realm.getName()),
                    settingsService.getSettingsStringValue(PLACEHOLDER_EMAIL, realm.getName())
            ));
            putAttribute("placeholderPhone", settingsService.getSettingsStringValue(PLACEHOLDER_PHONE, realm.getName()));
            //login page
            putAttribute("loginTitleText", settingsService.getSettingsStringValue(LOGIN_TITLE_TEXT, realm.getName()));
            putAttribute("yourlogin", settingsService.getSettingsStringValue(YOUR_LOGIN, realm.getName()));
            putAttribute("passwordPlaceholder", settingsService.getSettingsStringValue(PASS_PLACEHOLDER, realm.getName()));
            putAttribute("enter", settingsService.getSettingsStringValue(ENTER, realm.getName()));
            putAttribute("doForgotPassword", settingsService.getSettingsStringValue(DO_FORGOT_PASS, realm.getName()));
            //Общие поля register и login
            putAttribute("loginWith", settingsService.getSettingsStringValue(LOGIN_WITH, realm.getName()));
            putAttribute("doLogIn", settingsService.getSettingsStringValue(DO_LOGIN, realm.getName()));
            putAttribute("registerTitle", settingsService.getSettingsStringValue(REGISTER_TITLE, realm.getName()));
            //Login idp link confirm
            putAttribute("confirmLinkIdpReviewProfile", settingsService.getSettingsStringValue(CONFIRM_LINK_IDP_REVIEW_PROFILE, realm.getName()));
            putAttribute("confirmLinkIdpContinue", settingsService.getSettingsStringValue(CONFIRM_LINK_IDP_CONTINUE, realm.getName()));
            //Login idp link email
            //login page expired
            putAttribute("pageExpiredMsg1", settingsService.getSettingsStringValue(PAGE_EXPIRE_MSG_1, realm.getName()));
            putAttribute("pageExpiredMsg2", settingsService.getSettingsStringValue(PAGE_EXPIRE_MSG_2, realm.getName()));
            putAttribute("doClickHere", settingsService.getSettingsStringValue(DO_CLICK_HERE, realm.getName()));
            //login reset password
            putAttribute("username", settingsService.getSettingsStringValue(USERNAME, realm.getName()));
            putAttribute("usernameOrEmail", settingsService.getSettingsStringValue(USERNAME_OR_EMAIL, realm.getName()));
            putAttribute("phoneOrEmail", settingsService.getSettingsStringValue(PHONE_OR_EMAIL, realm.getName()));
            putAttribute("next", settingsService.getSettingsStringValue(NEXT, realm.getName()));
            putAttribute("emailInstruction", settingsService.getSettingsStringValue(EMAIL_INSTRUCTION, realm.getName()));
            //Общие поля login и reset password
            putAttribute("usernameOrEmailPlaceholder", settingsService.getSettingsStringValue(USERNAME_OR_EMAIL_PLACEHOLDER, realm.getName()));
            //Общие поля reset password и update password
            putAttribute("emailForgotContentTitle", settingsService.getSettingsStringValue(EMAIL_FORGOT_CONTENT_TITLE, realm.getName()));
            //Общие поля reset password и update password и Update profile
            putAttribute("doCancel", settingsService.getSettingsStringValue(DO_CANCEL, realm.getName()));
            //Update password
            putAttribute("resetPassword", settingsService.getSettingsStringValue(RESET_PASSWORD, realm.getName()));
            //Update profile
            putAttribute("loginProfileTitle", settingsService.getSettingsStringValue(LOGIN_PROFILE_TITLE, realm.getName()));
            putAttribute("doSubmit", settingsService.getSettingsStringValue(DO_SUBMIT, realm.getName()));
            putAttribute("doAccept", settingsService.getSettingsStringValue(DO_ACCEPT, realm.getName()));
            //Общие поля Update profile и register
            putAttribute("doRegister", settingsService.getSettingsStringValue(DO_REGISTER, realm.getName()));
            //Info page
            putAttribute("proceedWithAction", settingsService.getSettingsStringValue(PROCEED_WITH_ACTION, realm.getName()));
            putAttribute("backToApplication", settingsService.getSettingsStringValue(BACK_TO_APP, realm.getName()));

            putAttribute("password", settingsService.getSettingsStringValue(PASS, realm.getName()));
            putAttribute("requiredFields", settingsService.getSettingsStringValue(REQUIRED_FIELDS, realm.getName()));

            putAttribute("url", new SsoUrlBean(realm, theme, baseUri, this.actionUri, Util.isFrame(session)));
            putAttribute("requiredActionUrl", new RequiredActionUrlFormatterMethod(realm, baseUri));
            putAttribute("activateNewAuth", isNewAuthActivated(client));
            putAttribute("loginViaSms", isLoginViaSms());
            putAttribute("loginViaEmailOrUsernameAndPassword", isLoginViaEmailOrUsernameAndPassword());
            putAttribute("loginViaPhoneCall", isLoginViaPhoneCall());
            putAttribute("hideChat", isChatHidden());
            putAttribute(
                    "isRegistrationRedirect",
                    hasClientIdInSettings(REGISTRATION_FIRST_TAB_CLIENT_IDS)
                            && page == LoginFormsPages.LOGIN
                            && !Util.TRUE_STR.equals(uriInfo.getQueryParameters().getFirst(SELF))
                            && !uriInfo.getQueryParameters().containsKey(TAB_ID)
                            && (formData == null || formData.isEmpty())
                            && !isHideRegistration()

            );
            putAttribute("isRegistrationFullTexts", hasClientIdInSettings(REGISTRATION_FIRST_TAB_CLIENT_IDS));
            putAttribute("isLoginFullTexts", hasClientIdInSettings(LOGIN_FULL_TEXTS_CLIENT_IDS));
            if(hasClientIdInSettings(LOGIN_FAIL_TO_REGISTRATION_CLIENT_IDS)
                    && messages != null && !messages.isEmpty()
                    && messages.stream().allMatch(m -> Messages.INVALID_USER.equals(m.getMessage()))
                    && page == LoginFormsPages.LOGIN && formData != null && !formData.isEmpty()
            ) {
                putAttribute("loginFailToRegistrationMessage", notEmptySettingsValue(
                        settingsService.getSettingsStringValue(LOGIN_FAIL_TO_REGISTRATION_MESSAGE, realm.getName()),
                        ""));
            }
            if(hasClientIdInSettings(LOGIN_FAIL_TO_REGISTRATION_CLIENT_IDS) && page == LoginFormsPages.REGISTER) {
                putAttribute("lastLoginUsername", getAndRemoveLastLoginUsername());
                putAttribute("lastLoginPhone", getAndRemoveLastLoginPhone());
            }
            putAttribute("restoreButtonLabel", settingsService.getSettingsStringValue(RESTORE_BUTTON_LABEL, realm.getName()));

            if (realm.isInternationalizationEnabled()) {
                UriBuilder b;
                if (page != null) {
                    switch (page) {
                        case LOGIN:
                            b = UriBuilder.fromUri(Urls.realmLoginPage(baseUri, realm.getName()));
                            break;
                        case X509_CONFIRM:
                            b = UriBuilder.fromUri(Urls.realmLoginPage(baseUri, realm.getName()));
                            break;
                        case REGISTER:
                            b = UriBuilder.fromUri(Urls.realmRegisterPage(baseUri, realm.getName()));
                            break;
                        default:
                            b = UriBuilder.fromUri(baseUri).path(uriInfo.getPath());
                            break;
                    }
                } else {
                    b = UriBuilder.fromUri(baseUri)
                            .path(uriInfo.getPath());
                }

                if (execution != null) {
                    b.queryParam(Constants.EXECUTION, execution);
                }

                if (authenticationSession != null && authenticationSession.getAuthNote(Constants.KEY) != null) {
                    b.queryParam(Constants.KEY, authenticationSession.getAuthNote(Constants.KEY));
                }

                putAttribute("locale", new LocaleBean(realm, locale, b, messagesBundle));
            }
        }
        if (realm != null && user != null && session != null) {
            putAttribute("authenticatorConfigured", new AuthenticatorConfiguredMethod(realm, user, session));
            putAttribute("actionIsNull", user.getRequiredActionsStream().findAny().isEmpty());
            putAttribute("actionIsEmpty", user.getRequiredActionsStream().count() == 1);

            log.info(String.format("actionIsEmpty is %s", user.getRequiredActionsStream().count() == 1));

            putAttribute("clientIsB2B", CLIENT_B2B.equals(client.getClientId()));
        }
        if(TraceUtil.isTraceEnabled()) {
            log.debug("Attributes: {}", attributes);
        }
        if(context != null && context.getHttpRequest().getDecodedFormParameters().getFirst("username") != null){
            putAttribute("userEmail", getEmailBy());
        }
    }

    private boolean hasClientIdInSettings(SettingConstants setting) {
        if(client == null) return false;
        return Arrays.stream(settingsService.getSettingsStringValue(setting, realm.getName()).split(","))
                .map(String::trim)
                .map(String::toLowerCase)
                .filter(s -> !s.isEmpty())
                .toList()
                .contains(client.getClientId().toLowerCase());
    }

    private boolean isHideRegistration() {
        final boolean registrationOnlyInFrame = realm.getAttribute(REGISTRATION_ONLY_IN_FRAME_ATTRIBUTE, false);

        final boolean isIframe = Util.isFrame(session);

        return registrationOnlyInFrame && !isIframe;
    }

    private boolean isLoginViaSms() {
        return client != null && Boolean.parseBoolean(client.getAttribute(AUTH_VIA_SMS));
    }

    private boolean isLoginViaEmailOrUsernameAndPassword() {
        return client != null && Boolean.parseBoolean(client.getAttribute(AUTH_VIA_EMAIL_OR_USERNAME_AND_PASSWORD));
    }

    private boolean isLoginViaPhoneCall() {
        return client != null && Boolean.parseBoolean(client.getAttribute(AUTH_VIA_PHONE_CALL));
    }

    private boolean isNewAuthActivated(ClientModel client) {
        return client != null && Boolean.parseBoolean(client.getAttribute("activateNewAuth"));
    }

    private Object isChatHidden() {
        return Boolean.parseBoolean(realm.getAttribute(HIDDEN_CHAT));
    }


    private String getRedirectUrl() {

        // не успевает иначе
        if (clientService == null) {
            clientService = Lookup.lookup(ClientService.class);
        }

        if (settingsService == null) {
            settingsService = Lookup.lookup(SettingsService.class);
        }

        String redirectUri = clientService.findMainRedirectUri(client);

        if (redirectUri != null) {
            return redirectUri;
        }

        return settingsService.getSettingsStringValue(HOME_PAGE, realm.getName());
    }

    @Override
    protected UriBuilder prepareBaseUriBuilder(boolean resetRequestUriParams) {
        UriBuilder ret = super.prepareBaseUriBuilder(resetRequestUriParams);
        return addQueryParamToBuilder(ret);
    }

    @Override
    public LoginFormsProvider setActionUri(URI actionUri) {
        URI uri = addQueryParams(actionUri);
        return super.setActionUri(uri);
    }

    private Response createRestResponse() {
        if (Util.isPasswordGrandType(session)) {
            if (!(accessCode == null || execution == null || authenticationSession == null)) {
                Map<String, String> entity = new HashMap<>();

                entity.put("session_state", authenticationSession.getParentSession().getId());
                entity.put("access_code", accessCode);
                entity.put("execution", execution);
                entity.put("tab_id", authenticationSession.getTabId());
                chooseYourDestiny(entity, authenticationSession);

                return Response.ok().entity(entity).type(MediaType.APPLICATION_JSON_TYPE).build();
            }
            return Response.status(BAD_REQUEST).build();
        }
        return null;
    }

    @Override
    public Response createForm(String form) {
        Response restResponse = createRestResponse();
        return restResponse != null
                ? restResponse
                : super.createForm(form);
    }

    @Override
    protected Response processTemplate(Theme theme, String templateName, Locale locale) {
        try {
            String result = freeMarker.processTemplate(attributes, templateName, theme);
            Response.ResponseBuilder builder = Response.status(status == null ? Response.Status.OK : status)
                    .type(httpResponseHeaders.getOrDefault(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_HTML_UTF_8))
                    .language(locale)
                    .entity(result);
            if(context != null) {
                for (Map.Entry<String, String> entry : context.getRealm().getBrowserSecurityHeaders().entrySet()) {
                    builder.header(entry.getKey(), entry.getValue());
                }
            }
            for (Map.Entry<String, String> entry : httpResponseHeaders.entrySet()) {
                builder.header(entry.getKey(), entry.getValue());
            }
            return builder.build();
        } catch (FreeMarkerException e) {
            log.error("Failed to process template", e);
            if (templateName.equals(Templates.getTemplate(LoginFormsPages.ERROR))) {
                return Response.serverError().build();
            }
            return ErrorPage.error(session, authenticationSession, Response.Status.INTERNAL_SERVER_ERROR, "500");
        }
    }

    private URI addQueryParams(URI src) {
        UriBuilder builder = UriBuilder.fromUri(src);
        return addQueryParamToBuilder(builder).build();
    }

    private UriBuilder addQueryParamToBuilder(UriBuilder builder) {
        MultivaluedMap<String, String> queryParameters = this.session.getContext().getUri().getQueryParameters();
        if (queryParameters != null) {
            queryParameters.forEach((k, v) -> {
                switch (k) {
                    case HIDDEN_HEADER:
                        builder.queryParam(HIDDEN_HEADER, v.get(0));
                        break;
                    case I_FRAME:
                        builder.queryParam(I_FRAME, v.get(0));
                        break;
                    case CITY:
                        builder.queryParam(CITY, v.get(0));
                        break;
                }
            });
        }
        return builder;
    }

    @Override
    public Response createRegistration() {
        if (isHideRegistration()) {
            URI redirectUri = Urls.realmLoginPage(uriInfo.getBaseUri(), realm.getName());
            return Response.status(302).location(redirectUri).build();
        }

        authenticationSession.setAuthNote("REGISTRATION", "REGISTRATION");
        RealmModel realm = this.session.getContext().getRealm();
        List<String> twoStepAuth = realm.getRequiredActionProvidersStream()
                .filter(RequiredActionProviderModel::isDefaultAction)
                .map(RequiredActionProviderModel::getAlias)
                .collect(Collectors.toList());
        AuthType authType = AuthType.getByList(twoStepAuth);
        if (authType != null) {
            // TODO: может вообще от этого избавиться?
            // TODO: это для информации какая двухфакторная аутентификация будет
//            this.putAttribute("twoStepAuthType", authType.getDescription());
            this.putAttribute("twoStepAuthType", "");
        } else {
            this.putAttribute("twoStepAuthType", "");
        }
        if (formData != null) {

            String phone = Util.getCleanUserPhone(formData.getFirst(FormConstants.FIELD_PHONE));

            this.putAttribute(FormConstants.FIELD_ORG_NAME, formData.getFirst(FormConstants.FIELD_ORG_NAME));
            this.putAttribute(FormConstants.FIELD_EMAIL, formData.getFirst(FormConstants.FIELD_EMAIL));
            this.putAttribute(FormConstants.FIELD_FIRST_NAME, formData.getFirst(FormConstants.FIELD_FIRST_NAME));
            this.putAttribute(FormConstants.FIELD_USERNAME, formData.getFirst(FormConstants.FIELD_USERNAME));
            this.putAttribute(FormConstants.FIELD_PHONE, phone);
        }
        log.info("create form attr = {}", attributes);
        return super.createRegistration();
    }

    @Override
    public Response createIdpLinkEmailPage() {
        BrokeredIdentityContext brokerContext = (BrokeredIdentityContext) this.attributes.get(IDENTITY_PROVIDER_BROKER_CONTEXT);
        String idpAlias = brokerContext.getIdpConfig().getAlias();
        idpAlias = ObjectUtil.capitalize(idpAlias);
        setMessage(MessageType.WARNING, Messages.LINK_IDP, idpAlias);

        UserModel existingUser = AbstractIdpAuthenticator.getExistingUser(session, session.getContext().getRealm(), brokerContext.getAuthenticationSession());
        if (existingUser != null) {
            putAttribute(FormConstants.EXISTING_USER_EMAIL, existingUser.getEmail());
        }
        return createResponse(LoginFormsPages.LOGIN_IDP_LINK_EMAIL);
    }

    @Override
    public Response createLoginUsernamePassword() {
        if(authenticationSession != null) {
            authenticationSession.removeAuthNote(AUTH_NOTE_LAST_LOGIN_PHONE);
            authenticationSession.removeAuthNote(AUTH_NOTE_LAST_LOGIN_USERNAME);
            if(formData != null) {
                String username = formData.getFirst("username");
                if(username != null && !username.trim().isEmpty()) {
                    if(formData.containsKey("smsButton") || MiscUtil.isPhoneNumber(username)) {
                        authenticationSession.setAuthNote(AUTH_NOTE_LAST_LOGIN_PHONE, username);
                    } else {
                        authenticationSession.setAuthNote(AUTH_NOTE_LAST_LOGIN_USERNAME, username);
                    }
                }
            }
        }
        return super.createLoginUsernamePassword();
    }
    
    private void putAttribute(String key, Object value) {
        if(value == null) {
            log.warn("null value for key = {}", key);
        } else {
            attributes.put(key, value);
        }
    }

    private String getAndRemoveLastLoginPhone() {
        if(authenticationSession != null) {
            String phone = authenticationSession.getAuthNote(AUTH_NOTE_LAST_LOGIN_PHONE);
            authenticationSession.removeAuthNote(AUTH_NOTE_LAST_LOGIN_PHONE);
            return phone;
        }
        return null;
    }

    private String getAndRemoveLastLoginUsername() {
        if(authenticationSession != null) {
            String username = authenticationSession.getAuthNote(AUTH_NOTE_LAST_LOGIN_USERNAME);
            authenticationSession.removeAuthNote(AUTH_NOTE_LAST_LOGIN_USERNAME);
            return username;
        }
        return null;
    }

    private String getEmailBy() {
        String username = context.getHttpRequest().getDecodedFormParameters().getFirst("username");
        if (username.startsWith("+7")) {
            UserEntity userFind = userFindService.getUserByPhone(context.getRealm(), username);
            if(userFind != null && userFind.getEmail() != null){
                return userFind.getEmail();
            } else {
                return null;
            }
        }
        return username;
    }
}
