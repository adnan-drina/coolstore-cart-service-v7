# S01 Platform Conversion Tasks

#### T-001: Replace Spring Boot parent with Quarkus platform BOM
**Class**: rewrite  
**Findings**: javaee-pom-to-quarkus-00010 (1), springboot-parent-pom-to-quarkus-00000 (1)  
**Goal**: Convert Spring Boot parent POM to Quarkus 3.27.3.SP1 platform BOM  
**Acceptance**: `/projects/modernized/pom.xml` with Quarkus BOM parent; no Spring Boot parent references

#### T-002: Replace Spring Boot Maven plugin with Quarkus Maven plugin
**Class**: rewrite  
**Findings**: javaee-pom-to-quarkus-00020 (1), springboot-plugins-to-quarkus-0000 (1)  
**Goal**: Update Maven build plugin from spring-boot-maven-plugin to quarkus-maven-plugin  
**Acceptance**: `/projects/modernized/pom.xml` with Quarkus Maven plugin configuration

#### T-003: Update Maven plugins to Quarkus-compatible versions
**Class**: rewrite  
**Findings**: javaee-pom-to-quarkus-00030 (1), javaee-pom-to-quarkus-00040 (1), javaee-pom-to-quarkus-00050 (1)  
**Goal**: Add Maven Compiler, Surefire, and Failsafe plugins with Quarkus-compatible versions  
**Acceptance**: `/projects/modernized/pom.xml` with explicit Maven plugin configurations

#### T-004: Add Quarkus native build profile
**Class**: rewrite  
**Findings**: javaee-pom-to-quarkus-00060 (1)  
**Goal**: Add Maven profile configuration for Quarkus native executable build  
**Acceptance**: `/projects/modernized/pom.xml` with native build profile section

#### T-005: Replace Spring Boot Actuator with Quarkus SmallRye Health
**Class**: rewrite  
**Findings**: springboot-actuator-to-quarkus-0100 (1)  
**Goal**: Replace spring-boot-starter-actuator with quarkus-smallrye-health dependency  
**Acceptance**: `/projects/modernized/pom.xml` with SmallRye Health dependency; no Spring Boot Actuator

#### T-006: Replace Micrometer metrics with Quarkus SmallRye Metrics
**Class**: rewrite  
**Findings**: springboot-metrics-to-quarkus-0100 (1)  
**Goal**: Add quarkus-smallrye-metrics dependency for metrics support  
**Acceptance**: `/projects/modernized/pom.xml` with SmallRye Metrics dependency

#### T-007: Update JAX-RS dependencies to Quarkus REST
**Class**: rewrite  
**Findings**: jakarta-jaxrs-to-quarkus-00010 (1)  
**Goal**: Replace spring-boot-starter-jersey with quarkus-rest-jackson for JAX-RS support  
**Acceptance**: `/projects/modernized/pom.xml` with Quarkus REST dependency; no Jersey dependency

#### T-008: Replace Spring Boot test framework with Quarkus test artifacts
**Class**: rewrite  
**Findings**: javaee-pom-to-quarkus-00080 (1)  
**Goal**: Update test dependencies from Spring Boot Test to Quarkus JUnit 5 and test framework  
**Acceptance**: `/projects/modernized/pom.xml` with Quarkus test dependencies; spring-boot-starter-test removed

#### T-009: Create application.properties with CATALOG_ENDPOINT
**Class**: rewrite  
**Findings**: demo-env-integration-00001 (1)  
**Goal**: Create Quarkus application.properties with CATALOG_ENDPOINT (preserve)  
**Acceptance**: `/projects/modernized/src/main/resources/application.properties` with CATALOG_ENDPOINT property; Feign dependency removed  
**UI surface: waived** (API conversion owned by deploying story)  
**Acceptance Path Waiver**: `/api/cart/acceptance-check` deferred to deploying story — that story adds a real `@Path` / Endpoint resource returning catalog `products[]` (not a status-map); S01 must not create AcceptanceEndpoint  
**Out of scope**: AcceptanceEndpoint Java class in S01 (S-AC1)

#### T-011: Resolve Spring Boot version incompatibility with Jakarta EE 9+
**Class**: infer  
**Findings**: spring-components-00001 (1)  
**Goal**: Ensure POM has no Spring Boot artifacts left that conflict with Jakarta EE 9+ / Quarkus BOM  
**Acceptance**: `pom.xml` has Quarkus BOM parent/platform deps; no `spring-boot-starter-*` remaining  
**Design**: Target file `pom.xml` — Quarkus BOM parent + platform BOM import; remove any remaining `spring-boot-starter-*` / Spring Boot plugin coordinates; do not add `src/main/java` classes in S01

#### T-012: Resolve Spring framework version incompatibility with Jakarta EE 9+
**Class**: infer  
**Findings**: spring-components-00002 (1)  
**Goal**: Ensure POM has no Spring Framework artifacts left that conflict with Jakarta EE 9+  
**Acceptance**: `pom.xml` free of Spring Framework deps (`spring-context`, `spring-web`, etc.)  
**Design**: Target file `pom.xml` — delete Spring Framework dependencies; rely on Quarkus BOM for Jakarta EE 9+; do **not** harvest `CartEndpoint` / `ShoppingCartServiceImpl` in S01 (later stories)
