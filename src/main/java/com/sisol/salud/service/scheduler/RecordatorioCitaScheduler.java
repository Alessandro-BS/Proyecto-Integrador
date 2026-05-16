package com.sisol.salud.service.scheduler;

import java.time.LocalDate;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.sisol.salud.model.entity.Cita;
import com.sisol.salud.model.enums.EstadoCita;
import com.sisol.salud.repository.CitaRepository;
import com.sisol.salud.service.NotificacionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class RecordatorioCitaScheduler {

    private final CitaRepository citaRepository;
    private final NotificacionService notificacionService;

    /**
     * Se ejecuta todos los días a las 8:00 AM.
     * Cron expression: "0 0 8 * * ?" (segundos minutos horas día-del-mes mes día-de-la-semana)
     */
    @Scheduled(cron = "0 0 8 * * ?")
    public void enviarRecordatoriosDiarios() {
        log.info("Iniciando proceso automático de envío de recordatorios de citas...");

        LocalDate manana = LocalDate.now().plusDays(1);
        
        // Buscar todas las citas PENDIENTES programadas para el día de mañana
        List<Cita> citasManana = citaRepository.findByFechaAndEstado(manana, EstadoCita.PENDIENTE);

        if (citasManana.isEmpty()) {
            log.info("No hay citas programadas para el día de mañana ({}).", manana);
            return;
        }

        log.info("Se encontraron {} citas para mañana. Enviando correos...", citasManana.size());

        int enviosExitosos = 0;
        for (Cita cita : citasManana) {
            try {
                notificacionService.enviarRecordatorioCita(cita);
                enviosExitosos++;
            } catch (Exception e) {
                log.error("Error al enviar recordatorio para la cita ID {}: {}", cita.getId(), e.getMessage());
            }
        }

        log.info("Proceso de recordatorios finalizado. {}/{} correos enviados exitosamente.", enviosExitosos, citasManana.size());
    }
}
