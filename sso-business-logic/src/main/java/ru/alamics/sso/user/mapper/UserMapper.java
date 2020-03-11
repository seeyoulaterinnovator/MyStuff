package ru.alamics.sso.user.mapper;

import org.keycloak.authentication.FormContext;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.entities.UserAttributeEntity;
import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.keycloak.entity.ImportUsersDataEntity;
import ru.alamics.sso.keycloak.entity.ImportUsersReportEntity;
import ru.alamics.sso.keycloak.entity.common.ImportUsersReportStatus;
import ru.alamics.sso.registration.dto.ExternalSystemRoleRequest;
import ru.alamics.sso.registration.dto.UserPostRequest;
import ru.alamics.sso.user.model.ImportResponse;
import ru.alamics.sso.user.model.UserRequest;
import ru.alamics.sso.user.web.ImportUsersReportDto;
import ru.alamics.sso.user.web.UserDto;
import ru.alamics.sso.user.web.UserSearch;
import ru.alamics.sso.user.web.UserSearchDto;

import javax.persistence.Tuple;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static ru.alamics.sso.registration.model.UserConstants.*;

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
                .organization(toString(tuple.get("org")))
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
            return Collections.emptyList();
        }

        LinkedList<UserSearchDto> userDtos = new LinkedList<>();
        tuples.forEach(o -> userDtos.add(toUserDto(o)));
        return userDtos;
    }

    public static List<UserSearch> toUserSearchList(List<UserEntity> users) {
        if (users.isEmpty()) {
            return Collections.emptyList();
        }

        return users.stream()
                .map(user ->
                        UserSearch.builder()
                                .id(user.getId())
                                .email(user.getEmail())
                                .username(user.getUsername())
//                                .phone(user.getAttributes().stream()
//                                        .filter(attr -> ATTR_PHONE_NAME.equals(attr.getName()))
//                                        .map(UserAttributeEntity::getValue).findFirst().get())
                                .firstName(user.getFirstName())
                                .lastName(user.getLastName())
                                .enabled(user.isEnabled())
                                .build())
                .collect(Collectors.toList());
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
            String userPostId = userDto.getUserPostId();
            String systemNames = userDto.getSystemName();
            if (userPostId != null) {
                for (int j = i + 1; j < userDtos.size(); j++) {
                    UserSearchDto userDtoJ = userDtos.get(j);
                    if (userPostId.equals(userDtoJ.getUserPostId()) && userDto.getId().equals(userDtoJ.getId()) && userDtoJ.getSystemName() != null && !userDtoJ.getSystemName().isBlank()) {
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
            }
            result.add(userDto);
        }
        return result;
    }

    public static ImportUsersDataEntity toUserImport(String[] row) {
        ImportUsersDataEntity userImport = new ImportUsersDataEntity();
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

    public static List<ImportUsersDataEntity> toUserRequestList(List<String[]> rows) {
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        List<ImportUsersDataEntity> userImports = new LinkedList<>();
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
        if (!userModel.getAttribute(ATTR_ORG_NAME).isEmpty()) {
            userPostRequest.setOrgName(userModel.getAttribute(ATTR_ORG_NAME).get(0));
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

    public static UserRequest toUserRequest(ImportUsersDataEntity importUsersDataEntity) {
        if (importUsersDataEntity == null) {
            return null;
        }
        UserRequest userRequest = new UserRequest();
        userRequest.setEmail(importUsersDataEntity.getEmail());
        userRequest.setName(importUsersDataEntity.getFirstName());
        userRequest.setPhone(importUsersDataEntity.getPhone());
        userRequest.setTomsId(importUsersDataEntity.getTomsId());
        userRequest.setDmpId(importUsersDataEntity.getDmpId());
        return userRequest;
    }

    public static ImportUsersReportEntity toImportUsersReportEntity(String realmId, String name, List<ImportUsersDataEntity> importUserDataEntities, ImportResponse importResponse) {
        if (importUserDataEntities == null) {
            return null;
        }
        ImportUsersReportEntity importUserReport = new ImportUsersReportEntity();
        importUserReport.setName(name);
        importUserReport.setCountImportUsers(importUserDataEntities.size());
        importUserReport.setCountCreatedUsers(importResponse.getCreatedUsers().intValue());
        importUserReport.setCountClones(importResponse.getCountClones().intValue());
        importUserReport.setRealmId(realmId);
        importUserReport.setStatus(ImportUsersReportStatus.DONE);
        importUserDataEntities.forEach(o -> o.setImportUsersReport(importUserReport));
        importUserReport.setImportUserData(importUserDataEntities);
        return importUserReport;
    }

    public static ImportUsersReportEntity toImportUsersReportEntity(String realmId, String name, List<ImportUsersDataEntity> importUserDataEntities) {
        if (importUserDataEntities == null) {
            return null;
        }
        ImportUsersReportEntity importUsersReport = new ImportUsersReportEntity();
        importUsersReport.setName(name);
        importUsersReport.setCountImportUsers(importUserDataEntities.size());
        importUsersReport.setRealmId(realmId);
        importUserDataEntities.forEach(o -> o.setImportUsersReport(importUsersReport));
        importUsersReport.setImportUserData(importUserDataEntities);
        return importUsersReport;
    }

    public static ImportUsersReportDto toImportUsersReportDto(ImportUsersReportEntity importUsersReportEntity) {
        if (importUsersReportEntity == null) {
            return null;
        }
        return ImportUsersReportDto.builder()
                .id(importUsersReportEntity.getId())
                .realmId(importUsersReportEntity.getRealmId())
                .name(importUsersReportEntity.getName())
                .importDate(importUsersReportEntity.getImportDate().toString())
                .countClones(importUsersReportEntity.getCountClones())
                .countCreatedUsers(importUsersReportEntity.getCountCreatedUsers())
                .countImportUsers(importUsersReportEntity.getCountImportUsers())
                .status(importUsersReportEntity.getStatus().getDiscription())
                .build();
    }

    public static List<ImportUsersReportDto> toImportUsersReportDtos(List<ImportUsersReportEntity> importUserHistoryEntities) {
        if (importUserHistoryEntities == null || importUserHistoryEntities.isEmpty()) {
            return null;
        }
        return importUserHistoryEntities.stream()
                .map(UserMapper::toImportUsersReportDto)
                .collect(Collectors.toList());
    }
}
