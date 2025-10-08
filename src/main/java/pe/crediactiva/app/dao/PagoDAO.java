package pe.crediactiva.app.dao;

import pe.crediactiva.app.model.Pago;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO para la entidad Pago
 */
public interface PagoDAO {

    /**
     * Busca un pago por su ID
     */
    Optional<Pago> findById(Long idPago);

    /**
     * Crea un nuevo pago
     */
    boolean create(Pago pago);

    /**
     * Actualiza un pago existente
     */
    boolean update(Pago pago);

    /**
     * Elimina un pago
     */
    boolean delete(Long idPago);

    /**
     * Obtiene todos los pagos
     */
    List<Pago> findAll();

    /**
     * Obtiene pagos por cuota
     */
    List<Pago> findByCuota(Long idCuota);

    /**
     * Obtiene pagos por cliente
     */
    List<Pago> findByCliente(Long idCliente);

    /**
     * Obtiene pagos por asesor
     */
    List<Pago> findByAsesor(Long idAsesor);

    /**
     * Obtiene pagos por fecha
     */
    List<Pago> findByFecha(LocalDate fecha);

    /**
     * Obtiene pagos por cliente y rango de fechas
     */
    List<Pago> findByClienteAndFecha(Long idCliente, LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Obtiene pagos por asesor y rango de fechas
     */
    List<Pago> findByAsesorAndFecha(Long idAsesor, LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Obtiene pagos por préstamo
     */
    List<Pago> findByPrestamo(Long idPrestamo);
    
    /**
     * Obtiene pagos pendientes de validación
     */
    List<Pago> findPendientesValidacion();
}
