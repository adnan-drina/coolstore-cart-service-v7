# S02 Domain Models Migration Plan

## Migration Strategy

This story implements a **HARVEST** approach for all four domain model classes, preserving existing structure and business logic while performing package rename transformation. All classes are classified as **rewrite** tasks since they involve mechanical package declarations and import updates only.

## Class Conversion Order

Following the dependency order in migration/dependency-order.md, the god-node classes with highest fan-in convert first:

1. **Product** (fan-in: 4) - Base class referenced by ShoppingCartItem
2. **Promotion** (fan-in: 1) - Independent discount rule class  
3. **ShoppingCartItem** (fan-in: 3) - Mediates between ShoppingCart and Product
4. **ShoppingCart** (fan-in: 5) - Central container with highest coupling

This order ensures the dependency tree remains buildable at each commit.

## Target Architecture

### Package Rename Specification
- **Source**: `com.redhat.coolstore.model.*`
- **Target**: `com.demo.model.*`
- **Scope**: All four model classes
- **Approach**: Full prefix replacement (never `com.demo.coolstore.model`)

### Import Updates
- **javax-to-jakarta**: Already handled by OpenRewrite recipe (migration/recipe-log.md:7)
- **java.io.Serializable**: Preserved as-is (no jakarta equivalent needed)
- **java.util.ArrayList/List**: Preserved as-is

### Class Preservation Rules
All classes maintain **exact structural preservation**:
- Constructors: Default + parameterized constructors unchanged
- Fields: Same names, types, and access modifiers  
- Methods: getXXX/setXXX/toString unchanged
- Serialization: serialVersionUID values preserved
- Business logic: Null-safe add/remove operations maintained

## Migration Tasks by Finding Rule

### Package Rename (mechanical)
**Class: rewrite**

Task T-001: Product.java harvest with package rename
- File: migration/staging/src/main/java/com/redhat/coolstore/model/Product.java
- Target: src/main/java/com/demo/model/Product.java
- Changes: `com.redhat.coolstore.model` → `com.demo.model`

Task T-002: Promotion.java harvest with package rename
- File: migration/staging/src/main/java/com/redhat/coolstore/model/Promotion.java
- Target: src/main/java/com/demo/model/Promotion.java  
- Changes: `com.redhat.coolstore.model` → `com.demo.model`

Task T-003: ShoppingCartItem.java harvest with package rename
- File: migration/staging/src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java
- Target: src/main/java/com/demo/model/ShoppingCartItem.java
- Changes: `com.redhat.coolstore.model` → `com.demo.model`

Task T-004: ShoppingCart.java harvest with package rename
- File: migration/staging/src/main/java/com/redhat/coolstore/model/ShoppingCart.java
- Target: src/main/java/com/demo/model/ShoppingCart.java
- Changes: `com.redhat.coolstore.model` → `com.demo.model`

### Package Structure Creation
**Class: rewrite**

Task T-005: Create model package structure
- Target: src/main/java/com/demo/model/
- Ensure proper directory structure exists for harvested classes

### Package Rename Verification
**Class: rewrite**

Task T-006: Verify no legacy package references remain
- Command: `find src/main/java -name "*.java" | xargs grep -l "com.redhat.coolstore" | wc -l`
- Expected: 0 (zero legacy package references)

## Architecture Profile Compliance

All classes follow the HARVEST classification from architecture-profile.md §7:

### Product - HARVEST
- **Preserve**: itemId, name, desc, price with full constructor and accessors
- **Integration**: Catalog service compatibility maintained
- **Evidence**: /projects/legacy/src/main/java/com/redhat/coolstore/model/Product.java:5-11

### Promotion - HARVEST  
- **Preserve**: itemId, percentOff with constructor and accessors
- **Business logic**: Promotion matching compatibility maintained
- **Evidence**: /projects/legacy/src/main/java/com/redhat/coolstore/model/Promotion.java:5-7

### ShoppingCartItem - HARVEST
- **Preserve**: price, quantity, promoSavings, product references
- **Integration**: ShoppingCart pricing workflow compatibility
- **Evidence**: /projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java:9-12

### ShoppingCart - HARVEST
- **Preserve**: cartId, shoppingCartItemList, totals, savings fields
- **Business logic**: Pricing workflow integration points maintained
- **Evidence**: /projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCart.java:21-23

## Story Dependency Analysis

**God-Node Strategy**: All four classes are god-nodes that require characterization tests to pin behavior before conversion:
- ShoppingCart (fan-in: 5) - Central integration point
- Product (fan-in: 4) - Catalog data source  
- ShoppingCartItem (fan-in: 3) - Cart line item mediator

**Testing Strategy**: Characterization tests will be added in a subsequent story once service integration is complete, as these models serve as the foundation for all service-layer operations.

## Findings Resolution

This story partially resolves two mandatory findings:

1. **removed-javaee-modules-00020** (partial): Model files contain no JavaEE imports, so this story provides partial coverage
2. **javax-to-jakarta-import-00001** (complete): Already handled by OpenRewrite recipe per migration/recipe-log.md

## Quality Gates

### Compilation
- All four classes must compile with package rename
- No import resolution errors  
- Maven build: `mvn -q clean compile`

### Package Verification  
- Zero `com.redhat.coolstore` package references in src/main/java
- Command verification: `find src/main/java -name "*.java" | xargs grep -l "com.redhat.coolstore" | wc -l == 0`

### Business Logic Preservation
- Constructor signatures unchanged
- Field types and names preserved
- toString() methods unchanged
- Null-safe add/remove operations maintained
