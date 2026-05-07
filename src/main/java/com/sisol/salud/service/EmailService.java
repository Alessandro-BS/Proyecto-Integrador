package com.sisol.salud.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String mailFrom;

    @Value("${app.mail.nombre-sistema}")
    private String nombreSistema;

    /**
     * Enviar correo con plantilla HTML de Thymeleaf.
     * 
     * @param to               Correo del destinatario
     * @param subject          Asunto del correo
     * @param templateName     Nombre de la plantilla HTML (sin la extensión .html)
     * @param templateModel    Variables a inyectar en la plantilla
     */
    public void enviarCorreoHtml(String to, String subject, String templateName, Map<String, Object> templateModel) {
        try {
            Context thymeleafContext = new Context();
            thymeleafContext.setVariables(templateModel);
            
            // Procesar la plantilla HTML con las variables
            String htmlBody = templateEngine.process("emails/" + templateName, thymeleafContext);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            // "SISOL Salud <noreply@sisolsalud.pe>"
            helper.setFrom(mailFrom, nombreSistema);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = es HTML

            javaMailSender.send(message);
            log.info("Email enviado exitosamente a: {}", to);

        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            log.error("Error al enviar email a {}: {}", to, e.getMessage());
            // No lanzamos excepción para no romper el flujo principal si el correo falla
        }
    }
}
