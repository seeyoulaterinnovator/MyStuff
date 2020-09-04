package ru.alamics.sso.util.validator;

import java.util.List;

public class AllNotValidException extends Exception {

    private List<String> messageList;

    public AllNotValidException(List<String> messageList) {
        super(messageList.toString());
        this.messageList = messageList;
    }

    public AllNotValidException(List<String> messageList, Throwable cause) {
        super(messageList.toString(), cause);
        this.messageList = messageList;
    }

    public List<String> getMessageList() {
        return messageList;
    }
}
