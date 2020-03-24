package ru.alamics.sso.keycloak.policy.rest;

import org.jboss.resteasy.annotations.cache.NoCache;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.response.JsonResponse;

import javax.ws.rs.GET;
import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class PasswordPolicyProvider {

    private final KeycloakSession session;

    public PasswordPolicyProvider (KeycloakSession session) {
        this.session = session;
    }


    @GET
    @NoCache
    public Response getRealmPasswordPolicy() {
        var realm = session.getContext().getRealm();
        var realmPasswordPolicy = realm.getPasswordPolicy();
        var policies = realmPasswordPolicy.getPolicies();
        List<PasswordPolicyDto> ret = Optional.ofNullable(policies).orElseGet(Collections::emptySet)
                .stream()
                .map(policy -> PasswordPolicyDto.builder()
                        .value(realmPasswordPolicy.getPolicyConfig(policy))
                        .key(policy)
                        .build())
                .collect(Collectors.toList());


        return JsonResponse.success()
                .addResult("policies", ret)
                .build();
    }
}
