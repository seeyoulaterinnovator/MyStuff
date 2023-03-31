package ru.alamics.sso.registration.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Data;

import java.io.Serializable;

@Data
@JsonDeserialize(builder = ExternalSystemDto.ExternalSystemDtoBuilder.class)
public class ExternalSystemDto implements Serializable {
    private Long id;
    private String name;
    private String label;
    private String realmId;
    // падает сериализация. для кластера
    //private Set<ExternalSystemRoleDto> systemRoles;

    // нужен для сериализации Infinspan
    // Builder'у сделан delombok тк он не создает оба конструктора
    public ExternalSystemDto() {
    }

    @java.beans.ConstructorProperties({"id", "name", "label", "realmId"})
    ExternalSystemDto(Long id, String name, String label, String realmId) {
        this.id = id;
        this.name = name;
        this.label = label;
        this.realmId = realmId;
    }

    public static ExternalSystemDtoBuilder builder() {
        return new ExternalSystemDtoBuilder();
    }

    public ExternalSystemDtoBuilder toBuilder() {
        return new ExternalSystemDtoBuilder().id(this.id).name(this.name).label(this.label).realmId(this.realmId);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class ExternalSystemDtoBuilder {
        private Long id;
        private String name;
        private String label;
        private String realmId;

        ExternalSystemDtoBuilder() {
        }

        public ExternalSystemDtoBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public ExternalSystemDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public ExternalSystemDtoBuilder label(String label) {
            this.label = label;
            return this;
        }

        public ExternalSystemDtoBuilder realmId(String realmId) {
            this.realmId = realmId;
            return this;
        }

        public ExternalSystemDto build() {
            return new ExternalSystemDto(id, name, label, realmId);
        }

        public String toString() {
            return "ExternalSystemDto.ExternalSystemDtoBuilder(id=" + this.id + ", name=" + this.name + ", label=" + this.label + ", realmId=" + this.realmId + ")";
        }
    }
}
