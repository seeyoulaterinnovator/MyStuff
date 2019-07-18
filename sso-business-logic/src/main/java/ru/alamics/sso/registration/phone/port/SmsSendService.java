package ru.alamics.sso.registration.phone.port;

public interface SmsSendService {

    Integer sensSms(String phone, String text);

}
