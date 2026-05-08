# Arquitectura — TheraTrack Home

## 1. Patrón arquitectónico

TheraTrack Home implementa una variante de **MVC (Model-View-Controller)** adaptada al ecosistema Android nativo con Kotlin:

| Capa | Componentes | Responsabilidad |
|---|---|---|
| **Model** | `model/*.kt` | Data classes serializables que mapean tablas de Supabase |
| **View** | Activities, Fragments, layouts XML | Presentación y captura de input del usuario |
| **Controller** | `controller/*.kt` | Lógica de negocio, validaciones, comunicación con Supabase |

No se usa MVVM ni LiveData. Las operaciones asíncronas se gestionan con **Kotlin Coroutines** (`lifecycleScope` en Activities/Fragments, `viewLifecycleOwner.lifecycleScope` en Fragments).

---

## 2. Diagrama de capas

```
┌─────────────────────────────────────────────────────────────────┐
│                    CAPA DE PRESENTACIÓN                         │
│                                                                 │
│  Activities (flujos lineales)      Fragments (dentro BottomNav) │
│  ┌──────────────────────────┐     ┌───────────────────────────┐ │
│  │ SplashActivity           │     │ DashboardPacienteFragment │ │
│  │ PerfilActivity           │     │ HistorialFragment         │ │
│  │ LoginActivity            │     │ RegistroDiarioFragment    │ │
│  │ RegisterActivity         │     │ PerfilPacienteFragment    │ │
│  │ LoginHospitalActivity    │     │ DashboardHospitalFragment │ │
│  │ RegistroHospitalActivity │     │ PacientesHospitalFragment │ │
│  │ EsperaAprobacionActivity │     │ AlertasHospitalFragment   │ │
│  │ HospitalMainActivity ◄───┼─────│ InformesHospitalFragment  │ │
│  │ PacienteMainActivity ◄───┼─────│ AjustesHospitalFragment   │ │
│  │ AlertasActivity          │     └───────────────────────────┘ │
│  │ AsistenciaActivity       │                                   │
│  │ ConfiguracionNotif...    │                                   │
│  │ ConfirmacionSintomas...  │                                   │
│  │ SolicitudesPendientes... │                                   │
│  │ CrearFichaPaciente...    │                                   │
│  │ DetallePacienteActivity  │                                   │
│  └──────────────────────────┘                                   │
└────────────────────────────┬────────────────────────────────────┘
                             │ suspend fun / Result<T>
┌────────────────────────────▼────────────────────────────────────┐
│                    CAPA DE CONTROL                               │
│                                                                 │
│  AuthController          → Auth: login, registro, logout        │
│  PacienteController      → Pacientes, alertas, recomendaciones  │
│  RegistroController      → Registros diarios de síntomas        │
│  AdminController         → Aprobación, fichas clínicas          │
│  NotificacionesManager   → Canales y notificaciones locales     │
│  SyncNotificacionesManager → Polling de alertas al abrir app   │
│  PdfReportGenerator      → Generación de informes PDF nativo    │
│  CsvExporter             → Exportación a CSV                    │
│  SupabaseClient          → Singleton con el cliente HTTP        │
└────────────────────────────┬────────────────────────────────────┘
                             │ PostgREST / Auth API (HTTPS)
┌────────────────────────────▼────────────────────────────────────┐
│                    BACKEND (Supabase)                            │
│                                                                 │
│  Auth (gotrue)           → Usuarios, sesiones, tokens           │
│  PostgREST               → CRUD sobre tablas PostgreSQL         │
│  Row Level Security      → Control de acceso por rol y estado   │
│  RPCs (plpgsql)          → validar_codigo, crear_perfil, etc.   │
│                                                                 │
│  Tablas:                                                        │
│  hospitales · codigos_hospital · perfiles · pacientes           │
│  registros_diarios · alertas · recomendaciones                  │
└─────────────────────────────────────────────────────────────────┘
```

---

## 3. Flujo de datos típico

