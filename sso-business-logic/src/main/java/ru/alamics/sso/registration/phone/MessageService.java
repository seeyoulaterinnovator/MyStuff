package ru.alamics.sso.registration.phone;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.registration.phone.exception.SendMessageExceprion;
import ru.alamics.sso.registration.phone.model.MessageRequest;
import ru.alamics.sso.registration.phone.port.SendMessageService;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.UUID;

@Slf4j
@Stateless
public class MessageService {

    @EJB
    private SendMessageService smsSendService;

    public void sendMsg(MessageRequest messageRequest) throws SendMessageExceprion {
        String id = UUID.randomUUID().toString();

        String response = null;

        String phone = messageRequest.getUserPhone();
        String text = messageRequest.getText();
        String messengerName = messageRequest.getMessengerName().getType();

        try {
            response = smsSendService.sendSms(messageRequest);
        } finally {
            log.info("Sent {} to phone: {}, text: {}, id: {}, resp: {}", messengerName, phone, text, id, response);
        }
    }

}
