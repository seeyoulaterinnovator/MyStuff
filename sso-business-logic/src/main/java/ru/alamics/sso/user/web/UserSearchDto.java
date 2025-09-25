package ru.alamics.sso.user.web;

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Builder(builderClassName = "UserDtoBuilder", toBuilder = true)
public class UserSearchDto implements Serializable {

    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Boolean enabled;
    private String userPostId;
    private String tomsId;
    private String markBrandId;
    private String dmpId;
    private String organization;
    private String roleId;
    private String roleName;
    private List<String> account;
    private String systemRoleId;
    private String systemRoleName;
    private String systemId;
    private String systemName;
    private String systemLabel;

    @JsonPOJOBuilder(withPrefix = "")
    public static class UserDtoBuilder {
    }
}
