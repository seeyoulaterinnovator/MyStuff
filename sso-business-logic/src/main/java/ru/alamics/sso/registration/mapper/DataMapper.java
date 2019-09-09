package ru.alamics.sso.registration.mapper;

import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.keycloak.entity.ExternalSystem;
import ru.alamics.sso.keycloak.entity.ExternalSystemRole;
import ru.alamics.sso.keycloak.entity.UserPostRole;
import ru.alamics.sso.keycloak.entity.UserPost;
import ru.alamics.sso.registration.dto.ExternalSystemDto;
import ru.alamics.sso.registration.dto.ExternalSystemRoleDto;
import ru.alamics.sso.registration.dto.UserPostDto;
import ru.alamics.sso.registration.dto.UserPostRoleDto;

import java.util.*;

public class DataMapper {

    public static UserPost toUserPost(UserPostDto userPostDto){
        if ( userPostDto == null ) {
            return null;
        }

        UserPost userPost = new UserPost();

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userPostDto.getUserId());
        userPost.setUser(userEntity);
        userPost.setId( userPostDto.getId() );
        userPost.setTomsId( userPostDto.getTomsId() );
        UserPostRole userPostRole = new UserPostRole();
        userPostRole.setId(userPostDto.getRoleId());
        userPost.setRole(userPostRole);
/*
        if (userPostDto.getSystemsId() != null){
            Set<System> systems = new HashSet<>();
            userPostDto.getSystemsId()
                    .forEach(o -> {
                        System system = new System();
                        system.setId(o);
                        systems.add(system);
                    });
            userPost.setSystems(systems);
        }
        if (userPostDto.getAccessId() != null){
            Set<Access> accesss = new HashSet<>();
            userPostDto.getSystemsId()
                    .forEach(o -> {
                        Access access = new Access();
                        access.setId(o);
                        accesss.add(access);
                    });
            userPost.setAccess(accesss);
        }*/

        return userPost;
    }

    public static UserPostDto toUserPostDto(UserPost userPost){
        if ( userPost == null ) {
            return null;
        }
        UserPostDto userPostDto = new UserPostDto();
        userPostDto.setId(userPost.getId());
        userPostDto.setUserId(userPost.getUser().getId());
        userPostDto.setTomsId(userPost.getTomsId());
        userPostDto.setRmsId(userPost.getDmpId());
        userPostDto.setRoleId(userPost.getRole().getId());

//        if (userPost.getSystems() != null) {
//            Set<Long> systemsId = new HashSet<>();
//            userPost.getSystems()
//                    .forEach(o -> systemsId.add(o.getId()));
//            userPostDto.setSystemsId(systemsId);
//        }
        return userPostDto;
    }

    public static List<UserPostDto> toUserPostDtoList(List<UserPost> userPostList){
        if (userPostList == null) {
            return null;
        }
        List<UserPostDto> userPostDtos = new LinkedList<>();
        userPostList
                .forEach(o -> userPostDtos.add(toUserPostDto(o)));
        return userPostDtos;
    }

    public static UserPostRoleDto toUserPostRoleDto(UserPostRole userPostRole){
        UserPostRoleDto userPostRoleDto = new UserPostRoleDto();
        userPostRoleDto.setId(userPostRole.getId());
        userPostRoleDto.setName(userPostRole.getName());
        return  userPostRoleDto;
    }

    public static List<UserPostRoleDto> toUserPostRoleDtoList(List<UserPostRole> userPostRoleList){
        List<UserPostRoleDto> userPostRoleDto = new LinkedList<>();
        userPostRoleList.forEach(o -> userPostRoleDto.add(toUserPostRoleDto(o)));
        return userPostRoleDto;
    }

    public static ExternalSystemDto toExternalSystemDto(ExternalSystem externalSystem){
        return ExternalSystemDto.builder()
                .id(externalSystem.getId())
                .name(externalSystem.getName())
                .build();
    }

    public static List<ExternalSystemDto> toExternalSystemDtos(List<ExternalSystem> externalSystems){
        List<ExternalSystemDto> externalSystemDtos = new LinkedList<>();
        externalSystems.forEach(o -> externalSystemDtos.add(toExternalSystemDto(o)));
        return externalSystemDtos;
    }

    public static ExternalSystemRoleDto toExternalSystemRoleDto(ExternalSystemRole externalSystemRole){
        return ExternalSystemRoleDto.builder()
                .id(externalSystemRole.getId())
                .name(externalSystemRole.getName())
                .externalSystem(toExternalSystemDto(externalSystemRole.getExternalSystem()))
                .build();
    }

    public static List<ExternalSystemRoleDto> toExternalSystemRoleDtos(List<ExternalSystemRole> externalSystemRoles){
        List<ExternalSystemRoleDto> externalSystemDtos = new LinkedList<>();
        externalSystemRoles.forEach(o -> externalSystemDtos.add(toExternalSystemRoleDto(o)));
        return externalSystemDtos;
    }
}
