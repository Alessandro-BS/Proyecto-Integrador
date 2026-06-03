package com.sisol.salud.controller.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.sisol.salud.dto.response.MedicoResponse;
import com.sisol.salud.service.MedicoService;

@SpringBootTest
@AutoConfigureMockMvc
public class MedicoRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MedicoService medicoService;

    private MedicoResponse medicoResponse;

    @BeforeEach
    void setUp() {
        medicoResponse = new MedicoResponse();
        medicoResponse.setId(1L);
        medicoResponse.setNombre("Dr. Carlos");
        medicoResponse.setApellido("Perez");
        medicoResponse.setCpm("12345");
        medicoResponse.setEspecialidades(Arrays.asList("Cardiología"));
    }

    @Test
    @WithMockUser(roles = "PACIENTE") // Simulamos que estamos logueados para pasar la seguridad si la hubiera
    void listarTodos_DebeRetornarListaDeMedicos() throws Exception {
        // Arrange
        when(medicoService.obtenerTodos()).thenReturn(Arrays.asList(medicoResponse));

        // Act & Assert
        mockMvc.perform(get("/api/medicos")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Dr. Carlos"))
                .andExpect(jsonPath("$[0].especialidades[0]").value("Cardiología"));
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    void buscarPorEspecialidad_DebeRetornarMedicosFiltrados() throws Exception {
        // Arrange
        Long especialidadId = 1L;
        when(medicoService.obtenerPorEspecialidad(especialidadId)).thenReturn(Arrays.asList(medicoResponse));

        // Act & Assert
        mockMvc.perform(get("/api/medicos/especialidad/{id}", especialidadId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].especialidades[0]").value("Cardiología"));
    }
}
