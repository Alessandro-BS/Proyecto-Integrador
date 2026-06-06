-- ============================================================
-- SISOL SALUD - Datos Iniciales (Spring Boot seed)
-- Se ejecuta al iniciar la aplicación (spring.sql.init.mode=always)
-- Usa INSERT IGNORE para evitar duplicados
-- Password para todos: Admin@2026
-- ============================================================

-- 1. Administrador Principal
INSERT IGNORE INTO usuarios (dni, nombre, apellido, email, password, telefono, rol, activo, created_at, updated_at) VALUES
('00000001', 'Administrador', 'Sistema', 'admin@sisolsalud.pe',
 '$2a$10$FEEfmL3.q7Hwi5Ehx9E0Zu/Rl145acqMzmxa6JbBglP6omhXx4e7G',
 '999000001', 'ADMIN', true, NOW(), NOW());

-- 2. Usuarios Médicos de ejemplo
INSERT IGNORE INTO usuarios (dni, nombre, apellido, email, password, telefono, rol, activo, created_at, updated_at) VALUES
('10000002', 'Ricardo', 'Mendoza', 'r.mendoza@sisolsalud.pe',
 '$2a$10$FEEfmL3.q7Hwi5Ehx9E0Zu/Rl145acqMzmxa6JbBglP6omhXx4e7G',
 '987654321', 'MEDICO', true, NOW(), NOW()),
('10000003', 'Elena', 'Vazquez', 'e.vazquez@sisolsalud.pe',
 '$2a$10$FEEfmL3.q7Hwi5Ehx9E0Zu/Rl145acqMzmxa6JbBglP6omhXx4e7G',
 '987654322', 'MEDICO', true, NOW(), NOW()),
('10000004', 'Roberto', 'Valdivia Mendoza', 'r.valdivia@sisolsalud.pe',
 '$2a$10$FEEfmL3.q7Hwi5Ehx9E0Zu/Rl145acqMzmxa6JbBglP6omhXx4e7G',
 '987654323', 'MEDICO', true, NOW(), NOW());

-- 3. Usuarios Pacientes de ejemplo
INSERT IGNORE INTO usuarios (dni, nombre, apellido, email, password, telefono, rol, activo, created_at, updated_at) VALUES
('45281900', 'Carlos Alberto', 'Ruiz', 'carlos.ruiz@gmail.com',
 '$2a$10$FEEfmL3.q7Hwi5Ehx9E0Zu/Rl145acqMzmxa6JbBglP6omhXx4e7G',
 '912345678', 'PACIENTE', true, NOW(), NOW()),
('45281901', 'María', 'López García', 'maria.lopez@gmail.com',
 '$2a$10$FEEfmL3.q7Hwi5Ehx9E0Zu/Rl145acqMzmxa6JbBglP6omhXx4e7G',
 '912345679', 'PACIENTE', true, NOW(), NOW());

-- 4. Especialidades médicas
INSERT IGNORE INTO especialidades (nombre, descripcion, costo, activo, created_at) VALUES
('Medicina General', 'Atención primaria y diagnóstico general de enfermedades comunes', 30.00, true, NOW()),
('Pediatría', 'Atención médica especializada para niños y adolescentes', 40.00, true, NOW()),
('Ginecología', 'Salud reproductiva y atención integral de la mujer', 50.00, true, NOW()),
('Cardiología', 'Diagnóstico y tratamiento de enfermedades del corazón', 60.00, true, NOW()),
('Dermatología', 'Tratamiento de enfermedades de la piel, cabello y uñas', 45.00, true, NOW()),
('Traumatología', 'Lesiones del sistema músculo-esquelético y ortopedia', 50.00, true, NOW()),
('Oftalmología', 'Diagnóstico y tratamiento de enfermedades de los ojos', 40.00, true, NOW()),
('Neurología', 'Enfermedades del sistema nervioso central y periférico', 70.00, true, NOW()),
('Otorrinolaringología', 'Enfermedades del oído, nariz y garganta', 45.00, true, NOW()),
('Psiquiatría', 'Diagnóstico y tratamiento de trastornos mentales', 50.00, true, NOW());

