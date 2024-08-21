package ru.alamics.sso.registration.tbapi.port;

import ru.alamics.sso.registration.tbapi.exception.TbapiRegisterException;
import ru.alamics.sso.registration.tbapi.model.TbapiConnectConfig;
import ru.alamics.sso.registration.tbapi.model.TbapiRequest;
import ru.alamics.sso.registration.tbapi.model.TbapiResponse;

import java.util.List;
import java.util.Map;

public interface TbapiRemoteService {
    TbapiResponse createCustomer(TbapiRequest request, TbapiConnectConfig connectConfig) throws TbapiRegisterException;
    Map<String, Object> getCustomerName(List<String> id, TbapiConnectConfig connectConfig) throws TbapiRegisterException;
}
