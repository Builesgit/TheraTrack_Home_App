# ENTREGA

## Resumen

TheraTrack Home es una app Android nativa para seguimiento domiciliario de pacientes tras tratamiento radiofarmacológico. Permite registro diario de síntomas, revisión hospitalaria, alertas clínicas, recomendaciones, aprobación de usuarios e informes exportables.

## Roles

- Paciente: registra síntomas, consulta recomendaciones, revisa alertas, accede a historial, perfil, soporte y configuración de notificaciones.
- Hospital/profesional: gestiona pacientes, solicitudes, alertas, fichas clínicas, informes, CSV y ajustes.

## Tecnologías

- Android nativo Kotlin + XML.
- Supabase Auth + PostgREST.
- MPAndroidChart para gráficas.
- Material Components.
- Corrutinas Kotlin.
- PDF nativo con android.graphics.pdf.PdfDocument.
- Notificaciones locales Android, sin FCM.

## Supabase

El backend usa `schema_v2.sql` como base de tablas, RPCs y políticas RLS. Para desplegarlo, ejecutar el SQL desde Supabase Dashboard -> SQL Editor en el proyecto correspondiente.

Configuración Auth para entorno académico/dev:

- Authentication -> Providers -> Email -> Confirm email = OFF.
- Authentication -> Providers -> Email -> Allow new users to sign up = ON.

## Profesional Admin Inicial

Tras registrar el primer profesional, aprobarlo manualmente con:

```sql
UPDATE public.perfiles
SET estado = 'aprobado'
WHERE email = 'doctor.prueba@test.com';
```

Cambiar el email por el usuario real usado en pruebas.

## Flujos Principales

- Registro profesional -> perfil pendiente -> aprobación hospitalaria/bootstrap -> dashboard hospitalario.
- Registro paciente con código -> validación de código -> auth -> perfil -> uso de código.
- Profesional crea ficha clínica y aprueba paciente.
- Paciente registra síntomas y revisa historial.
- Hospital consulta alertas, pacientes e informes.
- Hospital genera PDF y CSV desde informes.
- Notificaciones locales se sincronizan al abrir dashboards.

## Limitaciones Conocidas

- 2FA no implementado; queda para próxima versión.
- Notificaciones locales, no push real.
- Polling solo al abrir dashboard o volver a primer plano.
- Inglés preparado desde selector de idioma, pero faltan strings-en completos.
- PDF Canvas nativo sin firma digital.
