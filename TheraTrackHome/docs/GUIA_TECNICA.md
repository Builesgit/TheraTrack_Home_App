# Guía Técnica — TheraTrack Home

## 1. Introducción

Este documento es la referencia técnica completa del proyecto TheraTrack Home. Está dirigido a desarrolladores que trabajen activamente en el proyecto y necesiten entender la arquitectura, los componentes, los flujos de datos y las convenciones de código.

**Audiencia:** Desarrolladores Android con conocimientos básicos de Kotlin, Coroutines y Supabase.

**Convenciones de este documento:**
- Los bloques de código Kotlin muestran firmas reales del código fuente
- Los fragmentos SQL son ejecutables en Supabase SQL Editor
- Las rutas de archivo son relativas a `TheraTrackHome/app/src/main/`
- "Controller" se refiere a los objetos en `controller/`, no al componente MVC clásico de Android

Para la arquitectura global ver [ARQUITECTURA.md](ARQUITECTURA.md). Para el backend ver [SUPABASE.md](SUPABASE.md).

---

## 2. Arquitectura general

### 2.1 Patrón MVC adaptado

```
Model     → data classes @Serializable en model/
View      → Activities, Fragments, layouts XML
Controller → objetos en controller/ con suspend fun
```

La comunicación View → Controller ocurre dentro de coroutines (`lifecycleScope.launch`). El Controller nunca toca el hilo UI directamente; devuelve `Result<T>` y la View actualiza la UI en el bloque `.onSuccess { }`.

### 2.2 Manejo de sesión

La sesión la gestiona Supabase Auth internamente. `AuthController.sesionActiva()` verifica si existe un token activo. `SplashActivity` usa este método para decidir si mostrar login o el panel correspondiente según el rol y estado del perfil.

---

## 3. Estructura de directorios completa

```
TheraTrackHome/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/theratrackhome/
│   │   │   │
│   │   │   ├── MainApplication.kt              Inicializa canales de notificación
│   │   │   ├── SplashActivity.kt               Routing inicial (2s delay)
│   │   │   ├── PerfilActivity.kt               Selector de rol de acceso
│   │   │   │
│   │   │   ├── ── PACIENTE ──
│   │   │   ├── LoginActivity.kt                Login CIPA/email + PIN
│   │   │   ├── RegisterActivity.kt             Registro con CIPA
│   │   │   ├── PacienteMainActivity.kt         Contenedor BottomNav 4 tabs
│   │   │   ├── AlertasActivity.kt              Alertas y recomendaciones
│   │   │   ├── AsistenciaActivity.kt           FAQ + contacto + soporte
│   │   │   ├── ConfiguracionNotificacionesActivity.kt
│   │   │   ├── ConfirmacionSintomasActivity.kt Post-registro síntomas
│   │   │   │
│   │   │   ├── ── PROFESIONAL ──
│   │   │   ├── LoginHospitalActivity.kt        Login colegiado/email + PIN
│   │   │   ├── RegistroHospitalActivity.kt     Registro con colegiado
│   │   │   ├── HospitalMainActivity.kt         Contenedor BottomNav 5 tabs
│   │   │   ├── SolicitudesPendientesActivity.kt Aprobación/rechazo
│   │   │   ├── CrearFichaPacienteActivity.kt   Ficha clínica
│   │   │   ├── DetallePacienteActivity.kt      Vista completa paciente
│   │   │   │
│   │   │   ├── ── COMPARTIDO ──
│   │   │   ├── EsperaAprobacionActivity.kt     Cuenta pendiente
│   │   │   │
│   │   │   ├── fragments/
│   │   │   │   ├── DashboardPacienteFragment.kt
│   │   │   │   ├── HistorialFragment.kt
│   │   │   │   ├── RegistroDiarioFragment.kt
│   │   │   │   ├── PerfilPacienteFragment.kt
│   │   │   │   ├── DashboardHospitalFragment.kt
│   │   │   │   ├── PacientesHospitalFragment.kt
│   │   │   │   ├── AlertasHospitalFragment.kt
│   │   │   │   ├── InformesHospitalFragment.kt
│   │   │   │   └── AjustesHospitalFragment.kt
│   │   │   │
│   │   │   ├── controller/
│   │   │   │   ├── SupabaseClient.kt           Singleton del cliente HTTP
│   │   │   │   ├── AuthController.kt           Auth: login, registro, logout
│   │   │   │   ├── PacienteController.kt       Datos pacientes, alertas, recomendaciones
│   │   │   │   ├── RegistroController.kt       Registros diarios
│   │   │   │   ├── AdminController.kt          Solicitudes, aprobaciones, fichas
│   │   │   │   ├── NotificacionesManager.kt    Canales y notificaciones locales
│   │   │   │   ├── SyncNotificacionesManager.kt Polling al abrir dashboard
│   │   │   │   ├── PdfReportGenerator.kt       PDF con PdfDocument nativo
│   │   │   │   └── CsvExporter.kt              Exportación CSV
│   │   │   │
│   │   │   ├── model/
│   │   │   │   ├── Perfil.kt
│   │   │   │   ├── Paciente.kt
│   │   │   │   ├── Hospital.kt
│   │   │   │   ├── RegistroDiario.kt
│   │   │   │   ├── Alerta.kt
│   │   │   │   ├── Recomendacion.kt
│   │   │   │   └── CodigoHospital.kt
│   │   │   │
│   │   │   └── util/
│   │   │       └── IdentificadorUtils.kt
│   │   │
│   │   ├── res/
│   │   │   ├── layout/     34 layouts XML
│   │   │   ├── drawable/   78 recursos gráficos
│   │   │   ├── menu/       bottom_nav_menu.xml, menu_bottom_nav_hospital.xml
│   │   │   ├── color/      nav_item_color.xml, nav_hospital_color.xml
│   │   │   ├── values/     strings.xml, themes.xml, colors.xml
│   │   │   └── xml/        file_paths.xml, backup_rules.xml
│   │   │
│   │   └── AndroidManifest.xml
│   │
│   └── build.gradle.kts
│
├── supabase/
│   └── update_crear_perfil_v2.sql
│
├── docs/
│   ├── GUIA_TECNICA.md     ← este archivo
│   ├── ARQUITECTURA.md
│   ├── SUPABASE.md
│   ├── IDENTIFICACION.md
│   ├── TESTING.md
│   ├── CONTRIBUIR.md
│   └── historico/
│
└── README.md
```

