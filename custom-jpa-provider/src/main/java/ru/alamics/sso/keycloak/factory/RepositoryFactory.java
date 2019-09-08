package ru.alamics.sso.keycloak.factory;

import org.keycloak.connections.jpa.JpaConnectionProvider;
import org.keycloak.models.KeycloakSession;
import ru.alamics.sso.keycloak.repository.UserHistoryLoginRepository;
import ru.alamics.sso.keycloak.repository.impl.UserHistoryLoginRepositoryImpl;

import javax.persistence.EntityManager;

public class RepositoryFactory {

    public static UserHistoryLoginRepository create(KeycloakSession session) {
        EntityManager em = session.getProvider(JpaConnectionProvider.class).getEntityManager();
        return new UserHistoryLoginRepositoryImpl(em);
    }

}
