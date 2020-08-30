package ru.alamics.sso.user;

import javassist.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.authentication.RequiredActionProvider;
import org.keycloak.events.admin.OperationType;
import org.keycloak.events.admin.ResourceType;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.jpa.entities.UserEntity;
import org.keycloak.provider.ProviderFactory;
import org.keycloak.services.resources.admin.AdminAuth;
import org.keycloak.services.resources.admin.AdminEventBuilder;
import ru.alamics.sso.jpa.entity.ImportUsersDataEntity;
import ru.alamics.sso.jpa.entity.ImportUsersReportEntity;
import ru.alamics.sso.jpa.entity.common.ImportUsersReportStatus;
import ru.alamics.sso.keycloak.facade.UserPostFacade;
import ru.alamics.sso.keycloak.lookup.Lookup;
import ru.alamics.sso.registration.FoundException;
import ru.alamics.sso.registration.FoundUserPostException;
import ru.alamics.sso.registration.dto.ExternalSystemRoleDto;
import ru.alamics.sso.registration.dto.UserPostRequest;
import ru.alamics.sso.registration.dto.UserPostResponse;
import ru.alamics.sso.registration.service.UserFindService;
import ru.alamics.sso.user.filetype.FileFactory;
import ru.alamics.sso.user.filetype.FileModel;
import ru.alamics.sso.user.format.ImportFormat;
import ru.alamics.sso.user.format.StandartImportFormat;
import ru.alamics.sso.user.mapper.UserMapper;
import ru.alamics.sso.user.model.*;
import ru.alamics.sso.util.Util;
import ru.alamics.sso.util.validator.EmailValidator;
import ru.alamics.sso.util.validator.NotValidException;
import ru.alamics.sso.util.validator.PhoneValidator;

import javax.activation.UnsupportedDataTypeException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Slf4j
public class UserServiceImpl implements UserService {

    protected KeycloakSession session;
    private AdminAuth auth;
    private RealmModel realm;
    private ImportUsersReportService importUsersReportService;
    private ImportService importService;

    private ExportWorker exportWorker;
    private UserExtService userExtService;

    public UserServiceImpl(KeycloakSession session, AdminAuth auth) {
        this.auth = auth;
        this.session = session;
        this.realm = session.getContext().getRealm();

        this.importUsersReportService = (ImportUsersReportService) Lookup.lookup(ImportUsersReportService.class);
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
        ImportUsersReportEntity importUsersReport = importUsersReportService.findImportUsersReportByImportId(importId);
        for (ImportUsersDataEntity importData : importUsersReport.getImportUserData()) {
            String id = importData.getUserId();
            if (id == null || id.isEmpty()) {
                continue;
            }
            UserModel user = session.users().getUserById(id, realm);
            if (user == null || user.isEnabled()) {
                continue;
            }
            user.setEnabled(true);
            createAdminEvent(OperationType.CREATE, user);
        }
    }

    @Override
    public ImportResponse importUsers(InputStream inputStream, String content) throws IOException, FileServiceException {
        log.info("Start upload users");

        FileModel file = FileFactory.createFileModel(inputStream, Util.getFileExtension(content));
        if (file == null) {
            throw new UnsupportedDataTypeException("Unsupported file format!");
        }

        ImportFormat impF = new StandartImportFormat();
        impF.checkStructure(file);
        List<ImportUsersDataEntity> dataList = impF.getDataList(file);

        // create report
        ImportUsersReportEntity importUsersReport = importUsersReportService.createImportUsersReport(realm, Util.getFileName(content), dataList);

        importService.createImportUsers(importUsersReport);

        ImportResponse importResponse = UserMapper.toImportUsersReportEntity(importUsersReport);

        log.info("Upload users success!", importResponse);
        return importResponse;
    }

    @Override
    public void uploadImportUsersFile(InputStream inputStream, String content) throws IOException, FileServiceException {
        log.info("Start upload import users file");

        // different file types
        FileModel file = FileFactory.createFileModel(inputStream, Util.getFileExtension(content));
        if (file == null) {
            throw new UnsupportedDataTypeException("Unsupported file format!");
        }

        // different file format
        ImportFormat impF = new StandartImportFormat();
        impF.checkStructure(file);
        List<ImportUsersDataEntity> dataList = impF.getDataList(file);

        // create report
        importUsersReportService.createImportUsersReportAsync(realm, Util.getFileName(content), dataList);

        log.info("Upload import users file success");
    }

    @Override
    public UserModel createUser(UserRequest request, boolean bss) throws FoundException, NotFoundException, FoundUserPostException, NotValidException {

        return userExtService.createUser(request, bss);
    }

    private void createAdminEvent(OperationType operationType, UserModel user) {
        new AdminEventBuilder(realm, auth, session, session.getContext().getConnection())
                .realm(realm)
                .resource(ResourceType.USER)
                .operation(operationType)
                .resourcePath(session.getContext().getUri(), user.getId())
                .success();
    }
}
