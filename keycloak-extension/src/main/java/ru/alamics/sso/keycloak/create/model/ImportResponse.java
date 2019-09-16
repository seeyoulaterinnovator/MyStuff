package ru.alamics.sso.keycloak.create.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Data
@NoArgsConstructor
public class ImportResponse {
    private int countClones;
    private AtomicInteger tbapiErrors;
    private AtomicInteger tbapiSuccess;
    private List<Map<String, Object>> createdUserIds;
    private List<Map<String, Object>> notCreatedUsers;

    public void addCreatedUserIds(String key, Object value){
        if (createdUserIds == null){
            createdUserIds = new LinkedList<>();
        }
        Map<String, Object> map = new HashMap<>();
        map.put(key,value);
        createdUserIds.add(map);
    }

    public void addNotCreatedUsers(String key, Object value){
        if (notCreatedUsers == null){
            notCreatedUsers = new LinkedList<>();
        }
        Map<String, Object> map = new HashMap<>();
        map.put(key,value);
        notCreatedUsers.add(map);
    }
}
