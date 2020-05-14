package ru.alamics.sso.util.validator;

import javax.validation.ValidationException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class EmailValidator {

    private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static void validate(String emailStr) {

        if (emailStr == null)
            throw new ValidationException("Email is null");

        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);

        if (!matcher.find()) {
            throw new ValidationException(String.format("Email is not valid: email=%s", emailStr));
        }
    }
}
