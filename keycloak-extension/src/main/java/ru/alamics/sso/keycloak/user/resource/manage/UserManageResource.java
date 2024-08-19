package ru.alamics.sso.keycloak.user.resource.manage;

import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.*;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.alamics.sso.jpa.entity.common.BlockType;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.registration.AttributeFormatException;
import ru.alamics.sso.registration.FoundException;
import ru.alamics.sso.registration.service.UserFindService;
import ru.alamics.sso.user.UserAttributeService;
import ru.alamics.sso.user.web.AttributeRequest;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class UserManageResource {

    private static final Logger log = LoggerFactory.getLogger(UserManageResource.class);
    private final KeycloakSession session;
    private final RealmModel realm;
    private final AdminEventBuilder eventBuilder;
    private final UserAttributeService attributeService;

    UserManageResource(KeycloakSession session, AdminEventBuilder eventBuilder) {
        this.session = session;
        KeycloakContext context = session.getContext();
        this.eventBuilder = eventBuilder.resource(ResourceType.USER);
        this.realm = context.getRealm();
        this.attributeService = new UserAttributeService(session, Lookup.lookup(UserFindService.class));
    }


    @Path("/block")
    @POST
    public Response blockUsers(List<String> ids) {
        changeUserBlockState(ids, false);
        return JsonResponse.success()
                .httpStatus(Response.Status.NO_CONTENT)
                .build();
    }

    @Path("/unlock")
    @POST
    public Response unlockUsers(List<String> ids) {
        changeUserBlockState(ids, true);
        return JsonResponse.success()
                .httpStatus(Response.Status.NO_CONTENT)
                .build();
    }

    @Path("/credential/reset")
    @POST
    public Response resetPassword(List<String> ids) {
        UserProvider userProvider = getUsers();
        if (ids != null) {
            ids.forEach(id -> {
                UserModel user = userProvider.getUserById(realm, id);
                user.addRequiredAction(UserModel.RequiredAction.UPDATE_PASSWORD);
            });
        }
        return JsonResponse.success()
                .httpStatus(Response.Status.NO_CONTENT)
                .build();
    }

    private void changeUserBlockState(List<String> ids, boolean unlocking) {
        UserProvider userProvider = getUsers();
        if (ids != null) {
            ids.forEach(id -> {
                UserModel user = userProvider.getUserById(realm, id);
                if (user != null) {
                    user.setEnabled(unlocking);
                    addManagerBlockAttribute(user);
                    UserRepresentation rep = ModelToRepresentation.toRepresentation(session, realm, user);
                    eventBuilder.operation(OperationType.UPDATE)
                            .resourcePath(session.getContext().getUri())
                            .representation(rep)
                            .realm(realm)
                            .success();
                }
            });
        }
    }

    private void addManagerBlockAttribute(UserModel user) {
        if (!user.isEnabled()) {
            try {
                if (user.getFirstAttribute(BlockType.SYSTEM_BLOCK.getType()) != null) {
                    attributeService.deleteAttributes(user.getId(), Collections.singletonList(BlockType.SYSTEM_BLOCK.getType()));
                }
                attributeService.createAttributes(user.getId(),
                        Collections.singletonList(new AttributeRequest(BlockType.MANAGER_BLOCK.getType(), LocalDateTime.now().toString())));

            } catch (FoundException | AttributeFormatException e) {
                log.warn(e.getMessage(), e);
            }
        } else {
            attributeService.deleteAttributes(user.getId(), Collections.singletonList(BlockType.MANAGER_BLOCK.getType()));

            if (user.getFirstAttribute(BlockType.SYSTEM_BLOCK.getType()) != null) {
                attributeService.deleteAttributes(user.getId(), Collections.singletonList(BlockType.SYSTEM_BLOCK.getType()));
            }
        }
    }

    private UserProvider getUsers() {
        return this.session.users();
    }
}
