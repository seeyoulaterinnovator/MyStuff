package ru.alamics.sso.registration.mapper;

import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.keycloak.entity.Post;
import ru.alamics.sso.keycloak.entity.System;
import ru.alamics.sso.keycloak.entity.UserPost;
import ru.alamics.sso.registration.dto.UserPostDto;

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
        Post post = new Post();
        post.setId(userPostDto.getRoleId());
        userPost.setRole(post);

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

        return userPost;
    }

    public static UserPostDto toUserPostDto(UserPost userPost){
        if ( userPost == null ) {
            return null;
        }
        UserPostDto userPostDto = new UserPostDto();
        userPostDto.setId(userPost.getId());
        userPostDto.setUserId(userPost.getId());
        userPostDto.setTomsId(userPost.getTomsId());
        userPostDto.setRmsId(userPost.getDmpId());
        userPostDto.setRoleId(userPost.getRole().getId());

        if (userPost.getSystems() != null) {
            Set<Long> systemsId = new HashSet<>();
            userPost.getSystems().stream()
                    .forEach(o -> systemsId.add(o.getId()));
            userPostDto.setSystemsId(systemsId);
        }
        return userPostDto;
    }

    public static List<UserPostDto> toUserPostDtoList(List<UserPost> userPostList){
        if (userPostList == null) {
            return null;
        }
        List<UserPostDto> userPostDtos = new LinkedList<>();
        userPostList.stream()
                .forEach(o -> userPostDtos.add(toUserPostDto(o)));
        return userPostDtos;
    }

}
