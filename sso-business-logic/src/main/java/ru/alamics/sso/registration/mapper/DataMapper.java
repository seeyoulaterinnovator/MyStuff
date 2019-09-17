package ru.alamics.sso.registration.mapper;

import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.keycloak.entity.ExternalSystem;
import ru.alamics.sso.keycloak.entity.ExternalSystemRole;
import ru.alamics.sso.keycloak.entity.UserPost;
import ru.alamics.sso.keycloak.entity.UserPostRole;
import ru.alamics.sso.registration.dto.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class DataMapper {

    public static UserPost toUserPost(UserPost userPost, UserPostRequest userPostRequest) {
        if (userPostRequest == null) {
            return null;
        }

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userPostRequest.getUserId());
        userPost.setUser(userEntity);
        userPost.setTomsId(userPostRequest.getTomsId());
        userPost.setDmpId(userPostRequest.getDmpId());
        UserPostRole userPostRole = new UserPostRole();
        userPostRole.setId(userPostRequest.getRoleId());
        userPost.setRole(userPostRole);

        return userPost;
    }

    public static UserPost toUserPost(UserPost userPost, UserPostEditRequest userPostEditRequest) {
        if (userPostEditRequest == null) {
            return null;
        }
        UserPostRole userPostRole = new UserPostRole();
        userPostRole.setId(userPostEditRequest.getRoleId());
        userPost.setRole(userPostRole);

        return userPost;
    }

    public static UserPostResponse toUserPostResponse(UserPost userPost) {
        if (userPost == null) {
            return null;
        }
        Set<ExternalSystemRole> externalSystemRole = userPost.getSystemRoles();
        List<ExternalSystemRole> externalSystemRoles = null;
        if (externalSystemRole != null){
            externalSystemRoles = externalSystemRole.stream().collect(Collectors.toList());
        }
        return UserPostResponse.builder()
                .id(userPost.getId())
                .userId(userPost.getUser().getId())
                .userRole(toUserPostRoleDto(userPost.getRole()))
                .dmpId(userPost.getDmpId())
                .systemRoles(toExternalSystemRoleDtos(externalSystemRoles))
                .build();
    }

    public static List<UserPostResponse> toUserPostResponseList(List<UserPost> userPostList) {
        if (userPostList == null) {
            return null;
        }
        List<UserPostResponse> userPostDtos = new LinkedList<>();
        userPostList
                .forEach(o -> userPostDtos.add(toUserPostResponse(o)));
        return userPostDtos;
    }

    public static UserPostRoleDto toUserPostRoleDto(UserPostRole userPostRole) {
        UserPostRoleDto userPostRoleDto = new UserPostRoleDto();
        userPostRoleDto.setId(userPostRole.getId());
        userPostRoleDto.setName(userPostRole.getName());
        return userPostRoleDto;
    }

    public static List<UserPostRoleDto> toUserPostRoleDtoList(List<UserPostRole> userPostRoleList) {
        List<UserPostRoleDto> userPostRoleDto = new LinkedList<>();
        userPostRoleList.forEach(o -> userPostRoleDto.add(toUserPostRoleDto(o)));
        return userPostRoleDto;
    }

    public static ExternalSystemDto toExternalSystemDto(ExternalSystem externalSystem) {
        return ExternalSystemDto.builder()
                .id(externalSystem.getId())
                .name(externalSystem.getName())
                .build();
    }

    public static List<ExternalSystemDto> toExternalSystemDtos(List<ExternalSystem> externalSystems) {
        List<ExternalSystemDto> externalSystemDtos = new LinkedList<>();
        externalSystems.forEach(o -> externalSystemDtos.add(toExternalSystemDto(o)));
        return externalSystemDtos;
    }

    public static ExternalSystemRoleDto toExternalSystemRoleDto(ExternalSystemRole externalSystemRole) {
        return ExternalSystemRoleDto.builder()
                .id(externalSystemRole.getId())
                .name(externalSystemRole.getName())
                .externalSystem(toExternalSystemDto(externalSystemRole.getExternalSystem()))
                .build();
    }

    public static List<ExternalSystemRoleDto> toExternalSystemRoleDtos(List<ExternalSystemRole> externalSystemRoles) {
        if (externalSystemRoles == null || externalSystemRoles.isEmpty()){
            return null;
        }
        List<ExternalSystemRoleDto> externalSystemDtos = new LinkedList<>();
        externalSystemRoles.forEach(o -> externalSystemDtos.add(toExternalSystemRoleDto(o)));
        return externalSystemDtos;
    }
}
