package ru.alamics.sso.registration.phone.port;

public interface SmsSendService {

    Integer sendSms(String phone, String text);

}
