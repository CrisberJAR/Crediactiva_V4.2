package pe.crediactiva.app.service;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Servicio de generación de reportes PDF
 */
public class ReporteService {

    private static final Logger logger = LoggerFactory.getLogger(ReporteService.class);
    private static final String REPORTS_PATH = "data/reports";

    private final PrestamoService prestamoService;
    private final ClienteService clienteService;
    private final AsesorService asesorService;
    private final RecaudacionService recaudacionService;

    public ReporteService() {
        this.prestamoService = new PrestamoService();
        this.clienteService = new ClienteService();
        this.asesorService = new AsesorService();
        this.recaudacionService = new RecaudacionService();
    }

    /**
     * Genera una cartilla de cronograma para un préstamo
     */
    public String generarCartillaCronograma(Long idPrestamo) {
        try {
            // Crear directorio de reportes si no existe
            crearDirectorioReportes();

            // Obtener datos del préstamo
            var prestamo = prestamoService.obtenerPrestamoPorId(idPrestamo);
            if (prestamo == null) {
                logger.error("Préstamo no encontrado: " + idPrestamo);
                return null;
            }

            // Obtener datos del cliente
            var clienteOpt = clienteService.obtenerClientePorId(prestamo.getIdCliente());
            if (!clienteOpt.isPresent()) {
                logger.error("Cliente no encontrado para préstamo: " + idPrestamo);
                return null;
            }
            var cliente = clienteOpt.get();

            // Obtener cronograma
            var cronograma = prestamoService.obtenerCuotasPorPrestamo(idPrestamo);
            if (cronograma.isEmpty()) {
                logger.error("No se encontró cronograma para préstamo: " + idPrestamo);
                return null;
            }

            // Generar PDF
            String nombreArchivo = "cartilla_prestamo_" + idPrestamo + "_" + 
                                 LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
            String rutaArchivo = REPORTS_PATH + File.separator + nombreArchivo;

            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage();
                document.addPage(page);

                PDPageContentStream contentStream = new PDPageContentStream(document, page);
                try {
                    // Configurar fuente
                    var font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                    var fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

                    float yPosition = 750;
                    float leftMargin = 50;
                    float rightMargin = 550;

                    // Título
                    contentStream.beginText();
                    contentStream.setFont(fontBold, 16);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("CARNET DE PAGOS - CREDIACTIVA");
                    contentStream.endText();
                    yPosition -= 30;

                    // Información del cliente
                    contentStream.beginText();
                    contentStream.setFont(fontBold, 12);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("CLIENTE:");
                    contentStream.endText();
                    yPosition -= 20;

                    contentStream.beginText();
                    contentStream.setFont(font, 10);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("DNI: " + cliente.getIdCliente());
                    contentStream.endText();
                    yPosition -= 15;

                    contentStream.beginText();
                    contentStream.setFont(font, 10);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("Nombre: " + cliente.getNombreCompleto());
                    contentStream.endText();
                    yPosition -= 15;

                    contentStream.beginText();
                    contentStream.setFont(font, 10);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("Teléfono: " + cliente.getTelefono());
                    contentStream.endText();
                    yPosition -= 30;

                    // Información del préstamo
                    contentStream.beginText();
                    contentStream.setFont(fontBold, 12);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("PRÉSTAMO:");
                    contentStream.endText();
                    yPosition -= 20;

                    contentStream.beginText();
                    contentStream.setFont(font, 10);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("Monto Solicitado: S/ " + String.format("%.2f", prestamo.getMontoSolicitado()));
                    contentStream.endText();
                    yPosition -= 15;

                    contentStream.beginText();
                    contentStream.setFont(font, 10);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("Monto Desembolsado: S/ " + String.format("%.2f", prestamo.getMontoDesembolsado()));
                    contentStream.endText();
                    yPosition -= 15;

                    contentStream.beginText();
                    contentStream.setFont(font, 10);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("Tasa de Interés: " + prestamo.getTasaInteres() + "%");
                    contentStream.endText();
                    yPosition -= 15;

                    contentStream.beginText();
                    contentStream.setFont(font, 10);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("Período: " + prestamo.getPeriodoMeses() + " meses");
                    contentStream.endText();
                    yPosition -= 15;

                    contentStream.beginText();
                    contentStream.setFont(font, 10);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("Fecha Inicio: " + prestamo.getFechaInicio());
                    contentStream.endText();
                    yPosition -= 30;

                    // Tabla de cronograma
                    contentStream.beginText();
                    contentStream.setFont(fontBold, 12);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("CRONOGRAMA DE PAGOS:");
                    contentStream.endText();
                    yPosition -= 25;

                    // Encabezados de la tabla
                    contentStream.beginText();
                    contentStream.setFont(fontBold, 8);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("Cuota");
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.setFont(fontBold, 8);
                    contentStream.newLineAtOffset(leftMargin + 50, yPosition);
                    contentStream.showText("Fecha");
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.setFont(fontBold, 8);
                    contentStream.newLineAtOffset(leftMargin + 150, yPosition);
                    contentStream.showText("Monto");
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.setFont(fontBold, 8);
                    contentStream.newLineAtOffset(leftMargin + 250, yPosition);
                    contentStream.showText("Estado");
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.setFont(fontBold, 8);
                    contentStream.newLineAtOffset(leftMargin + 350, yPosition);
                    contentStream.showText("Pago");
                    contentStream.endText();
                    yPosition -= 20;

                    // Línea separadora
                    contentStream.moveTo(leftMargin, yPosition + 5);
                    contentStream.lineTo(rightMargin, yPosition + 5);
                    contentStream.stroke();
                    yPosition -= 10;

                    // Datos del cronograma
                    for (var cuota : cronograma) {
                        if (yPosition < 100) {
                            // Nueva página si no hay espacio
                            contentStream.close();
                            PDPage newPage = new PDPage();
                            document.addPage(newPage);
                            contentStream = new PDPageContentStream(document, newPage);
                            yPosition = 750;
                            font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                            fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
                        }

                        contentStream.beginText();
                        contentStream.setFont(font, 8);
                        contentStream.newLineAtOffset(leftMargin, yPosition);
                        contentStream.showText(String.valueOf(cuota.getNumeroCuota()));
                        contentStream.endText();

                        contentStream.beginText();
                        contentStream.setFont(font, 8);
                        contentStream.newLineAtOffset(leftMargin + 50, yPosition);
                        contentStream.showText(cuota.getFechaProgramada().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                        contentStream.endText();

                        contentStream.beginText();
                        contentStream.setFont(font, 8);
                        contentStream.newLineAtOffset(leftMargin + 150, yPosition);
                        contentStream.showText("S/ " + String.format("%.2f", cuota.getMontoCuota()));
                        contentStream.endText();

                        contentStream.beginText();
                        contentStream.setFont(font, 8);
                        contentStream.newLineAtOffset(leftMargin + 250, yPosition);
                        contentStream.showText(cuota.getEstadoCuota().getDescripcion());
                        contentStream.endText();

                        contentStream.beginText();
                        contentStream.setFont(font, 8);
                        contentStream.newLineAtOffset(leftMargin + 350, yPosition);
                        contentStream.showText("_______");
                        contentStream.endText();

                        yPosition -= 15;
                    }

                    // Pie de página
                    yPosition = 100;
                    contentStream.beginText();
                    contentStream.setFont(font, 8);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("Fecha de emisión: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.setFont(font, 8);
                    contentStream.newLineAtOffset(leftMargin, yPosition - 15);
                    contentStream.showText("Este documento es válido para presentar en oficinas de CrediActiva");
                    contentStream.endText();
                } finally {
                    contentStream.close();
                }

                document.save(rutaArchivo);
                logger.info("Cartilla de cronograma generada: " + rutaArchivo);
                return rutaArchivo;
            }

        } catch (IOException e) {
            logger.error("Error al generar cartilla de cronograma", e);
            return null;
        }
    }

    /**
     * Genera constancia de cancelación de préstamo
     */
    public String generarConstanciaCancelacion(Long idPrestamo) {
        try {
            // Crear directorio de reportes si no existe
            crearDirectorioReportes();

            // Obtener datos del préstamo
            var prestamo = prestamoService.obtenerPrestamoPorId(idPrestamo);
            if (prestamo == null) {
                logger.error("Préstamo no encontrado: " + idPrestamo);
                return null;
            }

            // Obtener datos del cliente
            var clienteOpt = clienteService.obtenerClientePorId(prestamo.getIdCliente());
            if (!clienteOpt.isPresent()) {
                logger.error("Cliente no encontrado para préstamo: " + idPrestamo);
                return null;
            }
            var cliente = clienteOpt.get();

            // Generar PDF
            String nombreArchivo = "constancia_cancelacion_" + idPrestamo + "_" + 
                                 LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
            String rutaArchivo = REPORTS_PATH + File.separator + nombreArchivo;

            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage();
                document.addPage(page);

                PDPageContentStream contentStream = new PDPageContentStream(document, page);
                try {
                    var font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                    var fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

                    float yPosition = 750;
                    float leftMargin = 50;

                    // Título
                    contentStream.beginText();
                    contentStream.setFont(fontBold, 16);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("CONSTANCIA DE CANCELACIÓN");
                    contentStream.endText();
                    yPosition -= 40;

                    contentStream.beginText();
                    contentStream.setFont(fontBold, 14);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("CREDIACTIVA");
                    contentStream.endText();
                    yPosition -= 40;

                    // Información del préstamo
                    contentStream.beginText();
                    contentStream.setFont(font, 12);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("Se certifica que el préstamo N° " + idPrestamo + 
                                         " ha sido completamente cancelado.");
                    contentStream.endText();
                    yPosition -= 30;

                    // Datos del cliente
                    contentStream.beginText();
                    contentStream.setFont(fontBold, 12);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("Cliente:");
                    contentStream.endText();
                    yPosition -= 20;

                    contentStream.beginText();
                    contentStream.setFont(font, 10);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("DNI: " + cliente.getIdCliente());
                    contentStream.endText();
                    yPosition -= 15;

                    contentStream.beginText();
                    contentStream.setFont(font, 10);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("Nombre: " + cliente.getNombreCompleto());
                    contentStream.endText();
                    yPosition -= 30;

                    // Detalles del préstamo
                    contentStream.beginText();
                    contentStream.setFont(fontBold, 12);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("Detalles del Préstamo:");
                    contentStream.endText();
                    yPosition -= 20;

                    contentStream.beginText();
                    contentStream.setFont(font, 10);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("Monto Original: S/ " + String.format("%.2f", prestamo.getMontoSolicitado()));
                    contentStream.endText();
                    yPosition -= 15;

                    contentStream.beginText();
                    contentStream.setFont(font, 10);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("Fecha de Cancelación: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    contentStream.endText();
                    yPosition -= 30;

                    // Firma
                    contentStream.beginText();
                    contentStream.setFont(font, 10);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("_________________________");
                    contentStream.endText();
                    yPosition -= 20;

                    contentStream.beginText();
                    contentStream.setFont(font, 10);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("Firma del Administrador");
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.setFont(font, 8);
                    contentStream.newLineAtOffset(leftMargin, 100);
                    contentStream.showText("Fecha de emisión: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    contentStream.endText();
                } finally {
                    contentStream.close();
                }

                document.save(rutaArchivo);
                logger.info("Constancia de cancelación generada: " + rutaArchivo);
                return rutaArchivo;
            }

        } catch (IOException e) {
            logger.error("Error al generar constancia de cancelación", e);
            return null;
        }
    }

    /**
     * Genera reporte de recaudación por asesor
     */
    public String generarReporteRecaudacionAsesor(Long idAsesor, int año, int mes) {
        try {
            // Crear directorio de reportes si no existe
            crearDirectorioReportes();

            // Obtener datos del asesor
            var asesorOpt = asesorService.obtenerAsesorPorId(idAsesor);
            if (!asesorOpt.isPresent()) {
                logger.error("Asesor no encontrado: " + idAsesor);
                return null;
            }
            var asesor = asesorOpt.get();

            // Obtener recaudación del mes
            var recaudacionMes = recaudacionService.obtenerRecaudacionMensualPorAsesor(idAsesor, año, mes);
            var sueldoEstimado = recaudacionMes.multiply(new BigDecimal("0.10")); // 10%

            // Generar PDF
            String nombreArchivo = "reporte_recaudacion_" + idAsesor + "_" + año + "_" + mes + ".pdf";
            String rutaArchivo = REPORTS_PATH + File.separator + nombreArchivo;

            try (PDDocument document = new PDDocument()) {
                PDPage page = new PDPage();
                document.addPage(page);

                PDPageContentStream contentStream = new PDPageContentStream(document, page);
                try {
                    var font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
                    var fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

                    float yPosition = 750;
                    float leftMargin = 50;

                    // Título
                    contentStream.beginText();
                    contentStream.setFont(fontBold, 16);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("REPORTE DE RECAUDACIÓN MENSUAL");
                    contentStream.endText();
                    yPosition -= 40;

                    contentStream.beginText();
                    contentStream.setFont(fontBold, 14);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("CREDIACTIVA");
                    contentStream.endText();
                    yPosition -= 40;

                    // Información del asesor
                    contentStream.beginText();
                    contentStream.setFont(fontBold, 12);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("Asesor:");
                    contentStream.endText();
                    yPosition -= 20;

                    contentStream.beginText();
                    contentStream.setFont(font, 10);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("ID: " + asesor.getIdAsesor());
                    contentStream.endText();
                    yPosition -= 15;

                    contentStream.beginText();
                    contentStream.setFont(font, 10);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("Nombre: " + asesor.getNombreCompleto());
                    contentStream.endText();
                    yPosition -= 30;

                    // Período
                    contentStream.beginText();
                    contentStream.setFont(fontBold, 12);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("Período: " + mes + "/" + año);
                    contentStream.endText();
                    yPosition -= 30;

                    // Resumen
                    contentStream.beginText();
                    contentStream.setFont(fontBold, 12);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("RESUMEN:");
                    contentStream.endText();
                    yPosition -= 20;

                    contentStream.beginText();
                    contentStream.setFont(font, 10);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("Total Recaudado: S/ " + String.format("%.2f", recaudacionMes));
                    contentStream.endText();
                    yPosition -= 15;

                    contentStream.beginText();
                    contentStream.setFont(font, 10);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("Sueldo Estimado (10%): S/ " + String.format("%.2f", sueldoEstimado));
                    contentStream.endText();
                    yPosition -= 30;

                    // Firma
                    contentStream.beginText();
                    contentStream.setFont(font, 10);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("_________________________");
                    contentStream.endText();
                    yPosition -= 20;

                    contentStream.beginText();
                    contentStream.setFont(font, 10);
                    contentStream.newLineAtOffset(leftMargin, yPosition);
                    contentStream.showText("Firma del Administrador");
                    contentStream.endText();

                    contentStream.beginText();
                    contentStream.setFont(font, 8);
                    contentStream.newLineAtOffset(leftMargin, 100);
                    contentStream.showText("Fecha de emisión: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                    contentStream.endText();
                } finally {
                    contentStream.close();
                }

                document.save(rutaArchivo);
                logger.info("Reporte de recaudación generado: " + rutaArchivo);
                return rutaArchivo;
            }

        } catch (IOException e) {
            logger.error("Error al generar reporte de recaudación", e);
            return null;
        }
    }

    /**
     * Crea el directorio de reportes si no existe
     */
    private void crearDirectorioReportes() {
        try {
            File reportsDir = new File(REPORTS_PATH);
            if (!reportsDir.exists()) {
                reportsDir.mkdirs();
                logger.info("Directorio de reportes creado: " + REPORTS_PATH);
            }
        } catch (Exception e) {
            logger.error("Error al crear directorio de reportes", e);
        }
    }
}
