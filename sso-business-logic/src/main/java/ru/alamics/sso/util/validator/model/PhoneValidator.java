package ru.alamics.sso.util.validator.model;

import lombok.AllArgsConstructor;
import ru.alamics.sso.util.validator.Validator;

import javax.validation.ValidationException;

@AllArgsConstructor
public class PhoneValidator implements Validator {

    private String phone;

    @Override
    public void validate() {
        if (phone == null || !phone.matches("[\\d]+") || !phone.startsWith("7") || phone.length() != 11) {
            throw new ValidationException(String.format("Phone is not valid: phone=%s", phone));
        }
    }
}
