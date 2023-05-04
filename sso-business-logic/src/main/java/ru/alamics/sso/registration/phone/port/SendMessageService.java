package ru.alamics.sso.registration.phone.port;

import ru.alamics.sso.registration.phone.exception.SendMessageException;
import ru.alamics.sso.registration.phone.model.MessageRequest;

import javax.ejb.Remote;
import java.util.List;

public interface SendMessageService {
    String sendMessageByRequestAndLogInfo(MessageRequest messageRequest) throws SendMessageException;
    String sendMessageByRequest(MessageRequest messageRequest) throws SendMessageException;
    /**
     *
     * @param phone Телефон
     * @param message Сообщение, например, код для верификации
     * @param realmId Реалм
     * @param messengerList Список месенджеров. "Отправка по СМС" - это тоже мессенджер
     * @throws SendMessageException
     */
    void sendMessageToMessengers(String phone, String message, String realmId, String[] messengerList) throws SendMessageException;
}
