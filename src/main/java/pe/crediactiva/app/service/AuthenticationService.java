package pe.crediactiva.app.service;

import pe.crediactiva.app.config.SessionManager;
import pe.crediactiva.app.dao.UsuarioDAO;
import pe.crediactiva.app.dao.impl.UsuarioDAOImpl;
import pe.crediactiva.app.model.Usuario;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * Servicio de autenticación de usuarios
 */
public class AuthenticationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    
    private final UsuarioDAO usuarioDAO;
    private final SessionManager sessionManager;
    
    public AuthenticationService() {
        this.usuarioDAO = new UsuarioDAOImpl();
        this.sessionManager = SessionManager.getInstance();
    }
    
    /**
     * Autentica un usuario con ID y contraseña
     */
    public boolean authenticate(Long idUsuario, String password) {
        if (idUsuario == null || password == null || password.trim().isEmpty()) {
            logger.warn("Intento de autenticación con datos inválidos");
            return false;
        }
        
        try {
            Optional<Usuario> usuarioOpt = usuarioDAO.authenticate(idUsuario, password);
            
            if (usuarioOpt.isPresent()) {
                Usuario usuario = usuarioOpt.get();
                sessionManager.setCurrentUser(usuario);
                logger.info("Usuario autenticado exitosamente: " + usuario.getIdUsuario() + " - " + usuario.getRol().getNombre());
                return true;
            } else {
                logger.warn("Credenciales inválidas para usuario: " + idUsuario);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Error durante la autenticación del usuario: " + idUsuario, e);
            return false;
        }
    }
    
    /**
     * Cierra la sesión del usuario actual
     */
    public void logout() {
        Usuario currentUser = sessionManager.getCurrentUser();
        if (currentUser != null) {
            logger.info("Usuario cerró sesión: " + currentUser.getIdUsuario());
        }
        sessionManager.logout();
    }
    
    /**
     * Verifica si hay un usuario autenticado
     */
    public boolean isAuthenticated() {
        return sessionManager.isAuthenticated();
    }
    
    /**
     * Obtiene el usuario actual de la sesión
     */
    public Usuario getCurrentUser() {
        return sessionManager.getCurrentUser();
    }
    
    /**
     * Verifica si el usuario actual es administrador
     */
    public boolean isAdmin() {
        return sessionManager.isAdmin();
    }
    
    /**
     * Verifica si el usuario actual es asesor
     */
    public boolean isAsesor() {
        return sessionManager.isAsesor();
    }
    
    /**
     * Verifica si el usuario actual es cliente
     */
    public boolean isCliente() {
        return sessionManager.isCliente();
    }
    
    /**
     * Verifica si el usuario tiene un permiso específico
     */
    public boolean hasPermission(String action) {
        return sessionManager.hasPermission(action);
    }
    
    /**
     * Obtiene el ID del asesor actual (si es asesor)
     */
    public Long getAsesorId() {
        return sessionManager.getAsesorId();
    }
    
    /**
     * Obtiene el ID del cliente actual (si es cliente)
     */
    public Long getClienteId() {
        return sessionManager.getClienteId();
    }
    
    /**
     * Cambia la contraseña del usuario actual
     */
    public boolean changePassword(String currentPassword, String newPassword) {
        Usuario currentUser = getCurrentUser();
        if (currentUser == null) {
            logger.warn("Intento de cambio de contraseña sin usuario autenticado");
            return false;
        }
        
        if (newPassword == null || newPassword.trim().length() < 4) {
            logger.warn("Nueva contraseña inválida");
            return false;
        }
        
        try {
            // Verificar contraseña actual
            if (!currentUser.getPasswordHash().equals(currentPassword)) {
                logger.warn("Contraseña actual incorrecta para usuario: " + currentUser.getIdUsuario());
                return false;
            }
            
            // Actualizar contraseña
            boolean success = usuarioDAO.changePassword(currentUser.getIdUsuario(), newPassword);
            
            if (success) {
                logger.info("Contraseña actualizada exitosamente para usuario: " + currentUser.getIdUsuario());
                // Actualizar el usuario en sesión
                currentUser.setPasswordHash(newPassword);
            }
            
            return success;
            
        } catch (Exception e) {
            logger.error("Error al cambiar contraseña del usuario: " + currentUser.getIdUsuario(), e);
            return false;
        }
    }
}