-- 5. Registros de médicos
INSERT IGNORE INTO medicos (usuario_id, numero_colegiatura, created_at, updated_at) VALUES
(2, 'CMP-058401', NOW(), NOW()),
(3, 'CMP-058402', NOW(), NOW()),
(4, 'CMP-058422', NOW(), NOW());

-- 6. Asignación de especialidades a médicos
INSERT IGNORE INTO medico_especialidades (medico_id, especialidad_id) VALUES
(1, 4),  -- Dr. Mendoza → Cardiología
(2, 5),  -- Dra. Vazquez → Dermatología
(3, 4);  -- Dr. Valdivia → Cardiología

-- 7. Registros de pacientes
INSERT IGNORE INTO pacientes (usuario_id, fecha_nacimiento, direccion, grupo_sanguineo, genero,
                              contacto_emergencia_nombre, contacto_emergencia_parentesco, contacto_emergencia_telefono,
                              created_at, updated_at) VALUES
(5, '1990-03-15', 'Av. Los Jazmines 450, Surco', 'O+', 'Masculino',
 'María Pérez', 'Esposa', '912000001', NOW(), NOW()),
(6, '1995-08-22', 'Jr. Junín 122, Miraflores', 'A+', 'Femenino',
 'Jorge López', 'Hermano', '912000002', NOW(), NOW());

-- 8. Disponibilidad médica
-- Dr. Mendoza (medico_id = 1): L-V 08:00-13:00 + 15:00-19:00, Sáb 08:00-13:00
INSERT IGNORE INTO disponibilidad_medica (medico_id, dia_semana, hora_inicio, hora_fin, duracion_consulta_min, activo, created_at, updated_at) VALUES
(1, 'LUNES',     '08:00', '13:00', 30, true, NOW(), NOW()),
(1, 'LUNES',     '15:00', '19:00', 30, true, NOW(), NOW()),
(1, 'MARTES',    '08:00', '13:00', 30, true, NOW(), NOW()),
(1, 'MARTES',    '15:00', '19:00', 30, true, NOW(), NOW()),
(1, 'MIERCOLES', '08:00', '13:00', 30, true, NOW(), NOW()),
(1, 'MIERCOLES', '15:00', '19:00', 30, true, NOW(), NOW()),
(1, 'JUEVES',    '08:00', '13:00', 30, true, NOW(), NOW()),
(1, 'JUEVES',    '15:00', '19:00', 30, true, NOW(), NOW()),
(1, 'VIERNES',   '08:00', '13:00', 30, true, NOW(), NOW()),
(1, 'VIERNES',   '15:00', '19:00', 30, true, NOW(), NOW()),
(1, 'SABADO',    '08:00', '13:00', 30, true, NOW(), NOW());

-- Dra. Vazquez (medico_id = 2): L-V 09:00-14:00 + 16:00-20:00
INSERT IGNORE INTO disponibilidad_medica (medico_id, dia_semana, hora_inicio, hora_fin, duracion_consulta_min, activo, created_at, updated_at) VALUES
(2, 'LUNES',     '09:00', '14:00', 30, true, NOW(), NOW()),
(2, 'LUNES',     '16:00', '20:00', 30, true, NOW(), NOW()),
(2, 'MARTES',    '09:00', '14:00', 30, true, NOW(), NOW()),
(2, 'MARTES',    '16:00', '20:00', 30, true, NOW(), NOW()),
(2, 'MIERCOLES', '09:00', '14:00', 30, true, NOW(), NOW()),
(2, 'JUEVES',    '09:00', '14:00', 30, true, NOW(), NOW()),
(2, 'JUEVES',    '16:00', '20:00', 30, true, NOW(), NOW()),
(2, 'VIERNES',   '09:00', '14:00', 30, true, NOW(), NOW()),
(2, 'VIERNES',   '16:00', '20:00', 30, true, NOW(), NOW());

