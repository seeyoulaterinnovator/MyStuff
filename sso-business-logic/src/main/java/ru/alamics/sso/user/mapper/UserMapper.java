package ru.alamics.sso.user.mapper;

import org.keycloak.authentication.FormContext;
import org.keycloak.models.UserModel;
import ru.alamics.sso.jpa.entity.ImportUsersDataEntity;
import ru.alamics.sso.jpa.entity.ImportUsersReportEntity;
import ru.alamics.sso.jpa.entity.PersonalAccountEntity;
import ru.alamics.sso.jpa.entity.PersonalAccountPostEntity;
import ru.alamics.sso.jpa.entity.common.ImportUsersReportStatus;
import ru.alamics.sso.jpa.model.UserSummaryView;
import ru.alamics.sso.registration.dto.ExternalSystemRoleRequest;
import ru.alamics.sso.registration.dto.UserPostRequest;
import ru.alamics.sso.user.model.*;
import ru.alamics.sso.user.web.ImportUsersReportDto;
import ru.alamics.sso.user.web.UserDto;
import ru.alamics.sso.user.web.UserSearch;
import ru.alamics.sso.user.web.UserSearchDto;

import javax.persistence.Tuple;
import java.util.*;
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

    public static List<UserSearch> toUserSearchList(List<UserSummaryView> users) {
        if (users.isEmpty()) {
            return Collections.emptyList();
        }

        return users.stream()
                .map(user ->
                        UserSearch.builder()
                                .id(user.getId())
                                .email(user.getEmail())
                                .username(user.getUsername())
                                .phone(user.getPhone())
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

    // что это за хрень
    public static List<UserSearchDto> toGroupUserDtos(List<UserSearchDto> userDtos) {
        List<UserSearchDto> result = new LinkedList<>();
        for (int i = 0; i < userDtos.size(); i++) {
            UserSearchDto userDto = userDtos.get(i);
            String userPostId = userDto.getUserPostId();

            StringBuilder sysNames = new StringBuilder();
            if (userDto.getSystemName() != null)
                sysNames.append(userDto.getSystemName());

            if (userPostId != null) {
                for (int j = i + 1; j < userDtos.size(); j++) {
                    UserSearchDto userDtoJ = userDtos.get(j);
                    if (userPostId.equals(userDtoJ.getUserPostId()) && userDto.getId().equals(userDtoJ.getId()) && userDtoJ.getSystemName() != null && !userDtoJ.getSystemName().isEmpty()) {

                        if (userDtoJ.getSystemName() != null) {

                            if (sysNames.length() > 0)
                                sysNames.append(", ");

                            sysNames.append(userDtoJ.getSystemName());
                            userDtos.remove(j);
                            j--;
                        }
                    }
                }
                userDto.setSystemName(sysNames.toString());
            }
            result.add(userDto);
        }
        return result;
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

    public static ImportResponse toImportUsersReportEntity(ImportUsersReportModel importUserReport, List<ImportUsersDataModel> dataList ) {
        if (importUserReport == null) {
            return null;
        }

        ImportResponse importResponse = new ImportResponse();
        importResponse.getCreatedUsers().set(importUserReport.getCountCreatedUsers());
        importResponse.getCountClones().set(importUserReport.getCountClones());
        // TODO ?
        for (ImportUsersDataModel data : dataList) {
            importResponse.addCreatedUserIds(data.getEmail(), data.getUserId());

            Map<String, Object> map = new HashMap<>();
            map.put(data.getEmail(), data.getErrors());
            importResponse.addError(map);
        }
        // tbapi errors
        // tbapi success

        return importResponse;
    }

    public static ImportUsersReportModel toImportUsersReportEntity(String realmId, String name, List<ImportUsersDataModel> importUserDataEntities) {
        if (importUserDataEntities == null) {
            return null;
        }
        ImportUsersReportModel importUsersReport = new ImportUsersReportModel();
        importUsersReport.setName(name);
        importUsersReport.setCountImportUsers(importUserDataEntities.size());
        importUsersReport.setRealmId(realmId);
        //importUserDataEntities.forEach(o -> o.setImportUsersReport(importUsersReport));
        //importUsersReport.setImportUserData(importUserDataEntities);
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

    public static PersonalAccountModel toPADto(PersonalAccountEntity entity) {

        if (entity == null)
            return null;

        return PersonalAccountModel.builder()
                .uuid(entity.getUuid())
                //.postId(entity.getPostId())
                .value(entity.getValue())
                .build();
    }

    public static List<PersonalAccountModel> toPADtoList(Collection<PersonalAccountEntity> list) {

        if (list == null)
            return null;

        return list.stream()
                .map(UserMapper::toPADto)
                .collect(Collectors.toList());
    }

    public static PersonalAccountPostModel toPAPostDto(PersonalAccountPostEntity entity) {

        if (entity == null)
            return null;

        return PersonalAccountPostModel.builder()
                .postId(entity.getPostId())
                .accounts(toPADtoList(entity.getAccounts()))
                .build();
    }
}
