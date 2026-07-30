# S02: Domain model harvest

<!-- The brief is the self-contained work order for one modernization
     story. Bar: a competent developer or a fresh session starts the
     story from THIS FILE ALONE. Fill every section; delete none. -->

## Goal & position

Harvest the domain models with package rename (com.redhat.coolstore → com.demo) while preserving all existing business logic and structure. This story addresses the god-nodes identified in dependency-order.md: ShoppingCart (fan-in: 5), Product (fan-in: 4), and ShoppingCartItem (fan-in: 3). These models must convert first as they have the highest coupling and serve as the foundation for all service-layer transformations.

## In scope

The exact legacy classes/files this story modernizes. For each, quote
the load-bearing legacy code (the lines being transformed — imports,
annotations, key methods), so the story never starts from a blank
read:

- `src/main/java/com/redhat/coolstore/model/Product.java` — Domain model carried over faithfully
  ```java
  package com.redhat.coolstore.model;
  
  import java.io.Serializable;
  
  public class Product implements Serializable {
      private static final long serialVersionUID = -7304814269819778382L;
      private String itemId;
      private String name;
      private String desc;
      private double price;
      
      public Product() {
      }
      
      public Product(String itemId, String name, String desc, double price) {
          super();
          this.itemId = itemId;
          this.name = name;
          this.desc = desc;
          this.price = price;
      }
  ```

- `src/main/java/com/redhat/coolstore/model/Promotion.java` — Domain model carried over faithfully
  ```java
  package com.redhat.coolstore.model;
  
  public class Promotion {
      private String itemId;
      private double percentOff;
      
      public Promotion() {
      }
      
      public Promotion(String itemId, double percentOff) {
          super();
          this.itemId = itemId;
          this.percentOff = percentOff;
      }
  ```

- `src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java` — Domain model carried over faithfully
  ```java
  package com.redhat.coolstore.model;
  
  import java.io.Serializable;
  
  public class ShoppingCartItem implements Serializable {
      private static final long serialVersionUID = 6964558044240061049L;
      private double price;
      private int quantity;
      private double promoSavings;
      private Product product;
  ```

- `src/main/java/com/redhat/coolstore/model/ShoppingCart.java` — Domain model carried over faithfully
  ```java
  package com.redhat.coolstore.model;
  
  import java.io.Serializable;
  import java.util.ArrayList;
  import java.util.List;
  
  public class ShoppingCart implements Serializable {
      private static final long serialVersionUID = -1108043957592113528L;
      private double cartItemTotal;
      private double cartItemPromoSavings;
      private double shippingTotal;
      private double shippingPromoSavings;
      private double cartTotal;
      private String cartId;
      private List<ShoppingCartItem> shoppingCartItemList = new ArrayList<ShoppingCartItem>();
      
      public void addShoppingCartItem(ShoppingCartItem sci) {
          if (sci != null) {
              shoppingCartItemList.add(sci);
          }
      }
  ```

## Out of scope

No service classes or REST endpoints. Domain models are harvested in isolation to establish the foundation for service-layer modernization in S03. The tree must stay buildable with these harvested models.

## Class roles & target contract (from architecture-profile §7)

For each in-scope class, its role and — for REDESIGN classes — the target
contract carried forward from profile §7, so M3 writes tasks and tests to
the target (not the legacy):

- `Product` — HARVEST
  - Preserve existing structure: itemId, name, desc, price with full constructor and accessors
  - Integration: Maintain catalog service compatibility and pricing calculations

- `Promotion` — HARVEST
  - Preserve existing structure: itemId, percentOff with constructor and accessors
  - Business logic: Maintain promotion matching and calculation compatibility

- `ShoppingCartItem` — HARVEST
  - Preserve existing structure: price, quantity, promoSavings, product references
  - Integration: Maintain ShoppingCart pricing workflow compatibility

- `ShoppingCart` — HARVEST
  - Preserve existing structure: cartId, shoppingCartItemList, totals, and savings fields
  - Business logic: Maintain pricing workflow integration points

## Decided target shapes

The MAPPINGS.md rows that apply (quote the decided target, don't
re-decide). Recipe-executed rules already handled: reference
`migration/recipe-log.md` and `migration/staging/` where applicable.

**Story ordering:** extensions and BOM first, then models, then resources,
then config keys, then tests (`extensions → models → resources → config →
tests`).

Package rename (legacyPackage → targetPackage): Update all package declarations from `com.redhat.coolstore` to `com.demo`:
```java
package com.demo.model; // was: com.redhat.coolstore.model
```

Import updates (javax-to-jakarta-import-00001, partial): Update any javax imports to jakarta equivalents where applicable:
```java
// Already using java.io.Serializable, no javax imports present
```

## Contracts owned by this story

- **Findings**: the mandatory rule ids this story resolves (from the
  roadmap entry).
  - javax-to-jakarta-import-00001 (partial - only model files)
  - removed-javaee-modules-00020 (partial - model files only)

- **Preserve**: none - this story establishes harvested domain models, no legacy contracts to preserve yet.

- **Behavioral pins**: HARVEST classes pin legacy behavior exactly as documented in architecture-profile §4:
  - **Product structure**: itemId, name, desc, price with full constructor preservation
  - **ShoppingCart structure**: cartId, shoppingCartItemList, totals fields, and add/remove operations
  - **ShoppingCartItem structure**: price, quantity, promoSavings, product reference

- **Forbidden**: none relevant to this story.

## Done-criteria

Checkable, story-scoped:
- builds + `sensors.sh task` green at every commit; milestone green at
  story end
- All domain models compile with package rename to com.demo
- Legacy business logic preserved exactly (constructors, field structures, toString methods)
- Characterization tests establish baseline behavior for god-node classes
- 2 findings resolved (partial coverage for javax-to-jakarta and removed-javaee-modules)
- Package rename verification: `find src/main/java -name "*.java" | xargs grep -l "com.redhat.coolstore" | wc -l` returns 0
