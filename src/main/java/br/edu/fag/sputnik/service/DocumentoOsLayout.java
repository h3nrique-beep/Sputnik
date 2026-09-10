package br.edu.fag.sputnik.service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.*;

/** Primitivas em pontos A4 compartilhadas por HTML e PDF; excedentes vão para anexos. */
final class DocumentoOsLayout {
    private static final Charset ENCODING = Charset.forName("windows-1252");

    private record Text(double x, double y, double size, boolean bold, String value) {}

    private record Line(double x, double y, double endX, double endY) {}

    private static final class Page {
        final List<Text> texts = new ArrayList<>();
        final List<Line> lines = new ArrayList<>();
    }

    private final List<Page> pages = new ArrayList<>();
    private final List<String> overflow = new ArrayList<>();
    private Page page = new Page();

    DocumentoOsLayout() {
        pages.add(page);
    }

    void text(double x, double y, double size, boolean bold, String value) {
        page.texts.add(new Text(x, y, size, bold, value));
    }

    void line(double x, double y, double endX, double endY) {
        page.lines.add(new Line(x, y, endX, endY));
    }

    void box(double x, double y, double width, double height, String title, List<String> content) {
        line(x, y, x + width, y);
        line(x, y, x, y + height);
        line(x + width, y, x + width, y + height);
        line(x, y + height, x + width, y + height);
        if (title.isEmpty()) return;
        text(x + 6, y + 4, 8.5, true, title);
        line(x, y + 16, x + width, y + 16);
        List<String> lines = new ArrayList<>();
        for (String value : content) lines.addAll(wrap(value, width - 14, 8));
        int capacity = Math.max(1, (int) ((height - 21) / 11));
        boolean exceeds = lines.size() > capacity;
        int count = Math.min(lines.size(), exceeds ? capacity - 1 : capacity);
        for (int i = 0; i < count; i++) text(x + 6, y + 20 + i * 11, 8, false, lines.get(i));
        if (exceeds) {
            text(x + 6, y + 20 + count * 11, 8, true, "Continua no anexo.");
            overflow.add(title);
            overflow.addAll(content);
            overflow.add("");
        }
    }

    void appendix(String title, List<String> content) {
        if (!content.isEmpty()) {
            overflow.add(title);
            overflow.addAll(content);
            overflow.add("");
        }
    }

    void finish(Long id) {
        double y = 0;
        for (String value : overflow)
            for (String row : wrap(value, 547, 9)) {
                if (y == 0 || y > 788) {
                    page = new Page();
                    pages.add(page);
                    text(24, 26, 13, true, "SPUTNIK | OS #" + id + " - CONTINUAÇÃO");
                    line(24, 48, 571, 48);
                    y = 58;
                }
                text(24, y, 9, false, row);
                y += 13;
            }
        for (int i = 0; i < pages.size(); i++) {
            page = pages.get(i);
            text(470, 826, 7, false, "OS #" + id + " | " + (i + 1) + " / " + pages.size());
        }
    }

    private static List<String> wrap(String value, double width, int size) {
        java.awt.Font font = new java.awt.Font("Arial", java.awt.Font.PLAIN, size);
        java.awt.font.FontRenderContext metrics =
                new java.awt.font.FontRenderContext(null, true, true);
        List<String> result = new ArrayList<>();
        for (String paragraph : value.replace('\r', ' ').replace('\t', ' ').split("\n", -1)) {
            String rest = paragraph;
            while (font.getStringBounds(rest, metrics).getWidth() > width * .95) {
                int limit = 1;
                while (limit < rest.length()
                        && font.getStringBounds(rest.substring(0, limit + 1), metrics).getWidth()
                                <= width * .95) limit++;
                int cut = rest.lastIndexOf(' ', limit);
                if (cut <= 0) cut = limit;
                result.add(rest.substring(0, cut));
                rest = rest.substring(cut).stripLeading();
            }
            result.add(rest);
        }
        return result;
    }

