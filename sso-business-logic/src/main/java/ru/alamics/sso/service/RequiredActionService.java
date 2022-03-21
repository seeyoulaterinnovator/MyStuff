package ru.alamics.sso.service;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.models.RequiredActionProviderModel;
import org.keycloak.models.jpa.entities.RealmEntity;
import org.keycloak.models.jpa.entities.RequiredActionProviderEntity;
import org.keycloak.representations.idm.RequiredActionProviderRepresentation;
import ru.alamics.sso.jpa.repository.RealmRepository;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Stateless
@LocalBean
@Slf4j
public class RequiredActionService {

    @EJB
    private RealmRepository realmRepository;

    private static RequiredActionProviderRepresentation toRepresentation(RequiredActionProviderModel model) {
        RequiredActionProviderRepresentation rep = new RequiredActionProviderRepresentation();
        rep.setAlias(model.getAlias());
        rep.setName(model.getName());
        rep.setDefaultAction(model.isDefaultAction());
        rep.setPriority(model.getPriority());
        rep.setEnabled(model.isEnabled());
        rep.setConfig(model.getConfig());
        return rep;
    }

    private static RequiredActionProviderModel entityToModel(RequiredActionProviderEntity entity) {
        RequiredActionProviderModel model = new RequiredActionProviderModel();
        model.setId(entity.getId());
        model.setProviderId(entity.getProviderId());
        model.setAlias(entity.getAlias());
        model.setEnabled(entity.isEnabled());
        model.setDefaultAction(entity.isDefaultAction());
        model.setPriority(entity.getPriority());
        model.setName(entity.getName());
        Map<String, String> config = new HashMap<>();
        if (entity.getConfig() != null) config.putAll(entity.getConfig());
        model.setConfig(config);
        return model;
    }

    public List<RequiredActionProviderRepresentation> getRequiredActions(String realmId) {
        RealmEntity realmEntity = realmRepository.findRealmEntityById(realmId);
        return realmEntity.getRequiredActionProviders().stream()
                .filter(RequiredActionProviderEntity::isEnabled)
                .map(RequiredActionService::entityToModel)
                .map(RequiredActionService::toRepresentation)
                .collect(Collectors.toList());
    }
}
