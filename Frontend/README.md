# gestion-usuario-frontend

Frontend Vue 3 para el sistema de gestión de usuarios.

## Stack

- **Vue 3** + Composition API (`<script setup>`)
- **Vite 5** (dev server en puerto 3000)
- **Pinia** — State management
- **Vue Router 4** — SPA routing con Navigation Guards
- **Axios** — HTTP client con interceptores JWT
- **Vue Toastification** — Notificaciones
- Vanilla CSS con dark mode y glassmorphism

## Módulos

| Módulo | Ruta | Acceso |
|---|---|---|
| Login | `/auth/login` | Público |
| Registro | `/auth/register` | Público |
| Recuperar contraseña | `/auth/forgot-password` | Público |
| Nueva contraseña | `/reset-password?token=...` | Público |
| Mi Perfil | `/profile` | Autenticado |
| Gestión de Usuarios | `/admin/users` | Solo ROLE_ADMIN |

## Requisitos

- Node.js >= 18
- Backend `gestion-usuario-service` corriendo en `localhost:8091`

## Instalación y desarrollo

```bash
npm install
npm run dev
```

El servidor arranca en **http://localhost:3000** (o `3001` si el puerto está ocupado)

## Variables de entorno

No se requieren variables de entorno para desarrollo. El proxy de Vite redirige `/api/*` → `http://localhost:8091/api/*`.

## Recuperación de contraseña

El backend genera el token y lo imprime en consola (simulación). El link generado es:
```
http://localhost:3000/reset-password?token=<TOKEN>
```
Copia el token desde la consola del backend y navega a esa URL.
