<!-- spec-version: 1.0 | last-updated: 2026-07-08 -->
# Módulo: Inventario (Inventory)

## Dependencias
- **Requiere**: products, users
- **Requerido por**: sales, purchases, reports

## Reglas de Negocio

### RN-INV-001 — Prohibición de Inventario Negativo
- `quantity_available >= 0` siempre. Si una operación resulta en stock negativo → bloquear transacción.

### RN-INV-002 — Actualización Automática de Inventario
- Compra confirmada → stock sube. Venta completada → stock baja. Siempre atómico.

## Requerimientos Funcionales

### RF-INV-001 — Registrar Entrada de Inventario (Manual)
- **Prioridad**: Alta
- **Criterios de Aceptación**:
  - Campos: productId, quantity (> 0), reason.
  - Tipo de movimiento: ENTRADA.
  - Stock nuevo = stock actual + quantity.
  - Registrar movimiento con previous_stock y new_stock.

### RF-INV-002 — Registrar Salida de Inventario (Manual)
- **Prioridad**: Alta
- **Criterios de Aceptación**:
  - Campos: productId, quantity (> 0), reason.
  - Tipo de movimiento: SALIDA.
  - Validar RN-INV-001: stock resultante ≥ 0.
  - Registrar movimiento con previous_stock y new_stock.

### RF-INV-003 — Registrar Ajuste de Inventario
- **Prioridad**: Media
- Ajuste manual tras conteo físico. Tipo: AJUSTE.

### RF-INV-004 — Consultar Historial de Movimientos (Kardex)
- **Prioridad**: Alta
- Filtros: productId, movementType, rango de fechas.
- Mostrar: producto, tipo, cantidad, stock anterior, stock nuevo, usuario, fecha.

## API Endpoints

### POST /api/v1/inventory/movements
- **Permisos**: Admin, Supervisor
- **Request**:
  ```json
  { "productId": 1, "movementType": "SALIDA", "quantity": 3, "reason": "Merma por vencimiento" }
  ```
- **Response 201**:
  ```json
  { "id": 1500, "productName": "Coca Cola 350ml", "movementType": "SALIDA", "quantity": 3, "previousStock": 45, "newStock": 42, "reason": "Merma por vencimiento", "user": "supervisor1", "createdAt": "2026-07-07T10:49:00Z" }
  ```
- **Errores**: 400 (cantidad inválida, stock negativo resultante)

### GET /api/v1/inventory/movements
- **Permisos**: Admin, Supervisor, Vendedor (solo lectura)
- **Query params**: productId, movementType, dateFrom, dateTo, page, size

## Modelo de Datos

### Tabla: `inventory_movements`
| Campo | Tipo | Restricciones |
|---|---|---|
| id | BIGINT | PK, Auto-increment |
| product_id | BIGINT | FK → products(id), NOT NULL |
| movement_type | VARCHAR(20) | NOT NULL. Valores: ENTRADA, SALIDA, AJUSTE, COMPRA, VENTA |
| quantity | INT | NOT NULL (siempre positivo) |
| previous_stock | INT | NOT NULL |
| new_stock | INT | NOT NULL |
| reason | VARCHAR(255) | NULLABLE |
| user_id | BIGINT | FK → users(id), NOT NULL |
| created_at | TIMESTAMP | NOT NULL |

**Constraints**: FK_movements_product, FK_movements_user

## Permisos
| Operación | Admin | Supervisor | Vendedor |
|---|---|---|---|
| Crear movimiento | ✓ | ✓ | - |
| Leer historial | ✓ | ✓ | ✓ |
