package com.demo.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

@QuarkusTest
class AcceptanceEndpointTest {

    @Test
    void returnsOkStatus() {
        given()
                .when().get("/api/cart/acceptance-check")
                .then()
                .statusCode(200)
                .body("status", is("ok"));
    }
}
