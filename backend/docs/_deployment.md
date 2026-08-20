<!-- doc-version: 1.1 | last-updated: 2026-08-20 -->
# Despliegue y Configuración

## Requisitos del Servidor
| Componente | Mínimo | Recomendado |
|---|---|---|
| SO | Linux Ubuntu 22.04 / Windows Server 2022 | Ubuntu 22.04 LTS |
| JDK | 21 LTS | Amazon Corretto 21 |
| CPU/RAM | 1 vCPU / 2 GB | 2 vCPU / 4 GB |
| Disco | 10 GB | 20 GB |
| BD | PostgreSQL 16 | PostgreSQL 16, 5 GB inicial |

## Variables de Entorno
| Variable | Descripción | Ejemplo |
|---|---|---|
| PORT | Puerto HTTP del backend | 8080 |
| DB_HOST | Host PostgreSQL | localhost |
| DB_PORT | Puerto PostgreSQL | 5432 |
| DB_NAME | Nombre de la base de datos | app_pos_db |
| DB_USERNAME | Usuario de BD | pos_db_user |
| DB_PASSWORD | Contraseña de BD | (secreto) |
| JWT_SECRET | Clave simétrica JWT (≥256 bits) | (secreto) |
| JWT_EXPIRATION_MS | TTL del access token en ms (15 min) | 900000 |
| JWT_REFRESH_EXPIRATION_MS | TTL del refresh token en ms (7 días) | 604800000 |
| UPLOAD_DIR | Directorio de imágenes | /var/lib/app-pos/uploads |

## Compilación Tradicional (Nativa)
```bash
./mvnw clean package -DskipTests
```
Genera: `target/app.pos-0.0.1-SNAPSHOT.jar`

## Ejecución Tradicional (Nativa)
```bash
java -jar target/app.pos-0.0.1-SNAPSHOT.jar
```

## Inicialización de BD
- Esquema: auto-generado con `spring.jpa.hibernate.ddl-auto=update` (o `validate` en producción)
- Datos semilla obligatorios: Usuario Admin por defecto + "Cliente General" (RN-CUST-001)

---

## Despliegue Containerizado (Docker y Docker Compose)

El proyecto cuenta con soporte nativo para despliegue en contenedores Docker mediante una construcción multi-etapa en el `Dockerfile`.

### 1. Entorno de Desarrollo (Local)
Utiliza la etapa `dev` del Dockerfile. Monta el código fuente y el repositorio local de Maven como volúmenes para acelerar compilaciones y habilitar el reinicio en caliente.

**Comandos**:
- Levantar servicios (PowerShell): `.\docker-dev.ps1 start`
- Levantar servicios (Unix Bash): `./docker-dev.sh start`
- Detener servicios: `docker compose down`

### 2. Entorno de Producción
Utiliza la etapa `prod` del Dockerfile. Construye el JAR internamente y genera una imagen JRE ligera y segura sin código fuente ni dependencias de desarrollo, ejecutando bajo el usuario no privilegiado `spring`.

**Comandos**:
- Levantar servicios en producción:
  ```bash
  docker compose -f docker-compose.prod.yml up --build -d
  ```
- Detener servicios en producción:
  ```bash
  docker compose -f docker-compose.prod.yml down
  ```

### Transición de Desarrollo a Producción

El paso de desarrollo a producción se realiza de manera limpia:
1. **Sin dependencias del host**: Todo el proceso de empaquetado final ocurre dentro del contenedor de construcción de Docker (etapa `builder`), garantizando que no se arrastren archivos locales innecesarios ni configuraciones corruptas del host.
2. **Configuración de Variables de Entorno**: En producción, asegúrate de proporcionar los valores correctos para las variables de entorno de base de datos (`DB_NAME`, `DB_USER`, `DB_PASSWORD`) y seguridad (`JWT_SECRET`, `JWT_EXPIRATION_MS`), las cuales sobrescriben los valores por defecto configurados en `application.properties`.
3. **Hibernate DDL Auto**: En desarrollo se usa `update` para iterar rápidamente sobre el esquema. En producción se establece en `validate` para evitar alteraciones accidentales del esquema de la base de datos de producción.

