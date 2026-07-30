# S05: Bootstrap and Configuration Cleanup - Specification

## Behavioral Contract & API Surface

This story performs final cleanup of obsolete Spring Boot bootstrap and configuration that are now subsumed by Quarkus auto-discovery. No behavioral changes to the application API are introduced.

## Legacy Evidence

### CartServiceApplication.java - Spring Boot Bootstrap
**File**: `src/main/java/com/redhat/coolstore/CartServiceApplication.java`

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
}
```

**Role**: Main bootstrap class using Spring Boot's `@SpringBootApplication` and `SpringApplication.run()` to initialize the Spring context and enable Feign client auto-configuration.

### JerseyConfig.java - Manual JAX-RS Resource Registration
**File**: `src/main/java/com/redhat/coolstore/rest/JerseyConfig.java`

```java
package com.redhat.coolstore.rest;

import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.stereotype.Component;

@Component
public class JerseyConfig extends ResourceConfig {
    public JerseyConfig() {
        register(CartEndpoint.class);
    }
}
```

**Role**: Manual JAX-RS resource configuration registering `CartEndpoint` with Jersey's `ResourceConfig`. Uses Spring `@Component` for lifecycle management.

### application.properties - Spring Boot Configuration
**File**: `src/main/resources/application.properties`

```properties
spring.application.name=coolstore-cart-legacy
spring.jersey.application-path=/api

# Catalog products endpoint used by the Feign CatalogService.
# Override with env CATALOG_ENDPOINT or -DCATALOG_ENDPOINT=...
CATALOG_ENDPOINT=http://localhost:8081
```

**Configuration Purpose**:
- `spring.application.name` - Application identification
- `spring.jersey.application-path=/api` - JAX-RS application path mapping
- `CATALOG_ENDPOINT=http://localhost:8081` - External catalog service integration endpoint (environment-driven)

## API Contract Preservation

### REST API Endpoints
The application's REST API surface remains unchanged:
- `GET /cart/{cartId}` - retrieves cart state
- `POST /cart/{cartId}/{itemId}/{quantity}` - adds items to cart
- `DELETE /cart/{cartId}/{itemId}/{quantity}` - removes items from cart  
- `POST /cart/{cartId}/{tmpId}` - transfers cart items between carts
- `POST /cart/checkout/{cartId}` - processes checkout

**Path Mapping**: The `/api` context path is preserved by migrating `spring.jersey.application-path=/api` to Quarkus `quarkus.http.root-path=/api`.

### Configuration Contract
**CATALOG_ENDPOINT**: Environment-driven configuration preserved exactly. Service integrates with external catalog service via this endpoint for product data retrieval.

## Class Roles (from Architecture Profile)

### REDESIGN Classes
**CartServiceApplication** - REMOVED
- Target: Removed entirely
- Rationale: Quarkus provides default main() with CDI bootstrap, no custom bootstrap required

**JerseyConfig** - REMOVED  
- Target: Removed entirely
- Rationale: Quarkus auto-discovers JAX-RS resources without manual ResourceConfig registration

## Behavioral Pins

1. **API Path Preservation**: `/api/cart/*` endpoints maintain identical access paths
2. **Configuration Preservation**: CATALOG_ENDPOINT environment-driven configuration unchanged
3. **Startup Behavior**: Application starts without Spring Boot dependencies
4. **CDI Bootstrap**: Quarkus default CDI bootstrap provides same component discovery
5. **Resource Discovery**: CartEndpoint auto-discovered by Quarkus (no manual registration)

## Migration Findings Resolved

- **springboot-annotations-to-quarkus-00000**: Remove Spring Boot bootstrap (`CartServiceApplication`)
- **springboot-properties-to-quarkus-00000**: Convert Spring properties to Quarkus configuration

## Package Rename

Legacy package `com.redhat.coolstore` → Target package `com.demo` (full prefix replacement only)
