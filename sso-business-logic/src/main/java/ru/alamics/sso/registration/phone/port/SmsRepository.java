package ru.alamics.sso.registration.phone.port;

import ru.alamics.sso.registration.phone.model.Sms;

import java.util.Optional;

public interface SmsRepository {

    Optional<Sms> findById(String id);

    Sms save(Sms sms);

}
