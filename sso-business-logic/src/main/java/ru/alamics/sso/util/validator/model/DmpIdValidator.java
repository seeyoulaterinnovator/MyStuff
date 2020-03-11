package ru.alamics.sso.util.validator.model;

import lombok.AllArgsConstructor;
import ru.alamics.sso.util.validator.Validator;

import javax.validation.ValidationException;
import java.util.UUID;

@AllArgsConstructor
public class DmpIdValidator implements Validator {

    private String dmpId;

    @Override
    public void validate() {
        try {
            UUID.fromString(dmpId);
        } catch (IllegalArgumentException e) {
            throw new ValidationException(String.format("DmpId is not valid : dmpId=\"%s\"", dmpId));
        }
    }
}
