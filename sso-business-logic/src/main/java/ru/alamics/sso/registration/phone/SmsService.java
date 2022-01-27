package ru.alamics.sso.registration.phone;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.registration.phone.exception.SmsSendException;
import ru.alamics.sso.registration.phone.model.SmsDeliveryStatus;
import ru.alamics.sso.registration.phone.port.SmsSendService;

import javax.ejb.EJB;
import javax.ejb.Stateless;
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

    public void sendSms(String phone, String text, String realmId) throws SmsSendException {
        String id = UUID.randomUUID().toString();


        String response = null;

        try {
            response = smsSendService.sendSms(phone, text, realmId);
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
