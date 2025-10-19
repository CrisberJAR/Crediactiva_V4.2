package pe.crediactiva.app.dao.impl;

import pe.crediactiva.app.config.DatabaseConfig;
import pe.crediactiva.app.dao.UsuarioDAO;
import pe.crediactiva.app.model.Rol;
import pe.crediactiva.app.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC del DAO para Usuario
 */
public class UsuarioDAOImpl implements UsuarioDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(UsuarioDAOImpl.class);
    
    @Override
    public Optional<Usuario> findById(Long idUsuario) {
        String sql = "SELECT u.*, r.nombre as rol_nombre " +
                    "FROM usuarios u " +
                    "JOIN roles r ON u.id_rol = r.id_rol " +
                    "WHERE u.id_usuario = ? AND u.activo = true";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idUsuario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToUsuario(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar usuario por ID: " + idUsuario, e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public Optional<Usuario> authenticate(Long idUsuario, String password) {
        String sql = "SELECT u.*, r.nombre as rol_nombre " +
                    "FROM usuarios u " +
                    "JOIN roles r ON u.id_rol = r.id_rol " +
                    "WHERE u.id_usuario = ? AND u.password_hash = ? AND u.activo = true";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idUsuario);
            stmt.setString(2, password);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    logger.info("Usuario autenticado exitosamente: " + idUsuario);
                    return Optional.of(mapResultSetToUsuario(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error en autenticación de usuario: " + idUsuario, e);
        }
        
        logger.warn("Intento de autenticación fallido para usuario: " + idUsuario);
        return Optional.empty();
    }
    
    @Override
    public List<Usuario> findAll() {
        String sql = "SELECT u.*, r.nombre as rol_nombre " +
                    "FROM usuarios u " +
                    "JOIN roles r ON u.id_rol = r.id_rol " +
                    "WHERE u.activo = true " +
                    "ORDER BY u.id_usuario";
        
        List<Usuario> usuarios = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                usuarios.add(mapResultSetToUsuario(rs));
            }
            
        } catch (SQLException e) {
            logger.error("Error al obtener todos los usuarios", e);
        }
        
        return usuarios;
    }
    
    @Override
    public List<Usuario> findByRol(int idRol) {
        String sql = "SELECT u.*, r.nombre as rol_nombre " +
                    "FROM usuarios u " +
                    "JOIN roles r ON u.id_rol = r.id_rol " +
                    "WHERE u.id_rol = ? AND u.activo = true " +
                    "ORDER BY u.id_usuario";
        
        List<Usuario> usuarios = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, idRol);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    usuarios.add(mapResultSetToUsuario(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar usuarios por rol: " + idRol, e);
        }
        
        return usuarios;
    }
    
    @Override
    public boolean create(Usuario usuario) {
        String sql = "INSERT INTO usuarios (id_usuario, password_hash, id_rol, creado_en, activo) " +
                    "VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, usuario.getIdUsuario());
            stmt.setString(2, usuario.getPasswordHash());
            stmt.setInt(3, usuario.getIdRol());
            
            // Usar la fecha del usuario si está disponible, sino usar la fecha actual
            LocalDateTime fechaCreacion = usuario.getCreadoEn() != null ? 
                usuario.getCreadoEn() : LocalDateTime.now();
            stmt.setTimestamp(4, Timestamp.valueOf(fechaCreacion));
            
            stmt.setBoolean(5, usuario.isActivo());
            
            logger.info("Creando usuario - ID: " + usuario.getIdUsuario() + 
                       ", Rol: " + usuario.getIdRol() + 
                       ", Activo: " + usuario.isActivo() + 
                       ", Fecha: " + fechaCreacion);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Usuario creado exitosamente: " + usuario.getIdUsuario());
                return true;
            } else {
                logger.warn("No se insertó ningún registro para usuario: " + usuario.getIdUsuario());
            }
            
        } catch (SQLException e) {
            logger.error("Error al crear usuario: " + usuario.getIdUsuario(), e);
        }
        
        return false;
    }
    
    @Override
    public boolean update(Usuario usuario) {
        String sql = "UPDATE usuarios SET password_hash = ?, id_rol = ?, activo = ? " +
                    "WHERE id_usuario = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, usuario.getPasswordHash());
            stmt.setInt(2, usuario.getIdRol());
            stmt.setBoolean(3, usuario.isActivo());
            stmt.setLong(4, usuario.getIdUsuario());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Usuario actualizado exitosamente: " + usuario.getIdUsuario());
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al actualizar usuario: " + usuario.getIdUsuario(), e);
        }
        
        return false;
    }
    
    @Override
    public boolean delete(Long idUsuario) {
        // Soft delete
        String sql = "UPDATE usuarios SET activo = false WHERE id_usuario = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idUsuario);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Usuario eliminado (soft delete): " + idUsuario);
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al eliminar usuario: " + idUsuario, e);
        }
        
        return false;
    }
    
    @Override
    public boolean exists(Long idUsuario) {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE id_usuario = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idUsuario);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al verificar existencia de usuario: " + idUsuario, e);
        }
        
        return false;
    }
    
    @Override
    public boolean changePassword(Long idUsuario, String newPasswordHash) {
        String sql = "UPDATE usuarios SET password_hash = ? WHERE id_usuario = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, newPasswordHash);
            stmt.setLong(2, idUsuario);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Contraseña actualizada para usuario: " + idUsuario);
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al cambiar contraseña de usuario: " + idUsuario, e);
        }
        
        return false;
    }
    
    @Override
    public boolean setActive(Long idUsuario, boolean active) {
        String sql = "UPDATE usuarios SET activo = ? WHERE id_usuario = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBoolean(1, active);
            stmt.setLong(2, idUsuario);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Estado de usuario actualizado: " + idUsuario + " -> " + (active ? "activo" : "inactivo"));
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al cambiar estado de usuario: " + idUsuario, e);
        }
        
        return false;
    }
    
    /**
     * Mapea un ResultSet a un objeto Usuario
     */
    private Usuario mapResultSetToUsuario(ResultSet rs) throws SQLException {
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(rs.getLong("id_usuario"));
        usuario.setPasswordHash(rs.getString("password_hash"));
        usuario.setIdRol(rs.getInt("id_rol"));
        usuario.setCreadoEn(rs.getTimestamp("creado_en").toLocalDateTime());
        usuario.setActivo(rs.getBoolean("activo"));
        
        // Crear el rol
        Rol rol = new Rol();
        rol.setIdRol(rs.getInt("id_rol"));
        rol.setNombre(rs.getString("rol_nombre"));
        usuario.setRol(rol);
        
        return usuario;
    }
}
