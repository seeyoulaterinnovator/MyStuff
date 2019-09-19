package ru.alamics.sso.registration.dto;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;

@Data
@Builder(builderClassName = "ExternalSystemRoleDtoBuilder", toBuilder = true)
public class ExternalSystemRoleDto {
    private Long id;
    private String name;
    private ExternalSystemDto externalSystem;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ExternalSystemRoleDtoBuilder {
    }
}
