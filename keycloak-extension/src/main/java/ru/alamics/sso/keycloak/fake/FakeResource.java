package ru.alamics.sso.keycloak.fake;

import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.property.ApplicationProperties;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Collections;

public class FakeResource {

    protected KeycloakSession session;

    public FakeResource(KeycloakSession session) {

        this.session = session;
        ApplicationProperties properties = (ApplicationProperties) Lookup.lookup(ApplicationProperties.class);
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
