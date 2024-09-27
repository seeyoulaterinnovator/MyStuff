package ru.alamics.sso.e2e;

import org.junit.jupiter.api.Test;
import ru.alamics.sso.e2e.common.Tests;
import ru.alamics.sso.e2e.common.TestsRealms;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class CitiesTests extends Tests {
    @Test
    void getCities() {
        given()
                .baseUri(KEYCLOAK.getAuthServerUrl())
                .pathParam("realm", TestsRealms.E2E.getId())
                .get("/realms/{realm}/cities")
                .then()
                .body("results.cities", is(not(empty())));
    }

    @Test
    void getCurrentCity() {
        given()
                .baseUri(KEYCLOAK.getAuthServerUrl())
                .pathParam("realm", TestsRealms.E2E.getId())
                .get("/realms/{realm}/cities/current")
                .then()
                .log()
                .body()
                .body("results.title", notNullValue());
    }
}
