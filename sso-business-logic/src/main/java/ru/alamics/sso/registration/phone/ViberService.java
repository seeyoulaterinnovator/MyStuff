package ru.alamics.sso.registration.phone;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.registration.phone.port.ViberSendService;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Stateless
public class ViberService {

    @EJB
    private ViberSendService viberSendService;

    public void sendMsg(String userId, String phone, String text) {

        String id = UUID.randomUUID().toString();

        /*
        Sms sms = Sms.builder()
                .id(id)
                .phone(phone)
                .sendTime(LocalDateTime.now())
                .userId(userId)
                .build();
        */

        String resp = viberSendService.sendMsg(phone, text);

        log.info("Sent msg to viber: {}, text: {}, id: {}, resp: {}", phone, text, id, resp);
    }
}
