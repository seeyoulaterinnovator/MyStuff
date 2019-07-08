package ru.alamics.sso.remote.tbapi;

import ru.alamics.sso.registration.model.TbapiRequest;
import ru.alamics.sso.registration.port.TbapiRemoteService;

import java.util.Map;
import java.util.UUID;

public class TbapiMockService implements TbapiRemoteService {

    @Override
    public Map<String, String> createLead(TbapiRequest request) {
        return Map.of(
                "customer_id", UUID.randomUUID().toString(),
                "name", "test-name",
                "legalName", "test-legal-name",
                "description", "test-description",
                "status", "Open",
                "identificationNumber", "test-identificationNumber"
        );
    }}
