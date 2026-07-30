# S05: Bootstrap and Configuration Cleanup Tasks

#### T-001: Migrate Spring application.properties to Quarkus configuration
**Class**: rewrite
**Findings**: springboot-properties-to-quarkus-00000 (1)
**Goal**: Convert Spring Boot properties to Quarkus configuration format
**Target design**: 
- src/main/resources/application.properties: Replace spring.jersey.application-path with quarkus.http.root-path, preserve CATALOG_ENDPOINT environment configuration
**Acceptance**: application.properties contains Quarkus configuration; sensors green

#### T-002: Remove CartServiceApplication Spring Boot bootstrap class
**Class**: rewrite
**Findings**: springboot-annotations-to-quarkus-00000 (1)
**Goal**: Remove obsolete Spring Boot main class replaced by Quarkus default bootstrap
**Target design**: 
- src/main/java/com/redhat/coolstore/CartServiceApplication.java: File removed (Quarkus provides default main() with CDI bootstrap)
**Acceptance**: CartServiceApplication.java does not exist; application starts via Quarkus default bootstrap; sensors green

#### T-003: Remove JerseyConfig manual JAX-RS resource registration
**Class**: rewrite
**Findings**: springboot-annotations-to-quarkus-00000 (1)
**Goal**: Remove manual JAX-RS resource configuration replaced by Quarkus auto-discovery
**Target design**: 
- src/main/java/com/redhat/coolstore/rest/JerseyConfig.java: File removed (Quarkus auto-discovers JAX-RS resources)
**Acceptance**: JerseyConfig.java does not exist; JAX-RS resources auto-discovered by Quarkus; sensors green

#### T-004: Verify API path preservation and configuration functionality
**Class**: rewrite
**Findings**: springboot-properties-to-quarkus-00000 (1), springboot-annotations-to-quarkus-00000 (1)
**Goal**: Validate that migrated configuration preserves API paths and Quarkus configuration is functional
**Target design**: 
- src/main/resources/application.properties: Contains quarkus.http.root-path=/api and CATALOG_ENDPOINT configuration
**Acceptance**: application.properties contains proper Quarkus configuration; API paths preserved; sensors green

#### T-005: Package directory cleanup
**Class**: rewrite
**Findings**: (none - structural cleanup)
**Goal**: Ensure package directories remain committable after file removal
**Target design**: 
- src/main/java/com/demo/rest: Create .gitkeep to maintain directory structure after JerseyConfig.java removal
**Acceptance**: Package directories remain in repository; no empty directory errors; sensors green

#### T-006: Implement acceptance check endpoint
**Class**: infer
**Findings**: (none - new functionality required)
**Goal**: Implement the migration.yaml acceptance path endpoint required by the deployment gate
**Target design**: 
- src/main/java/com/demo/rest/HealthEndpoint.java: Create JAX-RS endpoint serving /api/cart/acceptance-check
- Returns JSON response indicating service health and cart functionality
**Acceptance**: GET /api/cart/acceptance-check returns 200 OK with health status; migration acceptance gate passes; sensors green

#### S05-WAIVER: Legacy UI Surface
**Class**: infer
**Findings**: (waiver)
**Goal**: Waive legacy UI surface coverage requirement
**Target design**: 
- This story handles only bootstrap cleanup and configuration migration
- No legacy web UI surface exists - application is pure REST API
- REST API endpoints tested implicitly through service functionality tests
**Acceptance**: Waiver documented; no UI surface to test; sensors green
