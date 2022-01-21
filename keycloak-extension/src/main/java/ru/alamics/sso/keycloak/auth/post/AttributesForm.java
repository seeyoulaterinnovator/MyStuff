package ru.alamics.sso.keycloak.auth.post;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.common.ClientConnection;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.UserSessionModel;
import org.keycloak.services.managers.AuthenticationManager;
import org.keycloak.sessions.AuthenticationSessionModel;
import ru.alamics.sso.auth.UserRole;
import ru.alamics.sso.keycloak.facade.CachedUserPostFacade;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.registration.dto.UserPostResponse;
import ru.alamics.sso.settings.SettingConstants;
import ru.alamics.sso.settings.SettingsService;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static org.keycloak.services.managers.AuthenticationManager.END_AFTER_REQUIRED_ACTIONS;
import static ru.alamics.sso.registration.model.UserConstants.*;

@Slf4j
public class AttributesForm implements Authenticator {
    private final static String DMP_ID = "dmp-kc-sit";
    private static final String FORM = "attributes.ftl";
    private final UserRole roleService;
    private CachedUserPostFacade cachedUserPostFacade;

    private SettingsService settingsService;

    public AttributesForm(UserRole roleService) {
        this.roleService = roleService;
        try {
            this.cachedUserPostFacade = (CachedUserPostFacade) new InitialContext().lookup("java:global/domru-sso/" + CachedUserPostFacade.class.getSimpleName());
        } catch (NamingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Something wrong with context");
        }
        settingsService = (SettingsService) Lookup.lookup(SettingsService.class);
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        final String DEBUG_STR = "authenticate";
        AuthenticationSessionModel authSession = context.getAuthenticationSession();

        log.info("{}: frame={}, user={}", DEBUG_STR, authSession.getAuthNote(I_FRAME), context.getUser().getId());
        UriInfo uriInfo = context.getUriInfo();
        MultivaluedMap<String, String> queryParams = uriInfo.getQueryParameters();
        queryParams.forEach((key, value) -> log.info("{}: key={} value={}", DEBUG_STR, key, value.toString()));
        String frame = uriInfo.getQueryParameters().getFirst(I_FRAME);
        Map<String, String> redirectUriQueryParams = extractQueryParamsFromRedirectUri(queryParams.getFirst(REDIRECT_URI));
        String redirectIframe = redirectUriQueryParams.get(I_FRAME);
        boolean isAuth = "1".equals(authSession.getAuthNote(AUTH_FORM_SUCCESS));//it`s magick

        if (frame != null || isAuth || redirectIframe != null) {
            UserModel user = context.getUser();
            List<UserPostResponse> attributes = null;
            try {
                attributes = cachedUserPostFacade.findByUserId(user.getId());
            } catch (NotFoundException e) {
                attributes = Collections.emptyList();
            }
            if (attributes != null) {
                attributes = attributes.stream()
                        .filter(attribute -> Objects.nonNull(attribute.getTomsId()) && Objects.nonNull(attribute.getUserRole()))
                        .collect(Collectors.toList());
            } else {
                attributes = Collections.emptyList();
            }
            if (attributes.isEmpty()) {
                context.success();
            } else {
                Response challenge = createForm(context, attributes);
                context.challenge(challenge);
            }
        } else {
            context.success();
        }


    }

    private Response createForm(AuthenticationFlowContext context, List<UserPostResponse> posts) {
        LoginFormsProvider form = context.form();

        if (!posts.isEmpty()) {
            form.setAttribute("posts", posts);
        }

        form.setAttribute("chooseOrganization", settingsService.getSettingsStringValue(SettingConstants.CHOOSE_ON_ORGANIZATION, context.getRealm().getId()));
        form.setAttribute("organization", settingsService.getSettingsStringValue(SettingConstants.ORGANIZATION, context.getRealm().getId()));
        form.setAttribute("roleUser", settingsService.getSettingsStringValue(SettingConstants.ROLE_USER, context.getRealm().getId()));
        form.setAttribute("footer", settingsService.getSettingsStringValue(SettingConstants.FOOTER, context.getRealm().getId()));
        form.setAttribute("phoneConst", settingsService.getSettingsStringValue(SettingConstants.PHONE_CONST, context.getRealm().getId()));
        form.setAttribute("phoneConstLink", settingsService.getSettingsStringValue(SettingConstants.PHONE_CONST_LINK, context.getRealm().getId()));
        form.setAttribute("homePage", settingsService.getSettingsStringValue(SettingConstants.HOME_PAGE, context.getRealm().getId()));

        return form.createForm(FORM);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        AuthenticationSessionModel authSession = context.getAuthenticationSession();
        roleService.setUserPost(context);
        KeycloakSession session = context.getSession();
        RealmModel realm = context.getAuthenticationSession().getRealm();
        session.userCache().clear();
        UserSessionModel userSession = session.sessions().getUserSession(realm, context.getAuthenticationSession().getParentSession().getId());
        ClientConnection clientConnection = session.getContext().getConnection();
        AuthenticationManager.backchannelLogout(session, realm, userSession, session.getContext().getUri(), clientConnection, session.getContext().getRequestHeaders(), true);
        authSession.setAuthNote(AUTH_FORM_SUCCESS, "0");

        String iframe = context.getUriInfo().getQueryParameters().getFirst(I_FRAME);
        String clientId = session.getContext().getClient().getClientId();
        if (iframe != null && DMP_ID.equals(clientId)) {
            authSession.setAuthNote(END_AFTER_REQUIRED_ACTIONS, "1");
        }
        context.success();
    }

    @Override
    public boolean requiresUser() {
        return false;
    }

    @Override
    public boolean configuredFor(KeycloakSession session, RealmModel realm, UserModel user) {
        return false;
    }

    @Override
    public void setRequiredActions(KeycloakSession session, RealmModel realm, UserModel user) {

    }

    @Override
    public void close() {

    }

    private Map<String, String> extractQueryParamsFromRedirectUri(String redirectUri) {
        Map<String, String> queryParameters = new HashMap<>();
        if (redirectUri != null && redirectUri.indexOf('?') >= 0) {
            redirectUri = redirectUri.substring(redirectUri.indexOf('?') + 1);
            String[] pairs = redirectUri.split("&");
            for (String pair : pairs) {
                int idx = pair.indexOf('=');
                if (idx >= 0) {
                    try {
                        queryParameters.put(URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8.name()), URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8.name()));
                    } catch (UnsupportedEncodingException ignore) {

                    }
                }
            }
        }
        return queryParameters;
    }
}
