package com.demo.rest;

import com.github.tomakehurst.wiremock.WireMockServer;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static io.restassured.RestAssured.given;
import static io.restassured.config.JsonConfig.jsonConfig;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
@QuarkusTestResource(CatalogWireMockResource.class)
class CartEndpointTest {

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

    // ---- GET /cart/{cartId} ----

    @Test
    void getCartReturnsNotFoundForEmptyCart() {
        given()
            .pathParam("cartId", "empty-cart")
            .when()
            .get("/api/cart/{cartId}")
            .then()
            .statusCode(404)
            .body("status", equalTo(404))
            .body("title", equalTo("Not Found"));
    }

    @Test
    void getCartReturnsCartWithItems() {
        String cartId = "get-cart-" + System.nanoTime();

        given()
            .pathParam("cartId", cartId)
            .pathParam("itemId", "999999")
            .pathParam("quantity", 1)
            .when()
            .post("/api/cart/{cartId}/{itemId}/{quantity}")
            .then()
            .statusCode(200);

        given()
            .config(io.restassured.RestAssured.config()
                .jsonConfig(jsonConfig().numberReturnType(
                    io.restassured.path.json.config.JsonPathConfig.NumberReturnType.BIG_DECIMAL)))
            .pathParam("cartId", cartId)
            .when()
            .get("/api/cart/{cartId}")
            .then()
            .statusCode(200)
            .body("cartId", equalTo(cartId))
            .body("shoppingCartItemList.size()", equalTo(1))
            .body("shoppingCartItemList[0].product.itemId", equalTo("999999"))
            .body("shoppingCartItemList[0].quantity", equalTo(1))
            .body("shoppingCartItemList[0].price", equalTo(new BigDecimal("50.0")));
    }

    @Test
    void getCartIsIdempotent() {
        String cartId = "idempotent-cart-" + System.nanoTime();

        given()
            .pathParam("cartId", cartId)
            .pathParam("itemId", "999999")
            .pathParam("quantity", 2)
            .when()
            .post("/api/cart/{cartId}/{itemId}/{quantity}")
            .then()
            .statusCode(200);

        given()
            .pathParam("cartId", cartId)
            .when()
            .get("/api/cart/{cartId}")
            .then()
            .statusCode(200)
            .body("shoppingCartItemList.size()", equalTo(1))
            .body("shoppingCartItemList[0].quantity", equalTo(2));

        given()
            .pathParam("cartId", cartId)
            .when()
            .get("/api/cart/{cartId}")
            .then()
            .statusCode(200)
            .body("shoppingCartItemList.size()", equalTo(1))
            .body("shoppingCartItemList[0].quantity", equalTo(2));
    }

    // ---- POST /cart/{cartId}/{itemId}/{quantity} ----

    @Test
    void addItemReturnsBadRequestForZeroQuantity() {
        given()
            .pathParam("cartId", "cart-1")
            .pathParam("itemId", "999999")
            .pathParam("quantity", 0)
            .when()
            .post("/api/cart/{cartId}/{itemId}/{quantity}")
            .then()
            .statusCode(400)
            .body("status", equalTo(400))
            .body("title", equalTo("Bad Request"));
    }

    @Test
    void addItemReturnsBadRequestForNegativeQuantity() {
        given()
            .pathParam("cartId", "cart-1")
            .pathParam("itemId", "999999")
            .pathParam("quantity", -1)
            .when()
            .post("/api/cart/{cartId}/{itemId}/{quantity}")
            .then()
            .statusCode(400)
            .body("status", equalTo(400))
            .body("title", equalTo("Bad Request"));
    }

    @Test
    void addItemAddsItemToCart() {
        String cartId = "add-item-" + System.nanoTime();

        given()
            .pathParam("cartId", cartId)
            .pathParam("itemId", "999999")
            .pathParam("quantity", 3)
            .when()
            .post("/api/cart/{cartId}/{itemId}/{quantity}")
            .then()
            .statusCode(200)
            .body("cartId", equalTo(cartId))
            .body("shoppingCartItemList.size()", equalTo(1))
            .body("shoppingCartItemList[0].product.itemId", equalTo("999999"))
            .body("shoppingCartItemList[0].quantity", equalTo(3));
    }

    @Test
    void addItemDeduplicatesWithAdditiveQuantity() {
        String cartId = "dedupe-" + System.nanoTime();

        given()
            .pathParam("cartId", cartId)
            .pathParam("itemId", "999999")
            .pathParam("quantity", 2)
            .when()
            .post("/api/cart/{cartId}/{itemId}/{quantity}")
            .then()
            .statusCode(200);

        given()
            .pathParam("cartId", cartId)
            .pathParam("itemId", "999999")
            .pathParam("quantity", 3)
            .when()
            .post("/api/cart/{cartId}/{itemId}/{quantity}")
            .then()
            .statusCode(200)
            .body("shoppingCartItemList.size()", equalTo(1))
            .body("shoppingCartItemList[0].quantity", equalTo(5));
    }

    @Test
    void addItemWithUnknownProductReturnsCartUnchanged() {
        String cartId = "unknown-product-" + System.nanoTime();

        given()
            .pathParam("cartId", cartId)
            .pathParam("itemId", "UNKNOWN")
            .pathParam("quantity", 1)
            .when()
            .post("/api/cart/{cartId}/{itemId}/{quantity}")
            .then()
            .statusCode(200)
            .body("shoppingCartItemList.size()", equalTo(0));
    }

