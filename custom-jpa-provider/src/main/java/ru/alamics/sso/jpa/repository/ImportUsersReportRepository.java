package ru.alamics.sso.jpa.repository;

import lombok.extern.slf4j.Slf4j;
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
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
@Slf4j
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
        log.info("import report with id = {} saved, status = {}", importUsersReportEntity.getId(), importUsersReportEntity.getStatus());
        em.persist(importUsersReportEntity);
        em.flush();
    }

    public void saveImportUsersData(ImportUsersDataEntity entity) {
        log.info("importUserData with id = {} saved", entity.getId());
        em.persist(entity);
        em.flush();
    }

    public void updateImportUsersData(ImportUsersDataEntity entity) {

        em.createQuery("update ImportUsersDataEntity data " +
                "set data.status = :status, data.isCreated = :isCreated, data.userId = :userId, data.errors = : errors, data.personalAccountUser = :personalAccount" +
                        " where data.id = :id")
                .setParameter("id", entity.getId())
                .setParameter("status", entity.getStatus())
                .setParameter("isCreated", entity.isCreated())
                .setParameter("errors", entity.getErrors())
                .setParameter("userId", entity.getUserId())
                .setParameter("personalAccount", entity.getPersonalAccountUser())
                .executeUpdate();

        refreshEntityById(entity.getId(), ImportUsersDataEntity.class);

    }

    public void setReportStatus(String id, ImportUsersReportStatus status) {

        em.createQuery("update ImportUsersReportEntity rep set rep.status = :status where rep.id = :id")
                .setParameter("status", status)
                .setParameter("id", id)
                .executeUpdate();

        refreshEntityById(id, ImportUsersReportEntity.class);
    }

    public void updateReport(String id, ImportUsersReportStatus status, int clones, int created) {
        log.info("report with id = {} was updated", id);
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
