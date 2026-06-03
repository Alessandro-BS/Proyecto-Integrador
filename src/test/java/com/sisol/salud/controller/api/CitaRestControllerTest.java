package com.sisol.salud.controller.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sisol.salud.dto.request.CitaRequest;
import com.sisol.salud.dto.response.CitaResponse;
import com.sisol.salud.service.CitaService;

@SpringBootTest
@AutoConfigureMockMvc
public class CitaRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CitaService citaService;

    @Autowired
    private ObjectMapper objectMapper;

    private CitaRequest citaRequest;
    private CitaResponse citaResponse;

    @BeforeEach
    void setUp() {
        citaRequest = new CitaRequest();
        citaRequest.setPacienteId(1L);
        citaRequest.setMedicoId(1L);
        citaRequest.setEspecialidadId(1L);
        citaRequest.setMetodoPago(com.sisol.salud.model.enums.MetodoPago.TARJETA_CREDITO);
        citaRequest.setFechaHora(LocalDateTime.of(LocalDate.now().plusDays(1), LocalTime.of(10, 0)));
        citaRequest.setMotivoConsulta("Chequeo general");

        citaResponse = new CitaResponse();
        citaResponse.setId(1L);
        citaResponse.setEstado("PENDIENTE");
        citaResponse.setMotivoConsulta("Chequeo general");
    }

    @Test
    @WithMockUser(roles = "PACIENTE") // Importante para pasar la barrera de seguridad de /api/citas
    void reservarCita_ConDatosValidos_DebeRetornar201Created() throws Exception {
        // Arrange
        when(citaService.reservarCita(any(CitaRequest.class))).thenReturn(citaResponse);

        // Act & Assert
        mockMvc.perform(post("/api/citas/reservar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(citaRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.motivoConsulta").value("Chequeo general"));
    }

    @Test
    @WithMockUser(roles = "PACIENTE")
    void reservarCita_ConFechaPasada_DebeRetornar400BadRequest() throws Exception {
        // Arrange
        citaRequest.setFechaHora(LocalDateTime.now().minusDays(1)); // Invalido porque @Future

        // Act & Assert
        mockMvc.perform(post("/api/citas/reservar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(citaRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));
    }
}
