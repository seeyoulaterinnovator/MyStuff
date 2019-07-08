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

    public Map<String, String> registerUser(UserModel user) {
        var request = TbapiRequest.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();

        Map<String, String> result = remoteService.createLead(request);

        return result;
    }

}
