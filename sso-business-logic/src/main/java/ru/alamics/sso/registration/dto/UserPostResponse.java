package ru.alamics.sso.registration.dto;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.alamics.sso.keycloak.entity.UserPostRole;

import java.util.List;
import java.util.Set;

@Data
@Builder(builderClassName = "UserPostResponseBuilder", toBuilder = true)
public class UserPostResponse {
    private String id;
    private String userId;
    private String tomsId;
    private String dmpId;
    private UserPostRoleDto userRole;
    private List<ExternalSystemRoleDto> systemRoles;

    @JsonPOJOBuilder(withPrefix = "")
    public static class UserPostResponseBuilder {
    }
}
