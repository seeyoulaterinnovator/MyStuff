package ru.alamics.sso.registration.rias.model;

import lombok.Data;

@Data
public class RiasLogin {

    private String refresh_token;
    private String access_token;

    private String error;
    private String error_description;
}
