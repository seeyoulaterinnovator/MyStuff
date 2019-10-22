package ru.alamics.sso.keycloak.repository;

import ru.alamics.sso.keycloak.entity.ImportUserHistoryEntity;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Date;
import java.util.List;

@LocalBean
@Stateless
public class ImportUserHistoryRepository {

    @PersistenceContext
    private EntityManager em;

    public ImportUserHistoryEntity findImportUserHistory(final String importId) {
        return em.find(ImportUserHistoryEntity.class, importId);
    }

    public List<ImportUserHistoryEntity> findAllImportUserHistoryEntities(String realmId) {
        return em.createQuery(
                "select ire " +
                "from ImportUserHistoryEntity ire where ire.realmId = :realmId ", ImportUserHistoryEntity.class)
                .setParameter("realmId", realmId)
                .getResultList();
    }

    public List<ImportUserHistoryEntity> findAllImportUserHistoryEntities() {
        return em.createQuery(
                "select ire " +
                        "from ImportUserHistoryEntity ire ", ImportUserHistoryEntity.class)
                .getResultList();
    }

    public ImportUserHistoryEntity saveImportUserHistory(ImportUserHistoryEntity importUserHistoryEntity) {
        em.persist(importUserHistoryEntity);
        em.flush();
        return importUserHistoryEntity;
    }

    public ImportUserHistoryEntity updateImportUserHistory(ImportUserHistoryEntity importUserHistoryEntity) {
        em.merge(importUserHistoryEntity);
        em.flush();
        return importUserHistoryEntity;
    }
}
