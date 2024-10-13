package ru.alamics.sso.e2e;

import jakarta.ws.rs.core.MediaType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Test;
import ru.alamics.sso.e2e.common.Tests;
import ru.alamics.sso.e2e.common.TestsEnabled;
import ru.alamics.sso.e2e.common.TestsRealms;
import ru.alamics.sso.e2e.common.TestsUsers;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.alamics.sso.e2e.common.TestsUtils.getAdminCliAccessToken;

@TestsEnabled
public class AdminTests extends Tests {
    @Test
    void searchUsersByAdmin() {
        searchUsers(TestsUsers.ADMIN);
    }

    @Test
    void searchUsersByManager() {
        searchUsers(TestsUsers.MANAGER);
    }

    @Test
    void getAccessibleRealmsByAdmin() {
        var realms = getAccessibleRealms(TestsUsers.ADMIN);
        assertTrue(realms.contains(TestsRealms.E2E.getId()));
        assertTrue(realms.contains(TestsRealms.E2E_MANAGER.getId()));
        assertTrue(realms.contains(TestsRealms.MASTER.getId()));
    }

    @Test
    void getAccessibleRealmsByManager() {
        var realms = getAccessibleRealms(TestsUsers.MANAGER);
        assertTrue(realms.contains(TestsRealms.E2E.getId()));
        assertFalse(realms.contains(TestsRealms.E2E_MANAGER.getId()));
        assertFalse(realms.contains(TestsRealms.MASTER.getId()));
    }

    void searchUsers(TestsUsers user) {
        given()
                .contentType(MediaType.APPLICATION_JSON)
                .auth().oauth2(getAdminCliAccessToken(user))
                .baseUri(getKeycloakUrl())
                .queryParam("searchRealm", TestsRealms.E2E.getId())
                .queryParam("first", 0)
                .queryParam("max", 10)
                .queryParam("sortField", "firstName")
                .queryParam("sortAsc", true)
                .pathParam("realm", user.getRealm().getId())
                .get("/realms/{realm}/users-info/search")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body("status", is("SUCCESS"))
                .body("results.users-info", hasSize(greaterThan(0)));
    }

    List<String> getAccessibleRealms(TestsUsers user) {
        return given()
                .contentType(MediaType.APPLICATION_JSON)
                .auth().oauth2(getAdminCliAccessToken(user))
                .baseUri(getKeycloakUrl())
                .pathParam("realm", user.getRealm().getId())
                .get("/realms/{realm}/users-info/accessible-realms")
                .then()
                .assertThat()
                .statusCode(HttpStatus.SC_OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body(".", hasSize(greaterThan(0)))
                .extract()
                .jsonPath().getList(".", String.class);

    }
}
