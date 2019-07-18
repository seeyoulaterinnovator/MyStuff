package ru.alamics.sso.registration.phone.port;

import ru.alamics.sso.registration.phone.model.SmsStatus;

import java.util.List;
import java.util.Optional;

public interface SmsStatusRepository {

    Optional<SmsStatus> findById(String id);

    List<SmsStatus> findBySmsId(String smsId);

    SmsStatus save(SmsStatus smsStatus);

}
