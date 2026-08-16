<!-- spec-version: 1.0 | last-updated: 2026-07-08 -->
# Módulo: Configuración (Settings)

## Dependencias
- **Requiere**: ninguno
- **Requerido por**: sales (datos de factura: nombre, dirección, teléfono del negocio)

## Reglas de Negocio

### RN-SET-001 — Única Configuración Global
- Solo existe un registro de configuración (`id = 1`). No hay soporte para múltiples sucursales.

## Requerimientos Funcionales

### RF-SET-001 — Configurar Datos del Negocio
- **Prioridad**: Alta
- Campos: businessName, address, phone, taxId, email, logoUrl.
- Solo lectura/escritura, no se crea ni elimina (registro único).

### RF-SET-002 — Consultar Configuración
- **Prioridad**: Alta
- Retorna la configuración actual del negocio.

## API Endpoints

### GET /api/v1/settings
- **Permisos**: Admin, Supervisor
- **Response 200**:
  ```json
  { "businessName": "Mi Tienda", "address": "Calle Principal 123", "phone": "555-0001", "taxId": "900111222-3", "email": "info@mitienda.com", "logoUrl": null }
  ```

### PUT /api/v1/settings
- **Permisos**: Admin
- **Request**: Mismo esquema del GET
- **Response 200**: Configuración actualizada

## Modelo de Datos

### Tabla: `settings`
| Campo | Tipo | Restricciones |
|---|---|---|
| id | INT | PK, forzado a 1 |
| business_name | VARCHAR(100) | NOT NULL |
| address | VARCHAR(255) | NOT NULL |
| phone | VARCHAR(20) | NOT NULL |
| tax_id | VARCHAR(50) | NULLABLE |
| email | VARCHAR(100) | NULLABLE |
| logo_url | VARCHAR(255) | NULLABLE |
| updated_at | TIMESTAMP | NOT NULL |

## Permisos
| Operación | Admin | Supervisor | Vendedor |
|---|---|---|---|
| Leer | ✓ | ✓ | - |
| Editar | ✓ | - | - |
