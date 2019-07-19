package ru.alamics.sso.registration.phone.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SmsStatus {

    private String id;

    private SmsDeliveryStatus status;

    private LocalDateTime updated;

    private Sms sms;

}
