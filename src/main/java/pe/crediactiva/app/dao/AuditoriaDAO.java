package pe.crediactiva.app.dao;

import pe.crediactiva.app.model.Auditoria;
import java.util.List;

/**
 * Interfaz DAO para la entidad Auditoria
 */
public interface AuditoriaDAO {
    
    /**
     * Crea un nuevo registro de auditoría
     */
    boolean create(Auditoria auditoria);
    
    /**
     * Obtiene todas las auditorías
     */
    List<Auditoria> findAll();
    
    /**
     * Obtiene auditorías por tabla
     */
    List<Auditoria> findByTabla(String tabla);
    
    /**
     * Obtiene auditorías por usuario
     */
    List<Auditoria> findByUsuario(Long idUsuario);
    
    /**
     * Obtiene auditorías por registro específico
     */
    List<Auditoria> findByRegistro(String tabla, String idRegistro);
    
    /**
     * Obtiene auditorías por tipo de acción
     */
    List<Auditoria> findByAccion(Auditoria.TipoAccion accion);
    
    /**
     * Obtiene auditorías por rango de fechas
     */
    List<Auditoria> findByFechaRange(java.time.LocalDateTime fechaInicio, java.time.LocalDateTime fechaFin);
}
