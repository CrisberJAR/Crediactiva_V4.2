package pe.crediactiva.app.service;

import pe.crediactiva.app.dao.RecaudacionAsesorDAO;
import pe.crediactiva.app.dao.impl.RecaudacionAsesorDAOImpl;
import pe.crediactiva.app.model.RecaudacionAsesor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de recaudaciones de asesores
 */
public class RecaudacionService {

    private static final Logger logger = LoggerFactory.getLogger(RecaudacionService.class);

    private final RecaudacionAsesorDAO recaudacionAsesorDAO;
    private final AuditoriaService auditoriaService;

    public RecaudacionService() {
        this.recaudacionAsesorDAO = new RecaudacionAsesorDAOImpl();
        this.auditoriaService = new AuditoriaService();
    }

    /**
     * Registra un borrador de recaudación
     */
    public boolean registrarBorrador(Long idAsesor, Long idCliente, Long idPrestamo, BigDecimal monto) {
        try {
            RecaudacionAsesor recaudacion = new RecaudacionAsesor();
            recaudacion.setIdAsesor(idAsesor);
            recaudacion.setIdCliente(idCliente);
            recaudacion.setIdPrestamo(idPrestamo);
            recaudacion.setFechaRegistro(LocalDateTime.now());
            recaudacion.setMontoRegistrado(monto);
            recaudacion.setValidado(false);

            boolean success = recaudacionAsesorDAO.create(recaudacion);
            if (success) {
                auditoriaService.registrarAuditoria("recaudacion_asesor", 
                    recaudacion.getIdRecaudacion().toString(), 
                    "INSERT", null, recaudacion.toString());
            }
            return success;
        } catch (Exception e) {
            logger.error("Error al registrar borrador de recaudación", e);
            return false;
        }
    }

    /**
     * Valida un borrador de recaudación
     */
    public boolean validarBorrador(Long idRecaudacion) {
        try {
            Optional<RecaudacionAsesor> recaudacionOpt = recaudacionAsesorDAO.findById(idRecaudacion);
            if (!recaudacionOpt.isPresent()) {
                logger.warn("Recaudación no encontrada: " + idRecaudacion);
                return false;
            }

            RecaudacionAsesor recaudacion = recaudacionOpt.get();
            recaudacion.setValidado(true);

            boolean success = recaudacionAsesorDAO.update(recaudacion);
            if (success) {
                auditoriaService.registrarAuditoria("recaudacion_asesor", 
                    idRecaudacion.toString(), 
                    "UPDATE", "validado=false", "validado=true");
            }
            return success;
        } catch (Exception e) {
            logger.error("Error al validar borrador de recaudación: " + idRecaudacion, e);
            return false;
        }
    }

    /**
     * Obtiene borradores pendientes de validación
     */
    public List<RecaudacionAsesor> obtenerBorradoresPendientes() {
        try {
            return recaudacionAsesorDAO.findPendientes();
        } catch (Exception e) {
            logger.error("Error al obtener borradores pendientes", e);
            return List.of();
        }
    }

    /**
     * Obtiene borradores por asesor
     */
    public List<RecaudacionAsesor> obtenerBorradoresPorAsesor(Long idAsesor) {
        try {
            return recaudacionAsesorDAO.findByAsesor(idAsesor);
        } catch (Exception e) {
            logger.error("Error al obtener borradores por asesor: " + idAsesor, e);
            return List.of();
        }
    }

