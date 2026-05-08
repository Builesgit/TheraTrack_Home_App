# Plan de Pruebas — TheraTrack Home

Plan de pruebas manual para validar los flujos principales de la aplicación.

---

## 1. Preparación del entorno

### 1.1 Configurar Supabase

- Confirm email **OFF**: Supabase Dashboard → Authentication → Providers → Email → Confirm email = OFF
- Allow new users **ON**: misma pantalla → Allow new users to sign up = ON
- Debe existir al menos un profesional con `estado = 'aprobado'` (ver sección 1.2)
- Deben existir códigos de hospital disponibles (`usado = false`)

### 1.2 Preparar primer profesional administrador

```sql
-- Después de registrar el primer profesional en la app:
UPDATE public.perfiles
SET estado = 'aprobado'
WHERE email = 'TU_COLEGIADO@medico.theratrack.internal';
```

### 1.3 Verificar datos de partida

```sql
-- Ver todos los perfiles
SELECT id, nombre, email, rol, estado, hospital_id
FROM public.perfiles
ORDER BY created_at DESC;

-- Ver pacientes con ficha
SELECT pf.nombre, pf.email, pa.radiofarmaco, pa.fecha_alta, pa.hospital_id
FROM public.pacientes pa
JOIN public.perfiles pf ON pf.id = pa.id;

-- Obtener UUID del paciente (necesario para insertar alertas/recomendaciones manualmente)
SELECT p.id, pf.nombre, pf.email, p.hospital_id
FROM public.pacientes p
JOIN public.perfiles pf ON pf.id = p.id
ORDER BY pf.created_at DESC;
```

### 1.4 Limpiar entre pruebas

```sql
-- Resetear códigos de hospital
UPDATE public.codigos_hospital
SET usado = false
WHERE codigo IN ('THERA-0001', 'THERA-0002', 'THERA-0003');
```

> Para registros nuevos con el mismo CIPA/colegiado: borrar el usuario anterior en Supabase Dashboard → Authentication → Users antes de volver a registrar.

---

## 2. Casos de prueba — Registro de paciente

| ID | Caso | Pasos | Resultado esperado |
|---|---|---|---|
| REG-P-01 | Registro válido | CIPA 10 dígitos + código THERA-0001 + PIN 6 dígitos | Cuenta creada en estado "pendiente"; redirige a EsperaAprobacionActivity |
| REG-P-02 | CIPA inválido (9 dígitos) | Introducir CIPA de 9 dígitos | Mensaje de error, no avanza |
| REG-P-03 | CIPA inválido (letras) | Introducir "1234ABCD12" | Mensaje de error, no avanza |
| REG-P-04 | Código de hospital inválido | Código "THERA-9999" inexistente | Toast "Código no válido", no avanza |
| REG-P-05 | Código ya usado | Código válido pero `usado = true` | Toast "Código no válido o ya utilizado" |
| REG-P-06 | PIN corto (5 dígitos) | PIN "12345" | Mensaje de error en campo PIN |
| REG-P-07 | PIN no coinciden | PIN y confirmación distintos | Mensaje de error |
| REG-P-08 | Sin marcar términos | Dejar checkbox sin marcar | Validación impide avanzar |
| REG-P-09 | CIPA ya registrado | Mismo CIPA usado antes | Error de Supabase (email duplicado) |

---

## 3. Casos de prueba — Registro de profesional

| ID | Caso | Pasos | Resultado esperado |
|---|---|---|---|
| REG-H-01 | Registro válido | Colegiado 6-10 dígitos + hospital del spinner + cargo + PIN 6 dígitos | Cuenta "pendiente"; redirige a EsperaAprobacionActivity |
| REG-H-02 | Colegiado inválido (5 dígitos) | Introducir "12345" | Mensaje de error |
| REG-H-03 | Colegiado inválido (11 dígitos) | Introducir "12345678901" | Mensaje de error |
| REG-H-04 | PIN corto | PIN de 5 dígitos | Mensaje de error |
| REG-H-05 | PIN no coinciden | PIN y confirmación distintos | Mensaje de error |

---

## 4. Casos de prueba — Login

