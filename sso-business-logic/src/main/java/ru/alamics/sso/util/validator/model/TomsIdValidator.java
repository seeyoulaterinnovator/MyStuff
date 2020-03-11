package ru.alamics.sso.util.validator.model;

import lombok.AllArgsConstructor;
import ru.alamics.sso.util.validator.Validator;

import javax.validation.ValidationException;

@AllArgsConstructor
public class TomsIdValidator implements Validator {

    private String tomsId;

    @Override
    public void validate() {
        if (tomsId == null || tomsId.isBlank() || !tomsId.matches("[0-9]+")) {
            throw new ValidationException(String.format("TomsIds is not valid : tomsId=\"%s\"", tomsId));
        }
    }
}
