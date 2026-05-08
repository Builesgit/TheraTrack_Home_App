# Sistema de Identificación — CIPA / Colegiado / PIN

## 1. Contexto

TheraTrack Home sustituye el par email/contraseña tradicional por identificadores propios del sistema sanitario español:

- **CIPA** para pacientes (Código de Identificación Personal Autonómico)
- **Número de colegiado** para profesionales sanitarios
- **PIN de 6 dígitos** como factor de autenticación personal

Esta decisión elimina la necesidad de que el usuario invente y recuerde una contraseña, y vincula el acceso a la identidad sanitaria real del usuario. El backend (Supabase Auth) requiere un email y una contraseña, por lo que ambos se generan internamente de forma determinista y nunca se exponen en la interfaz.

---

## 2. CIPA — Pacientes

El CIPA (Código de Identificación Personal Autonómico) es el identificador único del paciente en el sistema sanitario de su comunidad autónoma.

**Formato:** 10 dígitos numéricos exactos.

**Dónde encontrarlo:** Figura en el dorso de la tarjeta sanitaria (tarjeta SIP, TSI u equivalente autonómica). La pantalla de ayuda de la app muestra una imagen de referencia de la tarjeta.

**Ejemplo:** `1724979086`

**Validación:** `IdentificadorUtils.validarCipa("1724979086")` → `true`

---

## 3. Número de colegiado — Profesionales

El número de colegiado identifica al profesional sanitario en el Colegio Oficial correspondiente (Medicina, Enfermería, etc.).

**Formato:** 6 a 10 dígitos numéricos.

**Dónde encontrarlo:** En el carnet de colegiado o en el portal online del Colegio Oficial.

**Ejemplo:** `281234567`

**Validación:** `IdentificadorUtils.validarColegiado("281234567")` → `true`

---

## 4. PIN

El PIN es el único secreto que el usuario debe recordar.

**Formato:** 6 dígitos numéricos.

**Por qué PIN y no contraseña:** Es más corto y familiar para usuarios no técnicos (pacientes mayores, personal sanitario sin perfil digital avanzado). Se introduce con teclado numérico en la UI, lo que reduce errores.

> **Limitación actual:** El campo de PIN en Supabase Auth se deriva del identificador (ver sección 6). Si el usuario quiere cambiar el PIN desde Ajustes, la password de Supabase se actualiza directamente; sin embargo, si vuelve a intentar hacer login con el PIN antiguo derivado, fallará. El flujo de cambio de PIN en `AjustesHospitalFragment` actualiza Supabase Auth correctamente.

---

## 5. Email ficticio interno

Supabase Auth exige un email para registrar usuarios. TheraTrack genera emails ficticios internos que nunca se muestran al usuario:

| Rol | Formato | Ejemplo |
|---|---|---|
| Paciente | `{cipa}@paciente.theratrack.internal` | `1724979086@paciente.theratrack.internal` |
| Profesional | `{colegiado}@medico.theratrack.internal` | `281234567@medico.theratrack.internal` |

Estos emails nunca se envían a ningún servidor de correo externo y son puramente funcionales dentro del sistema de autenticación.

---

## 6. Password interna

La contraseña enviada a Supabase Auth se genera de forma determinista:

```
password = "{identificador}#{pin}"
```

**Ejemplo (paciente):** CIPA=`1724979086`, PIN=`123456` → password=`1724979086#123456`

**Por qué es determinista:**
- Permite al usuario recuperar el acceso sin flujo de "olvidé mi contraseña" (basta con tener el CIPA o colegiado + el PIN)
- No se almacena en ningún lugar de la app; se reconstruye en el momento del login
- La seguridad real la aporta la combinación CIPA/colegiado (conocido solo por el usuario) + PIN (elegido por el usuario)

---

## 7. Login flexible

El campo de login acepta tanto el identificador (CIPA o colegiado) como el email completo ficticio. Esto es útil si un desarrollador necesita hacer debug con el email directamente.

**Lógica de normalización:**

```
input = "1724979086"
→ detectarTipoInput() → "cipa"
→ normalizarAEmail(input, esPaciente=true) → "1724979086@paciente.theratrack.internal"

input = "1724979086@paciente.theratrack.internal"
→ detectarTipoInput() → "email"
→ normalizarAEmail() → devuelve el email tal cual

input = "281234567"
→ detectarTipoInput() → "colegiado"
→ normalizarAEmail(input, esPaciente=false) → "281234567@medico.theratrack.internal"
```

---

## 8. IdentificadorUtils — Referencia completa

