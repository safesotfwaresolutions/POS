<!-- spec-version: 1.1 | last-updated: 2026-09-03 -->
# Módulo: Facturación Electrónica (Billing)

## Dependencias
- **Requiere**: sales, customers, products, settings
- **Requerido por**: sales (consumo desacoplado vía Facade)

## Estructura Interna Desacoplada

El módulo está internamente segregado por responsabilidades (Feature Slicing & Interface Segregation):
1. **Facturas Electrónicas de Venta**: `InvoiceService` / `BillingInvoiceController` / `InvoiceProvider` / `FactusInvoiceClient`.
2. **Rangos de Numeración DIAN**: `NumberingRangeService` / `BillingNumberingRangeController` / `NumberingRangeProvider` / `FactusNumberingRangeClient`.
3. **Fachada Unificada**: `BillingFacade` / `BillingServiceImpl` (punto de integración único para otros módulos).

---

## Reglas de Negocio

### RN-BILL-001 — Idempotencia de Emisión
- Si una venta ya cuenta con una factura electrónica en estado `VALIDATED`, no se reenvía a Factus ni se duplica la solicitud ante la DIAN.

### RN-BILL-002 — Desacoplamiento y No Bloqueo Local
- El fallo de conexión, timeout o indisponibilidad de la API externa de Factus nunca revierte la transacción de venta registrada localmente en el POS. La factura queda registrada con estado `PENDING` o `ERROR` para su posterior reintento.

### RN-BILL-003 — Ciclo de Estados de Facturación
| Estado | Significado |
|---|---|
| `PENDING` | Registro inicial creado, pendiente de envío o en espera de reintento por fallo de red/auth temporal |
| `VALIDATED` | Factura validada exitosamente por Factus y la DIAN (contiene CUFE, QR y PDF) |
| `REJECTED` | Rechazada por la DIAN por inconsistencias en los datos fiscales (HTTP 400 / 422) |
| `ERROR` | Error imprevisto interno o fallo en la estructura de respuesta del proveedor |

### RN-BILL-004 — Cache y Renovación de Token OAuth2
- El token de acceso OAuth2 se almacena en memoria y se renueva automáticamente con un margen de seguridad de 60 segundos previo a su expiración.

---

## API Endpoints

### 1. Facturas Electrónicas de Ventas (`BillingInvoiceController`)

- `GET /api/v1/billing/sales/{saleId}`: Consultar estado y datos de la factura electrónica generada para una venta.
- `POST /api/v1/billing/sales/{saleId}/retry`: Reintentar facturación electrónica de ventas fallidas (`status = ERROR` o `PENDING`).
- `POST /api/v1/billing/sales/{saleId}/send-email`: Enviar ZIP con PDF y XML legal de la factura al email indicado.

### 2. Rangos de Numeración DIAN (`BillingNumberingRangeController`)

- `GET /api/v1/billing/numbering-ranges/dian`: Consultar en tiempo real ante la DIAN las resoluciones y rangos autorizados.
- `GET /api/v1/billing/numbering-ranges`: Listar rangos de numeración registrados y activos en Factus.
- `GET /api/v1/billing/numbering-ranges/{id}`: Consultar detalle de un rango de numeración.
- `POST /api/v1/billing/numbering-ranges`: Registrar y vincular una resolución DIAN en Factus.
- `DELETE /api/v1/billing/numbering-ranges/{id}`: Eliminar un rango en Factus.
- `PATCH /api/v1/billing/numbering-ranges/{id}/toggle-status`: Alternar estado activo/inactivo del rango.

---

## Modelo de Datos

### Tabla: `electronic_invoices`
| Campo | Tipo | Restricciones |
|---|---|---|
| id | BIGSERIAL | PK, Auto-increment |
| sale_id | BIGINT | UNIQUE, NOT NULL, FK → sales(id) |
| factus_number | VARCHAR(50) | NULLABLE |
| cufe | VARCHAR(255) | NULLABLE |
| qr_code | TEXT | NULLABLE |
| status | VARCHAR(20) | NOT NULL, DEFAULT 'PENDING' |
| error_message | TEXT | NULLABLE |
| pdf_url | VARCHAR(500) | NULLABLE |
| validated_at | TIMESTAMP | NULLABLE |
| created_at | TIMESTAMP | NOT NULL |
| updated_at | TIMESTAMP | NOT NULL |

---

## Permisos
| Operación | Admin | Supervisor | Vendedor |
|---|---|---|---|
| Consultar factura venta | ✓ | ✓ | ✓ |
| Reintentar emisión factura | ✓ | ✓ | - |
| Enviar factura por email | ✓ | ✓ | ✓ |
| Consultar rangos DIAN / Factus | ✓ | ✓ | - |
| Crear / Eliminar / Alternar rangos | ✓ | - | - |
