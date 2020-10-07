package ru.alamics.sso.jpa.repository;

import ru.alamics.sso.jpa.entity.ImportUsersDataEntity;
import ru.alamics.sso.jpa.entity.ImportUsersReportEntity;
import ru.alamics.sso.jpa.entity.common.ImportUsersReportStatus;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@LocalBean
@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ImportUsersReportRepository {

    @PersistenceContext
    private EntityManager em;

    public ImportUsersReportEntity findImportUsersReportByImportId(final String importId) {
        return em.find(ImportUsersReportEntity.class, importId);
    }

    public List<ImportUsersReportEntity> findImportUsersReports(String realmId) {
        return em.createQuery(
                "select ire " +
                "from ImportUsersReportEntity ire where ire.realmId = :realmId " +
                        "order by ire.importDate desc ", ImportUsersReportEntity.class)
                .setParameter("realmId", realmId)
                .getResultList();
    }

    public List<ImportUsersReportEntity> findAllImportUsersReports() {
        return em.createQuery(
                "select ire " +
                        "from ImportUsersReportEntity ire ", ImportUsersReportEntity.class)
                .getResultList();
    }

    public ImportUsersReportEntity saveImportUsersReport(ImportUsersReportEntity importUsersReportEntity) {
        em.persist(importUsersReportEntity);
        em.flush();
        return importUsersReportEntity;
    }

    public ImportUsersReportEntity updateImportUsersReport(ImportUsersReportEntity importUsersReportEntity) {
        em.merge(importUsersReportEntity);
        em.flush();
        return importUsersReportEntity;
    }

    public ImportUsersDataEntity updateImportUsersData(ImportUsersDataEntity entity) {
        em.merge(entity);
        em.flush();
        return entity;
    }

    public void setReportStatus(String id, ImportUsersReportStatus status) {

        em.createQuery("update ImportUsersReportEntity rep set rep.status = :status where rep.id = :id")
                .setParameter("status", status.getDiscription())
                .setParameter("id", id)
                .executeUpdate();
    }
}
