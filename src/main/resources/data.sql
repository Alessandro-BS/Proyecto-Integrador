-- Especialidades médicas comunes en SISOL
INSERT IGNORE INTO especialidad (nombre, descripcion, activo) VALUES
('Medicina General', 'Atención primaria y diagnóstico general', 1),
('Pediatría', 'Atención médica para niños y adolescentes', 1),
('Ginecología', 'Salud reproductiva femenina', 1),
('Cardiología', 'Enfermedades del corazón y sistema circulatorio', 1),
('Dermatología', 'Enfermedades de la piel', 1),
('Oftalmología', 'Enfermedades y cirugía de los ojos', 1),
('Traumatología', 'Lesiones del sistema musculoesquelético', 1),
('Odontología', 'Salud bucal y dental', 1),
('Neurología', 'Enfermedades del sistema nervioso', 1),
('Psicología', 'Salud mental y bienestar emocional', 1);

-- Consultorios
INSERT IGNORE INTO consultorio (numero, piso, descripcion, activo) VALUES
('101', 1, 'Consultorio de Medicina General', 1),
('102', 1, 'Consultorio de Pediatría', 1),
('201', 2, 'Consultorio de Cardiología', 1),
('202', 2, 'Consultorio de Ginecología', 1),
('301', 3, 'Consultorio de Dermatología', 1);

-- Usuario administrador base (password temporal en texto plano hasta Sprint 5)
INSERT IGNORE INTO usuario (email, password, rol, activo, fecha_creacion) VALUES
('admin@sisol.gob.pe', 'admin123', 'ADMIN', 1, NOW());
