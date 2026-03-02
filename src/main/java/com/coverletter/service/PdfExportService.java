package com.coverletter.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service d'export PDF pour les lettres de motivation.
 * Mise en page française :
 *   Expéditeur (gauche) → Destinataire (droite) → Lieu+Date → Objet → Corps
 */
public class PdfExportService {

    private static final float MARGIN_LEFT = 60;
    private static final float MARGIN_RIGHT = 60;
    private static final float MARGIN_TOP = 60;
    private static final float MARGIN_BOTTOM = 60;
    private static final float FONT_SIZE = 11f;
    private static final float LEADING = 16f;
    private static final float SECTION_SPACING = LEADING * 1.5f;

    // ---- Structure interne pour les sections de la lettre ----
    private static class LetterLayout {
        List<String> senderLines = new ArrayList<>();
        String dateLine = "";
        List<String> recipientLines = new ArrayList<>();
        List<String> bodyLines = new ArrayList<>();
    }

    /**
     * Exporte le contenu de la lettre en PDF format A4.
     */
    public void exportToPdf(String letterContent, File outputFile) throws IOException {
        LetterLayout layout = parseLetterLayout(letterContent.split("\n"));

        try (PDDocument document = new PDDocument()) {
            float pageWidth = PDRectangle.A4.getWidth();
            float pageHeight = PDRectangle.A4.getHeight();
            float usableWidth = pageWidth - MARGIN_LEFT - MARGIN_RIGHT;

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            PDPageContentStream cs = new PDPageContentStream(document, page);

            float yPos = pageHeight - MARGIN_TOP;

            // 1. Bloc expéditeur — gauche
            for (String line : layout.senderLines) {
                drawLeftAligned(cs, line, yPos);
                yPos -= LEADING;
            }

            yPos -= SECTION_SPACING;

            // 2. Bloc destinataire — droite
            for (String line : layout.recipientLines) {
                drawRightAligned(cs, line, yPos, pageWidth);
                yPos -= LEADING;
            }

            yPos -= SECTION_SPACING;

            // 3. Lieu et date — droite
            if (!layout.dateLine.isEmpty()) {
                drawRightAligned(cs, layout.dateLine, yPos, pageWidth);
                yPos -= LEADING;
            }

            yPos -= SECTION_SPACING;

            // 4. Corps (Objet + texte)
            for (String line : layout.bodyLines) {
                if (line.trim().isEmpty()) {
                    yPos -= LEADING;
                    if (yPos < MARGIN_BOTTOM) {
                        cs.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        cs = new PDPageContentStream(document, page);
                        yPos = pageHeight - MARGIN_TOP;
                    }
                    continue;
                }

                List<String> wrappedLines = wrapText(line, usableWidth);
                for (String wrappedLine : wrappedLines) {
                    if (yPos < MARGIN_BOTTOM) {
                        cs.close();
                        page = new PDPage(PDRectangle.A4);
                        document.addPage(page);
                        cs = new PDPageContentStream(document, page);
                        yPos = pageHeight - MARGIN_TOP;
                    }
                    drawLeftAligned(cs, wrappedLine, yPos);
                    yPos -= LEADING;
                }
            }

            cs.close();
            document.save(outputFile);
        }
    }

    // ---- Analyse de la structure de la lettre ----

    private LetterLayout parseLetterLayout(String[] allLines) {
        LetterLayout layout = new LetterLayout();

        int dateIndex = -1;
        int objetIndex = -1;

        // Repérer la ligne de date et la ligne "Objet"
        for (int i = 0; i < allLines.length; i++) {
            String trimmed = allLines[i].trim();
            if (dateIndex == -1 && isDateLine(trimmed)) {
                dateIndex = i;
            }
            if (objetIndex == -1 && trimmed.toLowerCase().startsWith("objet")) {
                objetIndex = i;
                break;
            }
        }

        // Si on ne peut pas déterminer la structure, tout mettre dans le corps
        if (dateIndex == -1 || objetIndex == -1) {
            for (String line : allLines) layout.bodyLines.add(line);
            return layout;
        }

        // Expéditeur = lignes non vides avant la date
        for (int i = 0; i < dateIndex; i++) {
            String trimmed = allLines[i].trim();
            if (trimmed.isEmpty()) continue;
            // Ignorer les préambules IA ("Voici la lettre...")
            String lower = trimmed.toLowerCase();
            if (lower.contains("voici") && (lower.contains("lettre") || lower.contains("motivation"))) continue;
            if (lower.startsWith("bien s\u00fbr") || lower.startsWith("avec plaisir")) continue;
            layout.senderLines.add(trimmed);
        }

        // Date
        layout.dateLine = allLines[dateIndex].trim();

        // Destinataire = lignes non vides entre la date et "Objet"
        for (int i = dateIndex + 1; i < objetIndex; i++) {
            String trimmed = allLines[i].trim();
            if (!trimmed.isEmpty()) {
                layout.recipientLines.add(trimmed);
            }
        }

        // Corps = tout à partir de "Objet"
        for (int i = objetIndex; i < allLines.length; i++) {
            layout.bodyLines.add(allLines[i]);
        }

        return layout;
    }

    private boolean isDateLine(String line) {
        return line.matches(".*\\d{1,2}\\s+(janvier|f\u00e9vrier|mars|avril|mai|juin|juillet|ao\u00fbt|septembre|octobre|novembre|d\u00e9cembre)\\s+\\d{4}.*");
    }

    // ---- Dessin ----

    private void drawLeftAligned(PDPageContentStream cs, String text, float y) throws IOException {
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, FONT_SIZE);
        cs.newLineAtOffset(MARGIN_LEFT, y);
        cs.showText(sanitize(text));
        cs.endText();
    }

    private void drawRightAligned(PDPageContentStream cs, String text, float y, float pageWidth) throws IOException {
        String safeText = sanitize(text);
        float textWidth = PDType1Font.HELVETICA.getStringWidth(safeText) / 1000 * FONT_SIZE;
        float x = pageWidth - MARGIN_RIGHT - textWidth;
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, FONT_SIZE);
        cs.newLineAtOffset(x, y);
        cs.showText(safeText);
        cs.endText();
    }

    private List<String> wrapText(String text, float maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split("\\s+");
        StringBuilder current = new StringBuilder();

        for (String word : words) {
            String testLine = current.length() > 0 ? current + " " + word : word;
            try {
                float width = PDType1Font.HELVETICA.getStringWidth(sanitize(testLine)) / 1000 * FONT_SIZE;
                if (width > maxWidth && current.length() > 0) {
                    lines.add(current.toString());
                    current = new StringBuilder(word);
                } else {
                    current = new StringBuilder(testLine);
                }
            } catch (IOException e) {
                current = new StringBuilder(testLine);
            }
        }

        if (current.length() > 0) {
            lines.add(current.toString());
        }
        if (lines.isEmpty()) {
            lines.add("");
        }
        return lines;
    }

    private String sanitize(String text) {
        return text
                .replace("\u2019", "'")
                .replace("\u2018", "'")
                .replace("\u201c", "\"")
                .replace("\u201d", "\"")
                .replace("\u2013", "-")
                .replace("\u2014", "-")
                .replace("\u2026", "...")
                .replace("\u00a0", " ");
    }
}