---

## 4. Capa de presentación

### 4.1 Activities — Referencia completa

#### SplashActivity
- **Layout:** `activity_splash.xml`
- **Propósito:** Pantalla de carga inicial con delay de 2 segundos. Determina el destino según el estado de sesión y el perfil.
- **Llega desde:** Launcher del sistema
- **Navega a:** `PerfilActivity` (sin sesión), `EsperaAprobacionActivity` (pendiente), `PerfilActivity` (rechazado + logout), `HospitalMainActivity` (profesional aprobado), `PacienteMainActivity` (paciente aprobado)
- **Extras de entrada:** ninguno
- **Estado:** estable

#### PerfilActivity
- **Layout:** `activity_perfil.xml`
- **Propósito:** Selector de rol. Botón "Paciente" → `LoginActivity`; botón "Hospital/Profesional" → `LoginHospitalActivity`.
- **Llega desde:** `SplashActivity`, `AjustesHospitalFragment` (cerrar sesión)
- **Navega a:** `LoginActivity`, `LoginHospitalActivity`
- **Estado:** estable

#### LoginActivity
- **Layout:** `activity_login.xml`
- **Propósito:** Login de paciente. Campo CIPA-o-email + campo PIN. Botón de ayuda CIPA abre `dialog_ayuda_cipa.xml`. Link a `RegisterActivity`.
- **Navega a:** `PacienteMainActivity`, `EsperaAprobacionActivity`, `RegisterActivity`
- **Estado:** estable

#### RegisterActivity
- **Layout:** `activity_register.xml`
- **Propósito:** Registro de paciente. Campos: nombre, CIPA (10 dígitos), código hospital, PIN (6 dígitos), confirmación PIN, checkbox términos. Botón de ayuda muestra diálogo con imagen de tarjeta sanitaria.
- **Navega a:** `EsperaAprobacionActivity` (registro exitoso), `LoginActivity` (link)
- **Estado:** estable

#### LoginHospitalActivity
- **Layout:** `activity_login_hospital.xml`
- **Propósito:** Login de profesional. Campo colegiado-o-email + PIN. Botón de ayuda muestra `AlertDialog` explicativo del colegiado. Link a `RegistroHospitalActivity`.
- **Navega a:** `HospitalMainActivity`, `EsperaAprobacionActivity`, `RegistroHospitalActivity`
- **Estado:** estable

#### RegistroHospitalActivity
- **Layout:** `activity_registro_hospital.xml`
- **Propósito:** Registro de profesional. Campos: nombre, número de colegiado (6-10 dígitos), spinner de hospitales (cargado de Supabase), cargo (dropdown), teléfono, motivo de solicitud, PIN, confirmación PIN, checkbox privacidad.
- **Navega a:** `EsperaAprobacionActivity` (registro exitoso)
- **Estado:** estable

#### EsperaAprobacionActivity
- **Layout:** `activity_espera_aprobacion.xml`
- **Propósito:** Pantalla de espera para usuarios con `estado = 'pendiente'`. Botones: "Volver al inicio" → `PerfilActivity`; "Cerrar sesión" → logout + `PerfilActivity`.
- **Estado:** estable

#### HospitalMainActivity
- **Layout:** `activity_hospital_main.xml`
- **Propósito:** Contenedor del panel hospitalario. Gestiona el `BottomNavigationView` con 5 tabs y los Fragments correspondientes. Solicita permiso `POST_NOTIFICATIONS` en Android 13+.
- **launchMode:** `singleTop`
- **Navega a:** (recibe intents externos con `fragment_inicial` extra)
- **Método público:** `fun navegarA(tag: String)` — usado por Fragments y Activities externas
- **Estado:** estable

