package ru.alamics.sso.registration.port;

import ru.alamics.sso.registration.model.TbapiRequest;

import java.util.Map;

public interface TbapiRemoteService {
    Map<String, Object> createLead(TbapiRequest request, String host, int port, String path, boolean secure);
}
