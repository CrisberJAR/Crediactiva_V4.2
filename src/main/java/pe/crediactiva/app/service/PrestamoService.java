package pe.crediactiva.app.service;

import pe.crediactiva.app.dao.ClienteDAO;
import pe.crediactiva.app.dao.CronogramaDAO;
import pe.crediactiva.app.dao.PrestamoDAO;
import pe.crediactiva.app.dao.impl.ClienteDAOImpl;
import pe.crediactiva.app.dao.impl.CronogramaDAOImpl;
import pe.crediactiva.app.dao.impl.PrestamoDAOImpl;
import pe.crediactiva.app.model.Cliente;
import pe.crediactiva.app.model.Cronograma;
import pe.crediactiva.app.model.Prestamo;
import pe.crediactiva.app.util.FechaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de préstamos
 */
public class PrestamoService {
    
    private static final Logger logger = LoggerFactory.getLogger(PrestamoService.class);
    
    private final PrestamoDAO prestamoDAO;
    private final ClienteDAO clienteDAO;
    private final CronogramaDAO cronogramaDAO;
    private final AuditoriaService auditoriaService;
    
    public PrestamoService() {
        this.prestamoDAO = new PrestamoDAOImpl();
        this.clienteDAO = new ClienteDAOImpl();
        this.cronogramaDAO = new CronogramaDAOImpl();
        this.auditoriaService = new AuditoriaService();
    }
    
