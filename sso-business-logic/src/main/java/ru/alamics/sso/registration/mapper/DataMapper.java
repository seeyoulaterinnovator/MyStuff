package ru.alamics.sso.registration.mapper;

import org.keycloak.models.jpa.entities.UserEntity;
import ru.alamics.sso.keycloak.entity.Post;
import ru.alamics.sso.keycloak.entity.UserPost;
import ru.alamics.sso.registration.dto.UserPostDto;

public class DataMapper {

    public static UserPost toUserPost(UserPostDto userPostDto){
        if ( userPostDto == null ) {
            return null;
        }

        UserPost userPost = new UserPost();

        UserEntity userEntity = new UserEntity();
        userEntity.setId(userPostDto.getId());
        userPost.setUser(userEntity);
        userPost.setId( userPostDto.getId() );
        userPost.setTomsId( userPostDto.getTomsId() );
        Post post = new Post();
        post.setId(userPostDto.getRoleId());
        userPost.setRole(post);

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
        return userPostDto;
    }

}
