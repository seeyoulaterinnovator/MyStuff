package ru.alamics.sso.keycloak.auth;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.authenticators.broker.AbstractIdpAuthenticator;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.common.util.ObjectUtil;
import org.keycloak.forms.login.LoginFormsPages;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.forms.login.freemarker.FreeMarkerLoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RequiredActionProviderModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.messages.Messages;
import org.keycloak.theme.FreeMarkerUtil;
import org.keycloak.theme.Theme;
import org.keycloak.theme.beans.MessageType;
import ru.alamics.sso.keycloak.auth.model.AuthType;
import ru.alamics.sso.registration.model.FormConstants;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Collectors;

import static ru.alamics.sso.registration.model.UserConstants.*;

@Slf4j
public class SsoFreeMarkerLoginForm extends FreeMarkerLoginFormsProvider {

    public SsoFreeMarkerLoginForm (KeycloakSession session, FreeMarkerUtil freeMarker) {
        super(session, freeMarker);
    }

    @Override
    protected UriBuilder prepareBaseUriBuilder (boolean resetRequestUriParams) {
        var ret = super.prepareBaseUriBuilder(resetRequestUriParams);
        return addQueryParamToBuilder(ret);
    }

    @Override
    public LoginFormsProvider setActionUri (URI actionUri) {
        var uri = addQueryParams(actionUri);
        var ret = super.setActionUri(uri);
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

    private URI addQueryParams(URI src) {
        UriBuilder builder = UriBuilder.fromUri(src);
        builder = addQueryParamToBuilder(builder);
        return builder.build();
    }

    private UriBuilder addQueryParamToBuilder (UriBuilder builder) {
        var queryParameters = this.session.getContext().getUri().getQueryParameters();
        if(queryParameters != null) {
            queryParameters.forEach((k, v) -> {
                if(k.equals(HIDDEN_HEADER)) {
                    builder.queryParam(HIDDEN_HEADER, v.get(0));
                } else if(k.equals(I_FRAME)) {
                    builder.queryParam(I_FRAME, v.get(0));
                } else if(k.equals(CITY)) {
                    builder.queryParam(CITY, v.get(0));
                }
            });
        }
        return builder;
    }

    @Override
    public Response createRegistration () {
        var realm = this.session.getContext().getRealm();
        var requiredActionsProvider = realm.getRequiredActionProviders();
        var twoStepAuth = requiredActionsProvider.stream()
                .filter(RequiredActionProviderModel::isDefaultAction)
                .map(RequiredActionProviderModel::getAlias)
                .collect(Collectors.toList());
        var authType = AuthType.getByList(twoStepAuth);
        if(authType != null) {
            this.attributes.put("twoStepAuthType", authType.getDescription());
        } else {
            this.attributes.put("twoStepAuthType", "");
        }
        return super.createRegistration();
    }

    @Override
    public Response createIdpLinkEmailPage() {
        BrokeredIdentityContext brokerContext = (BrokeredIdentityContext) this.attributes.get(IDENTITY_PROVIDER_BROKER_CONTEXT);
        String idpAlias = brokerContext.getIdpConfig().getAlias();
        idpAlias = ObjectUtil.capitalize(idpAlias);
        setMessage(MessageType.WARNING, Messages.LINK_IDP, idpAlias);

        UserModel existingUser = AbstractIdpAuthenticator.getExistingUser(session, session.getContext().getRealm(), brokerContext.getAuthenticationSession());
        if (existingUser != null ) {
            attributes.put(FormConstants.EXISTING_USER_EMAIL, existingUser.getEmail());
        }
        return createResponse(LoginFormsPages.LOGIN_IDP_LINK_EMAIL);
    }
}
