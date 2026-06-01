# TheraTrack Home

Aplicación Android nativa para el seguimiento domiciliario de pacientes sometidos a tratamiento radiofarmacológico (I-131, Lu-177 y similares). Conecta al paciente con su equipo médico durante el periodo de aislamiento post-tratamiento, permitiendo el reporte diario de síntomas, la visualización de alertas y recomendaciones clínicas, y la generación de informes exportables.

Proyecto académico desarrollado en el marco del Grado en Imagen Médica y Medicina Nuclear de la **Universidad Europea de Madrid**.

---

## Características principales

### Panel del paciente
- Registro con CIPA (10 dígitos) y código de hospital
- Login con CIPA o email interno + PIN de 6 dígitos
- Dashboard con indicador de estado de seguridad radiológica (Seguro / Precaución / Alerta)
- Reporte diario de síntomas: temperatura, pulso, nivel de fatiga, nivel de dolor, síntomas y notas
- Historial con gráfica (MPAndroidChart) de los últimos 30 días
- Centro de alertas y recomendaciones clínicas con detalle en BottomSheet
- Configuración de notificaciones locales (alertas críticas, recomendaciones, recordatorios)
- Perfil con selector de idioma y cierre de sesión
- Asistencia: FAQ expandible, contacto con hospital, soporte técnico, privacidad y términos

### Panel hospitalario / profesional
- Registro con número de colegiado (6-10 dígitos) y selección de hospital
- Login con colegiado o email interno + PIN de 6 dígitos
- Dashboard con métricas: total pacientes, alertas activas, en seguimiento, altas
- Gestión de pacientes con búsqueda en tiempo real y ficha clínica detallada
- Solicitudes pendientes (pacientes y profesionales) con aprobación y rechazo
- Creación de fichas clínicas: radiofármaco, dosis, fechas de tratamiento/alta, días de aislamiento
- Centro de alertas del hospital con filtro de no leídas
- Generación de informes clínicos en **PDF nativo** con gráfica, tabla de alertas y recomendaciones
- Exportación de registros a **CSV** compatible con Excel
- Ajustes: cambio de PIN, notificaciones, umbrales de alerta, 2FA (stub preparado)

### Funcionalidades transversales
- Notificaciones locales Android con 3 canales (alertas críticas, recomendaciones, recordatorios)
- Sincronización automática de notificaciones al abrir los dashboards
- Compartición de PDF/CSV mediante FileProvider y selector del sistema
- Flujo de aprobación por estado (pendiente → aprobado / rechazado)

---

## Stack tecnológico

| Componente | Tecnología | Versión |
|---|---|---|
| Lenguaje | Kotlin | 1.9+ |
| UI | XML Views (sin Compose) | — |
| SDK mínimo | Android 7.0 (API 24) | — |
| SDK compilación / target | Android API 36 | — |
| Backend | Supabase (Auth + PostgREST) | — |
| Base de datos | PostgreSQL gestionado por Supabase | — |
| HTTP client | Ktor Android Engine | 3.1.3 |
| Async | Kotlin Coroutines | 1.8.0 |
| Gráficas | MPAndroidChart | 3.1.0 |
| Material UI | Material Components for Android | 1.13.0 |
| Generación PDF | `android.graphics.pdf.PdfDocument` (nativo) | — |
| Serialización | kotlinx.serialization | — |
| Notificaciones | NotificationCompat (local, sin FCM) | — |

---

## Requisitos previos

- **Android Studio** Hedgehog 2023.1.1 o superior
- **JDK 21** (incluido en Android Studio Hedgehog+ o vía Eclipse JustJ)
- **Gradle** 8.x (gestionado por el wrapper del proyecto)
- Emulador Android API 24+ o dispositivo físico con Android 7.0+
- Cuenta Supabase (si se va a desplegar un backend propio)
- Conexión a internet durante el desarrollo

---

## Instalación y configuración

### 1. Clonar el repositorio

```bash
git clone <URL_DEL_REPOSITORIO>
cd TheraTrack_Home_App/TheraTrackHome
```

### 2. Abrir en Android Studio

`File → Open` → seleccionar la carpeta `TheraTrackHome/`.

Android Studio sincronizará Gradle automáticamente y configurará `local.properties` con la ruta del Android SDK.

### 3. Configurar JAVA_HOME (solo si compilas desde terminal)

Si usas `gradlew` desde la terminal en lugar de Android Studio, debes apuntar al JDK 21. En el entorno actual del proyecto se usa el JRE integrado de Eclipse JustJ:

```powershell
# Windows PowerShell
$env:JAVA_HOME = "C:\Users\roger\.p2\pool\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.7.v20250502-0916\jre"
./gradlew assembleDebug
```

```bash
# macOS / Linux — ajustar la ruta al JDK 21 local
export JAVA_HOME="/path/to/jdk21"
./gradlew assembleDebug
```

### 4. Configuración de Supabase

Las credenciales están en `app/src/main/java/com/example/theratrackhome/controller/SupabaseClient.kt`.

El backend académico ya está configurado. Para desplegar un backend propio:

