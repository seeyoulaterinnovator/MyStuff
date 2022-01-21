package ru.alamics.sso.keycloak.settings;

import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.settings.SettingsDto;
import ru.alamics.sso.settings.SettingsService;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
@Consumes(MediaType.APPLICATION_JSON)
public class SettingsResource {

    private final KeycloakSession session;
    private final SettingsService service;

    public SettingsResource(KeycloakSession session, SettingsService service) {
        this.session = session;
        this.service = service;
    }

    @Path("/")
    @GET
    public Response getSettings() {
        String realmId = session.getContext().getRealm().getId();
        return JsonResponse.success()
                .addResult("settings", service.getRealmSettings(realmId))
                .build();
    }

    @Path("/{settingId}")
    @DELETE
    public Response deleteSetting(@PathParam("settingId") final String settingId) {
        String realmId = session.getContext().getRealm().getId();
        service.deleteSetting(settingId);
        return JsonResponse.success()
                .httpStatus(Response.Status.NO_CONTENT)
                .build();
    }

    @Path("/{settingId}")
    @PUT
    public Response saveSetting(@PathParam("settingId") final String settingId, final SettingsDto setting) {
        setting.setId(settingId);
        return JsonResponse.success()
                .addResult("setting", service.save(setting))
                .build();
    }
}
