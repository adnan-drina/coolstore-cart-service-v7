package com.demo.rest;

import java.util.Map;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

class CartExceptionMappers {

    @ServerExceptionMapper
    Response mapNotFound(NotFoundException e) {
        return Response.status(404)
            .type("application/problem+json")
            .entity(Map.of("status", 404, "title", "Not Found", "detail", e.getMessage()))
            .build();
    }
}