1. Crear proyecto en [supabase.com](https://supabase.com)
2. Ejecutar el schema SQL (ver [docs/SUPABASE.md](docs/SUPABASE.md))
3. Actualizar `supabaseUrl` y `supabaseKey` en `SupabaseClient.kt`

### 5. Configuración de Auth en Supabase Dashboard

> **Obligatorio para que el registro funcione en entorno de desarrollo:**

1. Supabase Dashboard → **Authentication → Providers → Email**
2. **Confirm email** → OFF
3. **Allow new users to sign up** → ON
4. Guardar cambios

### 6. Build y ejecutar

```bash
./gradlew assembleDebug     # Genera APK de debug
./gradlew installDebug      # Instala en dispositivo/emulador conectado
```

O desde Android Studio: botón ▶ Run.

---

## Cómo usar la app

### Como paciente

1. Pantalla de inicio → **Acceso Paciente**
2. **Registro**: nombre, CIPA (10 dígitos del dorso de la tarjeta sanitaria), código de hospital (ej. `THERA-0001`), PIN de 6 dígitos
3. La cuenta queda en estado **Pendiente** hasta que el equipo médico la apruebe
4. Una vez aprobado, el personal sanitario crea la ficha clínica
5. **Login**: CIPA (o email interno asignado) + PIN
6. Reportar síntomas diariamente desde la pestaña Síntomas
7. Consultar historial, alertas y recomendaciones

### Como profesional sanitario

1. Pantalla de inicio → **Acceso Hospital**
2. **Registro**: nombre, número de colegiado, hospital, cargo, teléfono, motivo, PIN de 6 dígitos
3. La cuenta queda **Pendiente** hasta aprobación de un administrador (ver [docs/SUPABASE.md](docs/SUPABASE.md#7-cómo-crear-un-administrador-inicial))
4. **Login**: número de colegiado (o email interno) + PIN
5. Aprobar solicitudes de pacientes y crear sus fichas clínicas
6. Monitorizar pacientes, gestionar alertas y generar informes PDF/CSV

---

## Datos de prueba para desarrollo

| Campo | Valor |
|---|---|
| Códigos de hospital | `THERA-0001`, `THERA-0002`, `THERA-0003` |
| CIPA ejemplo | `1724979086` |
| Colegiado ejemplo | `281234567` |

**Resetear códigos de hospital:**

```sql
UPDATE public.codigos_hospital
SET usado = false
WHERE codigo IN ('THERA-0001', 'THERA-0002', 'THERA-0003');
```

**Crear primer administrador:**

```sql
UPDATE public.perfiles
SET estado = 'aprobado'
WHERE email = 'TU_COLEGIADO@medico.theratrack.internal';
```

Para SQL completo de pruebas, ver [docs/TESTING.md](docs/TESTING.md).

---

## Estructura del proyecto

```
TheraTrackHome/
├── app/src/main/java/com/example/theratrackhome/
│   ├── SplashActivity.kt               # Routing inicial por rol y estado
│   ├── PerfilActivity.kt               # Selector paciente / hospital
│   ├── HospitalMainActivity.kt         # Contenedor BottomNav hospital (5 tabs)
│   ├── PacienteMainActivity.kt         # Contenedor BottomNav paciente (4 tabs)
│   ├── [otras Activities...]
│   ├── fragments/                      # 9 Fragments del BottomNav
│   ├── controller/                     # Lógica de negocio y acceso a Supabase
│   ├── model/                          # Data classes serializables
│   └── util/IdentificadorUtils.kt      # CIPA / colegiado / PIN / emails internos
├── app/src/main/res/
│   ├── layout/                         # 34 layouts XML
│   ├── drawable/                       # 78 recursos gráficos
│   └── menu/                           # Menús de BottomNavigationView
├── supabase/                           # Scripts SQL
└── docs/                               # Documentación técnica
```

---

## Documentación adicional

| Documento | Descripción |
|---|---|
| [docs/GUIA_TECNICA.md](docs/GUIA_TECNICA.md) | Referencia técnica completa: Activities, Fragments, Controllers, Models, flujos |
| [docs/ARQUITECTURA.md](docs/ARQUITECTURA.md) | Patrón MVC, diagrama de capas, navegación, relaciones de BD |
| [docs/SUPABASE.md](docs/SUPABASE.md) | Schema SQL, RLS, RPCs, configuración Auth, mantenimiento |
| [docs/IDENTIFICACION.md](docs/IDENTIFICACION.md) | Sistema CIPA/colegiado, PIN, emails ficticios, IdentificadorUtils |
| [docs/TESTING.md](docs/TESTING.md) | Plan de pruebas manual, casos de prueba, SQL helper, Logcat |
| [docs/CONTRIBUIR.md](docs/CONTRIBUIR.md) | Workflow Git, convenciones de código, cómo añadir pantallas y tablas |

---

## Equipo

| Nombre | Rol | Responsabilidad principal |
|---|---|---|
| Roy (Roger) | Desarrollador técnico | Backend, integración Supabase, arquitectura Android |
| Daniel | Desarrollador | (completar por el equipo) |
| Andrea | Desarrolladora | (completar por el equipo) |

**Stakeholders:** Estudiantes y docentes del Grado en Imagen Médica y Medicina Nuclear — Universidad Europea de Madrid.

---

## Estado del proyecto

**Versión actual:** 1.0 (versionCode 1)

### Completado
- Registro y login paciente con CIPA + PIN
- Registro y login profesional con colegiado + PIN
- Flujo completo de aprobación
- Fichas clínicas, reporte de síntomas, historial con gráfica
- Alertas, recomendaciones y notificaciones locales
- Informe PDF nativo y exportación CSV
- Panel hospitalario completo (5 secciones)
- Panel paciente completo (4 secciones)

### Limitaciones conocidas
- 2FA: stub implementado, sin funcionalidad real
- Notificaciones locales (sin FCM); polling solo al abrir dashboard
- Strings en inglés incompletos
- PDF sin firma digital
- `sdk.dir` en `local.properties` debe configurarse por cada desarrollador

### Próximas versiones
- ViewBinding, FCM, i18n completo, tests automatizados, firma digital PDF

---

## Licencia

Proyecto académico — **Universidad Europea de Madrid**. Uso restringido al entorno académico. Sin licencia de distribución comercial.

---

## Contacto

Para issues técnicos, abrir ticket en el repositorio del proyecto.
