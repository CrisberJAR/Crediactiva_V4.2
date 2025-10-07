package pe.crediactiva.app.config;

import pe.crediactiva.app.model.Usuario;

/**
 * Gestor de sesión del usuario autenticado
 */
public class SessionManager {
    
    private static SessionManager instance;
    private Usuario currentUser;
    
    private SessionManager() {}
    
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }
    
    /**
     * Establece el usuario de la sesión actual
     */
    public void setCurrentUser(Usuario usuario) {
        this.currentUser = usuario;
    }
    
    /**
     * Obtiene el usuario de la sesión actual
     */
    public Usuario getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Verifica si hay un usuario autenticado
     */
    public boolean isAuthenticated() {
        return currentUser != null;
    }
    
    /**
     * Verifica si el usuario actual es administrador
     */
    public boolean isAdmin() {
        return currentUser != null && currentUser.getIdRol() == 1;
    }
    
    /**
     * Verifica si el usuario actual es asesor
     */
    public boolean isAsesor() {
        return currentUser != null && currentUser.getIdRol() == 2;
    }
    
    /**
     * Verifica si el usuario actual es cliente
     */
    public boolean isCliente() {
        return currentUser != null && currentUser.getIdRol() == 3;
    }
    
    /**
     * Obtiene el ID del asesor actual (si es asesor)
     */
    public Long getAsesorId() {
        return currentUser != null && isAsesor() ? currentUser.getIdUsuario() : null;
    }
    
    /**
     * Obtiene el ID del cliente actual (si es cliente)
     */
    public Long getClienteId() {
        return currentUser != null && isCliente() ? currentUser.getIdUsuario() : null;
    }
    
    /**
     * Cierra la sesión actual
     */
    public void logout() {
        this.currentUser = null;
    }
    
    /**
     * Verifica si el usuario tiene permiso para realizar una acción
     */
    public boolean hasPermission(String action) {
        if (currentUser == null) {
            return false;
        }
        
        switch (action) {
            case "crear_cliente":
            case "crear_solicitud":
                return isAdmin() || isAsesor();
                
            case "aprobar_prestamo":
            case "validar_cobros":
            case "gestionar_usuarios":
            case "cambiar_etiquetas":
                return isAdmin();
                
            case "registrar_cobro":
                return isAsesor();
                
            default:
                return true; // Acciones básicas permitidas para todos
        }
    }
}
