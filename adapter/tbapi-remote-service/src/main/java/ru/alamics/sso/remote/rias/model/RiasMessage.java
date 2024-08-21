package ru.alamics.sso.remote.rias.model;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import lombok.*;

@XmlType
@Data
public class RiasMessage {
    @XmlElement
    private String code;

    @XmlElement
    private String text;
}
