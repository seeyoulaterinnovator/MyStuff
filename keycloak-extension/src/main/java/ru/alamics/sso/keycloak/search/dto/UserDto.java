package ru.alamics.sso.keycloak.search.dto;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder(builderClassName = "UserDtoBuilder", toBuilder = true)
public class UserDto implements Serializable {

    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Boolean enabled;
    private String userPostId;
    private String tomsId;
    private String organization;
    private String roleId;
    private String roleName;
    private String systemRoleId;
    private String systemRoleName;
    private String systemId;
    private String systemName;
    private String systemLabel;

    @JsonPOJOBuilder(withPrefix = "")
    public static class UserDtoBuilder {
    }
}
