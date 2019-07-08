package ru.alamics.sso.registration.port;

import ru.alamics.sso.registration.model.TbapiRequest;

import java.util.Map;

public interface TbapiRemoteService {
    Map<String, String> createLead(TbapiRequest request);
}
