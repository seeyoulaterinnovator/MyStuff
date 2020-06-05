package ru.alamics.sso.keycloak.auth.post;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.Authenticator;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import ru.alamics.sso.auth.UserRole;
import ru.alamics.sso.keycloak.facade.CachedUserPostFacade;
import ru.alamics.sso.registration.dto.UserPostResponse;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.core.Response;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static ru.alamics.sso.registration.model.UserConstants.*;

@Slf4j
public class AttributesForm implements Authenticator {
    private static final String FORM = "attributes.ftl";
    private final UserRole roleService;
    private CachedUserPostFacade cachedUserPostFacade;

    public AttributesForm(UserRole roleService) {
        this.roleService = roleService;
        try {
            this.cachedUserPostFacade = (CachedUserPostFacade) new InitialContext().lookup("java:global/domru-sso/" + CachedUserPostFacade.class.getSimpleName());
        } catch (NamingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Something wrong with context");
        }
    }

    @Override
    public void authenticate(AuthenticationFlowContext context) {
        final String DEBUG_STR = "authenticate";
        var authSession = context.getAuthenticationSession();

        log.info("{}: frame={}, user={}", DEBUG_STR, authSession.getAuthNote(I_FRAME), context.getUser().getId());
        var uriInfo = context.getUriInfo();
        var queryParams = uriInfo.getQueryParameters();
        queryParams.forEach((key, value) -> log.info("{}: key={} value={}", DEBUG_STR, key, value));

        String frame = uriInfo.getQueryParameters().getFirst(I_FRAME);
        var redirectUriQueryParams = extractQueryParamsFromRedirectUri(queryParams.getFirst(REDIRECT_URI));
        String redirectIframe = redirectUriQueryParams.get(I_FRAME);
        boolean isAuth = "1".equals(authSession.getAuthNote(AUTH_FORM_SUCCESS));//it`s magick

        if (frame != null || isAuth || redirectIframe != null) {
            var user = context.getUser();
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

        return form.createForm(FORM);
    }

    @Override
    public void action(AuthenticationFlowContext context) {
        var authSession = context.getAuthenticationSession();
        roleService.setUserPost(context);
        authSession.setAuthNote(AUTH_FORM_SUCCESS, "0");
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
                if (idx >= 0)
                    queryParameters.put(URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8), URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8));
            }
        }
        return queryParameters;
    }
}
