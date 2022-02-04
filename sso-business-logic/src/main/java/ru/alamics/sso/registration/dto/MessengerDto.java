package ru.alamics.sso.registration.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Data;

import java.util.UUID;

@Data
@JsonDeserialize(builder = MessengerDto.MessengerDtoBuilder.class)
public class MessengerDto {
    private UUID id;
    private String name;
    private String label;

    public MessengerDto() {
    }

    @java.beans.ConstructorProperties({"id", "name", "label"})
    MessengerDto(UUID id, String name, String label) {
        this.id = id;
        this.name = name;
        this.label = label;
    }

    public static MessengerDto.MessengerDtoBuilder builder() {
        return new MessengerDto.MessengerDtoBuilder();
    }

    public MessengerDto.MessengerDtoBuilder toBuilder() {
        return new MessengerDto.MessengerDtoBuilder().id(this.id).name(this.name).label(this.label);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class MessengerDtoBuilder {
        private UUID id;
        private String name;
        private String label;

        MessengerDtoBuilder() {
        }

        public MessengerDto.MessengerDtoBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public MessengerDto.MessengerDtoBuilder name(String name) {
            this.name = name;
            return this;
        }

        public MessengerDto.MessengerDtoBuilder label(String label) {
            this.label = label;
            return this;
        }

        public MessengerDto build() {
            return new MessengerDto(id, name, label);
        }

        public String toString() {
            return "ExternalSystemDto.ExternalSystemDtoBuilder(id=" + this.id + ", name=" + this.name + ", label=" + this.label + ")";
        }
    }
}
