# TheraTrack Home — App Android

Aplicación móvil de seguimiento post-tratamiento con radiofármacos para pacientes en aislamiento domiciliario. Permite a los pacientes registrar síntomas y consultar pautas de aislamiento, y al personal hospitalario monitorizar el estado de sus pacientes desde un panel dedicado.

---

## Equipo

| Nombre | Rol |
|---|---|
| Daniel Builes | Desarrollo Android / Backend Supabase |
| Andrea Caballero | Diseño UI/UX / Desarrollo Android |
| Roger Colón | Desarrollo Android / Integración Supabase |

**Titulación:** 2º DAM — Universidad Europea  
**Cliente:** Estudiantes de Imagen para el Diagnóstico y Medicina Nuclear

---

## Stack tecnológico

| Tecnología | Uso |
|---|---|
| **Kotlin** | Lenguaje principal Android |
| **XML** | Layouts de interfaz |
| **Supabase** | Base de datos PostgreSQL + Auth (JWT) |
| **MPAndroidChart** | Gráficas de evolución de síntomas |
| **Material Components** | Componentes de UI (BottomNav, Cards…) |
| **Kotlinx Coroutines** | Llamadas asíncronas a Supabase |
| **Kotlinx Serialization** | Deserialización de respuestas JSON |

---

## Arquitectura

El proyecto sigue el patrón **MVC** (Model-View-Controller):

```
app/src/main/java/com/example/theratrackhome/
│
├── model/               # Data classes serializables (Paciente, Alerta, Perfil…)
├── controller/          # Lógica de negocio y llamadas a Supabase (PacienteController)
├── network/             # Cliente Supabase singleton (SupabaseClient)
│
├── LoginActivity.kt              # Login paciente
├── RegisterActivity.kt           # Registro paciente
├── SplashActivity.kt             # Splash + redirección según rol
├── DashboardPacienteActivity.kt  # Panel principal del paciente
├── RegistroDiarioActivity.kt     # Registro de síntomas diario
├── ConfirmacionSintomasActivity.kt
├── HistorialActivity.kt          # Historial de registros
├── AlertasActivity.kt            # Alertas del paciente
├── AsistenciaActivity.kt         # Pautas de asistencia
├── PerfilActivity.kt             # Perfil usuario
├── PerfilPacienteActivity.kt
│
├── LoginHospitalActivity.kt      # Login personal hospitalario
├── BaseHospitalActivity.kt       # Clase base con BottomNav hospitalario
├── DashboardHospitalActivity.kt  # Panel hospitalario principal
├── PacientesHospitalActivity.kt  # Lista de pacientes con búsqueda
├── DetallePacienteActivity.kt    # Ficha completa de un paciente
├── AlertasHospitalActivity.kt    # Lista de alertas con filtros
├── InformesHospitalActivity.kt   # Informes (stub — en desarrollo)
└── AjustesHospitalActivity.kt    # Ajustes + cerrar sesión
```

---

## Configuración de Supabase

**URL del proyecto:** `https://ntgstwgrmxmnfpttiuby.supabase.co`

El `supabaseKey` del archivo `SupabaseClient.kt` es la **clave pública (anon key)**: es seguro tenerla en el código fuente ya que el acceso está controlado por las políticas RLS de Supabase.

### Para un compañero que clone el repositorio

