package com.demo.rest;

import java.util.Map;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

class CartExceptionMappers {

    @ServerExceptionMapper
    Response mapNotFound(NotFoundException e) {
        return Response.status(404)
            .type("application/problem+json")
            .entity(Map.of("status", 404, "title", "Not Found", "detail", e.getMessage()))
            .build();
    }

    @ServerExceptionMapper
    Response mapClientWebApplicationException(ClientWebApplicationException e) {
        int status = e.getResponse().getStatus();
        return Response.status(status)
            .type("application/problem+json")
            .entity(Map.of("status", status, "title", "Service Unavailable", "detail", e.getMessage()))
            .build();
    }
}
