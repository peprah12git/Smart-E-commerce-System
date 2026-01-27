# Application Architecture

## Layered Architecture (Separation of Concerns)

This application follows a **3-tier layered architecture** with strict separation of concerns:

```
┌─────────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                            │
│                    (Controllers + FXML)                          │
├─────────────────────────────────────────────────────────────────┤
│  LoginController      │  ProductBrowserController               │
│  AdminLoginController │  ProductDetailController                │
│  ClientViewController │  CartViewController                     │
│  CheckoutController   │  OrderHistoryController                 │
└────────────────────────────┬────────────────────────────────────┘
                             │ Uses
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    BUSINESS LOGIC LAYER                          │
│                         (Services)                               │
├─────────────────────────────────────────────────────────────────┤
│  UserService          │  ProductService                         │
│  OrderService         │  CategoryService                        │
│  CartService          │  ReviewService                          │
│  InventoryService     │                                         │
└────────────────────────────┬────────────────────────────────────┘
                             │ Uses
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    DATA ACCESS LAYER                             │
│                         (DAOs)                                   │
├─────────────────────────────────────────────────────────────────┤
│  UserDAO              │  ProductDAO                             │
│  OrderDAO             │  CategoryDAO                            │
│  OrderItemDAO         │  ReviewDAO / ReviewMongoDAO             │
│  InventoryDAO         │  ApplicationLogDAO                      │
└────────────────────────────┬────────────────────────────────────┘
                             │ Uses
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                    DATA STORAGE LAYER                            │
│                   (MySQL + MongoDB)                              │
├─────────────────────────────────────────────────────────────────┤
│  DatabaseConnection (MySQL Singleton)                           │
│  MongoDBConnection (MongoDB Singleton)                          │
└─────────────────────────────────────────────────────────────────┘
```

## Layer Responsibilities

### 1. Presentation Layer (Controllers)
- **Purpose**: Handle user interactions and UI updates
- **Responsibilities**:
  - Receive user input from FXML views
  - Validate user input (basic validation)
  - Call appropriate service methods
  - Display results/errors to user
  - Navigation between views
- **Does NOT**:
  - Access database directly
  - Contain business logic
  - Know about SQL/NoSQL operations

### 2. Business Logic Layer (Services)
- **Purpose**: Implement business rules and orchestrate operations
- **Responsibilities**:
  - Business validation (email format, password strength)
  - Complex calculations (tax, totals)
  - Caching strategies
  - Data transformation
  - Coordinate multiple DAO operations
  - Transaction management
- **Does NOT**:
  - Handle UI events
  - Know about FXML or JavaFX components
  - Write raw SQL (uses DAOs)

### 3. Data Access Layer (DAOs)
- **Purpose**: Abstract database operations
- **Responsibilities**:
  - CRUD operations
  - SQL query execution
  - Result set mapping to models
  - Connection management
  - Prepared statements (prevent SQL injection)
- **Does NOT**:
  - Contain business logic
  - Know about services or controllers
  - Make business decisions

## Service Layer Details

### UserService
```java
// Authentication
User authenticate(email, password)
User authenticateAdmin(email, password)

// Registration with validation
RegisterResult registerUser(name, email, password, phone, address)

// CRUD operations
List<User> getAllUsers()
User getUserById(int userId)
boolean updateUser(User user)
boolean deleteUser(int userId)

// Password management
boolean changePassword(int userId, String oldPassword, String newPassword)
```

### ProductService
```java
// CRUD operations
List<Product> getAllProducts()
Product getProductById(int productId)
boolean addProduct(Product product)
boolean updateProduct(Product product)
boolean deleteProduct(int productId)

// Search & Filter
List<Product> searchProducts(String searchTerm)
List<Product> searchAndFilter(String searchTerm, String categoryName)
List<Product> getProductsByCategory(int categoryId)
List<Product> getProductsByPriceRange(double min, double max)

// Sorting
List<Product> sortByName(boolean ascending)
List<Product> sortByPrice(boolean ascending)
List<Product> sortByDate(boolean newest)

// Business logic
boolean isProductAvailable(int productId, int quantity)
List<Product> getInStockProducts()
```

