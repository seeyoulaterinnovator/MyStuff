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
    private String fName;
    private String lName;
    private String email;
    private String phone;
    private String accessName;
    private String accessId;
    private String tomsId;
    private String roleId;
    private String roleName;

    @JsonPOJOBuilder(withPrefix = "")
    public static class UserDtoBuilder {
    }
}