#### PacienteMainActivity
- **Layout:** `activity_paciente_main.xml`
- **Propósito:** Contenedor del panel paciente. Gestiona `BottomNavigationView` con 4 tabs. Solicita permiso `POST_NOTIFICATIONS` en Android 13+.
- **launchMode:** `singleTop`
- **Método público:** `fun navegarA(tag: String)`
- **Estado:** estable

#### AlertasActivity
- **Layout:** `activity_alertas.xml`
- **Propósito:** Centro de alertas y recomendaciones del paciente. Chips para filtrar entre "Mis Alertas" y "Recomendaciones". BottomSheet para detalle de recomendación. `BottomNavigationView` propio para volver al panel.
- **Llega desde:** `DashboardPacienteFragment` (campana), `PerfilPacienteFragment`
- **Navega a:** `PacienteMainActivity` (con extra `fragment_inicial`)
- **Estado:** estable

#### AsistenciaActivity
- **Layout:** `activity_asistencia.xml`
- **Propósito:** FAQ expandible (5 preguntas). Contacto con hospital (ACTION_DIAL, carga teléfono de Supabase). Soporte técnico (mailto). Política de privacidad y términos (ACTION_VIEW). Todas las intents con fallback seguro.
- **Estado:** estable

#### ConfiguracionNotificacionesActivity
- **Layout:** `activity_configuracion_notificaciones.xml`
- **Propósito:** 3 switches de notificaciones. "Alertas críticas" siempre ON y deshabilitado. "Recomendaciones" y "Recordatorios" configurables. Persiste en `SharedPreferences("preferencias_notif")`.
- **Estado:** estable

#### ConfirmacionSintomasActivity
- **Layout:** `activity_confirmacion_sintomas.xml`
- **Propósito:** Confirmación post-registro de síntomas. Muestra la fecha del reporte. Botones para volver al dashboard o ver historial.
- **Llega desde:** `RegistroDiarioFragment` (tras guardar)
- **Navega a:** `PacienteMainActivity` (con extra `fragment_inicial`)
- **Estado:** estable

#### SolicitudesPendientesActivity
- **Layout:** `activity_solicitudes_pendientes.xml`
- **Propósito:** Listado de perfiles en estado "pendiente" del hospital del profesional. Chips para filtrar entre pacientes y profesionales. Botones Aprobar/Rechazar por fila. Los pacientes van a `CrearFichaPacienteActivity` al aprobar; los profesionales se aprueban directamente.
- **Llega desde:** `DashboardHospitalFragment`
- **Navega a:** `CrearFichaPacienteActivity`
- **Estado:** estable

#### CrearFichaPacienteActivity
- **Layout:** `activity_crear_ficha_paciente.xml`
- **Propósito:** Formulario de ficha clínica. Campos: radiofármaco* (texto libre), dosis en MBq (opcional), fecha de tratamiento* (DatePicker), fecha de alta* (DatePicker), días de aislamiento (default 7), notas clínicas. Al guardar: llama `aprobar_perfil` + `crearFichaPaciente`.
- **Extras de entrada:** `perfil_id` (String), `nombre_paciente` (String)
- **Estado:** estable

#### DetallePacienteActivity
- **Layout:** `activity_detalle_paciente.xml`
- **Propósito:** Vista completa de la ficha de un paciente desde el panel hospitalario. Calcula días restantes de aislamiento. Badge de color según días (verde/naranja/rojo). Últimas 5 alertas del paciente.
- **Extras de entrada:** `paciente_id` (String)
- **Llega desde:** `PacientesHospitalFragment`
- **Estado:** estable

---

### 4.2 Fragments — Referencia completa

Los Fragments se instancian y gestionan por las Activities contenedoras (`HospitalMainActivity`, `PacienteMainActivity`). Cada Fragment oculta el `BottomNavigationView` de su layout en `onViewCreated` si el layout lo incluye (layouts reutilizados de las antiguas Activities).

#### DashboardPacienteFragment
- **Layout:** `activity_dashboard_paciente.xml`
- **Tab:** `nav_inicio` en `PacienteMainActivity`
- **Propósito:** Dashboard del paciente. Carga el perfil y los datos de la ficha. Calcula los días restantes de aislamiento y determina el estado de seguridad (SEGURO / PRECAUCIÓN / ALERTA). Carga la primera recomendación. En `onResume()` llama a `SyncNotificacionesManager.revisarYNotificar()`.
- **Navegación interna:** campana → `AlertasActivity`; avatar → `PerfilPacienteFragment`; tarjetas de ayuda → `AsistenciaActivity`; botón historial → `HistorialFragment`; botón síntomas → `RegistroDiarioFragment`

#### HistorialFragment
- **Layout:** `activity_historial.xml`
- **Tab:** `nav_historial`
- **Propósito:** Gráfica MPAndroidChart de los últimos 30 días con temperatura, fatiga y dolor. Tabla con registros completos ordenados por fecha descendente.

#### RegistroDiarioFragment
- **Layout:** `activity_registro_diario.xml`
- **Tab:** `nav_sintomas`
- **Propósito:** Formulario de reporte diario. Campos: temperatura (decimal), pulso (0-200), nivel de fatiga (slider 0-10), nivel de dolor (slider 0-10), síntomas (checkboxes: náuseas, fatiga, dolor, sequedad, vómitos), notas libres. Tras guardar, navega a `ConfirmacionSintomasActivity`.

