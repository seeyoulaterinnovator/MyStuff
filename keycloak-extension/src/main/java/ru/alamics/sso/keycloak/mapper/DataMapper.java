package ru.alamics.sso.keycloak.mapper;

import org.keycloak.models.UserModel;
import ru.alamics.sso.keycloak.create.model.UserRequest;
import ru.alamics.sso.keycloak.search.dto.UserDto;
import ru.alamics.sso.registration.dto.UserPostRequest;

import javax.persistence.Tuple;
import java.util.LinkedList;
import java.util.List;

public abstract class DataMapper {

    public static UserDto toUserDto(Tuple tuple){
        if (tuple == null){
            return null;
        }

        return UserDto.builder()
                .id(toString(tuple.get("user_id")))
                .username(toString(tuple.get("username")))
                .firstName(toString(tuple.get("first_name")))
                .lastName(toString(tuple.get("last_name")))
                .email(toString(tuple.get("email")))
                .phone(toString(tuple.get("phone")))
                .userPostId(toString(tuple.get("user_post_id")))
                //.accessName(toString(tuple.get("access_name")))
                .tomsId(toString(tuple.get("toms_id")))
                .roleId(toString(tuple.get("role_id")))
                .roleName(toString(tuple.get("role_name")))
                //.clientRoleId(toString(tuple.get("client_role_id")))
                //.clientRoleName(toString(tuple.get("client_role_name")))
                .build();
    }

    public static List<UserDto> toUserDtoList(List<Tuple> tuples){
        if (tuples.isEmpty()){
            return null;
        }

        LinkedList<UserDto> userDtos = new LinkedList<>();
        tuples.forEach(o -> userDtos.add(toUserDto(o)));
        return userDtos;
    }

    public static UserPostRequest toUserPostRequest(UserModel userModel, UserRequest request) {
        if (userModel == null || request == null){
            return null;
        }
        UserPostRequest userPostDto = new UserPostRequest();
        userPostDto.setUserId(userModel.getId());
        userPostDto.setTomsId(request.getTomsId());
        userPostDto.setDmpId(request.getDmpId());
        return userPostDto;
    }


    private static String toString(Object object){
        if (object == null){
            return null;
        }
        return object.toString();
    }
}