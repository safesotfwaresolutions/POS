<!-- spec-version: 1.0 | last-updated: 2026-07-08 -->
# Módulo: Proveedores (Suppliers)

## Dependencias
- **Requiere**: ninguno
- **Requerido por**: purchases

## Reglas de Negocio

### RN-SUPP-001 — Proveedor Activo para Compras
- Solo se pueden asociar compras a proveedores con estado Activo.

## Requerimientos Funcionales

### RF-SUPP-001 — Crear Proveedor
- **Prioridad**: Alta
- Campos: companyName, taxId (único), contactName, email, phone, address.

### RF-SUPP-002 — Editar Proveedor
- **Prioridad**: Alta

### RF-SUPP-003 — Consultar Proveedores
- **Prioridad**: Alta
- Búsqueda por nombre o taxId. Paginación.

### RF-SUPP-004 — Eliminar Proveedor (Lógico)
- **Prioridad**: Media
- Borrado lógico si tiene compras asociadas.

## API Endpoints

### GET /api/v1/suppliers
- **Permisos**: Admin, Supervisor

### POST /api/v1/suppliers
- **Permisos**: Admin, Supervisor
- **Request**: `{ "companyName": "Distribuidora ABC", "taxId": "900123456-1", "contactName": "Pedro", "phone": "555-0200" }`
- **Response 201**: Proveedor creado

### PUT /api/v1/suppliers/{id}
- **Permisos**: Admin, Supervisor

### DELETE /api/v1/suppliers/{id}
- **Permisos**: Admin

## Modelo de Datos

### Tabla: `suppliers`
| Campo | Tipo | Restricciones |
|---|---|---|
| id | BIGINT | PK, Auto-increment |
| company_name | VARCHAR(100) | NOT NULL |
| tax_id | VARCHAR(50) | UNIQUE, NOT NULL |
| contact_name | VARCHAR(100) | NULLABLE |
| email | VARCHAR(100) | NULLABLE |
| phone | VARCHAR(20) | NULLABLE |
| address | VARCHAR(255) | NULLABLE |
| active | BOOLEAN | NOT NULL, DEFAULT TRUE |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL |

## Permisos
| Operación | Admin | Supervisor | Vendedor |
|---|---|---|---|
| Crear | ✓ | ✓ | - |
| Leer | ✓ | ✓ | - |
| Editar | ✓ | ✓ | - |
| Eliminar | ✓ | - | - |
