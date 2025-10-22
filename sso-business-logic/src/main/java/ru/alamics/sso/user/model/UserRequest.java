package ru.alamics.sso.user.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRawValue;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class UserRequest implements Serializable {

    private String email;
    private String phone;
    private String name;
    private String dmpId;
    private String tomsId;
    @JsonProperty(value = "")
    private String markBrandId;
    private Long roleId;
    @JsonProperty(value = "token")
    private String token;
    private Map<String, List<String>> attributes;
}