package ru.alamics.sso.remote.rias.model;

import jakarta.xml.bind.annotation.XmlElement;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "data")
@Data
public class RiasData {
    @XmlElement
    private Integer status;

    @XmlElement
    private RiasResult result;

    @XmlElement
    private RiasMessage messages;
}
