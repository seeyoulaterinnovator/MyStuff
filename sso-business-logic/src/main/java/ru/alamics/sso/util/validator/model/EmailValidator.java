package ru.alamics.sso.util.validator.model;

import lombok.AllArgsConstructor;
import ru.alamics.sso.util.validator.Validator;

import javax.validation.ValidationException;

@AllArgsConstructor
public class EmailValidator implements Validator {

    private String email;

    @Override
    public void validate() {
        if (email == null || !email.contains("@") || !email.substring(0, 1).matches("([\\w[\\s]])+")
                || email.substring(0, 1).matches("[\\d]+") || email.contains(" ") ||
                !email.substring(email.indexOf("@") + 1, email.indexOf("@") + 2).matches("([\\w[\\s]])+")) {
            throw new ValidationException(String.format("Email is not valid: email=%s", email));
        }
    }
}
