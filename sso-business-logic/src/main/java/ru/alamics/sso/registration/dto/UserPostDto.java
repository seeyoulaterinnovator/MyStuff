package ru.alamics.sso.registration.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserPostDto {
    private String id;
    private String userId;
    private String tomsId;
    private String rmsId;
    private Long roleId;
}
