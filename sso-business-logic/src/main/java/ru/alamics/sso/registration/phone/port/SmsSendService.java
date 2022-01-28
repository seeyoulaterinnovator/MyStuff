package ru.alamics.sso.registration.phone.port;

import ru.alamics.sso.registration.phone.exception.SmsSendException;

public interface SmsSendService extends MessageService {

    String sendMsg(String phone, String text) throws SmsSendException;

}
