package pe.crediactiva.app.dao;

import pe.crediactiva.app.model.Prestamo;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO para la entidad Préstamo
 */
public interface PrestamoDAO {
    
    /**
     * Busca un préstamo por su ID
     */
    Optional<Prestamo> findById(Long idPrestamo);
    
    /**
     * Obtiene todos los préstamos
     */
    List<Prestamo> findAll();
    
    /**
     * Obtiene préstamos por cliente
     */
    List<Prestamo> findByCliente(Long idCliente);
    
    /**
     * Obtiene préstamos por asesor
     */
    List<Prestamo> findByAsesor(Long idAsesor);
    
    /**
     * Obtiene préstamos por cliente y estado
     */
    List<Prestamo> findByClienteAndEstado(Long idCliente, Prestamo.EstadoPrestamo estado);
    
    /**
     * Obtiene préstamos por estado
     */
    List<Prestamo> findByEstado(Prestamo.EstadoPrestamo estado);
    
    /**
     * Obtiene préstamos pendientes
     */
    List<Prestamo> findPendientes();
    
    /**
     * Obtiene préstamos activos
     */
    List<Prestamo> findActivos();
    
    /**
     * Obtiene préstamos por etiqueta
     */
    List<Prestamo> findByEtiqueta(Prestamo.EtiquetaPrestamo etiqueta);
    
    /**
     * Crea un nuevo préstamo
     */
    boolean create(Prestamo prestamo);
    
    /**
     * Actualiza un préstamo existente
     */
    boolean update(Prestamo prestamo);
    
    /**
     * Actualiza el estado de un préstamo
     */
    boolean updateEstado(Long idPrestamo, Prestamo.EstadoPrestamo nuevoEstado);
    
    /**
     * Actualiza la etiqueta de un préstamo
     */
    boolean updateEtiqueta(Long idPrestamo, Prestamo.EtiquetaPrestamo etiqueta);
    
    /**
     * Verifica si existe un préstamo con el ID dado
     */
    boolean exists(Long idPrestamo);
    
    /**
     * Obtiene préstamos próximos a vencer
     */
    List<Prestamo> findProximosAVencer(int dias);
    
    /**
     * Obtiene préstamos vencidos
     */
    List<Prestamo> findVencidos();
    
    /**
     * Verifica si un cliente tiene préstamos activos
     */
    boolean hasActiveLoans(Long idCliente);
    
    /**
     * Verifica si un cliente tiene préstamos pendientes
     */
    boolean hasPendingLoans(Long idCliente);
    
    /**
     * Obtiene el último préstamo de un cliente
     */
    Optional<Prestamo> findLastByCliente(Long idCliente);
}
