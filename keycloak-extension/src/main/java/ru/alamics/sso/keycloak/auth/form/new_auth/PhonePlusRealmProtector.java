package ru.alamics.sso.keycloak.auth.form.new_auth;

import lombok.Data;
import org.keycloak.models.RealmModel;

@Data
public class PhonePlusRealmProtector {

    private String phoneNumber;

    private RealmModel userRealm;

    public PhonePlusRealmProtector(String phoneNumber, RealmModel userRealm) {
        this.phoneNumber = phoneNumber;
        this.userRealm = userRealm;
    }
}
