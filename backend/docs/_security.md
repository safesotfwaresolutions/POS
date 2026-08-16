<!-- doc-version: 1.0 | last-updated: 2026-07-08 -->
# Seguridad del Sistema

## Autenticación (JWT)
- **Tipo**: Stateless, token JWT firmado con HMAC-SHA256
- **Endpoint público**: `POST /api/v1/auth/login` (único sin token)
- **Transporte / Mecanismo dual**:
  - **Cookies Web**: `Set-Cookie: jwt_token=<token>; HttpOnly; Secure; SameSite=Strict; Path=/; Max-Age=28800` (Protección máxima XSS y mitigación CSRF)
  - **Encabezado HTTP**: `Authorization: Bearer <token>` (Compatibilidad con Swagger UI, Postman y clientes REST externos)
- **Payload JWT**: `sub` (username), `role` (ROLE_*), `iat`, `exp`
- **Expiración**: 8 horas desde emisión (configurable via `JWT_EXPIRATION_MS`)
- **Almacenamiento cliente**: 
  - Token seguro en Cookie `HttpOnly` (inaccesible por JavaScript).
  - `localStorage` / `sessionStorage` reservado únicamente para metadatos cosméticos de interfaz (`username`, `role`), nunca secretos criptográficos.

## Contraseñas (BCrypt)
- Algoritmo: `BCryptPasswordEncoder`
- Factor de costo: 10
- Nunca almacenar en texto plano
- Validación: `passwordEncoder.matches(raw, encoded)`

## Autorización (RBAC)
- Roles estáticos: `ADMINISTRATOR`, `SUPERVISOR`, `SELLER`
- Todas las rutas `/api/v1/**` requieren autenticación excepto `/api/v1/auth/login`
- Anotación `@PreAuthorize("hasRole('...')")` en cada método de Controller

### Matriz de Permisos
| Módulo | Admin | Supervisor | Vendedor |
|---|---|---|---|
| Usuarios | CRUD | - | - |
| Productos | CRUD | CRU | R |
| Categorías | CRUD | CRU | R |
| Inventario | CRUD | CRU | R |
| Compras | CRUD | CRU | - |
| Ventas | CRUD | CRUD | CRU |
| Clientes | CRUD | CRU | CRU |
| Proveedores | CRUD | CRU | - |
| Reportes | R | R (solo stock) | - |
| Configuración | CRU | R | - |

## Protecciones
| Amenaza | Mitigación |
|---|---|
| SQL Injection | Prepared Statements (JPA/Hibernate) |
| CORS | Whitelist de orígenes + `allowCredentials(true)` controlado |
| XSS | Token blindado en Cookie `HttpOnly` + `textContent` en UI |
| CSRF | Atributo `SameSite=Strict` en Cookies + Header check |
| Fuerza bruta | Bloqueo tras 5 intentos fallidos (15 min) |
| Data leaks | Validación exhaustiva de payloads en backend |
