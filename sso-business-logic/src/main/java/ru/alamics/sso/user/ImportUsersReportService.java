package ru.alamics.sso.user;

import org.keycloak.models.RealmModel;
import ru.alamics.sso.jpa.entity.ImportUsersDataEntity;
import ru.alamics.sso.jpa.entity.ImportUsersReportEntity;
import ru.alamics.sso.jpa.entity.common.ImportUsersReportStatus;
import ru.alamics.sso.jpa.repository.ImportUsersReportRepository;
import ru.alamics.sso.user.mapper.UserMapper;
import ru.alamics.sso.user.web.ImportUsersReportDto;
import ru.alamics.sso.util.Util;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.util.List;

@Stateless
@LocalBean
public class ImportUsersReportService {
    @EJB
    private ImportUsersReportRepository importUsersReportRepository;

    public void createImportUsersReportAsync(RealmModel realm, String filename, List<ImportUsersDataEntity> dataList) {

        ImportUsersReportEntity importUsersReport = UserMapper.toImportUsersReportEntity(realm.getName(), filename, dataList);
        importUsersReport.setStatus(ImportUsersReportStatus.AWAITING);
        importUsersReport.setFiletype(Util.getFileExtByFilename(filename));

        saveImportUsersReport(importUsersReport);
    }

    public ImportUsersReportEntity createImportUsersReport(RealmModel realm, String filename, List<ImportUsersDataEntity> dataList) {

        ImportUsersReportEntity importUsersReport = UserMapper.toImportUsersReportEntity(realm.getName(), filename, dataList);
        importUsersReport.setStatus(ImportUsersReportStatus.IN_PROGRESS);
        importUsersReport.setFiletype(Util.getFileExtByFilename(filename));

        saveImportUsersReport(importUsersReport);

        return importUsersReport;
    }

    public void saveImportUsersReport(ImportUsersReportEntity importUsersReportEntity) {
        if (importUsersReportEntity == null) {
            return;
        }
        importUsersReportRepository.saveImportUsersReport(importUsersReportEntity);
    }

    public List<ImportUsersReportDto> findImportUsersReportsByRealmId(String realmId) {
        return UserMapper.toImportUsersReportDtos(importUsersReportRepository.findImportUsersReports(realmId));
    }

    public ImportUsersReportEntity findImportUsersReportByImportId(String importId) {
        return importUsersReportRepository.findImportUsersReportByImportId(importId);
    }

    public void updateImportUsersData(ImportUsersDataEntity data) {
        importUsersReportRepository.updateImportUsersData(data);
    }
}
