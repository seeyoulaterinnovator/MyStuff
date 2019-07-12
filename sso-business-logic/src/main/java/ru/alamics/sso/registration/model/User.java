package ru.alamics.sso.registration.model;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class User {

    private String id;

    private String email;

    private String firstName;

    private String lastName;

    @Builder.Default
    private Map<String, List<String>> attributes = new HashMap<>();

}
