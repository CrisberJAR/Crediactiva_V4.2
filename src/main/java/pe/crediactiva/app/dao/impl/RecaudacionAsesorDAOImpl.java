package pe.crediactiva.app.dao.impl;

import pe.crediactiva.app.config.DatabaseConfig;
import pe.crediactiva.app.dao.RecaudacionAsesorDAO;
import pe.crediactiva.app.model.RecaudacionAsesor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import pe.crediactiva.app.util.DateTimeUtil;

/**
 * Implementación JDBC del DAO para RecaudacionAsesor
 */
public class RecaudacionAsesorDAOImpl implements RecaudacionAsesorDAO {

    private static final Logger logger = LoggerFactory.getLogger(RecaudacionAsesorDAOImpl.class);

    @Override
    public Optional<RecaudacionAsesor> findById(Long idRecaudacion) {
        String sql = "SELECT * FROM recaudacion_asesor WHERE id_recaudacion = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idRecaudacion);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToRecaudacionAsesor(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error al buscar recaudación por ID: " + idRecaudacion, e);
        }

        return Optional.empty();
    }

    @Override
    public boolean create(RecaudacionAsesor recaudacion) {
        String sql = "INSERT INTO recaudacion_asesor (id_asesor, id_cliente, id_prestamo, id_cuota, fecha_registro, monto_registrado, validado, observaciones) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, recaudacion.getIdAsesor());
            stmt.setLong(2, recaudacion.getIdCliente());
            stmt.setLong(3, recaudacion.getIdPrestamo());
            stmt.setObject(4, recaudacion.getIdCuota()); // Puede ser NULL
            stmt.setTimestamp(5, DateTimeUtil.nowAsTimestamp());
            stmt.setBigDecimal(6, recaudacion.getMontoRegistrado());
            stmt.setBoolean(7, recaudacion.isValidado());
            stmt.setString(8, recaudacion.getObservaciones());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        recaudacion.setIdRecaudacion(rs.getLong(1));
                    }
                }
                logger.info("Recaudación de asesor registrada: " + recaudacion.getIdRecaudacion());
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error al registrar recaudación de asesor", e);
        }
        return false;
    }

    @Override
    public boolean update(RecaudacionAsesor recaudacion) {
        String sql = "UPDATE recaudacion_asesor SET id_asesor = ?, id_cliente = ?, id_prestamo = ?, id_cuota = ?, " +
                    "fecha_registro = ?, monto_registrado = ?, validado = ?, observaciones = ? WHERE id_recaudacion = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, recaudacion.getIdAsesor());
            stmt.setLong(2, recaudacion.getIdCliente());
            stmt.setLong(3, recaudacion.getIdPrestamo());
            stmt.setObject(4, recaudacion.getIdCuota()); // Puede ser NULL
            stmt.setTimestamp(5, DateTimeUtil.nowAsTimestamp());
            stmt.setBigDecimal(6, recaudacion.getMontoRegistrado());
            stmt.setBoolean(7, recaudacion.isValidado());
            stmt.setString(8, recaudacion.getObservaciones());
            stmt.setLong(9, recaudacion.getIdRecaudacion());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Recaudación de asesor actualizada: " + recaudacion.getIdRecaudacion());
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error al actualizar recaudación de asesor: " + recaudacion.getIdRecaudacion(), e);
        }
        return false;
    }

    @Override
    public boolean delete(Long idRecaudacion) {
        String sql = "DELETE FROM recaudacion_asesor WHERE id_recaudacion = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idRecaudacion);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Recaudación de asesor eliminada: " + idRecaudacion);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error al eliminar recaudación de asesor: " + idRecaudacion, e);
        }
        return false;
    }

    @Override
    public List<RecaudacionAsesor> findAll() {
        String sql = "SELECT * FROM recaudacion_asesor ORDER BY fecha_registro DESC";
        List<RecaudacionAsesor> recaudaciones = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                recaudaciones.add(mapResultSetToRecaudacionAsesor(rs));
            }

        } catch (SQLException e) {
            logger.error("Error al obtener todas las recaudaciones", e);
        }

        return recaudaciones;
    }

    @Override
    public List<RecaudacionAsesor> findByAsesor(Long idAsesor) {
        String sql = "SELECT * FROM recaudacion_asesor WHERE id_asesor = ? ORDER BY fecha_registro DESC";
        List<RecaudacionAsesor> recaudaciones = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idAsesor);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    recaudaciones.add(mapResultSetToRecaudacionAsesor(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error al buscar recaudaciones por asesor: " + idAsesor, e);
        }

        return recaudaciones;
    }

    @Override
    public List<RecaudacionAsesor> findByCliente(Long idCliente) {
        String sql = "SELECT * FROM recaudacion_asesor WHERE id_cliente = ? ORDER BY fecha_registro DESC";
        List<RecaudacionAsesor> recaudaciones = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idCliente);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    recaudaciones.add(mapResultSetToRecaudacionAsesor(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error al buscar recaudaciones por cliente: " + idCliente, e);
        }

        return recaudaciones;
    }

    @Override
    public List<RecaudacionAsesor> findByPrestamo(Long idPrestamo) {
        String sql = "SELECT * FROM recaudacion_asesor WHERE id_prestamo = ? ORDER BY fecha_registro DESC";
        List<RecaudacionAsesor> recaudaciones = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idPrestamo);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    recaudaciones.add(mapResultSetToRecaudacionAsesor(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error al buscar recaudaciones por préstamo: " + idPrestamo, e);
        }

        return recaudaciones;
    }

    @Override
    public List<RecaudacionAsesor> findByFecha(LocalDate fecha) {
        String sql = "SELECT * FROM recaudacion_asesor WHERE DATE(fecha_registro) = ? ORDER BY fecha_registro DESC";
        List<RecaudacionAsesor> recaudaciones = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setDate(1, Date.valueOf(fecha));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    recaudaciones.add(mapResultSetToRecaudacionAsesor(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error al buscar recaudaciones por fecha: " + fecha, e);
        }

        return recaudaciones;
    }

    @Override
    public List<RecaudacionAsesor> findPendientes() {
        String sql = "SELECT * FROM recaudacion_asesor WHERE validado = FALSE ORDER BY fecha_registro DESC";
        List<RecaudacionAsesor> recaudaciones = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                recaudaciones.add(mapResultSetToRecaudacionAsesor(rs));
            }

        } catch (SQLException e) {
            logger.error("Error al buscar recaudaciones pendientes", e);
        }

        return recaudaciones;
    }

    @Override
    public boolean marcarComoValidado(Long idRecaudacion) {
        String sql = "UPDATE recaudacion_asesor SET validado = TRUE WHERE id_recaudacion = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idRecaudacion);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Recaudación marcada como validada: " + idRecaudacion);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error al marcar recaudación como validada: " + idRecaudacion, e);
        }
        return false;
    }
    
    @Override
    public boolean existeBorradorPendiente(Long idPrestamo) {
        String sql = "SELECT COUNT(*) FROM recaudacion_asesor WHERE id_prestamo = ? AND validado = FALSE";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idPrestamo);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count > 0;
                }
            }

        } catch (SQLException e) {
            logger.error("Error al verificar borrador pendiente para préstamo: " + idPrestamo, e);
        }
        return false;
    }
    
    @Override
    public Optional<RecaudacionAsesor> obtenerBorradorPendiente(Long idPrestamo) {
        String sql = "SELECT * FROM recaudacion_asesor WHERE id_prestamo = ? AND validado = FALSE ORDER BY fecha_registro DESC LIMIT 1";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idPrestamo);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToRecaudacionAsesor(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error al obtener borrador pendiente para préstamo: " + idPrestamo, e);
        }
        return Optional.empty();
    }
    
    /**
     * Verifica si existe una recaudación pendiente para una cuota específica
     */
    public boolean existeRecaudacionPendienteParaCuota(Long idPrestamo, Long idCuota) {
        String sql = "SELECT COUNT(*) FROM recaudacion_asesor WHERE id_prestamo = ? AND id_cuota = ? AND validado = FALSE";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idPrestamo);
            stmt.setLong(2, idCuota);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count > 0;
                }
            }

        } catch (SQLException e) {
            logger.error("Error al verificar recaudación pendiente para cuota: " + idCuota, e);
        }
        return false;
    }

    private RecaudacionAsesor mapResultSetToRecaudacionAsesor(ResultSet rs) throws SQLException {
        RecaudacionAsesor recaudacion = new RecaudacionAsesor();
        recaudacion.setIdRecaudacion(rs.getLong("id_recaudacion"));
        recaudacion.setIdAsesor(rs.getLong("id_asesor"));
        recaudacion.setIdCliente(rs.getLong("id_cliente"));
        recaudacion.setIdPrestamo(rs.getLong("id_prestamo"));
        
        // Manejar id_cuota que puede ser NULL
        Long idCuota = rs.getLong("id_cuota");
        if (rs.wasNull()) {
            idCuota = null;
        }
        recaudacion.setIdCuota(idCuota);
        
        recaudacion.setFechaRegistro(rs.getTimestamp("fecha_registro").toLocalDateTime());
        recaudacion.setMontoRegistrado(rs.getBigDecimal("monto_registrado"));
        recaudacion.setValidado(rs.getBoolean("validado"));
        
        // Manejar observaciones que puede ser NULL
        String observaciones = rs.getString("observaciones");
        recaudacion.setObservaciones(observaciones);
        
        return recaudacion;
    }
    
    @Override
    public List<RecaudacionAsesor> findByFechaAndAsesor(LocalDate fecha, Long idAsesor) {
        List<RecaudacionAsesor> recaudaciones = new ArrayList<>();
        
        String sql = """
            SELECT r.*, p.id_asesor 
            FROM recaudacion_asesor r
            INNER JOIN prestamos p ON r.id_prestamo = p.id_prestamo
            WHERE DATE(r.fecha_registro) = ? 
            AND p.id_asesor = ?
            ORDER BY r.fecha_registro DESC
            """;
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, java.sql.Date.valueOf(fecha));
            stmt.setLong(2, idAsesor);
            
            logger.info("Ejecutando consulta: " + sql);
            logger.info("Parámetros - Fecha: " + fecha + ", Asesor: " + idAsesor);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    RecaudacionAsesor recaudacion = mapResultSetToRecaudacionAsesor(rs);
                    recaudaciones.add(recaudacion);
                }
            }
            
            logger.info("Recaudaciones encontradas para asesor " + idAsesor + " en fecha " + fecha + ": " + recaudaciones.size());
            
        } catch (SQLException e) {
            logger.error("Error al buscar recaudaciones por fecha y asesor", e);
        }
        
        return recaudaciones;
    }
}