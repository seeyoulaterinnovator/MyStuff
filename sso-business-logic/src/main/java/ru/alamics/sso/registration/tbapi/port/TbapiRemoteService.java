package ru.alamics.sso.registration.tbapi.port;

import ru.alamics.sso.registration.tbapi.exception.TbapiRegisterException;
import ru.alamics.sso.registration.tbapi.model.TbapiConnectConfig;
import ru.alamics.sso.registration.tbapi.model.TbapiRequest;

import java.util.Map;

public interface TbapiRemoteService {
    Map<String, Object> createCustomer(TbapiRequest request, TbapiConnectConfig connectConfig) throws TbapiRegisterException;
}
