package ru.alamics.sso.settings;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.alamics.sso.jpa.entity.common.SettingType;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SettingsDto implements Serializable {

    private static final long serialVersionUID = -3769044840267241943L;
    private String name;
    private String id;
    private String extId;
    private String value;
    private String desc;
    private String realmId;
    private TimeUnit unit;
    private SettingType type;
}
