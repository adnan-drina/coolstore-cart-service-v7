# S04 REST Endpoint Modernization - Tasks

#### T-001: Convert CartEndpoint from Spring REST to Quarkus JAX-RS
**Class**: infer
**Findings**: springboot-di-to-quarkus-00003 (1 incident)
**Goal**: Modernize CartEndpoint to stateless Quarkus JAX-RS with proper error handling and HTTP status codes

**Target design** (cite MAPPINGS.md):
- `/projects/legacy/src/main/java/com/redhat/coolstore/rest/CartEndpoint.java` → `src/main/java/com/demo/rest/CartEndpoint.java`
- Package rename: `com.redhat.coolstore.rest` → `com.demo.rest` (full prefix replace per migration.yaml targetPackage)
- Convert Spring annotations to JAX-RS: `@RestController` + `@Scope(session)` → `@Path` (stateless design)
- Migrate imports: `javax.ws.rs.*` → `jakarta.ws.rs.*` (Jakarta EE9)
- Replace field injection: `@Autowired private ShoppingCartService` → `@Inject constructor(ShoppingCartService)`
- Implement error handling with Response wrapper: 404 for missing carts, 400 for invalid input, 503 for service failures
- GET endpoint: idempotent semantics return 404 on missing cartId (targetContract.getIdempotent: true)
- All endpoints: proper HTTP status codes instead of raw exception propagation

**Acceptance**: CartEndpoint.java converted with proper JAX-RS annotations, constructor injection, and error handling; project builds successfully

#### T-002: Create comprehensive endpoint tests with RestAssured
**Class**: infer  
**Findings**: none (new test coverage for endpoint modernization)
**Goal**: Establish REST endpoint test coverage to verify modernized CartEndpoint contracts and error scenarios

**Target design** (test strategy per MAPPINGS.md):
- Create integration tests: `src/test/java/com/demo/rest/CartEndpointTest.java`
- Test all endpoints with RestAssured: GET `/cart/{cartId}`, POST `/cart/{cartId}/{itemId}/{quantity}`, DELETE operations
- Error scenario testing: 400 Bad Request (invalid parameters), 404 Not Found (missing cart), 503 Service Unavailable
- Service layer mocking for isolated endpoint testing (per architecture-profile §7 testing guidance)
- Verify target contract: GET idempotency, input validation, error mapping per targetContract flags
- Cart behavior preservation: additive quantity with deduplication, pricing calculations, promotional rules

**Acceptance**: CartEndpointTest.java with RestAssured tests covering all endpoint scenarios and error conditions; tests pass and verify target contracts

#### T-003: Verify acceptance path and preserved configurations
**Class**: infer
**Findings**: none (verification task for existing configurations)
**Goal**: Ensure acceptance path `/api/cart/acceptance-check` serves correctly and preserved configurations remain functional

**Target design**:
- Verify CATALOG_ENDPOINT preserved from service layer (S03) continues to function in endpoint layer
- Ensure acceptance path serves correctly at `/api/cart/acceptance-check` (migration.yaml line 17)
- UI surface coverage: REST endpoints under `/api/cart/*` provide the complete legacy surface modernization
- Legacy UI surface modernization complete: Spring `@RestController` → JAX-RS `@Path` with proper HTTP semantics

**Acceptance**: Acceptance path verified functional, preserved configurations working, legacy UI surface fully modernized with stateless JAX-RS endpoints
