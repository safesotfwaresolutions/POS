# Módulo: Stores (Locales / Comercios SaaS)

## Bounded Context
Gestión centralizada de los comercios/locales que utilizan la plataforma POS como clientes SaaS. Permite al equipo de administración (SUPER_ADMIN) monitorear métricas globales, filtrar y buscar locales, gestionar su ciclo de vida y verificar documentos legales/KYC.

## Roles y Permisos
- `SUPER_ADMIN`: Control total (Lectura, Creación, Actualización de estado, Verificación de correo y Revisión de documentos KYC).

## Reglas de Negocio
- **RN-STORE-001 (Unicidad de correo)**: Cada local debe tener un correo electrónico único en la plataforma.
- **RN-STORE-002 (Estados válidos)**: Un local transiciona entre `PENDING_VERIFICATION`, `ACTIVE`, `INACTIVE` y `SUSPENDED`.
- **RN-STORE-003 (Documentos KYC)**: Tipos de documento soportados: `RUT`, `COMMERCE_CHAMBER`, `ID_CARD`, `BANK_CERTIFICATE`, `OTHER`. La revisión requiere motivo obligatorio si es rechazado.

## Endpoints
- `GET /api/v1/backoffice/metrics`: Métricas SaaS consolidadas.
- `GET /api/v1/backoffice/stores`: Búsqueda y listado paginado con filtros por estado y texto.
- `GET /api/v1/backoffice/stores/{id}`: Detalle de local.
- `POST /api/v1/backoffice/stores`: Creación de local.
- `PUT /api/v1/backoffice/stores/{id}`: Actualización de datos.
- `PATCH /api/v1/backoffice/stores/{id}/status`: Cambio de estado.
- `PATCH /api/v1/backoffice/stores/{id}/verify-email`: Verificación de correo.
- `GET /api/v1/backoffice/stores/{id}/documents`: Listado de documentos KYC.
- `PATCH /api/v1/backoffice/stores/{id}/documents/{docId}`: Aprobación/Rechazo de documento.
- `GET /api/v1/backoffice/store-categories`: Listado de categorías de comercios.
- `POST /api/v1/backoffice/store-categories`: Creación de categoría.
- `PUT /api/v1/backoffice/store-categories/{id}`: Edición de categoría.
- `DELETE /api/v1/backoffice/store-categories/{id}`: Desactivación de categoría.