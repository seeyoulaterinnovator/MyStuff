package ru.alamics.sso.registration.phone.exception;

public class ViberSendException extends Exception {

    public ViberSendException() {
    }

    public ViberSendException(String message) {
        super(message);
    }

    public ViberSendException(String message, Throwable cause) {
        super(message, cause);
    }

    public ViberSendException(Throwable cause) {
        super(cause);
    }
}
