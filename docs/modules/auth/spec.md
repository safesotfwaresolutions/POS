<!-- spec-version: 1.0 | last-updated: 2026-07-08 -->
# Módulo: Autenticación (Auth)

## Dependencias
- **Requiere**: users
- **Requerido por**: todos los módulos (JWT es prerrequisito)

## Reglas de Negocio

### RN-AUTH-001 — Credenciales Cifradas
- Las contraseñas se almacenan con BCrypt (factor de costo ≥ 10). Nunca en texto plano.

### RN-AUTH-002 — Bloqueo por Intentos Fallidos
- 5 intentos fallidos consecutivos en 15 minutos → cuenta bloqueada temporalmente (30 min o intervención de Admin).

## Requerimientos Funcionales

### RF-AUTH-001 — Inicio de Sesión (Login)
- **Prioridad**: Alta (Bloqueante)
- **Criterios de Aceptación**:
  - Credenciales válidas → genera JWT y emite Cookie `HttpOnly` segura (`SameSite=Strict`, `Secure`, `Path=/`, `Max-Age=28800`) junto con respuesta JSON de datos de sesión.
  - Credenciales inválidas → `401 Unauthorized`: "Usuario o contraseña incorrectos" (sin pistas).
  - Cuenta inactiva → `403 Forbidden`: "Cuenta de usuario desactivada".

### RF-AUTH-002 — Cierre de Sesión (Logout)
- **Prioridad**: Alta
- **Criterios de Aceptación**:
  - Eliminar cookie `HttpOnly` del cliente enviando `Set-Cookie: jwt_token=; Max-Age=0; ...`
  - Limpiar metadatos cosméticos del cliente (`localStorage`/`sessionStorage`).
  - Peticiones posteriores sin token → denegar acceso (`401 Unauthorized` / `403 Forbidden`).

## Requerimientos No Funcionales

### RNF-AUTH-001 — HTTPS Obligatorio
- Toda transacción de autenticación debe realizarse sobre HTTPS.

### RNF-AUTH-002 — Tiempo de Expiración del Token
- JWT expira a las 8 horas desde emisión (configurable via `JWT_EXPIRATION_MS`).

## API Endpoints

### POST /api/v1/auth/login
- **Permisos**: Público (sin token)
- **Request**:
  ```json
  { "username": "admin", "password": "Password123" }
  ```
- **Response 200**:
  - **Header**: `Set-Cookie: jwt_token=eyJ...; Path=/; Max-Age=28800; HttpOnly; SameSite=Strict`
  - **Body**:
  ```json
  { "token": "eyJ...", "username": "admin", "role": "ADMINISTRATOR", "expiresIn": 28800 }
  ```
- **Errores**: 401, 403

### POST /api/v1/auth/logout
- **Permisos**: Cualquier rol autenticado
- **Request**: Vacío
- **Response**: `204 No Content`
  - **Header**: `Set-Cookie: jwt_token=; Path=/; Max-Age=0; HttpOnly; SameSite=Strict`

## Modelo de Datos
Este módulo no posee tablas propias. Utiliza la tabla `users` del módulo users.

## Permisos
| Operación | Admin | Supervisor | Vendedor |
|---|---|---|---|
| Login/Logout | ✓ | ✓ | ✓ |
