package ru.alamics.sso.user;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.events.admin.OperationType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.resources.admin.AdminAuth;
import ru.alamics.sso.jpa.entity.ImportUsersDataEntity;
import ru.alamics.sso.jpa.entity.common.ImportUsersReportStatus;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.registration.FoundException;
import ru.alamics.sso.registration.FoundUserPostException;
import ru.alamics.sso.user.filetype.FileFactory;
import ru.alamics.sso.user.filetype.FileModel;
import ru.alamics.sso.user.format.ImportFormat;
import ru.alamics.sso.user.mapper.UserMapper;
import ru.alamics.sso.user.model.*;
import ru.alamics.sso.util.Util;
import ru.alamics.sso.util.validator.NotValidException;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Slf4j
public class UserServiceImpl implements UserService {

    protected KeycloakSession session;
    private final AdminAuth auth;
    private final RealmModel realm;
    // TODO rename these three
    private final ImportUsersReportService importUsersReportService;
    private final ImportReportService importReportService;
    private final ImportService importService;

    private final ExportWorker exportWorker;
    private final UserExtService userExtService;

    public UserServiceImpl(KeycloakSession session, AdminAuth auth) {
        this.auth = auth;
        this.session = session;
        this.realm = session.getContext().getRealm();

        this.importUsersReportService = (ImportUsersReportService) Lookup.lookup(ImportUsersReportService.class);
        this.importReportService = (ImportReportService) Lookup.lookup(ImportReportService.class);
        this.importService = (ImportService) Lookup.lookup(ImportService.class);

        this.exportWorker = new ExportWorker(realm);
        this.userExtService = new UserExtService(session, auth);
    }

    @Override
    public byte[] exportUsers(DownloadUserRequest userRequest) throws IOException {

        if (userRequest.getUserIds() == null)
            return null;

        if (userRequest.getUserParameters() == null)
            userRequest.setUserParameters(UserParameter.values());

        return exportWorker.exportUsers(userRequest);
    }

    @Override
    public FileModel downloadUsersByImportReportId(String importId) throws IOException {

        return exportWorker.downloadUsersByImportReportId(importId);
    }

    @Override
    public void activateImportUsersFromReport(String importId) {
        final List<ImportUsersDataEntity> importUsersData = importReportService.findImportUsersDataByImportId(importId);
        for (ImportUsersDataEntity importData : importUsersData) {
            String id = importData.getUserId();
            if (id == null || id.isEmpty()) {
                continue;
            }
            UserModel user = session.users().getUserById(id, realm);
            if (user == null || user.isEnabled()) {
                continue;
            }
            user.setEnabled(true);
            importService.createAdminEvent(OperationType.CREATE, user, realm, auth, session);
        }
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public ImportResponse importUsers(InputStream inputStream, String content) throws IOException, FileServiceException {
        log.info("Start upload users");

        FileModel file = FileFactory.createFileModel(inputStream, Util.getFileExtension(content));

        ImportFormat impF = FileFactory.getImportFormat(file);
        impF.checkStructure(file);
        List<ImportUsersDataModel> dataList = impF.getDataList(file);

        // create report
        ImportUsersReportModel importUsersReport = importUsersReportService.createImportUsersReport(realm, Util.getFileName(content), dataList);


        //List<Future<String>> asyncList = new ArrayList<>();

        int wndw = 200;

        int first = 0;
        int last = first + wndw;

        // b <= dataList.size() + (wndw - 1)
        while (first < dataList.size()) {
            List<ImportUsersDataModel> dataListBuffer = dataList.subList(first, Math.min(last, dataList.size()));

            //CompletableFuture<String> cf = new CompletableFuture<>();
            //asyncList.add(cf);

            importService.createImportUsers(importUsersReport, dataListBuffer, null, auth, session);
            first = last;
            last += wndw;
        }

        /*
        long countDone = -1;
        while (asyncList.size() > countDone) {
            countDone = asyncList.stream().filter(f -> (f.isCancelled() || f.isDone())).count();
            Thread.sleep(100);
        }
        */

        importUsersReport.setStatus(ImportUsersReportStatus.DONE);
        importUsersReportService.updateReportStatus(importUsersReport);

        ImportResponse importResponse = UserMapper.toImportUsersReportEntity(importUsersReport, dataList);

        log.info("Upload users success!", importResponse);
        return importResponse;
    }

    @Override
    public void uploadImportUsersFile(InputStream inputStream, String content) throws IOException, FileServiceException {
        log.info("Start upload import users file");

        // different file types
        FileModel file = FileFactory.createFileModel(inputStream, Util.getFileExtension(content));

        // different file format
        ImportFormat impF = FileFactory.getImportFormat(file);
        impF.checkStructure(file);
        List<ImportUsersDataModel> dataList = impF.getDataList(file);

        // create report
        String id = importUsersReportService.createImportUsersReportAsync(realm, Util.getFileName(content), dataList);

        importReportService.updateUploaded(id);
        log.info("Upload import users file success");
    }

    @Override
    public UserModel createUser(UserRequest request, boolean bss) throws FoundException, NotFoundException, FoundUserPostException, NotValidException {

        return userExtService.createUser(request, bss);
    }


}
