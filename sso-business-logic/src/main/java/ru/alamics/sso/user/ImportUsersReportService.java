package ru.alamics.sso.user;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.*;
import jakarta.transaction.Transactional;
import org.keycloak.models.RealmModel;
import ru.alamics.sso.jpa.entity.common.ImportUsersReportStatus;
import ru.alamics.sso.user.mapper.UserMapper;
import ru.alamics.sso.user.model.ImportUsersDataModel;
import ru.alamics.sso.user.model.ImportUsersReportModel;
import ru.alamics.sso.util.Util;

import java.util.List;

@ApplicationScoped
public class ImportUsersReportService {
    @Inject
    ImportReportService importReportService;

    public String createImportUsersReportAsync(RealmModel realm, String filename, List<ImportUsersDataModel> dataList) {

        ImportUsersReportModel importUsersReport = UserMapper.toImportUsersReportEntity(realm.getName(), filename, dataList);

        importUsersReport.setFiletype(Util.getFileExtByFilename(filename));
        importUsersReport.setStatus(ImportUsersReportStatus.UPLOADING);
        String reportId = importReportService.saveImportUsersReport(importUsersReport);
        importUsersReport.setId(reportId);
        importReportService.saveImportUsersData(reportId, dataList);
        return reportId;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public ImportUsersReportModel createImportUsersReport(RealmModel realm, String filename, List<ImportUsersDataModel> dataList) {

        ImportUsersReportModel importUsersReport = UserMapper.toImportUsersReportEntity(realm.getName(), filename, dataList);
        importUsersReport.setStatus(ImportUsersReportStatus.IN_PROGRESS);
        importUsersReport.setFiletype(Util.getFileExtByFilename(filename));

        String reportId = importReportService.saveImportUsersReport(importUsersReport);
        importUsersReport.setId(reportId);
        importReportService.saveImportUsersData(reportId, dataList);

        return importUsersReport;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void updateReportStatus(ImportUsersReportModel reportModel) {

        importReportService.updateReport(reportModel);
    }
}
