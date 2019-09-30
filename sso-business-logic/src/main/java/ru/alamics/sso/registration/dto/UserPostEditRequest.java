package ru.alamics.sso.registration.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
public class UserPostEditRequest {
    @NotNull
    private String id;
    @NotNull
    private Long roleId;
}
