package ru.alamics.sso.registration.phone.exception;

public class PhoneCallException extends Exception {
    public PhoneCallException(String message) {
        super(message);
    }

    public PhoneCallException(String message, Throwable cause) {
        super(message, cause);
    }
}
