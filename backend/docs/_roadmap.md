<!-- doc-version: 1.2 | last-updated: 2026-09-16 -->
# Roadmap y Estado por Módulo

Este documento complementa los `spec.md` de cada módulo (que describen el comportamiento *actual*). Aquí se registra lo que **falta**, la **deuda técnica** conocida y las **próximas ideas**, para que quede como referencia entre sesiones de trabajo. Ver también la skill `pos-doc-sync` para el procedimiento de mantenerlo al día.

## Estado por módulo

| Módulo | Estado actual | Deuda técnica / faltante | Próximas ideas |
|---|---|---|---|
| auth | JWT access+refresh, roles ADMIN/SUPERVISOR/SELLER, self-registration de tiendas (V7) | — | 2FA opcional para ADMIN |
| users | CRUD + seed de superadmin/admin por defecto (`DatabaseSeeder`) | — | Invitación de usuarios por email en vez de creación directa |
| categories | Catálogo global (no por tienda), seed base de 11 categorías genéricas (`CategorySeeder`). Subcategorías por tienda (`product_subcategories`), creadas libremente por Administrator/Supervisor bajo una categoría global, sin pedírselo a la plataforma (2026-09-16) | — | Íconos/colores por categoría para la grilla del POS |
| products | CRUD por tienda (`store_id`), barcode + código interno únicos por tienda, subcategoría opcional por producto, seed demo solo en `dev` (`ProductDemoSeeder`) | Sin variantes de producto (talla/color) — fuera de alcance actual | Carga masiva por CSV/Excel |
| inventory | Movimientos con pessimistic locking en descuento de stock | — | Conteo físico / ajuste de inventario con motivo |
| purchases | Compras a proveedor, sube stock automáticamente | — | Órdenes de compra sugeridas por stock bajo (ver "Valores agregados") |
| sales | Multi-método de pago (`payment_method`, catálogo `PAYMENT_METHODS`), devoluciones (`sale_returns`), multi-tenant | Spec de `sales` corregido en esta sesión (ver `modules/sales/spec.md`) | Venta a crédito/fiado (ver "Valores agregados") |
| customers | CRUD por tienda, cliente genérico sembrado por defecto | — | Historial de compras por cliente en su ficha |
| suppliers | CRUD por tienda | — | — |
| reports | CQRS con `JdbcClient` (SQL nativo, sin JPA) | — | Reportes de rentabilidad por categoría |
| settings | Datos del negocio + catálogos dinámicos de parámetros (`PAYMENT_METHODS`, etc.) | — | Parámetro de impuesto configurable (hoy 19% fijo en frontend, ver hallazgo abajo |
| billing | Adapter Factus/Mock, ciclo de estados PENDING/VALIDATED/REJECTED/ERROR, reintentos | — | Notificación al vendedor cuando una factura pasa a `ERROR` |
| stores | Backoffice SaaS: métricas, ciclo de vida, KYC. Sin `doc-version` header en su spec | Spec no documenta el flujo de self-registration (V7) ni referencia `doc-version`/`last-updated` | — |
| legal | Documentos legales de plataforma | — | — |
| support | Tickets de soporte | — | — |
| notifications | Módulo nuevo (fusionado 2026-09-16): persistencia + alertas en tiempo real para el dashboard | **Sin `spec.md`**, sin entrada en `.ai/init.ps1`/`init.sh` | — |
| storage | Cloudflare R2 (S3-compatible), subida/eliminación de archivos vía `StorageFacade` | **Sin `spec.md`**, sin entrada en `.ai/init.ps1`/`init.sh` | — |

> `notifications` y `storage` llegaron en el merge del 2026-09-16 (rama paralela) junto con el módulo de documentos de tienda, el service de auth/onboarding y la config de Spring Security. Ninguno de los dos tiene spec modular todavía — siguiente candidato para `pos-doc-sync` / `pos-new-module` (parte del checklist es justamente crear ese spec).

## Hallazgos detectados en esta revisión (no corregidos, quedan para decidir)

- **IVA fijo en frontend**: `POSBento.jsx` calcula `tax = subtotal * 0.19` hardcodeado cuando se marca "Transmitir a DIAN", en vez de usar un parámetro configurable de `settings`. Si el negocio no es responsable de IVA (régimen simplificado) esto sobrefactura.
- **`stores/spec.md`** no sigue el formato estándar (`spec-version`/`last-updated`, secciones RN-/RF-) de los demás specs — pendiente de normalizar cuando se vuelva a tocar el módulo.
- **`notifications` y `storage` sin documentación modular** (ver fila en la tabla de arriba) — el harness `.ai/init.ps1`/`init.sh` tampoco los valida, así que "ENTORNO SANO" no detecta el hueco.
- **`.ai/features.json` desactualizado**: sigue en la versión 1.2.0 con FEAT-001 a FEAT-006 (última completada 2026-08-19). No registra como features el trabajo fusionado el 2026-09-03 (notifications, storage/R2 inicial duplicado, auth/onboarding, store documents) ni la config de Spring Security del 2026-09-16 — ese trabajo tampoco tiene entradas correspondientes en `.ai/progress/history.md` (el log salta de 2026-08-27 a 2026-09-16 directo al commit del propio harness). Es una rama paralela que no pasó por el protocolo Leader→Implementer→Reviewer→Committer del arnés.
- **~~`categories/spec.md` permisos~~ — corregido en esta revisión**: el spec decía "Crear/Editar: Admin, Supervisor" pero `CategoryController` ya exige `SUPER_ADMIN` exclusivamente desde FEAT-006 (2026-08-19). Se corrigió el spec para reflejar el código real.
- **`CLAUDE.md` (raíz) desactualizado — corregido en esta revisión**: no mencionaba el rol `SUPER_ADMIN` ni los módulos `notifications`/`storage`. Ya se actualizó.

## Valores agregados propuestos (para retail colombiano)

Priorizados por impacto/esfuerzo estimado, no implementados aún salvo que se indique lo contrario:

1. **Escaneo dinámico en POS** — al escanear un código de barras se agrega directo al carrito sin clicks. *Implementado 2026-09-15* (`POSBento.jsx`).
1b. **Subcategorías de tienda** — cada tienda crea sus propias subcategorías bajo las categorías globales sembradas, sin depender del equipo de plataforma. *Implementado 2026-09-16* (`categories` — `SubcategoryFacade`, migración `V12`, UI en `ProductsBento.jsx`).
2. **Venta a crédito / fiado a clientes** — muy común en tiendas de barrio colombianas: registrar saldo pendiente por cliente y abonos parciales. Requiere nuevo campo en `sales` o módulo de cuentas por cobrar.
3. **Alertas de stock bajo accionables** — hoy el POS muestra el badge de stock bajo, pero no hay notificación proactiva ni sugerencia de orden de compra hacia `purchases`.
4. **Cierre de caja / arqueo diario** — resumen de ventas por método de pago, efectivo esperado vs. contado, al cierre de turno.
5. **Impresión térmica directa** (58mm/80mm) en vez de solo `window.print()` del navegador.
6. **Envío de tiquete por WhatsApp/email** al cliente tras la venta.
7. **Modo offline / PWA** para seguir vendiendo ante caída de internet, sincronizando ventas al reconectar (impacto alto, esfuerzo alto — requiere cola local y resolución de conflictos con el descuento de stock).
8. **Reportes de rentabilidad por categoría** (`reports` ya usa `JdbcClient`, es la extensión natural).
9. **Carga masiva de productos** por CSV/Excel para catálogos grandes al hacer onboarding de una tienda nueva.

## Plan de trabajo

- **Fase 0 — Tooling y documentación (esta sesión)**: skills `pos-new-module`/`pos-doc-sync`, corrección de `_project.md` y `sales/spec.md`, creación de este roadmap.
- **Fase 1 — Quick wins (esta sesión)**: `CategorySeeder` + `ProductDemoSeeder`, escaneo dinámico en el POS.
- **Fase 2 — Deuda técnica de documentación**: normalizar `stores/spec.md` al formato estándar; decidir y documentar el hallazgo de IVA fijo.
- **Fase 3 — Valores agregados**: abordar en orden de la lista de arriba, empezando por cierre de caja y alertas de stock bajo (impacto alto, esfuerzo moderado) antes de offline/PWA (impacto alto, esfuerzo alto).
