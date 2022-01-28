package ru.alamics.sso.registration.phone.port;

import ru.alamics.sso.registration.phone.exception.SendMessageExceprion;
import ru.alamics.sso.registration.phone.model.MessageRequest;

public interface SendMessageService {

    String sendSms(MessageRequest messageRequest) throws SendMessageExceprion;

}
