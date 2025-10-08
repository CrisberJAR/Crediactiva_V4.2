package pe.crediactiva.app.service;

import pe.crediactiva.app.dao.CronogramaDAO;
import pe.crediactiva.app.dao.PagoDAO;
import pe.crediactiva.app.dao.impl.CronogramaDAOImpl;
import pe.crediactiva.app.dao.impl.PagoDAOImpl;
import pe.crediactiva.app.model.Cronograma;
import pe.crediactiva.app.model.Pago;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de pagos
 */
public class PagoService {

    private static final Logger logger = LoggerFactory.getLogger(PagoService.class);

    private final PagoDAO pagoDAO;
    private final CronogramaDAO cronogramaDAO;
    private final AuditoriaService auditoriaService;

    public PagoService() {
        this.pagoDAO = new PagoDAOImpl();
        this.cronogramaDAO = new CronogramaDAOImpl();
        this.auditoriaService = new AuditoriaService();
    }

    /**
     * Registra un pago aplicándolo a una cuota específica
     */
    public boolean registrarPago(Long idCuota, Long idCliente, Long idAsesor, BigDecimal montoPagado) {
        try {
            // Verificar que la cuota existe y está pendiente
            Optional<Cronograma> cuotaOpt = cronogramaDAO.findById(idCuota);
            if (!cuotaOpt.isPresent()) {
                logger.warn("Cuota no encontrada: " + idCuota);
                return false;
            }

            Cronograma cuota = cuotaOpt.get();
            if (cuota.getEstadoCuota() != Cronograma.EstadoCuota.PENDIENTE) {
                logger.warn("La cuota ya está pagada: " + idCuota);
                return false;
            }

            // Crear el pago
            Pago pago = new Pago();
            pago.setIdCuota(idCuota);
            pago.setIdCliente(idCliente);
            pago.setIdAsesor(idAsesor);
            pago.setFechaPago(LocalDateTime.now());
            pago.setMontoPagado(montoPagado);

            boolean success = pagoDAO.create(pago);
            if (success) {
                // Marcar la cuota como pagada
                cronogramaDAO.marcarComoPagada(idCuota, LocalDateTime.now().toLocalDate());
                
                // Registrar auditoría
                auditoriaService.registrarAuditoria("pagos", pago.getIdPago().toString(), 
                    "INSERT", null, pago.toString());
                
                auditoriaService.registrarAuditoria("cronograma", idCuota.toString(), 
                    "UPDATE", "estado_cuota=PENDIENTE", "estado_cuota=PAGADA");
                
                logger.info("Pago registrado exitosamente: " + pago.getIdPago());
                return true;
            }

        } catch (Exception e) {
            logger.error("Error al registrar pago", e);
        }
        return false;
    }

    /**
     * Registra un pago aplicándolo a una cuota específica con fecha personalizada
     */
    public boolean registrarPagoConFecha(Long idCuota, Long idCliente, Long idAsesor, BigDecimal montoPagado, java.time.LocalDate fechaPago) {
        try {
            // Verificar que la cuota existe y está pendiente
            Optional<Cronograma> cuotaOpt = cronogramaDAO.findById(idCuota);
            if (!cuotaOpt.isPresent()) {
                logger.warn("Cuota no encontrada: " + idCuota);
                return false;
            }

            Cronograma cuota = cuotaOpt.get();
            if (cuota.getEstadoCuota() != Cronograma.EstadoCuota.PENDIENTE) {
                logger.warn("La cuota ya está pagada: " + idCuota);
                return false;
            }

            // Crear el pago
            Pago pago = new Pago();
            pago.setIdCuota(idCuota);
            pago.setIdCliente(idCliente);
            pago.setIdAsesor(idAsesor);
            pago.setFechaPago(fechaPago.atStartOfDay());
            pago.setMontoPagado(montoPagado);

            boolean success = pagoDAO.create(pago);
            if (success) {
                // Marcar la cuota como pagada con la fecha real del cobro
                cronogramaDAO.marcarComoPagada(idCuota, fechaPago);
                
                // Registrar auditoría
                auditoriaService.registrarAuditoria("pagos", pago.getIdPago().toString(), 
                    "INSERT", null, pago.toString());
                
                auditoriaService.registrarAuditoria("cronograma", idCuota.toString(), 
                    "UPDATE", "estado_cuota=PENDIENTE", "estado_cuota=PAGADA fecha_pago_real=" + fechaPago);
                
                logger.info("Pago registrado exitosamente con fecha real: " + pago.getIdPago() + " - Fecha: " + fechaPago);
                return true;
            }

        } catch (Exception e) {
            logger.error("Error al registrar pago con fecha", e);
        }
        return false;
    }

