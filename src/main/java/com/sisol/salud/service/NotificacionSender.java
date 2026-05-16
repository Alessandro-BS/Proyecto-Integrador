package com.sisol.salud.service;

import java.util.Map;

/**
 * Interfaz para el envío de notificaciones.
 * Permite aplicar el principio de Inversión de Dependencias (SOLID)
 * de forma que NotificacionService dependa de esta abstracción y no
 * de una implementación concreta (EmailService).
 * 
 * En el futuro, se puede crear un SmsService que implemente esta interfaz.
 */
public interface NotificacionSender {
    
    /**
     * Enviar una notificación con formato de plantilla.
     * 
     * @param destino El destinatario (email, número de teléfono, etc)
     * @param asunto Título o asunto del mensaje
     * @param templateName Nombre de la plantilla a utilizar
     * @param parametros Variables dinámicas para inyectar en la plantilla
     */
    void enviar(String destino, String asunto, String templateName, Map<String, Object> parametros);

}
