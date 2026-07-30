package com.demo.rest;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;

@Path("/")
public class RootEndpoint {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response index() {
        return Response.ok(Map.of(
            "service", "coolstore-cart",
            "version", "1.0.0",
            "endpoints", List.of(
                "/api/cart/{cartId}",
                "/api/cart/acceptance-check", 
                "/q/health"
            ),
            "root", "/"
        )).build();
    }
}