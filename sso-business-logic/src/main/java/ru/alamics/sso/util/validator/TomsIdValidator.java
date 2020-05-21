package ru.alamics.sso.util.validator;

public abstract class TomsIdValidator {

    public static void validate(String tomsId) throws NotValidException
    {
        if (tomsId == null || tomsId.isEmpty() || !tomsId.matches("[0-9]+")) {
            throw new NotValidException(String.format("TomsId не прошел валидацию: tomsId=\"%s\"", tomsId));
        }
    }
}