```
Usuario toca botón en Fragment
        │
        ▼
Fragment llama a Controller:
  lifecycleScope.launch {
      val result = PacienteController.obtenerPacienteActual()
      result.onSuccess { ... }
             .onFailure { ... }
  }
        │
        ▼
Controller construye query PostgREST:
  SupabaseClient.client.postgrest["pacientes"]
      .select { filter { eq("id", uid) } }
        │
        ▼
Supabase evalúa RLS + devuelve JSON
        │
        ▼
Controller deserializa a data class y devuelve Result<T>
        │
        ▼
Fragment actualiza UI en el hilo principal
```

Todos los controladores devuelven `Result<T>` de Kotlin, encapsulando éxito o fallo. Ningún controller lanza excepciones no capturadas al caller; las excepciones se capturan con `runCatching { ... }`.

---

## 4. Diagrama de navegación

### 4.1 Flujo de entrada

```
SplashActivity (2s)
    │
    ├── sesionActiva() == false ──────────────────► PerfilActivity
    │                                                   │
    │                                           ┌───────┴────────┐
    │                                           │                │
    │                                    LoginActivity   LoginHospitalActivity
    │                                           │                │
    │                               RegisterActivity  RegistroHospitalActivity
    │
    ├── perfil.estado == "pendiente" ──────────► EsperaAprobacionActivity
    │
    ├── perfil.estado == "rechazado" ──► logout ► PerfilActivity
    │
    ├── perfil.rol == "profesional" ───────────► HospitalMainActivity
    │
    └── perfil.rol == "paciente" ───────────────► PacienteMainActivity
```

### 4.2 Panel paciente (PacienteMainActivity)

```
PacienteMainActivity (launchMode=singleTop)
  BottomNavigationView (4 tabs)
    ├── nav_inicio    → DashboardPacienteFragment
    │       ├── campana  → AlertasActivity (Activity independiente)
    │       ├── avatar   → PerfilPacienteFragment (via navegarA("perfil"))
    │       └── tarjetas → AsistenciaActivity (Activity independiente)
    ├── nav_historial → HistorialFragment
    ├── nav_sintomas  → RegistroDiarioFragment
    │       └── guardar → ConfirmacionSintomasActivity (Activity independiente)
    └── nav_perfil    → PerfilPacienteFragment
            ├── Notificaciones → ConfiguracionNotificacionesActivity
            └── Idioma         → Dialog (in-place)
```

### 4.3 Panel hospitalario (HospitalMainActivity)

```
HospitalMainActivity (launchMode=singleTop)
  BottomNavigationView (5 tabs)
    ├── nav_dashboard → DashboardHospitalFragment
    │       ├── ver alertas → AlertasHospitalFragment (via navegarA("alertas"))
    │       └── solicitudes → SolicitudesPendientesActivity (Activity independiente)
    │               └── aprobar paciente → CrearFichaPacienteActivity
    ├── nav_pacientes → PacientesHospitalFragment
    │       └── click paciente → DetallePacienteActivity
    ├── nav_alertas   → AlertasHospitalFragment
    │       └── click alerta → BottomSheet (in-place)
    ├── nav_informes  → InformesHospitalFragment
    │       ├── generar → preview inline
    │       ├── PDF     → FileProvider + Intent chooser
    │       └── CSV     → FileProvider + Intent chooser
    └── nav_ajustes   → AjustesHospitalFragment
            └── cerrar sesión → LoginHospitalActivity
```

### 4.4 Navegación entre Activities y Fragments

Las Activities independientes (AlertasActivity, AsistenciaActivity, etc.) navegan de vuelta al panel usando:

```kotlin
Intent(this, PacienteMainActivity::class.java).apply {
    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
    putExtra("fragment_inicial", "dashboard")  // o "historial", "sintomas", "perfil"
}
```

`PacienteMainActivity.onNewIntent()` recoge el extra y llama a `navegarA(tag)`.

Los Fragments navegan entre sí mediante el casting:

```kotlin
(requireActivity() as PacienteMainActivity).navegarA("historial")
(requireActivity() as HospitalMainActivity).navegarA("alertas")
```

