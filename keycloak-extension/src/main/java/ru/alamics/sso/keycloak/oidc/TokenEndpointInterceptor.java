package ru.alamics.sso.keycloak.oidc;

import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import io.smallrye.mutiny.unchecked.Unchecked;
import jakarta.inject.Inject;
import jakarta.ws.rs.HttpMethod;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.core.Form;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.ext.Provider;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.reactive.server.ServerRequestFilter;
import org.keycloak.Config;
import org.keycloak.OAuth2Constants;
import ru.alamics.sso.keycloak.GeneralRealm;
import ru.alamics.sso.keycloak.util.HttpUtil;
import ru.alamics.sso.property.ApplicationProperties;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Доработка для клиентов по типу ЛК, которые нарушают спецификацию OIDC
 * и передают дубли form url encoded параметров
 * see org.keycloak.protocol.oidc.endpoints.TokenEndpoint#checkParameterDuplicated()
 */
@Provider
@PreMatching
@Slf4j
public class TokenEndpointInterceptor {
    @Inject
    ApplicationProperties properties;

    @ServerRequestFilter(preMatching = true)
    public Uni<Void> filter(ContainerRequestContext requestContext) {
        var result = Uni.createFrom().voidItem();
        if(!(
                requestContext.getUriInfo().getPath().matches("/realms/[^/]+/protocol/openid-connect/token")
                        && HttpMethod.POST.equals(requestContext.getMethod())
                        && requestContext.getMediaType() != null
                        && requestContext.getMediaType().isCompatible(MediaType.valueOf(MediaType.APPLICATION_FORM_URLENCODED))
        )) return result;

        String realmName = requestContext.getUriInfo().getPathSegments().get(1).toString();

        if(Config.getAdminRealm().equals(realmName) || GeneralRealm.MANAGER_REALMS.contains(realmName)) return result;

        if(requestContext.getLength() <= 0 || requestContext.getLength() > 1204 * 1024) return result;

        return Uni.createFrom().voidItem().chain(Unchecked.supplier(() -> {
                    byte[] content;
                    try(var stream = requestContext.getEntityStream()) {
                        // see runSubscriptionOn
                        // noinspection BlockingMethodInNonBlockingContext
                        content = stream.readNBytes(requestContext.getLength());
                    }
                    try {
                        List<String> lenientParams = Arrays.stream(
                                        properties.getProperty("oidc.token.allowedDuplicateParameters", "")
                                                .split(",")
                                )
                                .map(String::trim)
                                .filter(s -> !s.isBlank())
                                .toList();
                        var map = HttpUtil.parseFormUrlEncoded(requestContext.getMediaType(), content).asMap();
                        if(map.values().stream().anyMatch(values -> values.size() > 1)) {
                            if(map.entrySet()
                                    .stream()
                                    .filter(entry -> !lenientParams.contains(entry.getKey()))
                                    .map(Map.Entry::getValue)
                                    .allMatch(values -> values.stream().distinct().count() == 1)
                            ) {
                                map = new MultivaluedHashMap<>(
                                        map.entrySet()
                                                .stream()
                                                .collect(Collectors.toMap(
                                                        Map.Entry::getKey,
                                                        entry -> {
                                                            if(lenientParams.contains(entry.getKey())) {
                                                                if(entry.getKey().equals(OAuth2Constants.SCOPE)) {
                                                                    return entry.getValue()
                                                                            .stream()
                                                                            .filter(s -> !s.isBlank())
                                                                            .collect(Collectors.joining(" "));
                                                                }
                                                            }
                                                            return entry.getValue().get(0);
                                                        }
                                                ))
                                );
                                content = HttpUtil.writeFormUrlEncoded(requestContext.getMediaType(), new Form(map));
                            } else {
                                log.debug(
                                        "Token request has parameters collision for: {}",
                                        map.entrySet()
                                                .stream()
                                                .filter(entry -> entry.getValue().stream().distinct().count() > 1)
                                                .map(Map.Entry::getKey)
                                                .filter(key -> !lenientParams.contains(key))
                                                .collect(Collectors.joining(", "))
                                );
                            }
                        }
                    } catch (Exception e) {
                        log.debug(e.getMessage(), e);
                    }
                    requestContext.setEntityStream(new ByteArrayInputStream(content));
                    return result;
                }))
                .runSubscriptionOn(Infrastructure.getDefaultWorkerPool())
                .replaceWith(result);
    }
}
