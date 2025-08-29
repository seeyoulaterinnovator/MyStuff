package ru.alamics.sso.remote.message;

import ru.alamics.sso.registration.phone.exception.SendMessageException;
import ru.alamics.sso.registration.phone.model.MessageRequest;

public interface Sender {
    public String send(MessageRequest request) throws SendMessageException;
}
