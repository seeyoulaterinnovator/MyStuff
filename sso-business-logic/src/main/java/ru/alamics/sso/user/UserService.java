package ru.alamics.sso.user;

import javassist.NotFoundException;
import org.keycloak.models.UserModel;
import ru.alamics.sso.registration.FoundException;
import ru.alamics.sso.registration.FoundUserPostException;
import ru.alamics.sso.user.model.DownloadUserRequest;
import ru.alamics.sso.user.model.FileModel;
import ru.alamics.sso.user.model.ImportResponse;
import ru.alamics.sso.user.model.UserRequest;
import ru.alamics.sso.util.validator.NotValidException;

import java.io.IOException;
import java.io.InputStream;

public interface UserService {
    byte[] exportUsers(DownloadUserRequest userRequest) throws IOException;

    void activateImportUsersFromReport(String importId);

    ImportResponse importUsers(InputStream inputStream, String type) throws IOException, FileServiceException;

    void uploadImportUsersFile(InputStream inputStream, String content) throws IOException, FileServiceException;

    UserModel createUser(UserRequest request, boolean bss) throws FoundException, NotFoundException, FoundUserPostException, NotValidException;

    FileModel downloadUsersByImportReportId(String importId) throws IOException;
}