#### PerfilPacienteFragment
- **Layout:** `activity_perfil_paciente.xml`
- **Tab:** `nav_perfil`
- **Propósito:** Muestra datos del perfil (nombre, email, rol). Filas de acceso a: Notificaciones → `ConfiguracionNotificacionesActivity`; Idioma → `AlertDialog` con opciones ES/EN (usa `AppCompatDelegate.setApplicationLocales`); Cambiar contraseña (funcionalidad básica); Cerrar sesión → `PerfilActivity` con `FLAG_ACTIVITY_CLEAR_TASK`.

#### DashboardHospitalFragment
- **Layout:** `activity_dashboard_hospital.xml`
- **Tab:** `nav_dashboard` en `HospitalMainActivity`
- **Propósito:** Saludo con nombre del profesional. 4 métricas (total pacientes, alertas activas, en seguimiento, altas). Últimas 3 alertas. Últimos 3 pacientes. Card de solicitudes pendientes con contador. En `onResume()` llama a `SyncNotificacionesManager.revisarYNotificar()`.
- **Navegación:** "Ver todas alertas" → `AlertasHospitalFragment`; card solicitudes → `SolicitudesPendientesActivity`

#### PacientesHospitalFragment
- **Layout:** `activity_pacientes_hospital.xml`
- **Propósito:** Lista de pacientes del hospital con `SearchView` de filtrado en tiempo real. Click → `DetallePacienteActivity`.

#### AlertasHospitalFragment
- **Layout:** `activity_alertas_hospital.xml`
- **Propósito:** Lista de alertas del hospital. Chips para filtrar Todas / Sin leer. Click → `BottomSheetDialog` con detalle y botón "Marcar como leída".

#### InformesHospitalFragment
- **Layout:** `activity_informes_hospital.xml`
- **Propósito:** Spinner de pacientes del hospital. Spinner de tipo de informe (4 opciones). DatePicker para rango (default: últimos 30 días). Botón "Generar informe" → preview con `BarChart` MPAndroidChart. Botones PDF y CSV (deshabilitados hasta generar preview).

#### AjustesHospitalFragment
- **Layout:** `activity_ajustes_hospital.xml`
- **Propósito:** Datos del profesional (nombre, rol). Cambio de PIN (diálogo con validación ≥ 6 caracteres). 2FA (diálogo informativo, sin implementación real). Centro de Ayuda (navegador). Soporte por correo. Switches de notificaciones hospitalarias. Umbrales de alerta (SharedPreferences `umbrales_alerta`). Cerrar sesión.

---

### 4.3 Activities contenedoras — Detalle técnico

#### HospitalMainActivity

```kotlin
// Método público para navegación desde Fragments y Activities externas
fun navegarA(tag: String)

// Tags válidos:
// "dashboard" → DashboardHospitalFragment
// "pacientes" → PacientesHospitalFragment
// "alertas"   → AlertasHospitalFragment
// "informes"  → InformesHospitalFragment
// "ajustes"   → AjustesHospitalFragment

// onNewIntent maneja la navegación desde Activities externas:
override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    intent.getStringExtra("fragment_inicial")?.let { tag -> navegarA(tag) }
}

// BackPressed vuelve siempre al dashboard:
override fun onBackPressed() {
    if (fragmentoActualTag != "dashboard") navegarA("dashboard")
    else super.onBackPressed()
}
```

**Flujo de permisos `POST_NOTIFICATIONS`:** se solicita una vez al inicio usando `registerForActivityResult(ActivityResultContracts.RequestPermission())`. La solicitud se marca en `SharedPreferences("preferencias_app")` para no repetirse.

#### PacienteMainActivity

Mismo patrón que `HospitalMainActivity` pero con 4 tabs: `dashboard`, `historial`, `sintomas`, `perfil`.

---

### 4.4 Diálogos personalizados

#### dialog_ayuda_cipa.xml
Usado en `LoginActivity` y `RegisterActivity`. Layout con `ScrollView` + explicación del CIPA + `ImageView` que referencia `@drawable/img_tarjeta_sanitaria` (imagen PNG del dorso de la tarjeta sanitaria).

---

## 5. Capa de control

### 5.1 AuthController

Gestiona la autenticación con Supabase Auth.

