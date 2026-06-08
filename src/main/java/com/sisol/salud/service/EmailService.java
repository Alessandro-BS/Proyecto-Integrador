package com.sisol.salud.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService implements NotificacionSender {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from:noreply@sisolsalud.pe}")
    private String mailFrom;

    @Value("${app.mail.nombre-sistema:SISOL Salud}")
    private String sistemaNombre;

    @Async
    @Override
    public void enviar(String destino, String asunto, String templateName, Map<String, Object> parametros) {
        log.info("Preparando notificación (genérica) para: {}", destino);
        try {
            Context context = new Context();
            if (parametros != null) {
                context.setVariables(parametros);
            }
            context.setVariable("sistemaNombre", sistemaNombre);

            String htmlContent = templateEngine.process(templateName, context);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(mailFrom, sistemaNombre);
            helper.setTo(destino);
            helper.setSubject(asunto);
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.info("Notificación enviada exitosamente a: {}", destino);
            
        } catch (Exception e) {
            log.warn("No se pudo enviar el correo a {}", destino);
            log.warn("Error: {}", e.getMessage());
            log.warn("HTML que se hubiera enviado:");
            try {
                Context context = new Context();
                if (parametros != null) context.setVariables(parametros);
                context.setVariable("sistemaNombre", sistemaNombre);
                log.info(templateEngine.process(templateName, context));
            } catch (Exception ignored) {}
        }
    }
}
