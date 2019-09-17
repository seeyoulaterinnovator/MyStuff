package ru.alamics.sso.registration.phone.exception;

public class SmsSendException extends Exception {
    public SmsSendException() {
    }

    public SmsSendException(String message) {
        super(message);
    }

    public SmsSendException(String message, Throwable cause) {
        super(message, cause);
    }

    public SmsSendException(Throwable cause) {
        super(cause);
    }
}
