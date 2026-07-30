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

#### T-009: Create application.properties and acceptance endpoint structure
**Class**: rewrite  
**Findings**: demo-env-integration-00001 (1)  
**Goal**: Create Quarkus application.properties with CATALOG_ENDPOINT configuration and basic acceptance endpoint structure  
**Acceptance**: `/projects/modernized/src/main/resources/application.properties` with CATALOG_ENDPOINT property; Feign dependency removed; acceptance endpoint placeholder prepared

#### T-010: Create acceptance endpoint placeholder
**Class**: rewrite  
**Findings**: (acceptance path requirement)  
**Goal**: Create minimal acceptance endpoint structure for `/api/cart/acceptance-check`  
**Acceptance**: `/projects/modernized/src/main/java/com/demo/AcceptanceEndpoint.java` with `@Path("/cart/acceptance-check")` JAX-RS structure; endpoint returns simple status response for web surface validation

#### T-011: Resolve Spring Boot version incompatibility with Jakarta EE 9+
**Class**: infer  
**Findings**: spring-components-00001 (1)  
**Goal**: Address Spring Boot version compatibility issues with Jakarta EE 9+ namespace migration  
**Acceptance**: POM dependencies updated to eliminate Jakarta EE 9+ incompatibility; Spring Boot artifacts properly replaced or upgraded  
**Design**: `src/main/resources/application.properties` → Quarkus application.properties with `quarkus.rest-client."catalog-service.url=${CATALOG_ENDPOINT:http://localhost:8081}"` configuration; Spring Boot parent → Quarkus BOM parent with `@QuarkusMain` bootstrap; **Target**: Complete Spring Boot elimination, Quarkus 3.27.3.SP1 BOM with platform dependency management, Jakarta EE 9+ compatibility through unified coordinates

#### T-012: Resolve Spring framework version incompatibility with Jakarta EE 9+
**Class**: infer  
**Findings**: spring-components-00002 (1)  
**Goal**: Address Spring framework version compatibility issues with Jakarta EE 9+ namespace migration  
**Acceptance**: Spring dependencies updated to eliminate Jakarta EE 9+ incompatibility; javax.* → jakarta.* namespace migration complete  
**Design**: `migration/staging/src/main/java/com/redhat/coolstore/rest/CartEndpoint.java` → `src/main/java/com/demo/rest/CartEndpoint.java` with `@Path("/cart")`, `ConcurrentHashMap<String, ShoppingCart>` for thread-safe storage, and `@ApplicationScoped` CDI; `migration/staging/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java` → `src/main/java/com/demo/service/ShoppingCartServiceImpl.java` with constructor injection and `compute()` operations; **Target**: Complete dependency modernization with quarkus-rest-jackson, quarkus-smallrye-health, quarkus-smallrye-metrics replacing all Spring artifacts; javax.* → jakarta.* namespace transformation complete via OpenRewrite recipes