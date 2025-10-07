package pe.crediactiva.app.service;

import pe.crediactiva.app.config.SessionManager;
import pe.crediactiva.app.dao.AuditoriaDAO;
import pe.crediactiva.app.dao.impl.AuditoriaDAOImpl;
import pe.crediactiva.app.model.Auditoria;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Servicio de auditoría del sistema
 */
public class AuditoriaService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuditoriaService.class);
    
    private final AuditoriaDAO auditoriaDAO;
    private final SessionManager sessionManager;
    
    public AuditoriaService() {
        this.auditoriaDAO = new AuditoriaDAOImpl();
        this.sessionManager = SessionManager.getInstance();
    }
    
    /**
     * Registra una acción de auditoría
     */
    public void registrarAuditoria(String tablaAfectada, String idRegistroAfectado, 
                                   String accion, String valorAnterior, String valorNuevo) {
        try {
            Long idUsuario = sessionManager.getCurrentUser() != null ? 
                sessionManager.getCurrentUser().getIdUsuario() : null;
            
            if (idUsuario == null) {
                logger.warn("No se puede registrar auditoría sin usuario autenticado");
                return;
            }
            
            Auditoria.TipoAccion tipoAccion = Auditoria.TipoAccion.valueOf(accion.toUpperCase());
            
            Auditoria auditoria = new Auditoria(idUsuario, tablaAfectada, idRegistroAfectado, tipoAccion);
            auditoria.setValorAnterior(valorAnterior);
            auditoria.setValorNuevo(valorNuevo);
            
            boolean success = auditoriaDAO.create(auditoria);
            
            if (success) {
                logger.debug("Auditoría registrada: " + accion + " en " + tablaAfectada + " - " + idRegistroAfectado);
            } else {
                logger.error("Error al registrar auditoría");
            }
            
        } catch (Exception e) {
            logger.error("Error al registrar auditoría", e);
        }
    }
    
    /**
     * Obtiene el historial de auditoría por tabla
     */
    public List<Auditoria> obtenerAuditoriaPorTabla(String tabla) {
        try {
            return auditoriaDAO.findByTabla(tabla);
        } catch (Exception e) {
            logger.error("Error al obtener auditoría por tabla: " + tabla, e);
            return List.of();
        }
    }
    
    /**
     * Obtiene el historial de auditoría por usuario
     */
    public List<Auditoria> obtenerAuditoriaPorUsuario(Long idUsuario) {
        try {
            return auditoriaDAO.findByUsuario(idUsuario);
        } catch (Exception e) {
            logger.error("Error al obtener auditoría por usuario: " + idUsuario, e);
            return List.of();
        }
    }
    
    /**
     * Obtiene el historial de auditoría por registro específico
     */
    public List<Auditoria> obtenerAuditoriaPorRegistro(String tabla, String idRegistro) {
        try {
            return auditoriaDAO.findByRegistro(tabla, idRegistro);
        } catch (Exception e) {
            logger.error("Error al obtener auditoría por registro: " + tabla + " - " + idRegistro, e);
            return List.of();
        }
    }
    
    /**
     * Obtiene todas las auditorías
     */
    public List<Auditoria> obtenerTodasLasAuditorias() {
        try {
            return auditoriaDAO.findAll();
        } catch (Exception e) {
            logger.error("Error al obtener todas las auditorías", e);
            return List.of();
        }
    }
}
