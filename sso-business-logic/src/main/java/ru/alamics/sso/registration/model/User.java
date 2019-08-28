package ru.alamics.sso.registration.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// https://www.thecuriousdev.org/lombok-builder-with-jackson/

@Data
@JsonDeserialize(builder = User.UserBuilder.class)
@Builder(builderClassName = "UserBuilder", toBuilder = true)
public class User {

    private final String id;

    private final String email;

    private final String name;

    private final String phone;

    private LocalDateTime phoneVerifiedOn;

    @Builder.Default
    private final Map<String, List<String>> attributes = new HashMap<>();

    @JsonPOJOBuilder(withPrefix = "")
    public static class UserBuilder {
    }
}
