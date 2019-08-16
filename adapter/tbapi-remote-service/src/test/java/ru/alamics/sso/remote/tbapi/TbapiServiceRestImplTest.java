package ru.alamics.sso.remote.tbapi;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.alamics.sso.registration.model.TbapiConnectConfig;
import ru.alamics.sso.registration.model.TbapiRequest;

import java.util.Map;
import java.util.UUID;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;


class TbapiServiceRestImplTest {

    private static TbapiServiceRestImpl service;

    private static WireMockServer server;

    @BeforeAll
    static void initWireMock() {
        server = new WireMockServer(wireMockConfig().dynamicPort());
        server.start();

        service = new TbapiServiceRestImpl();
    }

    @AfterEach
    void resetWireMock() {
        server.resetMappings();
    }

    @Test
    void createLead() {

        server.stubFor(post(urlEqualTo("/api/v1/leadManagement/lead"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"id\":\"12345\",\"address\":{\"field1\":\"someValue1\",\"field2\":\"someValue2\"},\"name\":\"someName\",\"legalName\":\"someLegalName\",\"description\":\"someDescription\",\"addressDetails\":\"someAddressDetails\",\"assignedTo\":{\"id\":\"12345\",\"field1\":\"someValue1\"},\"customerCategory\":{\"id\":\"12345\",\"field1\":\"someValue1\"},\"plannedProductDetails\":\"somePlannedProductDetails\",\"serviceAddresses\":[{\"field1\":\"someValue1\"},{\"field1\":\"someValue1-2\"}],\"status\":\"Open\",\"extendedMap\":{\"id\":\"12345\",\"field1\":\"someValue1\"},\"identificationNumber\":\"someIdentificationNumber\",\"phoneNumber\":\"81234567890\",\"email\":\"some@mail.ru\"}")
                )
        );

        TbapiConnectConfig conectConfig = new TbapiConnectConfig();

        conectConfig.setHost("localhost");
        conectConfig.setPort(server.port());
        conectConfig.setAppname("appname");
        conectConfig.setUsername("username");
        conectConfig.setPath("/api/v1/leadManagement/lead");
        conectConfig.setSecure(false);

        Map<String, Object> lead = service.createLead(
                TbapiRequest.builder()
                        //.id(UUID.randomUUID().toString())
                        .email("test@test.ru")
                        //.firstName("User")
                        .name("Test")
                        .build(),
                conectConfig
        );

        assertThat(lead).containsKeys(/*"id", */"name", /*"legalName",*/ "description", "status", "identificationNumber");
    }
}