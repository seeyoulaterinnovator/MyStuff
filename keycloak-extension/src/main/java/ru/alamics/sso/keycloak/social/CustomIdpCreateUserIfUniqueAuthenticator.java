package ru.alamics.sso.keycloak.social;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.AuthenticationFlowContext;
import org.keycloak.authentication.authenticators.broker.IdpCreateUserIfUniqueAuthenticator;
import org.keycloak.authentication.authenticators.broker.util.ExistingUserInfo;
import org.keycloak.authentication.authenticators.broker.util.SerializedBrokeredIdentityContext;
import org.keycloak.broker.provider.BrokeredIdentityContext;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.services.validation.Validation;
import ru.alamics.sso.keycloak.registration.userpost.UserPostCreatorProvider;
import ru.alamics.sso.registration.FoundUserPostException;
import ru.alamics.sso.registration.dto.UserPostRequest;
import ru.alamics.sso.registration.model.FormConstants;
import ru.alamics.sso.registration.model.MessageConstants;
import ru.alamics.sso.registration.service.UserFindService;
import ru.alamics.sso.registration.service.UserPostService;
import ru.alamics.sso.user.mapper.UserMapper;
import ru.alamics.sso.util.validator.NotValidException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.NotFoundException;

@Slf4j
public class CustomIdpCreateUserIfUniqueAuthenticator extends IdpCreateUserIfUniqueAuthenticator {
    private final UserPostService userPostService;
    private final UserFindService userFindService;

    public CustomIdpCreateUserIfUniqueAuthenticator() {
        try {
            this.userPostService = (UserPostService) new InitialContext().lookup("java:global/domru-sso/" + UserPostService.class.getSimpleName());
            this.userFindService = (UserFindService) new InitialContext().lookup("java:global/domru-sso/" + UserFindService.class.getSimpleName());
        } catch (NamingException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException("Something wrong with context");
        }
    }

    @Override
    protected void userRegisteredSuccess(AuthenticationFlowContext context, UserModel registeredUser, SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {
        UserPostRequest userPostRequest = UserMapper.toUserPostRequest(registeredUser);
        userPostRequest.setRoleId(UserPostCreatorProvider.ROLE_ID);

        try {
            userPostService.addUserPostAndAllSystemRole(userPostRequest);
        } catch (NotFoundException | FoundUserPostException | NotValidException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Override
    protected ExistingUserInfo checkExistingUser(AuthenticationFlowContext context, String
            username, SerializedBrokeredIdentityContext serializedCtx, BrokeredIdentityContext brokerContext) {
        ExistingUserInfo user = super.checkExistingUser(context, username, serializedCtx, brokerContext);
        if (user != null) {
            return user;
        }

        final String phone = serializedCtx.getFirstAttribute(FormConstants.FIELD_PHONE);
        if (!Validation.isBlank(phone)) {
            UserEntity userEntity = userFindService.getUserByPhone(context.getRealm(), phone);
            if (userEntity != null) {
                return new ExistingUserInfo(userEntity.getId(), MessageConstants.PHONE, phone);
            }
        }

        return null;
    }
}