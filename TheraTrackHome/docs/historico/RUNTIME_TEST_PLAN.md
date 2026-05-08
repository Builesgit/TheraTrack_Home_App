# RUNTIME TEST PLAN

## Preparacion Supabase

- Confirm email OFF: Supabase Dashboard -> Authentication -> Providers -> Email -> Confirm email = OFF.
- Allow new users ON: Supabase Dashboard -> Authentication -> Providers -> Email -> Allow new users to sign up = ON.
- Profesional aprobado: debe existir un usuario profesional con `estado = 'aprobado'`.
- Paciente aprobado con ficha: debe existir un paciente aprobado en `perfiles` y con fila en `pacientes`.
- Borrar usuarios parciales de pruebas anteriores en Authentication -> Users si el registro quedo a medias.
- Usar emails nuevos para registros nuevos durante pruebas.

Query para obtener UUID del paciente:

```sql
SELECT p.id, pf.nombre, pf.email, p.hospital_id
FROM public.pacientes p
JOIN public.perfiles pf ON pf.id = p.id
ORDER BY pf.created_at DESC;
```

Query para insertar alerta:

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

Query para insertar recomendacion:

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

Query para resetear codigos de hospital en entorno dev:

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

Aprobar profesional/paciente manualmente si hiciera falta:

```sql
UPDATE public.perfiles
SET estado = 'aprobado'
WHERE email = 'EMAIL_A_APROBAR';
```

## Prueba 1 - Login hospital

Pasos:

1. Abrir la app.
2. Entrar por acceso hospital/profesional.
3. Iniciar sesion con un profesional aprobado.
4. Verificar que abre `DashboardHospitalActivity`.
5. Tocar BottomNav: Dashboard, Pacientes, Alertas, Informes, Ajustes.

Resultado esperado:

- Login correcto sin confirmacion de email.
- Dashboard muestra nombre profesional/hospital.
- BottomNav no crashea y cada pestaña abre su pantalla.

Si falla, mirar:

- Supabase Dashboard: `perfiles.estado`, `perfiles.rol`, `perfiles.hospital_id`.
- Logcat: `AuthController`, `DashboardHospitalActivity`.
- RLS de `perfiles`, `pacientes`, `alertas`.

## Prueba 2 - Informes PDF

Pasos:

1. Entrar como profesional aprobado.
2. Ir a BottomNav -> Informes.
3. Verificar que el spinner de pacientes carga pacientes del hospital.
4. Seleccionar un paciente.
5. Mantener rango por defecto de ultimos 30 dias o seleccionar fechas con los botones.
6. Pulsar `Generar informe`.
7. Verificar que aparece vista previa, datos del paciente, grafica y resumen.
8. Pulsar `Descargar PDF`.
9. Elegir visor PDF si Android lo solicita.

Resultado esperado:

- Si hay pacientes, el spinner muestra nombres o IDs parciales.
- Si no hay registros, el informe se genera igualmente con mensaje sin datos.
- El PDF se crea en Documents de la app y abre mediante FileProvider.
- Si no hay visor PDF, aparece Toast claro y no crashea.

Si falla, mirar:

- `InformesHospitalActivity`: carga de perfil profesional, pacientes y perfil paciente.
- `PdfReportGenerator`: escritura en `getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)`.
- `AndroidManifest.xml`: provider `${applicationId}.fileprovider`.
- `app/src/main/res/xml/file_paths.xml`.
- Logcat: `InformesHospitalActivity`, `PdfReportGenerator`.

## Prueba 3 - Exportar CSV

Pasos:

1. En Informes, generar primero una vista previa.
2. Pulsar `Exportar datos (.csv)`.
3. Elegir una app para compartir o guardar.
4. Abrir el CSV si el emulador/dispositivo tiene app compatible.

Resultado esperado:

