<!-- spec-version: 1.0 | last-updated: 2026-07-08 -->
# Módulo: Productos (Products)

## Dependencias
- **Requiere**: categories
- **Requerido por**: inventory, purchases, sales

## Reglas de Negocio

### RN-PROD-001 — Margen de Utilidad Positivo
- `sale_price >= purchase_price` siempre. Enforced a nivel de BD con CHECK constraint.

### RN-PROD-002 — Código de Barras Único
- Cada barcode es único en el sistema (nullable, pero si existe no puede repetirse).

## Requerimientos Funcionales

### RF-PROD-001 — Crear Producto
- **Prioridad**: Alta
- **Criterios de Aceptación**:
  - Campos obligatorios: internalCode (único), name, categoryId, purchasePrice, salePrice.
  - Campos opcionales: barcode (único si ingresado), description, imageUrl.
  - Stock inicial: 0 (se alimenta vía módulo de Compras o Inventario).
  - Validar RN-PROD-001 (precio venta ≥ precio compra).

### RF-PROD-002 — Editar Producto
- **Prioridad**: Alta
- **Criterios de Aceptación**:
  - Editable: name, description, categoryId, purchasePrice, salePrice, barcode, minStock, imageUrl.
  - Validar RN-PROD-001 al cambiar precios.
  - No editar directamente quantityAvailable (se modifica vía Inventario/Compras/Ventas).

### RF-PROD-003 — Consultar y Buscar Productos
- **Prioridad**: Alta
- **Criterios de Aceptación**:
  - Búsqueda por: barcode, internalCode, name.
  - Filtro por: categoryId, active.
  - Paginación: `page` (default 0), `size` (default 20).

### RF-PROD-004 — Activar/Desactivar Producto
- **Prioridad**: Alta
- Producto inactivo no aparece en el POS de ventas.

### RF-PROD-005 — Eliminar Producto (Borrado Lógico)
- **Prioridad**: Media
- Borrado lógico si tiene transacciones históricas.

## API Endpoints

### GET /api/v1/products
- **Permisos**: Admin, Supervisor, Vendedor
- **Query params**: search, categoryId, page, size
- **Response 200**:
  ```json
  {
    "content": [{ "id": 1, "internalCode": "PROD-001", "barcode": "7701234567890", "name": "Coca Cola 350ml", "categoryName": "Bebidas", "purchasePrice": 1.20, "salePrice": 1.80, "quantityAvailable": 45, "minStock": 10, "active": true }],
    "totalPages": 1, "totalElements": 1
  }
  ```

### POST /api/v1/products
- **Permisos**: Admin, Supervisor
- **Request**:
  ```json
  { "internalCode": "PROD-002", "barcode": "7701112223334", "name": "Papas Fritas", "categoryId": 2, "purchasePrice": 0.50, "salePrice": 0.90, "minStock": 20 }
  ```
- **Response 201**: Objeto producto creado
- **Errores**: 400 (código duplicado, RN-PROD-001 violado)

### PUT /api/v1/products/{id}
- **Permisos**: Admin, Supervisor

### PATCH /api/v1/products/{id}/status
- **Permisos**: Admin, Supervisor

### DELETE /api/v1/products/{id}
- **Permisos**: Admin

## Modelo de Datos

### Tabla: `products`
| Campo | Tipo | Restricciones |
|---|---|---|
| id | BIGINT | PK, Auto-increment |
| internal_code | VARCHAR(50) | UNIQUE, NOT NULL |
| barcode | VARCHAR(50) | UNIQUE, NULLABLE |
| name | VARCHAR(100) | NOT NULL |
| description | VARCHAR(255) | NULLABLE |
| category_id | BIGINT | FK → categories(id), NOT NULL |
| purchase_price | DECIMAL(12,2) | NOT NULL, DEFAULT 0.00 |
| sale_price | DECIMAL(12,2) | NOT NULL, DEFAULT 0.00 |
| quantity_available | INT | NOT NULL, DEFAULT 0 |
| min_stock | INT | NOT NULL, DEFAULT 0 |
| active | BOOLEAN | NOT NULL, DEFAULT TRUE |
| image_url | VARCHAR(255) | NULLABLE |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL |

**Constraints**:
- FK_products_category → categories(id)
- CK_product_prices: `sale_price >= purchase_price`
- CK_product_stock: `quantity_available >= 0`

## Permisos
| Operación | Admin | Supervisor | Vendedor |
|---|---|---|---|
| Crear | ✓ | ✓ | - |
| Leer | ✓ | ✓ | ✓ |
| Editar | ✓ | ✓ | - |
| Eliminar | ✓ | - | - |
