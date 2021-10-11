package ru.alamics.sso.keycloak.auth;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.keycloak.authentication.authenticators.broker.AbstractIdpAuthenticator;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.common.util.ObjectUtil;
import org.keycloak.forms.login.LoginFormsPages;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.forms.login.freemarker.AuthenticatorConfiguredMethod;
import org.keycloak.forms.login.freemarker.FreeMarkerLoginFormsProvider;
import org.keycloak.forms.login.freemarker.LoginFormsUtil;
import org.keycloak.forms.login.freemarker.Templates;
import org.keycloak.forms.login.freemarker.model.ClientBean;
import org.keycloak.forms.login.freemarker.model.IdentityProviderBean;
import org.keycloak.forms.login.freemarker.model.RealmBean;
import org.keycloak.forms.login.freemarker.model.RequiredActionUrlFormatterMethod;
import org.keycloak.models.*;
import org.keycloak.services.ErrorPage;
import org.keycloak.services.Urls;
import org.keycloak.services.messages.Messages;
import org.keycloak.services.resources.LoginActionsService;
import org.keycloak.theme.BrowserSecurityHeaderSetup;
import org.keycloak.theme.FreeMarkerException;
import org.keycloak.theme.FreeMarkerUtil;
import org.keycloak.theme.Theme;
import org.keycloak.theme.beans.LocaleBean;
import org.keycloak.theme.beans.MessageType;
import org.keycloak.utils.MediaType;
import ru.alamics.sso.client.ClientService;
import ru.alamics.sso.keycloak.auth.model.AuthType;
import ru.alamics.sso.keycloak.auth.model.SsoUrlBean;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.registration.model.FormConstants;
import ru.alamics.sso.util.Util;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static ru.alamics.sso.registration.model.UserConstants.*;

@Slf4j
public class SsoFreeMarkerLoginForm extends FreeMarkerLoginFormsProvider {
    private static final String HOME_PAGE = "https://newlkb2b.dom.ru";
    private static final String REGISTRATION_ONLY_IN_FRAME_ATTRIBUTE = "registrationOnlyInFrame";

    private ClientService clientService = null;

    public SsoFreeMarkerLoginForm(KeycloakSession session, FreeMarkerUtil freeMarker) {
        super(session, freeMarker);

        attributes.put("redirectUrl", getRedirectUrl());
        attributes.put("hideRegistration", isHideRegistration());

        clientService = (ClientService) Lookup.lookup(ClientService.class);
    }

