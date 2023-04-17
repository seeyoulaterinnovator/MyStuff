package ru.alamics.sso.keycloak.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.alamics.sso.registration.phone.ActivationCodeType;

@EqualsAndHashCode
@Data
@AllArgsConstructor
public class VerifyPhoneKey {
    private ActivationCodeType activationType;
    private String currentCode;
    private Integer currentCodeCounter;
}
