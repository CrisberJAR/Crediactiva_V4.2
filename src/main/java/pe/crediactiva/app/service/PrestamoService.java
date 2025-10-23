package pe.crediactiva.app.service;

import pe.crediactiva.app.dao.ClienteDAO;
import pe.crediactiva.app.dao.CronogramaDAO;
import pe.crediactiva.app.dao.PrestamoDAO;
import pe.crediactiva.app.dao.MovimientoCapitalDAO;
import pe.crediactiva.app.dao.impl.ClienteDAOImpl;
import pe.crediactiva.app.dao.impl.CronogramaDAOImpl;
import pe.crediactiva.app.dao.impl.PrestamoDAOImpl;
import pe.crediactiva.app.dao.impl.MovimientoCapitalDAOImpl;
import pe.crediactiva.app.model.Cliente;
import pe.crediactiva.app.model.Cronograma;
import pe.crediactiva.app.model.Pago;
import pe.crediactiva.app.model.Prestamo;
import pe.crediactiva.app.model.MovimientoCapital;
import pe.crediactiva.app.config.SessionManager;
import pe.crediactiva.app.util.FechaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import pe.crediactiva.app.util.DateTimeUtil;

/**
 * Servicio de gestión de préstamos
 */
public class PrestamoService {
    
    private static final Logger logger = LoggerFactory.getLogger(PrestamoService.class);
    
    private final PrestamoDAO prestamoDAO;
    private final ClienteDAO clienteDAO;
    private final CronogramaDAO cronogramaDAO;
    private final MovimientoCapitalDAO movimientoCapitalDAO;
    private final AuditoriaService auditoriaService;
    
