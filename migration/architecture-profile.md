# Architecture Profile — Coolstore Cart Service

## 1. Purpose & domain

The Coolstore Cart Service is a shopping cart management microservice that provides REST endpoints for cart lifecycle operations (add, remove, checkout, retrieve) and pricing calculations. It serves e-commerce functionality by maintaining shopping cart state, applying promotional discounts, calculating shipping costs, and integrating with a catalog service for product information (ShoppingCartServiceTest.java:42-44, 50-53).

The core domain encompasses three primary concepts: **Shopping Carts** (stateful containers identified by cartId), **Products** (catalog items with pricing and descriptions), and **Shopping Cart Items** (quantity-based cart entries that associate products with pricing and promotional savings). The service implements business rules for promotional pricing (25% off item "329299" per PromoService.java:27), tiered shipping calculations ($2.99-$10.99 based on cart total per ShippingService.java:11-22), and cart item deduplication to normalize quantities for the same product (ShoppingCartServiceImpl.java:200-221).

## 2. Components & relationships

The application forms a layered architecture with REST endpoints depending on business services, which in turn depend on domain models and external integrations. The **CartEndpoint** (`/cart` REST resource) acts as the entry point, delegating to **ShoppingCartServiceImpl** for cart operations (/projects/legacy/src/main/java/com/redhat/coolstore/rest/CartEndpoint.java:29, /projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:28). ShoppingCartService coordinates three supporting services: **PromoService** applies item-level and shipping promotions, **ShippingService** calculates shipping costs based on cart totals, and **CatalogService** retrieves product data via Feign client (/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:33-40, dependency-order.md:11-15).

The **ShoppingCart** domain model serves as the central data structure, containing cart metadata (totals, savings) and a list of **ShoppingCartItem** entries that reference **Product** objects. This creates a god-node dependency pattern where ShoppingCart has the highest fan-in (5 incoming dependencies per dependency-order.md:10), followed by Product (4 dependencies) and ShoppingCartItem (3 dependencies). The dependency graph shows tight coupling between service components and domain models, with conversion order requiring model classes first, then services, then REST endpoints (dependency-order.md:18-29).

```
┌─────────────┐    ┌──────────────────┐    ┌─────────────┐
│ CartEndpoint│───▶│ShoppingCartService│───▶│ CatalogService│
│   (REST)    │    │     (Impl)       │    │ (Feign)     │
└─────────────┘    └──────────────────┘    └─────────────┘
                          │
                          ▼
                   ┌─────────────────┐
                   │   PromoService  │
                   │ ShippingService │
                   └─────────────────┘
                          │
                          ▼
                   ┌─────────────────┐    ┌─────────────────┐
                   │ ShoppingCart    │◀───│ShoppingCartItem │
                   │ (god-node)      │    │ (god-node)      │
                   └─────────────────┘    └─────────────────┘
                          │
                          ▼
                   ┌─────────────────┐
                   │    Product      │
                   │   (god-node)    │
                   └─────────────────┘
```

## 3. Integration surfaces

**Exposed API**: The service provides five JAX-RS endpoints under `/cart` path (CartEndpoint.java:23-70):
- `GET /cart/{cartId}` — retrieves cart state with calculated totals
- `POST /cart/{cartId}/{itemId}/{quantity}` — adds items to cart
- `DELETE /cart/{cartId}/{itemId}/{quantity}` — removes items from cart  
- `POST /cart/{cartId}/{tmpId}` — transfers cart items between carts
- `POST /cart/checkout/{cartId}` — empties cart after purchase

**Consumed Services**: The application integrates with an external catalog service via Feign client (CatalogService.java:10), configured through `CATALOG_ENDPOINT` environment variable (application.properties:6). This endpoint provides product catalog data used for cart pricing and validation.

**Persistence**: The service uses in-memory storage via HashMap for cart state (ShoppingCartServiceImpl.java:42), with no external database dependencies. Product data is cached locally after first retrieval.

**Configuration**: External configuration is environment-driven through `CATALOG_ENDPOINT` property (demo-env-integration-00001 finding), supporting deployment flexibility. The port and context path are configured via Spring properties (`spring.jersey.application-path=/api` per application.properties:2).

**Health/Management**: Spring Boot Actuator provides health endpoints (springboot-actuator-to-quarkus-0100 finding), exposing application status for monitoring and management.

## 4. Behavioral contract sources

The application's expected behavior is defined by the legacy test suite, particularly **ShoppingCartServiceTest** which establishes numeric oracles and business rule validation. Key behavioral contracts include:

**Cart Initialization**: Empty carts return zero totals for all monetary fields (ShoppingCartServiceTest.java:32-35), ensuring consistent initial state.

**Pricing Calculations**: Cart pricing applies item-level promotions first, then shipping calculations, producing specific totals that must be preserved (ShoppingCartServiceTest.java:42-53). The test validates that 2 items at $1000 each produce $2000 cart total with -$10.99 shipping promo (free shipping over $75 threshold), resulting in $2000 final total.

