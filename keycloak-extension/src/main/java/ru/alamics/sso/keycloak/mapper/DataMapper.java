package ru.alamics.sso.keycloak.mapper;

import org.keycloak.authentication.FormContext;
import org.keycloak.models.UserModel;
import ru.alamics.sso.keycloak.create.model.UserImport;
import ru.alamics.sso.keycloak.create.model.UserRequest;
import ru.alamics.sso.keycloak.search.dto.UserDto;
import ru.alamics.sso.registration.dto.ExternalSystemRoleRequest;
import ru.alamics.sso.registration.dto.UserPostRequest;
import ru.alamics.sso.registration.model.FormConstants;
import ru.alamics.sso.registration.model.User;

import javax.persistence.Tuple;
import javax.ws.rs.core.MultivaluedMap;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static ru.alamics.sso.registration.model.UserConstants.ATTR_ORG_NAME;

import static ru.alamics.sso.registration.model.UserConstants.ATTR_DMP_NAME;
import static ru.alamics.sso.registration.model.UserConstants.ATTR_TOMS_NAME;

public abstract class DataMapper {

    public static UserDto toUserDto(Tuple tuple) {
        if (tuple == null) {
            return null;
        }

        return UserDto.builder()
                .id(toString(tuple.get("user_id")))
                .username(toString(tuple.get("username")))
                .firstName(toString(tuple.get("first_name")))
                .lastName(toString(tuple.get("last_name")))
                .email(toString(tuple.get("email")))
                .phone(toString(tuple.get("phone")))
                .enabled(toBoolean(tuple.get("enabled")))
                .userPostId(toString(tuple.get("user_post_id")))
                .tomsId(toString(tuple.get("toms_id")))
                .dmpId(toString(tuple.get("dmp_id")))
                .roleId(toString(tuple.get("role_id")))
                .roleName(toString(tuple.get("role_name")))
                .systemRoleId(toString(tuple.get("system_role_id")))
                .systemRoleName(toString(tuple.get("system_role")))
                .systemId(toString(tuple.get("system_id")))
                .systemName(toString(tuple.get("system_name")))
                .systemLabel(toString(tuple.get("system_label")))
                .build();
    }

    public static List<UserDto> toUserDtoList(List<Tuple> tuples) {
        if (tuples.isEmpty()) {
            return null;
        }

        LinkedList<UserDto> userDtos = new LinkedList<>();
        tuples.forEach(o -> userDtos.add(toUserDto(o)));
        return userDtos;
    }

    public static UserPostRequest toUserPostRequest(UserModel userModel, UserRequest request) {
        if (userModel == null || request == null) {
            return null;
        }
        UserPostRequest userPostDto = new UserPostRequest();
        userPostDto.setUserId(userModel.getId());
        userPostDto.setTomsId(request.getTomsId());
        userPostDto.setDmpId(request.getDmpId());
        return userPostDto;
    }


    private static String toString(Object object) {
        if (object == null) {
            return null;
        }
        return object.toString();
    }

    public static UserPostRequest toUserPostRequest(FormContext context){
        if (context.getUser() == null){
            return null;
        }
        UserModel userModel = context.getUser();
        return toUserPostRequest(userModel);
    }

    public static UserPostRequest toUserPostRequest(UserModel userModel) {
        UserPostRequest userPostRequest = new UserPostRequest();
        userPostRequest.setUserId(userModel.getId());
        if (!userModel.getAttribute(ATTR_TOMS_NAME).isEmpty()){
            userPostRequest.setTomsId(userModel.getAttribute(ATTR_TOMS_NAME).get(0));
        }
        if (!userModel.getAttribute(ATTR_DMP_NAME).isEmpty()) {
            userPostRequest.setDmpId(userModel.getAttribute(ATTR_DMP_NAME).get(0));
        }
        return userPostRequest;
    }

    private static boolean toBoolean(Object object) {
        if (object == null) {
            return false;
        }
        return Boolean.valueOf(object.toString());
    }

    public static UserImport toUserImport(String[] row) {
        UserRequest userRequest = new UserRequest();
        UserImport userImport = new UserImport();
        for (int i = 0; i < row.length; i++) {
            switch (i) {
                case 0:
                    userRequest.setName(row[i]);
                    break;
                case 1:
                    userRequest.setEmail(row[i]);
                    break;
                case 2:
                    userRequest.setPhone(row[i]);
                    break;
                case 3:
                    userRequest.setTomsId(row[i]);
                    break;
                case 4:
                    userRequest.setDmpId(row[i]);
                    break;
                case 5:
                    userImport.setRoleName(row[i]);
                    break;
                case 6:
                    userImport.setSystemNames(List.of(row[i].replaceAll("\\s","").split(",")));
                    break;
            }
        }
        userImport.setUserRequest(userRequest);
        return userImport;
    }

    public static List<UserImport> toUserRequestList(List<String[]> rows) {
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        List<UserImport> userImports = new LinkedList<>();
        rows.forEach(o -> userImports.add(toUserImport(o)));
        return userImports;
    }

    public static ExternalSystemRoleRequest toExternalSystemRoleRequest(String id, Long sysId){
        if (id == null || sysId == null){
            return null;
        }
        ExternalSystemRoleRequest externalSystemRoleRequest = new ExternalSystemRoleRequest();
        externalSystemRoleRequest.setUserPostId(id);
        externalSystemRoleRequest.setSystemRoleId(sysId);
        return externalSystemRoleRequest;
    }

    public static List<UserDto> toGroupUserDtos(List<UserDto> userDtos) {
        List<UserDto> result = new LinkedList<>();
        for (int i = 0; i < userDtos.size(); i++) {
            UserDto userDto = userDtos.get(i);
            String systemNames = userDto.getSystemName();
            for (int j = i + 1; j < userDtos.size(); j++) {
                UserDto userDtoJ = userDtos.get(j);
                if (userDto.getId().equals(userDtoJ.getId()) && userDtoJ.getSystemName() != null && !userDtoJ.getSystemName().isBlank()) {
                    if (systemNames == null || systemNames.isBlank()){
                        systemNames = userDtoJ.getSystemName();
                    } else {
                        systemNames += ", " + userDtoJ.getSystemName();
                    }
                    userDtos.remove(j);
                    j--;
                }
            }
            userDto.setSystemName(systemNames);
            result.add(userDto);
        }
        return result;
    }
}