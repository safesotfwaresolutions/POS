<!-- doc-version: 1.0 | last-updated: 2026-07-08 -->
# Esquema de Base de Datos — Vista Global

## Diagrama Entidad-Relación

```mermaid
erDiagram
    USERS ||--o{ PURCHASES : registra
    USERS ||--o{ SALES : registra
    USERS ||--o{ INVENTORY_MOVEMENTS : audita
    CATEGORIES ||--o{ PRODUCTS : clasifica
    PRODUCTS ||--o{ PURCHASE_ITEMS : contiene
    PRODUCTS ||--o{ SALE_ITEMS : contiene
    PRODUCTS ||--o{ INVENTORY_MOVEMENTS : registra
    CUSTOMERS ||--o{ SALES : asocia
    SUPPLIERS ||--o{ PURCHASES : vende
    PURCHASES ||--o{ PURCHASE_ITEMS : desglosa
    SALES ||--o{ SALE_ITEMS : desglosa
```

## Convenciones
- Motor: PostgreSQL 16 (prod), H2 (dev)
- Normalización: 3FN
- PKs: `id BIGINT AUTO_INCREMENT` (excepto `settings` que usa `id INT = 1`)
- Timestamps: `created_at`, `updated_at` en todas las tablas maestras
- Borrado lógico: campo `active BOOLEAN DEFAULT TRUE`
- Tipos monetarios: `DECIMAL(12,2)`
- Nombres de tablas: snake_case, plural
- FKs: `FK_<tabla>_<referencia>` (ej. `FK_products_category`)
- Índices: `IDX_<tabla>_<campo>` (ej. `IDX_products_barcode`)

## Índices de Rendimiento
| Índice | Tabla.Campo | Propósito |
|---|---|---|
| IDX_products_barcode | products(barcode) | Búsqueda POS con lector |
| IDX_products_internal_code | products(internal_code) | Búsqueda interna |
| IDX_sales_created_at | sales(created_at) | Historial y reportes diarios |
| IDX_movements_product_date | inventory_movements(product_id, created_at) | Kardex de producto |
| IDX_customers_identification | customers(identification) | Asociar cliente en POS |

## Migraciones de Base de Datos (Flyway)

Toda modificación al esquema físico de la base de datos (creación de tablas, alteración de columnas, inserción de datos semilla, creación de índices) **DEBE** gestionarse mediante scripts de migración de Flyway.

- **Ubicación obligatoria**: `src/main/resources/db/migration/`
- **Nomenclatura**: `V<Versión>__<descripción_breve_snake_case>.sql`
  - *Ejemplo*: `V1__init_schema.sql` (creación de tablas base).
  - *Ejemplo*: `V2__add_idx_products_barcode.sql` (creación de índice).
- **Inmutabilidad**: Una vez que una migración ha sido mezclada en la rama principal y ejecutada, **NUNCA** debe ser modificada. Cualquier corrección debe realizarse en una nueva versión de migración (ej. `V3__...`).
- **Validación**: Hibernate está configurado con `ddl-auto=validate`, por lo que cualquier discrepancia entre las entidades JPA de Java y el esquema migrado por Flyway provocará un fallo de compilación/arranque de la aplicación.

Nota: El DDL detallado de cada tabla está en el `spec.md` del módulo correspondiente.

