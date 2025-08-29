package ru.alamics.sso.remote.message;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.registration.phone.exception.SendMessageException;
import ru.alamics.sso.registration.phone.model.MessageRequest;
import ru.alamics.sso.registration.phone.model.MessengerType;
import ru.alamics.sso.registration.phone.port.SendMessageService;
import java.util.UUID;

import static ru.alamics.sso.registration.phone.model.MessengerType.SMS;

@ApplicationScoped
@Named("MessageSender")
@Slf4j
public class SendMessageServiceImpl implements SendMessageService {
    @Inject
    SmsMessageSender smsMessageSender;

    public String sendMessageByRequestAndLogInfo(MessageRequest messageRequest) throws SendMessageException {
        String response = sendMessageByRequest(messageRequest);
        String id = UUID.randomUUID().toString();
        String phone = messageRequest.getUserPhone();
        String text = messageRequest.getText();
        String messengerName = messageRequest.getMessengerName().toString();

        log.info("Sent {} to phone: {}, text: {}, id: {}, resp: {}", messengerName, phone, text, id, response);
        return response;
    }

    @Override
    public void sendMessageToMessengers(String phone, String message, String realmId, String[] messengerList, String host) throws SendMessageException {
//        без дополнительных данных в смс("Your OTP is..." не срабатывает автоподстановка кода из смс)
//        String pattern = String.format("Your OTP is: %s.\n\n" + "@%s #%s", message, host, message);

        String pattern = message;

        MessageRequest messageRequest = MessageRequest.builder()
                .userPhone(phone)
                .text(pattern)
                .realmId(realmId)
                .build();

        for (String messenger : messengerList) {
            messageRequest.setMessengerName(MessengerType.valueOf(messenger));
            sendMessageByRequestAndLogInfo(messageRequest);
        }
    }

    @Override
    public String sendMessageByRequest(MessageRequest messageRequest) throws SendMessageException {
        MessengerType messenger = messageRequest.getMessengerName();

        switch (messenger) {
            case SMS: return getSmsSender().send(messageRequest);
            default: throw new SendMessageException("Messenger is not supported");
        }
    }

    protected SmsMessageSender getSmsSender() {
        return smsMessageSender;
    }
}
