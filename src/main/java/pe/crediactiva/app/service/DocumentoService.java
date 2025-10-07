package pe.crediactiva.app.service;

import pe.crediactiva.app.dao.DocumentoDAO;
import pe.crediactiva.app.dao.impl.DocumentoDAOImpl;
import pe.crediactiva.app.model.Documento;
import pe.crediactiva.app.model.DocumentoDisponible;
import pe.crediactiva.app.model.Prestamo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de documentos
 */
public class DocumentoService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentoService.class);
    private static final String DOCUMENTS_BASE_PATH = "data/docs";

    private final DocumentoDAO documentoDAO;
    private final PrestamoService prestamoService;
    private final AuditoriaService auditoriaService;

    public DocumentoService() {
        this.documentoDAO = new DocumentoDAOImpl();
        this.prestamoService = new PrestamoService();
        this.auditoriaService = new AuditoriaService();
    }

    /**
     * Guarda un documento asociado a un préstamo
     */
    public boolean guardarDocumento(Long idPrestamo, String tipo, File archivo) {
        try {
            // Crear directorio del préstamo si no existe
            Path prestamoDir = Paths.get(DOCUMENTS_BASE_PATH, idPrestamo.toString());
            if (!Files.exists(prestamoDir)) {
                Files.createDirectories(prestamoDir);
            }

            // Generar nombre único para el archivo
            String extension = obtenerExtension(archivo.getName());
            String nombreArchivo = tipo + "_" + System.currentTimeMillis() + extension;
            Path destino = prestamoDir.resolve(nombreArchivo);

            // Copiar el archivo
            Files.copy(archivo.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);

            // Crear registro en la base de datos
            Documento documento = new Documento();
            documento.setIdPrestamo(idPrestamo);
            documento.setTipo(tipo);
            documento.setRuta(destino.toString());
            documento.setSubidoEn(LocalDateTime.now());

            boolean success = documentoDAO.create(documento);
            if (success) {
                auditoriaService.registrarAuditoria("documentos", documento.getIdDocumento().toString(), 
                    "INSERT", null, documento.toString());
                logger.info("Documento guardado exitosamente: " + destino);
                return true;
            }

        } catch (IOException e) {
            logger.error("Error al guardar documento: " + archivo.getName(), e);
        } catch (Exception e) {
            logger.error("Error al procesar documento: " + archivo.getName(), e);
        }
        return false;
    }

    /**
     * Obtiene un documento por ID
     */
    public Optional<Documento> obtenerDocumentoPorId(Long idDocumento) {
        try {
            return documentoDAO.findById(idDocumento);
        } catch (Exception e) {
            logger.error("Error al obtener documento por ID: " + idDocumento, e);
            return Optional.empty();
        }
    }

    /**
     * Obtiene todos los documentos de un préstamo
     */
    public List<Documento> obtenerDocumentosPorPrestamo(Long idPrestamo) {
        try {
            return documentoDAO.findByPrestamo(idPrestamo);
        } catch (Exception e) {
            logger.error("Error al obtener documentos por préstamo: " + idPrestamo, e);
            return List.of();
        }
    }
    
    /**
     * Obtiene documentos disponibles para un cliente
     */
    public List<DocumentoDisponible> obtenerDocumentosDisponiblesCliente(
            Long idCliente, Prestamo prestamo, String tipoDocumento, 
            LocalDate fechaDesde, LocalDate fechaHasta) {
        
        List<DocumentoDisponible> documentos = new ArrayList<>();
        
        try {
            // Obtener préstamos del cliente
            List<Prestamo> prestamos = prestamoService.obtenerPrestamosPorCliente(idCliente);
            
            for (Prestamo p : prestamos) {
                // Filtrar por préstamo específico si se especifica
                if (prestamo != null && !p.getIdPrestamo().equals(prestamo.getIdPrestamo())) {
                    continue;
                }
                
                // Generar documentos disponibles según el tipo
                generarDocumentosDisponibles(p, tipoDocumento, fechaDesde, fechaHasta, documentos);
            }
            
        } catch (Exception e) {
            logger.error("Error al obtener documentos disponibles para cliente", e);
        }
        
        return documentos;
    }
    
    /**
     * Genera los documentos disponibles para un préstamo
     */
    private void generarDocumentosDisponibles(Prestamo prestamo, String tipoDocumento, 
                                            LocalDate fechaDesde, LocalDate fechaHasta,
                                            List<DocumentoDisponible> documentos) {
        
        LocalDate fechaPrestamo = prestamo.getFechaInicio() != null ? 
            prestamo.getFechaInicio() : LocalDate.now();
        
        // Verificar si el préstamo está en el rango de fechas
        if (fechaDesde != null && fechaPrestamo.isBefore(fechaDesde)) return;
        if (fechaHasta != null && fechaPrestamo.isAfter(fechaHasta)) return;
        
        String numeroPrestamo = "PR-" + prestamo.getIdPrestamo();
        
        // Cronograma de pagos
        if (tipoDocumento.equals("Todos") || tipoDocumento.equals("Cronograma de Pagos")) {
            documentos.add(new DocumentoDisponible(
                "Cronograma de Pagos",
                numeroPrestamo,
                fechaPrestamo.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                prestamo.getMontoSolicitado(),
                "Disponible"
            ));
        }
        
        // Contrato de préstamo
        if (tipoDocumento.equals("Todos") || tipoDocumento.equals("Contrato de Préstamo")) {
            documentos.add(new DocumentoDisponible(
                "Contrato de Préstamo",
                numeroPrestamo,
                fechaPrestamo.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                prestamo.getMontoSolicitado(),
                "Disponible"
            ));
        }
        
        // Si el préstamo está finalizado, agregar constancia de cancelación
        if (prestamo.getEstado() == Prestamo.EstadoPrestamo.FINALIZADO) {
            if (tipoDocumento.equals("Todos") || tipoDocumento.equals("Constancia de Cancelación")) {
                documentos.add(new DocumentoDisponible(
                    "Constancia de Cancelación",
                    numeroPrestamo,
                    prestamo.getFechaFin().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    prestamo.getMontoSolicitado(),
                    "Disponible"
                ));
            }
        }
    }

    /**
     * Obtiene documentos por tipo
     */
    public List<Documento> obtenerDocumentosPorTipo(String tipo) {
        try {
            return documentoDAO.findByTipo(tipo);
        } catch (Exception e) {
            logger.error("Error al obtener documentos por tipo: " + tipo, e);
            return List.of();
        }
    }

    /**
     * Elimina un documento
     */
    public boolean eliminarDocumento(Long idDocumento) {
        try {
            Optional<Documento> documentoOpt = documentoDAO.findById(idDocumento);
            if (!documentoOpt.isPresent()) {
                logger.warn("Documento no encontrado: " + idDocumento);
                return false;
            }

            Documento documento = documentoOpt.get();
            
            // Eliminar archivo físico
            Path archivoPath = Paths.get(documento.getRuta());
            if (Files.exists(archivoPath)) {
                Files.delete(archivoPath);
            }

            // Eliminar registro de la base de datos
            boolean success = documentoDAO.delete(idDocumento);
            if (success) {
                auditoriaService.registrarAuditoria("documentos", idDocumento.toString(), 
                    "DELETE", documento.toString(), null);
                logger.info("Documento eliminado exitosamente: " + documento.getRuta());
                return true;
            }

        } catch (IOException e) {
            logger.error("Error al eliminar archivo físico del documento: " + idDocumento, e);
        } catch (Exception e) {
            logger.error("Error al eliminar documento: " + idDocumento, e);
        }
        return false;
    }

    /**
     * Verifica si un archivo existe
     */
    public boolean archivoExiste(String ruta) {
        return Files.exists(Paths.get(ruta));
    }

    /**
     * Obtiene el tamaño de un archivo
     */
    public long obtenerTamañoArchivo(String ruta) {
        try {
            return Files.size(Paths.get(ruta));
        } catch (IOException e) {
            logger.error("Error al obtener tamaño del archivo: " + ruta, e);
            return 0;
        }
    }

    /**
     * Obtiene la extensión de un archivo
     */
    private String obtenerExtension(String nombreArchivo) {
        int lastDotIndex = nombreArchivo.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < nombreArchivo.length() - 1) {
            return nombreArchivo.substring(lastDotIndex);
        }
        return "";
    }

    /**
     * Valida el tipo de archivo permitido
     */
    public boolean esTipoPermitido(String nombreArchivo) {
        String extension = obtenerExtension(nombreArchivo).toLowerCase();
        return extension.equals(".pdf") || extension.equals(".jpg") || 
               extension.equals(".jpeg") || extension.equals(".png");
    }

    /**
     * Valida el tamaño máximo del archivo (5MB)
     */
    public boolean esTamañoValido(File archivo) {
        long maxSize = 5 * 1024 * 1024; // 5MB
        return archivo.length() <= maxSize;
    }

    /**
     * Obtiene la ruta completa de un documento
     */
    public String obtenerRutaCompleta(Documento documento) {
        return documento.getRuta();
    }

    /**
     * Crea un directorio para un préstamo si no existe
     */
    public boolean crearDirectorioPrestamo(Long idPrestamo) {
        try {
            Path prestamoDir = Paths.get(DOCUMENTS_BASE_PATH, idPrestamo.toString());
            if (!Files.exists(prestamoDir)) {
                Files.createDirectories(prestamoDir);
                logger.info("Directorio creado para préstamo: " + prestamoDir);
                return true;
            }
            return true;
        } catch (IOException e) {
            logger.error("Error al crear directorio para préstamo: " + idPrestamo, e);
            return false;
        }
    }
}
