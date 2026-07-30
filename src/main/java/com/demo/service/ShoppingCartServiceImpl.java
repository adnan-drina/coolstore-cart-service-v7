package com.demo.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.demo.model.Product;
import com.demo.model.ShoppingCart;
import com.demo.model.ShoppingCartItem;

@ApplicationScoped
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private static final long CACHE_TTL_SECONDS = 300;

    private final ShippingService shippingService;
    private final CatalogService catalogService;
    private final PromoService promoService;

    private final Map<String, ShoppingCart> carts = new ConcurrentHashMap<>();
    private final Map<String, Product> productMap = new ConcurrentHashMap<>();

    private volatile Instant productCacheTimestamp = Instant.EPOCH;

    @Inject
    public ShoppingCartServiceImpl(ShippingService shippingService, @RestClient CatalogService catalogService, PromoService promoService) {
        this.shippingService = shippingService;
        this.catalogService = catalogService;
        this.promoService = promoService;
    }

    @Override
    public ShoppingCart getShoppingCart(String cartId) {
        ShoppingCart cart = carts.compute(cartId, (key, value) -> {
            if (value == null) {
                return new ShoppingCart(cartId);
            }
            return value;
        });
        priceShoppingCart(cart);
        return cart;
    }

    @Override
    public void priceShoppingCart(ShoppingCart sc) {
        if (sc != null) {
            initShoppingCartForPricing(sc);

            if (sc.getShoppingCartItemList() != null && !sc.getShoppingCartItemList().isEmpty()) {
                promoService.applyCartItemPromotions(sc);

                for (ShoppingCartItem sci : sc.getShoppingCartItemList()) {
                    sc.setCartItemPromoSavings(sc.getCartItemPromoSavings() + sci.getPromoSavings() * sci.getQuantity());
                    sc.setCartItemTotal(sc.getCartItemTotal() + sci.getPrice() * sci.getQuantity());
                }

                shippingService.calculateShipping(sc);
            }

            promoService.applyShippingPromotions(sc);

            sc.setCartTotal(sc.getCartItemTotal() + sc.getShippingTotal());
        }
    }

    void initShoppingCartForPricing(ShoppingCart sc) {
        sc.setCartItemTotal(0);
        sc.setCartItemPromoSavings(0);
        sc.setShippingTotal(0);
        sc.setShippingPromoSavings(0);
        sc.setCartTotal(0);

        for (ShoppingCartItem sci : sc.getShoppingCartItemList()) {
            Product p = getProduct(sci.getProduct().getItemId());

            if (p != null) {
                sci.setProduct(new Product(p.getItemId(), p.getName(), p.getDesc(), p.getPrice()));
                sci.setPrice(p.getPrice());
            }

            sci.setPromoSavings(0);
        }
    }

    @Override
    public Product getProduct(String itemId) {
        Product cached = productMap.get(itemId);
        if (cached != null) {
            return cached;
        }

        if (shouldRefreshCache()) {
            refreshProductCache();
        }

        return productMap.get(itemId);
    }

    private boolean shouldRefreshCache() {
        return productMap.isEmpty() || Instant.now().isAfter(productCacheTimestamp.plusSeconds(CACHE_TTL_SECONDS));
    }

    private void refreshProductCache() {
        try {
            List<Product> products = catalogService.products();
            Map<String, Product> refreshed = products.stream()
                    .collect(Collectors.toMap(Product::getItemId, Function.identity()));
            productMap.clear();
            productMap.putAll(refreshed);
            productCacheTimestamp = Instant.now();
        } catch (Exception e) {
            throw new CatalogUnavailableException("Failed to fetch products from catalog service", e);
        }
    }

    @Override
    public ShoppingCart deleteItem(String cartId, String itemId, int quantity) {
        ShoppingCart cart = getShoppingCart(cartId);

        List<ShoppingCartItem> toRemoveList = new ArrayList<>();

        cart.getShoppingCartItemList().stream()
                .filter(sci -> sci.getProduct().getItemId().equals(itemId))
                .forEach(sci -> {
                    if (quantity >= sci.getQuantity()) {
                        toRemoveList.add(sci);
                    } else {
                        sci.setQuantity(sci.getQuantity() - quantity);
                    }
                });

        toRemoveList.forEach(cart::removeShoppingCartItem);
        priceShoppingCart(cart);
        carts.put(cartId, cart);

        return cart;
    }

    @Override
    public ShoppingCart checkout(String cartId) {
        ShoppingCart cart = getShoppingCart(cartId);
        cart.resetShoppingCartItemList();
        priceShoppingCart(cart);
        carts.put(cartId, cart);
        return cart;
    }

    @Override
    public ShoppingCart addItem(String cartId, String itemId, int quantity) {
        ShoppingCart cart = getShoppingCart(cartId);
        Product product = getProduct(itemId);

        if (product == null) {
            return cart;
        }

        ShoppingCartItem sci = new ShoppingCartItem();
        sci.setProduct(product);
        sci.setQuantity(quantity);
        sci.setPrice(product.getPrice());
        cart.addShoppingCartItem(sci);

        try {
            priceShoppingCart(cart);
            cart.setShoppingCartItemList(dedupeCartItems(cart));
        } catch (Exception ex) {
            cart.removeShoppingCartItem(sci);
            throw ex;
        }

        carts.put(cartId, cart);
        return cart;
    }

    @Override
    public ShoppingCart set(String cartId, String tmpId) {
        ShoppingCart cart = getShoppingCart(cartId);
        ShoppingCart tmpCart = getShoppingCart(tmpId);

        if (tmpCart != null) {
            cart.resetShoppingCartItemList();
            cart.setShoppingCartItemList(tmpCart.getShoppingCartItemList());
        }

        priceShoppingCart(cart);
        cart.setShoppingCartItemList(dedupeCartItems(cart));

        carts.put(cartId, cart);
        return cart;
    }

    List<ShoppingCartItem> dedupeCartItems(ShoppingCart sc) {
        List<ShoppingCartItem> result = new ArrayList<>();
        Map<String, Integer> quantityMap = new HashMap<>();
        for (ShoppingCartItem sci : sc.getShoppingCartItemList()) {
            if (quantityMap.containsKey(sci.getProduct().getItemId())) {
                quantityMap.put(sci.getProduct().getItemId(), quantityMap.get(sci.getProduct().getItemId()) + sci.getQuantity());
            } else {
                quantityMap.put(sci.getProduct().getItemId(), sci.getQuantity());
            }
        }

        for (Map.Entry<String, Integer> entry : quantityMap.entrySet()) {
            String itemId = entry.getKey();
            Product p = getProduct(itemId);
            ShoppingCartItem newItem = new ShoppingCartItem();
            newItem.setQuantity(entry.getValue());
            newItem.setPrice(p.getPrice());
            newItem.setProduct(p);
            result.add(newItem);
        }

        return result;
    }
}
