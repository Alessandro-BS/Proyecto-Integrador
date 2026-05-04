-- ============================================================
-- SISOL SALUD - Sistema Inteligente de Turnos Hospitalarios
-- Script de creación de base de datos
-- Motor: MySQL 8.0+
-- ============================================================

-- -------------------------------------------------------
-- 1. Crear y seleccionar la base de datos
-- -------------------------------------------------------
CREATE DATABASE IF NOT EXISTS sisol_salud_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE sisol_salud_db;

-- -------------------------------------------------------
-- 2. Tabla: usuarios
-- Almacena credenciales y rol de todos los usuarios
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS usuarios (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    dni             VARCHAR(15)     NOT NULL,
    nombre          VARCHAR(100)    NOT NULL,
    apellido        VARCHAR(100)    NOT NULL,
    email           VARCHAR(150)    NOT NULL,
    password        VARCHAR(255)    NOT NULL,
    telefono        VARCHAR(20)     NULL,
    rol             ENUM('PACIENTE', 'MEDICO', 'ADMIN') NOT NULL DEFAULT 'PACIENTE',
    activo          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_usuario_dni   UNIQUE (dni),
    CONSTRAINT uk_usuario_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------
-- 3. Tabla: pacientes
-- Datos adicionales del paciente (relación 1:1 con usuarios)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS pacientes (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    usuario_id          BIGINT          NOT NULL,
    fecha_nacimiento    DATE            NULL,
    direccion           VARCHAR(255)    NULL,
    grupo_sanguineo     VARCHAR(5)      NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_paciente_usuario  UNIQUE (usuario_id),
    CONSTRAINT fk_paciente_usuario  FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------
-- 4. Tabla: especialidades
-- Catálogo de especialidades médicas
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS especialidades (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    nombre          VARCHAR(100)    NOT NULL,
    descripcion     VARCHAR(500)    NULL,
    activo          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_especialidad_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------
-- 5. Tabla: medicos
-- Datos del médico (relación 1:1 con usuarios, N:1 con especialidades)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS medicos (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    usuario_id          BIGINT          NOT NULL,
    especialidad_id     BIGINT          NOT NULL,
    numero_colegiatura  VARCHAR(20)     NOT NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_medico_usuario        UNIQUE (usuario_id),
    CONSTRAINT uk_medico_colegiatura    UNIQUE (numero_colegiatura),
    CONSTRAINT fk_medico_usuario        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_medico_especialidad   FOREIGN KEY (especialidad_id)
        REFERENCES especialidades(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------
-- 6. Tabla: disponibilidad_medica
-- Horarios de atención configurados por cada médico
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS disponibilidad_medica (
    id                      BIGINT      AUTO_INCREMENT PRIMARY KEY,
    medico_id               BIGINT      NOT NULL,
    dia_semana              ENUM('LUNES','MARTES','MIERCOLES','JUEVES','VIERNES','SABADO','DOMINGO') NOT NULL,
    hora_inicio             TIME        NOT NULL,
    hora_fin                TIME        NOT NULL,
    duracion_consulta_min   INT         NOT NULL DEFAULT 30,
    activo                  BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at              DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_disponibilidad_medico FOREIGN KEY (medico_id)
        REFERENCES medicos(id) ON DELETE CASCADE ON UPDATE CASCADE,

    -- Un médico no puede tener dos bloques en el mismo día y hora de inicio
    CONSTRAINT uk_disponibilidad_medico_dia_hora UNIQUE (medico_id, dia_semana, hora_inicio),

    -- Validar que la duración sea razonable (15 min mínimo)
    CONSTRAINT chk_duracion_minima CHECK (duracion_consulta_min >= 15),

    -- Validar que hora_fin sea posterior a hora_inicio
    CONSTRAINT chk_horario_valido CHECK (hora_fin > hora_inicio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------
-- 7. Tabla: citas
-- Registro de citas médicas reservadas
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS citas (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    paciente_id         BIGINT          NOT NULL,
    medico_id           BIGINT          NOT NULL,
    fecha               DATE            NOT NULL,
    hora_inicio         TIME            NOT NULL,
    hora_fin            TIME            NOT NULL,
    estado              ENUM('PENDIENTE','CONFIRMADA','CANCELADA','COMPLETADA','NO_ASISTIO')
                                        NOT NULL DEFAULT 'PENDIENTE',
    motivo_consulta     VARCHAR(500)    NULL,
    observaciones       TEXT            NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- Un médico no puede tener dos citas al mismo tiempo
    CONSTRAINT uk_cita_medico_fecha_hora UNIQUE (medico_id, fecha, hora_inicio),

    CONSTRAINT fk_cita_paciente FOREIGN KEY (paciente_id)
        REFERENCES pacientes(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_cita_medico   FOREIGN KEY (medico_id)
        REFERENCES medicos(id) ON DELETE RESTRICT ON UPDATE CASCADE,

    -- Validar que hora_fin sea posterior a hora_inicio
    CONSTRAINT chk_cita_horario_valido CHECK (hora_fin > hora_inicio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------
-- 8. Tabla: notificaciones
-- Registro de notificaciones enviadas (email, SMS)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS notificaciones (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    cita_id         BIGINT          NOT NULL,
    usuario_id      BIGINT          NOT NULL,
    tipo            ENUM('EMAIL','SMS') NOT NULL DEFAULT 'EMAIL',
    asunto          VARCHAR(255)    NOT NULL,
    mensaje         TEXT            NOT NULL,
    enviada         BOOLEAN         NOT NULL DEFAULT FALSE,
    fecha_envio     DATETIME        NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notificacion_cita     FOREIGN KEY (cita_id)
        REFERENCES citas(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_notificacion_usuario  FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------
-- 9. Índices adicionales para optimizar consultas frecuentes
-- -------------------------------------------------------

-- Buscar citas por paciente y fecha
CREATE INDEX idx_cita_paciente_fecha ON citas(paciente_id, fecha);

-- Buscar citas por médico y fecha (consultas del día)
CREATE INDEX idx_cita_medico_fecha ON citas(medico_id, fecha);

-- Buscar citas por estado (reportes de ausentismo)
CREATE INDEX idx_cita_estado ON citas(estado);

-- Buscar citas por fecha (reportes diarios)
CREATE INDEX idx_cita_fecha ON citas(fecha);

-- Buscar médicos por especialidad
CREATE INDEX idx_medico_especialidad ON medicos(especialidad_id);

-- Buscar disponibilidad por médico y día
CREATE INDEX idx_disponibilidad_medico_dia ON disponibilidad_medica(medico_id, dia_semana);

-- Buscar notificaciones pendientes de envío
CREATE INDEX idx_notificacion_pendiente ON notificaciones(enviada, fecha_envio);

-- Buscar usuario por rol (listar médicos, pacientes)
CREATE INDEX idx_usuario_rol ON usuarios(rol);

-- -------------------------------------------------------
-- 10. Datos iniciales (seed data)
-- -------------------------------------------------------

-- 10.1 Usuario administrador por defecto
-- Password: Admin@2026 (BCrypt hash)
INSERT INTO usuarios (dni, nombre, apellido, email, password, telefono, rol, activo) VALUES
('00000001', 'Administrador', 'Sistema', 'admin@sisolsalud.pe',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 '999000001', 'ADMIN', TRUE);

-- 10.2 Especialidades médicas iniciales
INSERT INTO especialidades (nombre, descripcion) VALUES
('Medicina General',        'Atención primaria y diagnóstico general de enfermedades comunes'),
('Pediatría',               'Atención médica especializada para niños y adolescentes'),
('Ginecología',             'Salud reproductiva y atención integral de la mujer'),
('Cardiología',             'Diagnóstico y tratamiento de enfermedades del corazón'),
('Dermatología',            'Tratamiento de enfermedades de la piel, cabello y uñas'),
('Traumatología',           'Lesiones del sistema músculo-esquelético y ortopedia'),
('Oftalmología',            'Diagnóstico y tratamiento de enfermedades de los ojos'),
('Neurología',              'Enfermedades del sistema nervioso central y periférico'),
('Otorrinolaringología',    'Enfermedades del oído, nariz y garganta'),
('Psiquiatría',             'Diagnóstico y tratamiento de trastornos mentales');

-- -------------------------------------------------------
-- FIN DEL SCRIPT
-- -------------------------------------------------------