    @Test
    void addItemAppliesPromotions() {
        String cartId = "promo-" + System.nanoTime();

        given()
            .config(io.restassured.RestAssured.config()
                .jsonConfig(jsonConfig().numberReturnType(
                    io.restassured.path.json.config.JsonPathConfig.NumberReturnType.BIG_DECIMAL)))
            .pathParam("cartId", cartId)
            .pathParam("itemId", "329299")
            .pathParam("quantity", 1)
            .when()
            .post("/api/cart/{cartId}/{itemId}/{quantity}")
            .then()
            .statusCode(200)
            .body("cartItemTotal", equalTo(new BigDecimal("75.0")))
            .body("cartItemPromoSavings", equalTo(new BigDecimal("-25.0")));
    }

    // ---- DELETE /cart/{cartId}/{itemId}/{quantity} ----

    @Test
    void deleteItemReturnsBadRequestForZeroQuantity() {
        given()
            .pathParam("cartId", "cart-1")
            .pathParam("itemId", "999999")
            .pathParam("quantity", 0)
            .when()
            .delete("/api/cart/{cartId}/{itemId}/{quantity}")
            .then()
            .statusCode(400)
            .body("status", equalTo(400))
            .body("title", equalTo("Bad Request"));
    }

    @Test
    void deleteItemRemovesItemWhenQuantityMatches() {
        String cartId = "delete-" + System.nanoTime();

        given()
            .pathParam("cartId", cartId)
            .pathParam("itemId", "999999")
            .pathParam("quantity", 3)
            .when()
            .post("/api/cart/{cartId}/{itemId}/{quantity}")
            .then()
            .statusCode(200);

        given()
            .pathParam("cartId", cartId)
            .pathParam("itemId", "999999")
            .pathParam("quantity", 3)
            .when()
            .delete("/api/cart/{cartId}/{itemId}/{quantity}")
            .then()
            .statusCode(200)
            .body("shoppingCartItemList.size()", equalTo(0));
    }

    @Test
    void deleteItemReducesQuantityWhenExceeding() {
        String cartId = "delete-partial-" + System.nanoTime();

        given()
            .pathParam("cartId", cartId)
            .pathParam("itemId", "999999")
            .pathParam("quantity", 5)
            .when()
            .post("/api/cart/{cartId}/{itemId}/{quantity}")
            .then()
            .statusCode(200);

        given()
            .pathParam("cartId", cartId)
            .pathParam("itemId", "999999")
            .pathParam("quantity", 2)
            .when()
            .delete("/api/cart/{cartId}/{itemId}/{quantity}")
            .then()
            .statusCode(200)
            .body("shoppingCartItemList.size()", equalTo(1))
            .body("shoppingCartItemList[0].quantity", equalTo(3));
    }

    // ---- POST /cart/checkout/{cartId} ----

    @Test
    void checkoutClearsCart() {
        String cartId = "checkout-" + System.nanoTime();

        given()
            .pathParam("cartId", cartId)
            .pathParam("itemId", "999999")
            .pathParam("quantity", 2)
            .when()
            .post("/api/cart/{cartId}/{itemId}/{quantity}")
            .then()
            .statusCode(200);

        given()
            .config(io.restassured.RestAssured.config()
                .jsonConfig(jsonConfig().numberReturnType(
                    io.restassured.path.json.config.JsonPathConfig.NumberReturnType.BIG_DECIMAL)))
            .pathParam("cartId", cartId)
            .when()
            .post("/api/cart/checkout/{cartId}")
            .then()
            .statusCode(200)
            .body("shoppingCartItemList.size()", equalTo(0))
            .body("cartItemTotal", equalTo(new BigDecimal("0.0")))
            .body("cartTotal", equalTo(new BigDecimal("0.0")));
    }

    // ---- POST /cart/{cartId}/{tmpId} ----

    @Test
    void setCartReplacesContents() {
        String sourceId = "source-" + System.nanoTime();
        String targetId = "target-" + System.nanoTime();

        given()
            .pathParam("cartId", sourceId)
            .pathParam("itemId", "999999")
            .pathParam("quantity", 2)
            .when()
            .post("/api/cart/{cartId}/{itemId}/{quantity}")
            .then()
            .statusCode(200);

        given()
            .pathParam("cartId", targetId)
            .pathParam("tmpId", sourceId)
            .when()
            .post("/api/cart/{cartId}/{tmpId}")
            .then()
            .statusCode(200)
            .body("shoppingCartItemList.size()", equalTo(1))
            .body("shoppingCartItemList[0].product.itemId", equalTo("999999"))
            .body("shoppingCartItemList[0].quantity", equalTo(2));
    }

    // ---- Error response format ----

    @Test
    void errorResponsesUseProblemJsonContentType() {
        given()
            .pathParam("cartId", "not-found-cart-" + System.nanoTime())
            .when()
            .get("/api/cart/{cartId}")
            .then()
            .statusCode(404)
            .header("Content-Type", containsString("application/problem+json"));
    }
}
