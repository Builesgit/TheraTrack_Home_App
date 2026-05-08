# Supabase — Backend de TheraTrack Home

## 1. Configuración inicial

### 1.1 Crear el proyecto

1. Ir a [supabase.com](https://supabase.com) y crear una cuenta o iniciar sesión
2. Crear un nuevo proyecto con región Europe (West) para minimizar latencia
3. Anotar la **Project URL** y la **anon/public key** (aparecen en Settings → API)

### 1.2 Configurar credenciales en la app

Abrir `app/src/main/java/com/example/theratrackhome/controller/SupabaseClient.kt` y actualizar:

```kotlin
val client = createSupabaseClient(
    supabaseUrl = "https://TU_PROYECTO.supabase.co",
    supabaseKey = "TU_ANON_KEY"
) {
    install(Auth)
    install(Postgrest)
    install(Realtime)
}
```

> La `anon key` es pública y segura para incluir en el código fuente de Android. El acceso real lo controla RLS (Row Level Security), no la key.

---

## 2. Configuración de Auth

### 2.1 Pasos en Supabase Dashboard

1. Ir a **Authentication → Providers → Email**
2. **Confirm email** → **OFF** (imprescindible para entorno de desarrollo; los usuarios son creados con emails ficticios internos que no pueden recibir correos)
3. **Allow new users to sign up** → **ON**
4. Guardar cambios

> Si "Confirm email" está ON, el registro no completará la sesión y la app quedará bloqueada esperando confirmación de un email que nunca llega.

### 2.2 Notas sobre la autenticación

- Los emails de usuarios tienen formato `{identificador}@paciente.theratrack.internal` o `{identificador}@medico.theratrack.internal`
- Las contraseñas tienen formato `{identificador}#{pin}` y se generan determinísticamente en `IdentificadorUtils`
- El trigger `on_auth_user_created` está **desactivado** deliberadamente; la app crea los perfiles manualmente vía RPC `crear_perfil`

---

## 3. Schema completo de tablas

Ejecutar en Supabase Dashboard → **SQL Editor**:

```sql
-- Hospitales
CREATE TABLE public.hospitales (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    nombre TEXT NOT NULL,
    direccion TEXT,
    telefono TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Códigos de acceso de hospital (uno por paciente registrado)
CREATE TABLE public.codigos_hospital (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    hospital_id UUID NOT NULL REFERENCES public.hospitales(id) ON DELETE CASCADE,
    codigo TEXT NOT NULL UNIQUE,
    usado BOOLEAN DEFAULT false,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Perfiles de usuarios (pacientes y profesionales)
CREATE TABLE public.perfiles (
    id UUID PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    nombre TEXT NOT NULL,
    rol TEXT NOT NULL CHECK (rol IN ('paciente', 'profesional')),
    hospital_id UUID REFERENCES public.hospitales(id),
    estado TEXT DEFAULT 'pendiente' CHECK (estado IN ('pendiente', 'aprobado', 'rechazado')),
    email TEXT,
    cargo TEXT,
    telefono TEXT,
    motivo_solicitud TEXT,
    identificador TEXT,
    tipo_identificador TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Fichas clínicas de pacientes
CREATE TABLE public.pacientes (
    id UUID PRIMARY KEY REFERENCES public.perfiles(id) ON DELETE CASCADE,
    hospital_id UUID NOT NULL REFERENCES public.hospitales(id),
    radiofarmaco TEXT NOT NULL,
    dosis_mbq DOUBLE PRECISION,
    fecha_tratamiento DATE NOT NULL,
    fecha_alta DATE NOT NULL,
    dias_aislamiento INT DEFAULT 7,
    notas_clinicas TEXT,
    creado_por UUID REFERENCES public.perfiles(id),
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Registros diarios de síntomas
CREATE TABLE public.registros_diarios (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    paciente_id UUID NOT NULL REFERENCES public.pacientes(id) ON DELETE CASCADE,
    fecha DATE NOT NULL,
    temperatura DOUBLE PRECISION,
    pulso INT,
    nivel_fatiga INT CHECK (nivel_fatiga BETWEEN 0 AND 10),
    nivel_dolor INT CHECK (nivel_dolor BETWEEN 0 AND 10),
    sintomas TEXT,
    notas TEXT,
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Alertas clínicas
CREATE TABLE public.alertas (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    paciente_id UUID NOT NULL REFERENCES public.pacientes(id) ON DELETE CASCADE,
    titulo TEXT NOT NULL,
    mensaje TEXT NOT NULL,
    tipo TEXT DEFAULT 'info' CHECK (tipo IN ('info', 'critica', 'alerta')),
    leida BOOLEAN DEFAULT false,
    registro_diario_id UUID REFERENCES public.registros_diarios(id),
    created_at TIMESTAMPTZ DEFAULT now()
);

-- Recomendaciones clínicas
CREATE TABLE public.recomendaciones (
    id UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    hospital_id UUID REFERENCES public.hospitales(id),
    paciente_id UUID REFERENCES public.pacientes(id) ON DELETE CASCADE,
    titulo TEXT NOT NULL,
    contenido TEXT NOT NULL,
    tipo TEXT NOT NULL,
    prioridad INT DEFAULT 0,
    leida BOOLEAN DEFAULT false,
    creado_por UUID REFERENCES public.perfiles(id),
    created_at TIMESTAMPTZ DEFAULT now()
);
```

> **Nota:** La columna `contenido` de `recomendaciones` está mapeada como `descripcion` en la data class `Recomendacion.kt` mediante `@SerialName("contenido")`. No renombrar la columna en BD sin actualizar el modelo.

---

## 4. Row Level Security (RLS)

Habilitar RLS en todas las tablas:

```sql
ALTER TABLE public.hospitales ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.codigos_hospital ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.perfiles ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.pacientes ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.registros_diarios ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.alertas ENABLE ROW LEVEL SECURITY;
ALTER TABLE public.recomendaciones ENABLE ROW LEVEL SECURITY;
```

### Políticas

```sql
-- Hospitales: lectura pública (anon puede ver hospitales para el registro)
CREATE POLICY "hospitales_select" ON public.hospitales
    FOR SELECT TO anon, authenticated USING (true);

-- Códigos hospital: solo lectura para anon (validación en registro)
CREATE POLICY "codigos_select" ON public.codigos_hospital
    FOR SELECT TO anon, authenticated USING (true);

-- Perfiles: cada usuario ve/edita el suyo; profesionales ven perfiles de su hospital
CREATE POLICY "perfiles_select_own" ON public.perfiles
    FOR SELECT TO authenticated
    USING (
        auth.uid() = id OR
        (
            rol = 'paciente' AND
            hospital_id = (SELECT hospital_id FROM public.perfiles WHERE id = auth.uid())
        ) OR
        (
            rol = 'profesional' AND
            hospital_id = (SELECT hospital_id FROM public.perfiles WHERE id = auth.uid())
        )
    );

CREATE POLICY "perfiles_update_own" ON public.perfiles
    FOR UPDATE TO authenticated
    USING (auth.uid() = id);

-- Pacientes: profesionales del mismo hospital pueden leer y actualizar
CREATE POLICY "pacientes_select" ON public.pacientes
    FOR SELECT TO authenticated
    USING (
        id = auth.uid() OR
        hospital_id = (SELECT hospital_id FROM public.perfiles WHERE id = auth.uid())
    );

CREATE POLICY "pacientes_insert" ON public.pacientes
    FOR INSERT TO authenticated
    WITH CHECK (
        hospital_id = (SELECT hospital_id FROM public.perfiles WHERE id = auth.uid())
    );

-- Registros diarios: paciente inserta los suyos; profesional del hospital puede leer
CREATE POLICY "registros_select" ON public.registros_diarios
    FOR SELECT TO authenticated
    USING (
        paciente_id = auth.uid() OR
        (SELECT hospital_id FROM public.pacientes WHERE id = paciente_id) =
        (SELECT hospital_id FROM public.perfiles WHERE id = auth.uid())
    );

CREATE POLICY "registros_insert" ON public.registros_diarios
    FOR INSERT TO authenticated
    WITH CHECK (paciente_id = auth.uid());

-- Alertas: paciente lee las suyas; profesional del hospital lee todas las del hospital
CREATE POLICY "alertas_select" ON public.alertas
    FOR SELECT TO authenticated
    USING (
        paciente_id = auth.uid() OR
        (SELECT hospital_id FROM public.pacientes WHERE id = paciente_id) =
        (SELECT hospital_id FROM public.perfiles WHERE id = auth.uid())
    );

CREATE POLICY "alertas_update" ON public.alertas
    FOR UPDATE TO authenticated
    USING (
        (SELECT hospital_id FROM public.pacientes WHERE id = paciente_id) =
        (SELECT hospital_id FROM public.perfiles WHERE id = auth.uid())
    );

CREATE POLICY "alertas_insert" ON public.alertas
    FOR INSERT TO authenticated
    WITH CHECK (
        (SELECT hospital_id FROM public.pacientes WHERE id = paciente_id) =
        (SELECT hospital_id FROM public.perfiles WHERE id = auth.uid())
    );

-- Recomendaciones: similar a alertas
CREATE POLICY "recomendaciones_select" ON public.recomendaciones
    FOR SELECT TO authenticated
    USING (
        paciente_id = auth.uid() OR
        hospital_id = (SELECT hospital_id FROM public.perfiles WHERE id = auth.uid())
    );

CREATE POLICY "recomendaciones_update" ON public.recomendaciones
    FOR UPDATE TO authenticated
    USING (
        hospital_id = (SELECT hospital_id FROM public.perfiles WHERE id = auth.uid())
    );
```

---

## 5. Funciones RPC

Las RPCs se ejecutan con `SECURITY DEFINER` para poder realizar operaciones que superan los permisos del usuario llamante (ej. insertar en `perfiles` desde el registro).

```sql
-- Valida un código de hospital y devuelve el hospital_id
CREATE OR REPLACE FUNCTION public.validar_codigo(codigo_text TEXT)
RETURNS UUID
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_hospital_id UUID;
BEGIN
    SELECT hospital_id INTO v_hospital_id
    FROM public.codigos_hospital
    WHERE codigo = codigo_text AND usado = false;

    IF v_hospital_id IS NULL THEN
        RAISE EXCEPTION 'Código no válido o ya utilizado';
    END IF;

    RETURN v_hospital_id;
END;
$$;

-- Marca un código como usado
CREATE OR REPLACE FUNCTION public.usar_codigo(codigo_text TEXT)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
    UPDATE public.codigos_hospital
    SET usado = true
    WHERE codigo = codigo_text;
END;
$$;

-- Crea el perfil del usuario (llamada post-registro en Auth)
CREATE OR REPLACE FUNCTION public.crear_perfil(
    p_id UUID,
    p_nombre TEXT,
    p_rol TEXT,
    p_hospital_id UUID,
    p_email TEXT,
    p_cargo TEXT DEFAULT NULL,
    p_telefono TEXT DEFAULT NULL,
    p_motivo TEXT DEFAULT NULL,
    p_identificador TEXT DEFAULT NULL,
    p_tipo_identificador TEXT DEFAULT NULL
)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
    INSERT INTO public.perfiles (
        id, nombre, rol, hospital_id, estado, email,
        cargo, telefono, motivo_solicitud,
        identificador, tipo_identificador
    )
    VALUES (
        p_id, p_nombre, p_rol, p_hospital_id, 'pendiente', p_email,
        p_cargo, p_telefono, p_motivo,
        p_identificador, p_tipo_identificador
    )
    ON CONFLICT (id) DO NOTHING;
END;
$$;

-- Aprueba un perfil (usado por AdminController)
CREATE OR REPLACE FUNCTION public.aprobar_perfil(p_perfil_id UUID)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
    UPDATE public.perfiles
    SET estado = 'aprobado'
    WHERE id = p_perfil_id;
END;
$$;

-- Rechaza un perfil
CREATE OR REPLACE FUNCTION public.rechazar_perfil(p_perfil_id UUID)
RETURNS void
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
    UPDATE public.perfiles
    SET estado = 'rechazado'
    WHERE id = p_perfil_id;
END;
$$;

-- Grants necesarios
GRANT EXECUTE ON FUNCTION public.validar_codigo TO anon;
GRANT EXECUTE ON FUNCTION public.usar_codigo TO authenticated;
GRANT EXECUTE ON FUNCTION public.crear_perfil TO anon;
GRANT EXECUTE ON FUNCTION public.crear_perfil TO authenticated;
GRANT EXECUTE ON FUNCTION public.aprobar_perfil TO authenticated;
GRANT EXECUTE ON FUNCTION public.rechazar_perfil TO authenticated;
```

---

## 6. Datos iniciales (desarrollo)

```sql
-- Hospital de prueba
INSERT INTO public.hospitales (id, nombre, direccion, telefono)
VALUES (
    'a1b2c3d4-e5f6-7890-abcd-ef1234567890',
    'Hospital Universitario La Paz',
    'Paseo de la Castellana, 261, Madrid',
    '+34 917 277 000'
);

-- Códigos de acceso para pacientes
INSERT INTO public.codigos_hospital (hospital_id, codigo, usado)
VALUES
    ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'THERA-0001', false),
    ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'THERA-0002', false),
    ('a1b2c3d4-e5f6-7890-abcd-ef1234567890', 'THERA-0003', false);
```

---

## 7. Cómo crear un administrador inicial

El primer profesional sanitario no puede ser aprobado por nadie (aún no hay administradores). Se aprueba manualmente desde la consola SQL:

```sql
-- Opción 1: por email ficticio (el que generó la app al registrarse)
UPDATE public.perfiles
SET estado = 'aprobado'
WHERE email = 'TU_COLEGIADO@medico.theratrack.internal';

-- Opción 2: por nombre
UPDATE public.perfiles
SET estado = 'aprobado'
WHERE nombre = 'Dr. Nombre Apellido' AND rol = 'profesional';

-- Verificar resultado
SELECT id, nombre, email, rol, estado, hospital_id
FROM public.perfiles
WHERE rol = 'profesional'
ORDER BY created_at DESC;
```

---

## 8. Mantenimiento

### Resetear códigos de hospital en desarrollo
```sql
UPDATE public.codigos_hospital
SET usado = false
WHERE codigo IN ('THERA-0001', 'THERA-0002', 'THERA-0003');
```

### Limpiar datos de prueba
```sql
-- Borrar registros diarios de prueba
DELETE FROM public.registros_diarios
WHERE paciente_id IN (
    SELECT id FROM public.perfiles WHERE email LIKE '%theratrack.internal%'
);

-- Borrar alertas de prueba
DELETE FROM public.alertas WHERE created_at < now() - interval '7 days';

-- Borrar usuario de Auth (desde Supabase Dashboard → Authentication → Users)
-- No hay SQL directo seguro para borrar de auth.users sin trigger
```

### Ver el estado completo de usuarios
```sql
SELECT
    p.id,
    p.nombre,
    p.email,
    p.rol,
    p.estado,
    p.hospital_id,
    p.identificador,
    p.tipo_identificador,
    p.created_at
FROM public.perfiles p
ORDER BY p.created_at DESC;
```

### Ver pacientes con su ficha
```sql
SELECT
    pf.nombre,
    pf.email,
    pa.radiofarmaco,
    pa.fecha_tratamiento,
    pa.fecha_alta,
    pa.dias_aislamiento,
    h.nombre AS hospital
FROM public.pacientes pa
JOIN public.perfiles pf ON pf.id = pa.id
JOIN public.hospitales h ON h.id = pa.hospital_id
ORDER BY pa.created_at DESC;
```

---

## 9. Troubleshooting

### "Código de hospital no válido o ya utilizado"
- El código ya fue usado en un registro anterior. Resetear con el SQL de mantenimiento o añadir nuevos códigos.
- Verificar que el `hospital_id` asociado al código existe en la tabla `hospitales`.

### "infinite recursion detected in policy for relation"
- Una política RLS hace referencia a la misma tabla que está filtrando. Revisar las políticas de `perfiles`; el subselect `SELECT hospital_id FROM perfiles WHERE id = auth.uid()` puede causar recursión. Usar `SECURITY DEFINER` en las RPCs para evitarlo.

### "function public.nombre_funcion is not unique"
- Supabase tiene una versión antigua de la función con diferentes parámetros. Ejecutar `DROP FUNCTION public.crear_perfil;` antes de ejecutar el `CREATE OR REPLACE`.

### "over_email_send_rate_limit"
- Supabase limita los emails de verificación. No aplica si "Confirm email" está OFF. Si aparece, es porque está activado; desactivarlo.

### "signup_disabled"
- "Allow new users to sign up" está en OFF. Activarlo en Authentication → Providers → Email.

### La app muestra "Cuenta pendiente de aprobación" en bucle
- El perfil existe pero `estado = 'pendiente'`. Aprobarlo manualmente con el SQL de la sección 7, o desde la app con un profesional aprobado.

### Los registros no aparecen en historial
- Verificar que el `paciente_id` del registro coincide con `auth.uid()` del paciente logueado.
- Verificar RLS de `registros_diarios`: la política `select` requiere que `paciente_id = auth.uid()` o que el hospital coincida.

### Las alertas no aparecen al profesional
- El `paciente_id` de la alerta debe estar en `pacientes` con `hospital_id` igual al del profesional.
- Verificar con: `SELECT * FROM alertas WHERE paciente_id = 'UUID_PACIENTE';`
