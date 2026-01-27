# Database Design Document

## 1. Executive Summary

This document describes the database architecture for the E-Commerce Desktop Application. The system uses a **hybrid database approach**:
- **MySQL** (Relational) - For structured transactional data (users, products, orders)
- **MongoDB** (NoSQL) - For unstructured data (application logs, flexible reviews)

## 2. Entity Relationship Diagram (ERD)

### 2.1 Conceptual Model

```
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│   USER      │         │   ORDER     │         │  PRODUCT    │
│             │────────<│             │>────────│             │
│ • user_id   │  places │ • order_id  │ contains│ • product_id│
│ • name      │         │ • status    │         │ • name      │
│ • email     │         │ • total     │         │ • price     │
└─────────────┘         └─────────────┘         └─────────────┘
      │                                               │
      │                                               │
      │  writes                                 belongs to
      ▼                                               ▼
┌─────────────┐                               ┌─────────────┐
│   REVIEW    │                               │  CATEGORY   │
│             │                               │             │
│ • review_id │                               │ • category_id│
│ • rating    │                               │ • name      │
│ • comment   │                               └─────────────┘
└─────────────┘                                      │
                                                     │
                                               ┌─────────────┐
                                               │  INVENTORY  │
                                               │             │
                                               │ • quantity  │
                                               └─────────────┘
```

### 2.2 Logical Model (Detailed ERD)

```
┌──────────────────────────────────────────────────────────────────────────┐
│                           E-COMMERCE DATABASE                             │
└──────────────────────────────────────────────────────────────────────────┘

    ┌────────────────────┐
    │       USERS        │
    ├────────────────────┤
    │ PK user_id    INT  │
    │    name       VARCHAR(100) │
    │ UK email      VARCHAR(150) │
    │    password   VARCHAR(255) │
    │    phone      VARCHAR(20)  │
    │    address    TEXT         │
    │    role       VARCHAR(20)  │
    │    created_at TIMESTAMP    │
    └────────────────────┘
            │
            │ 1:N
            ▼
    ┌────────────────────┐       ┌────────────────────┐
    │      ORDERS        │       │     REVIEWS        │
    ├────────────────────┤       ├────────────────────┤
    │ PK order_id   INT  │       │ PK review_id  INT  │
    │ FK user_id    INT  │◄──────│ FK user_id    INT  │
    │    order_date TIMESTAMP    │ FK product_id INT  │
    │    status     VARCHAR(50)  │    rating     INT  │
    │    total_amt  DECIMAL      │    comment    TEXT │
    └────────────────────┘       │    review_date TIMESTAMP│
            │                    └────────────────────┘
            │ 1:N                         │
            ▼                             │
    ┌────────────────────┐               │
    │    ORDER_ITEMS     │               │
    ├────────────────────┤               │
    │ PK order_item_id INT│               │
    │ FK order_id    INT │               │
    │ FK product_id  INT │◄──────────────┘
    │    quantity    INT │
    │    unit_price  DECIMAL │
    └────────────────────┘
            │
            │ N:1
            ▼
    ┌────────────────────┐       ┌────────────────────┐
    │     PRODUCTS       │       │    CATEGORIES      │
    ├────────────────────┤       ├────────────────────┤
    │ PK product_id INT  │       │ PK category_id INT │
    │    name VARCHAR(150)│◄─────│    category_name VARCHAR│
    │    description TEXT│  N:1  │    description TEXT│
    │    price DECIMAL   │       └────────────────────┘
    │ FK category_id INT │
    │    created_at TIMESTAMP│
    └────────────────────┘
            │
            │ 1:1
            ▼
    ┌────────────────────┐
    │     INVENTORY      │
    ├────────────────────┤
    │ PK inventory_id INT│
    │ FK product_id INT (UNIQUE)│
    │    qty_available INT│
    │    last_updated TIMESTAMP│
    └────────────────────┘
```

### 2.3 Physical Model (MySQL Implementation)

```sql
-- Users Table: Core user authentication and profile
CREATE TABLE Users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    role VARCHAR(20) DEFAULT 'user',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Categories Table: Product classification
CREATE TABLE Categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL,
    description TEXT
);

-- Products Table: Product catalog
CREATE TABLE Products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    category_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES Categories(category_id)
);

-- Inventory Table: Stock management (1:1 with Products)
CREATE TABLE Inventory (
    inventory_id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT UNIQUE,
    quantity_available INT NOT NULL,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES Products(product_id)
);

-- Orders Table: Customer purchases
CREATE TABLE Orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50),
    total_amount DECIMAL(10,2),
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

-- OrderItems Table: Line items in orders (Bridge table)
CREATE TABLE OrderItems (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT,
    product_id INT,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES Orders(order_id),
    FOREIGN KEY (product_id) REFERENCES Products(product_id)
);

-- Reviews Table: Product reviews and ratings
CREATE TABLE Reviews (
    review_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    product_id INT,
    rating INT CHECK (rating BETWEEN 1 AND 5),
    comment TEXT,
    review_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id),
    FOREIGN KEY (product_id) REFERENCES Products(product_id)
);
```

