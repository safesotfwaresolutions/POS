<!-- doc-version: 1.1 | last-updated: 2026-08-20 -->
# Proyecto POS — Contexto General

## Resumen
Aplicación web backend (API REST) para gestión de ventas en efectivo e inventario de un pequeño negocio con una sola sucursal y una sola caja física.

## Stack Tecnológico
- **Backend**: Java 21, Spring Boot, Spring Security, Spring Data JPA
- **Base de Datos**: PostgreSQL 16 (producción), H2 (desarrollo y tests)
- **Autenticación**: JWT (HMAC-SHA256, access token 15 min + refresh token rotativo)
- **Facturación Electrónica**: Factus API (Estándar DIAN Colombia)
- **Build**: Maven

## Roles del Sistema
| Rol | Perfil | Alcance |
|---|---|---|
| Administrador | Dueño del negocio | Control total: usuarios, reportes, configuración, facturación |
| Supervisor | Encargado de tienda | Compras, inventario, catálogo, clientes, reintento de facturas |
| Vendedor | Cajero en mostrador | Ventas, consulta de productos, registro de clientes, consulta de facturas |

## Flujo del Negocio
1. **Abastecimiento**: Supervisor registra compra a proveedor → stock sube automáticamente.
2. **Venta**: Vendedor procesa venta en efectivo → stock baja automáticamente → ticket generado → emisión opcional a Factus (DIAN).
3. **Control**: Administrador consulta reportes de rentabilidad, stock y estado de facturación.

## Exclusiones Explícitas (Fuera de Alcance)
| Excluido | Razón |
|---|---|
| Notas crédito/débito | Requiere contabilidad avanzada |
| Devoluciones | Ventas son finales e irreversibles |
| Múltiples métodos de pago | Solo efectivo |
| Múltiples sucursales/cajas | Una sola tienda |
| Mesas y reservas | No es software de restaurante |
| Despachos/domicilios | Entrega inmediata en mostrador |
| Múltiples monedas | Moneda local única |

## Objetivos
- Procesamiento de ventas ágil con lector de código de barras
- Control de inventario en tiempo real
- Reportes básicos de rentabilidad y stock mínimo
- Trazabilidad: toda transacción asociada a usuario y rol
