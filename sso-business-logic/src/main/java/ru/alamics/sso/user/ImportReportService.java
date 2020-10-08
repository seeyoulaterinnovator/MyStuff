package ru.alamics.sso.user;

import lombok.extern.slf4j.Slf4j;
import ru.alamics.sso.jpa.entity.ImportUsersDataEntity;
import ru.alamics.sso.jpa.entity.ImportUsersReportEntity;
import ru.alamics.sso.jpa.entity.common.ImportUsersReportStatus;
import ru.alamics.sso.jpa.repository.ImportUsersReportRepository;
import ru.alamics.sso.registration.mapper.DataMapper;
import ru.alamics.sso.user.model.ImportUsersDataModel;
import ru.alamics.sso.user.model.ImportUsersReportModel;

import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Stateless
@LocalBean
public class ImportReportService {

    @EJB
    private ImportUsersReportRepository importUsersReportRepository;

    public void setReportStatus(ImportUsersReportModel report, ImportUsersReportStatus status) {

        importUsersReportRepository.setReportStatus(report.getId(), status);
    }


    public List<ImportUsersReportModel> getReportListByStatus(ImportUsersReportStatus status) {

        List<ImportUsersReportEntity> list = importUsersReportRepository.getReportListByStatus(status);

        return list.stream().map(DataMapper::toReportModel).collect(Collectors.toList());
    }

    public List<ImportUsersDataModel> getDataList(ImportUsersReportModel report) {

        List<ImportUsersDataEntity> list = importUsersReportRepository.getDataByReportId(report.getId());

        return list.stream().map(DataMapper::toDataModel).collect(Collectors.toList());
    }

    public void setReportDone(ImportUsersReportModel report) {

        importUsersReportRepository.setReportDone(report.getId(), report.getCountClones(), report.getCountCreatedUsers());
    }

    public void updateImportUsersData(ImportUsersDataModel data) {

        ImportUsersDataEntity entity = importUsersReportRepository.findImportUsersDataById(data.getId());

        entity.setFirstName(data.getFirstName());
        entity.setEmail(data.getEmail());
        entity.setPhone(data.getPhone());
        entity.setRole(data.getRole());
        entity.setSystems(data.getSystems());
        entity.setCreated(data.isCreated());
        entity.setUserId(data.getUserId());
        entity.setErrors(data.getErrors());
    }
}
