package ru.alamics.sso.registration.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Data
@JsonDeserialize(builder = UserPostResponse.UserPostResponseBuilder.class)
public class UserPostResponse implements Serializable {
    private String id;
    private String userId;
    private String tomsId;
    private String dmpId;
    private String markBrandId;
    private UserPostRoleDto userRole;
    private List<ExternalSystemRoleDto> systemRoles;
    private boolean selected;
    private String organization;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime updateTime;

    // нужен для сериализации Infinspan
    // Builder'у сделан delombok тк он не создает оба конструктора
    public UserPostResponse() {
    }

    @java.beans.ConstructorProperties({"id", "userId", "tomsId", "dmpId", "markBrandId", "userRole", "systemRoles", "selected", "organization", "updateTime"})
    UserPostResponse(String id, String userId, String tomsId, String dmpId, String markBrandId, UserPostRoleDto userRole, List<ExternalSystemRoleDto> systemRoles, boolean selected, String organization, LocalDateTime updateTime) {
        this.id = id;
        this.userId = userId;
        this.tomsId = tomsId;
        this.dmpId = dmpId;
        this.markBrandId = markBrandId;
        this.userRole = userRole;
        this.systemRoles = systemRoles;
        this.selected = selected;
        this.organization = organization;
        this.updateTime = updateTime;
    }

    public static UserPostResponseBuilder builder() {
        return new UserPostResponseBuilder();
    }

    public UserPostResponseBuilder toBuilder() {
        return new UserPostResponseBuilder().id(this.id).userId(this.userId).tomsId(this.tomsId).dmpId(this.dmpId).markBrandId(this.markBrandId).userRole(this.userRole).systemRoles(this.systemRoles).selected(this.selected).organization(this.organization).updateTime(this.updateTime);
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class UserPostResponseBuilder {
        private String id;
        private String userId;
        private String tomsId;
        private String dmpId;
        private String markBrandId;
        private UserPostRoleDto userRole;
        private List<ExternalSystemRoleDto> systemRoles;
        private boolean selected;
        private String organization;
        private LocalDateTime updateTime;

        UserPostResponseBuilder() {
        }

        public UserPostResponseBuilder id(String id) {
            this.id = id;
            return this;
        }

        public UserPostResponseBuilder userId(String userId) {
            this.userId = userId;
            return this;
        }

        public UserPostResponseBuilder tomsId(String tomsId) {
            this.tomsId = tomsId;
            return this;
        }

        public UserPostResponseBuilder dmpId(String dmpId) {
            this.dmpId = dmpId;
            return this;
        }

        public UserPostResponseBuilder markBrandId(String markBrandId) {
            this.markBrandId = markBrandId;
            return this;
        }

        public UserPostResponseBuilder userRole(UserPostRoleDto userRole) {
            this.userRole = userRole;
            return this;
        }

        public UserPostResponseBuilder systemRoles(List<ExternalSystemRoleDto> systemRoles) {
            this.systemRoles = systemRoles;
            return this;
        }

        public UserPostResponseBuilder selected(boolean selected) {
            this.selected = selected;
            return this;
        }

        public UserPostResponseBuilder organization(String organization) {
            this.organization = organization;
            return this;
        }

        public UserPostResponseBuilder updateTime(LocalDateTime updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        public UserPostResponse build() {
            return new UserPostResponse(id, userId, tomsId, dmpId, markBrandId, userRole, systemRoles, selected, organization, updateTime);
        }

        @Override
        public String toString() {
            return "UserPostResponseBuilder{" +
                    "id='" + id + '\'' +
                    ", userId='" + userId + '\'' +
                    ", tomsId='" + tomsId + '\'' +
                    ", dmpId='" + dmpId + '\'' +
                    ", markBrandId='" + markBrandId + '\'' +
                    ", userRole=" + userRole +
                    ", systemRoles=" + systemRoles +
                    ", selected=" + selected +
                    ", organization='" + organization + '\'' +
                    ", updateTime=" + updateTime +
                    '}';
        }
    }
}
