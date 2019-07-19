package ru.alamics.sso.registration.phone.model;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class Sms {

    private String id;

    private String userId;

    private String phone;

    private LocalDateTime sendTime;

    @Singular
    private List<SmsStatus> statuses;

}