    /**
     * Crea una nueva solicitud de préstamo
     */
    public boolean crearSolicitud(Prestamo prestamo) {
        try {
            // Validaciones de negocio
            if (!validarSolicitud(prestamo)) {
                return false;
            }
            
            // Calcular monto desembolsado (monto solicitado - 10%)
            BigDecimal capitalRetenido = prestamo.getMontoSolicitado().multiply(new BigDecimal("0.10"));
            prestamo.setMontoDesembolsado(prestamo.getMontoSolicitado().subtract(capitalRetenido));
            
            // Crear el préstamo
            boolean success = prestamoDAO.create(prestamo);
            
            if (success) {
                logger.info("Solicitud de préstamo creada: " + prestamo.getIdPrestamo());
                auditoriaService.registrarAuditoria("prestamos", prestamo.getIdPrestamo().toString(), 
                    "INSERT", null, prestamo.toString());
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error al crear solicitud de préstamo", e);
            return false;
        }
    }
    
    /**
     * Aprueba un préstamo y genera el cronograma
     */
    public boolean aprobarPrestamo(Long idPrestamo, BigDecimal tasaInteres, int periodoMeses, 
                                   Prestamo.TipoPago tipoPago, LocalDate fechaInicio) {
        try {
            Optional<Prestamo> prestamoOpt = prestamoDAO.findById(idPrestamo);
            if (!prestamoOpt.isPresent()) {
                logger.warn("Préstamo no encontrado: " + idPrestamo);
                return false;
            }
            
            Prestamo prestamo = prestamoOpt.get();
            
            // Validar que esté pendiente
            if (prestamo.getEstado() != Prestamo.EstadoPrestamo.PENDIENTE) {
                logger.warn("Solo se pueden aprobar préstamos pendientes: " + idPrestamo);
                return false;
            }
            
            // Actualizar datos del préstamo
            prestamo.setTasaInteres(tasaInteres);
            prestamo.setPeriodoMeses(periodoMeses);
            prestamo.setTipoPago(tipoPago);
            prestamo.setFechaInicio(fechaInicio);
            prestamo.setEstado(Prestamo.EstadoPrestamo.ACTIVO);
            
            // Calcular fecha fin y monto desembolsado
            prestamo.setFechaFin(calcularFechaFin(fechaInicio, periodoMeses, tipoPago));
            BigDecimal capitalRetenido = prestamo.getMontoSolicitado().multiply(new BigDecimal("0.10"));
            prestamo.setMontoDesembolsado(prestamo.getMontoSolicitado().subtract(capitalRetenido));
            
            // Actualizar préstamo
            boolean success = prestamoDAO.update(prestamo);
            
            if (success) {
                // Generar cronograma
                generarCronograma(prestamo);
                
                // Registrar abono de capital (10%)
                registrarAbonoCapital(prestamo.getIdCliente(), capitalRetenido);
                
                logger.info("Préstamo aprobado exitosamente: " + idPrestamo);
                auditoriaService.registrarAuditoria("prestamos", idPrestamo.toString(), 
                    "UPDATE", "PENDIENTE", "ACTIVO");
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error al aprobar préstamo: " + idPrestamo, e);
            return false;
        }
    }
    
    /**
     * Rechaza un préstamo
     */
    public boolean rechazarPrestamo(Long idPrestamo, String motivo) {
        try {
            Optional<Prestamo> prestamoOpt = prestamoDAO.findById(idPrestamo);
            if (!prestamoOpt.isPresent()) {
                return false;
            }
            
            Prestamo prestamo = prestamoOpt.get();
            prestamo.setEstado(Prestamo.EstadoPrestamo.RECHAZADO);
            prestamo.setObservacion(motivo);
            
            boolean success = prestamoDAO.update(prestamo);
            
            if (success) {
                logger.info("Préstamo rechazado: " + idPrestamo + " - Motivo: " + motivo);
                auditoriaService.registrarAuditoria("prestamos", idPrestamo.toString(), 
                    "UPDATE", "PENDIENTE", "RECHAZADO");
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error al rechazar préstamo: " + idPrestamo, e);
            return false;
        }
    }
    
    /**
     * Genera el cronograma de pagos para un préstamo
     */
    private void generarCronograma(Prestamo prestamo) {
        try {
            BigDecimal montoTotal = calcularMontoTotal(prestamo);
            int numeroCuotas = calcularNumeroCuotas(prestamo.getPeriodoMeses(), prestamo.getTipoPago().name().toLowerCase());
            BigDecimal montoCuota = montoTotal.divide(new BigDecimal(numeroCuotas), 2, RoundingMode.HALF_UP);
            
            LocalDate fechaActual = prestamo.getFechaInicio();
            
            // Generar cuotas según el tipo de pago
            for (int i = 1; i <= numeroCuotas; i++) {
                Cronograma cuota = new Cronograma();
                cuota.setIdPrestamo(prestamo.getIdPrestamo());
                cuota.setNumeroCuota(i);
                cuota.setFechaProgramada(calcularFechaPago(fechaActual, i, prestamo.getTipoPago()));
                cuota.setMontoCuota(montoCuota);
                
                cronogramaDAO.create(cuota);
            }
            
            logger.info("Cronograma generado para préstamo: " + prestamo.getIdPrestamo() + 
                       " - " + numeroCuotas + " cuotas de " + montoCuota + " cada una");
            
        } catch (Exception e) {
            logger.error("Error al generar cronograma para préstamo: " + prestamo.getIdPrestamo(), e);
        }
    }
    
    /**
     * Calcula el monto total a pagar (monto + intereses)
     */
    private BigDecimal calcularMontoTotal(Prestamo prestamo) {
        BigDecimal intereses = prestamo.getMontoSolicitado()
            .multiply(prestamo.getTasaInteres())
            .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
        
        return prestamo.getMontoSolicitado().add(intereses);
    }
    
    
    /**
     * Calcula la fecha de fin del préstamo según el tipo de pago
     */
    private LocalDate calcularFechaFin(LocalDate fechaInicio, int periodoMeses, Prestamo.TipoPago tipoPago) {
        switch (tipoPago) {
            case DIARIO:
                // Para pago diario, calcular días hábiles (excluyendo domingos)
                int diasHabiles = 0;
                LocalDate fecha = fechaInicio;
                int diasTotales = periodoMeses * 26; // 26 días hábiles por mes
                
                while (diasHabiles < diasTotales) {
                    if (fecha.getDayOfWeek().getValue() != 7) { // No es domingo
                        diasHabiles++;
                    }
                    fecha = fecha.plusDays(1);
                }
                return fecha.minusDays(1);
                
            case SEMANAL:
                // Para pago semanal, cada 7 días
                return fechaInicio.plusWeeks(periodoMeses * 4 - 1);
                
            case MENSUAL:
                // Para pago mensual, cada mes
                return fechaInicio.plusMonths(periodoMeses - 1);
                
            default:
                return fechaInicio.plusDays(periodoMeses * 26 - 1);
        }
    }
    
    /**
     * Registra el abono de capital (10% del préstamo)
     */
    private void registrarAbonoCapital(Long idCliente, BigDecimal montoAbono) {
        try {
            Optional<Cliente> clienteOpt = clienteDAO.findById(idCliente);
            if (clienteOpt.isPresent()) {
                Cliente cliente = clienteOpt.get();
                BigDecimal nuevoSaldo = cliente.getSaldoCapital().add(montoAbono);
                clienteDAO.updateSaldoCapital(idCliente, nuevoSaldo);
                
                logger.info("Abono de capital registrado: " + montoAbono + " para cliente: " + idCliente);
            }
        } catch (Exception e) {
            logger.error("Error al registrar abono de capital", e);
        }
    }
    
    /**
     * Valida una solicitud de préstamo
     */
    private boolean validarSolicitud(Prestamo prestamo) {
        if (prestamo.getMontoSolicitado() == null || prestamo.getMontoSolicitado().compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Monto solicitado inválido");
            return false;
        }
        
        if (prestamo.getTasaInteres() == null || prestamo.getTasaInteres().compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("Tasa de interés inválida");
            return false;
        }
        
        // Verificar que el cliente existe
        if (!clienteDAO.exists(prestamo.getIdCliente())) {
            logger.warn("Cliente no existe: " + prestamo.getIdCliente());
            return false;
        }
        
        // Los clientes pueden tener múltiples préstamos activos
        // Solo verificamos que no tenga préstamos pendientes
        
        // Verificar que no tenga préstamos pendientes
        if (prestamoDAO.hasPendingLoans(prestamo.getIdCliente())) {
            logger.warn("Cliente ya tiene préstamos pendientes: " + prestamo.getIdCliente());
            return false;
        }
        
        return true;
    }
    
    /**
     * Obtiene préstamos pendientes
     */
    public List<Prestamo> obtenerPrestamosPendientes() {
        return prestamoDAO.findPendientes();
    }
    
    /**
     * Obtiene préstamos activos
     */
    public List<Prestamo> obtenerPrestamosActivos() {
        return prestamoDAO.findActivos();
    }
    
    /**
     * Obtiene préstamos por cliente
     */
    public List<Prestamo> obtenerPrestamosPorCliente(Long idCliente) {
        return prestamoDAO.findByCliente(idCliente);
    }
    
    /**
     * Obtiene préstamos por asesor
     */
    public List<Prestamo> obtenerPrestamosPorAsesor(Long idAsesor) {
        return prestamoDAO.findByAsesor(idAsesor);
    }
    
    /**
     * Simula un préstamo para mostrar cronograma estimado
     */
    public Cronograma simularPrestamo(BigDecimal monto, BigDecimal tasaInteres, int periodoMeses, Prestamo.TipoPago tipoPago) {
        try {
            Prestamo prestamoSimulado = new Prestamo();
            prestamoSimulado.setMontoSolicitado(monto);
            prestamoSimulado.setTasaInteres(tasaInteres);
            prestamoSimulado.setPeriodoMeses(periodoMeses);
            prestamoSimulado.setTipoPago(tipoPago);
            
            BigDecimal montoTotal = calcularMontoTotal(prestamoSimulado);
            int numeroCuotas = calcularNumeroCuotas(periodoMeses, tipoPago.name().toLowerCase());
            BigDecimal montoCuota = montoTotal.divide(new BigDecimal(numeroCuotas), 2, RoundingMode.HALF_UP);
            
            Cronograma cuotaSimulada = new Cronograma();
            cuotaSimulada.setMontoCuota(montoCuota);
            cuotaSimulada.setFechaProgramada(calcularFechaPago(LocalDate.now(), 1, tipoPago));
            
            return cuotaSimulada;
            
        } catch (Exception e) {
            logger.error("Error al simular préstamo", e);
            return null;
        }
    }
    
    /**
     * Obtiene cuotas del día
     */
    public List<Cronograma> obtenerCuotasDelDia() {
        try {
            return cronogramaDAO.findByFecha(LocalDate.now());
        } catch (Exception e) {
            logger.error("Error al obtener cuotas del día", e);
            throw new RuntimeException("Error al obtener las cuotas del día", e);
        }
    }
    
    /**
     * Obtiene cuotas vencidas
     */
    public List<Cronograma> obtenerCuotasVencidas() {
        try {
            return cronogramaDAO.findVencidas();
        } catch (Exception e) {
            logger.error("Error al obtener cuotas vencidas", e);
            throw new RuntimeException("Error al obtener las cuotas vencidas", e);
        }
    }
    
    /**
     * Obtiene cuotas pendientes por cliente
     */
    public List<Cronograma> obtenerCuotasPendientesPorCliente(Long idCliente) {
        try {
            return cronogramaDAO.findPendientesByCliente(idCliente);
        } catch (Exception e) {
            logger.error("Error al obtener cuotas pendientes por cliente: " + idCliente, e);
            throw new RuntimeException("Error al obtener las cuotas pendientes del cliente", e);
        }
    }
    
    /**
     * Obtiene cuotas vencidas por cliente
     */
    public List<Cronograma> obtenerCuotasVencidasPorCliente(Long idCliente) {
        try {
            return cronogramaDAO.findVencidasByCliente(idCliente);
        } catch (Exception e) {
            logger.error("Error al obtener cuotas vencidas por cliente: " + idCliente, e);
            throw new RuntimeException("Error al obtener las cuotas vencidas del cliente", e);
        }
    }
    
    /**
     * Obtiene cuotas por préstamo
     */
    public List<Cronograma> obtenerCuotasPorPrestamo(Long idPrestamo) {
        try {
            return cronogramaDAO.findByPrestamoId(idPrestamo);
        } catch (Exception e) {
            logger.error("Error al obtener cuotas por préstamo: " + idPrestamo, e);
            throw new RuntimeException("Error al obtener las cuotas del préstamo", e);
        }
    }
    
    /**
     * Obtiene préstamos activos por cliente
     */
    public List<Prestamo> obtenerPrestamosActivosPorCliente(Long idCliente) {
        try {
            return prestamoDAO.findByClienteAndEstado(idCliente, Prestamo.EstadoPrestamo.ACTIVO);
        } catch (Exception e) {
            logger.error("Error al obtener préstamos activos por cliente: " + idCliente, e);
            throw new RuntimeException("Error al obtener los préstamos activos del cliente", e);
        }
    }
    
    /**
     * Calcula la morosidad
     */
    public double calcularMorosidad() {
        try {
            // TODO: Implementar cálculo de morosidad
            return 0.0;
        } catch (Exception e) {
            logger.error("Error al calcular morosidad", e);
            throw new RuntimeException("Error al calcular la morosidad", e);
        }
    }
    
    /**
     * Obtiene el total pagado por cliente
     */
    public double obtenerTotalPagadoPorCliente(Long idCliente) {
        try {
            // TODO: Implementar en PagoDAO
            return 0.0;
        } catch (Exception e) {
            logger.error("Error al obtener total pagado por cliente: " + idCliente, e);
            throw new RuntimeException("Error al obtener el total pagado del cliente", e);
        }
    }
    
    /**
     * Obtiene el monto pendiente por cliente
     */
    public double obtenerMontoPendientePorCliente(Long idCliente) {
        try {
            // TODO: Implementar cálculo de monto pendiente
            return 0.0;
        } catch (Exception e) {
            logger.error("Error al obtener monto pendiente por cliente: " + idCliente, e);
            throw new RuntimeException("Error al obtener el monto pendiente del cliente", e);
        }
    }
    
    /**
     * Obtiene la fecha del último pago por cliente
     */
    public String obtenerUltimoPagoPorCliente(Long idCliente) {
        try {
            // TODO: Implementar en PagoDAO
            return null;
        } catch (Exception e) {
            logger.error("Error al obtener último pago por cliente: " + idCliente, e);
            throw new RuntimeException("Error al obtener el último pago del cliente", e);
        }
    }
    
    /**
     * Genera preview del cronograma sin guardar
     */
    public List<Cronograma> generarCronogramaPreview(double montoTotal, int periodo, String tipoPago, LocalDate fechaInicio) {
        try {
            List<Cronograma> cronograma = new ArrayList<>();
            int numeroCuotas = calcularNumeroCuotas(periodo, tipoPago);
            double montoCuota = montoTotal / numeroCuotas;
            
            LocalDate fechaActual = fechaInicio;
            for (int i = 1; i <= numeroCuotas; i++) {
                // Avanzar hasta el siguiente día hábil
                while (FechaUtil.isDomingo(fechaActual)) {
                    fechaActual = fechaActual.plusDays(1);
                }
                
                Cronograma cuota = new Cronograma();
                cuota.setNumeroCuota(i);
                cuota.setFechaProgramada(fechaActual);
                cuota.setMontoCuota(new BigDecimal(montoCuota));
                cuota.setEstadoCuota(Cronograma.EstadoCuota.PENDIENTE);
                
                cronograma.add(cuota);
                fechaActual = fechaActual.plusDays(1);
            }
            
            return cronograma;
            
        } catch (Exception e) {
            logger.error("Error al generar preview del cronograma", e);
            throw new RuntimeException("Error al generar el preview del cronograma", e);
        }
    }
    
    /**
     * Obtiene un préstamo por ID
     */
    public Prestamo obtenerPrestamoPorId(Long idPrestamo) {
        try {
            Optional<Prestamo> prestamoOpt = prestamoDAO.findById(idPrestamo);
            return prestamoOpt.orElse(null);
        } catch (Exception e) {
            logger.error("Error al obtener préstamo por ID: " + idPrestamo, e);
            return null;
        }
    }

    /**
     * Actualiza un préstamo existente
     */
    public boolean actualizarPrestamo(Prestamo prestamo) {
        try {
            return prestamoDAO.update(prestamo);
        } catch (Exception e) {
            logger.error("Error al actualizar préstamo: " + prestamo.getIdPrestamo(), e);
            return false;
        }
    }
    
    /**
     * Calcula el número de cuotas según el tipo de pago
     */
    private int calcularNumeroCuotas(int periodo, String tipoPago) {
        switch (tipoPago) {
            case "diario":
                return periodo * 26; // 26 días hábiles por mes
            case "semanal":
                return periodo * 4; // 4 semanas por mes
            case "mensual":
                return periodo; // 1 cuota por mes
            default:
                return periodo * 26;
        }
    }
    
    /**
     * Calcula la fecha de pago para una cuota específica según el tipo de pago
     */
    private LocalDate calcularFechaPago(LocalDate fechaInicio, int numeroCuota, Prestamo.TipoPago tipoPago) {
        switch (tipoPago) {
            case DIARIO:
                // Para pago diario, saltar domingos
                LocalDate fecha = fechaInicio.plusDays(numeroCuota - 1);
                while (fecha.getDayOfWeek().getValue() == 7) { // Si es domingo, avanzar al lunes
                    fecha = fecha.plusDays(1);
                }
                return fecha;
                
            case SEMANAL:
                // Para pago semanal, cada 7 días
                return fechaInicio.plusWeeks(numeroCuota - 1);
                
            case MENSUAL:
                // Para pago mensual, cada mes
                return fechaInicio.plusMonths(numeroCuota - 1);
                
            default:
                return fechaInicio.plusDays(numeroCuota - 1);
        }
    }
}