**Product Retrieval**: Product lookup caches catalog data and returns exact product matching itemId, validated against test fixture data (ShoppingCartServiceTest.java:57-63). This establishes the contract for catalog service integration and caching behavior.

**Promotional Rules**: The application applies 25% discount to product "329299" (PromoService.java:27) and free shipping for carts exceeding $75 total (PromoService.java:51-53). These business rules constitute the core pricing logic that migration must preserve.

**Contract Gap**: Cart deduplication behavior (ShoppingCartServiceImpl.java:200-221) is not explicitly tested, creating a potential contract gap that characterization tests should address.

## 5. Modernization surface

**Component: REST Layer**
- **MUST CHANGE**: JAX-RS imports migration from `javax.*` to `jakarta.*` (javax-to-jakarta-import-00001 finding, CartEndpoint.java:5-11)
- **MUST CHANGE**: Replace Spring `@RestController` with JAX-RS `@Path` and Quarkus REST annotations (springboot-web-to-quarkus-00000 finding, CartEndpoint.java:21)
- **MUST CHANGE**: Remove Spring `@Scope` session management in favor of stateless design or proper state handling (CartEndpoint.java:22)

**Component: Service Layer**  
- **MUST CHANGE**: Convert Spring `@Service` and `@Component` stereotypes to Quarkus CDI `@ApplicationScoped` (springboot-di-to-quarkus-00003 finding, ShoppingCartServiceImpl.java:28, PromoService.java:15, ShippingService.java:7)
- **MUST CHANGE**: Replace field injection with constructor injection for thread-safety and testability (springboot-di-to-quarkus-00003 finding, ShoppingCartServiceImpl.java:33-40)
- **MUST CHANGE**: Update `javax.annotation.PostConstruct` to Jakarta equivalent or use Quarkus lifecycle annotations (removed-javaee-modules-00020 finding, ShoppingCartServiceImpl.java:46)

**Component: Configuration**
- **MUST CHANGE**: Migrate Spring Boot configuration properties to Quarkus application.properties format (springboot-properties-to-quarkus-00000 finding)
- **SHOULD CHANGE**: Replace Spring Actuator with Quarkus SmallRye Health for health endpoints (springboot-actuator-to-quarkus-0100 finding)

**Component: Build & Dependencies**
- **MUST CHANGE**: Replace Spring Boot parent POM with Quarkus BOM (springboot-parent-pom-to-quarkus-00000 finding, pom.xml:17)
- **MUST CHANGE**: Replace Spring dependencies with Quarkus equivalents (javaee-pom-to-quarkus-* findings covering JAX-RS, metrics, plugin configuration)
- **MUST CHANGE**: Update Feign client to Quarkus REST client approach (springboot-web-to-quarkus-00000 finding, CatalogService.java:10)

## 6. Domain boundaries

The application represents a **single bounded context** for e-commerce cart management, with no natural domain boundaries requiring decomposition. All components are tightly coupled through the ShoppingCart domain model, which serves as the central integration point for pricing, promotion, and shipping calculations (/projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCart.java:21-23, dependency-order.md:10 identifies ShoppingCart as the highest fan-in god node).

While the application contains distinct service responsibilities (promotions, shipping, catalog integration), these cannot be separated without breaking the cart pricing workflow that depends on coordinated business rule execution (/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:66-85 applies promotions, then shipping, then calculates final totals).

## 7. Class roles & target contract

### REDESIGN Classes

**CartEndpoint** — JAX-RS REST resource modernized to Quarkus REST
- Target contract: Stateless cart operations with **404** on missing cartId (idempotent GET semantics)
- Input validation: **400** for invalid itemId/quantity parameters (numeric validation required)
- Error handling: **503** via JAX-RS **ExceptionMapper** for catalog service failures (never raw 500)
- Concurrency: Thread-safe singleton using **ConcurrentHashMap** for cart storage
- Evidence: /projects/legacy/src/main/java/com/redhat/coolstore/rest/CartEndpoint.java:21-23 (JAX-RS annotations), /projects/legacy/src/main/java/com/redhat/coolstore/rest/CartEndpoint.java:28 (Spring @Autowired), /projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:42 (current HashMap storage), /projects/legacy/src/main/java/com/redhat/coolstore/rest/CartEndpoint.java:34-36 (GET endpoint)

**ShoppingCartServiceImpl** — Core business logic service modernized to CDI
- Target contract: **ApplicationScoped** singleton with constructor injection
- Concurrency: **ConcurrentHashMap** with **compute()** for thread-safe cart access
- Cache policy: Product cache with **refresh guard** (no clear-on-miss; bounded TTL approach)
- Normalization: **normalize-before-derive** — deduplicate cart items before pricing calculations
- Evidence: /projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:28 (@Service), /projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:33-40 (Spring field injection), /projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:42 (HashMap storage), /projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:200-221 (deduplication logic), /projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:66-85 (pricing workflow)

