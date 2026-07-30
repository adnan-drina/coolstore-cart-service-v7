package com.demo.rest;

import java.util.Map;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

import org.jboss.resteasy.reactive.ClientWebApplicationException;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import com.demo.service.CatalogUnavailableException;

class CartExceptionMappers {
    
    private static final String CONTENT_TYPE = "application/problem+json";
    private static final String TITLE_NOT_FOUND = "Not Found";
    private static final String TITLE_SERVICE_UNAVAILABLE = "Service Unavailable";

    private static Response buildServiceUnavailableResponse(String detail) {
        return Response.status(503)
            .type(CONTENT_TYPE)
            .entity(Map.of("status", 503, "title", TITLE_SERVICE_UNAVAILABLE, "detail", detail))
            .build();
    }

    private static Response buildNotFoundResponse(String detail) {
        return Response.status(404)
            .type(CONTENT_TYPE)
            .entity(Map.of("status", 404, "title", TITLE_NOT_FOUND, "detail", detail))
            .build();
    }

    @ServerExceptionMapper
    Response mapNotFound(NotFoundException e) {
        return buildNotFoundResponse(e.getMessage());
    }

    @ServerExceptionMapper
    Response mapCatalogUnavailableException(CatalogUnavailableException e) {
        return buildServiceUnavailableResponse(e.getMessage());
    }

    @ServerExceptionMapper
    Response mapClientWebApplicationException(ClientWebApplicationException e) {
        int status = e.getResponse().getStatus();
        // Only map 503 responses, let other errors fall through to default handling
        if (status == 503) {
            return buildServiceUnavailableResponse(e.getMessage());
        }
        throw e; // Re-throw to let Quarkus handle other status codes
    }
}
