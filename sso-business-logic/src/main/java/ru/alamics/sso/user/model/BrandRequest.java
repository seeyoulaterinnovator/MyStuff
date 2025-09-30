package ru.alamics.sso.user.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BrandRequest {
    private String brandName;
    private String brandCode;
    private String markBrandId;
}
