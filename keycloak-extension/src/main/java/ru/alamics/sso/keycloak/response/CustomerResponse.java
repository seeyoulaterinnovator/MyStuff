package ru.alamics.sso.keycloak.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

@Data
public class CustomerResponse implements Serializable {

    private static final long serialVersionUID = 4996520031645016766L;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String,String> customerInfo;

    public CustomerResponse(Map<String, String> customerInfo) {
        this.customerInfo = customerInfo;
    }
}
