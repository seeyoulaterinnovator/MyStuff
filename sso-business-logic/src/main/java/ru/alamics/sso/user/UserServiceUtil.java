package ru.alamics.sso.user;

import ru.alamics.sso.user.model.UserParameter;
import ru.alamics.sso.user.web.UserSearchDto;

import java.util.LinkedList;
import java.util.List;

public class UserServiceUtil {

    public static List<String> getUserParameterNames(UserParameter[] userParameters) {

        List<String> names = new LinkedList<>();
        for (UserParameter userParameter : userParameters) {
            if (userParameter == null)
                continue;
            names.add(userParameter.getDesc());
        }
        return names;
    }

    public static List<String> getUserParameters(UserSearchDto userDto, UserParameter[] userParameters) {
        List<String> parameters = new LinkedList<>();
        for (UserParameter userParameter : userParameters) {
            if (userParameter == null)
                continue;

            switch (userParameter) {
                case EMAIL:
                    parameters.add(userDto.getEmail());
                    break;
                case PHONE:
                    parameters.add(userDto.getPhone());
                    break;
                case ROLE:
                    parameters.add(userDto.getRoleName());
                    break;
                case SYSTEM:
                    parameters.add(userDto.getSystemName());
                    break;
                case USER_ID:
                    parameters.add(userDto.getId());
                    break;
                case FIRST_NAME:
                    parameters.add(userDto.getFirstName());
                    break;
                case ENABLED:
                    parameters.add(userDto.getEnabled().toString());
                    break;
                case TOMS_ID:
                    parameters.add(userDto.getTomsId());
                    break;
                case DMP_ID:
                    parameters.add(userDto.getDmpId());
                    break;
                default:
                    parameters.add("");
            }
        }
        return parameters;
    }

    public static String doCleanMail(String mail) {

        if (mail == null) return null;

        return mail.toLowerCase().trim();
    }

    public static String doCleanPhone(String phone) {

        if (phone == null) return null;

        return phone.toLowerCase().trim();
    }
}
