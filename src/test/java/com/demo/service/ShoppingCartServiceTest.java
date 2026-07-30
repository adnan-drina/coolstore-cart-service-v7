package com.demo.service;

import com.demo.model.Product;
import com.demo.model.ShoppingCart;
import com.demo.model.ShoppingCartItem;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceTest {

    @Mock
    private CatalogService catalogService;

    private PromoService promoService;
    private ShippingService shippingService;
    private ShoppingCartServiceImpl shoppingCartService;

    @BeforeEach
    void setUp() {
        promoService = new PromoService();
        shippingService = new ShippingService();
        shoppingCartService = new ShoppingCartServiceImpl(shippingService, catalogService, promoService);

        Product p1 = new Product("329299", "Promo Item", "A promoted item", 100.0);
        Product p2 = new Product("999999", "Regular Item", "No promo", 50.0);
        lenient().when(catalogService.products()).thenReturn(List.of(p1, p2));
    }

    @Test
    void getShoppingCartCreatesNewCartForUnknownCartId() {
        ShoppingCart cart = shoppingCartService.getShoppingCart("NEW-CART");

        assertNotNull(cart);
        assertEquals("NEW-CART", cart.getCartId());
    }

    @Test
    void getShoppingCartReturnsExistingCart() {
        ShoppingCart cart1 = shoppingCartService.getShoppingCart("CART-1");
        ShoppingCart cart2 = shoppingCartService.getShoppingCart("CART-1");

        assertSame(cart1, cart2);
    }

    @Test
    void addItemWithKnownProductAddsItemToCart() {
        ShoppingCart cart = shoppingCartService.addItem("CART-1", "329299", 2);

        assertNotNull(cart);
        assertEquals(1, cart.getShoppingCartItemList().size());
        ShoppingCartItem item = cart.getShoppingCartItemList().get(0);
        assertEquals("329299", item.getProduct().getItemId());
        assertEquals(2, item.getQuantity());
    }

    @Test
    void addItemWithUnknownProductReturnsCartUnchanged() {
        ShoppingCart cart = shoppingCartService.addItem("CART-1", "UNKNOWN", 1);

        assertNotNull(cart);
        assertTrue(cart.getShoppingCartItemList().isEmpty());
    }

    @Test
    void addItemReflectsPromoDiscountInCartTotals() {
        ShoppingCart cart = shoppingCartService.addItem("CART-1", "329299", 1);

        assertEquals(75.0, cart.getCartItemTotal(), 0.001);
        assertEquals(-25.0, cart.getCartItemPromoSavings(), 0.001);
    }

    @Test
    void addItemDedupedItemPriceMatchesProductPrice() {
        ShoppingCart cart = shoppingCartService.addItem("CART-1", "329299", 1);

        ShoppingCartItem item = cart.getShoppingCartItemList().get(0);
        assertEquals(100.0, item.getPrice(), 0.001);
    }

    @Test
    void addItemCalculatesShippingBasedOnPromotedCartTotal() {
        ShoppingCart cart = shoppingCartService.addItem("CART-1", "999999", 1);

        assertEquals(50.0, cart.getCartItemTotal(), 0.001);
        assertEquals(6.99, cart.getShippingTotal(), 0.001);
    }

    @Test
    void addItemDeduplicatesItemsWithSameProductId() {
        shoppingCartService.addItem("CART-1", "999999", 2);
        shoppingCartService.addItem("CART-1", "999999", 3);

        ShoppingCart cart = shoppingCartService.getShoppingCart("CART-1");
        assertEquals(1, cart.getShoppingCartItemList().size());
        assertEquals(5, cart.getShoppingCartItemList().get(0).getQuantity());
    }

    @Test
    void deleteItemRemovesItemWhenQuantityMatches() {
        shoppingCartService.addItem("CART-1", "999999", 3);
        ShoppingCart cart = shoppingCartService.deleteItem("CART-1", "999999", 3);

        assertTrue(cart.getShoppingCartItemList().isEmpty());
    }

    @Test
    void deleteItemReducesQuantityWhenQuantityExceedsRequested() {
        shoppingCartService.addItem("CART-1", "999999", 5);
        ShoppingCart cart = shoppingCartService.deleteItem("CART-1", "999999", 2);

        assertEquals(1, cart.getShoppingCartItemList().size());
        assertEquals(3, cart.getShoppingCartItemList().get(0).getQuantity());
    }

    @Test
    void checkoutClearsCartItems() {
        shoppingCartService.addItem("CART-1", "999999", 2);
        ShoppingCart cart = shoppingCartService.checkout("CART-1");

        assertTrue(cart.getShoppingCartItemList().isEmpty());
    }

    @Test
    void checkoutResetsCartTotals() {
        shoppingCartService.addItem("CART-1", "999999", 2);
        ShoppingCart cart = shoppingCartService.checkout("CART-1");

        assertEquals(0.0, cart.getCartItemTotal(), 0.001);
        assertEquals(0.0, cart.getShippingTotal(), 0.001);
        assertEquals(0.0, cart.getCartTotal(), 0.001);
    }

    @Test
    void setReplacesCartContentsWithSourceCartItems() {
        shoppingCartService.addItem("SOURCE", "999999", 2);
        ShoppingCart cart = shoppingCartService.set("TARGET", "SOURCE");

        assertEquals(1, cart.getShoppingCartItemList().size());
        assertEquals("999999", cart.getShoppingCartItemList().get(0).getProduct().getItemId());
    }

    @Test
    void priceShoppingCartAppliesPromotionsAndShipping() {
        ShoppingCart cart = shoppingCartService.addItem("CART-1", "329299", 1);

        assertEquals(75.0, cart.getCartItemTotal(), 0.001);
        assertEquals(-25.0, cart.getCartItemPromoSavings(), 0.001);
        assertEquals(0.0, cart.getShippingTotal(), 0.001);
        assertEquals(-8.99, cart.getShippingPromoSavings(), 0.001);
        assertEquals(75.0, cart.getCartTotal(), 0.001);
    }

    @Test
    void priceShoppingCartAppliesFreeShippingPromotionWhenCartTotalGte75() {
        shoppingCartService.addItem("CART-1", "329299", 1);

        ShoppingCart cart = shoppingCartService.getShoppingCart("CART-1");
        assertEquals(75.0, cart.getCartItemTotal(), 0.001);
        assertEquals(0.0, cart.getShippingTotal(), 0.001);
        assertEquals(-8.99, cart.getShippingPromoSavings(), 0.001);
    }

    @Test
    void getProductReturnsCachedProduct() {
        Product p = shoppingCartService.getProduct("329299");

        assertNotNull(p);
        assertEquals("329299", p.getItemId());
        verify(catalogService, times(1)).products();

        Product p2 = shoppingCartService.getProduct("329299");
        assertSame(p, p2);
        verify(catalogService, times(1)).products();
    }

    @Test
    void getProductReturnsNullForUnknownItemId() {
        Product p = shoppingCartService.getProduct("UNKNOWN");

        assertNull(p);
    }

    @Test
    void cartStorageIsConcurrentHashMap() {
        ShoppingCart cart1 = shoppingCartService.getShoppingCart("CART-A");
        ShoppingCart cart2 = shoppingCartService.getShoppingCart("CART-B");

        assertEquals("CART-A", cart1.getCartId());
        assertEquals("CART-B", cart2.getCartId());
        assertNotSame(cart1, cart2);
    }

    @Test
    void priceShoppingCartDoesNothingForNullCart() {
        assertDoesNotThrow(() -> shoppingCartService.priceShoppingCart(null));
    }

    @Test
    void refreshGuardPreventsFrequentCatalogCalls() {
        shoppingCartService.getProduct("329299");
        verify(catalogService, times(1)).products();

        shoppingCartService.getProduct("999999");
        verify(catalogService, times(1)).products();
    }

    @Test
    void catalogFailureThrowsCatalogUnavailableException() {
        when(catalogService.products()).thenThrow(new RuntimeException("catalog down"));

        ShoppingCart cart = new ShoppingCart("FAIL-CART");
        Product p = new Product("FAILITEM", "Fail", "F", 10.0);
        ShoppingCartItem item = new ShoppingCartItem();
        item.setProduct(p);
        item.setQuantity(1);
        cart.addShoppingCartItem(item);

        assertThrows(CatalogUnavailableException.class, () -> shoppingCartService.priceShoppingCart(cart));
    }
}
