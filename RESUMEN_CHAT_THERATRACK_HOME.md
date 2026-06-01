# RESUMEN CHAT THERATRACK HOME

Este documento resume el trabajo realizado durante este chat, explica como funciona la app TheraTrack Home y deja informacion practica para continuar pruebas, entrega y mantenimiento.

## Estado final verificado

- Proyecto Android nativo Kotlin + XML, sin Compose.
- ViewBinding no esta habilitado; la app usa `findViewById`.
- Backend: Supabase Auth + PostgREST.
- Graficas: MPAndroidChart.
- Material Components, corrutinas, Supabase-kt y AndroidX ya estaban instalados.
- `schema_v2.sql` ya fue ejecutado en Supabase.
- No hay SQL adicional pendiente.
- Auth funciona en modo dev con `Confirm email = OFF`.
- Registro profesional funciona.
- Login profesional funciona.
- Registro paciente funciona.
- Aprobacion/ficha clinica funciona o queda lista para pruebas runtime.
- `testDebugUnitTest`: `BUILD SUCCESSFUL`.
- `assembleDebug`: `BUILD SUCCESSFUL`.
- `lintDebug`: inicialmente encontro errores, se corrigieron y ahora termina en `BUILD SUCCESSFUL`.

## Comandos de validacion usados

Desde `C:\Users\roger\AndroidStudioProjects\TheraTrack_Home_App\TheraTrackHome`:

```powershell
$env:JAVA_HOME="C:/Users/roger/.p2/pool/plugins/org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.7.v20250502-0916/jre"; ./gradlew testDebugUnitTest
```

```powershell
$env:JAVA_HOME="C:/Users/roger/.p2/pool/plugins/org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.7.v20250502-0916/jre"; ./gradlew assembleDebug
```

```powershell
$env:JAVA_HOME="C:/Users/roger/.p2/pool/plugins/org.eclipse.justj.openjdk.hotspot.jre.full.win32.x86_64_21.0.7.v20250502-0916/jre"; ./gradlew lintDebug
```

## Configuracion Supabase necesaria

En Supabase Dashboard:

- `Authentication -> Providers -> Email -> Confirm email = OFF`.
- `Authentication -> Providers -> Email -> Allow new users to sign up = ON`.
- Si hay usuarios creados parcialmente durante pruebas, borrarlos en `Authentication -> Users`.
- Para pruebas, usar emails nuevos.
- Para aprobar manualmente un perfil si hace falta:

```sql
UPDATE public.perfiles
SET estado = 'aprobado'
WHERE email = 'EMAIL_A_APROBAR';
```

## Arquitectura actual

La app esta organizada de forma simple:

- Activities en `app/src/main/java/com/example/theratrackhome/`.
- Controladores en `app/src/main/java/com/example/theratrackhome/controller/`.
- Modelos Kotlin serializables en `app/src/main/java/com/example/theratrackhome/model/`.
- Cliente Supabase en `app/src/main/java/com/example/theratrackhome/network/SupabaseClient.kt`.
- Layouts XML en `app/src/main/res/layout/`.
- Menus BottomNav en `app/src/main/res/menu/`.
- FileProvider paths en `app/src/main/res/xml/file_paths.xml`.

## Backend y tablas principales

La app depende de estas tablas principales:

- `perfiles`: perfil de Auth, rol, estado, hospital, email, telefono, cargo.
- `hospitales`: hospitales disponibles; incluye `telefono` en el schema real.
- `pacientes`: ficha clinica del paciente aprobada por hospital.
- `codigos_hospital`: codigos de registro paciente.
- `registros_diarios`: sintomas y constantes diarios.
- `alertas`: alertas clinicas por paciente.
- `recomendaciones`: recomendaciones por hospital o por paciente.

RPCs funcionales que no se deben cambiar:

- `validar_codigo`.
- `usar_codigo`.
- `crear_perfil`.
- `aprobar_perfil`.
- `rechazar_perfil`.

Importante: no se volvio a usar `validar_y_usar_codigo`.

## Flujo de autenticacion

### Registro profesional

