# S02 Domain Models Tasks

#### T-001: Create model package structure
**Class**: rewrite
**Findings**: N/A
**Goal**: Create com.demo.model package directory structure
**Target design**:
- Directory: src/main/java/com/demo/model/
**Acceptance**: Directory exists; subsequent tasks can place files here

#### T-002: Product.java harvest with package rename
**Class**: rewrite  
**Findings**: removed-javaee-modules-00020 (partial), javax-to-jakarta-import-00001 (complete via recipe)
**Goal**: Harvest Product domain model with package rename com.redhat.coolstore.model → com.demo.model
**Target design**:
- migration/staging/src/main/java/com/redhat/coolstore/model/Product.java → src/main/java/com/demo/model/Product.java
- Package declaration: `com.redhat.coolstore.model` → `com.demo.model`
- Preserve exact structure: constructors, fields, methods, serialVersionUID
**Acceptance**: Product.java compiles; toString() output unchanged; zero legacy package references

#### T-003: Promotion.java harvest with package rename
**Class**: rewrite
**Findings**: removed-javaee-modules-00020 (partial), javax-to-jakarta-import-00001 (complete via recipe)  
**Goal**: Harvest Promotion domain model with package rename com.redhat.coolstore.model → com.demo.model
**Target design**:
- migration/staging/src/main/java/com/redhat/coolstore/model/Promotion.java → src/main/java/com/demo/model/Promotion.java
- Package declaration: `com.redhat.coolstore.model` → `com.demo.model`
- Preserve exact structure: constructors, fields, methods
**Acceptance**: Promotion.java compiles; toString() output unchanged; zero legacy package references

#### T-004: ShoppingCartItem.java harvest with package rename
**Class**: rewrite
**Findings**: removed-javaee-modules-00020 (partial), javax-to-jakarta-import-00001 (complete via recipe)
**Goal**: Harvest ShoppingCartItem domain model with package rename com.redhat.coolstore.model → com.demo.model  
**Target design**:
- migration/staging/src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java → src/main/java/com/demo/model/ShoppingCartItem.java
- Package declaration: `com.redhat.coolstore.model` → `com.demo.model`
- Preserve exact structure: constructors, fields, methods, serialVersionUID
**Acceptance**: ShoppingCartItem.java compiles; toString() output unchanged; Product reference preserved

#### T-005: ShoppingCart.java harvest with package rename
**Class**: rewrite
**Findings**: removed-javaee-modules-00020 (partial), javax-to-jakarta-import-00001 (complete via recipe)
**Goal**: Harvest ShoppingCart domain model with package rename com.redhat.coolstore.model → com.demo.model
**Target design**:
- migration/staging/src/main/java/com/redhat/coolstore/model/ShoppingCart.java → src/main/java/com/demo/model/ShoppingCart.java  
- Package declaration: `com.redhat.coolstore.model` → `com.demo.model`
- Preserve exact structure: constructors, fields, methods, serialVersionUID
- Business logic: addShoppingCartItem(), removeShoppingCartItem() null-safety preserved
**Acceptance**: ShoppingCart.java compiles; ShoppingCartItem list operations unchanged; totals fields preserved

#### T-006: Add domain model characterization tests
**Class**: rewrite
**Findings**: N/A
**Goal**: Add characterization tests to verify domain model behavior for god-node classes
**Target design**:
- Test: src/test/java/com/demo/model/DomainModelTest.java
- Test Product constructor, getters, toString(), serialization
- Test ShoppingCart add/remove operations, cart management methods
- Test ShoppingCartItem structure and product references
- Test Promotion basic value object behavior
**Acceptance**: All domain model characterization tests pass; behavior verified

#### T-007: Package rename verification
**Class**: rewrite
**Findings**: removed-javaee-modules-00020 (complete), javax-to-jakarta-import-00001 (complete)
**Goal**: Verify no legacy package references remain in src/main/java
**Target design**:
- Command: `find src/main/java -name "*.java" | xargs grep -l "com.redhat.coolstore" | wc -l`
- Expected result: 0 (zero legacy package references)
**Acceptance**: Verification command returns 0; legacy package references eliminated

## Story Scope Waivers

**UI Surface**: Explicitly waived - S02 is domain model harvest only, no REST endpoints or web surface. UI coverage will be provided by service-layer modernization stories (S03+).

**CATALOG_ENDPOINT Integration**: Explicitly waived - External catalog service integration is service-layer concern handled in ShoppingCartService modernization story (S03). Domain models are data-only harvest.

**Acceptance Path '/api/cart/acceptance-check'**: Explicitly waived - REST endpoint acceptance testing requires JAX-RS @Path resource, which violates S02 brief scope "No service classes or REST endpoints". Handler requires service layer modernization (S03+).