    @Override
    protected void createCommonAttributes(Theme theme, Locale locale, Properties messagesBundle, UriBuilder baseUriBuilder, LoginFormsPages page) {
        URI baseUri = baseUriBuilder.build();
        if (accessCode != null) {
            baseUriBuilder.queryParam(LoginActionsService.SESSION_CODE, accessCode);
        }
        URI baseUriWithCodeAndClientId = baseUriBuilder.build();

        if (client != null) {
            attributes.put("client", new ClientBean(client, baseUri));
        }

        if (realm != null) {
            attributes.put("realm", new RealmBean(realm));

            List<IdentityProviderModel> identityProviders = realm.getIdentityProviders();
            identityProviders = LoginFormsUtil.filterIdentityProviders(identityProviders, session, realm, attributes, formData);
            attributes.put("social", new IdentityProviderBean(realm, session, identityProviders, baseUriWithCodeAndClientId));

            attributes.put("url", new SsoUrlBean(realm, theme, baseUri, this.actionUri, Util.isFrame(session)));
            attributes.put("requiredActionUrl", new RequiredActionUrlFormatterMethod(realm, baseUri));

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

                attributes.put("locale", new LocaleBean(realm, locale, b, messagesBundle));
            }
        }
        if (realm != null && user != null && session != null) {
            attributes.put("authenticatorConfigured", new AuthenticatorConfiguredMethod(realm, user, session));
        }
    }

    private boolean isHideRegistration() {
        final boolean registrationOnlyInFrame = realm.getAttribute(REGISTRATION_ONLY_IN_FRAME_ATTRIBUTE, false);

        final boolean isIframe = Util.isFrame(session);

        return registrationOnlyInFrame && !isIframe;
    }

    private String getRedirectUrl() {

        // не успевает иначе
        if (clientService == null) {
            clientService = (ClientService) Lookup.lookup(ClientService.class);
        }

        String redirectUri = clientService.findMainRedirectUri(client);

        if (redirectUri != null) {
            return redirectUri;
        }

        return HOME_PAGE;
    }

    private String getHash(String fileName) {
        String hash = "";
        try {
            hash = DigestUtils.md5Hex(SsoFreeMarkerLoginForm.class.getResourceAsStream("/themes/domru/login/resources/build/" + fileName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return hash;
    }

    @Override
    protected UriBuilder prepareBaseUriBuilder(boolean resetRequestUriParams) {
        UriBuilder ret = super.prepareBaseUriBuilder(resetRequestUriParams);
        return addQueryParamToBuilder(ret);
    }

    @Override
    public LoginFormsProvider setActionUri(URI actionUri) {
        URI uri = addQueryParams(actionUri);
        LoginFormsProvider ret = super.setActionUri(uri);
        return ret;
    }

    @Override
    public Response createForm(String form) {
        Theme theme;
        try {
            theme = super.getTheme();
        } catch (IOException e) {
            log.error("Failed to create theme", e);
            return Response.serverError().build();
        }

        Locale locale = session.getContext().resolveLocale(user);
        Properties messagesBundle = handleThemeResources(theme, locale);

        handleMessages(locale, messagesBundle);

        UriBuilder uriBuilder = prepareBaseUriBuilder(false);
        createCommonAttributes(theme, locale, messagesBundle, uriBuilder, null);

        return processTemplate(theme, form, locale);
    }

    @Override
    protected Response processTemplate(Theme theme, String templateName, Locale locale) {
        try {
            String result = freeMarker.processTemplate(attributes, templateName, theme);
            javax.ws.rs.core.MediaType mediaType = contentType == null ? MediaType.TEXT_HTML_UTF_8_TYPE : contentType;
            Response.ResponseBuilder builder = Response.status(status == null ? Response.Status.OK : status).type(mediaType).language(locale).entity(result);
            BrowserSecurityHeaderSetup.headers(builder, realm);
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
        builder = addQueryParamToBuilder(builder);
        return builder.build();
    }

    private UriBuilder addQueryParamToBuilder(UriBuilder builder) {
        MultivaluedMap<String, String> queryParameters = this.session.getContext().getUri().getQueryParameters();
        if (queryParameters != null) {
            queryParameters.forEach((k, v) -> {
                if (k.equals(HIDDEN_HEADER)) {
                    builder.queryParam(HIDDEN_HEADER, v.get(0));
                } else if (k.equals(I_FRAME)) {
                    builder.queryParam(I_FRAME, v.get(0));
                } else if (k.equals(CITY)) {
                    builder.queryParam(CITY, v.get(0));
                }
            });
        }
        return builder;
    }

    @Override
    public Response createRegistration() {
        if (isHideRegistration()) {
            final URI redirectUri = Urls.accountLogPage(uriInfo.getBaseUri(), realm.getName());
            return Response.status(302).location(redirectUri).build();
        }

        RealmModel realm = this.session.getContext().getRealm();
        List<RequiredActionProviderModel> requiredActionsProvider = realm.getRequiredActionProviders();
        List<String> twoStepAuth = requiredActionsProvider.stream()
                .filter(RequiredActionProviderModel::isDefaultAction)
                .map(RequiredActionProviderModel::getAlias)
                .collect(Collectors.toList());
        AuthType authType = AuthType.getByList(twoStepAuth);
        if (authType != null) {
            this.attributes.put("twoStepAuthType", authType.getDescription());
        } else {
            this.attributes.put("twoStepAuthType", "");
        }
        if (formData != null) {

            String phone = Util.getCleanUserPhone(formData.getFirst(FormConstants.FIELD_PHONE));

            this.attributes.put(FormConstants.FIELD_ORG_NAME, formData.getFirst(FormConstants.FIELD_ORG_NAME));
            this.attributes.put(FormConstants.FIELD_EMAIL, formData.getFirst(FormConstants.FIELD_EMAIL));
            this.attributes.put(FormConstants.FIELD_FIRST_NAME, formData.getFirst(FormConstants.FIELD_FIRST_NAME));
            this.attributes.put(FormConstants.FIELD_USERNAME, formData.getFirst(FormConstants.FIELD_USERNAME));
            this.attributes.put(FormConstants.FIELD_PHONE, phone);
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
            attributes.put(FormConstants.EXISTING_USER_EMAIL, existingUser.getEmail());
        }
        return createResponse(LoginFormsPages.LOGIN_IDP_LINK_EMAIL);
    }
}
