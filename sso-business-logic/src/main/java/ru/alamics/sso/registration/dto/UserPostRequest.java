package ru.alamics.sso.registration.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserPostRequest {
    @NotNull
    private String userId;
    @NotNull
    private String tomsId;
    private String orgName;
    private String dmpId;
    @NotNull
    private Long roleId;

    private boolean selected;
}
