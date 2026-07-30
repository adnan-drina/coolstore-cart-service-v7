package com.demo.rest;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
@QuarkusTestResource(CatalogWireMockResource.class)
class HealthEndpointTest {

    @BeforeEach
    void setUp() {
        WireMockServer server = CatalogWireMockResource.getServer();
        server.resetAll();
        server.stubFor(get(urlEqualTo("/api/products"))
            .willReturn(okJson(
                "[" +
                "{\"itemId\":\"329299\",\"name\":\"Promo Item\",\"desc\":\"A promoted item\",\"price\":100.0}," +
                "{\"itemId\":\"999999\",\"name\":\"Regular Item\",\"desc\":\"No promo\",\"price\":50.0}" +
                "]"
            )));
    }

    @Test
    void acceptanceCheckReturnsProducts() {
        given()
            .when()
            .get("/api/cart/acceptance-check")
            .then()
            .statusCode(200)
            .body("size()", equalTo(2))
            .body("[0].itemId", equalTo("329299"))
            .body("[1].itemId", equalTo("999999"));
    }

    @Test
    void acceptanceCheckReturns503WhenCatalogDown() {
        WireMockServer server = CatalogWireMockResource.getServer();
        server.resetAll();
        server.stubFor(get(urlEqualTo("/api/products"))
            .willReturn(aResponse().withStatus(503)));

        given()
            .when()
            .get("/api/cart/acceptance-check")
            .then()
            .statusCode(503);
    }
}
