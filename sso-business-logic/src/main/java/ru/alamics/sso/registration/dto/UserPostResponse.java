package ru.alamics.sso.registration.dto;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;

import java.util.List;

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
