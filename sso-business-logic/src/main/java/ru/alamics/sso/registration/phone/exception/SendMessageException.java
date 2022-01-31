package ru.alamics.sso.registration.phone.exception;

public class SendMessageException extends Exception {
    public SendMessageException() {
    }

    public SendMessageException(String message) {
        super(message);
    }

    public SendMessageException(String message, Throwable cause) {
        super(message, cause);
    }

    public SendMessageException(Throwable cause) {
        super(cause);
    }
}
