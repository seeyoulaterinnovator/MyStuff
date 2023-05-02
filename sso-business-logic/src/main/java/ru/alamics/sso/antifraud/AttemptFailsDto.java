package ru.alamics.sso.antifraud;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AttemptFailsDto {
    private String phone;
    private String code;
    private String realm;
    private String limitationCause;
}
