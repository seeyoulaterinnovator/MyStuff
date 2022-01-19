package ru.alamics.sso.user;

import org.keycloak.models.RealmModel;
import ru.alamics.sso.jpa.entity.ImportUsersDataEntity;
import ru.alamics.sso.jpa.entity.ImportUsersReportEntity;
import ru.alamics.sso.jpa.entity.common.ImportUsersReportStatus;
import ru.alamics.sso.jpa.repository.ImportUsersReportRepository;
import ru.alamics.sso.user.mapper.UserMapper;
import ru.alamics.sso.user.model.ImportUsersDataModel;
import ru.alamics.sso.user.model.ImportUsersReportModel;
import ru.alamics.sso.user.web.ImportUsersReportDto;
import ru.alamics.sso.util.Util;

import javax.ejb.*;
import java.util.List;

@Stateless
@LocalBean
public class ImportUsersReportService {
    @EJB
    private ImportReportService importReportService;

    public String  createImportUsersReportAsync(RealmModel realm, String filename, List<ImportUsersDataModel> dataList) {

        ImportUsersReportModel importUsersReport = UserMapper.toImportUsersReportEntity(realm.getName(), filename, dataList);

        importUsersReport.setFiletype(Util.getFileExtByFilename(filename));
        importUsersReport.setStatus(ImportUsersReportStatus.UPLOADING);
        String reportId = importReportService.saveImportUsersReport(importUsersReport);
        importUsersReport.setId(reportId);
        importReportService.saveImportUsersData(reportId, dataList); // TODO really need?
        return reportId;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public ImportUsersReportModel createImportUsersReport(RealmModel realm, String filename, List<ImportUsersDataModel> dataList) {

        ImportUsersReportModel importUsersReport = UserMapper.toImportUsersReportEntity(realm.getName(), filename, dataList);
        importUsersReport.setStatus(ImportUsersReportStatus.IN_PROGRESS);
        importUsersReport.setFiletype(Util.getFileExtByFilename(filename));

        String reportId = importReportService.saveImportUsersReport(importUsersReport);
        importUsersReport.setId(reportId);
        importReportService.saveImportUsersData(reportId, dataList); // TODO really need?

        return importUsersReport;
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void updateReportStatus(ImportUsersReportModel reportModel) {

        importReportService.updateReport(reportModel);
    }
}
