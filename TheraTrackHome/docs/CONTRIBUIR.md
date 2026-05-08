# Guía para contribuidores — TheraTrack Home

## 1. Cómo empezar

### 1.1 Setup del entorno

1. Instalar **Android Studio** Hedgehog 2023.1.1 o superior
2. Clonar el repositorio y abrir la carpeta `TheraTrackHome/` en Android Studio
3. Android Studio configurará automáticamente `local.properties` con la ruta del SDK
4. Sincronizar Gradle: `File → Sync Project with Gradle Files`
5. Verificar que el proyecto compila: `Build → Make Project` o `./gradlew assembleDebug`

Para compilar desde terminal, exportar JAVA_HOME al JDK 21:

```powershell
# Windows PowerShell
$env:JAVA_HOME = "C:\Users\roger\.p2\pool\plugins\org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.7.v20250502-0916\jre"
```

### 1.2 Configurar el backend de desarrollo

Seguir [docs/SUPABASE.md](SUPABASE.md) para configurar Supabase Auth (Confirm email OFF) y crear el primer administrador.

---

## 2. Workflow de Git

### 2.1 Ramas

| Tipo | Formato | Ejemplo |
|---|---|---|
| Funcionalidad nueva | `feature/descripcion` | `feature/filtro-alertas-fecha` |
| Corrección de bug | `fix/descripcion` | `fix/login-cipa-espacios` |
| Documentación | `docs/descripcion` | `docs/guia-tecnica-controllers` |
| Refactorización | `refactor/descripcion` | `refactor/viewbinding-login` |
| Datos / SQL | `data/descripcion` | `data/add-hospitales-canarias` |

Siempre crear la rama desde `main` actualizado:

```bash
git checkout main
git pull origin main
git checkout -b feature/mi-nueva-funcionalidad
```

### 2.2 Commits

Seguir la convención **Conventional Commits**:

```
<tipo>: <descripción corta en español>

[cuerpo opcional]
```

| Tipo | Cuándo usarlo |
|---|---|
| `feat:` | Nueva funcionalidad |
| `fix:` | Corrección de bug |
| `docs:` | Solo documentación |
| `refactor:` | Refactorización sin cambio de comportamiento |
| `style:` | Formato, sangría, sin cambio de lógica |
| `test:` | Añadir o corregir tests |
| `chore:` | Actualización de dependencias, configuración |

**Ejemplos:**

```bash
git commit -m "feat: añadir filtro de alertas por fecha en panel hospital"
git commit -m "fix: corregir validación de CIPA con espacios al inicio"
git commit -m "docs: actualizar SUPABASE.md con nueva política RLS"
git commit -m "refactor: migrar LoginActivity a ViewBinding"
```

### 2.3 Pull Requests

Antes de abrir un PR:
- [ ] `BUILD SUCCESSFUL` local
- [ ] El flujo afectado probado manualmente en emulador o dispositivo
- [ ] Sin errores nuevos en Logcat
- [ ] Documentación actualizada si añades Activity, Controller o tabla nueva

Descripción del PR debe incluir:
- Qué cambia y por qué
- Cómo probarlo (pasos o datos de prueba)
- Capturas de pantalla si hay cambios visuales

---

## 3. Convenciones de código

### 3.1 Idioma
- **Comentarios y mensajes de UI:** español
- **Nombres técnicos** (clases, variables, funciones): inglés o español, según el contexto ya establecido en el proyecto (seguir el estilo existente)
- **SQL:** snake_case para tablas y columnas

### 3.2 Naming
- Clases: `PascalCase` (ej. `PacienteController`, `RegistroDiarioFragment`)
- Variables y funciones: `camelCase` (ej. `hospitalId`, `obtenerPerfilActual`)
- Constantes: `UPPER_SNAKE_CASE` (ej. `CANAL_ALERTAS_CRITICAS`)
- Recursos XML: `snake_case` (ej. `activity_login.xml`, `ic_nav_home.xml`)

### 3.3 Manejo de errores
Todos los Controllers devuelven `Result<T>`. En la View:

```kotlin
viewLifecycleOwner.lifecycleScope.launch {
    PacienteController.obtenerPacienteActual()
        .onSuccess { paciente ->
            // actualizar UI
        }
        .onFailure { e ->
            Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
}
```

### 3.4 Coroutines
- En **Activities**: `lifecycleScope.launch { ... }`
- En **Fragments**: `viewLifecycleOwner.lifecycleScope.launch { ... }` (nunca `lifecycleScope` directamente en Fragment)
- En **Controllers**: `suspend fun` que lanza excepciones hacia arriba (capturadas con `runCatching` en el Controller)

### 3.5 Logs
- Usar `Log.d(TAG, "mensaje")` con `TAG = javaClass.simpleName`
- Solo en puntos de debugging relevantes; eliminar logs verbosos antes de PR

---

## 4. Cómo añadir una nueva pantalla

### 4.1 Decidir: Activity o Fragment

| Activity | Fragment |
|---|---|
| Flujo lineal que llega desde otra Activity | Sección del BottomNav en HospitalMainActivity o PacienteMainActivity |
| Lógica propia sin BottomNav | Contenido del panel principal |
| Diálogos de confirmación | Listas o dashboards dentro del panel |

