-- V2__Factus_Integration.sql

-- 1. Extensión de la tabla settings para credenciales Factus
ALTER TABLE settings ADD COLUMN IF NOT EXISTS factus_client_id VARCHAR(255);
ALTER TABLE settings ADD COLUMN IF NOT EXISTS factus_client_secret VARCHAR(255);
ALTER TABLE settings ADD COLUMN IF NOT EXISTS factus_username VARCHAR(255);
ALTER TABLE settings ADD COLUMN IF NOT EXISTS factus_password VARCHAR(255);
ALTER TABLE settings ADD COLUMN IF NOT EXISTS factus_numbering_range_id INT;
ALTER TABLE settings ADD COLUMN IF NOT EXISTS factus_enabled BOOLEAN NOT NULL DEFAULT FALSE;

-- 2. Campos requeridos por DIAN para Clientes
ALTER TABLE customers ADD COLUMN IF NOT EXISTS dv VARCHAR(1);
ALTER TABLE customers ADD COLUMN IF NOT EXISTS legal_organization_id INT DEFAULT 13; -- 13 = Persona Natural, 1 = Jurídica
ALTER TABLE customers ADD COLUMN IF NOT EXISTS tribute_id INT DEFAULT 21;             -- 21 = Consumidor Final / No Responsable
ALTER TABLE customers ADD COLUMN IF NOT EXISTS municipality_id INT;

-- 3. Campos de Impuestos para Productos
ALTER TABLE products ADD COLUMN IF NOT EXISTS tax_rate DECIMAL(5,2) NOT NULL DEFAULT 0.00;
ALTER TABLE products ADD COLUMN IF NOT EXISTS unspsc_code VARCHAR(50);

-- 4. Tabla de trazabilidad de Facturación Electrónica
CREATE TABLE IF NOT EXISTS electronic_invoices (
    id BIGSERIAL PRIMARY KEY,
    sale_id BIGINT NOT NULL UNIQUE,
    factus_number VARCHAR(50),
    cufe VARCHAR(255),
    qr_code TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING', -- PENDING, VALIDATED, REJECTED, ERROR
    error_message TEXT,
    pdf_url VARCHAR(500),
    validated_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT FK_einvoice_sale FOREIGN KEY (sale_id) REFERENCES sales(id)
);