Clase `object` (singleton) en `util/IdentificadorUtils.kt`.

### `cipaToEmail(cipa: String): String`
Genera el email ficticio para un paciente.
```kotlin
IdentificadorUtils.cipaToEmail("1724979086")
// → "1724979086@paciente.theratrack.internal"
```

### `colegiadoToEmail(numeroColegiado: String): String`
Genera el email ficticio para un profesional.
```kotlin
IdentificadorUtils.colegiadoToEmail("281234567")
// → "281234567@medico.theratrack.internal"
```

### `validarCipa(cipa: String): Boolean`
Devuelve `true` si el CIPA tiene exactamente 10 dígitos numéricos.
```kotlin
IdentificadorUtils.validarCipa("1724979086")  // true
IdentificadorUtils.validarCipa("12345")        // false (5 dígitos)
IdentificadorUtils.validarCipa("172497908A")   // false (tiene letra)
```

### `validarColegiado(numero: String): Boolean`
Devuelve `true` si el número tiene entre 6 y 10 dígitos numéricos.
```kotlin
IdentificadorUtils.validarColegiado("281234567")  // true (9 dígitos)
IdentificadorUtils.validarColegiado("123")         // false (menos de 6)
```

### `generarPassword(identificador: String, pin: String): String`
Combina identificador y PIN para generar la password de Supabase Auth.
```kotlin
IdentificadorUtils.generarPassword("1724979086", "123456")
// → "1724979086#123456"
```

### `detectarTipoInput(input: String): String`
Detecta si el input es un email, CIPA, colegiado o desconocido.
- Devuelve `"email"` si contiene `@`
- Devuelve `"cipa"` si es exactamente 10 dígitos
- Devuelve `"colegiado"` si tiene entre 6 y 10 dígitos (sin ser CIPA)
- Devuelve `"desconocido"` en cualquier otro caso

### `normalizarAEmail(input: String, esPaciente: Boolean): String`
Punto de entrada principal para el login. Acepta cualquier forma de identificación y devuelve el email completo para Supabase Auth.
```kotlin
// En LoginActivity (paciente)
val email = IdentificadorUtils.normalizarAEmail(etCipaOEmail.text.toString(), esPaciente = true)

// En LoginHospitalActivity (profesional)
val email = IdentificadorUtils.normalizarAEmail(etColegiadoOEmail.text.toString(), esPaciente = false)
```

### `extraerIdentificadorOriginal(input: String): String`
Si el input es un email ficticio interno, extrae el identificador original (la parte antes de `@`).
```kotlin
IdentificadorUtils.extraerIdentificadorOriginal("1724979086@paciente.theratrack.internal")
// → "1724979086"
```

---

## 9. Validaciones y casos límite

| Caso | Comportamiento |
|---|---|
| CIPA con letras | `validarCipa` → `false`; la UI muestra error |
| CIPA de 9 dígitos | `validarCipa` → `false` |
| Colegiado de 5 dígitos | `validarColegiado` → `false` |
| Colegiado de 11 dígitos | `validarColegiado` → `false` |
| Login con email completo | `normalizarAEmail` lo pasa sin modificar |
| Espacios al inicio/final | Todos los métodos aplican `.trim()` antes de procesar |
| PIN vacío | Validado en la UI antes de llamar a `generarPassword` |
| Dos usuarios con el mismo CIPA | Supabase Auth devuelve error de email duplicado en el registro |

---

## 10. Flujo completo de registro y primer login

```
1. Usuario introduce CIPA "1724979086" + PIN "123456"

2. RegisterActivity:
   ├── validarCipa("1724979086") → true
   ├── email = cipaToEmail("1724979086") → "1724979086@paciente.theratrack.internal"
   ├── password = generarPassword("1724979086", "123456") → "1724979086#123456"
   └── AuthController.registrarPaciente(nombre, email, password, codigoHospital, "1724979086", "cipa")

3. AuthController:
   ├── Supabase Auth signUpWith(email, password)
   └── RPC crear_perfil(id, nombre, "paciente", hospitalId, email, identificador="1724979086", tipo="cipa")

4. Perfil queda en estado "pendiente"

5. Profesional aprueba → CrearFichaPacienteActivity → AdminController.crearFichaPaciente(...)

6. Login posterior:
   ├── Usuario introduce "1724979086" + PIN "123456"
   ├── email = normalizarAEmail("1724979086", esPaciente=true) → "1724979086@paciente.theratrack.internal"
   ├── password = generarPassword("1724979086", "123456") → "1724979086#123456"
   └── AuthController.login(email, password) → Result.success
```
