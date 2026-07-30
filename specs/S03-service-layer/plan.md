# S03 Service Layer Migration Plan

## Migration Strategy

This story implements **REDESIGN** approach for all four service classes, transforming Spring DI patterns to Quarkus CDI with constructor injection. All classes require significant architectural changes beyond mechanical package updates.

## Class Conversion Order

Following dependency order in migration/dependency-order.md (services depend on models from S02):

1. **PromoService** - Independent service with simple promotion storage
2. **ShippingService** - Stateless service with shipping calculation
3. **CatalogService** - REST client interface requiring configuration updates
4. **ShoppingCartServiceImpl** - Central orchestrator with complex dependencies

This order ensures services can compile as dependencies are added incrementally.

## Target Architecture

### CDI Conversion Specification

**Spring DI → Quarkus CDI** (springboot-di-to-quarkus-00003):
```java
// Legacy: Field injection with @Autowired
@Service
public class ShoppingCartServiceImpl implements CartService {
    @Autowired
    ShippingService ss;
}

// Target: Constructor injection with @Inject
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
}
```

### REST Client Conversion

**Feign → Quarkus REST Client** (springboot-web-to-quarkus-00000):
```java
// Legacy: Spring Cloud Feign
@FeignClient(name = "catalogService", url = "${CATALOG_ENDPOINT}")
interface CatalogService {
    @GetMapping("/api/products")
    List<Product> products();
}

// Target: Quarkus REST Client
@RegisterRestClient(configKey = "catalog-service")
interface CatalogService {
    @GET
    @Path("/api/products")
    List<Product> products();
}
```

### Thread-Safe Storage Conversion

**HashMap → ConcurrentHashMap** (springboot-di-to-quarkus-00000):
```java
// Legacy: Non-thread-safe storage
Map<String, ShoppingCart> carts = new HashMap<>();

// Target: Thread-safe storage with compute()
private final Map<String, ShoppingCart> carts = new ConcurrentHashMap<>();

public void addToCart(String cartId, String productId, int quantity) {
    carts.compute(cartId, (id, cart) -> {
        if (cart == null) {
            cart = new ShoppingCart(id);
        }
        // Cart operations with thread safety
        return cart;
    });
}
```

## Migration Tasks by Finding Rule

### Spring DI to Quarkus CDI (springboot-di-to-quarkus-00003)
**Class: infer** - Requires architectural decisions

- ShoppingCartServiceImpl: @Service → @ApplicationScoped, field → constructor injection
- PromoService: @Component → @ApplicationScoped, field → constructor injection  
- ShippingService: @Component → @ApplicationScoped, field → constructor injection

### Feign to REST Client (springboot-web-to-quarkus-00000)
**Class: infer** - Requires interface redesign

- CatalogService: @FeignClient → @RegisterRestClient, Spring annotations → JAX-RS

### Environment Integration (demo-env-integration-00001)
**Class: rewrite** - Mechanical configuration preservation

- Preserve CATALOG_ENDPOINT environment variable
- Add quarkus.rest-client.catalog-service.url configuration

### Thread Safety (springboot-di-to-quarkus-00000)
**Class: infer** - Requires concurrency design decisions

- ShoppingCartServiceImpl: HashMap → ConcurrentHashMap with compute()
- Product cache: Implement refresh guard pattern

### HTTP Configuration (localhost-http-00001)
**Class: rewrite** - Mechanical property updates

- Add proper REST client configuration properties

### Metrics Integration (springboot-metrics-to-quarkus-0200)
**Class: rewrite** - Mechanical dependency updates

- Ensure proper metrics integration in service layer

## Architecture Profile Compliance

All classes follow REDESIGN classification from architecture-profile.md §7:

### ShoppingCartServiceImpl - REDESIGN
- **Target**: ApplicationScoped singleton with constructor injection
- **Concurrency**: ConcurrentHashMap with compute() for thread-safe cart access
- **Cache Policy**: Product cache with refresh guard (bounded TTL, no clear-on-miss)
- **Normalization**: normalize-before-derive pattern
- **Error Handling**: 503 via JAX-RS ExceptionMapper
- **Evidence**: /projects/legacy/src/main/java/com/redhat/coolstore/service/ShoppingCartServiceImpl.java:19-52

### PromoService - REDESIGN
- **Target**: ApplicationScoped singleton with immutable promotion set
- **Concurrency**: Thread-safe promotion lookup with ConcurrentHashMap
- **Business Rules**: Preserve 25% discount on item "329299"
- **Evidence**: /projects/legacy/src/main/java/com/redhat/coolstore/service/PromoService.java:55-68

### ShippingService - REDESIGN  
- **Target**: ApplicationScoped singleton with stateless shipping tiers
- **Business Rules**: Preserve tiered shipping ($2.99-$4.99 based on cart totals)
- **Evidence**: /projects/legacy/src/main/java/com/redhat/coolstore/service/ShippingService.java:71-87

### CatalogService - REDESIGN
- **Target**: ApplicationScoped REST client with environment-driven configuration
- **Integration**: Replace Feign with Quarkus @RegisterRestClient
- **Configuration**: ${CATALOG_ENDPOINT:default} for environment-driven URLs
- **Evidence**: /projects/legacy/src/main/java/com/redhat/coolstore/service/CatalogService.java:90-101

## Story Dependencies

**Build Order**: Services depend on models from S02 (com.demo.model.*), so services must be converted after model harvest is complete.

**Characterization Strategy**: Service-level tests will be added to verify business rule preservation (25% discount on 329299, free shipping >= $75) once all services are converted.

## Findings Resolution

This story resolves six mandatory findings:

1. **springboot-di-to-quarkus-00003**: Spring @Service/@Component → Quarkus @ApplicationScoped
2. **springboot-web-to-quarkus-00000**: FeignClient → @RegisterRestClient  
3. **localhost-http-00001**: HTTP client configuration
4. **demo-env-integration-00001**: CATALOG_ENDPOINT environment preservation
5. **springboot-di-to-quarkus-00000**: Field injection → constructor injection
6. **springboot-metrics-to-quarkus-0200**: Metrics integration updates

## Quality Gates

### Compilation
- All four service classes compile with new CDI patterns
- REST client interface properly configured
- Maven build: `mvn -q clean compile`

### Thread Safety  
- ConcurrentHashMap usage verified for cart storage
- No non-thread-safe collections in service layer

### Configuration Preservation
- CATALOG_ENDPOINT environment variable preserved
- Default fallback configuration maintained

### Business Rule Preservation
- 25% discount on item "329299" maintained
- Shipping tier logic preserved exactly
- Pricing workflow (PromoService → ShippingService) maintained
