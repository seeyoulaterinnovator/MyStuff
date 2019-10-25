package ru.alamics.sso.registration;

import java.util.HashMap;
import java.util.Map;

public class FoundException extends Exception {

    private Map<String, Object> result;

    public FoundException() {
        super();
    }

    public FoundException(String message) {
        super(message);
    }

    public FoundException addResult(String key, Object value){
        if (result == null){
            result = new HashMap<>();
        }
        result.put(key, value);
        return this;
    }

    public Map<String, Object> getResult(){
        return result;
    }
}
