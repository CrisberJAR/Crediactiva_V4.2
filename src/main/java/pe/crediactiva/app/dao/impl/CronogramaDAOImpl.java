package pe.crediactiva.app.dao.impl;

import pe.crediactiva.app.config.DatabaseConfig;
import pe.crediactiva.app.dao.CronogramaDAO;
import pe.crediactiva.app.model.Cronograma;
import pe.crediactiva.app.model.Prestamo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC del DAO para Cronograma
 */
public class CronogramaDAOImpl implements CronogramaDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(CronogramaDAOImpl.class);
    
    @Override
    public Optional<Cronograma> findById(Long idCuota) {
        String sql = "SELECT c.*, p.id_cliente, p.id_asesor " +
                    "FROM cronograma c " +
                    "JOIN prestamos p ON c.id_prestamo = p.id_prestamo " +
                    "WHERE c.id_cuota = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idCuota);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCronograma(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar cuota por ID: " + idCuota, e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<Cronograma> findByPrestamo(Long idPrestamo) {
        String sql = "SELECT c.*, p.id_cliente, p.id_asesor " +
                    "FROM cronograma c " +
                    "JOIN prestamos p ON c.id_prestamo = p.id_prestamo " +
                    "WHERE c.id_prestamo = ? " +
                    "ORDER BY c.numero_cuota";
        
        List<Cronograma> cronogramas = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idPrestamo);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cronogramas.add(mapResultSetToCronograma(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar cuotas por préstamo: " + idPrestamo, e);
        }
        
        return cronogramas;
    }
    
    @Override
    public List<Cronograma> findByEstado(Cronograma.EstadoCuota estado) {
        String sql = "SELECT c.*, p.id_cliente, p.id_asesor " +
                    "FROM cronograma c " +
                    "JOIN prestamos p ON c.id_prestamo = p.id_prestamo " +
                    "WHERE c.estado_cuota = ? " +
                    "ORDER BY c.fecha_programada";
        
        List<Cronograma> cronogramas = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, estado.name().toLowerCase());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cronogramas.add(mapResultSetToCronograma(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar cuotas por estado: " + estado, e);
        }
        
        return cronogramas;
    }
    
    @Override
    public List<Cronograma> findVencidas() {
        String sql = "SELECT c.*, p.id_cliente, p.id_asesor " +
                    "FROM cronograma c " +
                    "JOIN prestamos p ON c.id_prestamo = p.id_prestamo " +
                    "WHERE c.estado_cuota = 'pendiente' AND c.fecha_programada < CURDATE() " +
                    "ORDER BY c.fecha_programada";
        
        List<Cronograma> cronogramas = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                cronogramas.add(mapResultSetToCronograma(rs));
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar cuotas vencidas", e);
        }
        
        return cronogramas;
    }
    
    @Override
    public List<Cronograma> findProximasAVencer(int dias) {
        String sql = "SELECT c.*, p.id_cliente, p.id_asesor " +
                    "FROM cronograma c " +
                    "JOIN prestamos p ON c.id_prestamo = p.id_prestamo " +
                    "WHERE c.estado_cuota = 'pendiente' AND c.fecha_programada BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL ? DAY) " +
                    "ORDER BY c.fecha_programada";
        
        List<Cronograma> cronogramas = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, dias);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cronogramas.add(mapResultSetToCronograma(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar cuotas próximas a vencer", e);
        }
        
        return cronogramas;
    }
    
    @Override
    public List<Cronograma> findPendientesByPrestamo(Long idPrestamo) {
        String sql = "SELECT c.*, p.id_cliente, p.id_asesor " +
                    "FROM cronograma c " +
                    "JOIN prestamos p ON c.id_prestamo = p.id_prestamo " +
                    "WHERE c.id_prestamo = ? AND c.estado_cuota = 'pendiente' " +
                    "ORDER BY c.numero_cuota";
        
        List<Cronograma> cronogramas = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idPrestamo);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cronogramas.add(mapResultSetToCronograma(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar cuotas pendientes por préstamo: " + idPrestamo, e);
        }
        
        return cronogramas;
    }
    
    @Override
    public boolean create(Cronograma cronograma) {
        String sql = "INSERT INTO cronograma (id_prestamo, numero_cuota, fecha_programada, " +
                    "monto_cuota, estado_cuota, fecha_pago_real) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, cronograma.getIdPrestamo());
            stmt.setInt(2, cronograma.getNumeroCuota());
            stmt.setDate(3, Date.valueOf(cronograma.getFechaProgramada()));
            stmt.setBigDecimal(4, cronograma.getMontoCuota());
            stmt.setString(5, cronograma.getEstadoCuota().name().toLowerCase());
            stmt.setDate(6, cronograma.getFechaPagoReal() != null ? Date.valueOf(cronograma.getFechaPagoReal()) : null);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.debug("Cuota creada: " + cronograma.getNumeroCuota() + " para préstamo: " + cronograma.getIdPrestamo());
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al crear cuota", e);
        }
        
        return false;
    }
    
    @Override
    public boolean update(Cronograma cronograma) {
        String sql = "UPDATE cronograma SET fecha_programada = ?, monto_cuota = ?, " +
                    "estado_cuota = ?, fecha_pago_real = ? WHERE id_cuota = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(cronograma.getFechaProgramada()));
            stmt.setBigDecimal(2, cronograma.getMontoCuota());
            stmt.setString(3, cronograma.getEstadoCuota().name().toLowerCase());
            stmt.setDate(4, cronograma.getFechaPagoReal() != null ? Date.valueOf(cronograma.getFechaPagoReal()) : null);
            stmt.setLong(5, cronograma.getIdCuota());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.debug("Cuota actualizada: " + cronograma.getIdCuota());
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al actualizar cuota: " + cronograma.getIdCuota(), e);
        }
        
        return false;
    }
    
    @Override
    public boolean updateEstado(Long idCuota, Cronograma.EstadoCuota nuevoEstado) {
        String sql = "UPDATE cronograma SET estado_cuota = ? WHERE id_cuota = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nuevoEstado.name().toLowerCase());
            stmt.setLong(2, idCuota);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.debug("Estado de cuota actualizado: " + idCuota + " -> " + nuevoEstado);
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al actualizar estado de cuota: " + idCuota, e);
        }
        
        return false;
    }
    
    @Override
    public boolean marcarComoPagada(Long idCuota, LocalDate fechaPago) {
        String sql = "UPDATE cronograma SET estado_cuota = 'pagada', fecha_pago_real = ? WHERE id_cuota = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(fechaPago));
            stmt.setLong(2, idCuota);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.debug("Cuota marcada como pagada: " + idCuota + " - " + fechaPago);
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al marcar cuota como pagada: " + idCuota, e);
        }
        
        return false;
    }
    
    @Override
    public Optional<Cronograma> findProximaCuota(Long idPrestamo) {
        String sql = "SELECT c.*, p.id_cliente, p.id_asesor " +
                    "FROM cronograma c " +
                    "JOIN prestamos p ON c.id_prestamo = p.id_prestamo " +
                    "WHERE c.id_prestamo = ? AND c.estado_cuota = 'pendiente' " +
                    "ORDER BY c.numero_cuota LIMIT 1";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idPrestamo);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCronograma(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar próxima cuota del préstamo: " + idPrestamo, e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<Cronograma> findPagadasByPrestamo(Long idPrestamo) {
        String sql = "SELECT c.*, p.id_cliente, p.id_asesor " +
                    "FROM cronograma c " +
                    "JOIN prestamos p ON c.id_prestamo = p.id_prestamo " +
                    "WHERE c.id_prestamo = ? AND c.estado_cuota = 'pagada' " +
                    "ORDER BY c.numero_cuota";
        
        List<Cronograma> cronogramas = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idPrestamo);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cronogramas.add(mapResultSetToCronograma(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar cuotas pagadas por préstamo: " + idPrestamo, e);
        }
        
        return cronogramas;
    }
    
    @Override
    public boolean areAllCuotasPagadas(Long idPrestamo) {
        String sql = "SELECT COUNT(*) FROM cronograma WHERE id_prestamo = ? AND estado_cuota != 'pagada'";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idPrestamo);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) == 0;
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al verificar si todas las cuotas están pagadas: " + idPrestamo, e);
        }
        
        return false;
    }
    
    /**
     * Mapea un ResultSet a un objeto Cronograma
     */
    private Cronograma mapResultSetToCronograma(ResultSet rs) throws SQLException {
        Cronograma cronograma = new Cronograma();
        cronograma.setIdCuota(rs.getLong("id_cuota"));
        cronograma.setIdPrestamo(rs.getLong("id_prestamo"));
        cronograma.setNumeroCuota(rs.getInt("numero_cuota"));
        cronograma.setFechaProgramada(rs.getDate("fecha_programada").toLocalDate());
        cronograma.setMontoCuota(rs.getBigDecimal("monto_cuota"));
        cronograma.setEstadoCuota(Cronograma.EstadoCuota.valueOf(rs.getString("estado_cuota").toUpperCase()));
        
        Date fechaPagoReal = rs.getDate("fecha_pago_real");
        if (fechaPagoReal != null) {
            cronograma.setFechaPagoReal(fechaPagoReal.toLocalDate());
        }
        
        // Mapear el campo validacion_asesor
        int validacionAsesor = rs.getInt("validacion_asesor");
        cronograma.setValidacionAsesor(validacionAsesor == 1);
        
        // Crear el préstamo básico
        Prestamo prestamo = new Prestamo();
        prestamo.setIdPrestamo(rs.getLong("id_prestamo"));
        prestamo.setIdCliente(rs.getLong("id_cliente"));
        prestamo.setIdAsesor(rs.getLong("id_asesor"));
        cronograma.setPrestamo(prestamo);
        
        return cronograma;
    }
    
    @Override
    public List<Cronograma> findByFecha(LocalDate fecha) {
        String sql = "SELECT c.*, p.id_cliente, p.id_asesor " +
                    "FROM cronograma c " +
                    "JOIN prestamos p ON c.id_prestamo = p.id_prestamo " +
                    "WHERE c.fecha_programada = ? " +
                    "ORDER BY c.numero_cuota";
        
        List<Cronograma> cuotas = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setDate(1, Date.valueOf(fecha));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cuotas.add(mapResultSetToCronograma(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar cuotas por fecha: " + fecha, e);
        }
        
        return cuotas;
    }
    
    @Override
    public List<Cronograma> findPendientesByCliente(Long idCliente) {
        String sql = "SELECT c.*, p.id_cliente, p.id_asesor " +
                    "FROM cronograma c " +
                    "JOIN prestamos p ON c.id_prestamo = p.id_prestamo " +
                    "WHERE p.id_cliente = ? AND c.estado_cuota = 'pendiente' " +
                    "ORDER BY c.fecha_programada";
        
        List<Cronograma> cuotas = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idCliente);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cuotas.add(mapResultSetToCronograma(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar cuotas pendientes por cliente: " + idCliente, e);
        }
        
        return cuotas;
    }
    
    @Override
    public List<Cronograma> findVencidasByCliente(Long idCliente) {
        String sql = "SELECT c.*, p.id_cliente, p.id_asesor " +
                    "FROM cronograma c " +
                    "JOIN prestamos p ON c.id_prestamo = p.id_prestamo " +
                    "WHERE p.id_cliente = ? AND c.estado_cuota = 'retrasada' " +
                    "ORDER BY c.fecha_programada";
        
        List<Cronograma> cuotas = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idCliente);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cuotas.add(mapResultSetToCronograma(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar cuotas vencidas por cliente: " + idCliente, e);
        }
        
        return cuotas;
    }
    
    @Override
    public List<Cronograma> findByPrestamoId(Long idPrestamo) {
        return findByPrestamo(idPrestamo);
    }
    
    @Override
    public List<Cronograma> findDisponiblesParaRecaudacion(Long idPrestamo) {
        String sql = "SELECT c.*, p.id_cliente, p.id_asesor " +
                    "FROM cronograma c " +
                    "JOIN prestamos p ON c.id_prestamo = p.id_prestamo " +
                    "WHERE c.id_prestamo = ? " +
                    "AND c.estado_cuota IN ('pendiente', 'retrasada') " +
                    "AND (c.validacion_asesor = 0 OR c.validacion_asesor IS NULL) " +
                    "ORDER BY c.numero_cuota";
        
        List<Cronograma> cuotas = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idPrestamo);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    cuotas.add(mapResultSetToCronograma(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar cuotas disponibles para recaudación: " + idPrestamo, e);
        }
        
        return cuotas;
    }
    
    @Override
    public boolean marcarValidacionAsesor(Long idCuota, boolean validado) {
        String sql = "UPDATE cronograma SET validacion_asesor = ? WHERE id_cuota = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, validado ? 1 : 0);
            stmt.setLong(2, idCuota);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            logger.error("Error al marcar validación asesor para cuota: " + idCuota, e);
            return false;
        }
    }
}
