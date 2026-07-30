# S04 REST Endpoint Modernization - Plan

## Scope & Objectives

Convert the `CartEndpoint` REDESIGN class from Spring REST to Quarkus JAX-RS with stateless design, proper error handling, and idempotent GET semantics.

## Target Design (from MAPPINGS.md + architecture-profile §7)

### Class Conversion Strategy

**CartEndpoint** (REDESIGN → `com.demo.rest.CartEndpoint`)
- **Package Rename**: `com.redhat.coolstore.rest` → `com.demo.rest` (full prefix replace per migration.yaml)
- **Target Contract**: Stateless JAX-RS resource with proper HTTP status codes
- **Import Migration**: `javax.ws.rs.*` → `jakarta.ws.rs.*` (Jakarta EE9)
- **Annotation Migration**: `@RestController` → `@Path` + Quarkus REST annotations

### Target Implementation Shape

```java
package com.demo.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.demo.service.ShoppingCartService;
import com.demo.model.ShoppingCart;

@Path("/cart")
public class CartEndpoint {
    private final ShoppingCartService shoppingCartService;
    
    @Inject
    public CartEndpoint(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }
    
    @GET
    @Path("/{cartId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCart(@PathParam("cartId") String cartId) {
        if (cartId == null || cartId.trim().isEmpty()) {
            return Response.status(400).entity("{\"error\":\"Invalid cartId\"}").build();
        }
        
        ShoppingCart cart = shoppingCartService.getShoppingCart(cartId);
        if (cart == null || cart.getShoppingCartItemList().isEmpty()) {
            return Response.status(404).entity("{\"error\":\"Cart not found\"}").build();
        }
        
        return Response.ok(cart).build();
    }
    
    @POST
    @Path("/{cartId}/{itemId}/{quantity}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response add(@PathParam("cartId") String cartId,
                       @PathParam("itemId") String itemId,
                       @PathParam("quantity") int quantity) {
        // Input validation + business logic
    }
    
    // Additional endpoints with similar pattern
}
```

### Migration Mapping (per MAPPINGS.md catalog)

**Spring to JAX-RS Migration** (pure redesign - no new findings)
- `@RestController` + `@Scope(session)` → `@Path` (stateless)
- `@Autowired private field` → `@Inject constructor`
- `javax.ws.rs.*` imports → `jakarta.ws.rs.*` imports
- Direct return types → `Response` wrapper with status codes

**Class Tasking**: This is a single redesign class conversion, not multiple files.

### Error Handling Strategy (targetContract)

1. **Input Validation** (validateInput: true)
   - Empty/null cartId → 400 Bad Request
   - Invalid itemId/quantity → 400 Bad Request
   
2. **GET Idempotency** (getIdempotent: true)
   - Missing cartId → 404 Not Found (not null return)
   - Empty cart → 404 Not Found (idempotent semantics)

3. **Error Mapping** (mapErrors: true)
   - Catalog service failures → 503 Service Unavailable
   - Use JAX-RS ExceptionMapper for uniform error responses

### Service Integration

**Dependency Order** (per dependency-order.md:27)
- CartEndpoint (#10) depends on ShoppingCartService (#7), PromoService (#8), ShippingService (#9)
- Services already modernized in S03 (per brief positioning)
- Constructor injection ensures thread-safe service access

### Response Contract

**GET `/cart/{cartId}`** (primary endpoint)
- **200 OK**: Cart exists with items
- **400 Bad Request**: Invalid cartId parameter  
- **404 Not Found**: Cart doesn't exist or is empty

**POST `/cart/{cartId}/{itemId}/{quantity}`**
- **200 OK**: Item added successfully
- **400 Bad Request**: Invalid parameters
- **503 Service Unavailable**: Catalog service failure

### Package Structure

```
src/main/java/com/demo/rest/
└── CartEndpoint.java (renamed from com.redhat.coolstore.rest.CartEndpoint)
```

No new package directories required - existing `com.demo` structure maintained.

### Test Strategy

**Endpoint Testing** (no legacy tests exist for CartEndpoint)
- RestAssured integration tests for all endpoints
- Error condition testing (404, 400, 503 scenarios)
- Service mock tests for endpoint logic validation

### Class Classification

- **CartEndpoint**: REDESIGN (architectural change from Spring to JAX-RS)
- **Service Dependencies**: Already handled in S03 (per brief)
- **Models**: HARVEST from S01/S02 (ShoppingCart, ShoppingCartItem, Product)

### Findings Resolution

**Primary Finding**: springboot-di-to-quarkus-00003 (CartEndpoint @Autowired → constructor injection)
- **Resolution**: Replace field injection with constructor injection
- **Evidence**: Line 28 in legacy CartEndpoint.java
- **Task**: T-001 (Class: infer - architectural decision)

### Task Ordering

1. **Convert CartEndpoint class** (Class: infer)
   - Package rename + JAX-RS migration
   - Error handling implementation
   - Input validation + Response types

2. **Test endpoints** (Class: infer)
   - RestAssured integration tests
   - Error scenario validation

### Acceptance Criteria

- CartEndpoint converted to Quarkus JAX-RS with stateless design
- GET endpoints return 404 on missing carts (idempotent semantics)
- All endpoints return proper HTTP status codes (400, 404, 503)
- Constructor injection replaces Spring field injection
- Tests verify endpoint contracts and error handling
- Package rename: `com.redhat.coolstore.rest` → `com.demo.rest`
