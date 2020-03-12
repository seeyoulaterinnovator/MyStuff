package ru.alamics.sso.util.validator;

import javax.validation.ValidationException;

public abstract class TomsIdValidator {

    public static void validate(String tomsId) {
        if (tomsId == null || tomsId.isBlank() || !tomsId.matches("[0-9]+")) {
            throw new ValidationException(String.format("TomsIds is not valid : tomsId=\"%s\"", tomsId));
        }
    }
}
