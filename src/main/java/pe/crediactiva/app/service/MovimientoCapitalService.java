package pe.crediactiva.app.service;

import pe.crediactiva.app.dao.MovimientoCapitalDAO;
import pe.crediactiva.app.dao.impl.MovimientoCapitalDAOImpl;
import pe.crediactiva.app.model.MovimientoCapital;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import pe.crediactiva.app.util.DateTimeUtil;
import java.util.Optional;

/**
 * Servicio de gestión de movimientos de capital
 */
public class MovimientoCapitalService {

    private static final Logger logger = LoggerFactory.getLogger(MovimientoCapitalService.class);

    private final MovimientoCapitalDAO movimientoCapitalDAO;
    private final AuditoriaService auditoriaService;
    private final ClienteService clienteService;

    public MovimientoCapitalService() {
        this.movimientoCapitalDAO = new MovimientoCapitalDAOImpl();
        this.auditoriaService = new AuditoriaService();
        this.clienteService = new ClienteService();
    }

    /**
     * Registra un abono de capital (10% del préstamo)
     */
    public boolean registrarAbonoCapital(Long idCliente, BigDecimal monto, Long idAdmin) {
        try {
            MovimientoCapital movimiento = new MovimientoCapital();
            movimiento.setIdCliente(idCliente);
            movimiento.setTipoMovimiento(MovimientoCapital.TipoMovimiento.ABONO);
            movimiento.setMonto(monto);
            movimiento.setFecha(DateTimeUtil.now());
            movimiento.setIdAdmin(idAdmin);

            boolean success = movimientoCapitalDAO.create(movimiento);
            if (success) {
                // Actualizar saldo de capital del cliente
                Optional<pe.crediactiva.app.model.Cliente> clienteOpt = clienteService.obtenerClientePorId(idCliente);
                if (clienteOpt.isPresent()) {
                    pe.crediactiva.app.model.Cliente cliente = clienteOpt.get();
                    BigDecimal nuevoSaldo = cliente.getSaldoCapital().add(monto);
                    clienteService.actualizarSaldoCapital(idCliente, nuevoSaldo);
                }

                auditoriaService.registrarAuditoria("movimientos_capital", movimiento.getIdMovimiento().toString(), 
                    "INSERT", null, movimiento.toString());
                logger.info("Abono de capital registrado: " + monto + " para cliente " + idCliente);
                return true;
            }

        } catch (Exception e) {
            logger.error("Error al registrar abono de capital", e);
        }
        return false;
    }

    /**
     * Registra un desembolso de capital (50% del saldo acumulado)
     */
    public boolean registrarDesembolsoCapital(Long idCliente, BigDecimal monto, Long idAdmin) {
        try {
            // Verificar que el cliente tiene suficiente capital
            Optional<pe.crediactiva.app.model.Cliente> clienteOpt = clienteService.obtenerClientePorId(idCliente);
            if (!clienteOpt.isPresent()) {
                logger.warn("Cliente no encontrado: " + idCliente);
                return false;
            }

            pe.crediactiva.app.model.Cliente cliente = clienteOpt.get();
            if (cliente.getSaldoCapital().compareTo(monto) < 0) {
                logger.warn("Saldo insuficiente para desembolso. Cliente: " + idCliente + 
                           ", Saldo: " + cliente.getSaldoCapital() + ", Monto: " + monto);
                return false;
            }

            MovimientoCapital movimiento = new MovimientoCapital();
            movimiento.setIdCliente(idCliente);
            movimiento.setTipoMovimiento(MovimientoCapital.TipoMovimiento.DESEMBOLSO);
            movimiento.setMonto(monto);
            movimiento.setFecha(DateTimeUtil.now());
            movimiento.setIdAdmin(idAdmin);

            boolean success = movimientoCapitalDAO.create(movimiento);
            if (success) {
                // Actualizar saldo de capital del cliente
                BigDecimal nuevoSaldo = cliente.getSaldoCapital().subtract(monto);
                clienteService.actualizarSaldoCapital(idCliente, nuevoSaldo);

                auditoriaService.registrarAuditoria("movimientos_capital", movimiento.getIdMovimiento().toString(), 
                    "INSERT", null, movimiento.toString());
                logger.info("Desembolso de capital registrado: " + monto + " para cliente " + idCliente);
                return true;
            }

        } catch (Exception e) {
            logger.error("Error al registrar desembolso de capital", e);
        }
        return false;
    }

