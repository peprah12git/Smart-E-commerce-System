# E-Commerce Desktop Application

## Project Overview
A full-featured e-commerce desktop application built with JavaFX, featuring a complete shopping platform with product catalog, shopping cart, order management, and customer reviews.

## Technology Stack

| Component | Technology |
|-----------|------------|
| **Frontend** | JavaFX 21 (FXML + CSS) |
| **Backend** | Java 21 |
| **Relational Database** | MySQL 8.0 |
| **NoSQL Database** | MongoDB 6.0 |
| **Build Tool** | Maven 3.9+ |
| **Architecture** | MVC Pattern |

## Features

### Customer Features
- ✅ Browse products with search and category filters
- ✅ View detailed product information with reviews
- ✅ Shopping cart management (add, update, remove items)
- ✅ Secure checkout process
- ✅ Order history viewing
- ✅ Submit and view product reviews
- ✅ Guest browsing (no login required)

### Admin Features
- ✅ Product management (CRUD operations)
- ✅ Category management
- ✅ Inventory tracking
- ✅ Order management and status updates
- ✅ User management

## Prerequisites

Before running this application, ensure you have the following installed:

1. **Java Development Kit (JDK) 21+**
   - Download: https://adoptium.net/
   - Verify: `java --version`

2. **Apache Maven 3.9+**
   - Download: https://maven.apache.org/download.cgi
   - Verify: `mvn --version`

3. **MySQL Server 8.0+**
   - Download: https://dev.mysql.com/downloads/mysql/
   - Create database: `ecommerce_db`

4. **MongoDB 6.0+ (Optional - for NoSQL features)**
   - Download: https://www.mongodb.com/try/download/community
   - Start MongoDB service

## Installation & Setup

### Step 1: Clone the Repository
```bash
cd "c:\Users\EmmanuelPeprahMensah\Desktop\New folder\e-c0mmerce"
```

### Step 2: Configure Database Connection
Edit `src/main/java/com/ecommerce/config/DatabaseConnection.java`:
```java
private static final String URL = "jdbc:mysql://localhost:3306/ecommerce_db";
private static final String USERNAME = "your_username";
private static final String PASSWORD = "your_password";
```

### Step 3: Create Database Schema
Run the schema script in MySQL:
```sql
source src/main/resources/com/ecommerce/schema.sql
```

Or execute from command line:
```bash
mysql -u root -p < src/main/resources/com/ecommerce/schema.sql
```

### Step 4: Install Dependencies
```bash
mvn clean install
```

### Step 5: Run the Application
```bash
mvn javafx:run
```

Or use the provided batch file:
```bash
run_javafx.bat
```

## Project Structure

```
src/main/java/com/ecommerce/
├── Main.java                 # Application entry point
├── config/
│   ├── DatabaseConnection.java    # MySQL connection singleton
│   └── MongoDBConnection.java     # MongoDB connection singleton
├── controllers/
│   ├── LoginController.java       # User authentication
│   ├── AdminLoginController.java  # Admin authentication
│   ├── ClientViewController.java  # Main client interface
│   ├── ProductBrowserController.java  # Product catalog
│   ├── CartViewController.java    # Shopping cart
│   ├── CheckoutController.java    # Order checkout
│   └── OrderHistoryController.java # Order history
├── dao/
│   ├── UserDAO.java              # User database operations
│   ├── ProductDAO.java           # Product database operations
│   ├── CategoryDAO.java          # Category database operations
│   ├── OrderDAO.java             # Order database operations
│   ├── OrderItemDAO.java         # Order items operations
│   ├── ReviewDAO.java            # Reviews (MySQL)
│   ├── ReviewMongoDAO.java       # Reviews (MongoDB)
│   ├── InventoryDAO.java         # Inventory operations
│   └── ApplicationLogDAO.java    # Logging (MongoDB)
├── models/
│   ├── User.java
│   ├── Product.java
│   ├── Category.java
│   ├── Order.java
│   ├── OrderItem.java
│   ├── CartItem.java
│   ├── Review.java
│   └── Inventory.java
├── service/
│   ├── CartService.java          # Cart business logic
│   ├── ReviewService.java        # Review business logic
│   └── InventoryService.java     # Inventory business logic
└── ui/
    └── App.java                  # JavaFX Application class
```

## Database Schema

### Entity Relationship Overview
- **Users** - Customer and admin accounts
- **Products** - Product catalog with categories
- **Categories** - Product categorization
- **Inventory** - Stock tracking per product
- **Orders** - Customer orders
- **OrderItems** - Individual items within orders
- **Reviews** - Customer product reviews

### Performance Indexes
```sql
-- Optimized indexes for common queries
CREATE INDEX idx_users_email ON Users(email);
CREATE INDEX idx_products_category ON Products(category_id);
CREATE INDEX idx_products_name ON Products(name);
CREATE INDEX idx_orders_user ON Orders(user_id);
CREATE INDEX idx_orders_date ON Orders(order_date);
CREATE INDEX idx_reviews_product ON Reviews(product_id);
```

## Usage Guide

### Logging In
1. Launch the application
2. Click **"Continue as Guest"** for customer access
3. Or click **"Admin Login"** for administrative access
   - Default admin: `admin@admin.com` / `admin123`

### Shopping
1. Browse products on the main catalog page
2. Use search bar and category filters to find items
3. Click **"Add to Cart"** on any product
4. Navigate to **Cart** to review and modify items
5. Proceed to **Checkout** to complete purchase

### Admin Panel
1. Manage products (add, edit, delete)
2. Update inventory quantities
3. View and update order statuses
4. Manage user accounts

## Testing

### Test Credentials
| Role | Email | Password |
|------|-------|----------|
| Admin | admin@admin.com | admin123 |
| User | john.doe@email.com | hashed_password_123 |

### Sample Data
The application includes pre-populated sample data:
- 5 categories (Electronics, Clothing, Home & Garden, Sports, Books)
- 12 sample products
- Sample inventory records
- Sample orders and reviews

## Troubleshooting

### Common Issues

**1. "Cannot connect to MySQL"**
- Ensure MySQL service is running
- Verify credentials in DatabaseConnection.java
- Check if database `ecommerce_db` exists

**2. "MongoDB connection failed"**
- MongoDB is optional for core functionality
- Start MongoDB service if using NoSQL features
- Reviews will fall back to MySQL if MongoDB unavailable

**3. "JavaFX runtime components are missing"**
- Use `mvn javafx:run` instead of `java -jar`
- Ensure JavaFX dependencies in pom.xml

**4. Application won't start**
- Run `mvn clean install` to rebuild
- Check for compilation errors

## Documentation

- [Architecture Overview](docs/ARCHITECTURE.md)
- [Database Design Document](docs/DATABASE_DESIGN.md)
- [Performance Report](docs/PERFORMANCE_REPORT.md)
- [NoSQL Comparison](docs/NOSQL_COMPARISON.md)

## License
This project is for educational purposes.

## Contributors
- Emmanuel Peprah Mensah
