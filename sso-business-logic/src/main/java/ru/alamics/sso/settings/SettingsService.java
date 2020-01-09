package ru.alamics.sso.settings;

import ru.alamics.sso.keycloak.entity.Settings;
import ru.alamics.sso.keycloak.repository.SettingsRepository;
import ru.alamics.sso.registration.mapper.DataMapper;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Stateless
@LocalBean
public class SettingsService {

    @EJB
    private SettingsRepository repository;

    public List<SettingsDto> getRealmSettings(final String realmId) {

        List<SettingsDto> ret = repository.findRealmSettings(realmId)
                .stream()
                .map(DataMapper::toDto)
                .collect(Collectors.toList());
        return ret;
    }

    public void deleteSetting(final String settingId) {
        repository.deleteSetting(settingId);
    }

    public SettingsDto save(final SettingsDto settings) {
        var settingsToSave = Settings.builder()
                .desc(settings.getDesc())
                .name(settings.getName())
                .id(settings.getId())
                .value(settings.getValue())
                .extId(settings.getExtId())
                .realmId(settings.getRealmId())
                .unit(settings.getUnit())
                .build();

        return DataMapper.toDto(repository.save(settingsToSave));
    }

    public long getSettingsValue(final SettingConstants property, final String realmId) {
        final String keyName = property.getKey();
        Settings settings = repository.getSettings(keyName, realmId);
        long ret = -1;
        if (settings != null) {
            ret = TimeUnit.SECONDS.convert(Long.parseLong(settings.getValue()), settings.getUnit());
        }
        return ret;
    }

    public SettingsDto getSetting(final SettingConstants property, final String realmId) {
        final String keyName = property.getKey();
        return DataMapper.toDto(repository.getSettings(keyName, realmId));
    }
}
