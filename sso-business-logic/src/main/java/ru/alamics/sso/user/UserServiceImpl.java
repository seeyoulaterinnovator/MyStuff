package ru.alamics.sso.user;

import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.events.admin.OperationType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.services.resources.admin.AdminAuth;
import ru.alamics.sso.jpa.entity.ImportUsersDataEntity;
import ru.alamics.sso.jpa.entity.common.ImportUsersReportStatus;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.registration.FoundException;
import ru.alamics.sso.registration.FoundUserPostException;
import ru.alamics.sso.registration.mapper.DataMapper;
import ru.alamics.sso.registration.service.RegisteredUsersService;
import ru.alamics.sso.user.filetype.FileFactory;
import ru.alamics.sso.user.filetype.FileModel;
import ru.alamics.sso.user.format.ImportFormat;
import ru.alamics.sso.user.mapper.UserMapper;
import ru.alamics.sso.user.model.*;
import ru.alamics.sso.util.Util;
import ru.alamics.sso.util.validator.NotValidException;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

@Slf4j
public class UserServiceImpl implements UserService {

    private final AdminAuth auth;
    private final RealmModel realm;
    private final ImportUsersReportService importUsersReportService;
    private final ImportReportService importReportService;
    private final ImportService importService;
    private final ExportWorker exportWorker;
    private final UserExtService userExtService;
    protected KeycloakSession session;

    private final RegisteredUsersService registeredUsersService;

    public UserServiceImpl(KeycloakSession session, AdminAuth auth) {
        this.auth = auth;
        this.session = session;
        this.realm = session.getContext().getRealm();

        this.importUsersReportService = Lookup.lookup(ImportUsersReportService.class);
        this.importReportService = Lookup.lookup(ImportReportService.class);
        this.importService = Lookup.lookup(ImportService.class);

        this.exportWorker = new ExportWorker(realm);
        this.userExtService = new UserExtService(session, auth);
        this.registeredUsersService = Lookup.lookup(RegisteredUsersService.class);
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
        List<ImportUsersDataModel> importModel = new LinkedList<>();

        for (ImportUsersDataEntity importData : importUsersData) {
            String id = importData.getUserId();
            if (id == null || id.isEmpty() || !importData.isCreated()) {
                continue;
            }
            UserModel user = session.users().getUserById(realm, id);
            if (user == null || user.isEnabled()) {
                continue;
            }
            importModel.add(DataMapper.toDataModel(importData));
            user.setEnabled(true);
            importService.createAdminEvent(OperationType.CREATE, user, realm, auth, session);
        }
        importService.doGeneratePasswords(importModel, auth, session);
    }

    @Override
    @Transactional(Transactional.TxType.NEVER)
    public ImportResponse importUsers(InputStream inputStream, String content) throws IOException, FileServiceException {
        log.info("Start upload users");

        FileModel file = FileFactory.createFileModel(inputStream, Util.getFileExtension(content));

        Util.setTimeout(() -> {
            try {
                doImport(file, content);
            } catch (Exception e) {
                log.info("import exception = {}", e.getMessage(), e);
            }
        }, 2);

        return new ImportResponse();
    }

    public ImportResponse doImport(FileModel file, String content) throws IOException, FileServiceException {
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

            List<UserEntity> entities = importService.createImportUsers(importUsersReport, dataListBuffer, null, auth, session);

            if (entities!=null && !entities.isEmpty()){
                for (UserEntity user: entities) {
                    QuarkusTransaction.requiringNew().run(() -> {
                        registeredUsersService.saveSuccessfulReg(user.getId(), user.getRealmId(), "migration", 5);
                    });
                }
            }

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
