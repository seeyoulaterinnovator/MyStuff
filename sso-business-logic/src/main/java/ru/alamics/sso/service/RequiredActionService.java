package ru.alamics.sso.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.jpa.entities.RequiredActionProviderEntity;
import org.keycloak.representations.idm.RequiredActionProviderRepresentation;
import ru.alamics.sso.jpa.repository.RealmRepository;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class RequiredActionService {
    @Inject
    RealmRepository realmRepository;

    private static RequiredActionProviderRepresentation entityToRepresentation(RequiredActionProviderEntity entity) {
        RequiredActionProviderRepresentation rep = new RequiredActionProviderRepresentation();
        rep.setAlias(entity.getAlias());
        rep.setName(entity.getName());
        rep.setDefaultAction(entity.isDefaultAction());
        return rep;
    }

    public List<RequiredActionProviderRepresentation> getRequiredActions(String realmId) {
        return realmRepository.findRealmEntityById(realmId).getRequiredActionProviders().stream()
                .filter(RequiredActionProviderEntity::isEnabled)
                .map(RequiredActionService::entityToRepresentation)
                .collect(Collectors.toList());
    }
}
