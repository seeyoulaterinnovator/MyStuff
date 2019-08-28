package ru.alamics.sso.registration.model;

import lombok.Builder;
import lombok.Data;

@Data
public class TbapiRequest {

    final private CustomerCategory customerCategory = new CustomerCategory();

    final private LegalAddress legalAddress = new LegalAddress();

    private String email;

    private String name;

    private String phoneNumber;

    private String legalName;

    @Data
    private class LegalAddress {

        final private String id = "9153062588613043803";
    }

    @Data
    private class CustomerCategory {

        final private String id = "9149000490413788409";

        final private String name = "B2R";
    }
}
