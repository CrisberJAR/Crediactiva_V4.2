package pe.crediactiva.app.dao.impl;

import pe.crediactiva.app.config.DatabaseConfig;
import pe.crediactiva.app.dao.PagoDAO;
import pe.crediactiva.app.model.Pago;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC del DAO para Pago
 */
public class PagoDAOImpl implements PagoDAO {

    private static final Logger logger = LoggerFactory.getLogger(PagoDAOImpl.class);

    @Override
    public Optional<Pago> findById(Long idPago) {
        String sql = "SELECT * FROM pagos WHERE id_pago = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idPago);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPago(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error al buscar pago por ID: " + idPago, e);
        }

        return Optional.empty();
    }

    @Override
    public boolean create(Pago pago) {
        String sql = "INSERT INTO pagos (id_cuota, id_cliente, id_asesor, fecha_pago, monto_pagado) " +
                    "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, pago.getIdCuota());
            stmt.setLong(2, pago.getIdCliente());
            stmt.setLong(3, pago.getIdAsesor());
            stmt.setTimestamp(4, Timestamp.valueOf(pago.getFechaPago()));
            stmt.setBigDecimal(5, pago.getMontoPagado());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        pago.setIdPago(rs.getLong(1));
                    }
                }
                logger.info("Pago registrado exitosamente: " + pago.getIdPago());
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error al registrar pago", e);
        }
        return false;
    }

    @Override
    public boolean update(Pago pago) {
        String sql = "UPDATE pagos SET id_cuota = ?, id_cliente = ?, id_asesor = ?, " +
                    "fecha_pago = ?, monto_pagado = ? WHERE id_pago = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, pago.getIdCuota());
            stmt.setLong(2, pago.getIdCliente());
            stmt.setLong(3, pago.getIdAsesor());
            stmt.setTimestamp(4, Timestamp.valueOf(pago.getFechaPago()));
            stmt.setBigDecimal(5, pago.getMontoPagado());
            stmt.setLong(6, pago.getIdPago());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Pago actualizado exitosamente: " + pago.getIdPago());
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error al actualizar pago: " + pago.getIdPago(), e);
        }
        return false;
    }

    @Override
    public boolean delete(Long idPago) {
        String sql = "DELETE FROM pagos WHERE id_pago = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idPago);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Pago eliminado exitosamente: " + idPago);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error al eliminar pago: " + idPago, e);
        }
        return false;
    }

    @Override
    public List<Pago> findAll() {
        String sql = "SELECT * FROM pagos ORDER BY fecha_pago DESC";
        List<Pago> pagos = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                pagos.add(mapResultSetToPago(rs));
            }

        } catch (SQLException e) {
            logger.error("Error al obtener todos los pagos", e);
        }

        return pagos;
    }

    @Override
    public List<Pago> findByCuota(Long idCuota) {
        String sql = "SELECT * FROM pagos WHERE id_cuota = ? ORDER BY fecha_pago DESC";
        List<Pago> pagos = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idCuota);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pagos.add(mapResultSetToPago(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error al buscar pagos por cuota: " + idCuota, e);
        }

        return pagos;
    }

    @Override
    public List<Pago> findByCliente(Long idCliente) {
        String sql = "SELECT * FROM pagos WHERE id_cliente = ? ORDER BY fecha_pago DESC";
        List<Pago> pagos = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idCliente);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pagos.add(mapResultSetToPago(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error al buscar pagos por cliente: " + idCliente, e);
        }

        return pagos;
    }

    @Override
    public List<Pago> findByAsesor(Long idAsesor) {
        String sql = "SELECT * FROM pagos WHERE id_asesor = ? ORDER BY fecha_pago DESC";
        List<Pago> pagos = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idAsesor);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pagos.add(mapResultSetToPago(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error al buscar pagos por asesor: " + idAsesor, e);
        }

        return pagos;
    }

    @Override
    public List<Pago> findByFecha(LocalDate fecha) {
        String sql = "SELECT * FROM pagos WHERE DATE(fecha_pago) = ? ORDER BY fecha_pago DESC";
        List<Pago> pagos = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(fecha));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pagos.add(mapResultSetToPago(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error al buscar pagos por fecha: " + fecha, e);
        }

        return pagos;
    }

    @Override
    public List<Pago> findByClienteAndFecha(Long idCliente, LocalDate fechaInicio, LocalDate fechaFin) {
        String sql = "SELECT * FROM pagos WHERE id_cliente = ? AND DATE(fecha_pago) BETWEEN ? AND ? ORDER BY fecha_pago DESC";
        List<Pago> pagos = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idCliente);
            stmt.setDate(2, Date.valueOf(fechaInicio));
            stmt.setDate(3, Date.valueOf(fechaFin));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pagos.add(mapResultSetToPago(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error al buscar pagos por cliente y fecha: " + idCliente, e);
        }

        return pagos;
    }

    @Override
    public List<Pago> findByAsesorAndFecha(Long idAsesor, LocalDate fechaInicio, LocalDate fechaFin) {
        String sql = "SELECT * FROM pagos WHERE id_asesor = ? AND DATE(fecha_pago) BETWEEN ? AND ? ORDER BY fecha_pago DESC";
        List<Pago> pagos = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idAsesor);
            stmt.setDate(2, Date.valueOf(fechaInicio));
            stmt.setDate(3, Date.valueOf(fechaFin));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pagos.add(mapResultSetToPago(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error al buscar pagos por asesor y fecha: " + idAsesor, e);
        }

        return pagos;
    }

    @Override
    public List<Pago> findByPrestamo(Long idPrestamo) {
        String sql = "SELECT p.* FROM pagos p " +
                    "JOIN cronograma c ON p.id_cuota = c.id_cuota " +
                    "WHERE c.id_prestamo = ? ORDER BY p.fecha_pago DESC";
        List<Pago> pagos = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idPrestamo);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pagos.add(mapResultSetToPago(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error al buscar pagos por préstamo: " + idPrestamo, e);
        }

        return pagos;
    }
    
    @Override
    public List<Pago> findPendientesValidacion() {
        List<Pago> pagos = new ArrayList<>();
        String sql = "SELECT * FROM pagos WHERE validado = false ORDER BY fecha_registro DESC";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pagos.add(mapResultSetToPago(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error al buscar pagos pendientes de validación", e);
        }

        return pagos;
    }

    private Pago mapResultSetToPago(ResultSet rs) throws SQLException {
        Pago pago = new Pago();
        pago.setIdPago(rs.getLong("id_pago"));
        pago.setIdCuota(rs.getLong("id_cuota"));
        pago.setIdCliente(rs.getLong("id_cliente"));
        pago.setIdAsesor(rs.getLong("id_asesor"));
        pago.setIdPrestamo(rs.getLong("id_prestamo"));
        pago.setFechaPago(rs.getTimestamp("fecha_pago").toLocalDateTime());
        pago.setFechaRegistro(rs.getTimestamp("fecha_registro").toLocalDateTime());
        pago.setMontoPagado(rs.getBigDecimal("monto_pagado"));
        pago.setValidado(rs.getBoolean("validado"));
        
        // Campos opcionales
        if (rs.getTimestamp("fecha_validacion") != null) {
            pago.setFechaValidacion(rs.getTimestamp("fecha_validacion").toLocalDateTime());
        }
        if (rs.getString("observaciones") != null) {
            pago.setObservaciones(rs.getString("observaciones"));
        }
        
        return pago;
    }
}
