# 🛒 POS System Backend — API REST Monolito Modular

Sistema backend integral para Punto de Venta (POS), control de inventario en tiempo real y facturación electrónica (DIAN Colombia) para comercios minoristas.

Desarrollado bajo el enfoque arquitectónico de **Monolito Modular (Modular Monolith)** con **División Vertical (Vertical Slicing)**, **Java 21**, **Spring Boot**, **Spring Security (JWT)** y **PostgreSQL**.

---

## 📋 Tabla de Contenidos
- [Características Principales](#-características-principales)
- [Arquitectura del Sistema](#-arquitectura-del-sistema)
- [Stack Tecnológico](#-stack-tecnológico)
- [Módulos del Sistema](#-módulos-del-sistema)
- [Roles y Permisos](#-roles-y-permisos)
- [Requisitos Previos](#-requisitos-previos)
- [Puesta en Marcha](#-puesta-en-marcha)
  - [Opción 1: Con Docker Compose (Recomendado)](#opción-1-con-docker-compose-recomendado)
  - [Opción 2: Ejecución Local con Maven](#opción-2-ejecución-local-con-maven)
- [Documentación de la API (Swagger y Postman)](#-documentación-de-la-api)
- [Variables de Entorno](#-variables-de-entorno)
- [Pruebas Automatizadas](#-pruebas-automatizadas)

---

## ✨ Características Principales

* ⚡ **Procesamiento Ágil de Ventas**: Registro de ventas en mostrador con soporte para lector de código de barras y cálculo automático de cambio.
* 📦 **Control de Inventario en Tiempo Real**: Deducción automática de stock con bloqueo pesimista (`Pessimistic Locking`) para prevenir condiciones de carrera concurrentes.
* 🧾 **Facturación Electrónica DIAN**: Integración con **Factus API** y adaptador desacoplado para emisión de facturas electrónicas colombianas.
* 📊 **Reportes y Analítica con CQRS**: Consultas analíticas optimizadas de solo lectura (`Read Model`) implementadas con **`JdbcClient`**, eliminando sobrecostos de ORM y problemas N+1.
* 🚚 **Gestión de Abastecimiento**: Registro de compras a proveedores con incremento automático de existencias.
* 🔐 **Seguridad y Trazabilidad**: Autenticación mediante **JWT (HTTP-only Cookies / Bearer)**, control de acceso basado en roles (RBAC) y auditoría de transacciones.

---

## 🏛 Arquitectura del Sistema

El proyecto sigue una arquitectura de **Monolito Modular**:

```
                  ┌────────────────────────────────────────────────┐
                  │          API REST / Controladores JSON         │
                  └───────┬────────────────────────────────┬───────┘
                          │                                │
                          ▼                                ▼
              ┌──────────────────────┐          ┌──────────────────────┐
              │    MÓDULO: sales     │          │  MÓDULO: inventory   │
              │  (Vertical Slice)    │          │  (Vertical Slice)    │
              │                      │          │                      │
              │  + API Pública       │          │  + API Pública       │
              │  + Lógica Interna    │          │  + Lógica Interna    │
              └───────────┬──────────┘          └───────────┬──────────┘
                          │                                │
                          └────────────────┬───────────────┘
                                           │ JPA / JdbcClient
                                           ▼
                  ┌────────────────────────────────────────────────┐
                  │               PostgreSQL 16 (SQL)              │
                  └────────────────────────────────────────────────┘
```

* **API Pública del Módulo (`Facade` / DTOs)**: Interfaces de entrada expuestas para interactuar entre dominios.
* **Lógica Interna (`internal/`)**: Entidades JPA, repositorios y servicios encapsulados (`package-private`), inaccesibles desde otros módulos.
* **Read Models (CQRS)**: Para el módulo de reportes (`reports`), se utiliza `JdbcClient` con SQL nativo y agregaciones en base de datos (`SUM`, `COUNT`, `GROUP BY`), desacoplado de las entidades de dominio.

---

## 🛠 Stack Tecnológico

* **Lenguaje**: Java 21 LTS
* **Framework**: Spring Boot 3+ (Spring WebMvc, Spring Data JPA, Spring Security, Spring JDBC)
* **Base de Datos**: PostgreSQL 16 (Producción / Docker) y H2 (Entornos de prueba)
* **Seguridad**: JWT (JSON Web Tokens con algoritmo HMAC-SHA256)
* **Facturación**: Factus API (Estándar DIAN) / Mock Adapter
* **Documentación**: SpringDoc OpenAPI 3 / Swagger UI
* **Contenedores**: Docker & Docker Compose
* **Gestor de Dependencias**: Apache Maven (Wrapper incluido)

---

## 🧩 Módulos del Sistema

| Módulo | Paquete | Descripción |
|---|---|---|
| **Auth** | `com.sciencebot.pos.auth` | Autenticación, login, logout y validación de tokens JWT. |
| **Users** | `com.sciencebot.pos.users` | Gestión de usuarios, perfiles y estados de actividad. |
| **Categories** | `com.sciencebot.pos.categories` | Clasificación y categorías del catálogo de productos. |
| **Products** | `com.sciencebot.pos.products` | Catálogo de productos, códigos internos, códigos de barras y precios. |
| **Inventory** | `com.sciencebot.pos.inventory` | Ajustes de stock, entradas/salidas y control de mínimos. |
| **Customers** | `com.sciencebot.pos.customers` | Base de datos de clientes y datos fiscales para facturación. |
| **Suppliers** | `com.sciencebot.pos.suppliers` | Directorio de proveedores para compras. |
| **Purchases** | `com.sciencebot.pos.purchases` | Registro de compras de abastecimiento que aumentan el stock. |
| **Sales** | `com.sciencebot.pos.sales` | Procesamiento de ventas en efectivo y generación de comprobantes. |
| **Billing** | `com.sciencebot.pos.billing` | Emisión y sincronización de facturación electrónica con la DIAN. |
| **Reports** | `com.sciencebot.pos.reports` | Analítica de ventas, productos más vendidos, bajo stock y rentabilidad. |
| **Settings** | `com.sciencebot.pos.settings` | Configuración del negocio (nombre, NIT, encabezados de ticket). |

---

## 👥 Roles y Permisos

| Rol | Perfil | Alcance |
|---|---|---|
| `ADMIN` | Administrador / Dueño | Control total: gestión de usuarios, reportes, facturación y configuración. |
| `SUPERVISOR` | Supervisor de Tienda | Compras a proveedores, inventario, clientes y reintentos de facturas. |
| `SELLER` | Vendedor / Cajero | Operación de caja: ventas en efectivo, consulta de catálogo y tickets. |

---

## 📦 Requisitos Previos

* **Java 21 JDK** instalado (si ejecutas de forma nativa).
* **Docker** y **Docker Compose** instalados.
* **Git**.

---

## 🚀 Puesta en Marcha

### Opción 1: Con Docker Compose (Recomendado)

Inicia la base de datos PostgreSQL y la aplicación en un entorno contenedorizado:

```bash
# Levantar los contenedores de desarrollo
docker compose up -d

# Para verificar los logs de la aplicación
docker compose logs -f app
```

La aplicación quedará disponible en: `http://localhost:8080`  
Base de datos PostgreSQL expuesta en el puerto: `5434` (usuario: `postgres`, contraseña: `postgres`, bd: `app_db`).

---

### Opción 2: Ejecución Local con Maven

1. Inicia únicamente la base de datos PostgreSQL:
   ```bash
   docker compose up -d db
   ```

2. Ejecuta el backend usando el Maven Wrapper:
   * **Linux / macOS**:
     ```bash
     ./mvnw spring-boot:run
     ```
   * **Windows (PowerShell / CMD)**:
     ```powershell
     .\mvnw.cmd spring-boot:run
     ```

---

## 📖 Documentación de la API

Una vez iniciada la aplicación, puedes explorar e interactuar con los endpoints mediante:

* **Swagger UI interactivo**:  
  👉 [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

* **OpenAPI Docs (JSON)**:  
  👉 [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

* **Colección de Postman**:  
  El repositorio incluye la colección lista para importar en:  
  📁 [`POS_System_API_Postman_Collection.json`](./POS_System_API_Postman_Collection.json)

---

## ⚙️ Variables de Entorno

Puedes personalizar la configuración mediante variables de entorno en tu sistema o en el archivo `.env`:

| Variable | Valor por Defecto | Descripción |
|---|---|---|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/app_db` | URL de conexión a la base de datos PostgreSQL. |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | Usuario de la base de datos. |
| `SPRING_DATASOURCE_PASSWORD` | `postgres` | Contraseña de la base de datos. |
| `JWT_SECRET` | *Clave de 256 bits por defecto* | Clave secreta para firmar tokens JWT. |
| `JWT_EXPIRATION_MS` | `28800000` (8 horas) | Tiempo de vida del token en milisegundos. |
| `BILLING_PROVIDER` | `factus` | Proveedor de facturación (`factus` o `mock`). |
| `FACTUS_URL` | `https://api-sandbox.factus.com.co` | Endpoint de la API de Factus. |
| `FACTUS_CLIENT_ID` | *Sandbox Client ID* | Identificador de cliente en Factus. |
| `FACTUS_CLIENT_SECRET` | *Sandbox Client Secret* | Secreto de cliente en Factus. |

---

## 🧪 Pruebas Automatizadas

Ejecutar la suite completa de pruebas unitarias y de integración:

```bash
# Linux / macOS
./mvnw test

# Windows
.\mvnw.cmd test
```

Para generar el empaquetado de producción (`JAR`):

```bash
# Linux / macOS
./mvnw clean package -DskipTests

# Windows
.\mvnw.cmd clean package -DskipTests
```
