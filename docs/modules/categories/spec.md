<!-- spec-version: 1.0 | last-updated: 2026-07-08 -->
# Módulo: Categorías (Categories)

## Dependencias
- **Requiere**: ninguno
- **Requerido por**: products

## Reglas de Negocio

### RN-CAT-001 — Unicidad de Nombre (Case Insensitive)
- Los nombres de categoría son únicos sin distinguir mayúsculas/minúsculas.

## Requerimientos Funcionales

### RF-CAT-001 — Crear Categoría
- **Prioridad**: Alta
- **Criterios de Aceptación**:
  - Campos: name (único, case-insensitive), description (opcional).
  - Validar unicidad de nombre.

### RF-CAT-002 — Editar Categoría
- **Prioridad**: Alta
- Editable: name, description. Validar unicidad de nombre.

### RF-CAT-003 — Consultar Categorías
- **Prioridad**: Alta
- Listar todas las categorías con nombre y cantidad de productos asociados.

### RF-CAT-004 — Eliminar Categoría
- **Prioridad**: Media
- No se puede eliminar si tiene productos asociados activos.

## API Endpoints

### GET /api/v1/categories
- **Permisos**: Admin, Supervisor, Vendedor
- **Response 200**: Lista de categorías

### POST /api/v1/categories
- **Permisos**: Admin, Supervisor
- **Request**: `{ "name": "Bebidas", "description": "Bebidas frías y calientes" }`
- **Response 201**: Categoría creada
- **Errores**: 400 (nombre duplicado)

### PUT /api/v1/categories/{id}
- **Permisos**: Admin, Supervisor

### DELETE /api/v1/categories/{id}
- **Permisos**: Admin
- **Errores**: 409 (tiene productos asociados)

## Modelo de Datos

### Tabla: `categories`
| Campo | Tipo | Restricciones |
|---|---|---|
| id | BIGINT | PK, Auto-increment |
| name | VARCHAR(50) | UNIQUE, NOT NULL |
| description | VARCHAR(255) | NULLABLE |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL |

## Permisos
| Operación | Admin | Supervisor | Vendedor |
|---|---|---|---|
| Crear | ✓ | ✓ | - |
| Leer | ✓ | ✓ | ✓ |
| Editar | ✓ | ✓ | - |
| Eliminar | ✓ | - | - |
