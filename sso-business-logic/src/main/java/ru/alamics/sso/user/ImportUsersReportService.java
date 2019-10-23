package ru.alamics.sso.user;

import ru.alamics.sso.keycloak.entity.ImportUsersReportEntity;
import ru.alamics.sso.keycloak.repository.ImportUsersReportRepository;
import ru.alamics.sso.user.mapper.UserMapper;
import ru.alamics.sso.user.web.ImportUserHistoryDto;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.util.List;

@Stateless
@LocalBean
public class ImportUsersReportService {
    @EJB
    private ImportUsersReportRepository importUsersReportRepository;

    public void saveImportUsersReport(ImportUsersReportEntity importUsersReportEntity) {
        if (importUsersReportEntity == null) {
            return;
        }
        importUsersReportRepository.saveImportUsersReport(importUsersReportEntity);
    }

    public List<ImportUserHistoryDto> findImportUsersReportsByRealmId(String realmId) {
        return UserMapper.toImportUserHistoryDtos(importUsersReportRepository.findImportUsersReports(realmId));
    }

    public ImportUsersReportEntity findImportUsersReportByImportId(String importId) {
        return importUsersReportRepository.findImportUsersReportByImportId(importId);
    }
}
