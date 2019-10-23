package ru.alamics.sso.user.mapper;

import org.keycloak.authentication.FormContext;
import org.keycloak.models.UserModel;
import ru.alamics.sso.keycloak.entity.ImportUserDataEntity;
import ru.alamics.sso.keycloak.entity.ImportUserHistoryEntity;
import ru.alamics.sso.registration.dto.ExternalSystemRoleRequest;
import ru.alamics.sso.registration.dto.UserPostRequest;
import ru.alamics.sso.user.model.ImportResponse;
import ru.alamics.sso.user.model.UserRequest;
import ru.alamics.sso.user.web.ImportUserHistoryDto;
import ru.alamics.sso.user.web.UserDto;
import ru.alamics.sso.user.web.UserSearchDto;

import javax.persistence.Tuple;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static ru.alamics.sso.registration.model.UserConstants.ATTR_DMP_NAME;
import static ru.alamics.sso.registration.model.UserConstants.ATTR_TOMS_NAME;

public class UserMapper {

    public static UserDto toDto(UserModel model) {
        return UserDto.builder()
                .attributes(model.getAttributes())
                .firstName(model.getFirstName())
                .lastName(model.getLastName())
                .email(model.getEmail())
                .id(model.getId())
                .username(model.getUsername())
                .build();
    }

