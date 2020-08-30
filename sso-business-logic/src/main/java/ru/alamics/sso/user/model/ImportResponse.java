package ru.alamics.sso.user.model;

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
    private AtomicInteger countClones;
    private AtomicInteger tbapiErrors;
    private AtomicInteger tbapiSuccess;
    private AtomicInteger createdUsers;
    private List<Map<String, Object>> createdUserIds = new LinkedList<>();
    private List<Map<String, Object>> errors = new LinkedList<>();

    public void addCreatedUserIds(String key, Object value){

        Map<String, Object> map = new HashMap<>();
        map.put(key,value);
        createdUserIds.add(map);
    }

    public void addError(Map<String, Object> error){

        errors.add(error);
    }
}
