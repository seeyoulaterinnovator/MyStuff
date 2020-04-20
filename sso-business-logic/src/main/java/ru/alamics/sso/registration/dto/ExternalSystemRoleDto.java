package ru.alamics.sso.registration.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Data;

@Data
@JsonDeserialize(builder = ExternalSystemRoleDto.ExternalSystemRoleDtoBuilder.class)
public class ExternalSystemRoleDto {
    private Long id;
    private String name;
    private ExternalSystemDto externalSystem;

    // нужен для сериализации Infinspan
    // Builder'у сделан delombok тк он не создает оба конструктора
    public ExternalSystemRoleDto() {
    }

    @java.beans.ConstructorProperties({"id", "name", "externalSystem"})
    ExternalSystemRoleDto(Long id, String name, ExternalSystemDto externalSystem) {
        this.id = id;
        this.name = name;
        this.externalSystem = externalSystem;
    }

    public static ExternalSystemRoleDtoBuilder builder() {
        return new ExternalSystemRoleDtoBuilder();
    }

    public ExternalSystemRoleDtoBuilder toBuilder() {
        return new ExternalSystemRoleDtoBuilder().id(this.id).name(this.name).externalSystem(this.externalSystem);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class ExternalSystemRoleDtoBuilder {
        private Long id;
        private String name;
        private ExternalSystemDto externalSystem;

        ExternalSystemRoleDtoBuilder() {
        }

        public ExternalSystemRoleDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ExternalSystemRoleDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ExternalSystemRoleDtoBuilder externalSystem(ExternalSystemDto externalSystem) {
            this.externalSystem = externalSystem;
            return this;
        }

        public ExternalSystemRoleDto build() {
            return new ExternalSystemRoleDto(id, name, externalSystem);
        }

        public String toString() {
            return "ExternalSystemRoleDto.ExternalSystemRoleDtoBuilder(id=" + this.id + ", name=" + this.name + ", externalSystem=" + this.externalSystem + ")";
        }
    }
}
