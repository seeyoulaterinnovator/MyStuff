package ru.alamics.sso.registration.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExternalSystemRoleRequest {
    @NotNull
    private String userPostId;
    @NotNull
    private Long systemRoleId;
}
