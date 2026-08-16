-- V1__Initial_Schema.sql
-- Initial schema definition for app.pos project

-- 1. Table: users
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- 2. Table: categories
CREATE TABLE IF NOT EXISTS categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- 3. Table: products
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    internal_code VARCHAR(50) NOT NULL UNIQUE,
    barcode VARCHAR(50) UNIQUE,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(255),
    category_id BIGINT NOT NULL,
    purchase_price DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    sale_price DECIMAL(12,2) NOT NULL DEFAULT 0.00,
    quantity_available INT NOT NULL DEFAULT 0,
    min_stock INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    image_url VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT FK_products_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT CK_product_prices CHECK (sale_price >= purchase_price),
    CONSTRAINT CK_product_stock CHECK (quantity_available >= 0)
);

-- 4. Table: customers
CREATE TABLE IF NOT EXISTS customers (
    id BIGSERIAL PRIMARY KEY,
    full_name VARCHAR(100) NOT NULL,
    identification VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100),
    phone VARCHAR(20),
    address VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- 5. Table: suppliers
CREATE TABLE IF NOT EXISTS suppliers (
    id BIGSERIAL PRIMARY KEY,
    company_name VARCHAR(100) NOT NULL,
    tax_id VARCHAR(50) NOT NULL UNIQUE,
    contact_name VARCHAR(100),
    email VARCHAR(100),
    phone VARCHAR(20),
    address VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- 6. Table: inventory_movements
CREATE TABLE IF NOT EXISTS inventory_movements (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    movement_type VARCHAR(20) NOT NULL,
    quantity INT NOT NULL,
    previous_stock INT NOT NULL,
    new_stock INT NOT NULL,
    reason VARCHAR(255),
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT FK_movements_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT FK_movements_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 7. Table: purchases
CREATE TABLE IF NOT EXISTS purchases (
    id BIGSERIAL PRIMARY KEY,
    supplier_id BIGINT NOT NULL,
    invoice_number VARCHAR(50),
    total_amount DECIMAL(12,2) NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT FK_purchases_supplier FOREIGN KEY (supplier_id) REFERENCES suppliers(id),
    CONSTRAINT FK_purchases_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 8. Table: purchase_items
CREATE TABLE IF NOT EXISTS purchase_items (
    id BIGSERIAL PRIMARY KEY,
    purchase_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_cost DECIMAL(12,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    CONSTRAINT FK_purchase_items_purchase FOREIGN KEY (purchase_id) REFERENCES purchases(id) ON DELETE CASCADE,
    CONSTRAINT FK_purchase_items_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- 9. Table: sales
CREATE TABLE IF NOT EXISTS sales (
    id BIGSERIAL PRIMARY KEY,
    invoice_number VARCHAR(50) NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL,
    cash_received DECIMAL(12,2) NOT NULL,
    cash_change DECIMAL(12,2) NOT NULL,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT FK_sales_customer FOREIGN KEY (customer_id) REFERENCES customers(id),
    CONSTRAINT FK_sales_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT CK_sale_payment CHECK (cash_received >= total_amount)
);

-- 10. Table: sale_items
CREATE TABLE IF NOT EXISTS sale_items (
    id BIGSERIAL PRIMARY KEY,
    sale_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    CONSTRAINT FK_sale_items_sale FOREIGN KEY (sale_id) REFERENCES sales(id) ON DELETE CASCADE,
    CONSTRAINT FK_sale_items_product FOREIGN KEY (product_id) REFERENCES products(id)
);

-- 11. Table: settings
CREATE TABLE IF NOT EXISTS settings (
    id INT PRIMARY KEY,
    business_name VARCHAR(100) NOT NULL,
    address VARCHAR(255) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    tax_id VARCHAR(50),
    email VARCHAR(100),
    logo_url VARCHAR(255),
    updated_at TIMESTAMP NOT NULL
);

-- Sequence: invoice_seq for sequential invoice generation
CREATE SEQUENCE IF NOT EXISTS invoice_seq START WITH 1;
