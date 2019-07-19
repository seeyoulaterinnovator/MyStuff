package ru.alamics.sso.registration.phone;

import lombok.extern.slf4j.Slf4j;
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

    public void sendSms(String userId, String phone, String text) {
        String id = UUID.randomUUID().toString();

        Sms sms = Sms.builder()
                .id(id)
                .phone(phone)
                .sendTime(LocalDateTime.now())
                .userId(userId)
                .build();

//        smsRepository.save(sms);

        smsSendService.sendSms(phone, text);

        log.info("Sent sms to phone: {}, text: {}, id: {}", phone, text, id);
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