1. Usuario selecciona hospital y completa datos.
2. `AuthController.registrarProfesional()` llama `signUpWith(Email)`.
3. Supabase Auth crea el usuario.
4. `currentUserOrNull()` debe devolver usuario activo porque `Confirm email = OFF`.
5. Se llama `crear_perfil` con rol `profesional`.
6. El perfil queda pendiente hasta aprobacion manual o via flujo hospitalario.

### Registro paciente

1. Usuario introduce codigo hospitalario.
2. `validar_codigo` valida codigo y obtiene `hospital_id`.
3. `signUpWith(Email)` crea usuario en Supabase Auth.
4. `currentUserOrNull()` debe devolver usuario activo.
5. `crear_perfil` crea perfil `paciente`.
6. `usar_codigo` marca el codigo como usado.

### Login

1. `AuthController.login()` inicia sesion con `signInWith(Email)`.
2. Carga perfil desde `perfiles`.
3. Si `estado = 'pendiente'`, lanza `CuentaPendienteException`.
4. Si `estado = 'rechazado'`, cierra sesion y muestra mensaje.
5. Si esta aprobado, permite continuar.

## Correcciones hechas en Auth

Se reviso `AuthController.kt` completo y se confirmo que el error:

```text
over_email_send_rate_limit
email rate limit exceeded
```

venia de Supabase Auth intentando enviar correos de confirmacion. Eso no se debe ni puede resolver desde Kotlin sin permisos administrativos: se configura en Supabase Dashboard.

Se agrego en `AuthController.kt`:

- Comentario claro indicando `Confirm email = OFF` para pruebas.
- Manejo especifico de `over_email_send_rate_limit` y `email rate limit exceeded`.
- Mensaje claro si `currentUserOrNull()` es null tras `signUpWith()`.
- Logs con `TAG = "AuthController"` para pasos de registro:
  - `validar_codigo OK`.
  - `signUpWith OK`.
  - `currentUser OK`.
  - `crear_perfil OK`.
  - `usar_codigo OK`.

## Flujo hospital

El profesional aprobado entra a `DashboardHospitalActivity`.

Desde el BottomNav hospital puede navegar a:

- Dashboard.
- Pacientes.
- Alertas.
- Informes.
- Ajustes.

El dashboard hospital:

- Carga perfil actual.
- Obtiene `hospital_id`.
- Muestra nombre del hospital.
- Carga pacientes del hospital.
- Cuenta alertas no leidas.
- Muestra solicitudes pendientes.
- Pide permiso de notificaciones en Android 13+ si aun no se ha pedido.
- Ejecuta polling de notificaciones en `onResume()`.

## Solicitudes, aprobacion y ficha clinica

`AdminController` mantiene las operaciones administrativas:

- `obtenerSolicitudesPendientes(hospitalId)`.
- `aprobarSolicitud(perfilId)` mediante RPC `aprobar_perfil`.
- `rechazarSolicitud(perfilId)` mediante RPC `rechazar_perfil`.
- `crearFichaPaciente(...)` inserta en `pacientes` usando el hospital del profesional autenticado.

## Informes hospitalarios

Se implemento `InformesHospitalActivity` y `activity_informes_hospital.xml`.

La pantalla incluye:

- Header con flecha atras, titulo y subtitulo.
- Spinner de paciente, cargado desde pacientes del hospital del profesional.
- Spinner de tipo de informe:
  - Informe Clinico Completo.
  - Resumen Semanal.
  - Informe de Alertas.
  - Informe de Dosimetria.
- Fecha desde y fecha hasta con `DatePickerDialog`.
- Rango por defecto: ultimos 30 dias.
- Boton `Generar informe`.
- Vista previa con titulo, referencia, paciente, fecha, medico responsable, grafica MPAndroidChart, resumen de sintomas y alertas.
- Botones `Descargar PDF` y `Exportar datos (.csv)`, inicialmente deshabilitados.

Se agregaron metodos minimos en controladores:

- `RegistroController.obtenerRegistrosRango(pacienteId, desde, hasta)`.
- `PacienteController.obtenerPerfilPorId(perfilId)`.
- `PacienteController.obtenerAlertasPacienteEnRango(pacienteId, desde, hasta)`.
- `PacienteController.obtenerRecomendacionesPaciente(hospitalId)` ya se usa para recomendaciones.

