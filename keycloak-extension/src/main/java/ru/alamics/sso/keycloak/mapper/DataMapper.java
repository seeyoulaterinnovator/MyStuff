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

        UserDto userDto = new UserDto();
        userDto.setId(toString(tuple.get("user_id")));
        userDto.setUsername(toString(tuple.get("USERNAME")));
        userDto.setfName(toString(tuple.get("FIRST_NAME")));
        userDto.setlName(toString(tuple.get("LAST_NAME")));
        userDto.setEmail(toString(tuple.get("EMAIL")));
        userDto.setPhone(toString(tuple.get("phone")));
        userDto.setAccessName(toString(tuple.get("access_name")));
        userDto.setAccessId(toString(tuple.get("access_id")));
        userDto.setTomsId(toString(tuple.get("toms_id")));
        userDto.setRoleId(toString(tuple.get("ROLE_ID")));
        userDto.setRoleName(toString(tuple.get("role_name")));
        return userDto;
    }

    public static List<UserDto> toUserDtoList(List<Tuple> tuples){
        if (tuples.isEmpty()){
            return null;
        }

        LinkedList<UserDto> userDtos = new LinkedList<>();
        tuples.stream()
                .forEach(o -> {
                    userDtos.add(toUserDto(o));
                });
        return userDtos;
    }

    private static String toString(Object object){
        if (object == null){
            return null;
        }
        return object.toString();
    }

}