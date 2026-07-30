# S02 Domain Models Specification

## Observed Legacy Behavior

The Coolstore Cart Service domain model consists of four core classes that serve as the foundation for all e-commerce cart operations. These classes exhibit tightly coupled relationships where ShoppingCart contains ShoppingCartItem objects, which in turn reference Product objects. The Promotion class provides discount rules that apply to cart items.

### Class Relationships and Dependencies

The dependency analysis reveals a god-node pattern:
- **ShoppingCart**: Highest fan-in (5) - central container for all cart operations
- **Product**: Fan-in (4) - referenced by ShoppingCartItem and used throughout pricing calculations  
- **ShoppingCartItem**: Fan-in (3) - mediates between ShoppingCart and Product relationships
- **Promotion**: Independent - provides discount calculation rules

This god-node structure requires these models to convert first as they serve as the foundation for all service-layer transformations.

### Product Class Behavior (/projects/legacy/src/main/java/com/redhat/coolstore/model/Product.java:1-54)

**Structure**: Standard JavaBean with itemId, name, desc, and price fields
- Serializable with fixed serialVersionUID (-7304814269819778382L)
- Empty default constructor for serialization
- Full constructor: `Product(String itemId, String name, String desc, double price)`
- Standard getter/setter pairs for all fields
- toString() returns formatted string: `"Product [itemId=<id>, name=<name>, desc=<desc>, price=<price>]"`

**Business Rules**:
- All fields are required in full constructor
- Price is stored as primitive double (not BigDecimal) 
- No validation constraints (business rules handled at service layer)
- No equals/hashCode implementation (identity based on object reference)

### Promotion Class Behavior (/projects/legacy/src/main/java/com/redhat/coolstore/model/Promotion.java:1-41)

**Structure**: Simple value object for discount rules
- No explicit serialization (inherits from Object)
- Fields: itemId (String), percentOff (double)
- Empty default constructor
- Full constructor: `Promotion(String itemId, double percentOff)`
- Standard getter/setter pairs
- toString() returns: `"Promotion [itemId=<id>, percentOff=<percent>]"`

**Business Rules**:
- Represents percentage-based discount (e.g., 25% off = 25.0)
- Applied to specific product items via itemId matching
- No validation for percentOff range (0-100 assumed)

### ShoppingCartItem Class Behavior (/projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java:1-58)

**Structure**: Cart line item with pricing context
- Serializable with fixed serialVersionUID (6964558044240061049L)
- Fields: price (double), quantity (int), promoSavings (double), product (Product)
- Empty default constructor
- No parameterized constructor (uses setters)
- Standard getter/setter pairs
- toString() includes product.toString() representation

**Business Rules**:
- price field represents unit price at time of addition (not current catalog price)
- quantity represents integer count of items
- promoSavings accumulates discount amount (not percentage)
- product reference maintains catalog information at time of addition
- No business logic methods - purely data transfer object

### ShoppingCart Class Behavior (/projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCart.java:1-128)

**Structure**: Container for cart state and calculations
- Serializable with fixed serialVersionUID (-1108043957592113528L)
- Fields: cartId (String), shoppingCartItemList (List<ShoppingCartItem>), totals/savings fields
- Constructors: empty default, single-parameter `ShoppingCart(String cartId)`
- Business methods: addShoppingCartItem(), removeShoppingCartItem(), resetShoppingCartItemList()

**Business Logic** (/projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCart.java:52-74):
```java
public void addShoppingCartItem(ShoppingCartItem sci) {
    if (sci != null) {
        shoppingCartItemList.add(sci);
    }
}

public boolean removeShoppingCartItem(ShoppingCartItem sci) {
    boolean removed = false;
    if (sci != null) {
        removed = shoppingCartItemList.remove(sci);
    }
    return removed;
}
```

**Totals Fields** (/projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCart.java:11-23):
- cartItemTotal: Sum of (price × quantity) before promotions
- cartItemPromoSavings: Total discount applied to items
- shippingTotal: Shipping cost before shipping promotions
- shippingPromoSavings: Shipping discount amount
- cartTotal: Final total after all calculations

**Integration Points**:
- add/remove methods maintain list integrity (null-safe)
- No automatic total recalculation (service layer responsibility)
- toString() includes full shoppingCartItemList representation

## API Contract

### Domain Model Export
- **Access Pattern**: All classes use JavaBean conventions (getters/setters)
- **Serialization**: Product and ShoppingCartItem implement Serializable
- **Collection Management**: ShoppingCart maintains ArrayList<ShoppingCartItem>
- **Null Safety**: addShoppingCartItem() validates null input

### Service Integration Points
- **Product**: Referenced by ShoppingCartItem for pricing calculations
- **ShoppingCartItem**: Bridges between ShoppingCart container and Product catalog
- **ShoppingCart**: Central state container for cart operations
- **Promotion**: External dependency applied during pricing workflows

### Data Flow
1. Cart initialization: `new ShoppingCart(String cartId)`
2. Item addition: `cart.addShoppingCartItem(new ShoppingCartItem())`
3. Product association: `item.setProduct(product)` during service operations
4. Promotion application: Service layer calculates promoSavings and updates totals

### Legacy File Evidence
- Product: /projects/legacy/src/main/java/com/redhat/coolstore/model/Product.java
- Promotion: /projects/legacy/src/main/java/com/redhat/coolstore/model/Promotion.java  
- ShoppingCartItem: /projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCartItem.java
- ShoppingCart: /projects/legacy/src/main/java/com/redhat/coolstore/model/ShoppingCart.java

All files preserve existing business logic exactly as documented in the architecture profile §4 behavioral pins.
