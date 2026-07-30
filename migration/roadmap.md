# Modernization roadmap

## S01: Platform and BOM conversion
- scope: pom.xml
- findings: javaee-pom-to-quarkus-00010, javaee-pom-to-quarkus-00020, javaee-pom-to-quarkus-00030, javaee-pom-to-quarkus-00040, javaee-pom-to-quarkus-00050, javaee-pom-to-quarkus-00060, javaee-pom-to-quarkus-00080, springboot-parent-pom-to-quarkus-00000, springboot-plugins-to-quarkus-0000, springboot-actuator-to-quarkus-0100, springboot-metrics-to-quarkus-0100, jakarta-jaxrs-to-quarkus-00010, spring-components-00001, spring-components-00002
- depends: -
- deploy: false
- done: Quarkus BOM configured with platform dependencies, Maven plugins updated, Spring Boot dependencies replaced with Quarkus equivalents
- rationale: Foundation layer must be established before any code changes. All POM-based transformations must complete to ensure proper dependency resolution for subsequent stories.

## S02: Domain model harvest
- scope: src/main/java/com/redhat/coolstore/model/Product.java, src/main/java/com/redhat/coolstore/model/Promotion.java, src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java, src/main/java/com/redhat/coolstore/model/ShoppingCart.java
- findings: removed-javaee-modules-00020
- depends: S01
- deploy: false
- done: Domain models harvested with package rename to com.demo, all legacy business logic preserved, characterization tests establish baseline behavior
- rationale: Dependency order §18-23 shows models must convert first as god-nodes with highest fan-in. HARVEST classes carry over faithfully to preserve behavioral contracts.

## S03: Service layer modernization
- scope: src/main/java/com/redhat/coolstore/service/ShoppingCartService.java, src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java, src/main/java/com/redhat/coolstore/service/PromoService.java, src/main/java/com/redhat/coolstore/service/ShippingService.java, src/main/java/com/redhat/coolstore/service/CatalogService.java
- findings: springboot-web-to-quarkus-00000, localhost-http-00001, demo-env-integration-00001, springboot-di-to-quarkus-00000, springboot-metrics-to-quarkus-0200
- depends: S02
- deploy: false
- done: All services converted to @ApplicationScoped CDI with constructor injection, Feign client replaced with Quarkus REST client, environment-driven configuration preserved
- rationale: Service layer depends on models per dependency order §24-28. REDESIGN classes implement target contract from architecture-profile §7: thread-safe state, cache policy, normalize-before-derive.

## S04: REST endpoint modernization
- scope: src/main/java/com/redhat/coolstore/rest/CartEndpoint.java
- findings: springboot-di-to-quarkus-00003
- depends: S03
- deploy: true
- done: JAX-RS endpoints modernized with Quarkus annotations, stateless design with proper error handling, GET idempotency implemented (404 on missing cartId)
- rationale: REST surface depends on services per dependency order §27. REDESIGN class implements target contract from architecture-profile §7: stateless operations, input validation, error mapping.

## S05: Bootstrap and configuration cleanup
- scope: src/main/java/com/redhat/coolstore/CartServiceApplication.java, src/main/java/com/redhat/coolstore/rest/JerseyConfig.java, src/main/resources/application.properties
- findings: springboot-annotations-to-quarkus-00000, springboot-properties-to-quarkus-00000
- depends: S04
- deploy: true
- done: Spring Boot bootstrap classes removed, Jersey config eliminated (Quarkus auto-discovery), Spring properties migrated to Quarkus configuration
- rationale: Cleanup phase removes obsolete components now subsumed by Quarkus. Final story applies remaining configuration transformations.
