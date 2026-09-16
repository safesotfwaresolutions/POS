<!-- spec-version: 1.1 | last-updated: 2026-09-16 -->
# Módulo: Categorías (Categories)

## Dependencias
- **Requiere**: ninguno
- **Requerido por**: products

## Reglas de Negocio

### RN-CAT-001 — Unicidad de Nombre (Case Insensitive)
- Los nombres de categoría son únicos sin distinguir mayúsculas/minúsculas.

### RN-CAT-002 — Escritura Exclusiva de Plataforma
- Las categorías son un catálogo **global** (compartido por todas las tiendas, sin `store_id`, ver FEAT-006). Por eso crear, editar y eliminar está restringido exclusivamente al rol `SUPER_ADMIN`; ningún rol de tienda (Administrator/Supervisor/Seller) puede escribir el catálogo, solo leerlo.

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
- **Permisos**: SUPER_ADMIN, Admin, Supervisor, Vendedor (lectura abierta a cualquier rol autenticado)
- **Response 200**: Lista de categorías

### POST /api/v1/categories
- **Permisos**: SUPER_ADMIN exclusivamente
- **Request**: `{ "name": "Bebidas", "description": "Bebidas frías y calientes" }`
- **Response 201**: Categoría creada
- **Errores**: 400 (nombre duplicado), 403 (rol distinto de SUPER_ADMIN)

### PUT /api/v1/categories/{id}
- **Permisos**: SUPER_ADMIN exclusivamente

### DELETE /api/v1/categories/{id}
- **Permisos**: SUPER_ADMIN exclusivamente
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
| Operación | SUPER_ADMIN | Admin | Supervisor | Vendedor |
|---|---|---|---|---|
| Crear | ✓ | - | - | - |
| Leer | ✓ | ✓ | ✓ | ✓ |
| Editar | ✓ | - | - | - |
| Eliminar | ✓ | - | - | - |
