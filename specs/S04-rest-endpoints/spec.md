# S04 REST Endpoint Modernization - Specification

## Observed Legacy Behavior & API Contract

### Legacy Implementation Analysis

The `CartEndpoint` class (`/projects/legacy/src/main/java/com/redhat/coolstore/rest/CartEndpoint.java`) provides five REST endpoints under the `/cart` path, serving as the primary interface for shopping cart operations.

### Legacy Code Evidence (S04 Brief)

**Package and Imports:**
```java
package com.redhat.coolstore.rest;
import javax.ws.rs.*;  // javax namespace (needs jakarta migration)
import org.springframework.beans.factory.annotation.Autowired;  // Spring DI
import org.springframework.context.annotation.Scope;  // Spring scope
import org.springframework.web.bind.annotation.RestController;  // Spring web annotation
```

**Class Declaration:**
```java
@RestController
@Scope(scopeName = WebApplicationContext.SCOPE_SESSION)  // Session-scoped
@Path("/cart")
public class CartEndpoint implements Serializable {
    private static final long serialVersionUID = -7227732980791688773L;
    
    @Autowired  // Spring field injection
    private ShoppingCartService shoppingCartService;
```

### Endpoint Contract (from legacy implementation)

1. **GET `/cart/{cartId}`** - Retrieve cart state
   - Returns: `ShoppingCart` object (raw, no error handling)
   - Behavior: Direct pass-through to service layer
   - Legacy issue: Returns `null` for non-existent carts (no 404)

2. **POST `/cart/{cartId}/{itemId}/{quantity}`** - Add items to cart
   - Returns: `ShoppingCart` object
   - Behavior: Direct pass-through to `shoppingCartService.addItem()`
   - Legacy issue: No input validation, no error mapping

3. **POST `/cart/{cartId}/{tmpId}`** - Transfer cart items
   - Returns: `ShoppingCart` object  
   - Behavior: Direct pass-through to `shoppingCartService.set()`

4. **DELETE `/cart/{cartId}/{itemId}/{quantity}`** - Remove items
   - Returns: `ShoppingCart` object
   - Behavior: Direct pass-through to `shoppingCartService.deleteItem()`

5. **POST `/cart/checkout/{cartId}`** - Checkout cart
   - Returns: `ShoppingCart` object
   - Behavior: Direct pass-through to `shoppingCartService.checkout()`

### Service Dependencies

**Field Injection Pattern:**
```java
@Autowired
private ShoppingCartService shoppingCartService;
```

The endpoint depends on `ShoppingCartService` (interface) with implementation `ShoppingCartServiceImpl`. According to dependency-order.md, CartEndpoint is #10 in conversion order, depending on services #7-9.

### Legacy Issues Identified

1. **Session Scope**: `@Scope(WebApplicationContext.SCOPE_SESSION)` creates stateful behavior
2. **Field Injection**: Spring `@Autowired` on fields (not testable, thread-unsafe)
3. **No Error Handling**: Raw exceptions propagate, no status code mapping
4. **No Input Validation**: No validation of path parameters
5. **Spring Dependencies**: Requires Spring DI annotations, not Jakarta EE compatible

### Target Contract (from architecture-profile §7)

**Stateless Design**: Remove session scope, implement truly stateless REST operations
- **GET Idempotency**: Returns 404 on missing cartId (idempotent GET semantics)
- **Input Validation**: Returns 400 for invalid itemId/quantity parameters  
- **Error Mapping**: Returns 503 via JAX-RS ExceptionMapper for catalog service failures
- **Response Type**: Use JAX-RS `Response` wrapper for proper status codes

### Testing Evidence

Legacy tests are service-focused (`ShoppingCartServiceTest`), not endpoint-focused. This story must establish REST endpoint test coverage with RestAssured/JAX-RS testing.

**Test Strategy**: Characterize endpoint behavior through:
- RestAssured endpoint tests (verify status codes, error handling)
- Service mock tests (test endpoint logic independent of service implementation)
- Integration tests (end-to-end with real service layer)

### File References

- **Legacy Endpoint**: `/projects/legacy/src/main/java/com/redhat/coolstore/rest/CartEndpoint.java`
- **Service Interface**: `/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartService.java`
- **Service Implementation**: `/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java`
- **Test Evidence**: `/projects/legacy/src/test/java/com/redhat/coolstore/service/ShoppingCartServiceTest.java` (business rules)
