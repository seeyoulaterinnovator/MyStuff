package ru.alamics.sso.registration;

import org.keycloak.models.UserModel;
import ru.alamics.sso.registration.model.TbapiRequest;
import ru.alamics.sso.registration.port.TbapiRemoteService;

import java.util.Map;

public class TbapiService {

    private final TbapiRemoteService remoteService;

    public TbapiService(TbapiRemoteService remoteService) {
        this.remoteService = remoteService;
    }

    public Map<String, Object> registerUser(UserModel user, String host, int port, String path, boolean secure) {
        var request = TbapiRequest.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();

        Map<String, Object> result = remoteService.createLead(request, host, port, path, secure);

        return result;
    }

}
