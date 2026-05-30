# gestion-usuario-frontend

Frontend Vue 3 para el sistema de gestión de usuarios.

---

## Stack Tecnológico

| Tecnología | Versión |
|---|---|
| Vue | 3.x + Composition API (`<script setup>`) |
| Vite | 8.x |
| Pinia | 3.x — State management |
| Vue Router | 4.x — SPA routing con Navigation Guards |
| Axios | 1.x — HTTP client con interceptores JWT |
| Vue Toastification | 2.x — Notificaciones |
| CSS | Vanilla con dark mode y glassmorphism |

---

## Requisitos previos

- **Node.js >= 18** (recomendado: v22 LTS o superior)
- Backend `gestion-usuario-service` corriendo en `localhost:8091`

---

## Instalación y desarrollo

```bash
npm install
npm run dev
```

El servidor arranca en **http://localhost:5173**

---

## Solución de problemas comunes al levantar

### ❌ `"vite" no se reconoce como un comando`

El `node_modules` incluido en el ZIP no es compatible con Windows. Bórralo y reinstala:

```powershell
# En PowerShell
Remove-Item -Recurse -Force node_modules
npm install
npm run dev
```

### ❌ `Unexpected token '??='`

Tu versión de Node.js es muy antigua (menor a v18). Vite 8 requiere Node 18 o superior.

**Si usas NVM (Node Version Manager):**

```powershell
nvm install 24.16.0
nvm use 24.16.0
node --version   # debe mostrar v24.x.x
npm install
npm run dev
```

**Si no usas NVM:**

Descarga e instala Node LTS desde https://nodejs.org, luego abre una nueva terminal y vuelve a intentarlo.

> ⚠️ Después de instalar o cambiar la versión de Node, **cierra y abre una nueva terminal** para que el PATH se actualice.

### ❌ `npm ERR! code EEXIST` al hacer `npm install`

El `node_modules` del ZIP está corrupto. Bórralo primero:

```powershell
Remove-Item -Recurse -Force node_modules
npm install
```

---

## Módulos y rutas

| Módulo | Ruta | Acceso |
|---|---|---|
| Login | `/auth/login` | Público |
| Registro | `/auth/register` | Público |
| Recuperar contraseña | `/auth/forgot-password` | Público |
| Nueva contraseña | `/reset-password?token=...` | Público |
| Mi Perfil | `/profile` | Autenticado |
| Gestión de Usuarios | `/admin/users` | Solo ROLE_ADMIN |

---

## Variables de entorno

No se requieren variables de entorno para desarrollo. El proxy de Vite redirige `/api/*` → `http://localhost:8091/api/*` automáticamente.

---

## Usuarios de prueba

| Email | Contraseña | Rol |
|---|---|---|
| `admin@gestion.com` | `AdminPass123!` | ROLE_ADMIN |
| `user@gestion.com` | `UserPass123!` | ROLE_USER |
| `supervisor@gestion.com` | `Supervisor123!` | ROLE_SUPERVISOR |

> Los usuarios los crea el backend automáticamente al iniciar por primera vez.

---

## Recuperación de contraseña

El backend genera el token y lo imprime en consola (simulación). El link generado es:

```
http://localhost:5173/reset-password?token=<TOKEN>
```

Copia el token desde la consola del backend y navega a esa URL.
