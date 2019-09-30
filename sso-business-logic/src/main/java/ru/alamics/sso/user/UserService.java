package ru.alamics.sso.user;

import org.jboss.resteasy.spi.NotFoundException;
import org.keycloak.models.KeycloakContext;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import ru.alamics.sso.user.web.AttributeRequest;

import java.util.Collections;
import java.util.List;

public class UserService {

    private final KeycloakSession session;
    private final KeycloakContext context;
    private final RealmModel realm;

    public UserService (KeycloakSession session) {
        this.session = session;
        this.context = session.getContext();
        this.realm = context.getRealm();
    }

    public UserModel createAttributes (final String userId, final List<AttributeRequest> attributes) {
        var user = getUser(userId);
        if(attributes != null) {
            attributes.forEach(attribute -> user.setAttribute(attribute.getName(), Collections.singletonList(attribute.getValue())));
        }
        return user;
    }

    public UserModel patchAttributes (final String userId, final List<AttributeRequest> attributeRequests) {
        var user = getUser(userId);
        if(attributeRequests != null) {
            attributeRequests.forEach(attributeRequest -> user.setAttribute(attributeRequest.getName(), Collections.singletonList(attributeRequest.getValue())));
        }
        return user;
    }


    public UserModel deleteAttributes (final String userId, final List<String> attributeNames) {
        var user = getUser(userId);
        if(attributeNames != null) {
            attributeNames.forEach(user::removeAttribute);
        }
        return user;
    }

    private UserModel getUser (String userId) {
        var user = session.users().getUserById(userId, realm);
        if (user == null) {
            throw new NotFoundException("User not found");
        }
        return user;
    }
}
