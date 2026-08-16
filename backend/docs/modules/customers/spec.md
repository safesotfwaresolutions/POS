<!-- spec-version: 1.0 | last-updated: 2026-07-08 -->
# Módulo: Clientes (Customers)

## Dependencias
- **Requiere**: ninguno
- **Requerido por**: sales

## Reglas de Negocio

### RN-CUST-001 — Cliente por Defecto
- El sistema posee un registro inicial "Cliente General" (identification: "9999999999") que no puede editarse ni eliminarse.

## Requerimientos Funcionales

### RF-CUST-001 — Crear Cliente
- **Prioridad**: Alta
- Campos: fullName, identification (único), email, phone, address.
- Estado inicial: Activo.

### RF-CUST-002 — Editar Cliente
- **Prioridad**: Alta
- No editar "Cliente General" (RN-CUST-001).

### RF-CUST-003 — Consultar Clientes
- **Prioridad**: Alta
- Búsqueda por nombre o identification. Paginación.

### RF-CUST-004 — Eliminar Cliente (Lógico)
- **Prioridad**: Media
- Borrado lógico. No eliminar "Cliente General".

## API Endpoints

### GET /api/v1/customers
- **Permisos**: Admin, Supervisor, Vendedor
- **Query params**: search, page, size

### POST /api/v1/customers
- **Permisos**: Admin, Supervisor, Vendedor
- **Request**: `{ "fullName": "María López", "identification": "1234567890", "email": "maria@mail.com", "phone": "555-0100", "address": "Calle 1" }`
- **Response 201**: Cliente creado
- **Errores**: 400 (identification duplicado)

### PUT /api/v1/customers/{id}
- **Permisos**: Admin, Supervisor, Vendedor

### DELETE /api/v1/customers/{id}
- **Permisos**: Admin

## Modelo de Datos

### Tabla: `customers`
| Campo | Tipo | Restricciones |
|---|---|---|
| id | BIGINT | PK, Auto-increment |
| full_name | VARCHAR(100) | NOT NULL |
| identification | VARCHAR(50) | UNIQUE, NOT NULL |
| email | VARCHAR(100) | NULLABLE |
| phone | VARCHAR(20) | NULLABLE |
| address | VARCHAR(255) | NULLABLE |
| active | BOOLEAN | NOT NULL, DEFAULT TRUE |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL |

## Permisos
| Operación | Admin | Supervisor | Vendedor |
|---|---|---|---|
| Crear | ✓ | ✓ | ✓ |
| Leer | ✓ | ✓ | ✓ |
| Editar | ✓ | ✓ | ✓ |
| Eliminar | ✓ | - | - |