### 4.2 Crear el layout XML

En `app/src/main/res/layout/`:

```xml
<!-- activity_mi_nueva_pantalla.xml -->
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <!-- contenido -->

</LinearLayout>
```

**Convenciones de naming de layouts:**
- `activity_*.xml` para Activities y para Fragments que reutilizan el layout de una Activity
- `fragment_*.xml` para layouts exclusivos de Fragment
- `dialog_*.xml` para diálogos
- `item_*.xml` para elementos de RecyclerView/ListView
- `bottom_sheet_*.xml` para BottomSheetDialogFragment

### 4.3 Si es Activity: crear la clase Kotlin

```kotlin
package com.example.theratrackhome

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MiNuevaPantallaActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mi_nueva_pantalla)
        // inicializar vistas y lógica
    }
}
```

### 4.4 Registrar en AndroidManifest.xml

```xml
<activity android:name=".MiNuevaPantallaActivity" android:exported="false"/>
```

### 4.5 Si es Fragment: crear la clase Kotlin

```kotlin
package com.example.theratrackhome.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.theratrackhome.R

class MiNuevoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.activity_mi_nuevo_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // nunca acceder a vistas antes de onViewCreated
        // usar requireContext() y requireActivity() en lugar de this
        // usar viewLifecycleOwner.lifecycleScope para coroutines
    }
}
```

### 4.6 Añadir el Fragment al contenedor (si es tab de BottomNav)

En `HospitalMainActivity.kt` o `PacienteMainActivity.kt`:

1. Añadir el item al menú en `res/menu/menu_bottom_nav_hospital.xml`
2. Añadir el caso en `crearFragment()`:

```kotlin
private fun crearFragment(tag: String): Fragment = when (tag) {
    "dashboard" -> DashboardHospitalFragment()
    "mi_nueva"  -> MiNuevoFragment()  // ← añadir aquí
    else        -> DashboardHospitalFragment()
}
```

3. Añadir el caso en `actualizarBottomNavItem()` y `configurarBottomNav()`

---

## 5. Cómo añadir un nuevo Controller

Patrón estándar para un Controller:

```kotlin
package com.example.theratrackhome.controller

import com.example.theratrackhome.model.MiModel

object MiController {

    private val db get() = SupabaseClient.client.postgrest

    suspend fun obtenerDatos(): Result<List<MiModel>> = runCatching {
        db["mi_tabla"]
            .select()
            .decodeList<MiModel>()
    }

    suspend fun guardarDato(dato: MiModel): Result<Unit> = runCatching {
        db["mi_tabla"].insert(dato)
        Unit
    }
}
```

**Reglas del Controller:**
- Siempre `object` (singleton); no instanciar
- Siempre `suspend fun` para operaciones de red
- Siempre `runCatching { ... }` para capturar excepciones de red
- Nunca llamar a `android.util.Log` con datos sensibles
- Devolver `Result<T>`; no lanzar excepciones al caller

---

## 6. Cómo añadir una nueva tabla en Supabase

1. **Definir el SQL** de `CREATE TABLE` con las relaciones necesarias (ver [docs/SUPABASE.md](SUPABASE.md))
2. **Habilitar RLS** y definir políticas
3. **Crear la data class** en `model/`:

```kotlin
@Serializable
data class MiModelo(
    val id: String? = null,
    @SerialName("nombre_columna_bd") val nombreCampoKotlin: String,
    @SerialName("created_at") val createdAt: String? = null
)
```

4. **Crear métodos** en el Controller correspondiente
5. **Actualizar** [docs/SUPABASE.md](SUPABASE.md) con la nueva tabla y sus políticas

---

## 7. Cómo testear cambios

Antes de abrir un PR, verificar:

```bash
# 1. Compilación limpia
./gradlew clean assembleDebug

# 2. Instalar y probar manualmente
./gradlew installDebug
```

Lista de comprobación manual mínima:
- [ ] El flujo directamente afectado funciona de extremo a extremo
- [ ] Las pantallas adyacentes no crashean
- [ ] Logcat sin `Exception` ni `Error` nuevos relacionados con el cambio
- [ ] Si se añadió Activity: aparece en AndroidManifest
- [ ] Si se modificó DB: políticas RLS probadas con usuario con rol correcto
- [ ] Si se modificó un Controller: todos los callers siguen compilando

---

## 8. Code review — Qué revisar al aprobar un PR

- **Compilación:** el PR debe incluir confirmación de `BUILD SUCCESSFUL`
- **Correctitud:** el flujo funciona como se describe
- **Seguridad:** sin hardcoding de credenciales, sin SQL injection posible en RPCs
- **RLS:** si hay cambios de BD, las políticas son correctas y no exponen datos de otros usuarios/hospitales
- **Convenciones:** naming, idioma, manejo de errores con `Result<T>`
- **Fragments vs Activities:** uso correcto de `viewLifecycleOwner.lifecycleScope` en Fragments
- **Regresión:** los flujos existentes de login, registro y dashboards siguen funcionando
