package ru.alamics.sso.jpa.model;

import jakarta.persistence.EntityManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.jpa.UserAdapter;
import org.keycloak.models.jpa.entities.UserEntity;

public class CustomUserAdapter extends UserAdapter {
    public CustomUserAdapter(KeycloakSession session, RealmModel realm, EntityManager em, UserEntity user) {
        super(session, realm, em, user);
    }

    public RealmModel getRealm() {
        return realm;
    }
}
