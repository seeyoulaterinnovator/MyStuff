package ru.alamics.sso.jpa.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import org.hibernate.jpa.QueryHints;
import ru.alamics.sso.jpa.entity.Settings;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class SettingsRepository {
    @Inject
    private EntityManager em;

    public Settings getSettings(final String keyName, final String realmId) {
        Settings ret = null;
        if (keyName != null) {
            List<Settings> values = em.createQuery("select s from Settings s where s.extId = :extId and s.realmId =:realmId", Settings.class)
                    .setParameter("extId", keyName)
                    .setParameter("realmId", realmId)
                    .setHint(QueryHints.HINT_READONLY, true)
                    .getResultList();
            if (!values.isEmpty()) {
                ret = values.get(0);
            }
        }
        return ret;
    }


    public List<Settings> findRealmSettings(final String realmId) {

        return em.createQuery("select s from Settings s where s.realmId =:realmId", Settings.class)
                .setParameter("realmId", realmId)
                .getResultList();
    }

    public void deleteSetting(final String settingId) {
        em.createQuery("delete from Settings s where s.id =: settingId")
                .setParameter("settingId", settingId)
                .executeUpdate();
    }


    public Settings save(Settings settings) {
        if(settings != null) {
            if(settings.getId() == null) {
                settings.setId(UUID.randomUUID().toString());
                em.persist(settings);
            } else {
                em.createQuery("update Settings s set s.value =:val where s.id=:settingId")
                        .setParameter("val", settings.getValue())
                        .setParameter("settingId", settings.getId())
                        .executeUpdate();
                em.createQuery("update Settings s set s.unit =:unit where s.id=:settingId")
                        .setParameter("unit", settings.getUnit())
                        .setParameter("settingId", settings.getId())
                        .executeUpdate();
            }

            em.flush();
        }
        return settings;
    }
}
