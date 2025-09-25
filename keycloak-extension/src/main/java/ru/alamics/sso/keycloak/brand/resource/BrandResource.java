package ru.alamics.sso.keycloak.brand.resource;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.NoCache;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.registration.service.BrandService;

@Slf4j
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BrandResource {

    private final KeycloakSession session;
    private final AdminPermissionEvaluator auth;
    private final BrandService brandService;

    public BrandResource(KeycloakSession session, AdminPermissionEvaluator auth) {
        this.session = session;
        this.auth = auth;
        this.brandService = Lookup.lookup(BrandService.class);
    }

    private String realm() {
        return session.getContext().getRealm().getName();
    }

    @POST
    @Path("/{brandId}")
    @NoCache
    public Response attachToRealm(@PathParam("brandId") String brandId,
                                  @QueryParam("default") @DefaultValue("false") boolean makeDefault) {
        auth.users().requireManage();

        brandService.attachBrandToRealm(realm(), brandId, makeDefault);

        return JsonResponse.success()
                .message("Brand attached to realm" + (makeDefault ? " and set as default" : ""))
                .build();
    }

    @PUT
    @Path("/default/{brandId}")
    @NoCache
    public Response setDefault(@PathParam("brandId") String brandId) {
        auth.users().requireManage();

        brandService.setDefaultBrand(realm(), brandId);

        return JsonResponse.success().message("Default brand set").build();
    }

    @DELETE
    @Path("/{brandId}")
    @NoCache
    public Response detach(@PathParam("brandId") String brandId) {
        auth.users().requireManage();

        brandService.removeBrandFromRealm(realm(), brandId);

        return JsonResponse.success().message("Brand detached from realm").build();
    }

}