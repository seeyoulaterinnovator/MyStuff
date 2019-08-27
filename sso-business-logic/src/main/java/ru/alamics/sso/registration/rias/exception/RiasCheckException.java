package ru.alamics.sso.registration.rias.exception;

public class RiasCheckException extends Exception {

    public RiasCheckException() {
    }

    public RiasCheckException(String message) {
        super(message);
    }

    public RiasCheckException(String message, Throwable cause) {
        super(message, cause);
    }

    public RiasCheckException(Throwable cause) {
        super(cause);
    }
}
