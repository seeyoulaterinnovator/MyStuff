package ru.alamics.sso.user;

import javassist.NotFoundException;
import org.keycloak.models.UserModel;
import ru.alamics.sso.user.model.DownloadUserRequest;
import ru.alamics.sso.user.model.ImportResponse;
import ru.alamics.sso.user.model.UserRequest;
import ru.alamics.sso.registration.FoundException;

import java.io.IOException;
import java.io.InputStream;

public interface UserService {
    byte[] exportUsers(DownloadUserRequest userRequest) throws IOException;
    ImportResponse importUsers(InputStream inputStream, String type) throws IOException, FileServiceException;
    void deferredImportUsers(InputStream inputStream, String content) throws IOException, FileServiceException;
    UserModel createUser(UserRequest request, boolean bss) throws FoundException, NotFoundException;
}