    /**
     * Obtiene recaudación mensual por asesor
     */
    public BigDecimal obtenerRecaudacionMensualPorAsesor(Long idAsesor, int año, int mes) {
        try {
            List<RecaudacionAsesor> recaudaciones = recaudacionAsesorDAO.findByAsesor(idAsesor);
            
            return recaudaciones.stream()
                .filter(r -> r.isValidado())
                .filter(r -> {
                    LocalDateTime fecha = r.getFechaRegistro();
                    return fecha.getYear() == año && fecha.getMonthValue() == mes;
                })
                .map(RecaudacionAsesor::getMontoRegistrado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            logger.error("Error al calcular recaudación mensual para asesor: " + idAsesor, e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * Obtiene borradores del día
     */
    public List<RecaudacionAsesor> obtenerBorradoresDelDia(LocalDate fecha) {
        try {
            return recaudacionAsesorDAO.findByFecha(fecha);
        } catch (Exception e) {
            logger.error("Error al obtener borradores del día: " + fecha, e);
            return List.of();
        }
    }

    /**
     * Obtiene un borrador por ID
     */
    public RecaudacionAsesor obtenerBorradorPorId(Long idRecaudacion) {
        try {
            Optional<RecaudacionAsesor> recaudacionOpt = recaudacionAsesorDAO.findById(idRecaudacion);
            return recaudacionOpt.orElse(null);
        } catch (Exception e) {
            logger.error("Error al obtener borrador por ID: " + idRecaudacion, e);
            return null;
        }
    }

    /**
     * Obtiene borradores por fecha (alias de obtenerBorradoresDelDia)
     */
    public List<RecaudacionAsesor> obtenerBorradoresPorFecha(LocalDate fecha) {
        return obtenerBorradoresDelDia(fecha);
    }

    /**
     * Valida un borrador (sobrecarga para objeto)
     */
    public boolean validarBorrador(RecaudacionAsesor borrador) {
        if (borrador == null || borrador.getIdRecaudacion() == null) {
            return false;
        }
        return validarBorrador(borrador.getIdRecaudacion());
    }

    /**
     * Valida todos los borradores del día
     */
    public boolean validarTodosLosBorradores(LocalDate fecha) {
        try {
            List<RecaudacionAsesor> borradores = obtenerBorradoresDelDia(fecha);
            boolean todosValidados = true;
            
            for (RecaudacionAsesor borrador : borradores) {
                if (!borrador.isValidado()) {
                    boolean validado = validarBorrador(borrador.getIdRecaudacion());
                    if (!validado) {
                        todosValidados = false;
                    }
                }
            }
            
            return todosValidados;
        } catch (Exception e) {
            logger.error("Error al validar todos los borradores del día: " + fecha, e);
            return false;
        }
    }

    /**
     * Elimina un borrador
     */
    public boolean eliminarBorrador(RecaudacionAsesor borrador) {
        if (borrador == null || borrador.getIdRecaudacion() == null) {
            return false;
        }
        
        try {
            boolean success = recaudacionAsesorDAO.delete(borrador.getIdRecaudacion());
            if (success) {
                auditoriaService.registrarAuditoria("recaudacion_asesor", 
                    borrador.getIdRecaudacion().toString(), 
                    "DELETE", borrador.toString(), null);
            }
            return success;
        } catch (Exception e) {
            logger.error("Error al eliminar borrador: " + borrador.getIdRecaudacion(), e);
            return false;
        }
    }

    /**
     * Aplica pagos a cuotas específicas
     */
    public boolean aplicarPagosACuotas(RecaudacionAsesor borrador, List<pe.crediactiva.app.model.Cronograma> cuotas) {
        try {
            if (borrador == null || cuotas == null || cuotas.isEmpty()) {
                return false;
            }

            // Aquí se implementaría la lógica de aplicación de pagos
            // Por ahora, solo validamos el borrador
            boolean success = validarBorrador(borrador);
            
            if (success) {
                // Registrar en auditoría
                auditoriaService.registrarAuditoria("recaudacion_asesor", 
                    borrador.getIdRecaudacion().toString(), 
                    "UPDATE", "validado=false", "validado=true");
            }
            
            return success;
        } catch (Exception e) {
            logger.error("Error al aplicar pagos a cuotas", e);
            return false;
        }
    }

    /**
     * Cierra el día de recaudación
     */
    public boolean cerrarDia(LocalDate fecha) {
        try {
            // Validar todos los borradores pendientes del día
            boolean success = validarTodosLosBorradores(fecha);
            
            if (success) {
                logger.info("Día de recaudación cerrado exitosamente: " + fecha);
            } else {
                logger.warn("No se pudieron validar todos los borradores del día: " + fecha);
            }
            
            return success;
        } catch (Exception e) {
            logger.error("Error al cerrar día de recaudación: " + fecha, e);
            return false;
        }
    }

    /**
     * Obtiene recaudación del día actual
     */
    public BigDecimal obtenerRecaudacionDelDia() {
        return obtenerRecaudacionMensualPorAsesor(null, LocalDate.now().getYear(), LocalDate.now().getMonthValue());
    }

    /**
     * Obtiene recaudación del mes actual
     */
    public BigDecimal obtenerRecaudacionDelMes() {
        return obtenerRecaudacionMensualPorAsesor(null, LocalDate.now().getYear(), LocalDate.now().getMonthValue());
    }
}
