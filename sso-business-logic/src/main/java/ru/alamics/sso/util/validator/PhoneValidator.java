package ru.alamics.sso.util.validator;

public abstract class PhoneValidator {

    public static void validate(String phone) throws NotValidException
    {
        if (phone == null || !phone.matches("[\\d]+") || !phone.startsWith("7") || phone.length() != 11) {
            throw new NotValidException(String.format("Phone не прошел валидацию: phone=%s", phone));
        }
    }
}
