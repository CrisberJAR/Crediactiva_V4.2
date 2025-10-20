package pe.crediactiva.app.dao.impl;

import pe.crediactiva.app.config.DatabaseConfig;
import pe.crediactiva.app.dao.MovimientoCapitalDAO;
import pe.crediactiva.app.model.MovimientoCapital;
import pe.crediactiva.app.model.Cliente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import pe.crediactiva.app.util.DateTimeUtil;

/**
 * Implementación JDBC del DAO para MovimientoCapital
 */
public class MovimientoCapitalDAOImpl implements MovimientoCapitalDAO {

    private static final Logger logger = LoggerFactory.getLogger(MovimientoCapitalDAOImpl.class);

    @Override
    public Optional<MovimientoCapital> findById(Long idMovimiento) {
        String sql = "SELECT * FROM movimientos_capital WHERE id_movimiento = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idMovimiento);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToMovimientoCapital(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error al buscar movimiento por ID: " + idMovimiento, e);
        }

        return Optional.empty();
    }

    @Override
    public boolean create(MovimientoCapital movimiento) {
        String sql = "INSERT INTO movimientos_capital (id_cliente, tipo_movimiento, monto, fecha, id_admin, observacion) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, movimiento.getIdCliente());
            stmt.setString(2, movimiento.getTipoMovimiento().name().toLowerCase());
            stmt.setBigDecimal(3, movimiento.getMonto());
            stmt.setTimestamp(4, DateTimeUtil.nowAsTimestamp());
            stmt.setLong(5, movimiento.getIdAdmin());
            stmt.setString(6, movimiento.getObservacion());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        movimiento.setIdMovimiento(rs.getLong(1));
                    }
                }
                logger.info("Movimiento de capital creado exitosamente: " + movimiento.getIdMovimiento());
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error al crear movimiento de capital", e);
        }
        return false;
    }

    @Override
    public boolean update(MovimientoCapital movimiento) {
        String sql = "UPDATE movimientos_capital SET id_cliente = ?, tipo_movimiento = ?, monto = ?, " +
                    "fecha = ?, id_admin = ? WHERE id_movimiento = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, movimiento.getIdCliente());
            stmt.setString(2, movimiento.getTipoMovimiento().name().toLowerCase());
            stmt.setBigDecimal(3, movimiento.getMonto());
            stmt.setTimestamp(4, DateTimeUtil.nowAsTimestamp());
            stmt.setLong(5, movimiento.getIdAdmin());
            stmt.setLong(6, movimiento.getIdMovimiento());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Movimiento de capital actualizado exitosamente: " + movimiento.getIdMovimiento());
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error al actualizar movimiento de capital: " + movimiento.getIdMovimiento(), e);
        }
        return false;
    }

    @Override
    public boolean delete(Long idMovimiento) {
        String sql = "DELETE FROM movimientos_capital WHERE id_movimiento = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idMovimiento);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Movimiento de capital eliminado exitosamente: " + idMovimiento);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error al eliminar movimiento de capital: " + idMovimiento, e);
        }
        return false;
    }

    @Override
    public List<MovimientoCapital> findAll() {
        String sql = "SELECT * FROM movimientos_capital ORDER BY fecha DESC";
        List<MovimientoCapital> movimientos = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                movimientos.add(mapResultSetToMovimientoCapital(rs));
            }

        } catch (SQLException e) {
            logger.error("Error al obtener todos los movimientos de capital", e);
        }

        return movimientos;
    }

    @Override
    public List<MovimientoCapital> findByCliente(Long idCliente) {
        String sql = "SELECT * FROM movimientos_capital WHERE id_cliente = ? ORDER BY fecha DESC";
        List<MovimientoCapital> movimientos = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idCliente);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    movimientos.add(mapResultSetToMovimientoCapital(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error al buscar movimientos por cliente: " + idCliente, e);
        }

        return movimientos;
    }

    @Override
    public List<MovimientoCapital> findByTipo(MovimientoCapital.TipoMovimiento tipo) {
        String sql = "SELECT * FROM movimientos_capital WHERE tipo_movimiento = ? ORDER BY fecha DESC";
        List<MovimientoCapital> movimientos = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tipo.name().toLowerCase());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    movimientos.add(mapResultSetToMovimientoCapital(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error al buscar movimientos por tipo: " + tipo, e);
        }

        return movimientos;
    }

    @Override
    public List<MovimientoCapital> findByFecha(LocalDate fecha) {
        String sql = "SELECT * FROM movimientos_capital WHERE DATE(fecha) = ? ORDER BY fecha DESC";
        List<MovimientoCapital> movimientos = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(fecha));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    movimientos.add(mapResultSetToMovimientoCapital(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error al buscar movimientos por fecha: " + fecha, e);
        }

        return movimientos;
    }

    @Override
    public List<MovimientoCapital> findByRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        String sql = "SELECT * FROM movimientos_capital WHERE DATE(fecha) BETWEEN ? AND ? ORDER BY fecha DESC";
        List<MovimientoCapital> movimientos = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(fechaInicio));
            stmt.setDate(2, Date.valueOf(fechaFin));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    movimientos.add(mapResultSetToMovimientoCapital(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error al buscar movimientos por rango: " + fechaInicio + " - " + fechaFin, e);
        }

        return movimientos;
    }

    @Override
    public List<MovimientoCapital> findByAdmin(Long idAdmin) {
        String sql = "SELECT * FROM movimientos_capital WHERE id_admin = ? ORDER BY fecha DESC";
        List<MovimientoCapital> movimientos = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idAdmin);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    movimientos.add(mapResultSetToMovimientoCapital(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error al buscar movimientos por admin: " + idAdmin, e);
        }

        return movimientos;
    }

    @Override
    public boolean exists(Long idMovimiento) {
        String sql = "SELECT COUNT(*) FROM movimientos_capital WHERE id_movimiento = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idMovimiento);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            logger.error("Error al verificar existencia de movimiento: " + idMovimiento, e);
        }

        return false;
    }
    
    @Override
    public List<MovimientoCapital> findAllWithCliente() {
        String sql = "SELECT mc.*, c.nombre, c.apellido " +
                    "FROM movimientos_capital mc " +
                    "LEFT JOIN clientes c ON mc.id_cliente = c.id_cliente " +
                    "ORDER BY mc.fecha DESC";
        List<MovimientoCapital> movimientos = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                MovimientoCapital movimiento = mapResultSetToMovimientoCapital(rs);
                
                // Agregar información del cliente si existe
                String nombre = rs.getString("nombre");
                String apellido = rs.getString("apellido");
                if (nombre != null && apellido != null) {
                    Cliente cliente = new Cliente();
                    cliente.setNombre(nombre);
                    cliente.setApellido(apellido);
                    movimiento.setCliente(cliente);
                }
                
                movimientos.add(movimiento);
            }

        } catch (SQLException e) {
            logger.error("Error al obtener movimientos con información de cliente", e);
        }

        return movimientos;
    }

    private MovimientoCapital mapResultSetToMovimientoCapital(ResultSet rs) throws SQLException {
        MovimientoCapital movimiento = new MovimientoCapital();
        movimiento.setIdMovimiento(rs.getLong("id_movimiento"));
        movimiento.setIdCliente(rs.getLong("id_cliente"));
        movimiento.setTipoMovimiento(MovimientoCapital.TipoMovimiento.valueOf(rs.getString("tipo_movimiento").toUpperCase()));
        movimiento.setMonto(rs.getBigDecimal("monto"));
        movimiento.setFecha(rs.getTimestamp("fecha").toLocalDateTime());
        movimiento.setIdAdmin(rs.getLong("id_admin"));
        movimiento.setObservacion(rs.getString("observacion"));
        return movimiento;
    }
}
