package ru.alamics.sso.keycloak.response;

public interface CustomerResponseBuilder {


    CustomerResponseBuilder setTomsId(String tomsId);

    CustomerResponseBuilder setCustomerId(String customerId);

    CustomerResponseBuilder setDmpId(String dmpId);

    CustomerResponse build();

}
