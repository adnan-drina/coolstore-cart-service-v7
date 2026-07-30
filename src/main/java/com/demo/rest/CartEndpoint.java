package com.demo.rest;

import java.util.List;
import java.util.Map;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.demo.model.Product;
import com.demo.model.ShoppingCart;
import com.demo.service.CatalogService;
import com.demo.service.CatalogUnavailableException;
import com.demo.service.ShoppingCartService;

@Path("/cart")
public class CartEndpoint {

    private final ShoppingCartService shoppingCartService;
    private final CatalogService catalogService;

    public CartEndpoint(ShoppingCartService shoppingCartService, @RestClient CatalogService catalogService) {
        this.shoppingCartService = shoppingCartService;
        this.catalogService = catalogService;
    }

    @GET
    @Path("/{cartId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCart(@PathParam("cartId") String cartId) {
        requireCartId(cartId);
        ShoppingCart cart = shoppingCartService.getShoppingCart(cartId);
        if (cart.getShoppingCartItemList().isEmpty()) {
            throw new NotFoundException("Cart not found: " + cartId);
        }
        return Response.ok(cart).build();
    }

    @POST
    @Path("/{cartId}/{itemId}/{quantity}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addItem(@PathParam("cartId") String cartId,
                            @PathParam("itemId") String itemId,
                            @PathParam("quantity") int quantity) {
        requireCartId(cartId);
        requireItemId(itemId);
        requirePositiveQuantity(quantity);
        ShoppingCart cart = shoppingCartService.addItem(cartId, itemId, quantity);
        return Response.ok(cart).build();
    }

    @POST
    @Path("/{cartId}/{tmpId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setCart(@PathParam("cartId") String cartId,
                            @PathParam("tmpId") String tmpId) {
        requireCartId(cartId);
        requireItemId(tmpId);
        ShoppingCart cart = shoppingCartService.set(cartId, tmpId);
        return Response.ok(cart).build();
    }

    @DELETE
    @Path("/{cartId}/{itemId}/{quantity}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteItem(@PathParam("cartId") String cartId,
                               @PathParam("itemId") String itemId,
                               @PathParam("quantity") int quantity) {
        requireCartId(cartId);
        requireItemId(itemId);
        requirePositiveQuantity(quantity);
        ShoppingCart cart = shoppingCartService.deleteItem(cartId, itemId, quantity);
        return Response.ok(cart).build();
    }

    @POST
    @Path("/checkout/{cartId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkout(@PathParam("cartId") String cartId) {
        requireCartId(cartId);
        ShoppingCart cart = shoppingCartService.checkout(cartId);
        return Response.ok(cart).build();
    }

    @GET
    @Path("/acceptance-check")
    @Produces(MediaType.APPLICATION_JSON)
    public Response acceptanceCheck() {
        try {
            List<Product> products = catalogService.products();
            if (products.isEmpty()) {
                throw new CatalogUnavailableException("Catalog returned no products");
            }
            return Response.ok(products).build();
        } catch (Exception e) {
            // Ensure we get proper 503 response when catalog service fails
            throw new CatalogUnavailableException("Catalog service unavailable: " + e.getMessage());
        }
    }

    private void requireCartId(String cartId) {
        if (cartId == null || cartId.trim().isEmpty()) {
            throw badRequest("cartId is required");
        }
    }

    private void requireItemId(String itemId) {
        if (itemId == null || itemId.trim().isEmpty()) {
            throw badRequest("itemId is required");
        }
    }

    private void requirePositiveQuantity(int quantity) {
        if (quantity <= 0) {
            throw badRequest("quantity must be positive");
        }
    }

    private WebApplicationException badRequest(String detail) {
        return new WebApplicationException(
            Response.status(400)
                .type("application/problem+json")
                .entity(Map.of("httpStatus", 400, "title", "Bad Request", "detail", detail))
                .build());
    }
}
