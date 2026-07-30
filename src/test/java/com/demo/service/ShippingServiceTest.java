package com.demo.service;

import com.demo.model.ShoppingCart;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ShippingServiceTest {

    private final ShippingService shippingService = new ShippingService();

    @ParameterizedTest
    @CsvSource({
        "0, 2.99",
        "24.99, 2.99",
        "25, 4.99",
        "49.99, 4.99",
        "50, 6.99",
        "74.99, 6.99",
        "75, 8.99",
        "99.99, 8.99",
        "100, 10.99",
        "150, 10.99"
    })
    void calculateShippingByCartItemTotalTier(double cartItemTotal, double expectedShipping) {
        ShoppingCart cart = new ShoppingCart("CART");
        cart.setCartItemTotal(cartItemTotal);
        shippingService.calculateShipping(cart);
        assertEquals(expectedShipping, cart.getShippingTotal(), 0.001);
    }
}
