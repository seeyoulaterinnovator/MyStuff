package ru.alamics.sso.registration.mapper;

import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.jpa.entity.*;
import ru.alamics.sso.registration.dto.*;
import ru.alamics.sso.registration.model.UserEntityRepresentation;
import ru.alamics.sso.settings.SettingsDto;
import ru.alamics.sso.user.web.UserSearch;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DataMapper {

    public static UserPostEntity toUserPost(UserPostRequest userPostRequest) {
        if (userPostRequest == null) {
            return null;
        }

        UserPostEntity userPost = new UserPostEntity();

        userPost.setCustomer(Customer.builder()
                .id(userPostRequest.getTomsId())
                .name(userPostRequest.getOrgName())
                .build());

        userPost.setDmpId(userPostRequest.getDmpId());
        userPost.setSelected(userPostRequest.isSelected());

        return userPost;
    }

    public static UserPostResponse toUserPostResponse(UserPostEntity userPost) {
        if (userPost == null) {
            return null;
        }
        Set<ExternalSystemRoleEntity> externalSystemRole = userPost.getSystemRoles();
        List<ExternalSystemRoleEntity> externalSystemRoles = null;
        if (externalSystemRole != null) {
            externalSystemRoles = externalSystemRole.stream().collect(Collectors.toList());
        }
        return UserPostResponse.builder()
                .id(userPost.getId())
                .userId(userPost.getUser().getId())
                .userRole(toUserPostRoleDto(userPost.getRole()))
                .tomsId(userPost.getCustomer().getId())
                .organization(userPost.getCustomer().getName())
                .updateTime(userPost.getCustomer().getUpdateTime())
                .dmpId(userPost.getDmpId())
                .selected(userPost.isSelected())
                .systemRoles(toExternalSystemRoleDtos(externalSystemRoles))
                .updateTime(userPost.getCustomer().getUpdateTime())
                .build();
    }

    public static List<UserPostResponse> toUserPostResponseList(List<UserPostEntity> userPostList) {
        if (userPostList == null) {
            return null;
        }
        List<UserPostResponse> userPostDtos = new LinkedList<>();
        userPostList
                .forEach(o -> userPostDtos.add(toUserPostResponse(o)));
        return userPostDtos;
    }

    public static UserPostRoleDto toUserPostRoleDto(UserPostRoleEntity userPostRole) {
        UserPostRoleDto userPostRoleDto = new UserPostRoleDto();
        userPostRoleDto.setId(userPostRole.getId());
        userPostRoleDto.setName(userPostRole.getName());
        return userPostRoleDto;
    }

    public static List<UserPostRoleDto> toUserPostRoleDtoList(List<UserPostRoleEntity> userPostRoleList) {
        List<UserPostRoleDto> userPostRoleDto = new LinkedList<>();
        userPostRoleList.forEach(o -> userPostRoleDto.add(toUserPostRoleDto(o)));
        return userPostRoleDto;
    }

    public static ExternalSystemDto toExternalSystemDto(ExternalSystemEntity externalSystem) {
        return ExternalSystemDto.builder()
                .id(externalSystem.getId())
                .name(externalSystem.getName())
                .label(externalSystem.getLabel())
                .build();
    }

    public static List<ExternalSystemDto> toExternalSystemDtos(List<ExternalSystemEntity> externalSystems) {
        List<ExternalSystemDto> externalSystemDtos = new LinkedList<>();
        externalSystems.forEach(o -> externalSystemDtos.add(toExternalSystemDto(o)));
        return externalSystemDtos;
    }

    public static ExternalSystemRoleDto toExternalSystemRoleDto(ExternalSystemRoleEntity externalSystemRole) {
        return ExternalSystemRoleDto.builder()
                .id(externalSystemRole.getId())
                .name(externalSystemRole.getName())
                .externalSystem(toExternalSystemDto(externalSystemRole.getExternalSystem()))
                .build();
    }

    public static List<ExternalSystemRoleDto> toExternalSystemRoleDtos(List<ExternalSystemRoleEntity> externalSystemRoles) {
        if (externalSystemRoles == null || externalSystemRoles.isEmpty()) {
            return null;
        }
        List<ExternalSystemRoleDto> externalSystemDtos = externalSystemRoles.stream()
                .map(DataMapper::toExternalSystemRoleDto).collect(Collectors.toList());
        return externalSystemDtos;
    }


    public static SettingsDto toDto(Settings settings) {
        if (settings == null) {
            return null;
        }
        return SettingsDto.builder()
                .desc(settings.getDesc())
                .extId(settings.getExtId())
                .id(settings.getId())
                .name(settings.getName())
                .value(settings.getValue())
                .realmId(settings.getRealmId())
                .unit(settings.getUnit())
                .build();
    }

    public static UserEntityRepresentation toUserEntityRepresentation(UserEntity user) {
        if (user == null) {
            return null;
        }
        UserEntityRepresentation userEntityRepresentation = new UserEntityRepresentation();
        userEntityRepresentation.setId(user.getId());
        userEntityRepresentation.setCreatedTimestamp(user.getCreatedTimestamp());
        userEntityRepresentation.setEmail(user.getEmail());
        userEntityRepresentation.setEnabled(user.isEnabled());
        return userEntityRepresentation;
    }

    public static PageDto toPageDto(List<UserSearch> users, long totalElements, int pageNum, int pageSize) {
        long totalPages = pageSize == 0 ? 1 : (long) Math.ceil((double) totalElements / (double) pageSize);
        return new PageDto(pageNum, pageSize, users.size(), totalElements, totalPages, pageNum > 1, pageNum < totalPages);
    }
}
