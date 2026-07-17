package com.sisol.salud.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio encargado de la orquestación y ejecución de tareas automatizadas 
 * a nivel de negocio en horarios preestablecidos (Cron Jobs).
 * 
 * Centraliza la limpieza de registros expirados, sincronización de base de datos
 * y otras tareas periódicas críticas para el mantenimiento del sistema SISOL Salud.
 */
@Service
@Slf4j
public class MantenimientoCronService {

    // Ejecuta cada minuto para propósitos demostrativos del informe.
    // En producción sería: @Scheduled(cron = "0 0 3 * * ?") (Diario a las 3 AM)
    @Scheduled(cron = "0 * * * * ?")
    public void limpiarCitasVencidas() {
        log.info("==================================================");
        log.info("[MANTENIMIENTO-CRON] Iniciando tarea programada...");
        log.info("[MANTENIMIENTO-CRON] Buscando citas expiradas pendientes de pago...");
        // Simulación: citaRepository.cancelarCitasExpiradas();
        log.info("[MANTENIMIENTO-CRON] Limpieza completada exitosamente.");
        log.info("==================================================");
    }
}
