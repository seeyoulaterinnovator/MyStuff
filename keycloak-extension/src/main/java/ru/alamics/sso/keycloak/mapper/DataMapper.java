package ru.alamics.sso.keycloak.mapper;

import org.keycloak.models.UserModel;
import ru.alamics.sso.keycloak.create.model.UserImport;
import ru.alamics.sso.keycloak.create.model.UserRequest;
import ru.alamics.sso.keycloak.search.dto.UserDto;
import ru.alamics.sso.registration.dto.ExternalSystemRoleRequest;
import ru.alamics.sso.registration.dto.UserPostRequest;
import ru.alamics.sso.registration.model.User;

import javax.persistence.Tuple;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static ru.alamics.sso.registration.model.UserConstants.ATTR_ORG_NAME;

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
                .roleId(toString(tuple.get("role_id")))
                .roleName(toString(tuple.get("role_name")))
                .systemRoleId(toString(tuple.get("system_role_id")))
                .systemRoleName(toString(tuple.get("system_role")))
                .systemId(toString(tuple.get("system_id")))
                .systemName(toString(tuple.get("system_name")))
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
                    userRequest.setEmail(row[0]);
                    userRequest.setName(row[0]);
                    break;
                case 1:
                    userRequest.setPhone(row[1]);
                    break;
                case 2:
                    userImport.setOrg(row[2]);
                    break;
                case 3:
                    userImport.setRoleName(row[3]);
                    break;
                case 4:
                    userImport.setSystemName(row[4]);
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
        rows.stream().forEach(o -> userImports.add(toUserImport(o)));
        return userImports;
    }

    public static User toUser(UserImport userImport) {
        Map<String, List<String>> attr = new HashMap<String, List<String>>();
        attr.put(ATTR_ORG_NAME, List.of(userImport.getOrg()));
        return User.builder()
                .name(userImport.getUserRequest().getName())
                .email(userImport.getUserRequest().getEmail())
                .phone(userImport.getUserRequest().getPhone())
                .attributes(attr)
                .build();
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
}