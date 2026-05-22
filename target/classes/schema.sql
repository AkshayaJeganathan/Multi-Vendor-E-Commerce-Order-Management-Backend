-- =====================================================
-- Multi-Vendor E-Commerce - Database Schema
-- Run this manually in MySQL after creating the DB
-- =====================================================

CREATE DATABASE IF NOT EXISTS multivendor_ecom;
USE multivendor_ecom;

-- -----------------------------------------------
-- 1. SELLERS
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS sellers (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    phone       VARCHAR(15)  NOT NULL,
    gst_number  VARCHAR(20),
    address     TEXT,
    status      ENUM('ACTIVE','INACTIVE','SUSPENDED') DEFAULT 'ACTIVE',
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- -----------------------------------------------
-- 2. PRODUCTS
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS products (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id     BIGINT NOT NULL,
    name          VARCHAR(200) NOT NULL,
    description   TEXT,
    category      VARCHAR(100),
    price         DECIMAL(10,2) NOT NULL,
    mrp           DECIMAL(10,2),
    sku           VARCHAR(100) UNIQUE,
    status        ENUM('ACTIVE','INACTIVE','OUT_OF_STOCK') DEFAULT 'ACTIVE',
    created_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (seller_id) REFERENCES sellers(id)
);

-- -----------------------------------------------
-- 3. INVENTORY
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS inventory (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id          BIGINT NOT NULL UNIQUE,
    total_quantity      INT NOT NULL DEFAULT 0,
    reserved_quantity   INT NOT NULL DEFAULT 0,
    available_quantity  INT GENERATED ALWAYS AS (total_quantity - reserved_quantity) STORED,
    last_updated        DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (product_id) REFERENCES products(id)
);

-- -----------------------------------------------
-- 4. CUSTOMERS
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS customers (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(150) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    phone       VARCHAR(15)  NOT NULL,
    address     TEXT,
    city        VARCHAR(100),
    pincode     VARCHAR(10),
    created_at  DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- -----------------------------------------------
-- 5. ORDERS
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS orders (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number    VARCHAR(50) NOT NULL UNIQUE,
    customer_id     BIGINT NOT NULL,
    status          ENUM('PLACED','CONFIRMED','SHIPPED','DELIVERED','CANCELLED','RETURN_REQUESTED','RETURNED') DEFAULT 'PLACED',
    total_amount    DECIMAL(12,2) NOT NULL,
    shipping_address TEXT,
    placed_at       DATETIME DEFAULT CURRENT_TIMESTAMP,
    shipped_at      DATETIME,
    delivered_at    DATETIME,
    updated_at      DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (customer_id) REFERENCES customers(id)
);

-- -----------------------------------------------
-- 6. ORDER ITEMS
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS order_items (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id    BIGINT NOT NULL,
    product_id  BIGINT NOT NULL,
    seller_id   BIGINT NOT NULL,
    quantity    INT NOT NULL,
    unit_price  DECIMAL(10,2) NOT NULL,
    total_price DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (order_id)   REFERENCES orders(id),
    FOREIGN KEY (product_id) REFERENCES products(id),
    FOREIGN KEY (seller_id)  REFERENCES sellers(id)
);

-- -----------------------------------------------
-- 7. RETURNS
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS returns (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT NOT NULL,
    order_item_id   BIGINT NOT NULL,
    reason          TEXT NOT NULL,
    status          ENUM('REQUESTED','APPROVED','REJECTED','COMPLETED') DEFAULT 'REQUESTED',
    requested_at    DATETIME DEFAULT CURRENT_TIMESTAMP,
    resolved_at     DATETIME,
    FOREIGN KEY (order_id)      REFERENCES orders(id),
    FOREIGN KEY (order_item_id) REFERENCES order_items(id)
);

-- -----------------------------------------------
-- 8. PAYOUTS
-- -----------------------------------------------
CREATE TABLE IF NOT EXISTS payouts (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    seller_id       BIGINT NOT NULL,
    order_id        BIGINT NOT NULL,
    order_item_id   BIGINT NOT NULL,
    amount          DECIMAL(10,2) NOT NULL,
    commission_pct  DECIMAL(5,2)  DEFAULT 10.00,
    commission_amt  DECIMAL(10,2),
    net_payout      DECIMAL(10,2),
    status          ENUM('PENDING','PROCESSED','HOLD') DEFAULT 'PENDING',
    created_at      DATETIME DEFAULT CURRENT_TIMESTAMP,
    processed_at    DATETIME,
    FOREIGN KEY (seller_id)     REFERENCES sellers(id),
    FOREIGN KEY (order_id)      REFERENCES orders(id),
    FOREIGN KEY (order_item_id) REFERENCES order_items(id)
);

-- -----------------------------------------------
-- SAMPLE DATA
-- -----------------------------------------------
INSERT INTO sellers (name, email, phone, gst_number, address, status) VALUES
('TechZone Electronics', 'techzone@seller.com', '9876543210', 'GST001TZ', '123 MG Road, Bengaluru', 'ACTIVE'),
('Fashion Hub', 'fashionhub@seller.com', '9876543211', 'GST002FH', '45 Brigade Road, Bengaluru', 'ACTIVE'),
('HomeGoods Co.', 'homegoods@seller.com', '9876543212', 'GST003HG', '78 Park Street, Chennai', 'ACTIVE');

INSERT INTO products (seller_id, name, description, category, price, mrp, sku, status) VALUES
(1, 'Wireless Bluetooth Headphones', 'Premium noise-cancelling headphones', 'Electronics', 2499.00, 3999.00, 'SKU-ELEC-001', 'ACTIVE'),
(1, 'USB-C Fast Charger 65W', 'GaN technology fast charger', 'Electronics', 899.00, 1499.00, 'SKU-ELEC-002', 'ACTIVE'),
(2, 'Casual Cotton T-Shirt', 'Breathable 100% cotton tee', 'Fashion', 399.00, 699.00, 'SKU-FASH-001', 'ACTIVE'),
(2, 'Slim Fit Jeans', 'Premium stretch denim', 'Fashion', 1299.00, 2199.00, 'SKU-FASH-002', 'ACTIVE'),
(3, 'Stainless Steel Water Bottle', '1L insulated bottle', 'Home', 599.00, 999.00, 'SKU-HOME-001', 'ACTIVE');

INSERT INTO inventory (product_id, total_quantity, reserved_quantity) VALUES
(1, 100, 0),
(2, 200, 0),
(3, 500, 0),
(4, 150, 0),
(5, 300, 0);

INSERT INTO customers (name, email, phone, address, city, pincode) VALUES
('Rahul Sharma', 'rahul@customer.com', '9000000001', '12 Lal Bagh', 'Bengaluru', '560001'),
('Priya Nair', 'priya@customer.com', '9000000002', '34 Anna Nagar', 'Chennai', '600040'),
('Amit Patel', 'amit@customer.com', '9000000003', '56 CG Road', 'Ahmedabad', '380006');
