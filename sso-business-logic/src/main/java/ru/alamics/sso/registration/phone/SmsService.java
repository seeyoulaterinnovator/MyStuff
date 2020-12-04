package ru.alamics.sso.registration.phone;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.registration.phone.exception.SmsSendException;
import ru.alamics.sso.registration.phone.model.Sms;
import ru.alamics.sso.registration.phone.model.SmsDeliveryStatus;
import ru.alamics.sso.registration.phone.model.SmsStatus;
import ru.alamics.sso.registration.phone.port.SmsRepository;
import ru.alamics.sso.registration.phone.port.SmsSendService;
import ru.alamics.sso.registration.phone.port.SmsStatusRepository;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Stateless
public class SmsService {

//    @EJB
//    private SmsRepository smsRepository;
//
//    @EJB
//    private SmsStatusRepository smsStatusRepository;

    @EJB
    private SmsSendService smsSendService;

    public void sendSms(String userId, String phone, String text) throws SmsSendException {
        String id = UUID.randomUUID().toString();

        /*
        Sms sms = Sms.builder()
                .id(id)
                .phone(phone)
                .sendTime(LocalDateTime.now())
                .userId(userId)
                .build();
        */

//        smsRepository.save(sms);

        String response = null;

        try {
            response = smsSendService.sendSms(phone, text);
        } finally {
            log.info("Sent sms to phone: {}, text: {}, id: {}, resp: {}", phone, text, id, response);
        }
    }

    public void saveSmsStatusUpdate(String smsId, SmsDeliveryStatus status) {

//        Sms sms = smsRepository.findById(smsId).orElseThrow();
//
//        String id = UUID.randomUUID().toString();
//
//        SmsStatus smsStatus = SmsStatus.builder()
//                .id(id)
//                .status(status)
//                .sms(sms)
//                .updated(LocalDateTime.now())
//                .build();
//
//        smsStatusRepository.save(smsStatus);
    }

}
