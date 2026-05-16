package com.sisol.salud.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.sisol.salud.model.entity.Especialidad;
import com.sisol.salud.model.entity.Medico;
import com.sisol.salud.model.entity.Usuario;
import com.sisol.salud.model.enums.Rol;

@DataJpaTest
@org.springframework.test.context.ActiveProfiles("test")
public class MedicoRepositoryTest {

    @Autowired
    private MedicoRepository medicoRepository;

    @Autowired
    private TestEntityManager entityManager; // Para persistir entidades de prueba sin usar el repo directamente

    private Especialidad cardiologia;
    private Usuario usuarioMedico;

    @BeforeEach
    void setUp() {
        // 1. Crear y persistir especialidad
        cardiologia = new Especialidad();
        cardiologia.setNombre("Cardiología");
        cardiologia.setDescripcion("Especialidad del corazón");
        cardiologia = entityManager.persistAndFlush(cardiologia);

        // 2. Crear y persistir usuario para el médico
        usuarioMedico = Usuario.builder()
                .dni("12345678")
                .nombre("Juan")
                .apellido("Perez")
                .email("juan.perez@sisol.com")
                .password("encoded_pass")
                .rol(Rol.MEDICO)
                .build();
        usuarioMedico = entityManager.persistAndFlush(usuarioMedico);

        // 3. Crear y persistir médico
        Medico medico = Medico.builder()
                .usuario(usuarioMedico)
                .especialidad(cardiologia)
                .numeroColegiatura("CMP-12345")
                .build();
        entityManager.persistAndFlush(medico);
    }

    @Test
    void findByEspecialidadId_DebeRetornarListaDeMedicos() {
        // Act
        List<Medico> medicos = medicoRepository.findByEspecialidadId(cardiologia.getId());

        // Assert
        assertFalse(medicos.isEmpty());
        assertEquals(1, medicos.size());
        assertEquals("Juan", medicos.get(0).getUsuario().getNombre());
    }

    @Test
    void findByUsuarioId_CuandoExiste_DebeRetornarMedico() {
        // Act
        Optional<Medico> medicoEncontrado = medicoRepository.findByUsuarioId(usuarioMedico.getId());

        // Assert
        assertTrue(medicoEncontrado.isPresent());
        assertEquals("CMP-12345", medicoEncontrado.get().getNumeroColegiatura());
    }

    @Test
    void findByEspecialidadId_CuandoNoHayMedicos_DebeRetornarListaVacia() {
        // Act
        List<Medico> medicos = medicoRepository.findByEspecialidadId(999L);

        // Assert
        assertTrue(medicos.isEmpty());
    }
}
