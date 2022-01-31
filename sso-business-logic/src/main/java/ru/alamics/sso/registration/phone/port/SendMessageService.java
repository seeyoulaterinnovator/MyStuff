package ru.alamics.sso.registration.phone.port;

import ru.alamics.sso.registration.phone.exception.SendMessageException;
import ru.alamics.sso.registration.phone.model.MessageRequest;

public interface SendMessageService {

    String sendMsg(MessageRequest messageRequest) throws SendMessageException;

}
