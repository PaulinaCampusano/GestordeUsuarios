
# GestionUsuario — Auth & User Management API

Microservicio Spring Boot para autenticación JWT y gestión completa de usuarios, roles y permisos.

---

## Stack Tecnológico

| Tecnología | Versión |
|---|---|
| Java | 21 |
| Spring Boot | 3.3.0 |
| Spring Security | 6.x |
| JWT (jjwt) | 0.12.5 |
| JPA / Hibernate | via Spring Boot |
| MySQL | 8.x |
| Lombok | 1.18.34 |
| Swagger / OpenAPI | 2.5.0 |

---

## Requisitos previos

- Java 21
- Maven 3.9+
- MySQL 8 corriendo en `localhost:3306`

---

## Configuración

Copia o crea el archivo `.env` (opcional) con las variables:

```bash
DB_USERNAME=root
DB_PASSWORD=tu_password
JWT_SECRET=tu_secret_base64_de_al_menos_256_bits
```

O edita directamente `src/main/resources/application-dev.yml`.

---

## Ejecución

```bash
# Compilar y ejecutar
./mvnw spring-boot:run

# O con Maven instalado
mvn spring-boot:run
```

La app arrancará en: **http://localhost:8091**

Swagger UI disponible en: **http://localhost:8091/swagger-ui.html**

---

## Usuarios de prueba (DataSeeder)

| Email | Contraseña | Rol |
|---|---|---|
| `admin@gestion.com` | `AdminPass123!` | ROLE_ADMIN |
| `user@gestion.com` | `UserPass123!` | ROLE_USER |
| `supervisor@gestion.com` | `Supervisor123!` | ROLE_SUPERVISOR |

---

## Endpoints principales

### Auth (público)
| Método | Endpoint | Descripción |
|---|---|---|
| POST | `/api/auth/register` | Registrar usuario |
| POST | `/api/auth/login` | Login → JWT |
| POST | `/api/auth/logout` | Logout |
| POST | `/api/auth/reset-password` | Solicitar reset de contraseña |
| POST | `/api/auth/new-password` | Confirmar nueva contraseña |
| GET | `/api/auth/me` | Ver perfil propio |
| PUT | `/api/auth/me` | Actualizar perfil |
| PUT | `/api/auth/me/password` | Cambiar contraseña |

### Admin (ROLE_ADMIN)
| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/admin/users` | Listar usuarios |
| POST | `/api/admin/users` | Crear usuario |
| PUT | `/api/admin/users/{id}` | Editar usuario |
| PATCH | `/api/admin/users/{id}/toggle-status` | Activar/Desactivar |
| POST | `/api/admin/users/{id}/roles/{roleName}` | Asignar rol |
| DELETE | `/api/admin/users/{id}/roles/{roleName}` | Revocar rol |
| GET | `/api/admin/roles` | Listar roles |
| POST | `/api/admin/roles` | Crear rol |
| GET | `/api/admin/modules` | Listar módulos |
| GET | `/api/admin/access-types` | Listar tipos de acceso |
| POST | `/api/admin/roles/{id}/permissions` | Otorgar permiso |
| DELETE | `/api/admin/roles/{id}/permissions` | Revocar permiso |

---

## Política de contraseñas

- Mínimo 8 caracteres
- Al menos 1 letra mayúscula
- Al menos 1 letra minúscula
- Al menos 1 número
- Al menos 1 carácter especial (`!@#$%^&*` etc.)

---

## Seguridad

- JWT firmado con HS256
- Tokens con expiración de 24 horas (configurable)
- Rutas admin protegidas por `ROLE_ADMIN`
- Usuarios recién registrados quedan **inactivos** hasta activación manual por admin
- Respuestas de error uniformes con `timestamp`, `status`, `error` y `message`
=======
