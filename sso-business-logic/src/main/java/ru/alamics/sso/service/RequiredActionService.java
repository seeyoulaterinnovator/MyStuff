package ru.alamics.sso.service;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.jpa.entities.RequiredActionProviderEntity;
import org.keycloak.representations.idm.RequiredActionProviderRepresentation;
import ru.alamics.sso.jpa.repository.RealmRepository;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
@LocalBean
@Slf4j
public class RequiredActionService {

    @EJB
    private RealmRepository realmRepository;

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
