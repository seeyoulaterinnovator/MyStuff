package ru.alamics.sso.user;

import org.keycloak.models.RealmModel;
import ru.alamics.sso.jpa.entity.ImportUsersReportEntity;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.registration.service.UserFindService;
import ru.alamics.sso.user.filetype.FileFactory;
import ru.alamics.sso.user.filetype.FileModel;
import ru.alamics.sso.user.mapper.UserMapper;
import ru.alamics.sso.user.model.DownloadUserRequest;
import ru.alamics.sso.user.model.ImportUsersDataModel;
import ru.alamics.sso.user.model.UserParameter;
import ru.alamics.sso.user.web.UserSearchDto;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class ExportWorker {

    private final RealmModel realm;

    private final UserFindService userFindService;
    private final ImportReportService importReportService;

    public ExportWorker(RealmModel realm) {
        this.realm = realm;

        this.userFindService = Lookup.lookup(UserFindService.class);
        this.importReportService = Lookup.lookup(ImportReportService.class);
    }

    public byte[] exportUsers(DownloadUserRequest userRequest) throws IOException {

        FileModel file = FileFactory.createFileModel(userRequest.getType());

        List<UserSearchDto> userDto = userFindService.getUsersByParametersWithoutGrouping(
                realm.getName(), null, null, null,
                null, true, 1, 1000, Arrays.asList(userRequest.getUserIds()));

        if (userDto == null || userDto.isEmpty()) {
            return null;
        }

        userDto = UserMapper.toGroupUserDtos(userDto);
        file.addRow(UserServiceUtil.getUserParameterNames(userRequest.getUserParameters()));
        userDto.stream().forEach(o -> file.addRow(UserServiceUtil.getUserParameters(o, userRequest.getUserParameters())));
        return file.save();
    }

    public FileModel downloadUsersByImportReportId(String importId) throws IOException {
        ImportUsersReportEntity importUsersReport = importReportService.findImportUsersReportByImportId(importId);
        FileModel file = FileFactory.createFileModel(importUsersReport.getName().substring(importUsersReport.getName().lastIndexOf(".") + 1));

        List<String> userParameterNames = UserServiceUtil.getUserParameterNames(UserParameter.values());
        List<String> finishParameterNames = userParameterNames.stream().skip(1).limit(userParameterNames.size() - 2).collect(Collectors.toList());
        finishParameterNames.addAll(Arrays.asList("Статус импорта", "Ошибки"));
        file.addRow(finishParameterNames);

        List<ImportUsersDataModel> dataList = importReportService.getDataList(importUsersReport.getId());

        dataList.forEach(o -> {
            List<String> list = new LinkedList<>();
            list.add(o.getFirstName());
            list.add(o.getEmail());
            list.add(o.getPhone());
            list.add(o.getTomsId());
            list.add(o.getDmpId());
            list.add(o.getRole());
            list.add(o.getSystems());
            list.add(String.valueOf(o.isCreated()));
            list.add(o.getErrors());
            file.addRow(list);
        });
        return file;
    }
}
