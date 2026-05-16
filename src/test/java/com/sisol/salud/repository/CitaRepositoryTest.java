package com.sisol.salud.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import com.sisol.salud.model.entity.Cita;
import com.sisol.salud.model.entity.Especialidad;
import com.sisol.salud.model.entity.Medico;
import com.sisol.salud.model.entity.Paciente;
import com.sisol.salud.model.entity.Usuario;
import com.sisol.salud.model.enums.EstadoCita;
import com.sisol.salud.model.enums.Rol;

@DataJpaTest
@ActiveProfiles("test")
public class CitaRepositoryTest {

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Paciente paciente;
    private Medico medico;
    private LocalDate hoy = LocalDate.now();

    @BeforeEach
    void setUp() {
        // 1. Crear Especialidad
        Especialidad esp = new Especialidad();
        esp.setNombre("Pediatría");
        esp.setDescripcion("Niños");
        esp = entityManager.persistAndFlush(esp);

        // 2. Crear Usuarios
        Usuario userP = Usuario.builder()
                .dni("10000001")
                .nombre("Paciente")
                .apellido("Test")
                .email("paciente@test.com")
                .password("pass")
                .rol(Rol.PACIENTE)
                .build();
        userP = entityManager.persistAndFlush(userP);

        Usuario userM = Usuario.builder()
                .dni("20000001")
                .nombre("Medico")
                .apellido("Test")
                .email("medico@test.com")
                .password("pass")
                .rol(Rol.MEDICO)
                .build();
        userM = entityManager.persistAndFlush(userM);

        // 3. Crear Paciente y Medico
        paciente = new Paciente();
        paciente.setUsuario(userP);
        paciente = entityManager.persistAndFlush(paciente);

        medico = Medico.builder()
                .usuario(userM)
                .especialidad(esp)
                .numeroColegiatura("CMP-999")
                .build();
        medico = entityManager.persistAndFlush(medico);

        // 4. Crear Cita
        Cita cita = Cita.builder()
                .paciente(paciente)
                .medico(medico)
                .fecha(hoy)
                .horaInicio(LocalTime.of(9, 0))
                .horaFin(LocalTime.of(9, 30))
                .estado(EstadoCita.PENDIENTE)
                .build();
        entityManager.persistAndFlush(cita);
    }

    @Test
    void findByPacienteId_DebeRetornarCitasDelPaciente() {
        List<Cita> citas = citaRepository.findByPacienteId(paciente.getId());
        assertEquals(1, citas.size());
        assertEquals("Pediatría", citas.get(0).getMedico().getEspecialidad().getNombre());
    }

    @Test
    void countByEstado_DebeContarCitasCorrectamente() {
        long pendientes = citaRepository.countByEstado(EstadoCita.PENDIENTE);
        long canceladas = citaRepository.countByEstado(EstadoCita.CANCELADA);

        assertEquals(1, pendientes);
        assertEquals(0, canceladas);
    }

    @Test
    void buscarCitasPorMedicoYDia_CuandoHayCita_DebeRetornarla() {
        List<Cita> citas = citaRepository.buscarCitasPorMedicoYDia(medico.getId(), hoy);
        assertEquals(1, citas.size());
    }
}
