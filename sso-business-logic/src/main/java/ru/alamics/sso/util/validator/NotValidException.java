package ru.alamics.sso.util.validator;

public class NotValidException extends Exception {

    private EValidator validatorType = EValidator.UNDEFINED;

    public NotValidException(String message) {
        super(message);
    }

    public NotValidException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotValidException(Throwable cause) {
        super(cause);
    }

    public NotValidException(EValidator validatorType, String message) {
        super(message);
        this.validatorType = validatorType;
    }

    public NotValidException(EValidator validatorType, String message, Throwable cause) {
        super(message, cause);
        this.validatorType = validatorType;
    }

    public NotValidException(EValidator validatorType, Throwable cause) {
        super(cause);
        this.validatorType = validatorType;
    }
}
