package ru.alamics.sso.registration.mapper;

import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.keycloak.entity.ExternalSystemEntity;
import ru.alamics.sso.keycloak.entity.ExternalSystemRoleEntity;
import ru.alamics.sso.keycloak.entity.UserPostEntity;
import ru.alamics.sso.keycloak.entity.UserPostRoleEntity;
import ru.alamics.sso.registration.dto.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DataMapper {

    public static UserPostEntity toUserPost(UserPostEntity userPost, UserPostRequest userPostRequest) {
        if (userPostRequest == null) {
            return null;
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userPostRequest.getUserId());
        userPost.setUser(userEntity);
        userPost.setTomsId(userPostRequest.getTomsId());
        userPost.setDmpId(userPostRequest.getDmpId());
        UserPostRoleEntity userPostRole = new UserPostRoleEntity();
        userPostRole.setId(userPostRequest.getRoleId());
        userPost.setRole(userPostRole);

        return userPost;
    }

    public static UserPostEntity toUserPost(UserPostEntity userPost, UserPostEditRequest userPostEditRequest) {
        if (userPostEditRequest == null) {
            return null;
        }
        UserPostRoleEntity userPostRole = new UserPostRoleEntity();
        userPostRole.setId(userPostEditRequest.getRoleId());
        userPost.setRole(userPostRole);

        return userPost;
    }

    public static UserPostResponse toUserPostResponse(UserPostEntity userPost) {
        if (userPost == null) {
            return null;
        }
        Set<ExternalSystemRoleEntity> externalSystemRole = userPost.getSystemRoles();
        List<ExternalSystemRoleEntity> externalSystemRoles = null;
        if (externalSystemRole != null){
            externalSystemRoles = externalSystemRole.stream().collect(Collectors.toList());
        }
        return UserPostResponse.builder()
                .id(userPost.getId())
                .userId(userPost.getUser().getId())
                .userRole(toUserPostRoleDto(userPost.getRole()))
                .tomsId(userPost.getTomsId())
                .dmpId(userPost.getDmpId())
                .systemRoles(toExternalSystemRoleDtos(externalSystemRoles))
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
        if (externalSystemRoles == null || externalSystemRoles.isEmpty()){
            return null;
        }
        List<ExternalSystemRoleDto> externalSystemDtos = new LinkedList<>();
        externalSystemRoles.forEach(o -> externalSystemDtos.add(toExternalSystemRoleDto(o)));
        return externalSystemDtos;
    }
}