```kotlin
// Login de usuario existente
suspend fun login(email: String, password: String): Result<Unit>
// Llama a supabase.auth.signInWith(Email) { this.email = email; this.password = password }
// Si el perfil tiene estado "pendiente", lanza CuentaPendienteException

// Registro de paciente
suspend fun registrarPaciente(
    nombre: String,
    email: String,      // email ficticio: CIPA@paciente.theratrack.internal
    password: String,   // formato: "CIPA#PIN"
    codigoHospital: String,
    identificador: String = "",
    tipoIdentificador: String = "cipa"
): Result<Unit>
// 1. validar_codigo(codigoHospital) → hospitalId
// 2. supabase.auth.signUpWith(Email)
// 3. usar_codigo(codigoHospital)
// 4. crear_perfil(uid, nombre, "paciente", hospitalId, email, identificador, tipoIdentificador)

// Registro de profesional
suspend fun registrarProfesional(
    nombre: String,
    email: String,          // colegiado@medico.theratrack.internal
    password: String,
    hospitalId: String,
    cargo: String,
    telefono: String,
    motivoSolicitud: String,
    identificador: String = "",
    tipoIdentificador: String = "colegiado"
): Result<Unit>
// 1. supabase.auth.signUpWith(Email)
// 2. crear_perfil(uid, nombre, "profesional", hospitalId, email, cargo, telefono, motivoSolicitud, identificador, tipoIdentificador)

suspend fun logout(): Result<Unit>
// supabase.auth.signOut()

fun sesionActiva(): Boolean
// supabase.auth.currentSessionOrNull() != null

suspend fun obtenerPerfilActual(): Result<Perfil>
// SELECT * FROM perfiles WHERE id = auth.uid()

suspend fun obtenerRolActual(): Result<String>
// obtenerPerfilActual().map { it.rol }
```

**Excepción especial:**
```kotlin
class CuentaPendienteException : Exception("pendiente")
```
Lanzada desde `login()` cuando el perfil existe pero `estado == "pendiente"`. `LoginActivity` y `LoginHospitalActivity` la capturan para redirigir a `EsperaAprobacionActivity`.

---

### 5.2 PacienteController

Acceso a datos de pacientes, hospitales, alertas y recomendaciones.

```kotlin
// Datos del paciente actual logueado
suspend fun obtenerPacienteActual(): Result<Paciente>
suspend fun obtenerPerfilActual(): Result<Perfil>

// Hospitales
suspend fun obtenerHospitales(): Result<List<Hospital>>
suspend fun obtenerHospitalPorId(hospitalId: String): Result<Hospital>
suspend fun obtenerNombreHospital(hospitalId: String): Result<String>

// Pacientes del hospital (para panel hospitalario)
suspend fun obtenerPacientesHospital(hospitalId: String): Result<List<Paciente>>
suspend fun obtenerPacientePorId(pacienteId: String): Result<Paciente>
suspend fun obtenerPerfilPorId(perfilId: String): Result<Perfil>

// Alertas
suspend fun obtenerAlertasPaciente(pacienteId: String): Result<List<Alerta>>
suspend fun obtenerAlertasPacienteDesde(pacienteId: String, desdeIso: String): Result<List<Alerta>>
suspend fun obtenerAlertasPacienteEnRango(pacienteId: String, desde: String, hasta: String): Result<List<Alerta>>
suspend fun obtenerTodasAlertasHospital(hospitalId: String): Result<List<Alerta>>
suspend fun obtenerAlertasHospitalDesde(hospitalId: String, desdeIso: String): Result<List<Alerta>>
suspend fun obtenerAlertasNoLeidasPacientes(pacienteIds: List<String>): Result<List<Alerta>>
suspend fun marcarAlertaLeida(alertaId: String): Result<Unit>

// Recomendaciones
suspend fun obtenerRecomendaciones(hospitalId: String): Result<List<Recomendacion>>
suspend fun obtenerRecomendacionesPaciente(hospitalId: String): Result<List<Recomendacion>>
suspend fun obtenerRecomendacionesPacienteDesde(hospitalId: String, pacienteId: String, desdeIso: String): Result<List<Recomendacion>>
suspend fun marcarRecomendacionLeida(id: String): Result<Unit>

// Sesión
suspend fun cerrarSesion()
```

---

### 5.3 RegistroController

Gestiona los registros diarios de síntomas.

```kotlin
// Registros de los últimos N días
suspend fun obtenerRegistros(pacienteId: String, diasAtras: Int): Result<List<RegistroDiario>>

// Registros en un rango de fechas (formato: "yyyy-MM-dd")
suspend fun obtenerRegistrosRango(pacienteId: String, desde: String, hasta: String): Result<List<RegistroDiario>>

// Guardar un nuevo registro (INSERT)
suspend fun guardarRegistro(registro: RegistroDiario): Result<Unit>
```

---

### 5.4 AdminController

Gestiona aprobaciones, rechazos y creación de fichas clínicas.

```kotlin
// Perfiles en estado "pendiente" del hospital del profesional
suspend fun obtenerSolicitudesPendientes(hospitalId: String): Result<List<Perfil>>

// Aprueba un perfil (llama RPC aprobar_perfil)
suspend fun aprobarSolicitud(perfilId: String): Result<Unit>

// Rechaza un perfil (llama RPC rechazar_perfil)
suspend fun rechazarSolicitud(perfilId: String): Result<Unit>

// Crea la ficha clínica del paciente (INSERT en tabla pacientes + aprobar_perfil)
suspend fun crearFichaPaciente(
    perfilId: String,
    radiofarmaco: String,
    dosisMbq: Double?,
    fechaTratamiento: String,  // "yyyy-MM-dd"
    fechaAlta: String,         // "yyyy-MM-dd"
    diasAislamiento: Int = 7,
    notasClinicas: String? = null
): Result<Unit>
```

---

### 5.5 PdfReportGenerator

Genera informes clínicos en formato PDF usando la API nativa de Android.

