package ru.alamics.sso.registration.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class UserPostRoleDto implements Serializable {
    private Long id;
    private String name;
    private String description;

    @Override
    public String toString() {
        return "UserPostRoleDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
