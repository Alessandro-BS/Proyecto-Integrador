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
    id                              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    usuario_id                      BIGINT          NOT NULL,
    fecha_nacimiento                DATE            NULL,
    direccion                       VARCHAR(255)    NULL,
    grupo_sanguineo                 VARCHAR(5)      NULL,
    genero                          VARCHAR(20)     NULL,
    contacto_emergencia_nombre      VARCHAR(150)    NULL,
    contacto_emergencia_parentesco  VARCHAR(50)     NULL,
    contacto_emergencia_telefono    VARCHAR(20)     NULL,
    created_at                      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

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
    costo           DECIMAL(10,2)   NOT NULL DEFAULT 0.00,
    activo          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_especialidad_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------
-- 5. Tabla: medicos
-- Datos del médico (relación 1:1 con usuarios)
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS medicos (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    usuario_id          BIGINT          NOT NULL,
    numero_colegiatura  VARCHAR(20)     NOT NULL,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_medico_usuario        UNIQUE (usuario_id),
    CONSTRAINT uk_medico_colegiatura    UNIQUE (numero_colegiatura),
    CONSTRAINT fk_medico_usuario        FOREIGN KEY (usuario_id)
        REFERENCES usuarios(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------
-- 5.1 Tabla: medico_especialidades (Relación N:N)
-- Asocia médicos con una o más especialidades
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS medico_especialidades (
    medico_id       BIGINT          NOT NULL,
    especialidad_id BIGINT          NOT NULL,
    PRIMARY KEY (medico_id, especialidad_id),
    CONSTRAINT fk_medico_esp_medico FOREIGN KEY (medico_id) 
        REFERENCES medicos(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_medico_esp_especialidad FOREIGN KEY (especialidad_id) 
        REFERENCES especialidades(id) ON DELETE CASCADE ON UPDATE CASCADE
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
    especialidad_id     BIGINT          NOT NULL,
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
    CONSTRAINT fk_cita_especialidad FOREIGN KEY (especialidad_id)
        REFERENCES especialidades(id) ON DELETE RESTRICT ON UPDATE CASCADE,

    -- Validar que hora_fin sea posterior a hora_inicio
    CONSTRAINT chk_cita_horario_valido CHECK (hora_fin > hora_inicio)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -------------------------------------------------------
-- 7.1 Tabla: pagos
-- Registro de pagos asociados a las citas médicas
-- -------------------------------------------------------
CREATE TABLE IF NOT EXISTS pagos (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    cita_id         BIGINT          NOT NULL,
    paciente_id     BIGINT          NOT NULL,
    monto           DECIMAL(10,2)   NOT NULL,
    metodo_pago     ENUM('EFECTIVO','TARJETA_DEBITO','TARJETA_CREDITO','YAPE','PLIN','TRANSFERENCIA') NOT NULL,
    estado_pago     ENUM('PENDIENTE','PAGADO','REEMBOLSADO') NOT NULL DEFAULT 'PENDIENTE',
    referencia_pago VARCHAR(100)    NULL,
    fecha_pago      DATETIME        NULL,
    notas           VARCHAR(500)    NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_pago_cita UNIQUE (cita_id),
    CONSTRAINT fk_pago_cita FOREIGN KEY (cita_id)
        REFERENCES citas(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_pago_paciente FOREIGN KEY (paciente_id)
        REFERENCES pacientes(id) ON DELETE RESTRICT ON UPDATE CASCADE
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
    enviado         BOOLEAN         NOT NULL DEFAULT FALSE,
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

-- Buscar médicos por especialidad en la tabla intermedia
CREATE INDEX idx_medico_especialidades_especialidad ON medico_especialidades(especialidad_id);

-- Buscar pagos por paciente y estado
CREATE INDEX idx_pago_paciente ON pagos(paciente_id);
CREATE INDEX idx_pago_estado ON pagos(estado_pago);

-- Buscar disponibilidad por médico y día
CREATE INDEX idx_disponibilidad_medico_dia ON disponibilidad_medica(medico_id, dia_semana);

-- Buscar notificaciones pendientes de envío
CREATE INDEX idx_notificacion_pendiente ON notificaciones(enviado, fecha_envio);

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

-- 10.2 Usuarios médicos de ejemplo
INSERT INTO usuarios (dni, nombre, apellido, email, password, telefono, rol, activo) VALUES
('10000002', 'Ricardo', 'Mendoza', 'r.mendoza@sisolsalud.pe',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 '987654321', 'MEDICO', TRUE),
('10000003', 'Elena', 'Vazquez', 'e.vazquez@sisolsalud.pe',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 '987654322', 'MEDICO', TRUE),
('10000004', 'Roberto', 'Valdivia Mendoza', 'r.valdivia@sisolsalud.pe',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 '987654323', 'MEDICO', TRUE);

-- 10.3 Usuarios pacientes de ejemplo
INSERT INTO usuarios (dni, nombre, apellido, email, password, telefono, rol, activo) VALUES
('45281900', 'Carlos Alberto', 'Ruiz', 'carlos.ruiz@gmail.com',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 '912345678', 'PACIENTE', TRUE),
('45281901', 'María', 'López García', 'maria.lopez@gmail.com',
 '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
 '912345679', 'PACIENTE', TRUE);

-- 10.4 Especialidades médicas iniciales con costo de consulta
INSERT INTO especialidades (nombre, descripcion, costo) VALUES
('Medicina General',        'Atención primaria y diagnóstico general de enfermedades comunes', 30.00),
('Pediatría',               'Atención médica especializada para niños y adolescentes', 40.00),
('Ginecología',             'Salud reproductiva y atención integral de la mujer', 50.00),
('Cardiología',             'Diagnóstico y tratamiento de enfermedades del corazón', 60.00),
('Dermatología',            'Tratamiento de enfermedades de la piel, cabello y uñas', 45.00),
('Traumatología',           'Lesiones del sistema músculo-esquelético y ortopedia', 50.00),
('Oftalmología',            'Diagnóstico y tratamiento de enfermedades de los ojos', 40.00),
('Neurología',              'Enfermedades del sistema nervioso central y periférico', 70.00),
('Otorrinolaringología',    'Enfermedades del oído, nariz y garganta', 45.00),
('Psiquiatría',             'Diagnóstico y tratamiento de trastornos mentales', 50.00);

-- 10.5 Registros de médicos (referencia a usuarios médicos)
-- Dr. Ricardo Mendoza → Cardiología (usuario_id = 2)
-- Dra. Elena Vazquez → Dermatología (usuario_id = 3)
-- Dr. Roberto Valdivia → Cardiología (usuario_id = 4)
INSERT INTO medicos (usuario_id, numero_colegiatura) VALUES
(2, 'CMP-058401'),
(3, 'CMP-058402'),
(4, 'CMP-058422');

-- 10.6 Asignación de especialidades a médicos
INSERT INTO medico_especialidades (medico_id, especialidad_id) VALUES
(1, 4),  -- Dr. Mendoza → Cardiología
(2, 5),  -- Dra. Vazquez → Dermatología
(3, 4);  -- Dr. Valdivia → Cardiología

-- 10.7 Registros de pacientes
INSERT INTO pacientes (usuario_id, fecha_nacimiento, direccion, grupo_sanguineo, genero,
                       contacto_emergencia_nombre, contacto_emergencia_parentesco, contacto_emergencia_telefono) VALUES
(5, '1990-03-15', 'Av. Los Jazmines 450, Surco', 'O+', 'Masculino',
 'María Pérez', 'Esposa', '912000001'),
(6, '1995-08-22', 'Jr. Junín 122, Miraflores', 'A+', 'Femenino',
 'Jorge López', 'Hermano', '912000002');

-- 10.8 Disponibilidad médica (Lunes a Viernes mañana + tarde, Sábado solo mañana)
-- Dr. Mendoza (medico_id = 1)
INSERT INTO disponibilidad_medica (medico_id, dia_semana, hora_inicio, hora_fin, duracion_consulta_min) VALUES
(1, 'LUNES',    '08:00', '13:00', 30),
(1, 'LUNES',    '15:00', '19:00', 30),
(1, 'MARTES',   '08:00', '13:00', 30),
(1, 'MARTES',   '15:00', '19:00', 30),
(1, 'MIERCOLES','08:00', '13:00', 30),
(1, 'MIERCOLES','15:00', '19:00', 30),
(1, 'JUEVES',   '08:00', '13:00', 30),
(1, 'JUEVES',   '15:00', '19:00', 30),
(1, 'VIERNES',  '08:00', '13:00', 30),
(1, 'VIERNES',  '15:00', '19:00', 30),
(1, 'SABADO',   '08:00', '13:00', 30);

-- Dra. Vazquez (medico_id = 2)
INSERT INTO disponibilidad_medica (medico_id, dia_semana, hora_inicio, hora_fin, duracion_consulta_min) VALUES
(2, 'LUNES',    '09:00', '14:00', 30),
(2, 'LUNES',    '16:00', '20:00', 30),
(2, 'MARTES',   '09:00', '14:00', 30),
(2, 'MARTES',   '16:00', '20:00', 30),
(2, 'MIERCOLES','09:00', '14:00', 30),
(2, 'JUEVES',   '09:00', '14:00', 30),
(2, 'JUEVES',   '16:00', '20:00', 30),
(2, 'VIERNES',  '09:00', '14:00', 30),
(2, 'VIERNES',  '16:00', '20:00', 30);

-- Dr. Valdivia (medico_id = 3)
INSERT INTO disponibilidad_medica (medico_id, dia_semana, hora_inicio, hora_fin, duracion_consulta_min) VALUES
(3, 'LUNES',    '08:00', '13:00', 30),
(3, 'LUNES',    '15:00', '19:00', 30),
(3, 'MARTES',   '08:00', '13:00', 30),
(3, 'MARTES',   '15:00', '19:00', 30),
(3, 'MIERCOLES','08:00', '13:00', 30),
(3, 'MIERCOLES','15:00', '19:00', 30),
(3, 'JUEVES',   '08:00', '13:00', 30),
(3, 'JUEVES',   '15:00', '19:00', 30),
(3, 'VIERNES',  '08:00', '13:00', 30),
(3, 'VIERNES',  '15:00', '19:00', 30),
(3, 'SABADO',   '08:00', '13:00', 30);

-- 10.9 Citas de ejemplo
-- Cita CONFIRMADA próxima (Carlos → Dr. Mendoza, Cardiología)
INSERT INTO citas (paciente_id, medico_id, especialidad_id, fecha, hora_inicio, hora_fin, estado, motivo_consulta) VALUES
(1, 1, 4, '2026-06-18', '10:30', '11:00', 'CONFIRMADA', 'Control de presión arterial'),
-- Cita PENDIENTE próxima (Carlos → Dra. Vazquez, Dermatología)
(1, 2, 5, '2026-06-24', '15:45', '16:15', 'PENDIENTE', 'Revisión dermatológica anual'),
-- Cita COMPLETADA pasada (Carlos → Dr. Mendoza, Cardiología)
(1, 1, 4, '2026-05-15', '09:30', '10:00', 'COMPLETADA', 'Chequeo general cardíaco'),
-- Cita COMPLETADA pasada (Carlos → Dra. Vazquez, Medicina General ID=1 — no, Dermatología)
(1, 2, 5, '2026-05-02', '11:00', '11:30', 'COMPLETADA', 'Control dermatológico'),
-- Cita CANCELADA (Carlos → Dra. Vazquez, Dermatología)
(1, 2, 5, '2026-05-10', '09:30', '10:00', 'CANCELADA', 'Consulta de seguimiento');

-- 10.10 Pagos asociados a las citas
INSERT INTO pagos (cita_id, paciente_id, monto, metodo_pago, estado_pago, referencia_pago, fecha_pago) VALUES
(1, 1, 60.00, 'TARJETA_CREDITO', 'PAGADO', 'REF-2026-001', '2026-06-10 14:30:00'),
(2, 1, 45.00, 'YAPE', 'PENDIENTE', NULL, NULL),
(3, 1, 60.00, 'EFECTIVO', 'PAGADO', 'REF-2026-002', '2026-05-15 09:00:00'),
(4, 1, 45.00, 'PLIN', 'PAGADO', 'REF-2026-003', '2026-05-02 10:30:00'),
(5, 1, 45.00, 'TARJETA_DEBITO', 'REEMBOLSADO', 'REF-2026-004', '2026-05-08 16:00:00');

-- 10.11 Notificaciones de ejemplo
INSERT INTO notificaciones (cita_id, usuario_id, tipo, asunto, mensaje, enviado, fecha_envio) VALUES
(1, 5, 'EMAIL', 'Cita Confirmada', 'Tu cita con el Dr. Ricardo Mendoza para el 18 de Junio ha sido confirmada exitosamente.', TRUE, '2026-06-10 14:35:00'),
(2, 5, 'EMAIL', 'Recordatorio de Cita', 'Recuerda que tu cita de seguimiento es en menos de 24 horas.', TRUE, '2026-06-23 10:00:00'),
(3, 5, 'EMAIL', 'Resultados Listos', 'Tus análisis de sangre ya están disponibles en la sección de documentos.', TRUE, '2026-05-16 09:15:00'),
(1, 5, 'EMAIL', 'Seguridad', 'Nuevo inicio de sesión detectado en un dispositivo desconocido.', FALSE, NULL);

-- -------------------------------------------------------
-- FIN DEL SCRIPT
-- -------------------------------------------------------