**PromoService** — Promotion calculation service modernized to CDI  
- Target contract: **ApplicationScoped** singleton with immutable promotion set
- Concurrency: Thread-safe promotion lookup with ConcurrentHashMap for promo mapping
- Business rules: Preserve 25% discount on item "329299" and free shipping over $75 threshold
- Evidence: /projects/legacy/src/main/java/com/redhat/coolstore/service/PromoService.java:15 (@Component), /projects/legacy/src/main/java/com/redhat/coolstore/service/PromoService.java:27 (seed promotion 329299), /projects/legacy/src/main/java/com/redhat/coolstore/service/PromoService.java:30-46 (cart item promotions), /projects/legacy/src/main/java/com/redhat/coolstore/service/PromoService.java:48-55 (shipping promotions), /projects/legacy/src/main/java/com/redhat/coolstore/service/PromoService.java:19-21 (promotion set)

**ShippingService** — Shipping calculation service modernized to CDI
- Target contract: **ApplicationScoped** singleton with stateless shipping tiers
- Business rules: Preserve tiered shipping ($2.99-$10.99 based on cart totals 0-10000 range)
- Evidence: /projects/legacy/src/main/java/com/redhat/coolstore/service/ShippingService.java:7 (@Component), /projects/legacy/src/main/java/com/redhat/coolstore/service/ShippingService.java:10-23 (shipping calculation tiers)

**CatalogService** — External integration service modernized to Quarkus REST client
- Target contract: **ApplicationScoped** REST client with environment-driven configuration
- Integration: Replace Feign with Quarkus `@RegisterRestClient` for catalog endpoint
- Configuration: `${CATALOG_ENDPOINT:default}` for environment-driven URLs
- Evidence: /projects/legacy/src/main/java/com/redhat/coolstore/service/CatalogService.java:10 (FeignClient), /projects/legacy/src/main/java/com/redhat/coolstore/service/CatalogService.java:5 (Spring @FeignClient), /projects/legacy/src/main/resources/application.properties:6 (CATALOG_ENDPOINT configuration)

**JerseyConfig** — Removed (Quarkus auto-discovery subsumes it)
- Target contract: **removed — Quarkus auto-discovers JAX-RS resources**
- Evidence: /projects/legacy/src/main/java/com/redhat/coolstore/rest/JerseyConfig.java:6 (@Component), /projects/legacy/src/main/java/com/redhat/coolstore/rest/JerseyConfig.java:9 (Jersey ResourceConfig registration)

**CartServiceApplication** — Bootstrap class removed
- Target contract: **removed — Quarkus default main() with CDI bootstrap**
- Evidence: /projects/legacy/src/main/java/com/redhat/coolstore/CartServiceApplication.java:7 (@SpringBootApplication), /projects/legacy/src/main/java/com/redhat/coolstore/CartServiceApplication.java:11-12 (SpringApplication.run)

### HARVEST Classes

**ShoppingCart** — Domain model carried over faithfully
- Preserve existing structure: cartId, shoppingCartItemList, totals, and savings fields
- Business logic: Maintain pricing workflow integration points
- Evidence: /projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCart.java:21-23 (core fields), /projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCart.java:48-74 (cart management methods), /projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCart.java:11-19 (totals fields), /projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCart.java:75-126 (getters/setters)

**ShoppingCartItem** — Domain model carried over faithfully  
- Preserve existing structure: price, quantity, promoSavings, product references
- Integration: Maintain ShoppingCart pricing workflow compatibility
- Evidence: /projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java:9-12 (core fields), /projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java:18-47 (accessors), /projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java:56 (@ToString)

**Product** — Domain model carried over faithfully
- Preserve existing structure: itemId, name, desc, price with full constructor and accessors
- Integration: Maintain catalog service compatibility and pricing calculations
- Evidence: /projects/legacy/src/main/java/com/redhat/coolstore/model/Product.java:5-11 (core fields), /projects/legacy/src/main/java/com/redhat/coolstore/model/Product.java:13-23 (constructors), /projects/legacy/src/main/java/com/redhat/coolstore/model/Product.java:24-47 (accessors), /projects/legacy/src/main/java/com/redhat/coolstore/model/Product.java:50-53 (@ToString)

**Promotion** — Domain model carried over faithfully
- Preserve existing structure: itemId, percentOff with constructor and accessors
- Business logic: Maintain promotion matching and calculation compatibility
- Evidence: /projects/legacy/src/main/java/com/redhat/coolstore/model/Promotion.java:5-7 (core fields), /projects/legacy/src/main/java/com/redhat/coolstore/model/Promotion.java:13-16 (constructor), /projects/legacy/src/main/java/com/redhat/coolstore/model/Promotion.java:19-32 (accessors), /projects/legacy/src/main/java/com/redhat/coolstore/model/Promotion.java:36-39 (@ToString)