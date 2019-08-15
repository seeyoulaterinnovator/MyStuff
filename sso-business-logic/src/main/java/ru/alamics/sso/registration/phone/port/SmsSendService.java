package ru.alamics.sso.registration.phone.port;

public interface SmsSendService {

    String sendSms(String phone, String text);

}
