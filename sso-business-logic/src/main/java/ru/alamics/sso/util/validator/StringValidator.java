package ru.alamics.sso.util.validator;

import java.util.ArrayList;
import java.util.List;

public class StringValidator {

    public static void process(ValidatorBuilder builder) throws AllNotValidException
    {

        List<String> result = new ArrayList<>();

        try {
            if (builder.getDmpValue() != null)
                DmpIdValidator.validate(builder.getDmpValue());
        } catch (NotValidException e) {
            result.add(e.getMessage());
        }

        try {
            if (builder.getTomsValue() != null)
                TomsIdValidator.validate(builder.getTomsValue());
        } catch (NotValidException e) {
            result.add(e.getMessage());
        }

        try {
            if (builder.getEmailValue() != null)
                EmailValidator.validate(builder.getEmailValue());
        } catch (NotValidException e) {
            result.add(e.getMessage());
        }

        try {
            if (builder.getPhoneValue() != null)
                PhoneValidator.validate(builder.getPhoneValue());
        } catch (NotValidException e) {
            result.add(e.getMessage());
        }

        if (!result.isEmpty())
            throw new AllNotValidException(result);
    }
}
