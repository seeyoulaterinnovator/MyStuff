package ru.alamics.sso.keycloak.rest;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.services.resources.admin.AdminAuth;

@Getter
@Setter
@AllArgsConstructor
public class InitSession {
    private KeycloakSession session;
    private RealmModel realmFromToken;
    private AdminAuth adminAuth;
}
