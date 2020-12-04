package ru.alamics.sso.registration.model;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import ru.alamics.sso.registration.phone.ActivationCodeType;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Map;

@Data
@Builder
public class AuthContext {

    private ActivationCodeType activationCodeType;
    private LocalDateTime expirationTime;
    private Integer counter;
    private String hashProperty;
}
