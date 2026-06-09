# SmartBook Mobile (Android)

Aplicación móvil nativa Android para SmartBook CDI CECAR, desarrollada con:

- Jetpack Compose (UI)
- Ktor (cliente HTTP)
- MVVM
- Navigation Compose (zona pública/privada)
- EncryptedSharedPreferences + Keystore (JWT)
- DataStore (preferencias auxiliares)

## Requisitos

- Android Studio Koala o superior
- JDK 17
- Android SDK 35
- Mínimo Android 8.0 (API 26)

## Configuración rápida

1. Abre la carpeta del proyecto en Android Studio.
2. Sincroniza Gradle.
3. Ejecuta el módulo `app` en un emulador/dispositivo Android.

## Endpoints y seguridad

- Base URL: `https://api.smartbooks.cecar.cloud/api`
- Swagger oficial: `https://api.smartbooks.cecar.cloud/index.html`
- JWT en `Authorization: Bearer {token}` para endpoints protegidos.
- Cierre automático de sesión ante respuesta `401`.

## Módulos implementados

- Autenticación: login, solicitar restablecimiento, restablecer contraseña
- Dashboard: métricas y últimas ventas
- Clientes: listado, búsqueda, crear/editar
- Libros: listado, búsqueda, crear/editar
- Lotes: listado y registro
- Ingresos: listado y registro de entradas por lote/libro
- Inventario: resumen y detección de bajo stock
- Ventas: listado, detalle y registro
- Usuarios (admin): listado y crear/editar
- Perfil: información del usuario autenticado y cierre de sesión

## Arquitectura

- `core/`: red, seguridad, tema, utilidades
- `data/`: contenedor de dependencias y repositorio
- `features/`: módulos por dominio (viewmodel + screens)
- `navigation/`: rutas y composición de navegación pública/privada

## Notas técnicas

- El contrato de endpoints, queries y DTOs se alinea con el Swagger oficial del backend.
- Los modelos de request/response son tolerantes (`ignoreUnknownKeys = true`) para acoplarse a variaciones del backend.

## Credenciales de prueba (según especificación)

- Administrador:
  - `admincdi@yopmail.com`
  - `AdminCDI2026`
- Vendedor:
  - `nauxcdi@yopmail.com`
  - `AuxCDI2026`
