# 🏥 SISOL Salud

> Sistema Inteligente de Gestión de Citas Médicas para Centros de Salud.
> Plataforma web fullstack construida con Spring Boot 3.5 y Arquitectura MVC que permite a los pacientes reservar citas, a los médicos gestionar sus consultas, y a los administradores supervisar la operatividad del sistema de manera centralizada.

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.9-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-3.x-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)
![Bootstrap](https://img.shields.io/badge/Bootstrap-5.3-7952B3?style=for-the-badge&logo=bootstrap&logoColor=white)

---

## 📖 Índice

- [Arquitectura del Sistema](#-arquitectura-del-sistema)
- [Modelo de Base de Datos (ERD)](#-modelo-de-base-de-datos-erd)
- [Estructura del Proyecto (Clean Code)](#-estructura-del-proyecto-clean-code)
- [Roles y Flujos de Usuario](#-roles-y-flujos-de-usuario)
- [Mantenimiento y DevOps](#-mantenimiento-y-devops)
- [Stack Tecnológico](#-stack-tecnológico)
- [Endpoints REST y Controladores Web](#-endpoints-rest-y-controladores-web)
- [Instalación y Ejecución](#-instalación-y-ejecución)

---

## 🏗️ Arquitectura del Sistema

La aplicación sigue una **arquitectura de n-capas** (Layered Architecture) con separación estricta de responsabilidades, integrando procesamiento síncrono para peticiones HTTP y procesamiento asíncrono para tareas de fondo.

```mermaid
graph TB
    subgraph "🌐 Capa de Presentación (Frontend)"
        TH["Thymeleaf Templates<br/>(Bootstrap 5 + JS)"]
    end

    subgraph "🔒 Capa de Aplicación (Spring Boot)"
        direction TB
        SC["Filtros de Seguridad<br/>(Spring Security + JWT)"]
        WC["Web Controllers<br/>(Renderizado MVC)"]
        AC["API Controllers<br/>(Endpoints REST)"]
        SV["Service Layer<br/>(Lógica de Negocio Central)"]
        RP["Repository Layer<br/>(Spring Data JPA)"]
        
        subgraph "⚙️ Tareas en Segundo Plano"
            NT["Servicio Asíncrono<br/>(Envío de Emails)"]
            CR["Scheduler / Cron Jobs<br/>(Mantenimiento Automático)"]
        end
    end

    subgraph "💾 Capa de Persistencia"
        DB[("MySQL 8<br/>sisol_salud_db")]
    end

    TH <--> SC
    SC --> WC
    SC --> AC
    WC --> SV
    AC --> SV
    SV --> RP
    RP <--> DB
    SV -.->|"Llamada @Async"| NT
    CR -.->|"Ejecución @Scheduled"| SV
```

> **Nota Arquitectónica:** El proyecto es un monolito estructurado. Las vistas se renderizan desde el servidor (Server-Side Rendering) y coexisten con controladores REST para futuras integraciones móviles.

---

## 🗄️ Modelo de Base de Datos (ERD)

El diseño relacional asegura la integridad referencial y está auditado mediante *Hibernate Envers* (generación automática de tablas `_aud`).

```mermaid
erDiagram
    USUARIOS ||--o| PACIENTES : "1:1"
    USUARIOS ||--o| MEDICOS : "1:1"
    
    MEDICOS ||--o{ MEDICO_ESPECIALIDADES : "1:N"
    ESPECIALIDADES ||--o{ MEDICO_ESPECIALIDADES : "1:N"
    MEDICOS ||--o{ DISPONIBILIDAD_MEDICA : "1:N"
    
    PACIENTES ||--o{ CITAS : "1:N"
    MEDICOS ||--o{ CITAS : "1:N"
    ESPECIALIDADES ||--o{ CITAS : "1:N"
    
    CITAS ||--o| PAGOS : "1:1"
    CITAS ||--o| CONSULTAS : "1:1"
    CITAS ||--o{ NOTIFICACIONES : "1:N"

    USUARIOS {
        bigint id PK
        varchar dni UK
        varchar email UK
        enum rol "ADMIN, MEDICO, PACIENTE"
        boolean activo
    }

    PACIENTES {
        bigint id PK
        date fecha_nacimiento
        varchar grupo_sanguineo
    }

    MEDICOS {
        bigint id PK
        varchar numero_colegiatura UK
    }

    CITAS {
        bigint id PK
        date fecha
        time hora_inicio
        enum estado "PENDIENTE, CONFIRMADA, CANCELADA..."
    }
```

---

## 📁 Estructura del Proyecto (Clean Code)

La distribución de directorios respeta los principios de Clean Architecture y mantenibilidad de código.

```text
src/main/java/com/sisol/salud/
├── config/             # Configuraciones globales (Seguridad, Swagger, Mail, DataSeeder)
├── controller/         # Puntos de entrada HTTP
│   ├── api/            # Controladores que retornan JSON (@RestController)
│   └── web/            # Controladores que retornan vistas HTML (@Controller)
├── dto/                # Objetos de Transferencia de Datos
├── exception/          # Manejadores globales de errores (@ControllerAdvice)
├── mapper/             # Mapeos automáticos Entity <-> DTO (MapStruct)
├── model/              # Dominio del negocio
│   ├── entity/         # Entidades persistentes (JPA)
│   └── enums/          # Enumeradores de estado (Roles, Estados de cita)
├── repository/         # Interfaces de acceso a base de datos (Spring Data)
├── scheduler/          # Procesos automatizados y Cron Jobs
└── service/            # Lógica de negocio core (Transaccional)

src/main/resources/
├── templates/          # Vistas HTML Thymeleaf
│   ├── admin/          # Interfaz de gestión
│   ├── auth/           # Login y registro
│   ├── email/          # Plantillas de notificaciones (HTML/CSS inline)
│   ├── medico/         # Panel del médico
│   ├── paciente/       # Panel del paciente
│   └── layout/         # Componentes base (Header, Sidebar, Footer)
└── application.yml     # Configuración de propiedades del entorno
```

---

## 👥 Roles y Flujos de Usuario

El sistema cuenta con un control de acceso basado en roles (RBAC).

### 🔵 Paciente (`ROLE_PACIENTE`)
```mermaid
flowchart LR
    A("Autenticación") --> B("Panel Paciente")
    B --> C("Reservar Cita Médica")
    C --> C1("Elegir Especialidad") --> C2("Elegir Médico y Hora") --> C3("Confirmar")
    B --> D("Ver Historial Médico")
    B --> E("Actualizar Perfil")
```

### 🔴 Médico (`ROLE_MEDICO`)
```mermaid
flowchart LR
    A("Autenticación") --> B("Panel Médico")
    B --> C("Agenda del Día")
    B --> D("Atender Cita (En Consulta)")
    D --> D1("Registrar Diagnóstico y Notas")
    B --> E("Ver Historial de Citas")
```

### ⚙️ Administrador (`ROLE_ADMIN`)
```mermaid
flowchart LR
    A("Autenticación") --> B("Panel Admin")
    B --> C("Gestión de Médicos")
    B --> D("Gestión de Pacientes")
    C --> C1("Crear/Editar Médico")
    D --> D1("Auditar Citas de Paciente")
```

---

## 🛠️ Mantenimiento y DevOps

El sistema incluye operabilidad a nivel de infraestructura para asegurar la continuidad del negocio.

### 1. Tareas Programadas (Cron Jobs)
El proyecto utiliza `@EnableScheduling` nativo de Spring Boot. 
En el paquete `scheduler`, la clase `MantenimientoCronService.java` ejecuta limpieza de datos y cancelaciones automáticas (Ej: Citas vencidas no pagadas).

### 2. Copias de Seguridad (Backups Automáticos)
Se incluye un script ejecutable en la raíz del proyecto (`backup_sisol.bat`) que automatiza el volcado de la base de datos `sisol_salud` usando `mysqldump`.
**Uso sugerido en Producción:** Asociar este archivo al _Programador de Tareas de Windows_ o _Cron_ en Linux para generar backups a las 02:00 AM diarios en el directorio `C:\Backups\`.

---

## 💻 Stack Tecnológico

| Componente | Tecnología | Versión |
|------------|-----------|---------|
| **Core Backend** | Java / Spring Boot | 17 / 3.5.9 |
| **Persistencia** | MySQL / Spring Data JPA | 8.0 / Hibernate 6.x |
| **Seguridad** | Spring Security + JWT | 6.x |
| **Frontend** | Thymeleaf + Bootstrap | 3.x / 5.3 |
| **Documentación**| SpringDoc OpenAPI | 2.x |
| **Mapeo / Utils**| MapStruct / Lombok | 1.5.5 / - |

---

## 🚀 Instalación y Ejecución

### Requisitos
- **Java 17 JDK** instalado.
- **MySQL 8** ejecutándose en el puerto 3306.
- (Opcional) **Git** para clonar el repositorio.

### Paso 1: Base de Datos
Crea la base de datos en tu entorno local de MySQL:
```sql
CREATE DATABASE sisol_salud_db;
```

### Paso 2: Configuración (application.yml)
Navega a `src/main/resources/application.yml` y verifica tus credenciales locales:
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sisol_salud_db
    username: root
    password: tu_password_local
```

### Paso 3: Ejecución del Proyecto
Abre tu terminal en la carpeta raíz del proyecto y ejecuta el Wrapper de Maven (No necesitas descargar Maven manualmente):

**En Windows:**
```cmd
.\mvnw.cmd spring-boot:run
```

**En Linux / Mac:**
```bash
./mvnw spring-boot:run
```

El DataSeeder automático inyectará las especialidades, y los usuarios de prueba en la base de datos al arrancar por primera vez.

### Paso 4: Accesos de Prueba
Ingresa a `http://localhost:8080` y utiliza las credenciales que se imprimen en la consola al iniciar el sistema.
Por defecto:
- **Admin:** `admin@sisol.com` / `123456`
- **Médico:** `carlos.mendoza@sisol.com` / `123456`
- **Paciente:** `juan.perez@gmail.com` / `123456`

---
*Desarrollado con altos estándares de calidad e ingeniería de software para el Proyecto Integrador.*