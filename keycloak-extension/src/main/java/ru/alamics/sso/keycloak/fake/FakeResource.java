package ru.alamics.sso.keycloak.fake;

import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.response.JsonResponse;

import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

public class FakeResource {

    protected KeycloakSession session;

    public FakeResource(KeycloakSession session) {

        this.session = session;
    }

    @POST
    @Path("/customerAccounts/names")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    public Response getFakeCustomerNames() {

        return JsonResponse.success()
                //.addResult("", Collections.EMPTY_LIST)
                .build();
    }

}
