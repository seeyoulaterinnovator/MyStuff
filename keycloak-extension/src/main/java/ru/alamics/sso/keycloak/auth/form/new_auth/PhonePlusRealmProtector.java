package ru.alamics.sso.keycloak.auth.form.new_auth;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.keycloak.models.RealmModel;

@Data
@EqualsAndHashCode
public class PhonePlusRealmProtector {

    private String phoneNumber;

    private RealmModel userRealm;

    public PhonePlusRealmProtector(String phoneNumber, RealmModel userRealm) {
        this.phoneNumber = phoneNumber;
        this.userRealm = userRealm;
    }
}
