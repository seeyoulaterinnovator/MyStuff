package ru.alamics.sso.settings;

import ru.alamics.sso.keycloak.entity.Settings;
import ru.alamics.sso.keycloak.repository.SettingsRepository;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.util.List;
import java.util.stream.Collectors;

@Stateless
@LocalBean
public class SettingsService {


    @EJB
    private SettingsRepository repository;

    public List<SettingsDto> getRealmSettings(final String realmId) {

        List<SettingsDto> ret = repository.findRealmSettings(realmId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        return ret;
    }

    public void deleteSetting(final String settingId) {
        repository.deleteSetting(settingId);
    }

    public SettingsDto save(final SettingsDto settings) {
        var settingsToSave = Settings.builder()
                .desc(settings.getDesc())
                .id(settings.getId())
                .value(settings.getValue())
                .extId(settings.getExtId())
                .realmId(settings.getRealmId())
                .build();

        return toDto(repository.save(settingsToSave));
    }

    private SettingsDto toDto(Settings settings) {
        return SettingsDto.builder()
                .desc(settings.getDesc())
                .extId(settings.getExtId())
                .id(settings.getId())
                .value(settings.getValue())
                .realmId(settings.getRealmId())
                .build();
    }
}
