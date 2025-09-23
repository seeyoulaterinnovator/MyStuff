package ru.alamics.sso.keycloak.brand.resource;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.NoCache;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.jpa.entity.RealmBrandEntity;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.keycloak.response.JsonResponse;
import ru.alamics.sso.registration.service.BrandService;

import java.util.List;

@Slf4j
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Path("")
public class PublicBrandResource {

    private final KeycloakSession session;
    private final BrandService brandService;

    public PublicBrandResource(KeycloakSession session) {
        this.session = session;
        this.brandService = Lookup.lookup(BrandService.class);
    }

    @GET
    @Path("")
    @NoCache
    public Response list() {
        String realm = getRealm();
        List<RealmBrandEntity> links = brandService.getBrandsForRealm(realm);

        List<BrandItemDto> items = links.stream()
                .map(rb -> new BrandItemDto(
                        rb.getBrand().getId(),
                        rb.getBrand().getCode(),
                        rb.getBrand().getName(),
                        Boolean.TRUE.equals(rb.getIsDefault())
                ))
                .toList();

        return JsonResponse.success()
                .addResult("id", realm)
                .addResult("brands", items)
                .build();
    }

    @GET
    @Path("/default")
    @NoCache
    public Response getDefault() {
        String realm = getRealm();
        return brandService.getDefaultBrand(realm)
                .map(b -> JsonResponse.success()
                        .addResult("id", realm)
                        .addResult("brand", new BrandItemDto(b.getId(), b.getCode(), b.getName(), true))
                        .build())
                .orElseGet(() -> JsonResponse.success()
                        .addResult("id", realm)
                        .addResult("brand", null)
                        .build());
    }

    private String getRealm() {
        return session.getContext().getRealm().getName();
    }

    @Data
    @AllArgsConstructor
    public static class BrandItemDto {
        @JsonProperty("brandId")
        private String brandId;

        private String code;

        @JsonProperty("brandName")
        private String brandName;

        private boolean isDefault;
    }
}
