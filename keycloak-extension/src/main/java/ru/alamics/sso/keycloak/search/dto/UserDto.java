package ru.alamics.sso.keycloak.search.dto;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder(builderClassName = "UserDtoBuilder", toBuilder = true)
public class UserDto implements Serializable {
    private static final long serialVersionUID = -5333378808486130178L;

    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String userPostName;
    private String userPostId;
    private String tomsId;
    private String roleId;
    private String roleName;
    private String clientRoleId;
    private String clientRoleName;
    private String customerName;

    @JsonPOJOBuilder(withPrefix = "")
    public static class UserDtoBuilder {
    }
}