- El boton CSV esta deshabilitado antes de generar preview.
- El CSV contiene cabecera `fecha,temperatura,pulso,nivel_fatiga,nivel_dolor,sintomas,notas`.
- Las filas escapadas con comillas no rompen el archivo.
- Si no hay app para compartir, aparece Toast claro y no crashea.

Si falla, mirar:

- `CsvExporter`.
- FileProvider y `file_paths.xml`.
- Logcat: `InformesHospitalActivity`, `CsvExporter`.

## Prueba 4 - Login paciente

Pasos:

1. Abrir la app.
2. Entrar con paciente aprobado y con ficha clinica creada.
3. Verificar que abre `DashboardPacienteActivity`.
4. Tocar campana, avatar/perfil y BottomNav.

Resultado esperado:

- Login correcto sin confirmacion de email.
- Dashboard muestra nombre del paciente.
- Campana abre Alertas.
- Avatar abre Perfil.
- BottomNav abre Inicio, Historial, Sintomas y Perfil.

Si falla, mirar:

- `perfiles.estado = 'aprobado'`.
- Existencia de fila en `pacientes` con `id = perfiles.id`.
- Logcat: `AuthController`, `DashboardPacienteActivity`.

## Prueba 5 - Registro diario e historial

Pasos:

1. Entrar como paciente aprobado.
2. Ir a Sintomas.
3. Registrar temperatura, pulso, fatiga, dolor, sintomas y notas.
4. Confirmar guardado.
5. Ir a Historial.

Resultado esperado:

- El registro se guarda en `registros_diarios`.
- Historial muestra la grafica y los datos recientes.
- Dashboard no crashea al volver.

Si falla, mirar:

- Tabla `registros_diarios`.
- `RegistroController.guardarRegistro()`.
- RLS de insercion/select de registros.
- Logcat: `RegistroDiarioActivity`, `HistorialActivity`, `RegistroController`.

## Prueba 6 - Alertas y recomendaciones

Pasos:

1. Obtener `UUID_DEL_PACIENTE` y `UUID_DEL_HOSPITAL` con la query de preparacion.
2. Insertar alerta con el SQL de prueba.
3. Insertar recomendacion con el SQL de prueba.
4. Abrir la app como paciente.
5. Ir a Alertas.
6. Abrir la app como hospital.
7. Ir a Alertas hospital.

Resultado esperado:

- Paciente ve alerta y recomendacion.
- Hospital ve alerta del paciente de su hospital.
- BottomSheet de alerta/recomendacion abre sin crashear.

SQL de prueba:

