package ru.alamics.sso.keycloak.auth.form.new_auth.newAuthReqActions;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.RequiredActionContext;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.forms.login.LoginFormsProvider;
import org.keycloak.models.RequiredActionProviderModel;
import org.keycloak.sessions.AuthenticationSessionModel;
import ru.alamics.sso.jpa.entity.auth_reg.AuthOrRegType;
import ru.alamics.sso.registration.service.RegisteredUsersService;

import jakarta.ws.rs.core.Response;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class EmptyReq implements RequiredActionProvider {

    private final RegisteredUsersService registeredUsersService;

    public EmptyReq(RegisteredUsersService registeredUsersService) {
        this.registeredUsersService = registeredUsersService;
    }

    public static final String PROVIDER_ID = "empty_req";

    private static final String EMPTY_PAGE = "empty-page.ftl";

    public final static String CLIENT_B2B = "b2b";

    @Override
    public void evaluateTriggers(RequiredActionContext context) {
    }

    @Override
    public void requiredActionChallenge(RequiredActionContext context) {

        if (context.getAuthenticationSession().getAuthNote("addRegisteredUser") != null) {
            addRegisteredUser(context);
            context.getAuthenticationSession().removeAuthNote("addRegisteredUser");
        }


        if (context.getAuthenticationSession().getClient().getClientId().equals(CLIENT_B2B)) {
            context.challenge(createForm(context));
        } else {
            context.success();
        }
    }

    @Override
    public void processAction(RequiredActionContext context) {
        context.success();
    }

    private Response createForm(RequiredActionContext context) {
        LoginFormsProvider form = context.form();
        return form.createForm(EMPTY_PAGE);
    }

    @Override
    public void close() {

    }

    public void addRegisteredUser(RequiredActionContext context) {
        AuthenticationSessionModel authenticationSessionModel = context.getAuthenticationSession();

        List<RequiredActionProviderModel> requiredActionProviderModels = context.getRealm()
                .getRequiredActionProvidersStream()
                .filter(RequiredActionProviderModel::isDefaultAction)
                .toList();

        AuthOrRegType[] authOrRegTypes = AuthOrRegType.values();

        Set<String> providerIds = Arrays.stream(authOrRegTypes)
                .map(AuthOrRegType::getReqActProviderName)
                .collect(Collectors.toSet());

        Optional<AuthOrRegType> optionalAuthOrRegType = requiredActionProviderModels.stream()
                .map(RequiredActionProviderModel::getProviderId)
                .filter(providerIds::contains)
                .findFirst()
                .flatMap(e -> Arrays.stream(authOrRegTypes)
                        .filter(z -> z.getReqActProviderName().equals(e))
                        .findFirst());

        if (optionalAuthOrRegType.isPresent()) {
            log.info(" optionalAuthOrRegType.isPresent()" );
            log.info(" optionalAuthOrRegType.get().getId() is : " + optionalAuthOrRegType.get().getId());
            registeredUsersService.saveSuccessfulReg(context.getUser().getId(), context.getRealm().getId(), authenticationSessionModel.getClient().getClientId(), optionalAuthOrRegType.get().getId());
        } else {
            log.info(" else AuthOrRegType.LOG_PASS.getId() is " + AuthOrRegType.LOG_PASS.getId());
            registeredUsersService.saveSuccessfulReg(context.getUser().getId(), context.getRealm().getId(), authenticationSessionModel.getClient().getClientId(), AuthOrRegType.LOG_PASS.getId());
        }


    }
}
