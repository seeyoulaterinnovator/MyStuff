package ru.alamics.sso.registration.phone.exception;

public class SendMessageExceprion extends Exception {
    public SendMessageExceprion() {
    }

    public SendMessageExceprion(String message) {
        super(message);
    }

    public SendMessageExceprion(String message, Throwable cause) {
        super(message, cause);
    }

    public SendMessageExceprion(Throwable cause) {
        super(cause);
    }
}
