package ru.alamics.sso.keycloak.auth.form.new_auth;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
@Data
public class PhoneHashAndBanStatusKeeper {
    private boolean isSendingBanned;
    private String savedCodeHash;

    public PhoneHashAndBanStatusKeeper(boolean isSendingAllowed, String savedCodeHash) {
        this.isSendingBanned = isSendingAllowed;
        this.savedCodeHash = savedCodeHash;
    }
}
