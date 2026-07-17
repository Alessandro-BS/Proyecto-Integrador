package com.sisol.salud.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.jdbc.core.JdbcTemplate;

import com.sisol.salud.model.entity.Especialidad;
import com.sisol.salud.model.entity.Medico;
import com.sisol.salud.model.entity.Paciente;
import com.sisol.salud.model.entity.Usuario;
import com.sisol.salud.model.entity.Cita;
import com.sisol.salud.model.enums.Rol;
import com.sisol.salud.model.enums.EstadoCita;
import com.sisol.salud.repository.EspecialidadRepository;
import com.sisol.salud.repository.MedicoRepository;
import com.sisol.salud.repository.PacienteRepository;
import com.sisol.salud.repository.UsuarioRepository;
import com.sisol.salud.repository.CitaRepository;
import com.sisol.salud.repository.DisponibilidadMedicaRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Random;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final MedicoRepository medicoRepository;
    private final PacienteRepository pacienteRepository;
    private final EspecialidadRepository especialidadRepository;
    private final DisponibilidadMedicaRepository disponibilidadMedicaRepository;
    private final CitaRepository citaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void run(String... args) throws Exception {

        log.info("Verificando librerías requeridas...");
        demostrarGoogleGuava();
        demostrarApacheCommons();
        demostrarApachePOI();
        
        // 1. Asegurar que exista el Admin (independiente del seeder general)
        if (usuarioRepository.findByEmail("admin@sisol.com").isEmpty()) {
            Usuario adminUser = Usuario.builder()
                    .dni("00000000")
                    .nombre("Administrador")
                    .apellido("Sistema")
                    .email("admin@sisol.com")
                    .password(passwordEncoder.encode("123456"))
                    .telefono("999888777")
                    .rol(Rol.ADMIN)
                    .activo(true)
                    .build();
            usuarioRepository.save(adminUser);
            log.info("Usuario Admin creado correctamente.");
        }

        if (especialidadRepository.count() == 0) {
            log.info("Sembrando base de datos con médicos, pacientes y citas reales...");

            // Especialidades
            String[] nombresEspecialidades = {
                    "Cardiología", "Dermatología", "Traumatología", "Oftalmología", "Neurología",
                    "Pediatría", "Ginecología", "Psiquiatría", "Gastroenterología", "Oncología"
            };
            
            List<Especialidad> especialidadesGuardadas = new ArrayList<>();
            for (String n : nombresEspecialidades) {
                Especialidad esp = Especialidad.builder()
                        .nombre(n)
                        .descripcion("Atención especializada en " + n)
                        .costo(new BigDecimal("50.00"))
                        .activo(true)
                        .build();
                especialidadesGuardadas.add(especialidadRepository.save(esp));
            }

            // Médicos Reales
            String[][] medicosData = {
                {"Carlos", "Mendoza", "carlos.mendoza@sisol.com", "912345671", "71234561"},
                {"Lucía", "Torres", "lucia.torres@sisol.com", "912345672", "71234562"},
                {"Ricardo", "Guzmán", "ricardo.guzman@sisol.com", "912345673", "71234563"},
                {"Mariana", "Vega", "mariana.vega@sisol.com", "912345674", "71234564"},
                {"Jorge", "Salcedo", "jorge.salcedo@sisol.com", "912345675", "71234565"},
                {"Elena", "Vargas", "elena.vargas@sisol.com", "912345676", "71234566"},
                {"Fernando", "Ruiz", "fernando.ruiz@sisol.com", "912345677", "71234567"},
                {"Valeria", "Castro", "valeria.castro@sisol.com", "912345678", "71234568"},
                {"Roberto", "Navarro", "roberto.navarro@sisol.com", "912345679", "71234569"},
                {"Patricia", "Silva", "patricia.silva@sisol.com", "912345670", "71234560"}
            };
            
            List<Medico> medicosGuardados = new ArrayList<>();
            for (int i = 0; i < medicosData.length; i++) {
                Usuario u = Usuario.builder()
                        .nombre(medicosData[i][0])
                        .apellido(medicosData[i][1])
                        .email(medicosData[i][2])
                        .telefono(medicosData[i][3])
                        .dni(medicosData[i][4])
                        .password(passwordEncoder.encode("123456"))
                        .rol(Rol.MEDICO)
                        .activo(true)
                        .build();
                u = usuarioRepository.save(u);
                
                Medico m = Medico.builder()
                        .usuario(u)
                        .numeroColegiatura("CMP-" + (10000 + i))
                        .especialidades(Set.of(especialidadesGuardadas.get(i)))
                        .build();
                medicosGuardados.add(medicoRepository.save(m));
                
                for (com.sisol.salud.model.enums.DiaSemana dia : com.sisol.salud.model.enums.DiaSemana.values()) {
                    if (dia != com.sisol.salud.model.enums.DiaSemana.SABADO && dia != com.sisol.salud.model.enums.DiaSemana.DOMINGO) {
                        com.sisol.salud.model.entity.DisponibilidadMedica disp = com.sisol.salud.model.entity.DisponibilidadMedica.builder()
                                .medico(m)
                                .diaSemana(dia)
                                .horaInicio(LocalTime.of(8, 0))
                                .horaFin(LocalTime.of(17, 0))
                                .duracionConsultaMin(30)
                                .activo(true)
                                .build();
                        disponibilidadMedicaRepository.save(disp);
                    }
                }
            }

            // Pacientes Reales
            String[][] pacientesData = {
                {"Juan", "Pérez", "juan.perez@gmail.com", "987654321", "40123456"},
                {"Ana", "Gómez", "ana.gomez@gmail.com", "987654322", "40123457"},
                {"Luis", "Ramírez", "luis.ramirez@gmail.com", "987654323", "40123458"},
                {"Carmen", "López", "carmen.lopez@gmail.com", "987654324", "40123459"},
                {"Diego", "Rojas", "diego.rojas@gmail.com", "987654325", "40123450"}
            };
            
            List<Paciente> pacientesGuardados = new ArrayList<>();
            for (String[] pd : pacientesData) {
                Usuario u = Usuario.builder()
                        .nombre(pd[0])
                        .apellido(pd[1])
                        .email(pd[2])
                        .telefono(pd[3])
                        .dni(pd[4])
                        .password(passwordEncoder.encode("123456"))
                        .rol(Rol.PACIENTE)
                        .activo(true)
                        .build();
                u = usuarioRepository.save(u);
                
                Paciente p = new Paciente();
                p.setUsuario(u);
                pacientesGuardados.add(pacienteRepository.save(p));
            }

            // Citas
            Random random = new Random();
            LocalDate today = LocalDate.now();
            
            // Generar ~40 citas distribuidas en los últimos meses, semanas, y esta semana
            for (int i = 0; i < 40; i++) {
                Paciente p = pacientesGuardados.get(random.nextInt(pacientesGuardados.size()));
                Medico m = medicosGuardados.get(random.nextInt(medicosGuardados.size()));
                Especialidad e = m.getEspecialidades().iterator().next();
                
                // Distribución de fechas de reserva (created_at) y citas (fecha)
                int daysAgo = random.nextInt(150); // hasta 5 meses atrás
                LocalDateTime bookingDate = LocalDateTime.now().minusDays(daysAgo).minusHours(random.nextInt(24));
                LocalDate appointmentDate = bookingDate.toLocalDate().plusDays(random.nextInt(5) + 1); // Cita es 1-5 días después de reservar
                
                EstadoCita estado;
                if (appointmentDate.isBefore(today)) {
                    // Citas pasadas
                    int r = random.nextInt(10);
                    if (r < 7) estado = EstadoCita.COMPLETADA;
                    else if (r < 9) estado = EstadoCita.CANCELADA;
                    else estado = EstadoCita.NO_ASISTIO;
                } else if (appointmentDate.isEqual(today)) {
                    estado = EstadoCita.CONFIRMADA;
                } else {
                    // Citas futuras
                    estado = random.nextBoolean() ? EstadoCita.PENDIENTE : EstadoCita.CONFIRMADA;
                }
                
                Cita cita = Cita.builder()
                        .paciente(p)
                        .medico(m)
                        .especialidad(e)
                        .fecha(appointmentDate)
                        .horaInicio(LocalTime.of(9 + random.nextInt(7), 0)) // 9am a 4pm
                        .horaFin(LocalTime.of(9 + random.nextInt(7), 30))
                        .estado(estado)
                        .motivoConsulta("Consulta de rutina por molestias generales.")
                        .observaciones(estado == EstadoCita.COMPLETADA ? "Paciente evaluado. Se recomienda reposo." : null)
                        .build();
                
                cita = citaRepository.save(cita);
                
                // Truco para actualizar created_at usando JdbcTemplate porque Hibernate updatable=false lo impide
                jdbcTemplate.update("UPDATE citas SET created_at = ? WHERE id = ?", bookingDate, cita.getId());
            }

            log.info("¡Sembrado completado con éxito!");
            log.info("Se crearon {} citas.", citaRepository.count());
            log.info("Admin de prueba (contraseña: 123456): admin@sisol.com");
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
