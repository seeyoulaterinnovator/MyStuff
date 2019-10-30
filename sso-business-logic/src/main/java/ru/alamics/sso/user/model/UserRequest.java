package ru.alamics.sso.user.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class UserRequest implements Serializable {

    private String email;
    private String phone;
    private String name;
    private String dmpId;
    private String tomsId;
}