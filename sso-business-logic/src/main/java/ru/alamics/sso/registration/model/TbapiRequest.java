package ru.alamics.sso.registration.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TbapiRequest {

    //private String id;

    @Builder.Default
    final private String Type = "business";

    @Builder.Default
    final private String customerCategory = "B2R";

    private String email;

    private String name;

    private String phoneNumber;

}
