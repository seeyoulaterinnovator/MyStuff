package ru.alamics.sso.registration.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import ru.alamics.sso.jpa.entity.auth_reg.AuthOrRegTypeEntity;
import ru.alamics.sso.jpa.repository.AuthOrRegTypeRepository;
import ru.alamics.sso.keycloak.lookup.Lookup;

@ApplicationScoped
public class AuthOrRegTypeService {
    @Inject
    AuthOrRegTypeRepository authOrRegTypeRepository;

    public AuthOrRegTypeService() {
        this.authOrRegTypeRepository = Lookup.lookup(AuthOrRegTypeRepository.class);
    }

    public AuthOrRegTypeEntity findAndReturn(int id) {
        return authOrRegTypeRepository.findAuthOrRegType(id);
    }
}
