package ru.alamics.sso.antifraud;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AttemptFailsDto {
    private String phone;
    private String code;
    private String realm;
    private String limitationCause;
    private LocalDateTime created;
}
