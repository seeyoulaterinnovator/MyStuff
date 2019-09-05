package ru.alamics.sso.registration.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
public class UserPostDto {
    private String id;
    private String userId;
    private String tomsId;
    private String rmsId;
    private Long roleId;
    private Set<Long> systemsId;

    @Override
    public String toString() {
        return "UserPostDto{" +
                "id='" + id + '\'' +
                ", userId='" + userId + '\'' +
                ", tomsId='" + tomsId + '\'' +
                ", rmsId='" + rmsId + '\'' +
                ", roleId=" + roleId +
                '}';
    }
}
