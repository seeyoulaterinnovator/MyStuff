package ru.alamics.sso.keycloak.policy.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.NoCache;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.PasswordPolicy;
import org.keycloak.models.RealmModel;
import ru.alamics.sso.keycloak.response.JsonResponse;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class PasswordPolicyProvider {

    private final KeycloakSession session;

    public PasswordPolicyProvider(KeycloakSession session) {
        this.session = session;
    }


    @GET
    @NoCache
    public Response getRealmPasswordPolicy() {
        RealmModel realm = session.getContext().getRealm();
        PasswordPolicy realmPasswordPolicy = realm.getPasswordPolicy();
        Set<String> policies = realmPasswordPolicy.getPolicies();
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
