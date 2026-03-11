# ☢️ TheraTrack – Sistema Integral de Monitoreo Radiológico

**TheraTrack** es una solución tecnológica de salud integrada, diseñada para pacientes bajo tratamientos con radiofármacos (como el I-131). Su objetivo es cerrar la brecha entre el alta hospitalaria y la seguridad radiológica total, proporcionando monitoreo en tiempo real, protocolos de seguridad dinámicos y un canal de comunicación directo con el personal médico.

---

## 📌 Descripción General del Proyecto

TheraTrack se basa en dos pilares principales:
1.  **App Móvil para el Paciente**: Un acompañante para que los pacientes sigan el decaimiento de su radiación, reciban alertas de seguridad y registren síntomas.
2.  **Dashboard Web Hospitalario**: Un centro de control para que los profesionales médicos monitoreen a múltiples pacientes, gestionen alertas mediante un sistema de triaje y sigan la evolución clínica de forma remota.

---

## 📱 Funcionalidades de la App del Paciente

Basado en las últimas especificaciones de diseño:

### 🔹 Dashboard de Seguridad Dinámico
*   **Lógica de Tres Estados (Sistema de Semáforo)**:
    *   🟢 **Verde (Seguro)**: Interacción social permitida, cuidados estándar.
    *   🟡 **Ámbar (Precaución)**: Nivel restringido. Distanciamiento social de 2 metros y límite de exposición de máximo 15 minutos, aplicado mediante instrucciones de protocolo.
    *   🔴 **Rojo (Peligro)**: Aislamiento inmediato requerido. Alerta de alto riesgo radiológico.
*   **Indicadores en Tiempo Real**: Visualización dinámica de los niveles de radiación actuales ($\mu$Sv/h) y la distancia de seguridad recomendada.

### 🔹 Seguimiento Clínico
*   **Curvas de Decaimiento I-131**: Gráficos interactivos que muestran la disminución esperada del 15% diario de radiación.
*   **Registro de Síntomas**: Registro rápido de náuseas, fatiga, pérdida de apetito y otros efectos secundarios post-terapia.
*   **Historial**: Registro completo de mediciones sincronizado en tiempo real con Firebase.

---

## 🏥 Funcionalidades del Panel Hospitalario

*   **Sistema de Triaje**: Vista centralizada de todos los pacientes activos categorizados por nivel de riesgo (Crítico, Precaución, Resuelto).
*   **Perfiles de Paciente**: Vista clínica detallada que incluye la dosis administrada (ej. 150 mCi I-131), historial de dosis acumulada y síntomas reportados.
*   **Gestión de Alertas**: Notificaciones automáticas cuando un paciente se desvía de la curva de decaimiento esperada o reporta síntomas graves.

---

## 🛠️ Stack Tecnológico (Firebase Integration)

| Componente | Tecnología |
| :--- | :--- |
| **App Móvil (Android)** | **Kotlin (Jetpack Compose)** |
| **Dashboard Web (Admin)** | React / Next.js |
| **Base de Datos** | **Firebase Cloud Firestore (Real-time NoSQL)** |
| **Autenticación** | **Firebase Authentication** |
| **Lógica de Servidor** | **Firebase Cloud Functions** |
| **Notificaciones** | **Firebase Cloud Messaging (FCM)** |
| **Arquitectura Móvil** | MVVM / Clean Architecture |
| **Infraestructura** | Firebase Hosting & Google Cloud Platform |

---

## 🔄 Flujo de Trabajo (Roadmap)

Para asegurar una entrega exitosa, el desarrollo se divide en cuatro fases principales:

### Fase 1: Cimientos y Configuración Firebase
*   [ ] Crear y configurar el proyecto en la consola de **Firebase**.
*   [ ] Inicializar el proyecto Android con las dependencias de Firestore, Auth y Firebase BoM.
*   [ ] Definir el esquema de colecciones en **Firestore** (Pacientes, Mediciones, Alertas).
*   [ ] **Hito**: Usuario puede autenticarse y ver una pantalla base con datos desde Firestore.

### Fase 2: Lógica de Negocio y UI (Compose)
*   [ ] Implementar el **Algoritmo de Decaimiento I-131** en Kotlin/Android.
*   [ ] Desarrollar la interfaz del Dashboard (Semáforo) con Jetpack Compose.
*   [ ] Crear una "API de Sensor MOCK" para simular telemetría de radiación en tiempo real.
*   [ ] **Hito**: Dashboard funcional que cambia de color según los datos en Firestore.

### Fase 3: Panel Hospitalario y Triaje
*   [ ] Desarrollar el Dashboard Web en React integrado con el SDK de Firebase.
*   [ ] Implementar **Cloud Functions** para procesar alertas automáticamente.
*   [ ] Implementar el sistema de triaje médico basado en alertas críticas.
*   [ ] **Hito**: Sincronización completa paciente-médico en tiempo real.

### Fase 4: Pulido y Despliegue
*   [ ] Configurar **FCM (Firebase Cloud Messaging)** para notificaciones push de emergencia.
*   [ ] Funcionalidad de exportación de informes médicos (PDF).
*   [ ] Pruebas finales de seguridad y reglas de Firestore (Security Rules).
*   [ ] **Hito**: MVP desplegado en Firebase Hosting y App de prueba distribuida.

---

## 📂 Estructura del Repositorio

```
/
├── apps/
│   ├── patient_android/  # App Nativa Kotlin (NUEVO)
│   └── hospital_web/     # Dashboard React/Next.js
├── firebase/
│   ├── functions/        # Cloud Functions (Backend)
│   ├── firestore.rules   # Reglas de seguridad
│   └── storage.rules     # Reglas de almacenamiento
├── docs/                 # Documentación y Bocetos PDF
└── README.md
```

---

## 🖼️ Vistas y Responsabilidades

A continuación se detallan las vistas clave identificadas en el diseño y los responsables asignados para su desarrollo técnico:

### 📱 App Móvil (Paciente) - Kotlin / Jetpack Compose
| Vista | Descripción | Responsable |
| :--- | :--- | :--- |
| **Pantalla Inicio** | Pantalla de carga y selección inicial (Paciente/Hospital). | Andrea |
| **Pantalla Login** | Acceso seguro del paciente mediante credenciales o PIN. | Andrea |
| **Dashboard (Semáforo)** | Estado de seguridad dinámico, niveles de radiación y distanciamiento. | Andrea |
| **Curva de Decaimiento** | Gráfico interactivo de la evolución del I-131. | Builes |
| **Pantalla Síntomas** | Checklist para efectos secundarios y reporte rápido. | Builes |
| **Pantalla Historial** | Lista cronológica de todos los registros enviados. | Roger |
| **Chat & Contacto Médico** | Interfaz de comunicación directa con el hospital/doctor. | [Por Asignar] |
| **Perfil & Protocolos** | Configuración de usuario y consulta de protocolos de seguridad. | Roger |

### 🏥 Dashboard Web (Hospital) - React / Next.js
| Vista | Descripción | Responsable |
| :--- | :--- | :--- |
| **Login Administrativo** | Acceso para personal médico autorizado. | Andrea |
| **Triage Dashboard** | Panel central de pacientes categorizados por riesgo crítico. | Builes |
| **Ficha Clínica del Paciente** | Detalle técnico de un paciente: dosis, síntomas y gráficas. | Roger |
| **Gestor de Alertas** | Historial y gestión de alertas disparadas por el sistema. | Roger |
| **Configuración de Protocolos** | Panel para editar recomendaciones dinámicas. | Builes |

---

## 📄 Licencia

Proyecto Interno - Todos los derechos reservados.
