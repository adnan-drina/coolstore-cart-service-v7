package com.demo.service;

public class CatalogUnavailableException extends RuntimeException {
    public CatalogUnavailableException(String message) {
        super(message);
    }
    
    public CatalogUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}