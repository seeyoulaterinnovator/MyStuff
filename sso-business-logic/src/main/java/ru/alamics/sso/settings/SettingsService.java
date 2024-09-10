package ru.alamics.sso.settings;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.jpa.entity.Settings;
import ru.alamics.sso.jpa.entity.common.SettingType;
import ru.alamics.sso.jpa.repository.SettingsRepository;
import ru.alamics.sso.registration.mapper.DataMapper;
import ru.alamics.sso.schedule.ImportSchedule;
import ru.alamics.sso.schedule.UserSchedule;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class SettingsService {
    @Inject
    SettingsRepository repository;

    @Inject
    UserSchedule userSchedule;

    @Inject
    ImportSchedule importSchedule;

    public List<SettingsDto> getRealmSettings(final String realmId) {
        List<SettingsDto> ret = repository.findRealmSettings(realmId)
                .stream()
                .map(DataMapper::toDto)
                .collect(Collectors.toList());
        return ret;
    }

    public List<SettingsDto> getRealmSettings(String realmId, SettingType type) {
        return repository.findRealmSettings(realmId)
                .stream()
                .map(DataMapper::toDto)
                .collect(Collectors.toList());
    }

    public void deleteSetting(final String settingId) {
        repository.deleteSetting(settingId);
    }

    public SettingsDto save(final SettingsDto settings) {
        Settings settingsToSave = Settings.builder()
                .desc(settings.getDesc())
                .name(settings.getName())
                .id(settings.getId())
                .value(settings.getValue())
                .extId(settings.getExtId())
                .realmId(settings.getRealmId())
                .unit(settings.getUnit())
                .type(settings.getType())
                .build();


        Settings save = repository.save(settingsToSave);
        //При вызове метода save с Админконсоли для времени шедулера мы обновляем таймер
        if (SettingConstants.TIMER_INTERVAL_DURATION_PROPERTY.getKey().equals(save.getExtId())) {
            userSchedule.changeScheduleTimer();
            importSchedule.changeScheduleTimer();
        }
        return DataMapper.toDto(save);
    }

    public long getSettingsLongValue(final SettingConstants property, final String realmId) {
        final String keyName = property.getKey();
        Settings settings = repository.getSettings(keyName, realmId);
        long ret = -1;
        if (settings != null) {
            try {
                ret = TimeUnit.SECONDS.convert(Long.parseLong(settings.getValue()), settings.getUnit());
            } catch (Exception e) {
                log.error("Failed convert time settings. Default value={}", ret);
            }
        }
        return ret;
    }

    public int getSettingsIntValue(final SettingConstants property, final String realmId) {
        return (int) getSettingsLongValue(property, realmId);
    }

    public String getSettingsStringValue(final String property, final String realmId) {
        Settings settings = repository.getSettings(property, realmId);
        String value = "no settings";
        if (settings != null) {
            value = settings.getValue();
        }
        return value;
    }

    public String getSettingsStringValue(final SettingConstants property, final String realmId) {
        return getSettingsStringValue(property.getKey(), realmId);
    }

    public Integer getSettingsIntegerValue(final String property, final String realmId, Integer defValue, String logDefault) {
        Settings settings = repository.getSettings(property, realmId);
        try {
            return Integer.parseInt(settings.getValue());
        } catch (NumberFormatException nfe) {
            if (logDefault != null)
                log.info(logDefault, property, defValue);
            return defValue;
        }
    }

    public Integer getSettingsIntegerValue(final SettingConstants property, final String realmId, Integer defValue, String logDefault) {
        return getSettingsIntegerValue(property.getKey(), realmId, defValue, logDefault);
    }

    public SettingsDto getSetting(final SettingConstants property, final String realmId) {
        final String keyName = property.getKey();
        return DataMapper.toDto(repository.getSettings(keyName, realmId));
    }
}
