package ru.alamics.sso.user;

import jakarta.ws.rs.NotFoundException;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.registration.AttributeFormatException;
import ru.alamics.sso.registration.FoundException;
import ru.alamics.sso.registration.model.UserConstants;
import ru.alamics.sso.registration.service.UserFindService;
import ru.alamics.sso.user.web.AttributeRequest;
import ru.alamics.sso.util.validator.NotValidException;
import ru.alamics.sso.util.validator.PhoneValidator;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class UserAttributeService {

    private final KeycloakSession session;
    private final KeycloakContext context;
    private final RealmModel realm;
    private final UserFindService userFindService;

    public UserAttributeService(KeycloakSession session, UserFindService userFindService) {
        this.session = session;
        this.context = session.getContext();
        this.realm = context.getRealm();
        this.userFindService = userFindService;
    }

    public UserModel createAttributes(final String userId, final List<AttributeRequest> attributes) throws FoundException, AttributeFormatException {
        UserModel user = getUser(userId);
        if (attributes != null) {
            checkPhoneInAttr(attributes, userId);
            attributes.forEach(attribute -> user.setAttribute(attribute.getName(), Collections.singletonList(attribute.getValue())));
        }
        return user;
    }

    public UserModel patchAttributes(final String userId, final List<AttributeRequest> attributeRequests) throws FoundException, AttributeFormatException {
        UserModel user = getUser(userId);
        if (attributeRequests != null) {
            checkPhoneInAttr(attributeRequests, userId);
            attributeRequests.forEach(attributeRequest -> user.setAttribute(attributeRequest.getName(), Collections.singletonList(attributeRequest.getValue())));
        }
        return user;
    }


    public UserModel deleteAttributes(final String userId, final List<String> attributeNames) {
        UserModel user = getUser(userId);
        if (attributeNames != null) {
            attributeNames.forEach(user::removeAttribute);
        }
        return user;
    }

    private UserModel getUser(String userId) {
        UserModel user = session.users().getUserById(realm, userId);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        return user;
    }

    private void checkPhoneInAttr(final List<AttributeRequest> attributes, String userId) throws FoundException, AttributeFormatException {
        Optional<AttributeRequest> presentPhone = attributes.stream()
                .filter(x -> UserConstants.ATTR_PHONE_NAME.equals(x.getName()) && (x.getValue() != null && !x.getValue().isEmpty()))
                .findFirst();
        if (presentPhone.isPresent()) {

            try {
                PhoneValidator.validate(presentPhone.get().getValue());
            } catch (NotValidException e) {
                throw new AttributeFormatException("phone");
            }

            UserEntity user = userFindService.getUserByPhoneAndExcludedUserId(realm, presentPhone.get().getValue(), userId);
            if (user != null) {
                throw new FoundException("Another user found by phone");
            }
        }
    }
}
