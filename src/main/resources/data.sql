-- SISOL SALUD - Datos Iniciales

-- 1. Insertar el Administrador Principal si no existe

-- Usamos INSERT IGNORE (MySQL) para evitar errores si ya se insertó previamente.
-- La contraseña es 'Admin@2026' encriptada con BCrypt

INSERT IGNORE INTO usuarios (dni, nombre, apellido, email, password, telefono, rol, activo, created_at, updated_at) 
VALUES ('00000001', 'Administrador', 'Sistema', 'admin@sisolsalud.pe', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '999000001', 'ADMIN', true, NOW(), NOW());

-- 2. Insertar Especialidades si la tabla está vacía
INSERT IGNORE INTO especialidades (nombre, descripcion, activo, created_at) VALUES
('Medicina General', 'Atención primaria y diagnóstico general de enfermedades comunes', true, NOW()),
('Pediatría', 'Atención médica especializada para niños y adolescentes', true, NOW()),
('Ginecología', 'Salud reproductiva y atención integral de la mujer', true, NOW()),
('Cardiología', 'Diagnóstico y tratamiento de enfermedades del corazón', true, NOW()),
('Dermatología', 'Tratamiento de enfermedades de la piel, cabello y uñas', true, NOW()),
('Traumatología', 'Lesiones del sistema músculo-esquelético y ortopedia', true, NOW()),
('Oftalmología', 'Diagnóstico y tratamiento de enfermedades de los ojos', true, NOW()),
('Neurología', 'Enfermedades del sistema nervioso central y periférico', true, NOW()),
('Otorrinolaringología', 'Enfermedades del oído, nariz y garganta', true, NOW()),
('Psiquiatría', 'Diagnóstico y tratamiento de trastornos mentales', true, NOW());