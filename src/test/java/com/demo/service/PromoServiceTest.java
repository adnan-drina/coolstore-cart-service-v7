package com.demo.service;

import com.demo.model.Product;
import com.demo.model.Promotion;
import com.demo.model.ShoppingCart;
import com.demo.model.ShoppingCartItem;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PromoServiceTest {

    @Test
    void applies25PercentDiscountOnPromotedItem329299() {
        PromoService promoService = new PromoService();
        ShoppingCart cart = new ShoppingCart("CART-1");

        Product product = new Product("329299", "Promo Item", "A promoted item", 100.0);
        ShoppingCartItem item = new ShoppingCartItem();
        item.setProduct(product);
        item.setQuantity(1);
        item.setPrice(100.0);
        cart.addShoppingCartItem(item);

        promoService.applyCartItemPromotions(cart);

        ShoppingCartItem result = cart.getShoppingCartItemList().get(0);
        assertEquals(75.0, result.getPrice(), 0.001);
        assertEquals(-25.0, result.getPromoSavings(), 0.001);
    }

    @Test
    void doesNotApplyDiscountOnNonPromotedItem() {
        PromoService promoService = new PromoService();
        ShoppingCart cart = new ShoppingCart("CART-2");

        Product product = new Product("999999", "Regular Item", "No promo", 50.0);
        ShoppingCartItem item = new ShoppingCartItem();
        item.setProduct(product);
        item.setQuantity(1);
        item.setPrice(50.0);
        cart.addShoppingCartItem(item);

        promoService.applyCartItemPromotions(cart);

        ShoppingCartItem result = cart.getShoppingCartItemList().get(0);
        assertEquals(50.0, result.getPrice(), 0.001);
        assertEquals(0.0, result.getPromoSavings(), 0.001);
    }

    @Test
    void doesNothingForNullCart() {
        PromoService promoService = new PromoService();
        assertDoesNotThrow(() -> promoService.applyCartItemPromotions(null));
    }

    @Test
    void doesNothingForEmptyCart() {
        PromoService promoService = new PromoService();
        ShoppingCart cart = new ShoppingCart("CART-3");

        assertDoesNotThrow(() -> promoService.applyCartItemPromotions(cart));
    }

    @Test
    void getPromotionsReturnsUnmodifiableSet() {
        PromoService promoService = new PromoService();
        Set<Promotion> promotions = promoService.getPromotions();

        assertEquals(1, promotions.size());
        Promotion p = promotions.iterator().next();
        assertEquals("329299", p.getItemId());
        assertEquals(0.25, p.getPercentOff(), 0.001);

        Promotion fake = new Promotion("FAKE", 0.1);
        assertThrows(UnsupportedOperationException.class, () -> promotions.add(fake));
    }

    @Test
    void setPromotionsReplacesExistingPromotions() {
        PromoService promoService = new PromoService();
        Set<Promotion> newPromos = Set.of(new Promotion("NEWITEM", 0.10));
        promoService.setPromotions(newPromos);

        Set<Promotion> result = promoService.getPromotions();
        assertEquals(1, result.size());
        assertEquals("NEWITEM", result.iterator().next().getItemId());
    }

    @Test
    void setPromotionsWithNullClearsAllPromotions() {
        PromoService promoService = new PromoService();
        promoService.setPromotions(null);

        assertTrue(promoService.getPromotions().isEmpty());
    }

    @Test
    void applyShippingPromotionsGivesFreeShippingWhenCartTotalGte75() {
        PromoService promoService = new PromoService();
        ShoppingCart cart = new ShoppingCart("CART-4");
        cart.setCartItemTotal(75.0);
        cart.setShippingTotal(10.99);

        promoService.applyShippingPromotions(cart);

        assertEquals(0.0, cart.getShippingTotal(), 0.001);
        assertEquals(-10.99, cart.getShippingPromoSavings(), 0.001);
    }

    @Test
    void applyShippingPromotionsDoesNothingWhenCartTotalBelow75() {
        PromoService promoService = new PromoService();
        ShoppingCart cart = new ShoppingCart("CART-5");
        cart.setCartItemTotal(74.99);
        cart.setShippingTotal(8.99);

        promoService.applyShippingPromotions(cart);

        assertEquals(8.99, cart.getShippingTotal(), 0.001);
        assertEquals(0.0, cart.getShippingPromoSavings(), 0.001);
    }

    @Test
    void applyShippingPromotionsDoesNothingForNullCart() {
        PromoService promoService = new PromoService();
        assertDoesNotThrow(() -> promoService.applyShippingPromotions(null));
    }

    @Test
    void promotionStorageIsThreadSafe() {
        PromoService promoService = new PromoService();
        Set<Promotion> original = promoService.getPromotions();
        assertEquals(1, original.size());

        assertDoesNotThrow(() -> {
            promoService.setPromotions(Set.of(new Promotion("ITEM-A", 0.10)));
            promoService.setPromotions(Set.of(new Promotion("ITEM-B", 0.20)));
        });
    }
}
