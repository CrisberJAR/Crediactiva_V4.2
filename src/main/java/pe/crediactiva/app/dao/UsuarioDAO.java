package pe.crediactiva.app.dao;

import pe.crediactiva.app.model.Usuario;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO para la entidad Usuario
 */
public interface UsuarioDAO {
    
    /**
     * Busca un usuario por su ID
     */
    Optional<Usuario> findById(Long idUsuario);
    
    /**
     * Busca un usuario por su ID y contraseña para autenticación
     */
    Optional<Usuario> authenticate(Long idUsuario, String password);
    
    /**
     * Obtiene todos los usuarios
     */
    List<Usuario> findAll();
    
    /**
     * Obtiene usuarios por rol
     */
    List<Usuario> findByRol(int idRol);
    
    /**
     * Crea un nuevo usuario
     */
    boolean create(Usuario usuario);
    
    /**
     * Actualiza un usuario existente
     */
    boolean update(Usuario usuario);
    
    /**
     * Elimina un usuario (soft delete)
     */
    boolean delete(Long idUsuario);
    
    /**
     * Verifica si existe un usuario con el ID dado
     */
    boolean exists(Long idUsuario);
    
    /**
     * Cambia la contraseña de un usuario
     */
    boolean changePassword(Long idUsuario, String newPasswordHash);
    
    /**
     * Activa/desactiva un usuario
     */
    boolean setActive(Long idUsuario, boolean active);
}
