package pe.crediactiva.app.dao;

import pe.crediactiva.app.model.RecaudacionAsesor;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO para la entidad RecaudacionAsesor
 */
public interface RecaudacionAsesorDAO {

    /**
     * Busca una recaudación por su ID
     */
    Optional<RecaudacionAsesor> findById(Long idRecaudacion);

    /**
     * Crea una nueva recaudación
     */
    boolean create(RecaudacionAsesor recaudacion);

    /**
     * Actualiza una recaudación existente
     */
    boolean update(RecaudacionAsesor recaudacion);

    /**
     * Elimina una recaudación
     */
    boolean delete(Long idRecaudacion);

    /**
     * Obtiene todas las recaudaciones
     */
    List<RecaudacionAsesor> findAll();

    /**
     * Obtiene recaudaciones por asesor
     */
    List<RecaudacionAsesor> findByAsesor(Long idAsesor);

    /**
     * Obtiene recaudaciones por cliente
     */
    List<RecaudacionAsesor> findByCliente(Long idCliente);

    /**
     * Obtiene recaudaciones por préstamo
     */
    List<RecaudacionAsesor> findByPrestamo(Long idPrestamo);

    /**
     * Obtiene recaudaciones por fecha
     */
    List<RecaudacionAsesor> findByFecha(LocalDate fecha);

    /**
     * Obtiene recaudaciones pendientes de validación
     */
    List<RecaudacionAsesor> findPendientes();

    /**
     * Marca una recaudación como validada
     */
    boolean marcarComoValidado(Long idRecaudacion);
    
    /**
     * Verifica si existe un borrador pendiente para un préstamo específico
     */
    boolean existeBorradorPendiente(Long idPrestamo);
    
    /**
     * Obtiene el borrador pendiente para un préstamo específico
     */
    Optional<RecaudacionAsesor> obtenerBorradorPendiente(Long idPrestamo);
    
    /**
     * Verifica si existe una recaudación pendiente para una cuota específica
     */
    boolean existeRecaudacionPendienteParaCuota(Long idPrestamo, Long idCuota);
}