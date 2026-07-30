package com.demo.rest;

import java.util.Map;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/cart/acceptance-check")
public class AcceptanceEndpoint {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response check() {
        return Response.ok(Map.of("status", "ok")).build();
    }
}
