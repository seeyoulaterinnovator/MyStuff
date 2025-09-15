package ru.alamics.sso.keycloak.create.rest;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ru.alamics.sso.jpa.entity.BrandEntity;
import ru.alamics.sso.jpa.repository.BrandRepository;

import java.util.List;

@Path("/brands")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BrandResource {

    @Inject
    BrandRepository brandRepository;

    @GET
    public List<BrandEntity> list(@QueryParam("realm") String realm) {
        return brandRepository.findByRealm(realm);
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") String id) {
        return brandRepository.findById(id)
                .map(Response::ok)
                .orElse(Response.status(Response.Status.NOT_FOUND))
                .build();
    }

    @POST
    @Transactional
    public Response create(BrandEntity brand) {
        brandRepository.save(brand);
        return Response.status(Response.Status.CREATED).entity(brand).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") String id, BrandEntity brand) {
        brand.setId(id);
        brandRepository.save(brand);
        return Response.ok(brand).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") String id) {
        brandRepository.deleteById(id);
        return Response.noContent().build();
    }
}
