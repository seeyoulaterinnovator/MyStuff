package ru.alamics.sso.remote.rias.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.xml.bind.annotation.XmlType;
import lombok.Data;
import lombok.ToString;

import jakarta.xml.bind.annotation.XmlElement;

@XmlType
@Data
public class RiasResult {
    @XmlElement(name = "check_profile_data")
    @JsonProperty("check_profile_data")
    private Integer checkProfileData;
}
