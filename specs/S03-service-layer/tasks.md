# S03 Service Layer Tasks

#### T-001: Create service package structure
**Class**: rewrite
**Findings**: N/A
**Goal**: Create com.demo.service package directory structure
**Target design**:
- Directory: src/main/java/com/demo/service/
**Acceptance**: Directory exists; subsequent tasks can place files here
- Trackable marker: `src/main/java/com/demo/service/.gitkeep` (O-PKGDIR — empty dirs are not git-committable).

#### T-002: Confirm Quarkus metrics extension replaces Spring Actuator/Micrometer
**Class**: rewrite
**Findings**: springboot-metrics-to-quarkus-0200 (1)
**Goal**: Close the Actuator/Micrometer → MicroProfile Metrics finding for this story. S01 already added `quarkus-smallrye-metrics`; verify it remains in pom.xml and that no Spring Boot Actuator/Micrometer dependency remains in the modernized tree.
**Target design**:
- pom.xml keeps `io.quarkus:quarkus-smallrye-metrics`
- No `spring-boot-starter-actuator` / micrometer-spring dependency in modernized pom.xml
**Acceptance**: `grep quarkus-smallrye-metrics pom.xml` succeeds; Actuator artifact absent from pom.xml

#### T-003: Confirm Catalog REST client URL configuration
**Class**: rewrite
**Findings**: demo-env-integration-00001 (1), localhost-http-00001 (1)
**Goal**: Ensure CatalogService REST client URL is environment-driven via CATALOG_ENDPOINT (platform already seeded application.properties; keep or restore the property if missing).
**Target design**:
- File: src/main/resources/application.properties
- Property: `quarkus.rest-client."catalog-service".url=${CATALOG_ENDPOINT:http://localhost:8081}`
**Acceptance**: Property present with CATALOG_ENDPOINT override; default localhost:8081 retained for local demo

#### T-004: PromoService harvest with CDI conversion
**Class**: infer
**Findings**: springboot-di-to-quarkus-00000 (1)
**Goal**: Harvest and modernize PromoService with Quarkus CDI patterns
**Target design**:
- migration/staging/src/main/java/com/redhat/coolstore/service/PromoService.java → src/main/java/com/demo/service/PromoService.java
- Package: com.redhat.coolstore.service → com.demo.service
- Annotations: @Component → @ApplicationScoped
- Injection: no anonymous mutable set exposed; promotions held in ConcurrentHashMap (or ConcurrentHashMap.newKeySet) for thread-safe singleton state
- Business logic: Preserve 25% discount on item "329299"
**Acceptance**: PromoService compiles with @ApplicationScoped; promotion storage is thread-safe; business logic preserved

#### T-005: ShippingService harvest with CDI conversion
**Class**: infer
**Findings**: springboot-di-to-quarkus-00000 (1)
**Goal**: Harvest and modernize ShippingService with Quarkus CDI patterns
**Target design**:
- migration/staging/src/main/java/com/redhat/coolstore/service/ShippingService.java → src/main/java/com/demo/service/ShippingService.java
- Package: com.redhat.coolstore.service → com.demo.service
- Annotations: @Component → @ApplicationScoped
- Business logic: Preserve exact tiered shipping ($2.99 for $0-$25, $4.99 for $25-$50, higher tiers per legacy)
**Acceptance**: ShippingService compiles with @ApplicationScoped; shipping calculation logic unchanged

#### T-006: CatalogService conversion with REST client
**Class**: infer
**Findings**: springboot-web-to-quarkus-00000 (1), springboot-di-to-quarkus-00000 (1), demo-env-integration-00001 (1)
**Goal**: Convert CatalogService from FeignClient to Quarkus REST client
**Target design**:
- Legacy/staging CatalogService → src/main/java/com/demo/service/CatalogService.java
- Package: com.redhat.coolstore.service → com.demo.service
- Client: @FeignClient → @RegisterRestClient(configKey = "catalog-service")
- JAX-RS: @GetMapping("/api/products") → @GET @Path("/api/products")
- Configuration: Environment-driven CATALOG_ENDPOINT via configKey catalog-service
**Acceptance**: CatalogService compiles with @RegisterRestClient; JAX-RS annotations correct; environment configuration preserved

#### T-007: ShoppingCartService interface + ShoppingCartServiceImpl CDI redesign
**Class**: infer
**Findings**: springboot-di-to-quarkus-00000 (1), localhost-http-00001 (1), demo-env-integration-00001 (1)
**Goal**: Harvest CartService/ShoppingCartService interface and ShoppingCartServiceImpl with thread-safe storage and constructor injection
**Target design**:
- migration/staging/.../ShoppingCartService.java (or CartService) → src/main/java/com/demo/service/ (package com.demo.service)
- migration/staging/.../ShoppingCartServiceImpl.java → src/main/java/com/demo/service/ShoppingCartServiceImpl.java
- Annotations: @Service → @ApplicationScoped
- Injection: @Autowired fields → @Inject constructor with final ShippingService, CatalogService, PromoService
- Storage: HashMap → ConcurrentHashMap with compute() operations (thread-safe)
- Cache: Product cache with refresh guard (bounded TTL, no clear-on-miss)
- Normalize-before-derive for cart totals
- Error handling: 503 via JAX-RS ExceptionMapper for catalog failures — place mapper at src/main/java/com/demo/service/CatalogUnavailableExceptionMapper.java; endpoint wiring waits for S04
**Acceptance**: ShoppingCartServiceImpl compiles with constructor injection; cart storage is ConcurrentHashMap/compute; cache refresh-guard present; dependency injection works

#### T-008: Add service layer characterization tests
**Class**: infer
**Findings**: N/A
**Goal**: Add characterization tests pinning service-layer target behavior for classes this story owns
**Target design**:
- Tests: src/test/java/com/demo/service/ShoppingCartServiceTest.java, PromoServiceTest.java, ShippingServiceTest.java
- Pin promotion: 25% discount on item "329299"
- Pin shipping tiers: $2.99 / $4.99 preserved
- Pin ConcurrentHashMap cart storage / additive cart semantics per architecture-profile §7
- Tests use @QuarkusTest with CDI; do not invent REST endpoints
**Acceptance**: Service characterization tests compile and pass; business rules verified

#### T-009: Package rename verification for service layer
**Class**: infer
**Findings**: N/A
**Goal**: Verify no legacy service package references remain under src/main for this story's types
**Target design**:
- Command: `find src/main/java/com/demo/service -name "*.java" | xargs grep -l "com.redhat.coolstore" | wc -l` → 0
- Also: zero files under src/main/java/com/redhat/coolstore/service/
**Acceptance**: Verification returns 0; legacy service package references eliminated from migrated service types

## Story Scope Waivers

**UI Surface**: Explicitly waived — S03 is service layer modernization only, no REST endpoints or web surface. UI coverage is owned by REST endpoint stories (S04+).

**Acceptance Path `/api/cart/acceptance-check`**: Explicitly waived — REST endpoint acceptance testing requires a JAX-RS `@Path` resource; S03 brief keeps JAX-RS conversion for the REST story (S04). Service contracts and ExceptionMapper preparation may land here; HTTP acceptance path waits for S04.
