package com.demo.service;

import java.util.logging.Logger;

import jakarta.ws.rs.core.Response;

import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class CatalogUnavailableExceptionMapper implements ExceptionMapper<CatalogUnavailableException> {

    private static final Logger LOG = Logger.getLogger(CatalogUnavailableExceptionMapper.class.getName());

    @Override
    public Response toResponse(CatalogUnavailableException exception) {
        LOG.warning("Catalog service unavailable: " + exception.getMessage());
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity("Catalog service unavailable: " + exception.getMessage())
                .build();
    }
}