## PDF

Se creo `controller/PdfReportGenerator.kt`.

Caracteristicas:

- Usa `android.graphics.pdf.PdfDocument`, sin librerias externas.
- Crea PDF A4 `595 x 842`.
- Guarda en `context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)`.
- Nombre: `informe_${paciente.id.take(8)}_${timestamp}.pdf`.
- Header azul con texto blanco.
- Referencia `TT-{anio}-{iniciales}-{correlativo}`.
- Datos del paciente en grid 2x2.
- Edad/sexo muestran `No especificado` porque el modelo actual no tiene esos campos.
- Grafica de barras en Canvas usando dolor/fatiga.
- Resumen de sintomas parseando texto separado por comas o punto y coma.
- Tabla sencilla de alertas.
- Lista de recomendaciones emitidas.
- Footer por pagina.
- Paginacion defensiva con `ensure()`.
- `PdfDocument` se cierra en `finally`.

## CSV

Se creo `controller/CsvExporter.kt`.

Caracteristicas:

- Guarda en `context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)`.
- Nombre: `registros_${paciente.id.take(8)}_${timestamp}.csv`.
- Cabecera:

```csv
fecha,temperatura,pulso,nivel_fatiga,nivel_dolor,sintomas,notas
```

- Escapa comillas dobles, comas y saltos de linea.
- Comparte via `ACTION_SEND` con URI segura de FileProvider.

## FileProvider

Se configuro en `AndroidManifest.xml`:

```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

Se creo `app/src/main/res/xml/file_paths.xml`:

```xml
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-files-path
        name="documents"
        path="Documents/" />
