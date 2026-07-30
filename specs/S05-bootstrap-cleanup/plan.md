# S05: Bootstrap and Configuration Cleanup - Plan

## Quarkus Mapping

This story focuses on removing obsolete Spring Boot bootstrap components and migrating configuration to Quarkus equivalents.

### Spring Boot Bootstrap Removal

#### CartServiceApplication.java - REMOVED
**Class**: `com.redhat.coolstore.CartServiceApplication`
- **Target**: **REMOVED** - Quarkus provides default main() with CDI bootstrap
- **Rationale**: Quarkus provides built-in CDI bootstrap without requiring @SpringBootApplication
- **Impact**: Application starts via Quarkus default bootstrap, Feign client auto-configuration handled via Quarkus REST client
- **Package Rename**: `com.redhat.coolstore.CartServiceApplication` → **REMOVED**

#### JerseyConfig.java - REMOVED
**Class**: `com.redhat.coolstore.rest.JerseyConfig`
- **Target**: **REMOVED** - Quarkus auto-discovers JAX-RS resources
- **Rationale**: Quarkus automatically discovers JAX-RS resources without manual ResourceConfig registration
- **Impact**: CartEndpoint auto-discovered through Quarkus resource scanning
- **Package Rename**: `com.redhat.coolstore.rest.JerseyConfig` → **REMOVED**

### Configuration Migration

#### application.properties - Migration to Quarkus
**File**: `src/main/resources/application.properties`
- **Spring Properties**:
  ```properties
  spring.jersey.application-path=/api
  CATALOG_ENDPOINT=http://localhost:8081
  ```
- **Quarkus Target**:
  ```properties
  # Quarkus REST configuration (replaces spring.jersey.application-path)
  quarkus.http.root-path=/api
  
  # Preserved environment-driven configuration
  CATALOG_ENDPOINT=http://localhost:8081
  ```
- **Rationale**: 
  - `spring.jersey.application-path=/api` → `quarkus.http.root-path=/api` (equivalent JAX-RS path mapping)
  - `CATALOG_ENDPOINT` preserved unchanged (environment-driven configuration per migration.yaml preserve list)
- **Package Rename**: Configuration properties contain no package references

## Package Rename Mapping

**Full Prefix Replacement**: `com.redhat.coolstore` → `com.demo`
- CartServiceApplication.java: `com.redhat.coolstore.CartServiceApplication` → **REMOVED**
- JerseyConfig.java: `com.redhat.coolstore.rest.JerseyConfig` → **REMOVED**

## Conversion Order

Following the dependency order (`extensions → models → resources → config → tests`):

1. **Configuration Migration** (springboot-properties-to-quarkus-00000)
   - Rewrite: Migrate application.properties to Quarkus format
   - Preserves environment-driven CATALOG_ENDPOINT configuration

2. **Bootstrap Cleanup** (springboot-annotations-to-quarkus-00000)
   - Rewrite: Remove CartServiceApplication.java (Spring Boot bootstrap)
   - Rewrite: Remove JerseyConfig.java (manual JAX-RS registration)

3. **Verification Tasks**
   - Test: Validate API paths (/api/cart/*) unchanged
   - Test: Verify Quarkus dev mode startup
   - Test: Confirm CATALOG_ENDPOINT configuration functional

## Key Behavioral Contracts

1. **API Path Preservation**: JAX-RS endpoints remain accessible at `/api/cart/*`
2. **Configuration Contract**: CATALOG_ENDPOINT environment-driven configuration unchanged
3. **Startup Contract**: Application starts via Quarkus bootstrap (not Spring Boot)
4. **Resource Discovery**: JAX-RS resources auto-discovered by Quarkus (not manual registration)

## Migration Decisions Made

- **Feign Client**: Removed with JerseyConfig - CatalogService modernized to Quarkus REST client in previous story
- **Spring Boot Dependencies**: All removed - Quarkus provides equivalent functionality
- **CDI Bootstrap**: Default Quarkus CDI bootstrap sufficient for all component discovery
- **JAX-RS Configuration**: Automatic discovery replaces manual ResourceConfig registration