| ID | Caso | Pasos | Resultado esperado |
|---|---|---|---|
| LOG-P-01 | Login paciente con CIPA | CIPA + PIN correcto | Redirige a PacienteMainActivity (dashboard) |
| LOG-P-02 | Login paciente con email interno | `CIPA@paciente.theratrack.internal` + PIN | Redirige a PacienteMainActivity |
| LOG-P-03 | Login con cuenta pendiente | Usuario no aprobado | EsperaAprobacionActivity |
| LOG-P-04 | Login con cuenta rechazada | Usuario rechazado | Logout automático; redirige a PerfilActivity |
| LOG-P-05 | Credenciales incorrectas | PIN incorrecto | Toast con mensaje de error de Supabase Auth |
| LOG-H-01 | Login profesional con colegiado | Colegiado + PIN correcto | Redirige a HospitalMainActivity (dashboard) |
| LOG-H-02 | Login profesional con email | `colegiado@medico.theratrack.internal` + PIN | Redirige a HospitalMainActivity |
| LOG-H-03 | Login profesional pendiente | Profesional no aprobado | EsperaAprobacionActivity |

---

## 5. Casos de prueba — Flujo completo paciente

| ID | Paso | Resultado esperado |
|---|---|---|
| FLU-P-01 | Registro paciente con THERA-0001 | Estado pendiente |
| FLU-P-02 | Login como profesional aprobado | Dashboard hospitalario |
| FLU-P-03 | Solicitudes Pendientes → aprobar paciente | Abre CrearFichaPacienteActivity |
| FLU-P-04 | Crear ficha: radiofármaco I-131, dosis 3700 MBq, fecha tratamiento hoy, días 7 | Ficha guardada; paciente pasa a "aprobado" |
| FLU-P-05 | Login como paciente aprobado | Dashboard paciente con estado calculado |
| FLU-P-06 | Reportar síntomas: temperatura 36.8, pulso 72, fatiga 3, dolor 2 | ConfirmacionSintomasActivity |
| FLU-P-07 | Navegar a Historial | Gráfica visible con el registro guardado |
| FLU-P-08 | Navegar a Alertas (campana) | Lista de alertas (vacía si no se insertaron) |
| FLU-P-09 | Navegar a Asistencia (tarjetas del dashboard) | FAQ expandible; contacto y enlaces funcionan |
| FLU-P-10 | Perfil → Notificaciones → desactivar Recomendaciones | Switch desactivado; persiste al reabrir |

---

## 6. Casos de prueba — Flujo completo profesional

| ID | Paso | Resultado esperado |
|---|---|---|
| FLU-H-01 | Login como profesional aprobado | Dashboard hospitalario |
| FLU-H-02 | Dashboard muestra métricas correctas | Total pacientes, alertas, en seguimiento, altas |
| FLU-H-03 | Pestaña Pacientes → buscar por nombre | Lista filtrada en tiempo real |
| FLU-H-04 | Click en paciente → detalle | Ficha completa con días restantes y últimas alertas |
| FLU-H-05 | Pestaña Alertas → filtrar "Sin leer" | Solo alertas no leídas |
| FLU-H-06 | Click en alerta → BottomSheet | Detalle con botón "Marcar como leída" |
| FLU-H-07 | Pestaña Informes → seleccionar paciente → generar | Preview visible con gráfica |
| FLU-H-08 | Descargar PDF | PDF abierto con visor externo o Toast si no hay visor |
| FLU-H-09 | Exportar CSV | Selector de compartir o Toast si no hay app |
| FLU-H-10 | Ajustes → cambiar PIN → cerrar sesión → login con PIN nuevo | Login exitoso |

---

## 7. Casos de prueba — Notificaciones

| ID | Caso | Pasos | Resultado esperado |
|---|---|---|---|
| NOT-01 | Permiso POST_NOTIFICATIONS | Primer login en Android 13+ | Diálogo del sistema para conceder permiso |
| NOT-02 | Alerta crítica paciente | Insertar alerta `tipo='critica'` + abrir dashboard | Notificación en canal "alertas_criticas" |
| NOT-03 | Recomendación paciente | Insertar recomendación + abrir dashboard | Notificación si `notif_recomendaciones=true` |
| NOT-04 | Recomendación con notif. desactivada | Desactivar switch + insertar recomendación + abrir | Sin notificación |
| NOT-05 | Alerta crítica no se desactiva | Desactivar recomendaciones → insertar alerta crítica | Notificación de alerta crítica llega igualmente |
| NOT-06 | Recordatorio síntomas | Llamar directamente `NotificacionesManager.mostrarRecordatorioSintomas()` desde debug | Notificación con link a pestaña Síntomas |
| NOT-07 | Sin duplicados | Abrir dashboard varias veces sin nuevas alertas | No se repiten notificaciones anteriores |