1. Clona el repo y abre en Android Studio.
2. El archivo `app/src/main/java/com/example/theratrackhome/network/SupabaseClient.kt` ya contiene la URL y clave correctas — no necesitas cambiar nada para conectarte al proyecto compartido.
3. Si quieres usar tu propio proyecto Supabase, crea uno en [supabase.com](https://supabase.com), copia tu URL y anon key, y actualiza `SupabaseClient.kt`.
4. Asegúrate de crear las mismas tablas (ver sección siguiente) y habilitar Row Level Security según las políticas del proyecto original.

---

## Tablas de Supabase

| Tabla | Descripción |
|---|---|
| `perfiles` | Datos básicos de usuario (nombre, rol: `paciente` / `profesional`, hospital_id) |
| `pacientes` | Datos clínicos del paciente (radiofármaco, dosis_mbq, fechas, días de aislamiento, notas) |
| `hospitales` | Catálogo de hospitales (id, nombre) |
| `alertas` | Alertas generadas por pacientes hacia el hospital (título, mensaje, tipo, leida) |
| `registros_diarios` | Registros de síntomas diarios del paciente (fecha, síntomas en JSON, notas) |
| `recomendaciones` | Pautas de aislamiento por hospital (texto, categoría) |
| `mediciones` | Mediciones de radiactividad doméstica del paciente (valor, unidad, fecha) |

---

## Estado del proyecto

### COMPLETADO ✅

**Panel del paciente:**
- Login y registro de paciente con Supabase Auth
- SplashActivity con redirección automática según rol (paciente / profesional)
- Dashboard del paciente con métricas de aislamiento
- Registro diario de síntomas
- Confirmación de síntomas
- Historial de registros diarios
- Pantalla de alertas del paciente
- Asistencia / pautas de aislamiento
- Perfil del paciente

**Panel hospitalario:**
- Login del personal hospitalario con código de hospital
- Dashboard hospitalario con métricas (total pacientes, alertas activas, en seguimiento, altas)
- Lista de pacientes con barra de búsqueda en tiempo real
- Ficha de detalle de paciente (datos clínicos + alertas recientes)
- Lista completa de alertas con filtro "Todas / Sin leer"
- Ajustes hospitalarios con cierre de sesión

**Infraestructura:**
- Integración completa con Supabase (Auth + PostgREST)
- Arquitectura MVC con PacienteController
- BaseHospitalActivity con navegación inferior compartida
- Sistema de colores y drawables consistente

### EN STUB / PENDIENTE ⚠️

| Pantalla | Estado | Notas |
|---|---|---|
| `InformesHospitalActivity` | Stub visible (navega correctamente) | Falta diseño y lógica de gráficas |
| Gráficas de evolución | No iniciado | Usar MPAndroidChart ya incluido |
| Marcar alertas como leídas | Función en controller, no conectada a UI | `PacienteController.marcarAlertaLeida()` existe |
| Paginación de listas | No implementado | Actualmente carga todos los registros |

### MEJORAS VISUALES PENDIENTES 🎨

- Añadir estados vacíos (empty states) con ilustraciones
- Animaciones de carga (shimmer o skeleton)
- Colores de badge dinámicos según días restantes (verde/naranja/rojo)
- Dark mode

---

## Cómo ejecutar el proyecto

```bash
# 1. Clona el repositorio
git clone <url-del-repo>
cd TheraTrackHome

# 2. Abre en Android Studio
#    File → Open → selecciona la carpeta TheraTrackHome

# 3. Sincroniza Gradle
#    Android Studio mostrará el banner "Gradle files have changed" → haz clic en "Sync Now"
#    O desde el menú: File → Sync Project with Gradle Files

# 4. Ejecuta en emulador o dispositivo físico (API 26+)
#    Run → Run 'app' (Shift+F10)
```

**Requisitos:**
- Android Studio Hedgehog o superior
- JDK 17+ (incluida en Android Studio)
- Android SDK API 26 (Android 8.0) mínimo, API 34 target
- Conexión a internet (para Supabase)

---

## Credenciales de prueba

### Personal hospitalario
| Campo | Valor |
|---|---|
| Email | `admin@lapaz.es` |
| Contraseña | `TheraTrack2024` |

### Pacientes de prueba
| Código | Descripción |
|---|---|
| `THERA-0001` | Paciente en aislamiento activo |
| `THERA-0002` | Paciente con alta completada |
| `THERA-0003` | Paciente con alertas pendientes |

---

## Próximos pasos para retomar el desarrollo

1. **Implementar `InformesHospitalActivity`:** usar MPAndroidChart para mostrar evolución de síntomas y estadísticas por semana.
2. **Conectar "Marcar como leída":** `PacienteController.marcarAlertaLeida(alertaId)` ya existe, solo falta un botón en `item_alerta_dashboard.xml` y llamarlo desde `AlertasHospitalActivity`.
3. **Mejorar `DetallePacienteActivity`:** añadir botón para ir al historial de registros del paciente.
4. **Gráfica de mediciones** en el panel del paciente con MPAndroidChart.
5. **Notificaciones push** cuando se genera una alerta nueva (Supabase Realtime + FCM).

---

## Notas para los compañeros

- La rama principal de desarrollo es `roger-dev`. Antes de hacer merge a `main`, asegúrate de que el proyecto compila con `./gradlew assembleDebug`.
- Para compilar desde terminal (bash en Windows), primero exporta el JDK de Android Studio:
  ```bash
  export JAVA_HOME="/c/Users/roger/Desktop/Aplicaciones de Programacion1/jbr"
  ./gradlew assembleDebug
  ```
- El `SupabaseClient` usa la clave pública (anon key). Si ves errores 401 en las llamadas a Supabase, revisa que las políticas RLS de las tablas permitan lectura al rol `anon` o `authenticated`.
- Los modelos en `model/` están marcados con `@Serializable` de kotlinx — si añades un campo nuevo a una tabla, recuerda añadirlo también al data class con `@SerialName("nombre_columna")` y valor por defecto si puede ser null.
