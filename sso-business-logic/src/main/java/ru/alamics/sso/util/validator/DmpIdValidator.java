package ru.alamics.sso.util.validator;

import javax.validation.ValidationException;
import java.util.UUID;

public abstract class DmpIdValidator {

    public static void validate(String dmpId) {
        try {
            UUID.fromString(dmpId);
        } catch (IllegalArgumentException e) {
            throw new ValidationException(String.format("DmpId is not valid : dmpId=\"%s\"", dmpId));
        }
    }
}
