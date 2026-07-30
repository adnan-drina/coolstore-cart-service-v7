package com.demo.service;

import com.demo.model.ShoppingCart;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ShippingServiceTest {

    private final ShippingService shippingService = new ShippingService();

    @Test
    void chargesDollar2_99ForCartTotalBetween0And25() {
        ShoppingCart cart = new ShoppingCart("CART-1");
        cart.setCartItemTotal(0);

        shippingService.calculateShipping(cart);

        assertEquals(2.99, cart.getShippingTotal(), 0.001);
    }

    @Test
    void chargesDollar2_99ForCartTotalJustBelow25() {
        ShoppingCart cart = new ShoppingCart("CART-2");
        cart.setCartItemTotal(24.99);

        shippingService.calculateShipping(cart);

        assertEquals(2.99, cart.getShippingTotal(), 0.001);
    }

    @Test
    void chargesDollar4_99ForCartTotalBetween25And50() {
        ShoppingCart cart = new ShoppingCart("CART-3");
        cart.setCartItemTotal(25);

        shippingService.calculateShipping(cart);

        assertEquals(4.99, cart.getShippingTotal(), 0.001);
    }

    @Test
    void chargesDollar4_99ForCartTotalJustBelow50() {
        ShoppingCart cart = new ShoppingCart("CART-4");
        cart.setCartItemTotal(49.99);

        shippingService.calculateShipping(cart);

        assertEquals(4.99, cart.getShippingTotal(), 0.001);
    }

    @Test
    void chargesDollar6_99ForCartTotalBetween50And75() {
        ShoppingCart cart = new ShoppingCart("CART-5");
        cart.setCartItemTotal(50);

        shippingService.calculateShipping(cart);

        assertEquals(6.99, cart.getShippingTotal(), 0.001);
    }

    @Test
    void chargesDollar6_99ForCartTotalJustBelow75() {
        ShoppingCart cart = new ShoppingCart("CART-6");
        cart.setCartItemTotal(74.99);

        shippingService.calculateShipping(cart);

        assertEquals(6.99, cart.getShippingTotal(), 0.001);
    }

    @Test
    void chargesDollar8_99ForCartTotalBetween75And100() {
        ShoppingCart cart = new ShoppingCart("CART-7");
        cart.setCartItemTotal(75);

        shippingService.calculateShipping(cart);

        assertEquals(8.99, cart.getShippingTotal(), 0.001);
    }

    @Test
    void chargesDollar8_99ForCartTotalJustBelow100() {
        ShoppingCart cart = new ShoppingCart("CART-8");
        cart.setCartItemTotal(99.99);

        shippingService.calculateShipping(cart);

        assertEquals(8.99, cart.getShippingTotal(), 0.001);
    }

    @Test
    void chargesDollar10_99ForCartTotalAtOrAbove100() {
        ShoppingCart cart = new ShoppingCart("CART-9");
        cart.setCartItemTotal(100);

        shippingService.calculateShipping(cart);

        assertEquals(10.99, cart.getShippingTotal(), 0.001);
    }

    @Test
    void chargesDollar10_99ForCartTotalJustBelow10000() {
        ShoppingCart cart = new ShoppingCart("CART-10");
        cart.setCartItemTotal(9999.99);

        shippingService.calculateShipping(cart);

        assertEquals(10.99, cart.getShippingTotal(), 0.001);
    }

    @Test
    void doesNothingForNullCart() {
        assertDoesNotThrow(() -> shippingService.calculateShipping(null));
    }
}
