<!-- spec-version: 1.0 | last-updated: 2026-08-13 -->
# Módulo: Facturación Electrónica (Billing)

## Dependencias
- **Requiere**: sales, customers, products, settings
- **Requerido por**: sales (consumo desacoplado vía Facade)

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

## Requerimientos Funcionales

### RF-BILL-001 — Emitir Factura Electrónica
- **Prioridad**: Alta
- **Criterios de Aceptación**:
  - Se invoca al confirmar una venta con la bandera `sendToFactus: true` o mediante reintento.
  - Mapea cliente (persona natural/jurídica, consumidor final DIAN `222222222222`), ítems, subtotales, método de pago en efectivo (`10`) y rango de numeración.
  - Almacena el número de factura oficial de Factus, CUFE, código QR y URL pública del PDF.

### RF-BILL-002 — Consultar Factura Electrónica por Venta
- **Prioridad**: Alta
- **Criterios de Aceptación**:
  - Permite consultar el estado actual de la factura electrónica asociada a un `saleId`.
  - Retorna `status`, `cufe`, `qrCode`, `pdfUrl`, `factusNumber` y `errorMessage` si existió fallo.

### RF-BILL-003 — Reintentar Facturación Electrónica
- **Prioridad**: Alta
- **Criterios de Aceptación**:
  - Permite a Supervisores y Administradores reenviar a Factus una factura en estado `ERROR` o `PENDING`.
  - Si la factura ya está `VALIDATED`, la operación es rechazada con HTTP 400.

---

## Requerimientos No Funcionales

### RNF-BILL-001 — Aislamiento Transaccional de I/O
- Las llamadas remotas HTTP hacia la API de Factus deben ejecutarse fuera de la transacción de base de datos principal para evitar retención prolongada de conexiones de base de datos.

---

## API Endpoints

### GET /api/v1/billing/sales/{saleId}
- **Permisos**: Admin, Supervisor, Vendedor
- **Response 200**:
  ```json
  {
    "id": 1,
    "saleId": 105,
    "factusNumber": "SETP-990000123",
    "cufe": "c7a8b9c0d1e2f3...",
    "qrCode": "https://catalogo-vpfe.dian.gov.co/document/searchqr?documentkey=...",
    "status": "VALIDATED",
    "errorMessage": null,
    "pdfUrl": "https://api-sandbox.factus.com.co/v1/bills/SETP-990000123/pdf",
    "validatedAt": "2026-08-13T16:30:00"
  }
  ```
- **Errores**: 404 (venta sin factura electrónica)

### POST /api/v1/billing/sales/{saleId}/retry
- **Permisos**: Admin, Supervisor
- **Response 200**: Objeto `ElectronicInvoiceDto` con el nuevo estado tras el reintento.
- **Errores**: 400 (la factura ya está validada), 404 (venta no encontrada).

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

**Constraints**:
- FK_einvoice_sale → sales(id)

---

## Permisos
| Operación | Admin | Supervisor | Vendedor |
|---|---|---|---|
| Consultar estado | ✓ | ✓ | ✓ |
| Reintentar emisión | ✓ | ✓ | - |
