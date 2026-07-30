package com.demo.model;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DomainModelTest {

    // --- Product tests ---

    @Test
    void productDefaultConstructorCreatesEmptyFields() {
        Product p = new Product();

        assertNull(p.getItemId());
        assertNull(p.getName());
        assertNull(p.getDesc());
        assertEquals(0.0, p.getPrice(), 0.001);
    }

    @Test
    void productFullConstructorSetsAllFields() {
        Product p = new Product("ITEM-1", "Laptop", "A nice laptop", 999.99);

        assertEquals("ITEM-1", p.getItemId());
        assertEquals("Laptop", p.getName());
        assertEquals("A nice laptop", p.getDesc());
        assertEquals(999.99, p.getPrice(), 0.001);
    }

    @Test
    void productSettersUpdateFields() {
        Product p = new Product();
        p.setItemId("ITEM-2");
        p.setName("Phone");
        p.setDesc("A phone");
        p.setPrice(499.50);

        assertEquals("ITEM-2", p.getItemId());
        assertEquals("Phone", p.getName());
        assertEquals("A phone", p.getDesc());
        assertEquals(499.50, p.getPrice(), 0.001);
    }

    @Test
    void productToStringContainsAllFields() {
        Product p = new Product("ITEM-1", "Laptop", "A nice laptop", 999.99);
        String s = p.toString();

        assertTrue(s.contains("itemId=ITEM-1"));
        assertTrue(s.contains("name=Laptop"));
        assertTrue(s.contains("desc=A nice laptop"));
        assertTrue(s.contains("price=999.99"));
    }

    @Test
    void productSerializationRoundTrips() throws Exception {
        Product original = new Product("ITEM-1", "Laptop", "A nice laptop", 999.99);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(original);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            Product deserialized = (Product) ois.readObject();

            assertEquals(original.getItemId(), deserialized.getItemId());
            assertEquals(original.getName(), deserialized.getName());
            assertEquals(original.getDesc(), deserialized.getDesc());
            assertEquals(original.getPrice(), deserialized.getPrice(), 0.001);
        }
    }

    // --- Promotion tests ---

    @Test
    void promotionDefaultConstructorCreatesEmptyFields() {
        Promotion promo = new Promotion();

        assertNull(promo.getItemId());
        assertEquals(0.0, promo.getPercentOff(), 0.001);
    }

    @Test
    void promotionFullConstructorSetsAllFields() {
        Promotion promo = new Promotion("ITEM-1", 15.0);

        assertEquals("ITEM-1", promo.getItemId());
        assertEquals(15.0, promo.getPercentOff(), 0.001);
    }

    @Test
    void promotionSettersUpdateFields() {
        Promotion promo = new Promotion();
        promo.setItemId("ITEM-2");
        promo.setPercentOff(25.5);

        assertEquals("ITEM-2", promo.getItemId());
        assertEquals(25.5, promo.getPercentOff(), 0.001);
    }

    @Test
    void promotionToStringContainsAllFields() {
        Promotion promo = new Promotion("ITEM-1", 15.0);
        String s = promo.toString();

        assertTrue(s.contains("itemId=ITEM-1"));
        assertTrue(s.contains("percentOff=15.0"));
    }

    // --- ShoppingCartItem tests ---

    @Test
    void shoppingCartItemDefaultConstructorCreatesEmptyFields() {
        ShoppingCartItem item = new ShoppingCartItem();

        assertEquals(0.0, item.getPrice(), 0.001);
        assertEquals(0, item.getQuantity());
        assertEquals(0.0, item.getPromoSavings(), 0.001);
        assertNull(item.getProduct());
    }

    @Test
    void shoppingCartItemSettersUpdateFields() {
        ShoppingCartItem item = new ShoppingCartItem();
        Product p = new Product("ITEM-1", "Laptop", "A nice laptop", 999.99);
        item.setPrice(999.99);
        item.setQuantity(2);
        item.setPromoSavings(50.0);
        item.setProduct(p);

        assertEquals(999.99, item.getPrice(), 0.001);
        assertEquals(2, item.getQuantity());
        assertEquals(50.0, item.getPromoSavings(), 0.001);
        assertEquals(p, item.getProduct());
    }

    @Test
    void shoppingCartItemToStringContainsAllFields() {
        Product p = new Product("ITEM-1", "Laptop", "A nice laptop", 999.99);
        ShoppingCartItem item = new ShoppingCartItem();
        item.setPrice(999.99);
        item.setQuantity(2);
        item.setPromoSavings(50.0);
        item.setProduct(p);

        String s = item.toString();

        assertTrue(s.contains("price=999.99"));
        assertTrue(s.contains("quantity=2"));
        assertTrue(s.contains("promoSavings=50.0"));
        assertTrue(s.contains("product=" + p.toString()));
    }

    @Test
    void shoppingCartItemSerializationRoundTrips() throws Exception {
        Product p = new Product("ITEM-1", "Laptop", "A nice laptop", 999.99);
        ShoppingCartItem original = new ShoppingCartItem();
        original.setPrice(999.99);
        original.setQuantity(2);
        original.setPromoSavings(50.0);
        original.setProduct(p);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(original);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            ShoppingCartItem deserialized = (ShoppingCartItem) ois.readObject();

            assertEquals(original.getPrice(), deserialized.getPrice(), 0.001);
            assertEquals(original.getQuantity(), deserialized.getQuantity());
            assertEquals(original.getPromoSavings(), deserialized.getPromoSavings(), 0.001);
            assertEquals(original.getProduct().getItemId(), deserialized.getProduct().getItemId());
        }
    }

    // --- ShoppingCart tests ---

    @Test
    void shoppingCartDefaultConstructorCreatesEmptyCart() {
        ShoppingCart cart = new ShoppingCart();

        assertNull(cart.getCartId());
        assertEquals(0.0, cart.getCartItemTotal(), 0.001);
        assertEquals(0.0, cart.getCartItemPromoSavings(), 0.001);
        assertEquals(0.0, cart.getShippingTotal(), 0.001);
        assertEquals(0.0, cart.getShippingPromoSavings(), 0.001);
        assertEquals(0.0, cart.getCartTotal(), 0.001);
        assertNotNull(cart.getShoppingCartItemList());
        assertTrue(cart.getShoppingCartItemList().isEmpty());
    }

    @Test
    void shoppingCartConstructorWithCartIdSetsId() {
        ShoppingCart cart = new ShoppingCart("CART-1");

        assertEquals("CART-1", cart.getCartId());
    }

    @Test
    void shoppingCartSettersUpdateFields() {
        ShoppingCart cart = new ShoppingCart();
        cart.setCartId("CART-1");
        cart.setCartItemTotal(500.0);
        cart.setCartItemPromoSavings(25.0);
        cart.setShippingTotal(10.0);
        cart.setShippingPromoSavings(2.0);
        cart.setCartTotal(483.0);

        assertEquals("CART-1", cart.getCartId());
        assertEquals(500.0, cart.getCartItemTotal(), 0.001);
        assertEquals(25.0, cart.getCartItemPromoSavings(), 0.001);
        assertEquals(10.0, cart.getShippingTotal(), 0.001);
        assertEquals(2.0, cart.getShippingPromoSavings(), 0.001);
        assertEquals(483.0, cart.getCartTotal(), 0.001);
    }

    @Test
    void addShoppingCartItemAddsNonNullItem() {
        ShoppingCart cart = new ShoppingCart();
        ShoppingCartItem item = new ShoppingCartItem();
        item.setPrice(10.0);
        item.setQuantity(1);

        cart.addShoppingCartItem(item);

        assertEquals(1, cart.getShoppingCartItemList().size());
        assertEquals(item, cart.getShoppingCartItemList().get(0));
    }

    @Test
    void addShoppingCartItemIgnoresNull() {
        ShoppingCart cart = new ShoppingCart();

        cart.addShoppingCartItem(null);

        assertTrue(cart.getShoppingCartItemList().isEmpty());
    }

    @Test
    void addShoppingCartItemAccumulatesMultipleItems() {
        ShoppingCart cart = new ShoppingCart();
        ShoppingCartItem item1 = new ShoppingCartItem();
        item1.setPrice(10.0);
        ShoppingCartItem item2 = new ShoppingCartItem();
        item2.setPrice(20.0);

        cart.addShoppingCartItem(item1);
        cart.addShoppingCartItem(item2);

        assertEquals(2, cart.getShoppingCartItemList().size());
    }

    @Test
    void removeShoppingCartItemRemovesExistingItem() {
        ShoppingCart cart = new ShoppingCart();
        ShoppingCartItem item = new ShoppingCartItem();
        item.setPrice(10.0);
        cart.addShoppingCartItem(item);

        boolean removed = cart.removeShoppingCartItem(item);

        assertTrue(removed);
        assertTrue(cart.getShoppingCartItemList().isEmpty());
    }

    @Test
    void removeShoppingCartItemReturnsFalseForNull() {
        ShoppingCart cart = new ShoppingCart();

        boolean removed = cart.removeShoppingCartItem(null);

        assertFalse(removed);
    }

    @Test
    void removeShoppingCartItemReturnsFalseForUnknownItem() {
        ShoppingCart cart = new ShoppingCart();
        ShoppingCartItem item = new ShoppingCartItem();
        item.setPrice(10.0);

        boolean removed = cart.removeShoppingCartItem(item);

        assertFalse(removed);
    }

    @Test
    void resetShoppingCartItemListClearsItems() {
        ShoppingCart cart = new ShoppingCart();
        ShoppingCartItem item = new ShoppingCartItem();
        item.setPrice(10.0);
        cart.addShoppingCartItem(item);

        cart.resetShoppingCartItemList();

        assertTrue(cart.getShoppingCartItemList().isEmpty());
    }

    @Test
    void setShoppingCartItemListReplacesList() {
        ShoppingCart cart = new ShoppingCart();
        ShoppingCartItem item = new ShoppingCartItem();
        item.setPrice(10.0);
        cart.addShoppingCartItem(item);

        List<ShoppingCartItem> newList = List.of();
        cart.setShoppingCartItemList(newList);

        assertSame(newList, cart.getShoppingCartItemList());
    }

    @Test
    void shoppingCartToStringContainsAllFields() {
        ShoppingCart cart = new ShoppingCart("CART-1");
        cart.setCartItemTotal(500.0);
        cart.setCartItemPromoSavings(25.0);
        cart.setShippingTotal(10.0);
        cart.setShippingPromoSavings(2.0);
        cart.setCartTotal(483.0);

        String s = cart.toString();

        assertTrue(s.contains("cartId=CART-1"));
        assertTrue(s.contains("cartItemTotal=500.0"));
        assertTrue(s.contains("cartItemPromoSavings=25.0"));
        assertTrue(s.contains("shippingTotal=10.0"));
        assertTrue(s.contains("shippingPromoSavings=2.0"));
        assertTrue(s.contains("cartTotal=483.0"));
    }

    @Test
    void shoppingCartSerializationRoundTrips() throws Exception {
        ShoppingCart original = new ShoppingCart("CART-1");
        ShoppingCartItem item = new ShoppingCartItem();
        item.setPrice(100.0);
        item.setQuantity(3);
        item.setPromoSavings(10.0);
        Product p = new Product("ITEM-1", "Laptop", "A nice laptop", 999.99);
        item.setProduct(p);
        original.addShoppingCartItem(item);
        original.setCartItemTotal(300.0);
        original.setCartItemPromoSavings(30.0);
        original.setShippingTotal(15.0);
        original.setShippingPromoSavings(1.5);
        original.setCartTotal(283.5);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(original);
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        try (ObjectInputStream ois = new ObjectInputStream(bais)) {
            ShoppingCart deserialized = (ShoppingCart) ois.readObject();

            assertEquals(original.getCartId(), deserialized.getCartId());
            assertEquals(original.getCartItemTotal(), deserialized.getCartItemTotal(), 0.001);
            assertEquals(original.getCartItemPromoSavings(), deserialized.getCartItemPromoSavings(), 0.001);
            assertEquals(original.getShippingTotal(), deserialized.getShippingTotal(), 0.001);
            assertEquals(original.getShippingPromoSavings(), deserialized.getShippingPromoSavings(), 0.001);
            assertEquals(original.getCartTotal(), deserialized.getCartTotal(), 0.001);
            assertEquals(original.getShoppingCartItemList().size(), deserialized.getShoppingCartItemList().size());
        }
    }
}
