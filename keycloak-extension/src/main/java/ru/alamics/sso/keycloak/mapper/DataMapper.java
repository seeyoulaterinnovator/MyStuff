package ru.alamics.sso.keycloak.mapper;

import ru.alamics.sso.keycloak.create.model.UserRequest;
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
                //.accessId(toString(tuple.get("access_id")))
                //.accessName(toString(tuple.get("access_name")))
                //.tomsId(toString(tuple.get("toms_id")))
                //.roleId(toString(tuple.get("role_id")))
                //.roleName(toString(tuple.get("role_name")))
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

    private static String toString(Object object){
        if (object == null){
            return null;
        }
        return object.toString();
    }

    public static UserRequest toUserRequest(String[] row){
        UserRequest userRequest = new UserRequest();
        userRequest.setEmail(row[0]);
        userRequest.setName(row[0]);
        userRequest.setPhone(row[1]);
        userRequest.setRealmName("user");
        return userRequest;
    }

    public static List<UserRequest> toUserRequestList(List<String[]> rows){
        if (rows == null || rows.isEmpty()){
            return null;
        }
        List<UserRequest> userRequests = new LinkedList<>();
        rows.stream().forEach(o -> userRequests.add(toUserRequest(o)));
        return userRequests;
    }
}