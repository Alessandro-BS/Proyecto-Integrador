package com.sisol.salud.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.sisol.salud.model.entity.Especialidad;
import com.sisol.salud.model.entity.Medico;
import com.sisol.salud.model.entity.Paciente;
import com.sisol.salud.model.entity.Usuario;
import com.sisol.salud.model.enums.Rol;
import com.sisol.salud.repository.EspecialidadRepository;
import com.sisol.salud.repository.MedicoRepository;
import com.sisol.salud.repository.PacienteRepository;
import com.sisol.salud.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final MedicoRepository medicoRepository;
    private final PacienteRepository pacienteRepository;
    private final EspecialidadRepository especialidadRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        log.info("Verificando librerías requeridas...");

        // 1. Google Guava: Creación de listas inmutables y utilidades de colecciones
        java.util.List<String> nombresEspecialidades = com.google.common.collect.Lists.newArrayList(
                "cardiología", "dermatología", "traumatología", "oftalmología", "neurología",
                "pediatría", "ginecología", "psiquiatría", "gastroenterología", "oncología");
        log.info("Google Guava cargado correctamente con {} elementos.", nombresEspecialidades.size());

        // 2. Apache Commons Lang: Manipulación y formateo de cadenas seguras
        String codigoGenerado = org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric(10).toUpperCase();
        log.info("Apache Commons Lang funcionando. Código de sistema generado: {}", codigoGenerado);

        // 3. Apache POI: Generación de archivo Excel de auditoría (demostración)
        try (org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Auditoria de Inicio");
            org.apache.poi.ss.usermodel.Row row = sheet.createRow(0);
            row.createCell(0).setCellValue("Fecha de Inicio Sistema");
            row.createCell(1).setCellValue(java.time.LocalDateTime.now().toString());
            log.info("Apache POI funcionando. Libro de Excel creado exitosamente en memoria.");
        } catch (Exception e) {
            log.error("Error probando Apache POI", e);
        }

        if (especialidadRepository.count() == 0) {
            log.info("Sembrando base de datos desde 0 con 10 especialidades, 10 médicos y 2 pacientes...");

            // Crear 10 Especialidades y 10 Médicos
            for (int i = 0; i < 10; i++) {
                // Se utiliza Apache Commons para capitalizar (ej. "cardiología" ->
                // "Cardiología")
                String nombreCapitalizado = org.apache.commons.lang3.StringUtils
                        .capitalize(nombresEspecialidades.get(i));

                Especialidad esp = Especialidad.builder()
                        .nombre(nombreCapitalizado)
                        .descripcion("Atención especializada en " + nombreCapitalizado)
                        .costo(new BigDecimal("50.00"))
                        .activo(true)
                        .build();
                esp = especialidadRepository.save(esp);

                Usuario usuarioMedico = Usuario.builder()
                        .dni("2000000" + i)
                        .nombre("Dr. " + nombreCapitalizado.substring(0, 4))
                        .apellido("Medico " + i)
                        .email("medico" + i + "@sisol.com")
                        .password(passwordEncoder.encode("123456"))
                        .telefono("90000000" + i)
                        .rol(Rol.MEDICO)
                        .activo(true)
                        .build();
                usuarioMedico = usuarioRepository.save(usuarioMedico);

                Medico medico = Medico.builder()
                        .usuario(usuarioMedico)
                        .numeroColegiatura("CMP-9990" + i)
                        .especialidades(Set.of(esp))
                        .build();
                medicoRepository.save(medico);
            }

            // Crear 2 Pacientes
            for (int i = 1; i <= 2; i++) {
                Usuario usuarioPaciente = Usuario.builder()
                        .dni("1000000" + i)
                        .nombre("Paciente")
                        .apellido("Prueba " + i)
                        .email("paciente" + i + "@sisol.com")
                        .password(passwordEncoder.encode("123456"))
                        .telefono("99999999" + i)
                        .rol(Rol.PACIENTE)
                        .activo(true)
                        .build();
                usuarioPaciente = usuarioRepository.save(usuarioPaciente);

                Paciente paciente = new Paciente();
                paciente.setUsuario(usuarioPaciente);
                pacienteRepository.save(paciente);
            }

            log.info("¡Sembrado completado con éxito!");
            log.info("Médicos de prueba (contraseña: 123456): medico0@sisol.com a medico9@sisol.com");
            log.info("Pacientes de prueba (contraseña: 123456): paciente1@sisol.com y paciente2@sisol.com");
        } else {
            log.info("La base de datos ya contiene registros, omitiendo el sembrado.");
        }
    }
}
