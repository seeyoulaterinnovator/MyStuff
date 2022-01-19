package ru.alamics.sso.keycloak.cities.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CityDadataModel {

    private LocationModel location;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public class LocationModel {

        private String value;

        @JsonIgnoreProperties(ignoreUnknown = true)
        private DataModel data;

        @Data
        @JsonIgnoreProperties(ignoreUnknown = true)
        public class DataModel {

            private String city;

        }

    }

}
