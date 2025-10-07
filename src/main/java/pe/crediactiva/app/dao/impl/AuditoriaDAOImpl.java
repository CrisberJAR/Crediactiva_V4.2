package pe.crediactiva.app.dao.impl;

import pe.crediactiva.app.config.DatabaseConfig;
import pe.crediactiva.app.dao.AuditoriaDAO;
import pe.crediactiva.app.model.Auditoria;
import pe.crediactiva.app.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación JDBC del DAO para Auditoria
 */
public class AuditoriaDAOImpl implements AuditoriaDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditoriaDAOImpl.class);
    
    @Override
    public boolean create(Auditoria auditoria) {
        String sql = "INSERT INTO auditoria (id_usuario, tabla_afectada, id_registro_afectado, " +
                    "accion, valor_anterior, valor_nuevo, fecha) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, auditoria.getIdUsuario());
            stmt.setString(2, auditoria.getTablaAfectada());
            stmt.setString(3, auditoria.getIdRegistroAfectado());
            stmt.setString(4, auditoria.getAccion().name().toLowerCase());
            stmt.setString(5, auditoria.getValorAnterior());
            stmt.setString(6, auditoria.getValorNuevo());
            stmt.setTimestamp(7, Timestamp.valueOf(auditoria.getFecha()));
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.debug("Auditoría registrada: " + auditoria.getAccion() + " en " + auditoria.getTablaAfectada());
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al crear auditoría", e);
        }
        
        return false;
    }
    
    @Override
    public List<Auditoria> findAll() {
        String sql = "SELECT a.*, u.id_usuario as usuario_id " +
                    "FROM auditoria a " +
                    "LEFT JOIN usuarios u ON a.id_usuario = u.id_usuario " +
                    "ORDER BY a.fecha DESC";
        
        List<Auditoria> auditorias = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                auditorias.add(mapResultSetToAuditoria(rs));
            }
            
        } catch (SQLException e) {
            logger.error("Error al obtener todas las auditorías", e);
        }
        
        return auditorias;
    }
    
    @Override
    public List<Auditoria> findByTabla(String tabla) {
        String sql = "SELECT a.*, u.id_usuario as usuario_id " +
                    "FROM auditoria a " +
                    "LEFT JOIN usuarios u ON a.id_usuario = u.id_usuario " +
                    "WHERE a.tabla_afectada = ? " +
                    "ORDER BY a.fecha DESC";
        
        List<Auditoria> auditorias = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, tabla);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    auditorias.add(mapResultSetToAuditoria(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar auditorías por tabla: " + tabla, e);
        }
        
        return auditorias;
    }
    
    @Override
    public List<Auditoria> findByUsuario(Long idUsuario) {
        String sql = "SELECT a.*, u.id_usuario as usuario_id " +
                    "FROM auditoria a " +
                    "LEFT JOIN usuarios u ON a.id_usuario = u.id_usuario " +
                    "WHERE a.id_usuario = ? " +
                    "ORDER BY a.fecha DESC";
        
        List<Auditoria> auditorias = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idUsuario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    auditorias.add(mapResultSetToAuditoria(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar auditorías por usuario: " + idUsuario, e);
        }
        
        return auditorias;
    }
    
    @Override
    public List<Auditoria> findByRegistro(String tabla, String idRegistro) {
        String sql = "SELECT a.*, u.id_usuario as usuario_id " +
                    "FROM auditoria a " +
                    "LEFT JOIN usuarios u ON a.id_usuario = u.id_usuario " +
                    "WHERE a.tabla_afectada = ? AND a.id_registro_afectado = ? " +
                    "ORDER BY a.fecha DESC";
        
        List<Auditoria> auditorias = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, tabla);
            stmt.setString(2, idRegistro);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    auditorias.add(mapResultSetToAuditoria(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar auditorías por registro: " + tabla + " - " + idRegistro, e);
        }
        
        return auditorias;
    }
    
    @Override
    public List<Auditoria> findByAccion(Auditoria.TipoAccion accion) {
        String sql = "SELECT a.*, u.id_usuario as usuario_id " +
                    "FROM auditoria a " +
                    "LEFT JOIN usuarios u ON a.id_usuario = u.id_usuario " +
                    "WHERE a.accion = ? " +
                    "ORDER BY a.fecha DESC";
        
        List<Auditoria> auditorias = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, accion.name().toLowerCase());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    auditorias.add(mapResultSetToAuditoria(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar auditorías por acción: " + accion, e);
        }
        
        return auditorias;
    }
    
    @Override
    public List<Auditoria> findByFechaRange(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        String sql = "SELECT a.*, u.id_usuario as usuario_id " +
                    "FROM auditoria a " +
                    "LEFT JOIN usuarios u ON a.id_usuario = u.id_usuario " +
                    "WHERE a.fecha BETWEEN ? AND ? " +
                    "ORDER BY a.fecha DESC";
        
        List<Auditoria> auditorias = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(fechaInicio));
            stmt.setTimestamp(2, Timestamp.valueOf(fechaFin));
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    auditorias.add(mapResultSetToAuditoria(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar auditorías por rango de fechas", e);
        }
        
        return auditorias;
    }
    
    /**
     * Mapea un ResultSet a un objeto Auditoria
     */
    private Auditoria mapResultSetToAuditoria(ResultSet rs) throws SQLException {
        Auditoria auditoria = new Auditoria();
        auditoria.setIdAuditoria(rs.getLong("id_auditoria"));
        auditoria.setIdUsuario(rs.getLong("id_usuario"));
        auditoria.setTablaAfectada(rs.getString("tabla_afectada"));
        auditoria.setIdRegistroAfectado(rs.getString("id_registro_afectado"));
        auditoria.setAccion(Auditoria.TipoAccion.valueOf(rs.getString("accion").toUpperCase()));
        auditoria.setValorAnterior(rs.getString("valor_anterior"));
        auditoria.setValorNuevo(rs.getString("valor_nuevo"));
        
        Timestamp fecha = rs.getTimestamp("fecha");
        if (fecha != null) {
            auditoria.setFecha(fecha.toLocalDateTime());
        }
        
        // Crear el usuario si existe
        Long usuarioId = rs.getLong("usuario_id");
        if (usuarioId > 0) {
            Usuario usuario = new Usuario();
            usuario.setIdUsuario(usuarioId);
            auditoria.setUsuario(usuario);
        }
        
        return auditoria;
    }
}
