# Supabase Auth Dev Setup

Configuracion recomendada solo para entorno academico/dev.

1. Ir a Supabase Dashboard.
2. Abrir Authentication -> Providers -> Email.
3. Desactivar Confirm email.
4. Guardar cambios.
5. Borrar usuarios de prueba parcialmente creados en Authentication -> Users.
6. Probar de nuevo con emails nuevos.
7. Ejecutar el bootstrap del primer profesional:

```sql
UPDATE public.perfiles
SET estado = 'aprobado'
WHERE email = 'doctor.prueba@test.com';
```