    public PrestamoService() {
        this.prestamoDAO = new PrestamoDAOImpl();
        this.clienteDAO = new ClienteDAOImpl();
        this.cronogramaDAO = new CronogramaDAOImpl();
        this.movimientoCapitalDAO = new MovimientoCapitalDAOImpl();
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
     * NOTA: El cronograma inicia un día después de la fecha de solicitud del préstamo
     */
    private void generarCronograma(Prestamo prestamo) {
        try {
            BigDecimal montoTotal = calcularMontoTotal(prestamo);
            int numeroCuotas = calcularNumeroCuotas(prestamo.getPeriodoMeses(), prestamo.getTipoPago().name().toLowerCase());
            BigDecimal montoCuota = montoTotal.divide(new BigDecimal(numeroCuotas), 2, RoundingMode.HALF_UP);
            
            // IMPORTANTE: El cronograma inicia un día después de la fecha de solicitud
            LocalDate fechaInicioCronograma = prestamo.getFechaInicio().plusDays(1);
            // Si la fecha inicial cae en domingo, mover al siguiente día hábil (lunes)
            if (prestamo.getTipoPago() == Prestamo.TipoPago.DIARIO) {
                while (fechaInicioCronograma.getDayOfWeek().getValue() == 7) {
                    fechaInicioCronograma = fechaInicioCronograma.plusDays(1);
                }
            }
            
            // Generar cuotas según el tipo de pago
            for (int i = 1; i <= numeroCuotas; i++) {
                Cronograma cuota = new Cronograma();
                cuota.setIdPrestamo(prestamo.getIdPrestamo());
                cuota.setNumeroCuota(i);
                cuota.setFechaProgramada(calcularFechaPago(fechaInicioCronograma, i, prestamo.getTipoPago()));
                cuota.setMontoCuota(montoCuota);
                
                cronogramaDAO.create(cuota);
            }
            
            logger.info("Cronograma generado para préstamo: " + prestamo.getIdPrestamo() + 
                       " - " + numeroCuotas + " cuotas de " + montoCuota + " cada una" +
                       " - Inicia el: " + fechaInicioCronograma);
            
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
     * Registra un desembolso de capital del cliente
     */
    public boolean registrarDesembolsoCapital(Long idCliente, BigDecimal montoDesembolso, String observacion) {
        try {
            Optional<Cliente> clienteOpt = clienteDAO.findById(idCliente);
            if (!clienteOpt.isPresent()) {
                logger.warn("Cliente no encontrado para desembolso: " + idCliente);
                return false;
            }
            
            Cliente cliente = clienteOpt.get();
            
            // Validar que el cliente tenga suficiente capital
            if (cliente.getSaldoCapital().compareTo(montoDesembolso) < 0) {
                logger.warn("Saldo insuficiente para desembolso. Cliente: " + idCliente + 
                           ", Saldo actual: " + cliente.getSaldoCapital() + 
                           ", Monto solicitado: " + montoDesembolso);
                return false;
            }
            
            // Obtener ID del admin logueado
            SessionManager sessionManager = SessionManager.getInstance();
            Long idAdmin = sessionManager.getCurrentUser() != null ? 
                sessionManager.getCurrentUser().getIdUsuario() : null;
            
            if (idAdmin == null) {
                logger.error("No se pudo obtener ID del administrador para desembolso");
                return false;
            }
            
            // Actualizar saldo del cliente
            BigDecimal nuevoSaldo = cliente.getSaldoCapital().subtract(montoDesembolso);
            boolean saldoActualizado = clienteDAO.updateSaldoCapital(idCliente, nuevoSaldo);
            
            if (saldoActualizado) {
                // Registrar movimiento en la tabla movimientos_capital
                MovimientoCapital movimiento = new MovimientoCapital(
                    idCliente,
                    MovimientoCapital.TipoMovimiento.DESEMBOLSO,
                    montoDesembolso,
                    idAdmin,
                    observacion != null ? observacion : "Desembolsado por solicitud de desembolso capital del cliente"
                );
                
                boolean movimientoCreado = movimientoCapitalDAO.create(movimiento);
                if (movimientoCreado) {
                    logger.info("Desembolso de capital registrado: " + montoDesembolso + 
                              " para cliente " + idCliente + " por admin " + idAdmin);
                    return true;
                } else {
                    logger.error("Error al crear movimiento de desembolso para cliente: " + idCliente);
                    // Revertir cambio en saldo
                    clienteDAO.updateSaldoCapital(idCliente, cliente.getSaldoCapital());
                    return false;
                }
            } else {
                logger.error("Error al actualizar saldo de capital para cliente: " + idCliente);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Error al registrar desembolso de capital", e);
            return false;
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
                
                // Registrar movimiento en la tabla movimientos_capital
                SessionManager sessionManager = SessionManager.getInstance();
                Long idAdmin = sessionManager.getCurrentUser() != null ? 
                    sessionManager.getCurrentUser().getIdUsuario() : null;
                
                if (idAdmin != null) {
                    MovimientoCapital movimiento = new MovimientoCapital(
                        idCliente,
                        MovimientoCapital.TipoMovimiento.ABONO,
                        montoAbono,
                        idAdmin,
                        "Abono al capital por préstamo aprobado"
                    );
                    
                    boolean movimientoCreado = movimientoCapitalDAO.create(movimiento);
                    if (movimientoCreado) {
                        logger.info("Movimiento de capital registrado: ABONO de " + montoAbono + 
                                  " para cliente " + idCliente + " por admin " + idAdmin);
                    } else {
                        logger.error("Error al crear movimiento de capital para cliente: " + idCliente);
                    }
                } else {
                    logger.warn("No se pudo obtener ID del administrador para registrar movimiento de capital");
                }
                
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
        // No hay restricción sobre el número de préstamos que puede tener un cliente
        logger.info("Cliente " + prestamo.getIdCliente() + " puede tener múltiples préstamos - validación pasada");
        
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
            cuotaSimulada.setFechaProgramada(calcularFechaPago(DateTimeUtil.today(), 1, tipoPago));
            
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
            return cronogramaDAO.findByFecha(DateTimeUtil.today());
        } catch (Exception e) {
            logger.error("Error al obtener cuotas del día", e);
            throw new RuntimeException("Error al obtener las cuotas del día", e);
        }
    }
    
    /**
     * Obtiene cuotas del día por asesor
     */
    public List<Cronograma> obtenerCuotasDelDiaPorAsesor(Long idAsesor) {
        try {
            return cronogramaDAO.findByFechaAndAsesor(DateTimeUtil.today(), idAsesor);
        } catch (Exception e) {
            logger.error("Error al obtener cuotas del día por asesor: " + idAsesor, e);
            throw new RuntimeException("Error al obtener las cuotas del día del asesor", e);
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
     * Obtiene cuotas vencidas por asesor
     */
    public List<Cronograma> obtenerCuotasVencidasPorAsesor(Long idAsesor) {
        try {
            return cronogramaDAO.findVencidasByAsesor(idAsesor);
        } catch (Exception e) {
            logger.error("Error al obtener cuotas vencidas por asesor: " + idAsesor, e);
            throw new RuntimeException("Error al obtener las cuotas vencidas del asesor", e);
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
     * Cuenta el número de préstamos activos por cliente
     */
    public int contarPrestamosActivosPorCliente(Long idCliente) {
        try {
            logger.info("Contando préstamos activos para cliente: " + idCliente);
            List<Prestamo> prestamosActivos = prestamoDAO.findByClienteAndEstado(idCliente, Prestamo.EstadoPrestamo.ACTIVO);
            logger.info("Encontrados " + prestamosActivos.size() + " préstamos activos para cliente: " + idCliente);
            return prestamosActivos.size();
        } catch (Exception e) {
            logger.error("Error al contar préstamos activos por cliente: " + idCliente, e);
            return 0;
        }
    }
    
    /**
     * Calcula la morosidad como porcentaje de cuotas vencidas sobre total de cuotas
     */
    public double calcularMorosidad() {
        try {
            // Obtener todas las cuotas pendientes y vencidas
            List<Cronograma> cuotasVencidas = cronogramaDAO.findVencidas();
            List<Cronograma> cuotasPendientes = cronogramaDAO.findByEstado(Cronograma.EstadoCuota.PENDIENTE);
            
            int totalCuotasPendientes = cuotasPendientes.size();
            int totalCuotasVencidas = cuotasVencidas.size();
            
            if (totalCuotasPendientes == 0) {
                return 0.0; // No hay cuotas pendientes, morosidad 0%
            }
            
            // Calcular porcentaje de morosidad
            double morosidad = (double) totalCuotasVencidas / totalCuotasPendientes * 100.0;
            
            logger.info("Morosidad calculada: " + totalCuotasVencidas + " vencidas de " + 
                       totalCuotasPendientes + " pendientes = " + String.format("%.2f", morosidad) + "%");
            
            return morosidad;
            
        } catch (Exception e) {
            logger.error("Error al calcular morosidad", e);
            return 0.0; // Retornar 0 en caso de error
        }
    }
    
    /**
     * Calcula la morosidad por asesor
     */
    public double calcularMorosidadPorAsesor(Long idAsesor) {
        try {
            // Obtener cuotas vencidas y pendientes del asesor
            List<Cronograma> cuotasVencidas = cronogramaDAO.findVencidasByAsesor(idAsesor);
            List<Cronograma> cuotasPendientes = cronogramaDAO.findPendientesByAsesor(idAsesor);
            
            int totalCuotasPendientes = cuotasPendientes.size();
            int totalCuotasVencidas = cuotasVencidas.size();
            
            if (totalCuotasPendientes == 0) {
                return 0.0; // No hay cuotas pendientes, morosidad 0%
            }
            
            // Calcular porcentaje de morosidad
            double morosidad = (double) totalCuotasVencidas / totalCuotasPendientes * 100.0;
            
            logger.info("Morosidad calculada para asesor " + idAsesor + ": " + totalCuotasVencidas + " vencidas de " + 
                       totalCuotasPendientes + " pendientes = " + String.format("%.2f", morosidad) + "%");
            
            return morosidad;
        } catch (Exception e) {
            logger.error("Error al calcular morosidad por asesor: " + idAsesor, e);
            return 0.0;
        }
    }
    
    /**
     * Verifica la consistencia de datos para un asesor
     */
    public void verificarConsistenciaAsesor(Long idAsesor) {
        try {
            logger.info("=== VERIFICACIÓN DE CONSISTENCIA PARA ASESOR " + idAsesor + " ===");
            
            // Obtener todas las cuotas del día para este asesor
            List<Cronograma> cuotasDelDia = cronogramaDAO.findByFechaAndAsesor(DateTimeUtil.today(), idAsesor);
            
            logger.info("Cuotas del día encontradas: " + cuotasDelDia.size());
            
            for (Cronograma cuota : cuotasDelDia) {
                if (cuota.getPrestamo() != null) {
                    Long asesorPrestamo = cuota.getPrestamo().getIdAsesor();
                    String nombreCliente = "N/A";
                    
                    if (cuota.getPrestamo().getCliente() != null) {
                        nombreCliente = cuota.getPrestamo().getCliente().getNombre() + " " + 
                                      cuota.getPrestamo().getCliente().getApellido();
                    }
                    
                    logger.info("Cuota ID: " + cuota.getIdCuota() + 
                               ", Cliente: " + nombreCliente + 
                               ", Asesor del préstamo: " + asesorPrestamo + 
                               ", Asesor consultado: " + idAsesor);
                    
                    if (!idAsesor.equals(asesorPrestamo)) {
                        logger.error("¡INCONSISTENCIA DETECTADA! Cuota " + cuota.getIdCuota() + 
                                   " del cliente " + nombreCliente + 
                                   " pertenece al asesor " + asesorPrestamo + 
                                   " pero se está mostrando al asesor " + idAsesor);
                    }
                }
            }
            
            logger.info("=== FIN VERIFICACIÓN DE CONSISTENCIA ===");
            
        } catch (Exception e) {
            logger.error("Error al verificar consistencia del asesor: " + idAsesor, e);
        }
    }
    
    /**
     * Verifica si un asesor tiene préstamos asignados
     */
    public boolean tienePrestamosAsignados(Long idAsesor) {
        try {
            List<Prestamo> prestamos = prestamoDAO.findByAsesor(idAsesor);
            boolean tienePrestamos = !prestamos.isEmpty();
            
            logger.info("Asesor " + idAsesor + " tiene préstamos asignados: " + tienePrestamos + " (Total: " + prestamos.size() + ")");
            
            return tienePrestamos;
        } catch (Exception e) {
            logger.error("Error al verificar si el asesor tiene préstamos: " + idAsesor, e);
            return false; // Por seguridad, asumir que no tiene préstamos
        }
    }
    
    /**
     * Obtiene el total pagado por cliente
     */
    public double obtenerTotalPagadoPorCliente(Long idCliente) {
        try {
            PagoService pagoService = new PagoService();
            return pagoService.calcularTotalPagadoCliente(idCliente).doubleValue();
        } catch (Exception e) {
            logger.error("Error al obtener total pagado por cliente: " + idCliente, e);
            return 0.0;
        }
    }
    
    /**
     * Obtiene el monto pendiente por cliente
     */
    public double obtenerMontoPendientePorCliente(Long idCliente) {
        try {
            List<Prestamo> prestamos = prestamoDAO.findByCliente(idCliente);
            BigDecimal montoTotalPrestado = BigDecimal.ZERO;
            BigDecimal montoTotalPagado = BigDecimal.ZERO;
            
            for (Prestamo prestamo : prestamos) {
                montoTotalPrestado = montoTotalPrestado.add(prestamo.getMontoSolicitado());
                
                // Obtener total pagado para este cliente
                PagoService pagoService = new PagoService();
                BigDecimal totalPagado = pagoService.calcularTotalPagadoCliente(idCliente);
                montoTotalPagado = totalPagado;
            }
            
            BigDecimal montoPendiente = montoTotalPrestado.subtract(montoTotalPagado);
            return Math.max(0.0, montoPendiente.doubleValue());
            
        } catch (Exception e) {
            logger.error("Error al obtener monto pendiente por cliente: " + idCliente, e);
            return 0.0;
        }
    }
    
    /**
     * Obtiene la fecha del último pago por cliente
     */
    public String obtenerUltimoPagoPorCliente(Long idCliente) {
        try {
            PagoService pagoService = new PagoService();
            java.util.Optional<Pago> ultimoPago = pagoService.obtenerUltimoPagoCliente(idCliente);
            if (ultimoPago.isPresent()) {
                return FechaUtil.formatearFecha(ultimoPago.get().getFechaPago().toLocalDate());
            }
            return null;
        } catch (Exception e) {
            logger.error("Error al obtener último pago por cliente: " + idCliente, e);
            return null;
        }
    }
    
    /**
     * Obtiene una cuota por su ID
     */
    public Cronograma obtenerCuotaPorId(Long idCuota) {
        try {
            return cronogramaDAO.findById(idCuota).orElse(null);
        } catch (Exception e) {
            logger.error("Error al obtener cuota por ID: " + idCuota, e);
            return null;
        }
    }
    
    /**
     * Actualiza una cuota
     */
    public boolean actualizarCuota(Cronograma cuota) {
        try {
            return cronogramaDAO.update(cuota);
        } catch (Exception e) {
            logger.error("Error al actualizar cuota: " + cuota.getIdCuota(), e);
            return false;
        }
    }
    
    /**
     * Genera preview del cronograma sin guardar
     * NOTA: El cronograma inicia un día después de la fecha de solicitud del préstamo
     */
    public List<Cronograma> generarCronogramaPreview(double montoTotal, int periodo, String tipoPago, LocalDate fechaInicio) {
        try {
            List<Cronograma> cronograma = new ArrayList<>();
            int numeroCuotas = calcularNumeroCuotas(periodo, tipoPago);
            double montoCuota = montoTotal / numeroCuotas;
            
            // IMPORTANTE: El cronograma inicia un día después de la fecha de solicitud
            LocalDate fechaInicioCronograma = fechaInicio.plusDays(1);
            // Si la fecha inicial cae en domingo, mover al siguiente día hábil (lunes)
            if ("diario".equalsIgnoreCase(tipoPago)) {
                while (fechaInicioCronograma.getDayOfWeek().getValue() == 7) {
                    fechaInicioCronograma = fechaInicioCronograma.plusDays(1);
                }
            }
            
            for (int i = 1; i <= numeroCuotas; i++) {
                // Calcular fecha usando la misma lógica que el método principal
                LocalDate fechaPago = calcularFechaPagoPreview(fechaInicioCronograma, i, tipoPago);
                
                Cronograma cuota = new Cronograma();
                cuota.setNumeroCuota(i);
                cuota.setFechaProgramada(fechaPago);
                cuota.setMontoCuota(new BigDecimal(montoCuota));
                cuota.setEstadoCuota(Cronograma.EstadoCuota.PENDIENTE);
                
                cronograma.add(cuota);
            }
            
            return cronograma;
            
        } catch (Exception e) {
            logger.error("Error al generar preview del cronograma", e);
            throw new RuntimeException("Error al generar el preview del cronograma", e);
        }
    }
    
    /**
     * Calcula la fecha de pago para preview del cronograma
     * NOTA: TODOS los tipos de pago deben evitar domingos y tener fechas únicas
     */
    private LocalDate calcularFechaPagoPreview(LocalDate fechaInicio, int numeroCuota, String tipoPago) {
        LocalDate fecha;
        
        switch (tipoPago.toLowerCase()) {
            case "diario":
                // Para pago diario, calcular fecha secuencial saltando domingos
                fecha = fechaInicio;
                
                // Avanzar (numeroCuota - 1) días hábiles
                for (int i = 1; i < numeroCuota; i++) {
                    fecha = fecha.plusDays(1);
                    // Si cae en domingo, avanzar al lunes
                    while (fecha.getDayOfWeek().getValue() == 7) {
                        fecha = fecha.plusDays(1);
                    }
                }
                break;
                
            case "semanal":
                // Para pago semanal, cada 7 días pero evitando domingos
                fecha = fechaInicio.plusWeeks(numeroCuota - 1);
                // Si cae en domingo, avanzar al lunes
                while (fecha.getDayOfWeek().getValue() == 7) {
                    fecha = fecha.plusDays(1);
                }
                break;
                
            case "mensual":
                // Para pago mensual, cada mes pero evitando domingos
                fecha = fechaInicio.plusMonths(numeroCuota - 1);
                // Si cae en domingo, avanzar al lunes
                while (fecha.getDayOfWeek().getValue() == 7) {
                    fecha = fecha.plusDays(1);
                }
                break;
                
            default:
                // Fallback: pago diario
                fecha = fechaInicio.plusDays(numeroCuota - 1);
                while (fecha.getDayOfWeek().getValue() == 7) {
                    fecha = fecha.plusDays(1);
                }
                break;
        }
        
        return fecha;
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
     * NOTA: TODOS los tipos de pago deben evitar domingos y tener fechas únicas
     */
    private LocalDate calcularFechaPago(LocalDate fechaInicio, int numeroCuota, Prestamo.TipoPago tipoPago) {
        LocalDate fecha;
        
        switch (tipoPago) {
            case DIARIO:
                // Para pago diario, calcular fecha secuencial saltando domingos
                fecha = fechaInicio;
                
                // Avanzar (numeroCuota - 1) días hábiles
                for (int i = 1; i < numeroCuota; i++) {
                    fecha = fecha.plusDays(1);
                    // Si cae en domingo, avanzar al lunes
                    while (fecha.getDayOfWeek().getValue() == 7) {
                        fecha = fecha.plusDays(1);
                    }
                }
                break;
                
            case SEMANAL:
                // Para pago semanal, cada 7 días pero evitando domingos
                fecha = fechaInicio.plusWeeks(numeroCuota - 1);
                // Si cae en domingo, avanzar al lunes
                while (fecha.getDayOfWeek().getValue() == 7) {
                    fecha = fecha.plusDays(1);
                }
                break;
                
            case MENSUAL:
                // Para pago mensual, cada mes pero evitando domingos
                fecha = fechaInicio.plusMonths(numeroCuota - 1);
                // Si cae en domingo, avanzar al lunes
                while (fecha.getDayOfWeek().getValue() == 7) {
                    fecha = fecha.plusDays(1);
                }
                break;
                
            default:
                // Fallback: pago diario
                fecha = fechaInicio.plusDays(numeroCuota - 1);
                while (fecha.getDayOfWeek().getValue() == 7) {
                    fecha = fecha.plusDays(1);
                }
                break;
        }
        
        return fecha;
    }
}
