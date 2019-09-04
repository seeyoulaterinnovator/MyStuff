package ru.alamics.sso.keycloak.access.service;

import ru.alamics.sso.keycloak.access.entity.Access;
import ru.alamics.sso.keycloak.access.repository.AccessRepository;

public class AccessService {

    private AccessRepository accessRepository = new AccessRepository();

    public Access getAccess(String userId, String tomsId) {
        return accessRepository.getAccess(userId, tomsId);
    }

    public Access save(Access access) {
        if (accessRepository.getAccess(access.getUserId(), access.getTomsId()) != null) {
            return null;
        }
        return accessRepository.save(access);
    }

    public Access edit(Access access) {
        Access accessDb = accessRepository.getAccess(access.getId());
        if (accessDb != null) {
            return null;
        }
        return accessRepository.update(access);
    }
}
