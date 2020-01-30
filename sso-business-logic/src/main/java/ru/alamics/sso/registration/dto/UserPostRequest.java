package ru.alamics.sso.registration.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

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
}
