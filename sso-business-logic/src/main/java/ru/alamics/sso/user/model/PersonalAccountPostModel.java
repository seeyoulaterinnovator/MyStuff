package ru.alamics.sso.user.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
public class PersonalAccountPostModel implements Serializable {

    @JsonProperty("post_id")
    private String postId;
    @JsonProperty("accounts")
    private List<PersonalAccountModel> accounts;
}
