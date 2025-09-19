package ru.alamics.sso.keycloak.brand.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.jboss.resteasy.reactive.NoCache;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resources.admin.permissions.AdminPermissionEvaluator;
import ru.alamics.sso.jpa.entity.RealmBrandEntity;
import ru.alamics.sso.jpa.repository.BrandRepository;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.response.JsonResponse;

import java.util.List;

@Slf4j
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BrandResource {

    private static final String UK_REALM_BRAND_REALM_BRAND = "UK_REALM_BRAND_REALM_BRAND";

    private final KeycloakSession session;
    private final AdminPermissionEvaluator auth;
    private final BrandRepository brandRepository;

    public BrandResource(KeycloakSession session, AdminPermissionEvaluator auth) {
        this.session = session;
        this.auth = auth;
        this.brandRepository = Lookup.lookup(BrandRepository.class);
    }

    private String realm() {
        return session.getContext().getRealm().getName();
    }

    @GET
    @Path("")
    @NoCache
    public Response list() {
        auth.users().requireView();

        List<RealmBrandEntity> links = brandRepository.findByRealm(realm());
        List<BrandItemDto> items = links.stream()
                .map(rb -> new BrandItemDto(
                        rb.getBrand().getId(),
                        rb.getBrand().getCode(),
                        rb.getBrand().getName(),
                        Boolean.TRUE.equals(rb.getIsDefault())
                ))
                .toList();

        return JsonResponse.success()
                .addResult("realmId", realm())
                .addResult("brands", items)
                .build();
    }

    @GET
    @Path("/default")
    @NoCache
    public Response getDefault() {
        auth.users().requireView();

        return brandRepository.findDefaultByRealm(realm())
                .map(b -> JsonResponse.success()
                        .addResult("realmId", realm())
                        .addResult("brand", new BrandItemDto(
                                b.getId(), b.getCode(), b.getName(), true
                        ))
                        .build())
                .orElseGet(() -> JsonResponse.success()
                        .addResult("realmId", realm())
                        .addResult("brand", null)
                        .build());
    }

    @POST
    @Path("/{brandId}")
    @NoCache
    public Response attachToRealm(@PathParam("brandId") String brandId,
                                  @QueryParam("default") @DefaultValue("false") boolean makeDefault) {
        auth.users().requireManage();

        brandRepository.findById(brandId)
                .orElseThrow(() -> new NotFoundException("Brand not found: " + brandId));

        if (brandRepository.isBrandInRealm(realm(), brandId)) {
            return JsonResponse.success()
                    .addResult("realmId", realm())
                    .message("Brand already attached to realm")
                    .build();
        }

        try {
            brandRepository.addBrandToRealm(realm(), brandId, makeDefault);
            if (makeDefault) {
                brandRepository.setDefaultBrand(realm(), brandId);
            }
            return JsonResponse.success()
                    .addResult("realmId", realm())
                    .message("Brand attached to realm" + (makeDefault ? " and set as default" : ""))
                    .build();
        } catch (PersistenceException e) {
            if (isUk(e)) {
                return JsonResponse.success()
                        .addResult("realmId", realm())
                        .message("Brand already attached to realm")
                        .build();
            }
            throw e;
        }
    }

    @PUT
    @Path("/default/{brandId}")
    @NoCache
    public Response setDefault(@PathParam("brandId") String brandId) {
        auth.users().requireManage();

        if (!brandRepository.isBrandInRealm(realm(), brandId)) {
            throw new NotFoundException("Brand is not attached to realm");
        }

        brandRepository.setDefaultBrand(realm(), brandId);
        return JsonResponse.success()
                .addResult("realmId", realm())
                .message("Default brand set")
                .build();
    }

    @DELETE
    @Path("/{brandId}")
    @NoCache
    public Response detach(@PathParam("brandId") String brandId) {
        auth.users().requireManage();

        if (!brandRepository.isBrandInRealm(realm(), brandId)) {
            throw new NotFoundException("Brand is not attached to realm");
        }

        brandRepository.removeBrandFromRealm(realm(), brandId);
        return JsonResponse.success()
                .addResult("realmId", realm())
                .message("Brand detached from realm")
                .build();
    }

    private boolean isUk(Throwable t) {
        while (t != null) {
            if (t instanceof ConstraintViolationException cve) {
                if (cve.getConstraintName() != null && cve.getConstraintName().equalsIgnoreCase(BrandResource.UK_REALM_BRAND_REALM_BRAND)) {
                    return true;
                }
            }
            t = t.getCause();
        }
        return false;
    }

    @Data
    @AllArgsConstructor
    public static class BrandItemDto {
        @JsonProperty("brandId")
        private String brandId;

        private String code;

        @JsonProperty("brandName")
        private String brandName;

        @JsonProperty("isDefault")
        private boolean isDefault;
    }
}