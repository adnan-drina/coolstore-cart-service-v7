# S03: Service layer modernization

<!-- The brief is the self-contained work order for one modernization
     story. Bar: a competent developer or a fresh session starts the
     story from THIS FILE ALONE. Fill every section; delete none. -->

## Goal & position

Modernize all service classes to Quarkus CDI (@ApplicationScoped) with constructor injection, replacing Spring DI annotations. This story converts REDESIGN classes to their target contracts from architecture-profile §7: thread-safe singleton state, cache refresh-guard, read-only GET, validation + error mapping, normalize-before-derive. Services depend on models per dependency-order §24-28, so this story builds on the harvested domain models from S02.

## In scope

The exact legacy classes/files this story modernizes. For each, quote
the load-bearing legacy code (the lines being transformed — imports,
annotations, key methods), so the story never starts from a blank
read:

- `src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java` — REDESIGN class
  ```java
  package com.redhat.coolstore.service;
  
  import org.slf4j.Logger;
  import org.slf4j.LoggerFactory;
  import org.springframework.beans.factory.annotation.Autowired;
  import org.springframework.stereotype.Service;
  
  import javax.annotation.PostConstruct;
  import java.util.HashMap;
  import java.util.Map;
  
  @Service
  public class ShoppingCartServiceImpl implements CartService {
      private static final Logger LOG = LoggerFactory.getLogger(ShoppingCartServiceImpl.class);
      
      @Autowired
      ShippingService ss;
      
      @Autowired
      CatalogService catalogServie;
      
      @Autowired
      PromoService ps;
      
      Map<String, ShoppingCart> carts;
      Map<String, Product> productMap = new HashMap<>();
      
      @PostConstruct
      public void init() {
          LOG.info("Using local in-memory cache for cart data");
          carts = new HashMap<>();
      }
  ```

- `src/main/java/com/redhat/coolstore/service/PromoService.java` — REDESIGN class
  ```java
  package com.redhat.coolstore.service;
  
  import org.springframework.stereotype.Component;
  
  @Component
  public class PromoService implements Serializable {
      private Set<Promotion> promotionSet = null;
      
      public PromoService() {
          promotionSet = new HashSet<Promotion>();
          // Coolstore seed item also used by inventory/catalog demos
          promotionSet.add(new Promotion("329299", .25));
      }
  ```

- `src/main/java/com/redhat/coolstore/service/ShippingService.java` — REDESIGN class
  ```java
  package com.redhat.coolstore.service;
  
  import org.springframework.stereotype.Component;
  
  @Component
  public class ShippingService {
      public void calculateShipping(ShoppingCart sc) {
          if (sc != null) {
              if (sc.getCartItemTotal() >= 0 && sc.getCartItemTotal() < 25) {
                  sc.setShippingTotal(2.99);
              } else if (sc.getCartItemTotal() >= 25 && sc.getCartItemTotal() < 50) {
                  sc.setShippingTotal(4.99);
              }
          }
      }
  ```

- `src/main/java/com/redhat/coolstore/service/CatalogService.java` — REDESIGN class
  ```java
  package com.redhat.coolstore.service;
  
  import org.springframework.cloud.openfeign.FeignClient;
  import org.springframework.web.bind.annotation.GetMapping;
  
  @FeignClient(name = "catalogService", url = "${CATALOG_ENDPOINT}")
  interface CatalogService {
      @GetMapping("/api/products")
      List<Product> products();
  ```

## Out of scope

REST endpoints remain in legacy Spring structure until S04. Service classes are modernized to CDI with their target contracts, but JAX-RS conversion waits for the REST story. The tree must stay buildable with modernized services and legacy REST endpoints.

## Class roles & target contract (from architecture-profile §7)

For each in-scope class, its role and — for REDESIGN classes — the target
contract carried forward from profile §7, so M3 writes tasks and tests to
the target (not the legacy):

