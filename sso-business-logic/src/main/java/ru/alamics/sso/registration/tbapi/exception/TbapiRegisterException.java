package ru.alamics.sso.registration.tbapi.exception;

public class TbapiRegisterException extends Exception {

    private static final long serialVersionUID = 2420566597191052941L;

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
