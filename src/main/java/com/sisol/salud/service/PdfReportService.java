package com.sisol.salud.service;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.sisol.salud.model.entity.Cita;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class PdfReportService {

    public byte[] generarInformeCita(Cita cita) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            // Fonts
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Color.BLACK);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.DARK_GRAY);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Color.BLACK);

            // Title
            Paragraph title = new Paragraph("INFORME MÉDICO", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Header details table
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(20f);

            addTableRow(table, "Paciente:", cita.getPaciente().getUsuario().getNombre() + " " + cita.getPaciente().getUsuario().getApellido(), headerFont, bodyFont);
            addTableRow(table, "Médico Tratante:", "Dr. " + cita.getMedico().getUsuario().getNombre() + " " + cita.getMedico().getUsuario().getApellido(), headerFont, bodyFont);
            addTableRow(table, "Especialidad:", cita.getEspecialidad().getNombre(), headerFont, bodyFont);
            
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            addTableRow(table, "Fecha de Consulta:", cita.getFecha().format(dateFormatter), headerFont, bodyFont);
            addTableRow(table, "Hora:", cita.getHoraInicio() + " - " + cita.getHoraFin(), headerFont, bodyFont);
            
            document.add(table);

            // Diagnóstico/Observaciones
            Paragraph obsTitle = new Paragraph("Observaciones del Médico:", headerFont);
            obsTitle.setSpacingAfter(10);
            document.add(obsTitle);

            String observaciones = cita.getObservaciones();
            if (observaciones == null || observaciones.trim().isEmpty()) {
                observaciones = "No se registraron observaciones adicionales para esta cita.";
            }

            Paragraph obsContent = new Paragraph(observaciones, bodyFont);
            obsContent.setSpacingAfter(30);
            document.add(obsContent);

            // Footer
            Paragraph footer = new Paragraph("Este documento es generado automáticamente por el sistema SISOL Salud y es de uso exclusivo del paciente.", FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, Color.GRAY));
            footer.setAlignment(Element.ALIGN_CENTER);
            document.add(footer);

            document.close();
        } catch (Exception ex) {
            throw new RuntimeException("Error al generar el PDF del informe", ex);
        }

        return out.toByteArray();
    }

    private void addTableRow(PdfPTable table, String header, String value, Font headerFont, Font bodyFont) {
        PdfPCell hCell = new PdfPCell(new Phrase(header, headerFont));
        hCell.setBorder(PdfPCell.NO_BORDER);
        hCell.setPadding(5);
        table.addCell(hCell);

        PdfPCell vCell = new PdfPCell(new Phrase(value, bodyFont));
        vCell.setBorder(PdfPCell.NO_BORDER);
        vCell.setPadding(5);
        table.addCell(vCell);
    }
}
