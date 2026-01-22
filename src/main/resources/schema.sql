CREATE DATABASE ecommerce_db;
USE ecommerce_db;

CREATE TABLE Users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    phone VARCHAR(20),
    address TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE Categories (
    category_id INT AUTO_INCREMENT PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL,
    description TEXT
);

CREATE TABLE Products (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    description TEXT,
    price DECIMAL(10,2) NOT NULL,
    category_id INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES Categories(category_id)
);

CREATE TABLE Inventory (
    inventory_id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT UNIQUE,
    quantity_available INT NOT NULL,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES Products(product_id)
);

CREATE TABLE Orders (
    order_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT,
    order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(50),
    total_amount DECIMAL(10,2),
    FOREIGN KEY (user_id) REFERENCES Users(user_id)
);

CREATE TABLE OrderItems (
    order_item_id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT,
    product_id INT,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id) REFERENCES Orders(order_id),
    FOREIGN KEY (product_id) REFERENCES Products(product_id)
);

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

-- ============ INDEXES FOR PERFORMANCE ============
-- Indexes optimize common lookups, joins, and range queries
-- Each index improves query performance for specific access patterns

-- User lookup by email (login authentication)
CREATE INDEX idx_users_email ON Users(email);

-- Product filtering by category (catalog browsing)
CREATE INDEX idx_products_category ON Products(category_id);

-- Product search by name (search functionality)
CREATE INDEX idx_products_name ON Products(name);

-- Inventory lookup by product (stock checks)
CREATE INDEX idx_inventory_product ON Inventory(product_id);

-- Order retrieval by user (order history)
CREATE INDEX idx_orders_user ON Orders(user_id);

-- Order sorting by date (recent orders)
CREATE INDEX idx_orders_date ON Orders(order_date);

-- Order items lookup by order (order details)
CREATE INDEX idx_order_items_order ON OrderItems(order_id);

-- Order items lookup by product (product sales)
CREATE INDEX idx_order_items_product ON OrderItems(product_id);

-- Reviews lookup by product (product reviews page)
CREATE INDEX idx_reviews_product ON Reviews(product_id);

-- Reviews lookup by user (user's reviews)
CREATE INDEX idx_reviews_user ON Reviews(user_id);

SHOW TABLES;
