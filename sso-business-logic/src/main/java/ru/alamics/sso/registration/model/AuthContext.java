package ru.alamics.sso.registration.model;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.Map;

@Data
@Builder
public class AuthContext {

    @Singular
    private Map<String, String> properties;

}
