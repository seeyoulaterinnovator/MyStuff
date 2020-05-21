package ru.alamics.sso.util.validator;

import java.util.UUID;

public abstract class DmpIdValidator {

    public static void validate(String dmpId) throws NotValidException
    {
        try {
            UUID.fromString(dmpId);
        } catch (IllegalArgumentException e) {
            throw new NotValidException(String.format("DmpId не прошел валидацию : dmpId=\"%s\"", dmpId));
        }
    }
}
