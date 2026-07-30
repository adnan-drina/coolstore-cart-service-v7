# S04: REST endpoint modernization

<!-- The brief is the self-contained work order for one modernization
     story. Bar: a competent developer or a fresh session starts the
     story from THIS FILE ALONE. Fill every section; delete none. -->

## Goal & position

Modernize the REST endpoint to Quarkus JAX-RS with stateless design, proper error handling, and GET idempotency (404 on missing cartId). This story converts the REDESIGN class CartEndpoint to its target contract from architecture-profile §7: stateless operations, input validation, error mapping. The REST surface depends on services per dependency-order §27, so this story builds on the modernized service layer from S03. This is the first deploy milestone as it establishes the live API surface.

## In scope

The exact legacy classes/files this story modernizes. For each, quote
the load-bearing legacy code (the lines being transformed — imports,
annotations, key methods), so the story never starts from a blank
read:

- `src/main/java/com/redhat/coolstore/rest/CartEndpoint.java` — REDESIGN class
  ```java
  package com.redhat.coolstore.rest;
  
  import javax.ws.rs.DELETE;
  import javax.ws.rs.GET;
  import javax.ws.rs.POST;
  import javax.ws.rs.Path;
  import javax.ws.rs.PathParam;
  import javax.ws.rs.Produces;
  import javax.ws.rs.core.MediaType;
  
  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.context.annotation.Scope;
  import org.springframework.web.bind.annotation.RestController;
  import org.springframework.web.context.WebApplicationContext;
  
  import com.redhat.coolstore.model.ShoppingCart;
  import com.redhat.coolstore.service.ShoppingCartService;
  
  @RestController
  @Scope(scopeName = WebApplicationContext.SCOPE_SESSION)
  @Path("/cart")
  public class CartEndpoint implements Serializable {
      private static final long serialVersionUID = -7227732980791688773L;
      
      @Autowired
      private ShoppingCartService shoppingCartService;
      
      @GET
      @Path("/{cartId}")
      @Produces(MediaType.APPLICATION_JSON)
      public ShoppingCart getCart(@PathParam("cartId") String cartId) {
          return shoppingCartService.getShoppingCart(cartId);
      }
      
      @POST
      @Path("/{cartId}/{itemId}/{quantity}")
      @Produces(MediaType.APPLICATION_JSON)
      public ShoppingCart add(@PathParam("cartId") String cartId,
                              @PathParam("itemId") String itemId,
                              @PathParam("quantity") int quantity) throws Exception {
          return shoppingCartService.addItem(cartId, itemId, quantity);
      }
  ```

## Out of scope

Service implementations remain in their CDI structure from S03. Bootstrap classes (CartServiceApplication, JerseyConfig) remain until S05. REST endpoint modernization focuses only on the endpoint class itself, not the underlying service infrastructure. The tree must stay buildable with modernized REST endpoint and service layer.

## Class roles & target contract (from architecture-profile §7)

For each in-scope class, its role and — for REDESIGN classes — the target
contract carried forward from profile §7, so M3 writes tasks and tests to
the target (not the legacy):

- `CartEndpoint` — REDESIGN
  - target: Stateless cart operations with **404** on missing cartId (idempotent GET semantics)
  - Input validation: **400** for invalid itemId/quantity parameters (numeric validation required)
  - Error handling: **503** via JAX-RS **ExceptionMapper** for catalog service failures (never raw 500)
  - Concurrency: Thread-safe singleton using **ConcurrentHashMap** for cart storage (implemented in service layer)
  - Remove: `@RestController` + `@Scope(session)` replaced with JAX-RS `@Path` + Quarkus REST

## Decided target shapes

The MAPPINGS.md rows that apply (quote the decided target, don't
re-decide). Recipe-executed rules already handled: reference
`migration/recipe-log.md` and `migration/staging/` where applicable.

**Story ordering:** extensions and BOM first, then models, then resources,
then config keys, then tests (`extensions → models → resources → config →
tests`).

Spring @RestController to Quarkus JAX-RS conversion (pure redesign - no new findings): Replace Spring annotations with JAX-RS:
```java
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
}
```

## Contracts owned by this story

- **Findings**: the mandatory rule ids this story resolves (from the
  roadmap entry).
  - springboot-di-to-quarkus-00003 (CartEndpoint @Autowired → constructor injection)

- **Preserve**: CATALOG_ENDPOINT environment-driven configuration (inherited from service layer via migration.yaml)

- **Behavioral pins**: REDESIGN class pins TARGET behavior from architecture-profile §7:
  - **GET idempotency:** Returns 404 on missing cartId (idempotent GET semantics)
  - **Input validation:** Returns 400 for invalid itemId/quantity parameters
  - **Error mapping:** Returns 503 for catalog service failures (never raw 500)
  - **Cart `add()` oracle (additive):** two `add(cartId, itemId, 2)` calls → quantity **4** after dedupe (not 2)
  - **Cart initialization:** Empty carts return zero totals for all monetary fields

- **Forbidden**: `getMockProducts`, "Fallback to mock" (inherited from migration.yaml)

## Done-criteria

Checkable, story-scoped:
- builds + `sensors.sh task` green at every commit; milestone green at
  story end
- REST endpoint deployed and serving at `/api/cart/*` endpoints
- GET `/cart/{cartId}` returns 404 for non-existent carts
- POST `/cart/{cartId}/{itemId}/{quantity}` validates input and returns 400 for invalid parameters
- JAX-RS/RestAssured tests verify all endpoint contracts
- Cart `add()` behavior preserved: additive quantity with deduplication
- Service-level oracles maintained (25% discount on 329299, free shipping >= $75)
- Deploy milestone: factory pipeline green, deployed, acceptance path serving at `/api/cart/{cartId}`
