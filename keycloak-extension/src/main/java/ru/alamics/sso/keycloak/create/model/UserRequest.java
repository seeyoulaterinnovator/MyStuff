package ru.alamics.sso.keycloak.create.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class UserRequest implements Serializable {

    private String realmName;
    private String email;
    private String phone;
    private String name;
    private String UUID; // #DMP ID
    private String CAID; // #Customer Account number
}