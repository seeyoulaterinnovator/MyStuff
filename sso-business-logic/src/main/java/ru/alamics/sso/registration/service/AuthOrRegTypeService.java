package ru.alamics.sso.registration.service;

import ru.alamics.sso.jpa.entity.auth_reg.AuthOrRegTypeEntity;
import ru.alamics.sso.jpa.repository.AuthOrRegTypeRepository;
import ru.alamics.sso.keycloak.lookup.Lookup;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class AuthOrRegTypeService {
    @EJB
    private final AuthOrRegTypeRepository authOrRegTypeRepository;

    public AuthOrRegTypeService() {
        this.authOrRegTypeRepository = Lookup.lookup(AuthOrRegTypeRepository.class);

    }

    public AuthOrRegTypeEntity findAndReturn(int id) {
        return authOrRegTypeRepository.findAuthOrRegType(id);
    }
}
