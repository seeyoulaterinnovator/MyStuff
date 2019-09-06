package ru.alamics.sso.registration.phone.port;

import ru.alamics.sso.registration.phone.exception.PhoneCallException;

public interface PhoneCallerRemoteService {

    String call(String phone, int count) throws PhoneCallException;
}
