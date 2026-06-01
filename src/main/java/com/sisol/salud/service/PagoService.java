package com.sisol.salud.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sisol.salud.dto.response.PagoResponse;
import com.sisol.salud.exception.RecursoNoEncontradoException;
import com.sisol.salud.mapper.PagoMapper;
import com.sisol.salud.model.entity.Cita;
import com.sisol.salud.model.entity.Paciente;
import com.sisol.salud.model.entity.Pago;
import com.sisol.salud.model.enums.EstadoPago;
import com.sisol.salud.model.enums.MetodoPago;
import com.sisol.salud.repository.PagoRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PagoService {

    private final PagoRepository pagoRepository;
    private final PagoMapper pagoMapper;

    @Transactional
    public Pago registrarPago(Cita cita, Paciente paciente, BigDecimal monto, MetodoPago metodoPago, String referenciaPago) {
        log.info("Registrando pago para la cita ID: {} - Paciente ID: {} - Monto: {}", cita.getId(), paciente.getId(), monto);

        Pago pago = Pago.builder()
                .cita(cita)
                .paciente(paciente)
                .monto(monto)
                .metodoPago(metodoPago)
                .estadoPago(EstadoPago.PAGADO)
                .referenciaPago(referenciaPago)
                .fechaPago(LocalDateTime.now())
                .notas("Pago obligatorio registrado al reservar la cita.")
                .build();

        return pagoRepository.save(pago);
    }

    public PagoResponse obtenerPorCita(Long citaId) {
        Pago pago = pagoRepository.findByCitaId(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago no encontrado para la cita ID: " + citaId));
        return pagoMapper.toResponse(pago);
    }

    public List<PagoResponse> obtenerHistorialPaciente(Long pacienteId) {
        List<Pago> pagos = pagoRepository.findByPacienteId(pacienteId);
        return pagoMapper.toResponseList(pagos);
    }

    @Transactional
    public void reembolsarPago(Long citaId) {
        log.info("Reembolsando pago para la cita ID: {}", citaId);
        Pago pago = pagoRepository.findByCitaId(citaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pago no encontrado para la cita ID: " + citaId));
        
        pago.setEstadoPago(EstadoPago.REEMBOLSADO);
        pago.setNotas(pago.getNotas() + "\nReembolsado por cancelación de cita el " + LocalDateTime.now());
        pagoRepository.save(pago);
    }
}
