package pe.crediactiva.app.dao;

import pe.crediactiva.app.model.Cronograma;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO para la entidad Cronograma
 */
public interface CronogramaDAO {
    
    /**
     * Busca una cuota por su ID
     */
    Optional<Cronograma> findById(Long idCuota);
    
    /**
     * Obtiene todas las cuotas de un préstamo
     */
    List<Cronograma> findByPrestamo(Long idPrestamo);
    
    /**
     * Obtiene cuotas por estado
     */
    List<Cronograma> findByEstado(Cronograma.EstadoCuota estado);
    
    /**
     * Obtiene cuotas vencidas
     */
    List<Cronograma> findVencidas();
    
    /**
     * Obtiene cuotas próximas a vencer
     */
    List<Cronograma> findProximasAVencer(int dias);
    
    /**
     * Obtiene cuotas pendientes de un préstamo
     */
    List<Cronograma> findPendientesByPrestamo(Long idPrestamo);
    
    /**
     * Crea una nueva cuota
     */
    boolean create(Cronograma cronograma);
    
    /**
     * Actualiza una cuota existente
     */
    boolean update(Cronograma cronograma);
    
    /**
     * Actualiza el estado de una cuota
     */
    boolean updateEstado(Long idCuota, Cronograma.EstadoCuota nuevoEstado);
    
    /**
     * Marca una cuota como pagada
     */
    boolean marcarComoPagada(Long idCuota, java.time.LocalDate fechaPago);
    
    /**
     * Obtiene la próxima cuota pendiente de un préstamo
     */
    Optional<Cronograma> findProximaCuota(Long idPrestamo);
    
    /**
     * Obtiene cuotas pagadas de un préstamo
     */
    List<Cronograma> findPagadasByPrestamo(Long idPrestamo);
    
    /**
     * Verifica si todas las cuotas de un préstamo están pagadas
     */
    boolean areAllCuotasPagadas(Long idPrestamo);
    
    /**
     * Obtiene cuotas por fecha
     */
    List<Cronograma> findByFecha(LocalDate fecha);
    
    /**
     * Obtiene cuotas pendientes por cliente
     */
    List<Cronograma> findPendientesByCliente(Long idCliente);
    
    /**
     * Obtiene cuotas vencidas por cliente
     */
    List<Cronograma> findVencidasByCliente(Long idCliente);
    
    /**
     * Obtiene cuotas por préstamo (alias de findByPrestamo)
     */
    List<Cronograma> findByPrestamoId(Long idPrestamo);
}
