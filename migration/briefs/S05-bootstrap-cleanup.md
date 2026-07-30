# S05: Bootstrap and configuration cleanup

<!-- The brief is the self-contained work order for one modernization
     story. Bar: a competent developer or a fresh session starts the
     story from THIS FILE ALONE. Fill every section; delete none. -->

## Goal & position

Remove obsolete Spring Boot bootstrap classes and configuration that are now subsumed by Quarkus auto-discovery. This cleanup story finalizes the migration by eliminating JerseyConfig (Quarkus auto-discovers JAX-RS resources) and CartServiceApplication (Quarkus default main() with CDI bootstrap), and converting Spring properties to Quarkus configuration. This story depends on all previous stories and completes the modernization with the final configuration transformations.

## In scope

The exact legacy classes/files this story modernizes. For each, quote
the load-bearing legacy code (the lines being transformed — imports,
annotations, key methods), so the story never starts from a blank
read:

- `src/main/java/com/redhat/coolstore/CartServiceApplication.java` — removed
  ```java
  package com.redhat.coolstore;
  
  import org.springframework.boot.SpringApplication;
  import org.springframework.boot.autoconfigure.SpringBootApplication;
  import org.springframework.cloud.openfeign.EnableFeignClients;
  
  @SpringBootApplication
  @EnableFeignClients
  public class CartServiceApplication {
      public static void main(String[] args) {
          SpringApplication.run(CartServiceApplication.class, args);
      }
  ```

- `src/main/java/com/redhat/coolstore/rest/JerseyConfig.java` — removed
  ```java
  package com.redhat.coolstore.rest;
  
  import org.glassfish.jersey.server.ResourceConfig;
  import org.springframework.stereotype.Component;
  
  @Component
  public class JerseyConfig extends ResourceConfig {
      public JerseyConfig() {
          register(CartEndpoint.class);
      }
  ```

- `src/main/resources/application.properties` — configuration migration
  ```properties
  spring.jersey.application-path=/api
  CATALOG_ENDPOINT=http://localhost:8081
  ```

## Out of scope

All application code has been modernized in previous stories. This story only handles bootstrap cleanup and configuration migration. No service classes, REST endpoints, or domain models are modified.

## Class roles & target contract (from architecture-profile §7)

For each in-scope class, its role and — for REDESIGN classes — the target
contract carried forward from profile §7, so M3 writes tasks and tests to
the target (not the legacy):

- `CartServiceApplication` — REDESIGN (REMOVED)
  - target: **removed — Quarkus default main() with CDI bootstrap**
  - Rationale: Quarkus provides built-in CDI bootstrap without requiring @SpringBootApplication

- `JerseyConfig` — REDESIGN (REMOVED)
  - target: **removed — Quarkus auto-discovers JAX-RS resources**
  - Rationale: Quarkus automatically discovers JAX-RS resources without manual ResourceConfig registration

## Decided target shapes

The MAPPINGS.md rows that apply (quote the decided target, don't
re-decide). Recipe-executed rules already handled: reference
`migration/recipe-log.md` and `migration/staging/` where applicable.

**Story ordering:** extensions and BOM first, then models, then resources,
then config keys, then tests (`extensions → models → resources → config →
tests`).

Spring Boot bootstrap removal (springboot-annotations-to-quarkus-00000): Delete @SpringBootApplication main class:
```
REMOVE: src/main/java/com/redhat/coolstore/CartServiceApplication.java
RATIONALE: Quarkus provides default main() with CDI bootstrap
```

Spring configuration to Quarkus properties conversion (springboot-properties-to-quarkus-00000): Migrate application.properties:
```properties
# Quarkus REST configuration (replaces spring.jersey.application-path)
quarkus.http.root-path=/api

# Preserved environment-driven configuration
CATALOG_ENDPOINT=http://localhost:8081
```

Jersey configuration removal: Delete JerseyConfig class:
```
REMOVE: src/main/java/com/redhat/coolstore/rest/JerseyConfig.java
RATIONALE: Quarkus auto-discovers JAX-RS resources
```

## Contracts owned by this story

- **Findings**: the mandatory rule ids this story resolves (from the
  roadmap entry).
  - springboot-annotations-to-quarkus-00000, springboot-properties-to-quarkus-00000

- **Preserve**: CATALOG_ENDPOINT environment-driven configuration (preserve: from migration.yaml)

- **Behavioral pins**: Cleanup operations don't change behavior:
  - **API path:** `/api/cart/*` endpoints maintained (mapped from spring.jersey.application-path=/api)
  - **CATALOG_ENDPOINT:** Environment-driven configuration preserved exactly as configured

- **Forbidden**: none additional - all forbidden items handled in previous stories.

## Done-criteria

Checkable, story-scoped:
- builds + `sensors.sh task` green at every commit; milestone green at
  story end
- CartServiceApplication.java removed (no Spring Boot bootstrap)
- JerseyConfig.java removed (Quarkus auto-discovery active)
- application.properties migrated to Quarkus configuration format
- `/api/cart/*` endpoints still accessible at correct paths
- CATALOG_ENDPOINT configuration preserved and functional
- Quarkus dev mode starts without Spring Boot dependencies
- 2 findings resolved (no longer fire on re-analysis)
- Migration complete: all mandatory findings resolved, full test suite green
