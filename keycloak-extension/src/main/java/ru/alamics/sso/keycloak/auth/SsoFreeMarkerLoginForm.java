package ru.alamics.sso.keycloak.auth;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.forms.login.freemarker.FreeMarkerLoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.theme.FreeMarkerUtil;
import org.keycloak.theme.Theme;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.Properties;

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
}
