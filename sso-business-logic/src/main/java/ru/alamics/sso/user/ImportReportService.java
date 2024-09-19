package ru.alamics.sso.user;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.jpa.entity.ImportUsersDataEntity;
import ru.alamics.sso.jpa.entity.ImportUsersReportEntity;
import ru.alamics.sso.jpa.entity.common.ImportUsersDataStatus;
import ru.alamics.sso.jpa.entity.common.ImportUsersReportStatus;
import ru.alamics.sso.jpa.repository.ImportUsersReportRepository;
import ru.alamics.sso.registration.mapper.DataMapper;
import ru.alamics.sso.user.mapper.UserMapper;
import ru.alamics.sso.user.model.ImportUsersDataModel;
import ru.alamics.sso.user.model.ImportUsersReportModel;
import ru.alamics.sso.user.web.ImportUsersReportDto;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
@Slf4j
public class ImportReportService {

    @Inject
    ImportUsersReportRepository importUsersReportRepository;

    public void setReportStatus(ImportUsersReportModel report, ImportUsersReportStatus status) {

        importUsersReportRepository.setReportStatus(report.getId(), status);
    }


    public List<ImportUsersReportModel> getReportListByStatus(ImportUsersReportStatus status) {

        return importUsersReportRepository.getReportListByStatus(status).stream()
                .map(DataMapper::toReportModel)
                .collect(Collectors.toList());
    }

    public List<ImportUsersDataModel> getDataList(String reportId) {

        return importUsersReportRepository.getDataByReportId(reportId).stream()
                .map(DataMapper::toDataModel)
                .collect(Collectors.toList());
    }

    public List<ImportUsersDataModel> getDataListAwaiting(String reportId) {

        return importUsersReportRepository.getDataByReportIdAndStatus(reportId, ImportUsersDataStatus.AWAITING).stream()
                .map(DataMapper::toDataModel)
                .collect(Collectors.toList());
    }

    public void updateReport(ImportUsersReportModel report) {

        importUsersReportRepository.updateReport(report.getId(), report.getStatus(), report.getCountClones(), report.getCountCreatedUsers());
    }

    public void updateUploaded(String id) {

        importUsersReportRepository.setReportStatus(id, ImportUsersReportStatus.AWAITING);
    }

    public void updateImportUsersData(ImportUsersDataModel data) {

        ImportUsersDataEntity entity = importUsersReportRepository.findImportUsersDataById(data.getId());

        if (entity == null) {
            log.info("updateImportUsersData dataEntity = " + data.getId() + ", entity " + entity);
            return;
        }

        entity.setFirstName(data.getFirstName());
        entity.setEmail(data.getEmail());
        entity.setPhone(data.getPhone());
        entity.setRole(data.getRole());
        entity.setSystems(data.getSystems());
        entity.setCreated(data.isCreated());
        entity.setUserId(data.getUserId());
        entity.setErrors(data.getErrors());
        entity.setStatus(data.getStatus());
        entity.setPersonalAccountUser(data.getPersonalAccountUser());

        importUsersReportRepository.updateImportUsersData(entity);

    }

    public String saveImportUsersReport(ImportUsersReportModel report) {
        if (report == null) {
            return null;
        }

        ImportUsersReportEntity importUsersReportEntity = DataMapper.newReportEntity(report);

        importUsersReportRepository.saveImportUsersReport(importUsersReportEntity);

        return importUsersReportEntity.getId();
    }

    public void saveImportUsersData(String reportId, List<ImportUsersDataModel> dataList) {
        if (dataList == null) {
            return;
        }

        for (ImportUsersDataModel data : dataList) {

            data.setReportId(reportId);

            ImportUsersDataEntity importUsersdataEntity = DataMapper.newDataEntity(data);

            importUsersReportRepository.saveImportUsersData(importUsersdataEntity);
            data.setId(importUsersdataEntity.getId());
        }
    }

    public List<ImportUsersReportDto> findImportUsersReportsByRealmId(String realmId, int first, int max) {
        return UserMapper.toImportUsersReportDtos(importUsersReportRepository.findImportUsersReports(realmId, first, max));
    }

    public ImportUsersReportEntity findImportUsersReportByImportId(String importId) {
        return importUsersReportRepository.findImportUsersReportByImportId(importId);
    }

    public List<ImportUsersDataEntity> findImportUsersDataByImportId(String importId) {
        return importUsersReportRepository.findImportUsersDataByImportId(importId);
    }
}
