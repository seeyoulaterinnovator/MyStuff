package ru.alamics.sso.keycloak.auth.form.new_auth.new_rest.auth;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.UserModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import ru.alamics.sso.auth.UserRole;
import ru.alamics.sso.keycloak.facade.CachedUserPostFacade;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.registration.dto.UserPostResponse;
import ru.alamics.sso.settings.SettingConstants;
import ru.alamics.sso.settings.SettingsService;
import ru.alamics.sso.util.Util;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.alamics.sso.registration.model.UserConstants.ATTR_TOMS_NAME;


@Slf4j
public class RestNewAttributesForm implements RequiredActionProvider {

    private final UserRole roleService;

    private final CachedUserPostFacade cachedUserPostFacade;

    private final SettingsService settingsService;

    public static final String PROVIDER_ID = "rest_post_selector";

    public RestNewAttributesForm() {
        this.roleService = Lookup.lookup(UserRole.class);
        this.cachedUserPostFacade = Lookup.lookup(CachedUserPostFacade.class);
        this.settingsService = Lookup.lookup(SettingsService.class);
    }

    @Override
    public void evaluateTriggers(RequiredActionContext context) {

    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {
        List<UserPostResponse> attributes;
        try {
            attributes = cachedUserPostFacade.findByUserId(context.getUser().getId());
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
        if (attributes.size() <= 1) {
            context.success();
        } else {
            context.challenge(createForm(context, attributes));
        }
    }


    @Override
    public void processAction(RequiredActionContext context) {
        roleService.setUserPost(context);
        context.getAuthenticationSession().removeAuthNote("rest_post");
        context.success();
    }

    private Response createForm(RequiredActionContext context, List<UserPostResponse> attributes) {
        context.getAuthenticationSession().setAuthNote("rest_post", attributes.toString());
        return context.form().createForm("");
    }

    @Override
    public void close() {

    }
}
