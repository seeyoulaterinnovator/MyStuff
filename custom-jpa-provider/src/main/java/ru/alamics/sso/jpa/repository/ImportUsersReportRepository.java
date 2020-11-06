package ru.alamics.sso.jpa.repository;

import ru.alamics.sso.jpa.entity.ImportUsersDataEntity;
import ru.alamics.sso.jpa.entity.ImportUsersReportEntity;
import ru.alamics.sso.jpa.entity.common.ImportUsersDataStatus;
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

    public List<ImportUsersDataEntity> findImportUsersDataByImportId(final String importId) {
        return em.createQuery(
                "select d " +
                        "from ImportUsersDataEntity d where d.importUsersReport = :importId "
                , ImportUsersDataEntity.class)
                .setParameter("importId", importId)
                .getResultList();
    }

    public ImportUsersDataEntity findImportUsersDataById(String id) {
        return em.find(ImportUsersDataEntity.class, id);
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

    public void saveImportUsersReport(ImportUsersReportEntity importUsersReportEntity) {

        em.persist(importUsersReportEntity);
        em.flush();
    }

    public void saveImportUsersData(ImportUsersDataEntity entity) {

        em.persist(entity);
        em.flush();
    }

    public ImportUsersReportEntity updateImportUsersReport(ImportUsersReportEntity importUsersReportEntity) {
        //em.merge(importUsersReportEntity);
        //em.flush();
        return importUsersReportEntity;
    }

    public ImportUsersDataEntity updateImportUsersData(ImportUsersDataEntity entity) {
        //em.merge(entity);
        //em.flush();
        return entity;
    }

    public void setReportStatus(String id, ImportUsersReportStatus status) {

        em.createQuery("update ImportUsersReportEntity rep set rep.status = :status where rep.id = :id")
                .setParameter("status", status)
                .setParameter("id", id)
                .executeUpdate();

        refreshEntityById(id, ImportUsersReportEntity.class);
    }

    public void updateReport(String id, ImportUsersReportStatus status, int clones, int created) {

        em.createQuery("update ImportUsersReportEntity rep " +
                "set rep.status = :status, rep.countClones = :clones, rep.countCreatedUsers = :created where rep.id = :id")
                .setParameter("id", id)
                .setParameter("status", status)
                .setParameter("clones", clones)
                .setParameter("created", created)
                .executeUpdate();

        refreshEntityById(id, ImportUsersReportEntity.class);
    }

    public <T> T findEntityById(String id, Class<T> clazz) {
        return em.find(clazz, id);
    }

    public <T> void refreshEntityById(String id, Class<T> clazz) {

        T en = findEntityById(id, clazz);
        em.refresh(en);
    }

    public List<ImportUsersReportEntity> getReportListByStatus(ImportUsersReportStatus status) {

        return em.createQuery("select rep from ImportUsersReportEntity rep where rep.status = :status", ImportUsersReportEntity.class)
                .setParameter("status", status)
                .getResultList();
    }

    public List<ImportUsersDataEntity> getDataByReportId(String reportId) {

        return em.createQuery("select data from ImportUsersDataEntity data where data.importUsersReport = :id", ImportUsersDataEntity.class)
                .setParameter("id", reportId)
                .getResultList();
    }

    public List<ImportUsersDataEntity> getDataByReportIdAndStatus(String reportId, ImportUsersDataStatus status) {

        return em.createQuery("select data from ImportUsersDataEntity data where data.importUsersReport = :id and data.status=:status", ImportUsersDataEntity.class)
                .setParameter("id", reportId)
                .setParameter("status", status)
                .getResultList();
    }
}
