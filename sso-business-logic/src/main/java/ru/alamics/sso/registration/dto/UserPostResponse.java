package ru.alamics.sso.registration.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
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

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime updateTime;

    @JsonPOJOBuilder(withPrefix = "")
    public static class UserPostResponseBuilder {
    }
}
