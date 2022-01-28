package ru.alamics.sso.registration.phone.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageRequest {

    private String userPhone;

    private String text;

    private String realmId;

    private MessengerType messengerName;

}