```kotlin
suspend fun generarInformeClinico(
    context: Context,
    paciente: Paciente,
    perfilPaciente: Perfil,
    registros: List<RegistroDiario>,
    alertas: List<Alerta>,
    recomendaciones: List<Recomendacion>,
    medicoResponsable: String,
    rangoDesde: String,
    rangoHasta: String
): Result<File>
```

**Características técnicas:**
- API: `android.graphics.pdf.PdfDocument` (sin dependencias externas)
- Tamaño de página: A4 (595 × 842 px a 72 dpi)
- Directorio de salida: `context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)`
- Nombre del archivo: `informe_{8_chars_pacienteId}_{timestamp}.pdf`
- Compartición: mediante `FileProvider` con autoridad `${applicationId}.fileprovider`
- El `PdfDocument` se cierra en bloque `finally` para garantizar liberación de recursos
- Contenido del PDF:
  - Header azul (#1A56DB) con referencia TT-{año}-{iniciales}-{correlativo}
  - Datos del paciente y periodo del informe
  - Gráfica de barras (Canvas nativo) con últimos 14 registros de dolor/fatiga
  - Tabla de alertas con tipo y fecha
  - Resumen de síntomas por frecuencia
  - Recomendaciones emitidas
  - Paginación automática con footer de número de página

---

### 5.6 CsvExporter

Exporta los registros diarios de un paciente a formato CSV.

```kotlin
suspend fun exportarRegistros(
    context: Context,
    paciente: Paciente,
    registros: List<RegistroDiario>
): Result<File>
```

**Características técnicas:**
- Cabecera: `fecha,temperatura,pulso,nivel_fatiga,nivel_dolor,sintomas,notas`
- Valores con comas o saltos de línea se encierran entre comillas dobles
- Comillas dobles internas se escapan como `""`
- Directorio: `context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)`
- Nombre: `registros_{8_chars_pacienteId}_{timestamp}.csv`

---

### 5.7 NotificacionesManager

Gestiona la creación de canales y el envío de notificaciones locales.

```kotlin
// Llamado desde MainApplication.onCreate() — crea los 3 canales Android 8+
fun crearCanales(context: Context)

// Muestra notificación de alerta clínica (ignorada por preferencias, siempre se muestra)
fun mostrarNotificacionAlerta(context: Context, alerta: Alerta)

// Muestra notificación de recomendación (respeta preferencia "notif_recomendaciones")
fun mostrarNotificacionRecomendacion(context: Context, rec: Recomendacion)

// Muestra recordatorio de registro diario (respeta preferencia "notif_recordatorios")
// Target: PacienteMainActivity con extra "fragment_inicial"="sintomas"
fun mostrarRecordatorioSintomas(context: Context)

// Cancela todas las notificaciones activas
fun cancelarTodas(context: Context)
```

**Canales de notificación:**

| ID | Nombre | Importancia | Vibración | Configurable |
|---|---|---|---|---|
| `alertas_criticas` | Alertas críticas | HIGH | sí | no (siempre activo) |
| `recomendaciones` | Recomendaciones | DEFAULT | no | sí |
| `recordatorios` | Recordatorios | LOW | no | sí |

**Preferencias de notificación** (`SharedPreferences("preferencias_notif")`):

| Clave | Tipo | Default | Efecto |
|---|---|---|---|
| `notif_recomendaciones` | Boolean | true | Bloquea recomendaciones si false |
| `notif_recordatorios` | Boolean | true | Bloquea recordatorios si false |

> Las alertas críticas nunca se bloquean por preferencias de usuario.

**PendingIntent:** cada notificación incluye un `PendingIntent` con `FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT` que abre la Activity correspondiente. Para recordatorios, abre `PacienteMainActivity` con `putExtra("fragment_inicial", "sintomas")`.

---

### 5.8 SyncNotificacionesManager

Implementa el polling de notificaciones al abrir los dashboards.

```kotlin
// Llamado en onResume() de DashboardPacienteFragment y DashboardHospitalFragment
suspend fun revisarYNotificar(context: Context)
```

**Lógica:**
1. Obtiene el perfil actual. Si falla, no hace nada.
2. Lee la marca de tiempo de la última revisión de `SharedPreferences` con clave `ultima_revision_iso_{perfil.id}` (por usuario, no global).
3. Si es paciente: busca alertas desde `desdeIso` → `mostrarNotificacionAlerta()` para cada alerta nueva. Busca recomendaciones desde `desdeIso` → `mostrarNotificacionRecomendacion()`.
4. Si es profesional: busca alertas críticas no leídas de pacientes del hospital desde `desdeIso`.
5. Guarda la nueva marca de tiempo `now()` en `SharedPreferences`.

**Por qué polling y no WorkManager:** sin un servidor propio que gestione push, WorkManager añadiría complejidad y restricciones de batería sin mejorar la experiencia. El polling al abrir la app es suficiente para el contexto académico.

---

## 6. Capa de datos (Models)

Todos los models son `@Serializable` con `kotlinx.serialization`.

### Perfil

```kotlin
@Serializable
data class Perfil(
    val id: String,
    val nombre: String,
    val rol: String,                                    // "paciente" | "profesional"
    @SerialName("hospital_id") val hospitalId: String? = null,
    val estado: String = "pendiente",                  // "pendiente" | "aprobado" | "rechazado"
    val email: String? = null,
    val telefono: String? = null,
    val cargo: String? = null,
    @SerialName("motivo_solicitud") val motivoSolicitud: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
```

### Paciente

```kotlin
@Serializable
data class Paciente(
    val id: String,                                         // = perfiles.id
    @SerialName("hospital_id") val hospitalId: String,
    val radiofarmaco: String,
    @SerialName("dosis_mbq") val dosisMbq: Double? = null,
    @SerialName("fecha_tratamiento") val fechaTratamiento: String,  // yyyy-MM-dd
    @SerialName("fecha_alta") val fechaAlta: String,                // yyyy-MM-dd
    @SerialName("dias_aislamiento") val diasAislamiento: Int = 7,
    @SerialName("notas_clinicas") val notasClinicas: String? = null,
    @SerialName("creado_por") val creadoPor: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
```

### Hospital

```kotlin
@Serializable
data class Hospital(
    val id: String,
    val nombre: String,
    val direccion: String? = null,
    val telefono: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
```

### RegistroDiario

```kotlin
@Serializable
data class RegistroDiario(
    val id: String? = null,
    @SerialName("paciente_id") val pacienteId: String,
    val fecha: String,                                   // yyyy-MM-dd
    val temperatura: Double? = null,
    val pulso: Int? = null,                              // 0-200
    @SerialName("nivel_fatiga") val nivelFatiga: Int? = null,    // 0-10
    @SerialName("nivel_dolor") val nivelDolor: Int? = null,      // 0-10
    val sintomas: String? = null,
    val notas: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
```

### Alerta

```kotlin
@Serializable
data class Alerta(
    val id: String? = null,
    @SerialName("paciente_id") val pacienteId: String,
    val titulo: String,
    val mensaje: String,
    val tipo: String = "info",              // "info" | "critica" | "alerta"
    val leida: Boolean = false,
    @SerialName("registro_diario_id") val registroDiarioId: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
```

### Recomendacion

```kotlin
@Serializable
data class Recomendacion(
    val id: String? = null,
    @SerialName("hospital_id") val hospitalId: String? = null,
    @SerialName("paciente_id") val pacienteId: String? = null,
    val titulo: String,
    @SerialName("contenido") val descripcion: String,   // columna BD: "contenido"
    val tipo: String,
    val prioridad: Int = 0,
    val leida: Boolean = false,
    @SerialName("creado_por") val creadoPor: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
```

### CodigoHospital

```kotlin
@Serializable
data class CodigoHospital(
    val id: String,
    @SerialName("hospital_id") val hospitalId: String,
    val codigo: String,                     // ej: "THERA-0001"
    val usado: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)
```

---

## 7. Sistema de identificación

Ver [IDENTIFICACION.md](IDENTIFICACION.md) para la referencia completa. Resumen:

- Paciente: **CIPA** (10 dígitos) → email `CIPA@paciente.theratrack.internal` + password `CIPA#PIN`
- Profesional: **colegiado** (6-10 dígitos) → email `colegiado@medico.theratrack.internal` + password `colegiado#PIN`
- Login acepta identificador directo o email completo (normalizado por `IdentificadorUtils.normalizarAEmail()`)

---

## 8. Flujos principales

### 8.1 Registro de paciente

```
RegisterActivity
    │
    ├── validarCipa(cipa)         — 10 dígitos numéricos
    ├── validarCampos()           — PIN 6 dígitos, nombre no vacío, términos aceptados
    │
    └── AuthController.registrarPaciente(
            nombre,
            cipaToEmail(cipa),        → "1234567890@paciente.theratrack.internal"
            generarPassword(cipa,pin),→ "1234567890#123456"
            codigoHospital,
            cipa, "cipa"
        )
        │
        ├── RPC validar_codigo(codigoHospital) → hospitalId (o error)
        ├── Supabase Auth signUp(email, password) → uid
        ├── RPC usar_codigo(codigoHospital)
        └── RPC crear_perfil(uid, nombre, "paciente", hospitalId, email, cipa, "cipa")
            → perfiles.estado = "pendiente"
```

### 8.2 Aprobación y ficha clínica

```
Profesional en SolicitudesPendientesActivity
    │
    └── tap "Aprobar" en paciente pendiente
        │
        └── Abre CrearFichaPacienteActivity (con perfilId, nombre)
            │
            └── guardar formulario
                │
                ├── AdminController.crearFichaPaciente(perfilId, radiofarmaco, ...)
                │   └── INSERT INTO pacientes (...)
                └── AdminController.aprobarSolicitud(perfilId)
                    └── RPC aprobar_perfil(perfilId)
                        → perfiles.estado = "aprobado"
```

### 8.3 Reporte de síntomas

```
RegistroDiarioFragment
    │
    └── tap "Guardar registro"
        │
        └── RegistroController.guardarRegistro(RegistroDiario(
                pacienteId = auth.uid(),
                fecha = hoy,
                temperatura, pulso, nivelFatiga, nivelDolor, sintomas, notas
            ))
            │
            └── INSERT INTO registros_diarios (...)
                → navega a ConfirmacionSintomasActivity
```

### 8.4 Generación de informe PDF

```
InformesHospitalFragment
    │
    ├── cargar pacientes del hospital (PacienteController.obtenerPacientesHospital)
    ├── usuario selecciona paciente + rango de fechas
    ├── tap "Generar informe"
    │   ├── RegistroController.obtenerRegistrosRango(pacienteId, desde, hasta)
    │   ├── PacienteController.obtenerAlertasPacienteEnRango(...)
    │   └── mostrar preview + habilitar botones PDF/CSV
    │
    └── tap "Descargar PDF"
        │
        └── PdfReportGenerator.generarInformeClinico(
                context, paciente, perfilPaciente,
                registros, alertas, recomendaciones,
                medicoResponsable, desde, hasta
            )
            │
            └── File → FileProvider → Intent(ACTION_VIEW) → visor PDF externo
```

---

## 9. Compilación y ejecución

### 9.1 Comandos útiles

```bash
# Build APK de debug
./gradlew assembleDebug

# Instalar directamente en emulador/dispositivo conectado
./gradlew installDebug

# Limpiar caché de build
./gradlew clean

# Ver dependencias del módulo app
./gradlew app:dependencies

# Ver tareas disponibles
./gradlew tasks
```

### 9.2 Build en terminal (JAVA_HOME requerido)

```powershell
# Windows — una línea
$env:JAVA_HOME = "C:\Users\roger\.p2\pool\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.7.v20250502-0916\jre"; ./gradlew assembleDebug
```

### 9.3 Versiones del proyecto

```kotlin
// app/build.gradle.kts
compileSdk = 36
minSdk = 24
targetSdk = 36
versionCode = 1
versionName = "1.0"
```

### 9.4 Dependencias principales

```kotlin
// Supabase BOM (gestiona versiones de módulos Supabase)
implementation(platform(libs.supabase.bom))
implementation(libs.supabase.postgrest)
implementation(libs.supabase.auth)
implementation(libs.supabase.realtime)

// Ktor (requerido por Supabase SDK)
implementation(libs.ktor.android)

// Coroutines
implementation(libs.coroutines.android)

// Lifecycle (lifecycleScope en Activities)
implementation(libs.lifecycle.runtime)

// Gráficas
implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

// Material Components
implementation(libs.material)
```

### 9.5 Variables de entorno y secretos

La URL y la anon key de Supabase están actualmente hardcodeadas en `SupabaseClient.kt`.

> Para producción, mover a `local.properties`:
> ```properties
> supabase.url=https://xxx.supabase.co
> supabase.key=TU_ANON_KEY
> ```
> Y leer desde `BuildConfig` generando campos en `build.gradle.kts`:
> ```kotlin
> buildConfigField("String", "SUPABASE_URL", "\"${localProperties["supabase.url"]}\"")
> ```

---

## 10. Convenciones de código

- **Idioma:** español para comentarios y mensajes de UI; nombres técnicos en inglés según estándar Android
- **Estructura suspend fun:** siempre `runCatching { ... }` en Controllers; `viewLifecycleOwner.lifecycleScope.launch` en Fragments
- **Views:** `findViewById<Tipo>(R.id.nombre)` — sin ViewBinding (deuda técnica conocida)
- **Serialización:** `@Serializable` + `@SerialName` para mapear nombres de columnas de BD
- **Colores en código:** `requireContext().getColor(R.color.nombre)` — no usar literales hexadecimales

---

## 11. Limitaciones conocidas y deuda técnica

| Limitación | Impacto | Plan |
|---|---|---|
| `findViewById` sin ViewBinding | Más código, posibles null en vistas | Migrar a ViewBinding progresivamente |
| 2FA como stub | Seguridad adicional no disponible | Implementar TOTP en próxima versión |
| Notificaciones locales (sin FCM) | Solo notifica al abrir la app | FCM requiere servidor propio |
| Strings en inglés incompletos | Cambio de idioma muestra strings en español | Completar `strings-en.xml` |
| PDF sin firma digital | Informes no firmados digitalmente | Integrar firma en PDF con PKCS#7 |
| Supabase credentials en código | Riesgo si el repo es público | Mover a `local.properties` + BuildConfig |
| `sdk.dir` en `local.properties` apunta al JRE | Configurado por error; Android Studio lo corrige automáticamente | Corregir manualmente si falla la build |

---

## 12. Roadmap futuro

1. **ViewBinding** — eliminar `findViewById` en todas las Activities y Fragments
2. **FCM (Firebase Cloud Messaging)** — notificaciones push reales
3. **i18n completa** — `strings-en.xml` completo; soporte catalán
4. **Tests automatizados** — JUnit para Controllers, Espresso para flujos UI críticos
5. **Firma digital en PDF** — PKCS#7 o similar para informes oficiales
6. **2FA real** — TOTP con Google Authenticator o similar
7. **WorkManager para recordatorios** — recordatorios programados sin abrir la app
8. **Modo offline** — caché local de registros con Room o similar
