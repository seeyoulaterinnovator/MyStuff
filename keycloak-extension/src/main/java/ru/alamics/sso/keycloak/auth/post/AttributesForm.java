package ru.alamics.sso.keycloak.auth.post;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.provider.ProviderConfigProperty;
import ru.alamics.sso.auth.UserRole;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.keycloak.search.dto.UserDto;
import ru.alamics.sso.keycloak.search.rest.SearchResource;
import ru.alamics.sso.property.ApplicationProperties;
import ru.alamics.sso.registration.model.TbapiConstants;
import ru.alamics.sso.registration.tbapi.TbapiService;
import ru.alamics.sso.registration.tbapi.model.TbapiConnectConfig;

import javax.naming.InitialContext;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.List;
import java.util.Map;


import static ru.alamics.sso.keycloak.registration.UserConfigProperties.*;
import static ru.alamics.sso.keycloak.registration.UserConfigProperties.SCHEMA_PROPERTY_NAME;
import static ru.alamics.sso.registration.model.UserConstants.*;

@Slf4j
public class AttributesForm implements Authenticator {
    private static final String FORM = "attributes.ftl";
    private final UserRole role;
    private final TbapiService tbapiService;

    public AttributesForm (UserRole role, TbapiService tbapiService) {
        this.role = role;
        this.tbapiService = tbapiService;
    }

    @Override
    public void authenticate (AuthenticationFlowContext context) {
        final String DEBUG_STR = "authenticate";
        var authSession = context.getAuthenticationSession();
        log.info("{}: frame={}", DEBUG_STR, authSession.getAuthNote(I_FRAME));
        var uriInfo = context.getUriInfo();
        var queryParams = uriInfo.getQueryParameters();
        queryParams.forEach((key, value) -> log.info("{}: key={} value={}", DEBUG_STR, key, value));
        String frame = uriInfo.getQueryParameters().getFirst(I_FRAME);
        var redirectUriQueryParams = extractQueryParamsFromRedirectUri(queryParams.getFirst(REDIRECT_URI));
        String redirectIframe = redirectUriQueryParams.get(I_FRAME);
        boolean isAuth = "1".equals(authSession.getAuthNote(AUTH_FORM_SUCCESS));//it`s magick

        if( frame != null || isAuth || redirectIframe != null) {
            var session = context.getSession();
            var searchResource = new SearchResource(session);
            var user = context.getUser();
            var response = searchResource.getUsersInfo( "", user.getId(), "", "", true);
            JsonResponse body = (JsonResponse) response.getEntity();
            var results = body.getResults();
            List<UserDto> attributes = (List<UserDto>) results.get("users-info");
            if(attributes != null) {
                attributes = attributes.stream()
                        .filter(attribute -> Objects.nonNull(attribute.getTomsId()) && Objects.nonNull(attribute.getRoleId()))
                        .collect(Collectors.toList());
            } else {
                attributes = Collections.emptyList();
            }
            if(attributes.isEmpty()) {
                context.success();
            } else {
                Response challenge = createForm(context, attributes);
                context.challenge(challenge);
            }
        } else {
            context.success();
        }


    }

    private Response createForm(AuthenticationFlowContext context, List<UserDto> attributes) {
        LoginFormsProvider form = context.form();
        if(!attributes.isEmpty()) {
            Set<AttributesModel> models = attributes.stream()
                    .map(attribute -> AttributesModel.builder()
                            .roleName(attribute.getRoleName())
                            .tomsId(attribute.getTomsId())
                            .build()
                    ).collect(Collectors.toSet());

            form.setAttribute("posts", models);
        }

        if(attributes.size() > 0) {
            List<UserDto> userDtos = (List<UserDto>) attributes.get("users-info");
            Map<String, Object> customerNames = tbapiService.customerNames(connectConfig(), userDtos.stream().map(UserDto::getTomsId).toArray(String[]::new));
            userDtos.forEach(userDto -> userDto.setCustomerName((String) customerNames.get(userDto.getTomsId())));
            form.setAttribute("posts", userDtos);
        }
        return form.createForm(FORM);
    }

    @Override
    public void action (AuthenticationFlowContext context) {
        var authSession = context.getAuthenticationSession();
        role.setUserPost(context);
        authSession.setAuthNote(AUTH_FORM_SUCCESS, "0");
        context.success();
    }

    @Override
    public boolean requiresUser () {
        return false;
    }

    @Override
    public boolean configuredFor (KeycloakSession session, RealmModel realm, UserModel user) {
        return false;
    }

    @Override
    public void setRequiredActions (KeycloakSession session, RealmModel realm, UserModel user) {

    }

    @Override
    public void close () {

    }

    private Map<String, String> extractQueryParamsFromRedirectUri(String redirectUri) {
        Map<String, String> queryParameters = new HashMap<>();
        if(redirectUri != null && redirectUri.indexOf('?') >= 0) {
            redirectUri = redirectUri.substring(redirectUri.indexOf('?') + 1);
            String[] pairs = redirectUri.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf('=');
                if (idx >= 0)
                    queryParameters.put(URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8), URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8));
            }
        }
        return queryParameters;
    }

    private TbapiConnectConfig connectConfig() {
        ApplicationProperties properties = (ApplicationProperties) Lookup.lookup(ApplicationProperties.class);
        TbapiConnectConfig connectConfig = new TbapiConnectConfig();
        if(properties != null) {
            connectConfig.setHost(properties.getProperty(TbapiConstants.HOST));
            connectConfig.setPort(Integer.parseInt(properties.getProperty(TbapiConstants.PORT)));
            connectConfig.setAppname(properties.getProperty(TbapiConstants.AUTH_APPNAME));
            connectConfig.setUsername(properties.getProperty(TbapiConstants.AUTH_USERNAME));
            connectConfig.setPath(properties.getProperty(TbapiConstants.CUSTOMER_FIND_PATH));
            connectConfig.setSecure(Boolean.parseBoolean(TbapiConstants.SECURE));
        }


        return connectConfig;
    }
}
