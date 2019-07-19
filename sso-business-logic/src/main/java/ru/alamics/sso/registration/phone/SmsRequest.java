package ru.alamics.sso.registration.phone;

import lombok.Data;

@Data
public class SmsRequest {

    private String targetPhone;

    private String text;

}
