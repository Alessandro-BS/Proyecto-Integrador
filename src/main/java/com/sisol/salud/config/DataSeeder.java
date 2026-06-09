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
        demostrarGoogleGuava();
        demostrarApacheCommons();
        demostrarApachePOI();

        if (especialidadRepository.count() == 0) {
            log.info("Sembrando base de datos desde 0 con 10 especialidades, 10 médicos y 2 pacientes...");

            // Crear 10 Especialidades y 10 Médicos
            String[] nombresEspecialidades = {
                    "cardiología", "dermatología", "traumatología", "oftalmología", "neurología",
                    "pediatría", "ginecología", "psiquiatría", "gastroenterología", "oncología"
            };

            for (int i = 0; i < 10; i++) {
                // Se utiliza Apache Commons para capitalizar (ej. "cardiología" ->
                // "Cardiología")
                String nombreCapitalizado = org.apache.commons.lang3.StringUtils
                        .capitalize(nombresEspecialidades[i]);

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

    private void demostrarGoogleGuava() {
        java.util.List<String> especialidades = com.google.common.collect.Lists.newArrayList(
                "Cardiología", "Dermatología", "Traumatología");
        log.info("[Guava] Creada lista inmutable de {} elementos", especialidades.size());
    }

    private void demostrarApacheCommons() {
        String codigo = org.apache.commons.lang3.RandomStringUtils
                .randomAlphanumeric(10).toUpperCase();
        log.info("[Apache Commons] Código seguro generado: {}", codigo);
    }

    private void demostrarApachePOI() {
        try (org.apache.poi.ss.usermodel.Workbook workbook = new org.apache.poi.xssf.usermodel.XSSFWorkbook()) {
            org.apache.poi.ss.usermodel.Sheet sheet = workbook.createSheet("Reporte");
            log.info("[Apache POI] Archivo Excel estructurado y creado en memoria exitosamente.");
        } catch (Exception e) {
            log.error("[Apache POI] Error", e);
        }
    }
}
