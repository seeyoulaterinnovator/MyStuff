package ru.alamics.sso.registration.tbapi.exception;

public class TbapiRegisterException extends Exception {

    public TbapiRegisterException() {
    }

    public TbapiRegisterException(String message) {
        super(message);
    }

    public TbapiRegisterException(String message, Throwable cause) {
        super(message, cause);
    }

    public TbapiRegisterException(Throwable cause) {
        super(cause);
    }
}
