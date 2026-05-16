package com.sisol.salud.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sisol.salud.dto.response.MedicoResponse;
import com.sisol.salud.mapper.MedicoMapper;
import com.sisol.salud.model.entity.Especialidad;
import com.sisol.salud.model.entity.Medico;
import com.sisol.salud.model.entity.Usuario;
import com.sisol.salud.repository.MedicoRepository;

@ExtendWith(MockitoExtension.class)
public class MedicoServiceTest {

    @Mock
    private MedicoRepository medicoRepository;

    @Mock
    private MedicoMapper medicoMapper;

    @InjectMocks
    private MedicoService medicoService;

    private Medico medicoPrueba;
    private MedicoResponse medicoResponsePrueba;
    private Especialidad especialidadPrueba;

    @BeforeEach
    void setUp() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Dr. Carlos");
        usuario.setApellido("Perez");

        especialidadPrueba = new Especialidad();
        especialidadPrueba.setId(1L);
        especialidadPrueba.setNombre("Cardiología");

        medicoPrueba = new Medico();
        medicoPrueba.setId(1L);
        medicoPrueba.setNumeroColegiatura("12345");
        medicoPrueba.setUsuario(usuario);
        medicoPrueba.setEspecialidad(especialidadPrueba);

        medicoResponsePrueba = new MedicoResponse();
        medicoResponsePrueba.setId(1L);
        medicoResponsePrueba.setNombre("Dr. Carlos");
        medicoResponsePrueba.setApellido("Perez");
        medicoResponsePrueba.setCpm("12345");
        medicoResponsePrueba.setEspecialidad("Cardiología");
    }

    @Test
    void obtenerTodos_DebeRetornarListaDeMedicos() {
        // Arrange
        List<Medico> medicos = Arrays.asList(medicoPrueba);
        List<MedicoResponse> responses = Arrays.asList(medicoResponsePrueba);

        when(medicoRepository.findAll()).thenReturn(medicos);
        when(medicoMapper.toResponseList(medicos)).thenReturn(responses);

        // Act
        List<MedicoResponse> resultado = medicoService.obtenerTodos();

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Dr. Carlos", resultado.get(0).getNombre());
        verify(medicoRepository).findAll();
        verify(medicoMapper).toResponseList(medicos);
    }

    @Test
    void obtenerPorEspecialidad_DebeRetornarMedicosFiltrados() {
        // Arrange
        Long idEspecialidad = 1L;
        List<Medico> medicos = Arrays.asList(medicoPrueba);
        List<MedicoResponse> responses = Arrays.asList(medicoResponsePrueba);

        when(medicoRepository.findByEspecialidadId(idEspecialidad)).thenReturn(medicos);
        when(medicoMapper.toResponseList(medicos)).thenReturn(responses);

        // Act
        List<MedicoResponse> resultado = medicoService.obtenerPorEspecialidad(idEspecialidad);

        // Assert
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals("Cardiología", resultado.get(0).getEspecialidad());
        verify(medicoRepository).findByEspecialidadId(idEspecialidad);
    }

    @Test
    void obtenerPorId_CuandoMedicoExiste_DebeRetornarMedicoResponse() {
        // Arrange
        Long idMedico = 1L;
        when(medicoRepository.findById(idMedico)).thenReturn(Optional.of(medicoPrueba));
        when(medicoMapper.toResponse(medicoPrueba)).thenReturn(medicoResponsePrueba);

        // Act
        MedicoResponse resultado = medicoService.obtenerPorId(idMedico);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
        assertEquals("12345", resultado.getCpm());
        verify(medicoRepository).findById(idMedico);
    }

    @Test
    void obtenerPorId_CuandoMedicoNoExiste_DebeLanzarExcepcion() {
        // Arrange
        Long idMedico = 99L;
        when(medicoRepository.findById(idMedico)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            medicoService.obtenerPorId(idMedico);
        });

        assertEquals("Médico no encontrado con ID: 99", exception.getMessage());
        verify(medicoRepository).findById(idMedico);
        verify(medicoMapper, never()).toResponse(any());
    }
}
