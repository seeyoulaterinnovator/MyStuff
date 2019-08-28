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

        TbapiRequest request = new TbapiRequest();
        //.id(user.getId())
        request.setEmail(user.getEmail());
        request.setName(user.getFirstName());
        request.setPhoneNumber(user.getPhone());
        request.setLegalName(user.getFirstName()); // TODO attribute orgName

        Map<String, Object> result = remoteService.createCustomer(request, connectConfig);

        return result;
    }

}
