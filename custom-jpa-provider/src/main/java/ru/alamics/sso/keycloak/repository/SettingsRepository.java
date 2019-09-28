package ru.alamics.sso.keycloak.repository;

import org.hibernate.jpa.QueryHints;
import ru.alamics.sso.keycloak.entity.Settings;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Stateless
@LocalBean
public class SettingsRepository {

    @PersistenceContext
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

        List<Settings> ret = em.createQuery("select s from Settings s where s.realmId =:realmId", Settings.class)
                .setParameter("realmId", realmId)
                .getResultList();

        return ret;
    }

    public void deleteSetting(final String settingId) {
        em.createQuery("delete from Settings s where s.id =: settingId")
                .setParameter("settingId", settingId)
                .executeUpdate();
    }


    public Settings save(Settings settings) {
        if(settings != null) {
            if(settings.getId() == null) {
                em.persist(settings);
            } else {
                em.merge(settings);
            }

            em.flush();
        }
        return settings;
    }
}
