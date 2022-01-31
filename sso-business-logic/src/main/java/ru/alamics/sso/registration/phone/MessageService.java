package ru.alamics.sso.registration.phone;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.registration.phone.exception.SendMessageException;
import ru.alamics.sso.registration.phone.model.MessageRequest;
import ru.alamics.sso.registration.phone.port.SendMessageService;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import java.util.UUID;

@Slf4j
@Stateless
public class MessageService {

    @EJB
    private SendMessageService msgSendService;

    public void sendMsg(MessageRequest messageRequest) throws SendMessageException {
        String response = null;
        try {
            response = msgSendService.sendMsg(messageRequest);
        } finally {
            String id = UUID.randomUUID().toString();
            String phone = messageRequest.getUserPhone();
            String text = messageRequest.getText();
            String messengerName = messageRequest.getMessengerName().toString();
            log.info("Sent {} to phone: {}, text: {}, id: {}, resp: {}", messengerName, phone, text, id, response);
        }
    }

}
