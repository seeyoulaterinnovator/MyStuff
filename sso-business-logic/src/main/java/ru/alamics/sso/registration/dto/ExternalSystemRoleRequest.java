package ru.alamics.sso.registration.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ExternalSystemRoleRequest {
    private String userPostId;
    private Long systemRoleId;
}
