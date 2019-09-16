package ru.alamics.sso.registration.phone.port;

import ru.alamics.sso.registration.phone.exception.SmsSendException;

public interface SmsSendService {

    String sendSms(String phone, String text) throws SmsSendException;

}
