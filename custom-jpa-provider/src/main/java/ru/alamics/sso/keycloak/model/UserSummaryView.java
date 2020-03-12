package ru.alamics.sso.keycloak.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryView {
    private String id;

    private String username;

    private String firstName;

    private String lastName;

    private String email;

    private String phone;

    private boolean enabled;
    
}