### OrderService
```java
// Order creation
OrderResult createOrder(int userId, List<CartItem> cartItems)

// Calculations
BigDecimal calculateSubtotal(List<CartItem> items)
BigDecimal calculateTax(BigDecimal subtotal)
BigDecimal calculateTotal(BigDecimal subtotal)

// Order retrieval
List<Order> getAllOrders()
Order getOrderById(int orderId)
List<Order> getOrdersByUserId(int userId)
Order getOrderWithItems(int orderId)

// Status management
boolean updateOrderStatus(int orderId, String status)
boolean cancelOrder(int orderId)
List<String> getStatusOptions()

// Filtering & Sorting
List<Order> getOrdersByStatus(String status)
List<Order> sortByDate(boolean newest)
List<Order> sortByTotal(boolean descending)
```

### CategoryService
```java
// CRUD operations
List<Category> getAllCategories()
Category getCategoryById(int categoryId)
boolean addCategory(Category category)
boolean updateCategory(Category category)
boolean deleteCategory(int categoryId)

// Helpers
List<String> getCategoryNames()
List<String> getCategoryNamesWithAll()
int getCategoryIdByName(String name)
String getCategoryNameById(int categoryId)
```

### ReviewService
```java
// CRUD operations
boolean addReview(Review review)
List<Review> getReviewsByProductId(int productId)
List<Review> getAllReviews()
boolean deleteReview(int id)

// Business logic
double getAverageRating(int productId)
List<Review> getHighRatedReviews(int productId, int minRating)
List<Review> getRecentReviews(int productId, int limit)

// Sorting
List<Review> sortByRating(int productId, boolean ascending)
```

### InventoryService
```java
// CRUD operations
boolean updateInventory(int productId, int quantity)
Inventory getInventoryByProductId(int productId)
List<Inventory> getAllInventory()
List<Inventory> getLowStockItems(int threshold)

// Business logic
boolean isInStock(int productId)
boolean hasEnoughStock(int productId, int requestedQuantity)
boolean reduceStock(int productId, int quantity)
boolean addStock(int productId, int quantity)

// Filtering & Sorting
List<Inventory> getOutOfStockItems()
List<Inventory> sortByQuantity(boolean ascending)
```

### CartService
```java
// Cart operations
void addItem(CartItem item)
void removeItem(CartItem item)
void removeItem(int productId)
void updateQuantity(int productId, int quantity)
void clearCart()

// Queries
ObservableList<CartItem> getCartItems()
BigDecimal getTotalPrice()
int getItemCount()
boolean isEmpty()
```

## Data Flow Example

### User Login Flow
```
LoginController.handleSignIn()
    │
    ▼
UserService.authenticate(email, password)
    │
    ├── Validates input
    │
    ▼
UserDAO.getUserByEmail(email)
    │
    ├── Executes SQL query
    │
    ▼
Returns User object
    │
    ▼
UserService verifies password
    │
    ▼
Returns User to Controller
    │
    ▼
Controller updates UI
```

### Place Order Flow
```
CheckoutController.placeOrder()
    │
    ▼
OrderService.createOrder(userId, cartItems)
    │
    ├── Calculates subtotal, tax, total
    ├── Validates inventory
    │
    ▼
OrderDAO.addOrder(order)
    │
    ├── Inserts order record
    │
    ▼
OrderItemDAO.addOrderItem() [for each item]
    │
    ├── Inserts order items
    │
    ▼
InventoryService.reduceStock() [for each item]
    │
    ├── Updates inventory
    │
    ▼
Returns OrderResult to Controller
    │
    ▼
Controller clears cart, shows success message
```

## Caching Strategy

Services implement caching to reduce database load:

| Service | Cache Type | TTL | Invalidation |
|---------|-----------|-----|--------------|
| ProductService | Static List | 5 min | On CRUD operations |
| CategoryService | Instance List | 10 min | On CRUD operations |
| ReviewService | HashMap (by product) | 5 min | On add/delete |
| InventoryService | Instance List | 2 min | On update |

## Benefits of This Architecture

1. **Maintainability**: Changes in one layer don't affect others
2. **Testability**: Each layer can be unit tested independently
3. **Reusability**: Services can be used by multiple controllers
4. **Scalability**: Easy to add new features without affecting existing code
5. **Security**: Centralized validation in service layer
6. **Performance**: Caching in service layer reduces DB calls
