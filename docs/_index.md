<!-- doc-version: 1.0 | last-updated: 2026-07-08 -->
# Índice del Proyecto POS

## Resolución Rápida por Módulo

| Módulo | Spec | Tablas BD | Endpoint Base |
|---|---|---|---|
| auth | modules/auth/spec.md | users | /api/v1/auth |
| users | modules/users/spec.md | users | /api/v1/users |
| products | modules/products/spec.md | products | /api/v1/products |
| categories | modules/categories/spec.md | categories | /api/v1/categories |
| inventory | modules/inventory/spec.md | inventory_movements | /api/v1/inventory |
| purchases | modules/purchases/spec.md | purchases, purchase_items | /api/v1/purchases |
| sales | modules/sales/spec.md | sales, sale_items | /api/v1/sales |
| customers | modules/customers/spec.md | customers | /api/v1/customers |
| suppliers | modules/suppliers/spec.md | suppliers | /api/v1/suppliers |
| reports | modules/reports/spec.md | (read-only) | /api/v1/reports |
| settings | modules/settings/spec.md | settings | /api/v1/settings |
| billing | modules/billing/spec.md | electronic_invoices | /api/v1/billing |

## Resolución por ID de Requerimiento

- RF-AUTH-* / RN-AUTH-* → modules/auth/spec.md
- RF-USER-* / RN-USER-* → modules/users/spec.md
- RF-PROD-* / RN-PROD-* → modules/products/spec.md
- RF-CAT-* / RN-CAT-* → modules/categories/spec.md
- RF-INV-* / RN-INV-* → modules/inventory/spec.md
- RF-PUR-* / RN-PUR-* → modules/purchases/spec.md
- RF-SALE-* / RN-SALE-* → modules/sales/spec.md
- RF-BILL-* / RN-BILL-* → modules/billing/spec.md
- RF-CUST-* / RN-CUST-* → modules/customers/spec.md
- RF-SUPP-* / RN-SUPP-* → modules/suppliers/spec.md
- RF-REP-* → modules/reports/spec.md
- RF-SET-* / RN-SET-* → modules/settings/spec.md

## Documentos Globales

| Archivo | Cuándo cargarlo |
|---|---|
| _project.md | Primer contacto con el proyecto |
| _architecture.md | Crear módulo nuevo o refactorizar |
| _conventions.md | Escribir código nuevo |
| _security.md | Tocar autenticación o permisos |
| _database-schema.md | Crear tablas nuevas o relaciones |
| _deployment.md | Configurar entorno o desplegar |
| _glossary.md | Término desconocido del dominio |
