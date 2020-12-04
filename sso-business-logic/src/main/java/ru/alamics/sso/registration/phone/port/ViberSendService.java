package ru.alamics.sso.registration.phone.port;

import ru.alamics.sso.registration.phone.exception.ViberSendException;

public interface ViberSendService {

    String sendMsg(String phone, String text) throws ViberSendException;
}