    /**
     * Obtiene movimientos por cliente
     */
    public List<MovimientoCapital> obtenerMovimientosPorCliente(Long idCliente) {
        try {
            return movimientoCapitalDAO.findByCliente(idCliente);
        } catch (Exception e) {
            logger.error("Error al obtener movimientos por cliente: " + idCliente, e);
            return List.of();
        }
    }

    /**
     * Obtiene movimientos por tipo
     */
    public List<MovimientoCapital> obtenerMovimientosPorTipo(MovimientoCapital.TipoMovimiento tipo) {
        try {
            return movimientoCapitalDAO.findByTipo(tipo);
        } catch (Exception e) {
            logger.error("Error al obtener movimientos por tipo: " + tipo, e);
            return List.of();
        }
    }

    /**
     * Obtiene movimientos por fecha
     */
    public List<MovimientoCapital> obtenerMovimientosPorFecha(java.time.LocalDate fecha) {
        try {
            return movimientoCapitalDAO.findByFecha(fecha);
        } catch (Exception e) {
            logger.error("Error al obtener movimientos por fecha: " + fecha, e);
            return List.of();
        }
    }

    /**
     * Obtiene movimientos por rango de fechas
     */
    public List<MovimientoCapital> obtenerMovimientosPorRango(java.time.LocalDate fechaInicio, java.time.LocalDate fechaFin) {
        try {
            return movimientoCapitalDAO.findByRangoFechas(fechaInicio, fechaFin);
        } catch (Exception e) {
            logger.error("Error al obtener movimientos por rango: " + fechaInicio + " - " + fechaFin, e);
            return List.of();
        }
    }

    /**
     * Calcula el total de abonos de un cliente
     */
    public BigDecimal calcularTotalAbonos(Long idCliente) {
        try {
            List<MovimientoCapital> movimientos = movimientoCapitalDAO.findByCliente(idCliente);
            return movimientos.stream()
                .filter(m -> m.getTipoMovimiento() == MovimientoCapital.TipoMovimiento.ABONO)
                .map(MovimientoCapital::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            logger.error("Error al calcular total de abonos del cliente: " + idCliente, e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * Calcula el total de desembolsos de un cliente
     */
    public BigDecimal calcularTotalDesembolsos(Long idCliente) {
        try {
            List<MovimientoCapital> movimientos = movimientoCapitalDAO.findByCliente(idCliente);
            return movimientos.stream()
                .filter(m -> m.getTipoMovimiento() == MovimientoCapital.TipoMovimiento.DESEMBOLSO)
                .map(MovimientoCapital::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        } catch (Exception e) {
            logger.error("Error al calcular total de desembolsos del cliente: " + idCliente, e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * Obtiene un movimiento por ID
     */
    public MovimientoCapital obtenerMovimientoPorId(Long idMovimiento) {
        try {
            Optional<MovimientoCapital> movimientoOpt = movimientoCapitalDAO.findById(idMovimiento);
            return movimientoOpt.orElse(null);
        } catch (Exception e) {
            logger.error("Error al obtener movimiento por ID: " + idMovimiento, e);
            return null;
        }
    }

    /**
     * Verifica si un cliente puede realizar un desembolso
     */
    public boolean puedeRealizarDesembolso(Long idCliente, BigDecimal monto) {
        try {
            Optional<pe.crediactiva.app.model.Cliente> clienteOpt = clienteService.obtenerClientePorId(idCliente);
            if (!clienteOpt.isPresent()) {
                return false;
            }

            pe.crediactiva.app.model.Cliente cliente = clienteOpt.get();
            return cliente.getSaldoCapital().compareTo(monto) >= 0;
        } catch (Exception e) {
            logger.error("Error al verificar posibilidad de desembolso", e);
            return false;
        }
    }
}
