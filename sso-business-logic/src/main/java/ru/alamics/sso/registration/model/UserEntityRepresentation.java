package ru.alamics.sso.registration.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserEntityRepresentation {
    public static String SEND_LOGIN_AND_RESET_PASSWORD = "SEND_LOGIN_AND_RESET_PASSWORD";
    public static String SEND_LOGIN = "SEND_LOGIN";
    public static String DELETE_PASSWORD = "DELETE_PASSWORD";

    private String id;
    private String email;
    private boolean enabled;
    private Long createdTimestamp;
    private List<String> requiredActions;
}
