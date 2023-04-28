package ru.alamics.sso.antifraud;

import lombok.*;
import org.keycloak.models.jpa.entities.UserEntity;

import java.time.LocalDateTime;
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class BlackListDto {
    private String email;
    private LocalDateTime createdAt;
    private LocalDateTime unblockedAt;
    private Long blockDurationSec;
    private String phone;
    private String limitationCause;
    private String realm;
    private UserEntity user;
}
