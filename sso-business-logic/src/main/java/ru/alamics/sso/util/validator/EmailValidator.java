package ru.alamics.sso.util.validator;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class EmailValidator {

    private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static void validate(String emailStr) throws NotValidException
    {
        if (emailStr == null)
            throw new NotValidException("Email пуст");

        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);

        if (!matcher.find()) {
            throw new NotValidException(EValidator.EMAIL, String.format("Email не прошел валидацию: email=%s", emailStr));
        }
    }
}