</paths>
```

Los intents de PDF/CSV usan `FLAG_GRANT_READ_URI_PERMISSION` y capturan errores si no hay app compatible o si falla FileProvider.

## Notificaciones locales

Se creo `controller/NotificacionesManager.kt`.

Canales:

- `alertas_criticas`: importancia alta, vibracion.
- `recomendaciones`: importancia default.
- `recordatorios`: importancia baja.

Comportamiento:

- Alertas abren `AlertasActivity`.
- Recomendaciones abren `AlertasActivity`.
- Recordatorios abren `RegistroDiarioActivity`.
- Usa `PendingIntent.FLAG_IMMUTABLE | FLAG_UPDATE_CURRENT`.
- Usa icono seguro existente `ic_launcher_foreground`.
- Crea canales en `MainApplication` y tambien defensivamente antes de notificar.
- En Android 13+ comprueba `POST_NOTIFICATIONS` antes de llamar `notify()`.
- Alertas criticas no se bloquean por preferencias.
- Recomendaciones respetan `preferencias_notif/notif_recomendaciones`.
- Recordatorios respetan `preferencias_notif/notif_recordatorios`.

Se creo `MainApplication.kt` y se registro en `AndroidManifest.xml` con `android:name=".MainApplication"`.

## Polling de notificaciones

Se creo `controller/SyncNotificacionesManager.kt`.

Funcionamiento:

- Se llama desde `DashboardPacienteActivity.onResume()`.
- Se llama desde `DashboardHospitalActivity.onResume()`.
- Obtiene perfil actual con `AuthController.obtenerPerfilActual()`.
- Guarda ultima revision por usuario: `ultima_revision_iso_{perfil.id}`.
- Si no hay revision previa, consulta desde hace 24 horas.
- Si no hay perfil valido, no guarda timestamp y no rompe.

Paciente:

- Consulta alertas nuevas por `paciente_id`.
- Consulta recomendaciones nuevas por `hospital_id` y filtra por `paciente_id` o recomendaciones generales.

Profesional:

- Consulta pacientes del hospital.
- Consulta alertas nuevas de esos pacientes.
- Notifica alertas criticas/no leidas.

## Permiso Android 13+

Se agrego en `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
```

Se pide en:

- `DashboardPacienteActivity`.
- `DashboardHospitalActivity`.

Se evita insistir mas de una vez con `SharedPreferences preferencias_app/permiso_notif_solicitado`.

## Asistencia

Se implemento `AsistenciaActivity` y `activity_asistencia.xml`.

Incluye:

- Header con flecha atras, titulo y campana.
- FAQ expandible con 5 preguntas.
- Contactar mi hospital con `ACTION_DIAL` si hay `hospitales.telefono`.
- Soporte tecnico con `mailto:soporte@theratrack.com`.
- Version de app desde `PackageManager`.
- Politica de privacidad.
- Terminos.
- BottomNav paciente.

Hardening:

- Si no hay telefono, muestra `Telefono no disponible`.
- Si no hay app de telefono/correo/navegador, muestra Toast y no crashea.

## Configuracion de notificaciones paciente

Se creo `ConfiguracionNotificacionesActivity` y `activity_configuracion_notificaciones.xml`.

Incluye:

- Switch de alertas criticas activado y deshabilitado.
- Switch editable de recomendaciones.
- Switch editable de recordatorios diarios.
- Persistencia en `SharedPreferences preferencias_notif`:
  - `notif_recomendaciones`.
  - `notif_recordatorios`.

`PerfilPacienteActivity` ahora abre esta pantalla desde la fila `Notificaciones`.

## Idioma

`PerfilPacienteActivity` ahora conecta la fila `Idioma`.

Comportamiento:

- Muestra dialogo con `Espanol` y `English`.
- Usa `AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(locale))`.
- Guarda en `SharedPreferences preferencias_app/idioma`.
- Incluye TODO para strings ingleses completos.

Limitacion: el selector esta preparado, pero faltan `strings-en` completos.

## Ajustes hospital

Se implemento `AjustesHospitalActivity` y su layout.

Incluye:

- Carga nombre/rol del profesional.
- Cambio de contrasena con dialogo de 3 campos.
- Validacion de password nueva: longitud >= 6 y confirmacion igual.
- Llama `SupabaseClient.client.auth.updateUser { password = nueva }`.
- 2FA muestra dialogo informativo.
- Centro de Ayuda con navegador.
- Contactar Soporte por mailto.
- Switches de notificaciones hospitalarias guardados en `preferencias_notif_hospital`.
- Umbrales guardados en `SharedPreferences umbrales_alerta`:
  - `umbral_ambar_msv_h`.
  - `umbral_rojo_msv_h`.
- Cerrar sesion vuelve a `LoginHospitalActivity`.

Hardening:

- Enlaces externos y correo capturan `ActivityNotFoundException`.

## Documentacion creada

Se crearon estos documentos:

- `supabase/AUTH_DEV_SETUP.md`: configuracion Auth dev para evitar confirmacion por email y rate limit.
- `TESTING.md`: checklist manual corto.
- `ENTREGA.md`: resumen de entrega, tecnologias, roles, limitaciones y flujo general.
- `POST_PROMPT3_REVIEW.md`: revision tecnica post Prompt 3, riesgos y correcciones.
- `RUNTIME_TEST_PLAN.md`: guia de pruebas runtime paso a paso con SQL.
- `RESUMEN_CHAT_THERATRACK_HOME.md`: este documento.

## Revision final realizada

Se revisaron especialmente:

- `AndroidManifest.xml`: permisos, MainApplication, Activities, FileProvider.
- `InformesHospitalActivity`: spinners, rango, preview, PDF, CSV, FileProvider.
- `PdfReportGenerator`: directorio, listas vacias, nulls, cierre de PDF, paginacion.
- `CsvExporter`: cabecera, escape, guardado.
- `NotificacionesManager`: canales, permiso Android 13+, PendingIntent, preferencias.
- `SyncNotificacionesManager`: timestamp por usuario, perfil nulo, paciente/profesional.
- `AsistenciaActivity`: intents externos seguros, telefono nullable.
- `ConfiguracionNotificacionesActivity`: SharedPreferences.
- `AjustesHospitalActivity`: update password, intents externos, preferencias.
- `DashboardPacienteActivity` y `DashboardHospitalActivity`: permiso notificaciones y polling.
- `PacienteController`, `RegistroController`, `AdminController`, `AuthController`.

## Correcciones de ultima revision

Durante la revision final se corrigio:

- `NotificacionesManager`: comprobacion explicita de `POST_NOTIFICATIONS` antes de `notify()`.
- Layouts XML: cambio mecanico de `android:tint` a `app:tint` para cumplir AppCompat lint.
- Layouts XML sin namespace `app`: se agrego `xmlns:app="http://schemas.android.com/apk/res-auto"` donde hacia falta.
- `InformesHospitalActivity`: FileProvider URI dentro de `try/catch` para PDF y CSV.

## Resultado de pruebas automaticas

Resultado final:

- `testDebugUnitTest`: `BUILD SUCCESSFUL`.
- `assembleDebug`: `BUILD SUCCESSFUL`.
- `lintDebug`: `BUILD SUCCESSFUL` tras correcciones.

Nota: no hay tests instrumentados runtime ejecutados en emulador desde este chat. La validacion real de login, Supabase, PDF viewer, correo, navegador y notificaciones debe hacerse manualmente con `RUNTIME_TEST_PLAN.md`.

## Pruebas manuales prioritarias

Ejecutar en emulador/dispositivo:

1. Login hospital aprobado.
2. Navegacion BottomNav hospital.
3. Informes: generar preview, abrir PDF, compartir CSV.
4. Login paciente aprobado con ficha.
5. Registro diario de sintomas.
6. Historial con grafica.
7. Alertas/recomendaciones con SQL manual.
8. Notificaciones locales al volver a dashboards.
9. Asistencia: FAQ, telefono, email y enlaces.
10. Configuracion notificaciones paciente.
11. Cambio de idioma.
12. Ajustes hospital: 2FA informativo, ayuda, soporte, umbrales, cambio password y logout.

## SQL util para pruebas

Obtener pacientes:

```sql
SELECT p.id, pf.nombre, pf.email, p.hospital_id
FROM public.pacientes p
JOIN public.perfiles pf ON pf.id = p.id
ORDER BY pf.created_at DESC;
```

Insertar alerta:

```sql
INSERT INTO public.alertas (paciente_id, titulo, mensaje, tipo, leida)
VALUES (
  'UUID_DEL_PACIENTE',
  'Nivel critico detectado',
  'Tu nivel de dolor reportado supera el umbral establecido. Contacta con tu medico.',
  'critica',
  false
);
```

Insertar recomendacion:

```sql
INSERT INTO public.recomendaciones (hospital_id, paciente_id, titulo, contenido, tipo, prioridad, leida)
VALUES (
  'UUID_DEL_HOSPITAL',
  'UUID_DEL_PACIENTE',
  'Hidratacion recomendada',
  'Bebe agua con frecuencia y sigue las indicaciones de aislamiento proporcionadas por tu hospital.',
  'recomendacion',
  1,
  false
);
```

Resetear codigos solo en dev:

```sql
UPDATE public.codigos_hospital
SET usado = false
WHERE codigo IN ('THERA-0001', 'THERA-0002', 'THERA-0003');
```

Ver perfiles:

```sql
SELECT id, nombre, email, rol, estado, hospital_id
FROM public.perfiles
ORDER BY created_at DESC;
```

## Limitaciones conocidas

- Notificaciones son locales, no push real ni FCM.
- Polling solo ocurre al abrir/volver a dashboard.
- 2FA no esta implementado; solo dialogo informativo.
- Selector de idioma preparado, pero faltan traducciones inglesas completas.
- PDF se genera con Canvas nativo, sin firma digital.
- La apertura de PDF/CSV/correo/navegador depende de apps disponibles en el dispositivo.
- La validacion completa depende de Supabase real, RLS y datos de prueba correctos.

## Logcat recomendado

```powershell
adb logcat -s AuthController InformesHospitalActivity PdfReportGenerator CsvExporter NotificacionesManager SyncNotificacionesManager AsistenciaActivity AjustesHospitalActivity DashboardPacienteActivity DashboardHospitalActivity
```

Filtro amplio:

```powershell
adb logcat | Select-String "com.example.theratrackhome|AuthController|Supabase|PostgREST|FileProvider|Notification"
```

## Siguiente paso recomendado

Ejecutar `RUNTIME_TEST_PLAN.md` en el emulador con un profesional aprobado y un paciente aprobado con ficha clinica. Si alguna prueba falla, revisar primero Logcat y despues Supabase Dashboard/RLS segun la seccion `Si falla, mirar` de cada prueba.
