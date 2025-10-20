package pe.crediactiva.app.dao.impl;

import pe.crediactiva.app.config.DatabaseConfig;
import pe.crediactiva.app.dao.PrestamoDAO;
import pe.crediactiva.app.model.Asesor;
import pe.crediactiva.app.model.Cliente;
import pe.crediactiva.app.model.Prestamo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import pe.crediactiva.app.util.DateTimeUtil;

/**
 * Implementación JDBC del DAO para Préstamo
 */
public class PrestamoDAOImpl implements PrestamoDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(PrestamoDAOImpl.class);
    
    @Override
    public Optional<Prestamo> findById(Long idPrestamo) {
        String sql = "SELECT p.*, c.nombre as cliente_nombre, c.apellido as cliente_apellido, " +
                    "a.nombre as asesor_nombre, a.apellido as asesor_apellido " +
                    "FROM prestamos p " +
                    "JOIN clientes c ON p.id_cliente = c.id_cliente " +
                    "JOIN asesores a ON p.id_asesor = a.id_asesor " +
                    "WHERE p.id_prestamo = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idPrestamo);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPrestamo(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar préstamo por ID: " + idPrestamo, e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<Prestamo> findAll() {
        String sql = "SELECT p.*, c.nombre as cliente_nombre, c.apellido as cliente_apellido, " +
                    "a.nombre as asesor_nombre, a.apellido as asesor_apellido " +
                    "FROM prestamos p " +
                    "JOIN clientes c ON p.id_cliente = c.id_cliente " +
                    "JOIN asesores a ON p.id_asesor = a.id_asesor " +
                    "ORDER BY p.creado_en DESC";
        
        List<Prestamo> prestamos = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                prestamos.add(mapResultSetToPrestamo(rs));
            }
            
        } catch (SQLException e) {
            logger.error("Error al obtener todos los préstamos", e);
        }
        
        return prestamos;
    }
    
    @Override
    public List<Prestamo> findByCliente(Long idCliente) {
        String sql = "SELECT p.*, c.nombre as cliente_nombre, c.apellido as cliente_apellido, " +
                    "a.nombre as asesor_nombre, a.apellido as asesor_apellido " +
                    "FROM prestamos p " +
                    "JOIN clientes c ON p.id_cliente = c.id_cliente " +
                    "JOIN asesores a ON p.id_asesor = a.id_asesor " +
                    "WHERE p.id_cliente = ? " +
                    "ORDER BY p.creado_en DESC";
        
        List<Prestamo> prestamos = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idCliente);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prestamos.add(mapResultSetToPrestamo(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar préstamos por cliente: " + idCliente, e);
        }
        
        return prestamos;
    }
    
    @Override
    public List<Prestamo> findByAsesor(Long idAsesor) {
        String sql = "SELECT p.*, c.nombre as cliente_nombre, c.apellido as cliente_apellido, " +
                    "a.nombre as asesor_nombre, a.apellido as asesor_apellido " +
                    "FROM prestamos p " +
                    "JOIN clientes c ON p.id_cliente = c.id_cliente " +
                    "JOIN asesores a ON p.id_asesor = a.id_asesor " +
                    "WHERE p.id_asesor = ? " +
                    "ORDER BY p.creado_en DESC";
        
        List<Prestamo> prestamos = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idAsesor);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prestamos.add(mapResultSetToPrestamo(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar préstamos por asesor: " + idAsesor, e);
        }
        
        return prestamos;
    }
    
    @Override
    public List<Prestamo> findByEstado(Prestamo.EstadoPrestamo estado) {
        String sql = "SELECT p.*, c.nombre as cliente_nombre, c.apellido as cliente_apellido, " +
                    "a.nombre as asesor_nombre, a.apellido as asesor_apellido " +
                    "FROM prestamos p " +
                    "JOIN clientes c ON p.id_cliente = c.id_cliente " +
                    "JOIN asesores a ON p.id_asesor = a.id_asesor " +
                    "WHERE p.estado = ? " +
                    "ORDER BY p.creado_en DESC";
        
        List<Prestamo> prestamos = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, estado.name().toLowerCase());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prestamos.add(mapResultSetToPrestamo(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar préstamos por estado: " + estado, e);
        }
        
        return prestamos;
    }
    
    @Override
    public List<Prestamo> findPendientes() {
        return findByEstado(Prestamo.EstadoPrestamo.PENDIENTE);
    }
    
    @Override
    public List<Prestamo> findActivos() {
        return findByEstado(Prestamo.EstadoPrestamo.ACTIVO);
    }
    
    @Override
    public List<Prestamo> findByEtiqueta(Prestamo.EtiquetaPrestamo etiqueta) {
        String sql = "SELECT p.*, c.nombre as cliente_nombre, c.apellido as cliente_apellido, " +
                    "a.nombre as asesor_nombre, a.apellido as asesor_apellido " +
                    "FROM prestamos p " +
                    "JOIN clientes c ON p.id_cliente = c.id_cliente " +
                    "JOIN asesores a ON p.id_asesor = a.id_asesor " +
                    "WHERE p.etiqueta = ? " +
                    "ORDER BY p.creado_en DESC";
        
        List<Prestamo> prestamos = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, etiqueta.name().toLowerCase());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prestamos.add(mapResultSetToPrestamo(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar préstamos por etiqueta: " + etiqueta, e);
        }
        
        return prestamos;
    }
    
    @Override
    public boolean create(Prestamo prestamo) {
        String sql = "INSERT INTO prestamos (id_cliente, id_asesor, monto_solicitado, monto_desembolsado, " +
                    "tasa_interes, estado, etiqueta, periodo_meses, tipo_pago, fecha_inicio, fecha_fin, " +
                    "observacion, creado_en) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setLong(1, prestamo.getIdCliente());
            stmt.setLong(2, prestamo.getIdAsesor());
            stmt.setBigDecimal(3, prestamo.getMontoSolicitado());
            stmt.setBigDecimal(4, prestamo.getMontoDesembolsado());
            stmt.setBigDecimal(5, prestamo.getTasaInteres());
            stmt.setString(6, prestamo.getEstado().name().toLowerCase());
            stmt.setString(7, prestamo.getEtiqueta().name().toLowerCase());
            stmt.setInt(8, prestamo.getPeriodoMeses());
            stmt.setString(9, prestamo.getTipoPago().name().toLowerCase());
            stmt.setDate(10, prestamo.getFechaInicio() != null ? Date.valueOf(prestamo.getFechaInicio()) : null);
            stmt.setDate(11, prestamo.getFechaFin() != null ? Date.valueOf(prestamo.getFechaFin()) : null);
            stmt.setString(12, prestamo.getObservacion());
            stmt.setTimestamp(13, DateTimeUtil.nowAsTimestamp());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        prestamo.setIdPrestamo(generatedKeys.getLong(1));
                    }
                }
                logger.info("Préstamo creado exitosamente: " + prestamo.getIdPrestamo());
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al crear préstamo", e);
        }
        
        return false;
    }
    
    @Override
    public boolean update(Prestamo prestamo) {
        String sql = "UPDATE prestamos SET monto_solicitado = ?, monto_desembolsado = ?, tasa_interes = ?, " +
                    "estado = ?, etiqueta = ?, periodo_meses = ?, tipo_pago = ?, fecha_inicio = ?, " +
                    "fecha_fin = ?, observacion = ? WHERE id_prestamo = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBigDecimal(1, prestamo.getMontoSolicitado());
            stmt.setBigDecimal(2, prestamo.getMontoDesembolsado());
            stmt.setBigDecimal(3, prestamo.getTasaInteres());
            stmt.setString(4, prestamo.getEstado().name().toLowerCase());
            stmt.setString(5, prestamo.getEtiqueta().name().toLowerCase());
            stmt.setInt(6, prestamo.getPeriodoMeses());
            stmt.setString(7, prestamo.getTipoPago().name().toLowerCase());
            stmt.setDate(8, prestamo.getFechaInicio() != null ? Date.valueOf(prestamo.getFechaInicio()) : null);
            stmt.setDate(9, prestamo.getFechaFin() != null ? Date.valueOf(prestamo.getFechaFin()) : null);
            stmt.setString(10, prestamo.getObservacion());
            stmt.setLong(11, prestamo.getIdPrestamo());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Préstamo actualizado exitosamente: " + prestamo.getIdPrestamo());
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al actualizar préstamo: " + prestamo.getIdPrestamo(), e);
        }
        
        return false;
    }
    
    @Override
    public boolean updateEstado(Long idPrestamo, Prestamo.EstadoPrestamo nuevoEstado) {
        String sql = "UPDATE prestamos SET estado = ? WHERE id_prestamo = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, nuevoEstado.name().toLowerCase());
            stmt.setLong(2, idPrestamo);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Estado de préstamo actualizado: " + idPrestamo + " -> " + nuevoEstado);
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al actualizar estado del préstamo: " + idPrestamo, e);
        }
        
        return false;
    }
    
    @Override
    public boolean updateEtiqueta(Long idPrestamo, Prestamo.EtiquetaPrestamo etiqueta) {
        String sql = "UPDATE prestamos SET etiqueta = ? WHERE id_prestamo = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, etiqueta.name().toLowerCase());
            stmt.setLong(2, idPrestamo);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Etiqueta de préstamo actualizada: " + idPrestamo + " -> " + etiqueta);
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al actualizar etiqueta del préstamo: " + idPrestamo, e);
        }
        
        return false;
    }
    
    @Override
    public boolean exists(Long idPrestamo) {
        String sql = "SELECT COUNT(*) FROM prestamos WHERE id_prestamo = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idPrestamo);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al verificar existencia de préstamo: " + idPrestamo, e);
        }
        
        return false;
    }
    
    @Override
    public List<Prestamo> findProximosAVencer(int dias) {
        String sql = "SELECT p.*, c.nombre as cliente_nombre, c.apellido as cliente_apellido, " +
                    "a.nombre as asesor_nombre, a.apellido as asesor_apellido " +
                    "FROM prestamos p " +
                    "JOIN clientes c ON p.id_cliente = c.id_cliente " +
                    "JOIN asesores a ON p.id_asesor = a.id_asesor " +
                    "WHERE p.estado = 'activo' AND p.fecha_fin <= DATE_ADD(CURDATE(), INTERVAL ? DAY) " +
                    "ORDER BY p.fecha_fin ASC";
        
        List<Prestamo> prestamos = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, dias);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prestamos.add(mapResultSetToPrestamo(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar préstamos próximos a vencer", e);
        }
        
        return prestamos;
    }
    
    @Override
    public List<Prestamo> findVencidos() {
        String sql = "SELECT p.*, c.nombre as cliente_nombre, c.apellido as cliente_apellido, " +
                    "a.nombre as asesor_nombre, a.apellido as asesor_apellido " +
                    "FROM prestamos p " +
                    "JOIN clientes c ON p.id_cliente = c.id_cliente " +
                    "JOIN asesores a ON p.id_asesor = a.id_asesor " +
                    "WHERE p.estado = 'activo' AND p.fecha_fin < CURDATE() " +
                    "ORDER BY p.fecha_fin ASC";
        
        List<Prestamo> prestamos = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                prestamos.add(mapResultSetToPrestamo(rs));
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar préstamos vencidos", e);
        }
        
        return prestamos;
    }
    
    @Override
    public boolean hasActiveLoans(Long idCliente) {
        String sql = "SELECT COUNT(*) FROM prestamos WHERE id_cliente = ? AND estado = 'activo'";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idCliente);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al verificar préstamos activos del cliente: " + idCliente, e);
        }
        
        return false;
    }
    
    @Override
    public boolean hasPendingLoans(Long idCliente) {
        String sql = "SELECT COUNT(*) FROM prestamos WHERE id_cliente = ? AND estado = 'pendiente'";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idCliente);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al verificar préstamos pendientes del cliente: " + idCliente, e);
        }
        
        return false;
    }
    
    @Override
    public Optional<Prestamo> findLastByCliente(Long idCliente) {
        String sql = "SELECT p.*, c.nombre as cliente_nombre, c.apellido as cliente_apellido, " +
                    "a.nombre as asesor_nombre, a.apellido as asesor_apellido " +
                    "FROM prestamos p " +
                    "JOIN clientes c ON p.id_cliente = c.id_cliente " +
                    "JOIN asesores a ON p.id_asesor = a.id_asesor " +
                    "WHERE p.id_cliente = ? " +
                    "ORDER BY p.creado_en DESC LIMIT 1";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idCliente);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPrestamo(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar último préstamo del cliente: " + idCliente, e);
        }
        
        return Optional.empty();
    }

    @Override
    public List<Prestamo> findByClienteAndEstado(Long idCliente, Prestamo.EstadoPrestamo estado) {
        String sql = "SELECT * FROM prestamos WHERE id_cliente = ? AND estado = ? ORDER BY creado_en DESC";
        List<Prestamo> prestamos = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idCliente);
            stmt.setString(2, estado.name().toLowerCase());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prestamos.add(mapResultSetToPrestamo(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error al buscar préstamos por cliente y estado: " + idCliente + ", " + estado, e);
        }

        return prestamos;
    }
    
    /**
     * Mapea un ResultSet a un objeto Préstamo
     */
    private Prestamo mapResultSetToPrestamo(ResultSet rs) throws SQLException {
        Prestamo prestamo = new Prestamo();
        prestamo.setIdPrestamo(rs.getLong("id_prestamo"));
        prestamo.setIdCliente(rs.getLong("id_cliente"));
        prestamo.setIdAsesor(rs.getLong("id_asesor"));
        prestamo.setMontoSolicitado(rs.getBigDecimal("monto_solicitado"));
        prestamo.setMontoDesembolsado(rs.getBigDecimal("monto_desembolsado"));
        prestamo.setTasaInteres(rs.getBigDecimal("tasa_interes"));
        prestamo.setEstado(Prestamo.EstadoPrestamo.valueOf(rs.getString("estado").toUpperCase()));
        prestamo.setEtiqueta(Prestamo.EtiquetaPrestamo.valueOf(rs.getString("etiqueta").toUpperCase()));
        prestamo.setPeriodoMeses(rs.getInt("periodo_meses"));
        prestamo.setTipoPago(Prestamo.TipoPago.valueOf(rs.getString("tipo_pago").toUpperCase()));
        
        Date fechaInicio = rs.getDate("fecha_inicio");
        if (fechaInicio != null) {
            prestamo.setFechaInicio(fechaInicio.toLocalDate());
        }
        
        Date fechaFin = rs.getDate("fecha_fin");
        if (fechaFin != null) {
            prestamo.setFechaFin(fechaFin.toLocalDate());
        }
        
        prestamo.setObservacion(rs.getString("observacion"));
        
        Timestamp creadoEn = rs.getTimestamp("creado_en");
        if (creadoEn != null) {
            prestamo.setCreadoEn(creadoEn.toLocalDateTime());
        }
        
        // Crear el cliente
        Cliente cliente = new Cliente();
        cliente.setIdCliente(rs.getLong("id_cliente"));
        cliente.setNombre(rs.getString("cliente_nombre"));
        cliente.setApellido(rs.getString("cliente_apellido"));
        prestamo.setCliente(cliente);
        
        // Crear el asesor
        Asesor asesor = new Asesor();
        asesor.setIdAsesor(rs.getLong("id_asesor"));
        asesor.setNombre(rs.getString("asesor_nombre"));
        asesor.setApellido(rs.getString("asesor_apellido"));
        prestamo.setAsesor(asesor);
        
        return prestamo;
    }
}