-- Dr. Valdivia (medico_id = 3): L-V 08:00-13:00 + 15:00-19:00, Sáb 08:00-13:00
INSERT IGNORE INTO disponibilidad_medica (medico_id, dia_semana, hora_inicio, hora_fin, duracion_consulta_min, activo, created_at, updated_at) VALUES
(3, 'LUNES',     '08:00', '13:00', 30, true, NOW(), NOW()),
(3, 'LUNES',     '15:00', '19:00', 30, true, NOW(), NOW()),
(3, 'MARTES',    '08:00', '13:00', 30, true, NOW(), NOW()),
(3, 'MARTES',    '15:00', '19:00', 30, true, NOW(), NOW()),
(3, 'MIERCOLES', '08:00', '13:00', 30, true, NOW(), NOW()),
(3, 'MIERCOLES', '15:00', '19:00', 30, true, NOW(), NOW()),
(3, 'JUEVES',    '08:00', '13:00', 30, true, NOW(), NOW()),
(3, 'JUEVES',    '15:00', '19:00', 30, true, NOW(), NOW()),
(3, 'VIERNES',   '08:00', '13:00', 30, true, NOW(), NOW()),
(3, 'VIERNES',   '15:00', '19:00', 30, true, NOW(), NOW()),
(3, 'SABADO',    '08:00', '13:00', 30, true, NOW(), NOW());

-- 9. Citas de ejemplo
INSERT IGNORE INTO citas (paciente_id, medico_id, especialidad_id, fecha, hora_inicio, hora_fin, estado, motivo_consulta, created_at, updated_at) VALUES
(1, 1, 4, '2026-06-18', '10:30', '11:00', 'CONFIRMADA', 'Control de presión arterial', NOW(), NOW()),
(1, 2, 5, '2026-06-24', '15:45', '16:15', 'PENDIENTE', 'Revisión dermatológica anual', NOW(), NOW()),
(1, 1, 4, '2026-05-15', '09:30', '10:00', 'COMPLETADA', 'Chequeo general cardíaco', NOW(), NOW()),
(1, 2, 5, '2026-05-02', '11:00', '11:30', 'COMPLETADA', 'Control dermatológico', NOW(), NOW()),
(1, 2, 5, '2026-05-10', '09:30', '10:00', 'CANCELADA', 'Consulta de seguimiento', NOW(), NOW());

UPDATE usuarios SET password = '$2a$10$FEEfmL3.q7Hwi5Ehx9E0Zu/Rl145acqMzmxa6JbBglP6omhXx4e7G';

-- 10. Pagos asociados a las citas
INSERT IGNORE INTO pagos (cita_id, paciente_id, monto, metodo_pago, estado_pago, referencia_pago, fecha_pago, created_at, updated_at) VALUES
(1, 1, 60.00, 'TARJETA_CREDITO', 'PAGADO', 'REF-2026-001', '2026-06-10 14:30:00', NOW(), NOW()),
(2, 1, 45.00, 'YAPE', 'PENDIENTE', NULL, NULL, NOW(), NOW()),
(3, 1, 60.00, 'EFECTIVO', 'PAGADO', 'REF-2026-002', '2026-05-15 09:00:00', NOW(), NOW()),
(4, 1, 45.00, 'PLIN', 'PAGADO', 'REF-2026-003', '2026-05-02 10:30:00', NOW(), NOW()),
(5, 1, 45.00, 'TARJETA_DEBITO', 'REEMBOLSADO', 'REF-2026-004', '2026-05-08 16:00:00', NOW(), NOW());

-- 11. Notificaciones de ejemplo
INSERT IGNORE INTO notificaciones (cita_id, usuario_id, tipo, asunto, mensaje, enviado, fecha_envio, created_at) VALUES
(1, 5, 'EMAIL', 'Cita Confirmada', 'Tu cita con el Dr. Ricardo Mendoza para el 18 de Junio ha sido confirmada exitosamente.', true, '2026-06-10 14:35:00', NOW()),
(2, 5, 'EMAIL', 'Recordatorio de Cita', 'Recuerda que tu cita de seguimiento es en menos de 24 horas.', true, '2026-06-23 10:00:00', NOW()),
(3, 5, 'EMAIL', 'Resultados Listos', 'Tus análisis de sangre ya están disponibles en la sección de documentos.', true, '2026-05-16 09:15:00', NOW()),
(1, 5, 'EMAIL', 'Seguridad', 'Nuevo inicio de sesión detectado en un dispositivo desconocido.', false, NULL, NOW());