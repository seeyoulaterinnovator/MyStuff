package ru.alamics.sso.remote.tbapi;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.alamics.sso.registration.model.TbapiRequest;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.*;


class TbapiMockServiceTest {

    private static TbapiMockService service;

    @BeforeAll
    static void initAll() {
        service = new TbapiMockService();
    }

    @Test
    void createLead() {
        Map<String, String> lead = service.createLead(TbapiRequest.builder()
                .id(UUID.randomUUID().toString())
                .email("test@test.ru")
                .firstName("User")
                .lastName("Test")
                .build()
        );

        assertThat(lead).containsKeys("customer_id", "name", "legalName", "description", "status", "identificationNumber");
    }
}