    /**
     * Aplica un pago a múltiples cuotas (para pagos parciales o adelantos)
     */
    public boolean aplicarPagoAMultiplesCuotas(List<Long> idsCuotas, Long idCliente, Long idAsesor, BigDecimal montoTotal) {
        try {
            if (idsCuotas.isEmpty()) {
                logger.warn("No se proporcionaron cuotas para aplicar el pago");
                return false;
            }

            // Verificar que todas las cuotas existen y están pendientes
            for (Long idCuota : idsCuotas) {
                Optional<Cronograma> cuotaOpt = cronogramaDAO.findById(idCuota);
                if (!cuotaOpt.isPresent()) {
                    logger.warn("Cuota no encontrada: " + idCuota);
                    return false;
                }
                
                Cronograma cuota = cuotaOpt.get();
                if (cuota.getEstadoCuota() != Cronograma.EstadoCuota.PENDIENTE) {
                    logger.warn("La cuota ya está pagada: " + idCuota);
                    return false;
                }
            }

            // Calcular el monto por cuota
            BigDecimal montoPorCuota = montoTotal.divide(BigDecimal.valueOf(idsCuotas.size()), 2, java.math.RoundingMode.HALF_UP);
            
            // Aplicar el pago a cada cuota
            for (Long idCuota : idsCuotas) {
                boolean success = registrarPago(idCuota, idCliente, idAsesor, montoPorCuota);
                if (!success) {
                    logger.error("Error al aplicar pago a la cuota: " + idCuota);
                    return false;
                }
            }

            logger.info("Pago aplicado exitosamente a " + idsCuotas.size() + " cuotas");
            return true;

        } catch (Exception e) {
            logger.error("Error al aplicar pago a múltiples cuotas", e);
            return false;
        }
    }

    /**
     * Obtiene pagos por cliente
     */
    public List<Pago> obtenerPagosPorCliente(Long idCliente) {
        try {
            return pagoDAO.findByCliente(idCliente);
        } catch (Exception e) {
            logger.error("Error al obtener pagos por cliente: " + idCliente, e);
            return List.of();
        }
    }

    /**
     * Obtiene pagos por asesor
     */
    public List<Pago> obtenerPagosPorAsesor(Long idAsesor) {
        try {
            return pagoDAO.findByAsesor(idAsesor);
        } catch (Exception e) {
            logger.error("Error al obtener pagos por asesor: " + idAsesor, e);
            return List.of();
        }
    }

    /**
     * Obtiene pagos por fecha
     */
    public List<Pago> obtenerPagosPorFecha(java.time.LocalDate fecha) {
        try {
            return pagoDAO.findByFecha(fecha);
        } catch (Exception e) {
            logger.error("Error al obtener pagos por fecha: " + fecha, e);
            return List.of();
        }
    }

    /**
     * Calcula el total de pagos de un cliente en un período
     */
    public BigDecimal calcularTotalPagosCliente(Long idCliente, java.time.LocalDate fechaInicio, java.time.LocalDate fechaFin) {
        try {
            List<Pago> pagos = pagoDAO.findByClienteAndFecha(idCliente, fechaInicio, fechaFin);
            return pagos.stream()
                .map(Pago::getMontoPagado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            logger.error("Error al calcular total de pagos del cliente: " + idCliente, e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * Calcula el total de pagos de un asesor en un período
     */
    public BigDecimal calcularTotalPagosAsesor(Long idAsesor, java.time.LocalDate fechaInicio, java.time.LocalDate fechaFin) {
        try {
            List<Pago> pagos = pagoDAO.findByAsesor(idAsesor);
            return pagos.stream()
                .filter(p -> !p.getFechaPago().toLocalDate().isBefore(fechaInicio) && 
                           !p.getFechaPago().toLocalDate().isAfter(fechaFin))
                .map(Pago::getMontoPagado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            logger.error("Error al calcular total de pagos del asesor: " + idAsesor, e);
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * Obtiene el último pago de un cliente
     */
    public Optional<Pago> obtenerUltimoPagoCliente(Long idCliente) {
        try {
            List<Pago> pagos = pagoDAO.findByCliente(idCliente);
            return pagos.stream().findFirst();
        } catch (Exception e) {
            logger.error("Error al obtener último pago del cliente: " + idCliente, e);
            return Optional.empty();
        }
    }
    
    /**
     * Calcula el total pagado por un cliente
     */
    public BigDecimal calcularTotalPagadoCliente(Long idCliente) {
        try {
            List<Pago> pagos = pagoDAO.findByCliente(idCliente);
            return pagos.stream()
                .map(Pago::getMontoPagado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            logger.error("Error al calcular total pagado del cliente: " + idCliente, e);
            return BigDecimal.ZERO;
        }
    }
    
    /**
     * Obtiene pagos pendientes de validación
     */
    public List<Pago> obtenerPagosPendientesValidacion() {
        try {
            return pagoDAO.findPendientesValidacion();
        } catch (Exception e) {
            logger.error("Error al obtener pagos pendientes de validación", e);
            return List.of();
        }
    }
    
    /**
     * Actualiza un pago existente
     */
    public boolean actualizarPago(Pago pago) {
        try {
            return pagoDAO.update(pago);
        } catch (Exception e) {
            logger.error("Error al actualizar pago: " + pago.getIdPago(), e);
            return false;
        }
    }
    
    /**
     * Elimina un pago
     */
    public boolean eliminarPago(Long idPago) {
        try {
            return pagoDAO.delete(idPago);
        } catch (Exception e) {
            logger.error("Error al eliminar pago: " + idPago, e);
            return false;
        }
    }
}