**SQL para insertar alertas y recomendaciones de prueba:**

```sql
-- Alerta crítica
INSERT INTO public.alertas (paciente_id, titulo, mensaje, tipo, leida)
VALUES (
    'UUID_DEL_PACIENTE',
    'Nivel crítico detectado',
    'Tu nivel de dolor reportado supera el umbral establecido. Contacta con tu médico.',
    'critica',
    false
);

-- Recomendación
INSERT INTO public.recomendaciones (hospital_id, paciente_id, titulo, contenido, tipo, prioridad, leida)
VALUES (
    'UUID_DEL_HOSPITAL',
    'UUID_DEL_PACIENTE',
    'Hidratación recomendada',
    'Bebe agua con frecuencia y sigue las indicaciones de aislamiento proporcionadas por tu hospital.',
    'recomendacion',
    1,
    false
);
```

---

## 8. Casos de prueba — Informes

| ID | Caso | Resultado esperado |
|---|---|---|
| INF-01 | PDF con registros | PDF generado en Documents, abierto con FileProvider |
| INF-02 | PDF sin registros | PDF generado igualmente con sección vacía |
| INF-03 | PDF sin visor instalado | Toast "No hay visor PDF disponible"; sin crash |
| INF-04 | CSV con registros | CSV con cabecera correcta; valores con comas escapados |
| INF-05 | CSV sin registros | CSV solo con cabecera |
| INF-06 | Botones deshabilitados antes de preview | PDF/CSV deshabilitados hasta pulsar "Generar informe" |

---

## 9. Casos de prueba — Navegación

| ID | Caso | Resultado esperado |
|---|---|---|
| NAV-P-01 | BottomNav paciente: 4 tabs | Cada tab carga su Fragment sin crash |
| NAV-P-02 | BackPressed en DashboardPacienteFragment | Sale de la app (comportamiento por defecto) |
| NAV-P-03 | BackPressed en PerfilPacienteFragment | Vuelve a DashboardPacienteFragment |
| NAV-H-01 | BottomNav hospital: 5 tabs | Cada tab carga su Fragment sin crash |
| NAV-H-02 | BackPressed en AjustesHospitalFragment | Vuelve a DashboardHospitalFragment |
| NAV-EXT-01 | AlertasActivity → BottomNav inicio | Redirige a PacienteMainActivity tab dashboard |
| NAV-EXT-02 | AsistenciaActivity → BottomNav historial | Redirige a PacienteMainActivity tab historial |
| NAV-NOT-01 | Tap en notificación de recordatorio | Abre PacienteMainActivity tab síntomas |

---

## 10. Logcat útil

### Filtros por componente

```powershell
# Windows PowerShell
adb logcat -s AuthController PacienteController RegistroController AdminController `
    NotificacionesManager SyncNotificacionesManager `
    PdfReportGenerator CsvExporter `
    HospitalMainActivity PacienteMainActivity
```

```bash
# macOS / Linux / Git Bash
adb logcat -s AuthController PacienteController RegistroController AdminController \
    NotificacionesManager SyncNotificacionesManager \
    PdfReportGenerator CsvExporter \
    HospitalMainActivity PacienteMainActivity
```

### Filtro amplio por paquete

```powershell
adb logcat | Select-String "theratrackhome|Supabase|PostgREST|FileProvider|Notification"
```

```bash
adb logcat | grep -E "theratrackhome|Supabase|PostgREST|FileProvider|Notification"
```

---

## 11. SQL adicional de utilidad

```sql
-- Aprobar manualmente cualquier perfil
UPDATE public.perfiles
SET estado = 'aprobado'
WHERE email = 'EMAIL_A_APROBAR';

-- Rechazar manualmente
UPDATE public.perfiles
SET estado = 'rechazado'
WHERE email = 'EMAIL_A_RECHAZAR';

-- Ver registros diarios de un paciente
SELECT * FROM public.registros_diarios
WHERE paciente_id = 'UUID_DEL_PACIENTE'
ORDER BY fecha DESC;

-- Ver alertas de un paciente
SELECT * FROM public.alertas
WHERE paciente_id = 'UUID_DEL_PACIENTE'
ORDER BY created_at DESC;

-- Marcar todas las alertas como leídas (limpieza)
UPDATE public.alertas SET leida = true
WHERE paciente_id = 'UUID_DEL_PACIENTE';
```