---

## 5. Diagrama de tablas Supabase

```
hospitales
  ├── id (UUID, PK)
  ├── nombre
  ├── direccion
  └── telefono

codigos_hospital
  ├── id (UUID, PK)
  ├── hospital_id → hospitales.id
  ├── codigo       (ej. "THERA-0001")
  └── usado        (boolean)

perfiles
  ├── id (UUID, PK = auth.users.id)
  ├── nombre
  ├── rol           ("paciente" | "profesional")
  ├── hospital_id → hospitales.id
  ├── estado        ("pendiente" | "aprobado" | "rechazado")
  ├── email
  ├── cargo
  ├── telefono
  ├── motivo_solicitud
  ├── identificador      (CIPA o colegiado, guardado en v2)
  ├── tipo_identificador ("cipa" | "colegiado")
  └── created_at

pacientes
  ├── id (UUID, PK = perfiles.id del paciente)
  ├── hospital_id → hospitales.id
  ├── radiofarmaco
  ├── dosis_mbq
  ├── fecha_tratamiento  (yyyy-MM-dd)
  ├── fecha_alta         (yyyy-MM-dd)
  ├── dias_aislamiento
  ├── notas_clinicas
  └── creado_por → perfiles.id (profesional que creó la ficha)

registros_diarios
  ├── id (UUID, PK)
  ├── paciente_id → pacientes.id
  ├── fecha          (yyyy-MM-dd)
  ├── temperatura
  ├── pulso
  ├── nivel_fatiga   (0-10)
  ├── nivel_dolor    (0-10)
  ├── sintomas       (texto libre)
  └── notas

alertas
  ├── id (UUID, PK)
  ├── paciente_id → pacientes.id
  ├── titulo
  ├── mensaje
  ├── tipo     ("info" | "critica" | "alerta")
  ├── leida    (boolean)
  └── created_at

recomendaciones
  ├── id (UUID, PK)
  ├── hospital_id → hospitales.id
  ├── paciente_id → pacientes.id
  ├── titulo
  ├── contenido      (campo BD; mapeado como "descripcion" en data class)
  ├── tipo           ("descanso" | "medicacion" | "dieta" | "ejercicio")
  ├── prioridad      (int)
  ├── leida          (boolean)
  └── creado_por → perfiles.id
```

---

## 6. Decisiones arquitectónicas justificadas

### Por qué Kotlin + XML y no Compose
El equipo ya tenía experiencia con Views XML y el proyecto comenzó con esta base. Compose añadiría una curva de aprendizaje sin beneficio inmediato para el alcance académico.

### Por qué MVC y no MVVM
MVVM con ViewModel y LiveData añade complejidad (observadores, estado de UI, inyección de dependencias). Para el alcance del proyecto, el patrón MVC directo (Controller → View sincrónicamente en coroutines) es suficiente y más legible para el equipo.

### Por qué Supabase y no Firebase
Supabase ofrece PostgreSQL real con SQL estándar, RLS declarativa y RPCs en plpgsql. Facilita el control fino de acceso por rol sin depender de reglas propietarias de Firestore.

### Por qué notificaciones locales y no FCM
FCM requiere configuración de un servidor de mensajería, un servicio push y certificados. Para el entorno académico sin servidor propio, las notificaciones locales con polling al abrir la app son suficientes y eliminan dependencias externas.

### Por qué `findViewById` y no ViewBinding
Decisión de simplicidad en la etapa inicial. ViewBinding es preferible y se puede migrar sin cambios de arquitectura: es deuda técnica conocida y anotada en el roadmap.

### Por qué `launchMode="singleTop"` en las Main Activities
Evita crear instancias apiladas de `HospitalMainActivity` o `PacienteMainActivity` cuando una Activity independiente (ej. `AlertasActivity`) navega de vuelta al panel. Con `FLAG_ACTIVITY_SINGLE_TOP`, la instancia existente recibe `onNewIntent()` y simplemente cambia de Fragment, sin recrear la Activity entera.
