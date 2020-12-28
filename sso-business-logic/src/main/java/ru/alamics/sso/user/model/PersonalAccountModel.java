package ru.alamics.sso.user.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
public class PersonalAccountModel implements Serializable {

    @JsonProperty("uuid")
    private String uuid;
    @JsonProperty("post_id")
    private String postId;
    @JsonProperty("value")
    private String value;
}
