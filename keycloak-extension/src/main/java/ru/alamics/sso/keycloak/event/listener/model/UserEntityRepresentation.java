package ru.alamics.sso.keycloak.event.listener.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserEntityRepresentation {
    private String id;
    private String email;
    private boolean enabled;
    private Long createdTimestamp;
}
