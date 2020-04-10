package ru.alamics.sso.registration.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonDeserialize(builder = UserPostResponse.UserPostResponseBuilder.class)
@Builder(builderClassName = "UserPostResponseBuilder", toBuilder = true)
public class UserPostResponse implements Serializable {
    private String id;
    private String userId;
    private String tomsId;
    private String dmpId;
    private UserPostRoleDto userRole;
    private List<ExternalSystemRoleDto> systemRoles;
    private boolean selected;
    private String organization;
    private LocalDateTime updateTime;

    @JsonPOJOBuilder(withPrefix = "")
    public static class UserPostResponseBuilder {
    }
}