## 3. Table Relationships Summary

| Relationship | Type | Description |
|--------------|------|-------------|
| Users → Orders | 1:N | One user can have many orders |
| Users → Reviews | 1:N | One user can write many reviews |
| Orders → OrderItems | 1:N | One order contains many items |
| Products → OrderItems | 1:N | One product can be in many orders |
| Products → Reviews | 1:N | One product can have many reviews |
| Categories → Products | 1:N | One category has many products |
| Products → Inventory | 1:1 | Each product has one inventory record |

## 4. Database Constraints

### 4.1 Primary Keys
All tables use AUTO_INCREMENT integer primary keys for:
- Fast indexing and joins
- Simplicity in foreign key relationships
- Consistent ID generation

### 4.2 Foreign Keys
```sql
-- Product belongs to Category
FOREIGN KEY (category_id) REFERENCES Categories(category_id)

-- Inventory tracks Product
FOREIGN KEY (product_id) REFERENCES Products(product_id)

-- Order belongs to User
FOREIGN KEY (user_id) REFERENCES Users(user_id)

-- OrderItem belongs to Order and Product
FOREIGN KEY (order_id) REFERENCES Orders(order_id)
FOREIGN KEY (product_id) REFERENCES Products(product_id)

-- Review belongs to User and Product
FOREIGN KEY (user_id) REFERENCES Users(user_id)
FOREIGN KEY (product_id) REFERENCES Products(product_id)
```

### 4.3 Unique Constraints
- `Users.email` - Ensures unique user accounts
- `Inventory.product_id` - Enforces 1:1 relationship with Products

### 4.4 Check Constraints
- `Reviews.rating` - Must be between 1 and 5

## 5. Normalization Analysis

The database follows **Third Normal Form (3NF)**:

### First Normal Form (1NF) ✓
- All columns contain atomic values
- Each column has a unique name
- All entries in a column are of the same data type

### Second Normal Form (2NF) ✓
- All non-key attributes are fully dependent on the primary key
- No partial dependencies exist

### Third Normal Form (3NF) ✓
- No transitive dependencies
- All non-key attributes depend only on the primary key
- Example: `OrderItems.unit_price` stores the price at time of purchase (not derived from Products)

## 6. Data Types Selection

| Column Type | MySQL Type | Rationale |
|-------------|------------|-----------|
| IDs | INT AUTO_INCREMENT | Efficient, auto-generated |
| Names | VARCHAR(100-150) | Variable length, indexed |
| Email | VARCHAR(150) UNIQUE | Uniqueness enforced |
| Prices | DECIMAL(10,2) | Precise monetary values |
| Text Content | TEXT | Variable-length descriptions |
| Timestamps | TIMESTAMP | Automatic date/time tracking |
| Quantities | INT | Whole numbers for counts |
| Ratings | INT (1-5) | Constrained value range |

## 7. MongoDB Collections

### 7.1 Application Logs Collection
```javascript
{
  "_id": ObjectId("..."),
  "logLevel": "INFO",
  "message": "User login successful",
  "source": "LoginController",
  "methodName": "handleLogin",
  "timestamp": ISODate("2024-01-15T10:30:00Z"),
  "userId": 1,
  "executionTimeMs": 45,
  "metadata": {
    "browser": "JavaFX",
    "ipAddress": "127.0.0.1"
  }
}
```

### 7.2 Reviews Collection (MongoDB Mirror)
```javascript
{
  "_id": ObjectId("..."),
  "reviewId": 1,
  "productId": 5,
  "userId": 2,
  "rating": 4,
  "comment": "Great product!",
  "createdAt": 1705312200000,
  "helpful_count": 15,
  "unhelpful_count": 2,
  "verified_purchase": true,
  "tags": ["quality", "value"],
  "media": ["/images/review_1.jpg"],
  "replies": [
    {
      "userId": 1,
      "text": "Thank you!",
      "timestamp": 1705315800000
    }
  ],
  "sentiment": "positive"
}
```

## 8. Security Considerations

1. **Password Storage**: Passwords should be hashed (bcrypt recommended)
2. **SQL Injection Prevention**: All DAOs use PreparedStatement
3. **Input Validation**: Controllers validate input before database operations
4. **Role-Based Access**: User role field controls admin vs customer access

## 9. Conclusion

This database design provides:
- **Data Integrity**: Through foreign keys and constraints
- **Performance**: Through strategic indexing
- **Scalability**: Normalized structure prevents data anomalies
- **Flexibility**: MongoDB handles unstructured data needs
- **Maintainability**: Clear separation of concerns
