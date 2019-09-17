package ru.alamics.sso.keycloak.mapper;

import ru.alamics.sso.keycloak.search.dto.UserDto;

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
                .tomsId(toString(tuple.get("toms_id")))
                .roleId(toString(tuple.get("role_id")))
                .roleName(toString(tuple.get("role_name")))
                .systemRoleId(toString(tuple.get("system_role_id")))
                .systemRoleName(toString(tuple.get("system_role")))
                .systemId(toString(tuple.get("system_id")))
                .systemName(toString(tuple.get("system_name")))
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

    private static String toString(Object object){
        if (object == null){
            return null;
        }
        return object.toString();
    }

}