package ru.alamics.sso.registration.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;

import java.util.Set;

@Data
@JsonDeserialize(builder = ExternalSystemDto.ExternalSystemDtoBuilder.class)
@Builder(builderClassName = "ExternalSystemDtoBuilder", toBuilder = true)
public class ExternalSystemDto {
    private Long id;
    private String name;
    private String label;
    // падает сериализация. для кластера
    //private Set<ExternalSystemRoleDto> systemRoles;

    @JsonPOJOBuilder(withPrefix = "")
    public static class ExternalSystemDtoBuilder {
    }
}
