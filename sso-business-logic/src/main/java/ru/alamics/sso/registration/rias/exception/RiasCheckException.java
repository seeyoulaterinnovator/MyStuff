package ru.alamics.sso.registration.rias.exception;

public class RiasCheckException extends RuntimeException {

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
