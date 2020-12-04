package ru.alamics.sso.keycloak.policy.rest;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
public class PasswordPolicyDto implements Serializable {
    private static final long serialVersionUID = 281902644058245931L;

    private String key;
    private Object value;
}
