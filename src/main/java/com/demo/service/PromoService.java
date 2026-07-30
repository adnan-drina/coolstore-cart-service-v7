package com.demo.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.ApplicationScoped;

import com.demo.model.Promotion;
import com.demo.model.ShoppingCart;
import com.demo.model.ShoppingCartItem;

@ApplicationScoped
public class PromoService {

    private final Set<Promotion> promotionSet = ConcurrentHashMap.newKeySet();

    public PromoService() {
        promotionSet.add(new Promotion("329299", .25));
    }

    public void applyCartItemPromotions(ShoppingCart shoppingCart) {
        if (shoppingCart != null && !shoppingCart.getShoppingCartItemList().isEmpty()) {
            Map<String, Promotion> promoMap = new HashMap<>();
            for (Promotion promo : getPromotions()) {
                promoMap.put(promo.getItemId(), promo);
            }

            for (ShoppingCartItem sci : shoppingCart.getShoppingCartItemList()) {
                String productId = sci.getProduct().getItemId();
                Promotion promo = promoMap.get(productId);
                if (promo != null) {
                    sci.setPromoSavings(sci.getProduct().getPrice() * promo.getPercentOff() * -1);
                    sci.setPrice(sci.getProduct().getPrice() * (1 - promo.getPercentOff()));
                }
            }
        }
    }

    public void applyShippingPromotions(ShoppingCart shoppingCart) {
        if (shoppingCart != null && shoppingCart.getCartItemTotal() >= 75) {
            shoppingCart.setShippingPromoSavings(shoppingCart.getShippingTotal() * -1);
            shoppingCart.setShippingTotal(0);
        }
    }

    public Set<Promotion> getPromotions() {
        return Collections.unmodifiableSet(promotionSet);
    }

    public void setPromotions(Set<Promotion> promotionSet) {
        if (promotionSet != null) {
            this.promotionSet.clear();
            this.promotionSet.addAll(promotionSet);
        } else {
            this.promotionSet.clear();
        }
    }

    @Override
    public String toString() {
        return "PromoService [promotionSet=" + promotionSet + "]";
    }
}
