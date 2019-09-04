package ru.alamics.sso.keycloak.access.rest;

import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.annotations.cache.NoCache;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;
import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.access.model.Access;
import ru.alamics.sso.keycloak.mapper.DataMapper;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.keycloak.search.dto.UserDto;

import javax.persistence.EntityManager;
import javax.persistence.Tuple;
import javax.ws.rs.*;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

@Slf4j
public class AccessResource {


    protected KeycloakSession session;

    public AccessResource(KeycloakSession session) {
        this.session = session;
    }

    private EntityManager getEM() {
        return session.getProvider(JpaConnectionProvider.class).getEntityManager();
    }

    @POST
    @Path("/create")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    public Response create(Access access, HttpHeaders headers) {
        return JsonResponse.success()
                .addResult("users-info", null)
                .build();
    }

    @POST
    @Path("/delete/{id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    public Response delete(@PathParam("id") String id) {
        return JsonResponse.success()
                .addResult("users-info", null)
                .build();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    public Response get(@PathParam("id") String id) {
        return JsonResponse.success()
                .addResult("users-info", null)
                .build();
    }

    @GET
    @Path("")
    @Produces(MediaType.APPLICATION_JSON + ";charset=UTF-8")
    @Consumes(MediaType.APPLICATION_JSON)
    @NoCache
    public Response getAll() {
        return JsonResponse.success()
                .addResult("users-info", null)
                .build();
    }
}