```sql
SELECT p.id, pf.nombre, pf.email, p.hospital_id
FROM public.pacientes p
JOIN public.perfiles pf ON pf.id = p.id
ORDER BY pf.created_at DESC;

INSERT INTO public.alertas (paciente_id, titulo, mensaje, tipo, leida)
VALUES (
  'UUID_DEL_PACIENTE',
  'Nivel critico detectado',
  'Tu nivel de dolor reportado supera el umbral establecido. Contacta con tu medico.',
  'critica',
  false
);

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

Si falla, mirar:

- `alertas.paciente_id` coincide con paciente real.
- `recomendaciones.hospital_id` coincide con hospital del paciente.
- `Recomendacion` espera columna `contenido` para `descripcion`.
- RLS de `alertas` y `recomendaciones`.
- Logcat: `AlertasActivity`, `AlertasHospitalActivity`, `PacienteController`.

## Prueba 7 - Notificaciones locales

Pasos:

1. Instalar app limpia o borrar datos de la app.
2. Entrar como paciente aprobado.
3. Aceptar permiso `POST_NOTIFICATIONS` si Android lo pide.
4. Cerrar o mandar la app a segundo plano.
5. Insertar alerta nueva con SQL de prueba.
6. Volver a abrir dashboard paciente o traer app a primer plano.
7. Repetir con recomendacion nueva.
8. Entrar como hospital y repetir con alerta nueva de un paciente del hospital.

Resultado esperado:

- Se crean canales locales en Android 8+.
- Al volver a dashboard, polling consulta desde la ultima revision por usuario.
- Alertas criticas se notifican aunque recomendaciones/recordatorios esten desactivados.
- Recomendaciones respetan `notif_recomendaciones`.
- No se repite la misma notificacion en bucle al abrir varias veces.

Si falla, mirar:

- Permiso Android 13+: Settings -> Apps -> TheraTrack Home -> Notifications.
- `SyncNotificacionesManager`: clave `ultima_revision_iso_{perfil.id}`.
- Hora `created_at` de alerta/recomendacion: debe ser posterior a la ultima revision.
- Logcat: `NotificacionesManager`, `SyncNotificacionesManager`, `DashboardPacienteActivity`, `DashboardHospitalActivity`.

## Prueba 8 - Asistencia

Pasos:

1. Entrar como paciente aprobado.
2. Ir a Perfil -> Contacto con hospital o desde Dashboard tocar tarjetas de asistencia.
3. Abrir preguntas frecuentes.
4. Pulsar Contactar mi hospital.
5. Pulsar soporte tecnico.
6. Pulsar privacidad y terminos.

Resultado esperado:

- FAQ se expande/contrae.
- Si hay `hospitales.telefono`, abre marcador telefonico.
- Si no hay telefono, muestra `Telefono no disponible`.
- Si no hay app de correo/navegador/telefono, muestra Toast y no crashea.

Si falla, mirar:

- Columna `hospitales.telefono`.
- Ficha paciente con `hospital_id` correcto.
- Logcat: `AsistenciaActivity`, `PacienteController`.

## Prueba 9 - Configuracion notificaciones

Pasos:

1. Entrar como paciente.
2. Ir a Perfil -> Notificaciones.
3. Verificar que Alertas criticas esta activado y deshabilitado.
4. Desactivar Recomendaciones.
5. Desactivar Recordatorios diarios.
6. Salir y volver a abrir la pantalla.
7. Insertar recomendacion nueva y abrir dashboard.

Resultado esperado:

- Preferencias persisten en `SharedPreferences preferencias_notif`.
- Recomendacion nueva no dispara notificacion si `notif_recomendaciones = false`.
- Alertas criticas siguen notificando.

Si falla, mirar:

- `ConfiguracionNotificacionesActivity`.
- `NotificacionesManager.mostrarNotificacionRecomendacion()`.
- Logcat: `ConfiguracionNotificacionesActivity`, `NotificacionesManager`.

## Prueba 10 - Ajustes hospital

Pasos:

1. Entrar como profesional aprobado.
2. Ir a Ajustes.
3. Verificar nombre/rol cargado.
4. Probar 2FA.
5. Probar Centro de Ayuda y Contactar Soporte.
6. Cambiar switches de notificaciones.
7. Guardar umbrales.
8. Cambiar contrasena con una nueva de al menos 6 caracteres.
9. Cerrar sesion e iniciar sesion con la nueva contrasena.

Resultado esperado:

- 2FA muestra dialogo informativo.
- Enlaces externos muestran Toast si no hay app compatible.
- Umbrales persisten en `SharedPreferences umbrales_alerta`.
- Cambio de contrasena llama Supabase Auth y permite login posterior.
- Cerrar sesion vuelve a login hospital.

Si falla, mirar:

- Sesion activa de Supabase antes de llamar `updateUser`.
- Logcat: `AjustesHospitalActivity`, `AuthController`.
- Supabase Auth -> Users para confirmar usuario.

## Logcat recomendado

Filtros utiles por tag/clase:

```powershell
adb logcat -s AuthController InformesHospitalActivity PdfReportGenerator CsvExporter NotificacionesManager SyncNotificacionesManager AsistenciaActivity AjustesHospitalActivity DashboardPacienteActivity DashboardHospitalActivity
```

Filtro amplio por paquete si no aparecen tags especificos:

```powershell
adb logcat | Select-String "com.example.theratrackhome|AuthController|Supabase|PostgREST|FileProvider|Notification"
```
