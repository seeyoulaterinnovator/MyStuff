package ru.alamics.sso.registration.tbapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import static ru.alamics.sso.registration.model.TbapiConstants.*;

@Data
public class TbapiRequest {

    final private CustomerCategory customerCategory = new CustomerCategory();

    final private LegalAddress legalAddress = new LegalAddress();

    private ExtendedMap extendedMap = new ExtendedMap();

    @JsonIgnore
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

    @Data
    private class ExtendedMap {

        @JsonProperty(TBAPI_PHONE_ID)
        private MapObjectHolder phoneHolder = new MapObjectHolder(TBAPI_PHONE_NAME);

        @JsonProperty(TBAPI_EMAIL_ID)
        private MapObjectHolder emailHolder = new MapObjectHolder(TBAPI_EMAIL_NAME);

        @Data
        private class MapObjectHolder {

            private int attributeType = 0;
            private String attributeName;
            private SingleValue singleValue = new SingleValue();

            public MapObjectHolder() {
            }

            public MapObjectHolder(String attributeName) {
                this.attributeName = attributeName;
            }

            @Data
            private class SingleValue {

                private String attributeValue;
            }
        }
    }

    public void setEmail(String email) {
        this.email = email;
        getExtendedMap().getEmailHolder().getSingleValue().setAttributeValue(email);
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        //getExtendedMap().getPhoneHolder().getSingleValue().setAttributeValue(phoneNumber);
    }
}
