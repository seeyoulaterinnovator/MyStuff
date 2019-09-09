package ru.alamics.sso.registration.dto;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.*;

import java.util.Set;

@Data
@NoArgsConstructor
public class UserPostRequest {
    private String id;
    private String userId;
    private String tomsId;
    private String dmpId;
    private Long roleId;
}
