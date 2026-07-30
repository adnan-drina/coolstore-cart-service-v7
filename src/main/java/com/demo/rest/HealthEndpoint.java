package com.demo.rest;

import java.util.List;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.rest.client.inject.RestClient;

import com.demo.model.Product;
import com.demo.service.CatalogService;
import com.demo.service.CatalogUnavailableException;

@Path("/cart")
class HealthEndpoint {

    private final CatalogService catalogService;

    HealthEndpoint(@RestClient CatalogService catalogService) {
        this.catalogService = catalogService;
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
            throw new CatalogUnavailableException("Catalog service unavailable: " + e.getMessage());
        }
    }
}
