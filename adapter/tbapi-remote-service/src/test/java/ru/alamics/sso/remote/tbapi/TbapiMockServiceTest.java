package ru.alamics.sso.remote.tbapi;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.alamics.sso.registration.model.TbapiRequest;

import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;


class TbapiMockServiceTest {

    private static TbapiMockService service;

    @BeforeAll
    static void initAll() {
        service = new TbapiMockService();
    }

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

    @Test
    void createLead() {
        int port = wireMockRule.port();
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