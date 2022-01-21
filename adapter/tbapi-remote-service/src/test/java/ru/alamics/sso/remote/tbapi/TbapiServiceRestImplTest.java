package ru.alamics.sso.remote.tbapi;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import ru.alamics.sso.registration.tbapi.exception.TbapiRegisterException;
import ru.alamics.sso.registration.tbapi.model.TbapiConnectConfig;
import ru.alamics.sso.registration.tbapi.model.TbapiRequest;
import ru.alamics.sso.registration.tbapi.model.TbapiResponse;

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
    void createCustomer() throws TbapiRegisterException {

        server.stubFor(post(urlEqualTo("/api/v1/leadManagement/lead"))
                .withHeader("Accept", equalTo("application/json"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\n" +
                                "  \"id\" : \"9156571701513270883\",\n" +
                                "  \"name\" : \"РОГА И КОПЫТА\",\n" +
                                "  \"status\" : \"Prospect\",\n" +
                                "  \"extendedMap\" : {\n" +
                                "    \"9152455932013395741\" : {\n" +
                                "      \"attributeType\" : 0,\n" +
                                "      \"attributeName\" : \"DMP Customer Id\",\n" +
                                "      \"singleValue\" : {\n" +
                                "        \"attributeValue\" : \"9bfc86e3-29ed-4448-a65d-22ea07fe78c2\"\n" +
                                "      }\n" +
                                "    },\n" +
                                "    \"9132121613813866323\" : {\n" +
                                "      \"attributeType\" : 0,\n" +
                                "      \"attributeName\" : \"E-mail\",\n" +
                                "      \"singleValue\" : {\n" +
                                "        \"attributeValue\" : \"testemail@itrev.ru\"\n" +
                                "      }\n" +
                                "    },\n" +
                                "    \"9132121613813866318\" : {\n" +
                                "      \"attributeType\" : 0,\n" +
                                "      \"attributeName\" : \"Phone Number\",\n" +
                                "      \"singleValue\" : {\n" +
                                "        \"attributeValue\" : \"+7 (111) 123-45-67\"\n" +
                                "      }\n" +
                                "    }\n" +
                                "  }\n" +
                                "}")
                )
        );

        TbapiConnectConfig conectConfig = new TbapiConnectConfig();

        conectConfig.setHost("localhost");
        conectConfig.setIp("localhost");
        conectConfig.setPort(server.port());
        conectConfig.setAppname("appname");
        conectConfig.setUsername("username");
        conectConfig.setPassword("password");
        conectConfig.setPath("/api/v1/leadManagement/lead");
        conectConfig.setSecure(false);

        TbapiRequest req = new TbapiRequest();
        req.setEmail("testemail@itrev.ru");
        req.setName("Рога и копыта");

        TbapiResponse customer = service.createCustomer(
                req,
                conectConfig
        );

        assertThat(customer).isNotNull();
        assertThat(customer.getBusinessErrorCode()).isNull();

        assertThat(customer.getId()).isEqualTo("9156571701513270883");

        assertThat(customer.getExtendedMap().getCustomerHolder()).isNotNull();
        assertThat(customer.getExtendedMap().getCustomerHolder().getSingleValue()).isNotNull();
        assertThat(customer.getExtendedMap().getCustomerHolder().getSingleValue().getAttributeValue()).isEqualTo("9bfc86e3-29ed-4448-a65d-22ea07fe78c2");
    }
}