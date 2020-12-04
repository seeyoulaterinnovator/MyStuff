package ru.alamics.sso.keycloak.client;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.keycloak.authorization.admin.AuthorizationService;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.*;
import org.keycloak.models.utils.ModelToRepresentation;
import org.keycloak.models.utils.RepresentationToModel;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.services.ErrorResponse;
import org.keycloak.services.ErrorResponseException;
import org.keycloak.services.managers.ClientManager;
import org.keycloak.services.managers.RealmManager;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import org.keycloak.services.resources.admin.AdminRoot;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import org.keycloak.services.validation.ClientValidator;
import org.keycloak.services.validation.PairwiseClientValidator;
import org.keycloak.services.validation.ValidationMessages;
import ru.alamics.sso.client.ClientService;
import ru.alamics.sso.keycloak.lookup.Lookup;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Properties;

import static java.lang.Boolean.TRUE;

@Slf4j
public class ClientResource {
    private RealmModel realm;
    private AdminPermissionEvaluator auth;
    private AdminEventBuilder adminEvent;
    private KeycloakSession session;
    private ClientModel client;
    private ClientService service;

    public ClientResource(KeycloakSession session, AdminPermissionEvaluator auth, AdminAuth adminAuth, ClientModel client) {
        this.auth = auth;
        this.session = session;
        this.client = client;
        this.realm = session.getContext().getRealm();
        this.adminEvent = new AdminEventBuilder(realm, adminAuth, session, session.getContext().getConnection())
                .realm(realm).resource(ResourceType.CLIENT);

        this.service = (ClientService) Lookup.lookup(ClientService.class);
    }

    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public CustomClientRepresentation getClient() {
        auth.clients().requireView(client);

        CustomClientRepresentation representation = new CustomClientRepresentation(ModelToRepresentation.toRepresentation(client, session));

        representation.setAccess(auth.clients().getAccess(client));

        representation.setMainRedirectUri(service.getMainRedirectUri(client.getId()));

        return representation;
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(final CustomClientRepresentation rep) {
        auth.clients().requireConfigure(client);

        ValidationMessages validationMessages = new ValidationMessages();
        if (!ClientValidator.validate(rep, validationMessages) || !PairwiseClientValidator.validate(session, rep, validationMessages)) {
            Properties messages = AdminRoot.getMessages(session, realm, auth.adminAuth().getToken().getLocale());
            throw new ErrorResponseException(
                    validationMessages.getStringMessages(),
                    validationMessages.getStringMessages(messages),
                    Response.Status.BAD_REQUEST
            );
        }

        try {
            updateClientFromRep(rep, client, session);
            service.saveMainRedirectUri(client.getId(), rep.getMainRedirectUri());
            adminEvent.operation(OperationType.UPDATE).resourcePath(session.getContext().getUri()).representation(rep).success();
            updateAuthorizationSettings(rep);
            return Response.noContent().build();
        } catch (ModelDuplicateException e) {
            return ErrorResponse.exists("Client " + rep.getClientId() + " already exists");
        }
    }


    private void updateClientFromRep(ClientRepresentation rep, ClientModel client, KeycloakSession session) throws ModelDuplicateException {
        UserModel serviceAccount = this.session.users().getServiceAccount(client);
        if (TRUE.equals(rep.isServiceAccountsEnabled())) {
            if (serviceAccount == null) {
                new ClientManager(new RealmManager(session)).enableServiceAccount(client);
            }
        } else {
            if (serviceAccount != null) {
                new UserManager(session).removeUser(realm, serviceAccount);
            }
        }

        if (!rep.getClientId().equals(client.getClientId())) {
            new ClientManager(new RealmManager(session)).clientIdChanged(client, rep.getClientId());
        }

        if (rep.isFullScopeAllowed() != null && rep.isFullScopeAllowed() != client.isFullScopeAllowed()) {
            auth.clients().requireManage(client);
        }

        RepresentationToModel.updateClient(rep, client);
    }

    private void updateAuthorizationSettings(ClientRepresentation rep) {
        if (TRUE.equals(rep.getAuthorizationServicesEnabled())) {
            authorization().enable(false);
        } else {
            authorization().disable();
        }
    }

    private AuthorizationService authorization() {
        AuthorizationService resource = new AuthorizationService(this.session, this.client, this.auth, adminEvent);

        ResteasyProviderFactory.getInstance().injectProperties(resource);

        return resource;
    }

    @Data
    @NoArgsConstructor
    private static class CustomClientRepresentation extends ClientRepresentation {
        private String mainRedirectUri;

        public CustomClientRepresentation(ClientRepresentation representation) {
            setId(representation.getId());
            setOrigin(representation.getOrigin());
            setClientId(representation.getClientId());
            setName(representation.getName());
            setDescription(representation.getDescription());
            setEnabled(representation.isEnabled());
            setAdminUrl(representation.getAdminUrl());
            setPublicClient(representation.isPublicClient());
            setFrontchannelLogout(representation.isFrontchannelLogout());
            setProtocol(representation.getProtocol());
            setAttributes(representation.getAttributes());
            setAuthenticationFlowBindingOverrides(representation.getAuthenticationFlowBindingOverrides());
            setFullScopeAllowed(representation.isFullScopeAllowed());
            setBearerOnly(representation.isBearerOnly());
            setConsentRequired(representation.isConsentRequired());
            setStandardFlowEnabled(representation.isStandardFlowEnabled());
            setImplicitFlowEnabled(representation.isImplicitFlowEnabled());
            setDirectAccessGrantsEnabled(representation.isDirectAccessGrantsEnabled());
            setServiceAccountsEnabled(representation.isServiceAccountsEnabled());
            setSurrogateAuthRequired(representation.isSurrogateAuthRequired());
            setRootUrl(representation.getRootUrl());
            setBaseUrl(representation.getBaseUrl());
            setNotBefore(representation.getNotBefore());
            setNodeReRegistrationTimeout(representation.getNodeReRegistrationTimeout());
            setClientAuthenticatorType(representation.getClientAuthenticatorType());

            setDefaultClientScopes(representation.getDefaultClientScopes());
            setOptionalClientScopes(representation.getOptionalClientScopes());

            setRedirectUris(representation.getRedirectUris());

            setWebOrigins(representation.getWebOrigins());

            setDefaultRoles(representation.getDefaultRoles());

            setRegisteredNodes(representation.getRegisteredNodes());

            setProtocolMappers(representation.getProtocolMappers());
            setAuthorizationServicesEnabled(representation.getAuthorizationServicesEnabled());
        }
    }


}