package ru.alamics.sso.registration.tbapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import static ru.alamics.sso.registration.model.TbapiConstants.*;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TbapiResponse {

    private String id;
    private String name;
    private ExtendedMap extendedMap;

    private String businessErrorCode;
    private String userMessage;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class ExtendedMap {

        @JsonProperty(TBAPI_DMP_CUSTOMER_ID)
        private MapObjectHolder customerHolder;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public class MapObjectHolder {

            private Integer attributeType;
            private String attributeName;
            private SingleValue singleValue;

            @Data
            @JsonIgnoreProperties(ignoreUnknown = true)
            public class SingleValue {

                private String attributeValue;
                private String attributeValueName;
            }
        }
    }
}
