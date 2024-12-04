package ru.alamics.sso.registration.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserPostEditRequest {
    @NotNull
    private String id;
    @NotNull
    private Long roleId;
}
