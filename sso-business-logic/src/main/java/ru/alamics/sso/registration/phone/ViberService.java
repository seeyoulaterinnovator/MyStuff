package ru.alamics.sso.registration.phone;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.registration.phone.exception.ViberSendException;
import ru.alamics.sso.registration.phone.port.ViberSendService;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.UUID;

@Slf4j
@Stateless
public class ViberService {

    @EJB
    private ViberSendService viberSendService;

    public void sendMsg(String phone, String text, String realmId) throws ViberSendException {

        String id = UUID.randomUUID().toString();


        String resp = null;
        try {
            resp = viberSendService.sendMsg(phone, text, realmId);
        } finally {
            log.info("Sent msg to viber: {}, text: {}, id: {}, resp: {}", phone, text, id, resp);
        }
    }
}