- `ShoppingCartServiceImpl` — REDESIGN
  - target: **ApplicationScoped** singleton with constructor injection
  - Concurrency: **ConcurrentHashMap** with **compute()** for thread-safe cart access
  - Cache policy: Product cache with **refresh guard** (no clear-on-miss; bounded TTL approach)
  - Normalization: **normalize-before-derive** — deduplicate cart items before pricing calculations
  - Error handling: **503** via JAX-RS **ExceptionMapper** for catalog service failures (never raw 500)

- `PromoService` — REDESIGN
  - target: **ApplicationScoped** singleton with immutable promotion set
  - Concurrency: Thread-safe promotion lookup with ConcurrentHashMap for promo mapping
  - Business rules: Preserve 25% discount on item "329299" and free shipping over $75 threshold

- `ShippingService` — REDESIGN
  - target: **ApplicationScoped** singleton with stateless shipping tiers
  - Business rules: Preserve tiered shipping ($2.99-$10.99 based on cart totals 0-10000 range)

- `CatalogService` — REDESIGN
  - target: **ApplicationScoped** REST client with environment-driven configuration
  - Integration: Replace Feign with Quarkus `@RegisterRestClient` for catalog endpoint
  - Configuration: `${CATALOG_ENDPOINT:default}` for environment-driven URLs

## Decided target shapes

The MAPPINGS.md rows that apply (quote the decided target, don't
re-decide). Recipe-executed rules already handled: reference
`migration/recipe-log.md` and `migration/staging/` where applicable.

**Story ordering:** extensions and BOM first, then models, then resources,
then config keys, then tests (`extensions → models → resources → config →
tests`).

Spring DI to Quarkus CDI conversion (springboot-di-to-quarkus-00003): Replace @Service/@Component with @ApplicationScoped and field injection with constructor injection:
```java
@ApplicationScoped
public class ShoppingCartServiceImpl implements CartService {
    private final ShippingService ss;
    private final CatalogService catalogService;
    private final PromoService ps;
    
    @Inject
    public ShoppingCartServiceImpl(ShippingService ss, CatalogService catalogService, PromoService ps) {
        this.ss = ss;
        this.catalogService = catalogService;
        this.ps = ps;
    }
```

Feign to Quarkus REST client conversion (springboot-web-to-quarkus-00000): Replace FeignClient with Quarkus REST client:
```java
@RegisterRestClient(configKey = "catalog-service")
interface CatalogService {
    @GET
    @Path("/api/products")
    List<Product> products();
}
```

Environment-driven configuration (demo-env-integration-00001): Preserve CATALOG_ENDPOINT configuration:
```properties
quarkus.rest-client.catalog-service.url=${CATALOG_ENDPOINT:http://localhost:8081}
```

## Contracts owned by this story

- **Findings**: the mandatory rule ids this story resolves (from the
  roadmap entry).
  - springboot-di-to-quarkus-00003, springboot-web-to-quarkus-00000, localhost-http-00001, demo-env-integration-00001, springboot-di-to-quarkus-00000, springboot-metrics-to-quarkus-0200

- **Preserve**: CATALOG_ENDPOINT environment-driven configuration (preserve: from migration.yaml)

- **Behavioral pins**: REDESIGN classes pin TARGET behavior from architecture-profile §7:
  - **Cart `add()` oracle (additive):** two `add(cartId, itemId, 2)` calls → quantity **4** after dedupe (not 2)
  - **Pricing workflow:** PromoService → ShippingService → final totals calculation preserved
  - **Product 329299 promotion:** 25% discount maintained (PromoService.java:27)
  - **Free shipping threshold:** carts >= $75 get free shipping ( PromoService.java:51-53)

- **Forbidden**: `getMockProducts`, "Fallback to mock" (forbidden: from migration.yaml)

## Done-criteria

Checkable, story-scoped:
- builds + `sensors.sh task` green at every commit; milestone green at
  story end
- All services converted to @ApplicationScoped with constructor injection
- Feign client replaced with Quarkus REST client with CATALOG_ENDPOINT configuration
- Thread-safe ConcurrentHashMap implementation for cart storage
- Product cache with refresh guard (no clear-on-miss policy)
- Characterization tests verify business rule preservation (25% discount on 329299, free shipping >= $75)
- 6 findings resolved (no longer fire on re-analysis)
- All service-level tests pass with new CDI injection model
