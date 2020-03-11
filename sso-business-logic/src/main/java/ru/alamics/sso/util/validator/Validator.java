package ru.alamics.sso.util.validator;

import javax.validation.ValidationException;

public interface Validator {
    void validate() throws ValidationException;
}
