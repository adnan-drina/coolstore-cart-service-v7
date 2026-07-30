# S03 Service Layer Specification

## Behavioral Contract

This specification defines the observed behavior of legacy Spring service classes and their target Quarkus CDI contracts for modernization.

### ShoppingCartServiceImpl

**Legacy Behavior** (`/projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:19-52`):
- Maintains in-memory cart storage using HashMap
- Uses @Autowired field injection for dependencies (ShippingService, CatalogService, PromoService)
- Initializes cart storage in @PostConstruct method
- Provides cart management operations through CartService interface

**Target Contract**:
- ApplicationScoped singleton with constructor injection
- Thread-safe ConcurrentHashMap cart storage with compute() operations
- Product cache with refresh guard (bounded TTL, no clear-on-miss)
- Normalize-before-derive pattern: deduplicate cart items before pricing
- 503 error mapping for catalog service failures via JAX-RS ExceptionMapper

**API Surface**:
- `add(String cartId, String productId, int quantity)` - Adds items with deduplication
- `getShoppingCart(String cartId)` - Retrieves cart by ID
- `remove(String cartId, String productId)` - Removes items from cart
- `checkout(String cartId)` - Finalizes cart with pricing calculation

### PromoService

**Legacy Behavior** (`/projects/legacy/src/main/java/com/redhat/coolstore/service/PromoService.java:55-68`):
- Simple @Component with HashSet promotion storage
- Seed data includes 25% discount on item "329299"
- Implements Serializable interface
- No thread-safety considerations

**Target Contract**:
- ApplicationScoped singleton with immutable promotion set
- Thread-safe ConcurrentHashMap for promo lookup
- Business rule preservation: 25% discount on item "329299"

**API Surface**:
- `Promotion findPromotion(String itemId)` - Finds applicable promotion
- `Set<Promotion> getAllPromotions()` - Returns all active promotions

### ShippingService

**Legacy Behavior** (`/projects/legacy/src/main/java/com/redhat/coolstore/service/ShippingService.java:71-87`):
- Stateless @Component with tiered shipping logic
- $2.99 shipping for carts $0-$25, $4.99 for $25-$50
- No thread-safety issues (stateless)

**Target Contract**:
- ApplicationScoped singleton with stateless shipping tiers
- Business rule preservation: tiered pricing structure

**API Surface**:
- `void calculateShipping(ShoppingCart cart)` - Applies shipping cost based on cart total

### CatalogService

**Legacy Behavior** (`/projects/legacy/src/main/java/com/redhat/coolstore/service/CatalogService.java:90-101`):
- FeignClient interface for catalog service calls
- Environment-driven configuration via ${CATALOG_ENDPOINT}
- GET /api/products endpoint mapping

**Target Contract**:
- ApplicationScoped REST client with @RegisterRestClient
- Environment-driven configuration preservation
- Replaces Feign with Quarkus REST client

**API Surface**:
- `List<Product> products()` - Retrieves product catalog

## Integration Contracts

### Data Model Dependencies
All services depend on com.demo.model.* classes (harvested in S02):
- Product - catalog data model
- ShoppingCart - cart container
- ShoppingCartItem - cart line items
- Promotion - discount rules

### Service Dependency Graph
```
ShoppingCartServiceImpl
├── ShippingService (dependency)
├── CatalogService (dependency) 
└── PromoService (dependency)
```

### Environment Configuration
- `CATALOG_ENDPOINT` - Catalog service URL (preserve from migration.yaml)
- Default fallback: `http://localhost:8081`

## Legacy Evidence References

**File**: `/projects/legacy/src/main/java/com/redhat/coolstore/service/`
- ShoppingCartServiceImpl.java:19-52 (cart storage, dependency injection)
- PromoService.java:55-68 (promotion storage, seed data)
- ShippingService.java:71-87 (shipping calculation logic)
- CatalogService.java:90-101 (Feign client configuration)

**Key Legacy Patterns**:
- Spring @Service/@Component annotations → Quarkus @ApplicationScoped
- @Autowired field injection → Constructor injection with @Inject
- HashMap cart storage → ConcurrentHashMap with compute()
- FeignClient → @RegisterRestClient
- @PostConstruct initialization → Constructor-based initialization
