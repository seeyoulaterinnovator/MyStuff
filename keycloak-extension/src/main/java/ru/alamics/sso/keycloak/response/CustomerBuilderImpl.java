package ru.alamics.sso.keycloak.response;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class CustomerBuilderImpl implements CustomerResponseBuilder, Serializable {

    private static final long serialVersionUID = 4941520031645016766L;

    private Map<String, String> customerInfo = new HashMap<>();

    @Override
    public CustomerResponseBuilder setTomsId(String tomsId) {
        customerInfo.put("tomsId", tomsId);
        return this;
    }

    @Override
    public CustomerResponseBuilder setCustomerId(String customerId) {
        customerInfo.put("customerId", customerId);
        return this;
    }

    @Override
    public CustomerResponseBuilder setDmpId(String dmpId) {
        customerInfo.put("dmpId", dmpId);
        return this;
    }

    @Override
    public CustomerResponse build() {
        return new CustomerResponse(customerInfo);
    }
}
