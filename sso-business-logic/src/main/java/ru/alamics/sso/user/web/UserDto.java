package ru.alamics.sso.user.web;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {

    private String id;
    private String email;
    private String lastName;
    private String firstName;
    private Map<String, List<String>> attributes;
    private String username;
}
