package ru.alamics.sso.e2e;

import jakarta.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import ru.alamics.sso.e2e.common.Tests;
import ru.alamics.sso.e2e.common.TestsEnabled;
import ru.alamics.sso.e2e.common.TestsRealms;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static ru.alamics.sso.e2e.common.TestsUtils.getAdminAccessToken;

@TestsEnabled
public class AdminTests extends Tests {
    @Test
    void searchUsers() {
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .auth().oauth2(getAdminAccessToken())
                .baseUri(KEYCLOAK.getAuthServerUrl())
                .queryParam("searchRealm", TestsRealms.E2E.getId())
                .queryParam("first", 0)
                .queryParam("max", 10)
                .queryParam("sortField", "firstName")
                .queryParam("sortAsc", true)
                .get("/realms/master/users-info/search")
                .then()
                .assertThat()
                .log()
                .all()
                .statusCode(HttpStatus.SC_OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body("status", is("SUCCESS"))
                .body("results.users-info", hasSize(greaterThan(0)));
    }
}
