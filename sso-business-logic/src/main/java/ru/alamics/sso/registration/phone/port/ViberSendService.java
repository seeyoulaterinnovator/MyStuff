package ru.alamics.sso.registration.phone.port;

public interface ViberSendService {

    String sendMsg(String phone, String text);
}
