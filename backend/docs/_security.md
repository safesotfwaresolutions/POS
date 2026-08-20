<!-- doc-version: 1.3 | last-updated: 2026-08-20 -->
# Seguridad del Sistema

## Autenticación (JWT + Refresh Token)
- **Access token**: JWT firmado con HMAC-SHA256, stateless, de vida corta (`security.jwt.expiration-ms`, 15 minutos por defecto).
- **Refresh token**: valor opaco aleatorio (Base64URL, 48 bytes) de vida larga (`security.jwt.refresh-expiration-ms`, 7 días por defecto). Se persiste **solo como hash SHA-256** en la tabla `refresh_tokens` (referenciada a `users` por `user_id`), por lo que es revocable y una fuga de la tabla no permite reconstruir tokens válidos.
- **Rotación (single-use)**: cada llamada a `POST /api/v1/auth/refresh` revoca el refresh token usado (con bloqueo pesimista para serializar rotaciones concurrentes) y emite uno nuevo dentro de la misma **familia** (`family_id`).
- **Detección de reuso (RFC 6819)**: si se presenta un refresh token ya rotado (revocado), se asume robo y se **revoca la familia completa** (toda la sesión), no solo ese token. Un usuario desactivado durante una sesión activa también provoca la revocación de su familia en el siguiente `refresh`.
- **Ciclo de vida**: en cada login se purgan los tokens caducados/revocados del usuario para acotar el crecimiento de la tabla.
- **Endpoints públicos**:
  - `POST /api/v1/auth/login` — emite access + refresh token (body y cookies `HttpOnly`).
  - `POST /api/v1/auth/refresh` — renueva el access token a partir del refresh token (cookie `HttpOnly` o campo `refreshToken` en el body).
  - `GET /api/v1/legal/{slug}` (lectura pública de políticas y términos)
  - `POST /api/v1/support/tickets` (radicación pública de PQRs y bugs)
  - `GET /api/v1/support/tickets/track/{ticketNumber}` (tracking público)
- **Cierre de sesión**: `POST /api/v1/auth/logout` revoca el refresh token asociado y limpia ambas cookies.
- **Cookies**: `jwt_token` (path `/`) para el access token y `refresh_token` (path `/api/v1/auth`, alcance restringido a los endpoints de sesión), ambas `HttpOnly`.
- **Payload JWT**: `sub` (username), `role` (SUPER_ADMIN, ADMINISTRATOR, SUPERVISOR, SELLER), `storeId` (Long, null para SUPER_ADMIN), `iat`, `exp`

## Roles del Sistema (RBAC)
- **SUPER_ADMIN**: Equipo interno de la plataforma SaaS. Control total sobre Back Office (`/api/v1/backoffice/**`) y gestión exclusiva de categorías (`/api/v1/categories` POST, PUT, DELETE). No pertenece a ningún local (`store_id = null`).
- **ADMINISTRATOR**: Administrador/Dueño de un local específico.
- **SUPERVISOR**: Encargado de tienda de un local específico.
- **SELLER**: Vendedor / cajero de un local específico.

### Matriz de Permisos
| Módulo | SUPER_ADMIN | Admin Local | Supervisor | Vendedor | Público |
|---|---|---|---|---|---|
| Back Office (Locales / Métricas) | CRUD | - | - | - | - |
| Categorías (Globales) | CRUD | R | R | R | - |
| Textos Legales | CRUD / Publicar | R | R | R | R (Vigente) |
| Soporte / PQRs / Bugs | CRUD / Resolver | R (Propios) | - | - | Crear / R (Radicado) |
| Usuarios Local | CRUD | CRUD (Local) | - | - | - |
| Productos Local | R | CRUD | CRU | R | - |
| Ventas Local | R | CRUD | CRUD | CRU | - |