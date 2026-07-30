package com.demo.rest;

import com.github.tomakehurst.wiremock.WireMockServer;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;

import java.util.Map;

public class CatalogWireMockResource implements QuarkusTestResourceLifecycleManager {

    public CatalogWireMockResource() {
    }

    private static WireMockServer server;

    static WireMockServer getServer() {
        return server;
    }

    @Override
    public Map<String, String> start() {
        server = new WireMockServer(0);
        server.start();
        return Map.of("quarkus.rest-client.\"catalog-service\".url", server.baseUrl());
    }

    @Override
    public void stop() {
        if (server != null) {
            server.stop();
        }
    }
}
