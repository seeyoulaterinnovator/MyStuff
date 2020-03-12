package ru.alamics.sso.util.validator;

import javax.validation.ValidationException;

public abstract class PhoneValidator {

    public static void validate(String phone) {
        if (phone == null || !phone.matches("[\\d]+") || !phone.startsWith("7") || phone.length() != 11) {
            throw new ValidationException(String.format("Phone is not valid: phone=%s", phone));
        }
    }
}
