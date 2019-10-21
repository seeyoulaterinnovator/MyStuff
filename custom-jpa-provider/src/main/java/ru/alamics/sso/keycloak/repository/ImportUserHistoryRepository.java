package ru.alamics.sso.keycloak.repository;

import ru.alamics.sso.keycloak.entity.ImportUserHistoryEntity;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@LocalBean
@Stateless
public class ImportUserHistoryRepository {

    @PersistenceContext
    private EntityManager em;

    public ImportUserHistoryEntity findImportReport(final String importId) {
        return em.find(ImportUserHistoryEntity.class, importId);
    }

    public List<ImportUserHistoryEntity> findAllImportReports(String realmId) {
        return em.createQuery(
                "select ire " +
                "from ImportReportEntity ire where ire.realmId = :realmId ", ImportUserHistoryEntity.class)
                .setParameter("realmId", realmId)
                .getResultList();
    }

    public ImportUserHistoryEntity saveImportReport(ImportUserHistoryEntity importUserHistoryEntity) {
        em.persist(importUserHistoryEntity);
        em.flush();
        return importUserHistoryEntity;
    }
}