    public static UserSearchDto toUserDto(Tuple tuple) {
        if (tuple == null) {
            return null;
        }

        return UserSearchDto.builder()
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

    public static List<UserSearchDto> toUserDtoList(List<Tuple> tuples) {
        if (tuples.isEmpty()) {
            return null;
        }

        LinkedList<UserSearchDto> userDtos = new LinkedList<>();
        tuples.forEach(o -> userDtos.add(toUserDto(o)));
        return userDtos;
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

    public static List<UserSearchDto> toGroupUserDtos(List<UserSearchDto> userDtos) {
        List<UserSearchDto> result = new LinkedList<>();
        for (int i = 0; i < userDtos.size(); i++) {
            UserSearchDto userDto = userDtos.get(i);
            String systemNames = userDto.getSystemName();
            for (int j = i + 1; j < userDtos.size(); j++) {
                UserSearchDto userDtoJ = userDtos.get(j);
                if (userDto.getId().equals(userDtoJ.getId()) && userDtoJ.getSystemName() != null && !userDtoJ.getSystemName().isBlank()) {
                    if (systemNames == null || systemNames.isBlank()) {
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

    public static ImportUserDataEntity toUserImport(String[] row) {
        ImportUserDataEntity userImport = new ImportUserDataEntity();
        for (int i = 0; i < row.length; i++) {
            switch (i) {
                case 0:
                    userImport.setFirstName(row[i]);
                    break;
                case 1:
                    userImport.setEmail(row[i]);
                    break;
                case 2:
                    userImport.setPhone(row[i]);
                    break;
                case 3:
                    userImport.setTomsId(row[i]);
                    break;
                case 4:
                    userImport.setDmpId(row[i]);
                    break;
                case 5:
                    userImport.setRole(row[i]);
                    break;
                case 6:
                    //userImport.setSystemNames(List.of(row[i].replaceAll("\\s", "").split(",")));
                    userImport.setSystems(row[i]);
                    break;
            }
        }
        userImport.setCreated(false);
        return userImport;
    }

    public static List<ImportUserDataEntity> toUserRequestList(List<String[]> rows) {
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        List<ImportUserDataEntity> userImports = new LinkedList<>();
        rows.forEach(o -> userImports.add(toUserImport(o)));
        return userImports;
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

    public static UserPostRequest toUserPostRequest(FormContext context) {
        if (context.getUser() == null) {
            return null;
        }
        UserModel userModel = context.getUser();
        return toUserPostRequest(userModel);
    }

    public static UserPostRequest toUserPostRequest(UserModel userModel) {
        UserPostRequest userPostRequest = new UserPostRequest();
        userPostRequest.setUserId(userModel.getId());
        if (!userModel.getAttribute(ATTR_TOMS_NAME).isEmpty()) {
            userPostRequest.setTomsId(userModel.getAttribute(ATTR_TOMS_NAME).get(0));
        }
        if (!userModel.getAttribute(ATTR_DMP_NAME).isEmpty()) {
            userPostRequest.setDmpId(userModel.getAttribute(ATTR_DMP_NAME).get(0));
        }
        return userPostRequest;
    }

    public static ExternalSystemRoleRequest toExternalSystemRoleRequest(String id, Long sysId) {
        if (id == null || sysId == null) {
            return null;
        }
        ExternalSystemRoleRequest externalSystemRoleRequest = new ExternalSystemRoleRequest();
        externalSystemRoleRequest.setUserPostId(id);
        externalSystemRoleRequest.setSystemRoleId(sysId);
        return externalSystemRoleRequest;
    }

    public static UserRequest toUserRequest(ImportUserDataEntity importUserDataEntity) {
        if (importUserDataEntity == null) {
            return null;
        }
        UserRequest userRequest = new UserRequest();
        userRequest.setEmail(importUserDataEntity.getEmail());
        userRequest.setName(importUserDataEntity.getFirstName());
        userRequest.setPhone(importUserDataEntity.getPhone());
        userRequest.setTomsId(importUserDataEntity.getTomsId());
        userRequest.setDmpId(importUserDataEntity.getDmpId());
        return userRequest;
    }

    public static ImportUserHistoryEntity toImportUserHistoryEntity(String realmId, String name, List<ImportUserDataEntity> importUserDataEntities, ImportResponse importResponse) {
        if (importUserDataEntities == null) {
            return null;
        }
        ImportUserHistoryEntity importUserHistory = new ImportUserHistoryEntity();
        importUserHistory.setName(name);
        importUserHistory.setCountImportUsers(importUserDataEntities.size());
        importUserHistory.setCountCreatedUsers(importResponse.getCreatedUsers().intValue());
        importUserHistory.setCountClones(importResponse.getCountClones().intValue());
        importUserHistory.setRealmId(realmId);
        importUserHistory.setDone(true);
        importUserDataEntities.forEach(o -> o.setImportUserHistory(importUserHistory));
        importUserHistory.setImportUserData(importUserDataEntities);
        return importUserHistory;
    }

    public static ImportUserHistoryEntity toImportUserHistoryEntity(String realmId, String name, List<ImportUserDataEntity> importUserDataEntities) {
        if (importUserDataEntities == null) {
            return null;
        }
        ImportUserHistoryEntity importUserHistory = new ImportUserHistoryEntity();
        importUserHistory.setName(name);
        importUserHistory.setCountImportUsers(importUserDataEntities.size());
        importUserHistory.setRealmId(realmId);
        importUserDataEntities.forEach(o -> o.setImportUserHistory(importUserHistory));
        importUserHistory.setImportUserData(importUserDataEntities);
        return importUserHistory;
    }

    public static ImportUserHistoryDto toImportUserHistoryDto(ImportUserHistoryEntity importUserHistoryEntity) {
        if (importUserHistoryEntity == null) {
            return null;
        }
        return ImportUserHistoryDto.builder()
                .id(importUserHistoryEntity.getId())
                .realmId(importUserHistoryEntity.getRealmId())
                .name(importUserHistoryEntity.getName())
                .importDate(importUserHistoryEntity.getImportDate().toString())
                .countClones(importUserHistoryEntity.getCountClones())
                .countCreatedUsers(importUserHistoryEntity.getCountCreatedUsers())
                .countImportUsers(importUserHistoryEntity.getCountImportUsers())
                .isDone(importUserHistoryEntity.isDone())
                .build();
    }

    public static List<ImportUserHistoryDto> toImportUserHistoryDtos(List<ImportUserHistoryEntity> importUserHistoryEntities) {
        if (importUserHistoryEntities == null || importUserHistoryEntities.isEmpty()) {
            return null;
        }
        return importUserHistoryEntities.stream()
                .map(UserMapper::toImportUserHistoryDto)
                .collect(Collectors.toList());
    }
}
