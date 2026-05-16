package com.sisol.salud.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sisol.salud.model.entity.Cita;
import com.sisol.salud.model.entity.Notificacion;
import com.sisol.salud.model.enums.TipoNotificacion;
import com.sisol.salud.repository.NotificacionRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificacionService {

    private final NotificacionSender notificacionSender;
    private final NotificacionRepository notificacionRepository;

    @Transactional
    public void enviarConfirmacionCita(Cita cita) {
        String asunto = "¡Cita Confirmada! - SISOL Salud";
        String template = "cita-confirmacion";

        Map<String, Object> modelo = new HashMap<>();
        modelo.put("nombrePaciente", cita.getPaciente().getUsuario().getNombre());
        modelo.put("especialidad", cita.getMedico().getEspecialidad().getNombre());
        modelo.put("nombreMedico", cita.getMedico().getUsuario().getNombre() + " " + cita.getMedico().getUsuario().getApellido());
        modelo.put("fecha", cita.getFecha().toString());
        modelo.put("hora", cita.getHoraInicio().toString());

        // Enviar la notificación
        notificacionSender.enviar(cita.getPaciente().getUsuario().getEmail(), asunto, template, modelo);

        // Guardar registro en la base de datos
        guardarRegistroNotificacion(cita, asunto, "Correo de confirmación enviado exitosamente.");
    }

    @Transactional
    public void enviarCancelacionCita(Cita cita) {
        String asunto = "Cita Cancelada - SISOL Salud";
        String template = "cita-cancelacion";

        Map<String, Object> modelo = new HashMap<>();
        modelo.put("nombrePaciente", cita.getPaciente().getUsuario().getNombre());
        modelo.put("especialidad", cita.getMedico().getEspecialidad().getNombre());
        modelo.put("nombreMedico", cita.getMedico().getUsuario().getNombre() + " " + cita.getMedico().getUsuario().getApellido());
        modelo.put("fecha", cita.getFecha().toString());
        modelo.put("hora", cita.getHoraInicio().toString());

        // Enviar la notificación
        notificacionSender.enviar(cita.getPaciente().getUsuario().getEmail(), asunto, template, modelo);

        // Guardar registro en la base de datos
        guardarRegistroNotificacion(cita, asunto, "Correo de cancelación enviado exitosamente.");
    }

    @Transactional
    public void enviarRecordatorioCita(Cita cita) {
        String asunto = "Recordatorio: Tienes una cita mañana - SISOL Salud";
        String template = "cita-recordatorio";

        Map<String, Object> modelo = new HashMap<>();
        modelo.put("nombrePaciente", cita.getPaciente().getUsuario().getNombre());
        modelo.put("especialidad", cita.getMedico().getEspecialidad().getNombre());
        modelo.put("nombreMedico", cita.getMedico().getUsuario().getNombre() + " " + cita.getMedico().getUsuario().getApellido());
        modelo.put("fecha", cita.getFecha().toString());
        modelo.put("hora", cita.getHoraInicio().toString());

        // Enviar la notificación
        notificacionSender.enviar(cita.getPaciente().getUsuario().getEmail(), asunto, template, modelo);

        // Guardar registro en la base de datos
        guardarRegistroNotificacion(cita, asunto, "Correo de recordatorio enviado exitosamente.");
    }

    private void guardarRegistroNotificacion(Cita cita, String asunto, String mensajeLog) {
        Notificacion notificacion = Notificacion.builder()
                .cita(cita)
                .usuario(cita.getPaciente().getUsuario())
                .tipo(TipoNotificacion.EMAIL)
                .asunto(asunto)
                .mensaje(mensajeLog)
                .enviado(true)
                .fechaEnvio(LocalDateTime.now())
                .build();
        
        notificacionRepository.save(notificacion);
        log.info("Registro de notificación guardado en BD para la cita ID: {}", cita.getId());
    }
}
