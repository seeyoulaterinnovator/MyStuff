package ru.alamics.sso.user.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AttributeRequest implements Serializable {
    private static final long serialVersionUID = 4197709070688701449L;
    private String name;
    private String value;
}
