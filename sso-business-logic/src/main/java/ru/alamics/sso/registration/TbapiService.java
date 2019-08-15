package ru.alamics.sso.registration;

import ru.alamics.sso.registration.model.TbapiConnectConfig;
import ru.alamics.sso.registration.model.TbapiRequest;
import ru.alamics.sso.registration.model.User;
import ru.alamics.sso.registration.port.TbapiRemoteService;


import java.util.Map;

public class TbapiService {

    private final TbapiRemoteService remoteService;

    public TbapiService(TbapiRemoteService remoteService) {
        this.remoteService = remoteService;
    }

    public Map<String, Object> registerUser(User user, TbapiConnectConfig connectConfig) {
        var request = TbapiRequest.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();

        Map<String, Object> result = remoteService.createLead(request, connectConfig);

        return result;
    }

}
