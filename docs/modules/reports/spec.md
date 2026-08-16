<!-- spec-version: 1.0 | last-updated: 2026-07-08 -->
# Módulo: Reportes (Reports)

## Dependencias
- **Requiere (read-only)**: sales, purchases, inventory, products
- **Requerido por**: ninguno

## Reglas de Negocio
Este módulo no define reglas de negocio propias. Solo consulta datos de otros módulos.

## Requerimientos Funcionales

### RF-REP-001 — Reporte de Ventas por Período
- **Prioridad**: Alta
- Rango de fechas, total vendido, cantidad de transacciones, desglose por vendedor.

### RF-REP-002 — Reporte de Productos Más Vendidos
- **Prioridad**: Media
- Top N productos por cantidad vendida en un período.

### RF-REP-003 — Reporte de Inventario Actual (Stock)
- **Prioridad**: Alta
- Listado de productos con stock actual, stock mínimo, estado de alerta.
- Productos con stock ≤ minStock se marcan como "Bajo Stock".

### RF-REP-004 — Reporte de Rentabilidad
- **Prioridad**: Media
- Margen de utilidad por producto: `(salePrice - purchasePrice) × cantidadVendida`.

### RF-REP-005 — Reporte de Compras por Período
- **Prioridad**: Media
- Total invertido en compras, desglose por proveedor.

## API Endpoints

### GET /api/v1/reports/sales
- **Permisos**: Admin
- **Query params**: dateFrom, dateTo

### GET /api/v1/reports/top-products
- **Permisos**: Admin
- **Query params**: dateFrom, dateTo, limit (default 10)

### GET /api/v1/reports/stock
- **Permisos**: Admin, Supervisor
- **Query params**: belowMinStock (boolean)

### GET /api/v1/reports/profitability
- **Permisos**: Admin
- **Query params**: dateFrom, dateTo

### GET /api/v1/reports/purchases
- **Permisos**: Admin
- **Query params**: dateFrom, dateTo

## Modelo de Datos
Este módulo no posee tablas propias. Ejecuta queries de solo lectura sobre las tablas de sales, sale_items, purchases, purchase_items, products e inventory_movements.

## Permisos
| Operación | Admin | Supervisor | Vendedor |
|---|---|---|---|
| Ver reportes | ✓ (todos) | ✓ (solo stock) | - |