    String html(String title) {
        StringBuilder h =
                new StringBuilder(
                        "<!doctype html><html lang=\"pt-BR\"><head><meta"
                                + " charset=\"UTF-8\"><title>");
        h.append(escape(title))
                .append(
                        "</title><link rel=\"stylesheet\""
                            + " href=\"/css/os-documento.css\"></head><body><nav"
                            + " class=\"print-actions\"><button id=\"imprimir-os\">Imprimir ordem"
                            + " de serviço</button><span>Formato A4. Desative os cabeçalhos e"
                            + " rodapés do navegador.</span></nav>");
        for (Page p : pages) {
            h.append("<section class=\"os-sheet\">");
            for (Line l : p.lines)
                h.append("<i aria-hidden=\"true\" style=\"position:absolute;left:")
                        .append(l.x)
                        .append("pt;top:")
                        .append(l.y)
                        .append("pt;width:")
                        .append(l.endX - l.x)
                        .append("pt;height:")
                        .append(l.endY - l.y)
                        .append(
                                "pt;border-top:0.6pt solid #222;border-left:0.6pt solid"
                                        + " #222\"></i>");
            for (Text t : p.texts)
                h.append("<span style=\"position:absolute;white-space:pre;left:")
                        .append(t.x)
                        .append("pt;top:")
                        .append(t.y)
                        .append("pt;font-size:")
                        .append(t.size)
                        .append("pt;font-weight:")
                        .append(t.bold ? 700 : 400)
                        .append("\">")
                        .append(escape(t.value))
                        .append("</span>");
            h.append("</section>");
        }
        return h.append("<script src=\"/js/os-documento.js\" defer></script></body></html>")
                .toString();
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    byte[] pdf() {
        List<String> objects = new ArrayList<>();
        objects.add("<< /Type /Catalog /Pages 2 0 R >>");
        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < pages.size(); i++) kids.append(5 + i * 2).append(" 0 R ");
        objects.add("<< /Type /Pages /Kids [" + kids + "] /Count " + pages.size() + " >>");
        objects.add(
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding"
                        + " >>");
        objects.add(
                "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding"
                        + " /WinAnsiEncoding >>");
        for (Page p : pages) {
            StringBuilder stream = new StringBuilder("0.6 w\n");
            for (Line l : p.lines)
                stream.append(l.x)
                        .append(' ')
                        .append(842 - l.y)
                        .append(" m ")
                        .append(l.endX)
                        .append(' ')
                        .append(842 - l.endY)
                        .append(" l S\n");
            for (Text t : p.texts)
                stream.append("BT /")
                        .append(t.bold ? "FB" : "F")
                        .append(' ')
                        .append(t.size)
                        .append(" Tf ")
                        .append(t.x)
                        .append(' ')
                        .append(842 - t.y - t.size)
                        .append(" Td (")
                        .append(pdfText(t.value))
                        .append(") Tj ET\n");
            objects.add(
                    "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F"
                            + " 3 0 R /FB 4 0 R >> >> /Contents "
                            + (objects.size() + 2)
                            + " 0 R >>");
            objects.add(
                    "<< /Length "
                            + stream.toString().getBytes(ENCODING).length
                            + " >>\nstream\n"
                            + stream
                            + "endstream");
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(out, "%PDF-1.4\n%âãÏÓ\n");
        List<Integer> offsets = new ArrayList<>();
        for (int i = 0; i < objects.size(); i++) {
            offsets.add(out.size());
            write(out, (i + 1) + " 0 obj\n" + objects.get(i) + "\nendobj\n");
        }
        int xref = out.size();
        write(out, "xref\n0 " + (objects.size() + 1) + "\n0000000000 65535 f \n");
        for (int offset : offsets)
            write(out, String.format(Locale.ROOT, "%010d 00000 n \n", offset));
        write(
                out,
                "trailer\n<< /Size "
                        + (objects.size() + 1)
                        + " /Root 1 0 R >>\nstartxref\n"
                        + xref
                        + "\n%%EOF");
        return out.toByteArray();
    }

    private static String pdfText(String s) {
        return s.replace("\\", "\\\\")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replaceAll("[\\p{Cntrl}]", " ");
    }

    private static void write(ByteArrayOutputStream out, String s) {
        out.writeBytes(s.getBytes(ENCODING));
    }
}
