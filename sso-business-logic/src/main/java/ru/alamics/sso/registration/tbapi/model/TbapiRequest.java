package ru.alamics.sso.registration.tbapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Setter;

import static ru.alamics.sso.registration.model.TbapiConstants.*;

@Data
public class TbapiRequest {

    final private CustomerCategory customerCategory = new CustomerCategory();

    private ExtendedMap extendedMap = new ExtendedMap();

    @JsonIgnore
    private String email;

    private String name;

    //getExtendedMap().getPhoneHolder().getSingleValue().setAttributeValue(phoneNumber);
    @Setter
    private String phoneNumber;

    private String legalName;

    @Data
    public static class CustomerCategory {

        final private String id = "9149000490413788409";

        final private String name = "B2R";
    }

    @Data
    public static class ExtendedMap {

        //@JsonProperty(TBAPI_PHONE_ID)
        //private MapObjectHolder phoneHolder = new MapObjectHolder(TBAPI_PHONE_NAME);

        @JsonProperty(TBAPI_EMAIL_ID)
        private MapObjectHolder emailHolder = new MapObjectHolder(TBAPI_EMAIL_NAME);

        @Data
        public static class MapObjectHolder {

            private int attributeType = 0;
            private String attributeName;
            private SingleValue singleValue = new SingleValue();

            public MapObjectHolder(String attributeName) {
                this.attributeName = attributeName;
            }

            @Data
            public static class SingleValue {

                private String attributeValue;
            }
        }
    }

    public void setEmail(String email) {
        this.email = email;
        getExtendedMap().getEmailHolder().getSingleValue().setAttributeValue(email);
    }
}
