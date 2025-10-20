package pe.crediactiva.app.dao;

import pe.crediactiva.app.model.MovimientoCapital;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO para la entidad MovimientoCapital
 */
public interface MovimientoCapitalDAO {

    /**
     * Busca un movimiento por su ID
     */
    Optional<MovimientoCapital> findById(Long idMovimiento);

    /**
     * Crea un nuevo movimiento
     */
    boolean create(MovimientoCapital movimiento);

    /**
     * Actualiza un movimiento existente
     */
    boolean update(MovimientoCapital movimiento);

    /**
     * Elimina un movimiento
     */
    boolean delete(Long idMovimiento);

    /**
     * Obtiene todos los movimientos
     */
    List<MovimientoCapital> findAll();

    /**
     * Obtiene movimientos por cliente
     */
    List<MovimientoCapital> findByCliente(Long idCliente);

    /**
     * Obtiene movimientos por tipo
     */
    List<MovimientoCapital> findByTipo(MovimientoCapital.TipoMovimiento tipo);

    /**
     * Obtiene movimientos por fecha
     */
    List<MovimientoCapital> findByFecha(LocalDate fecha);

    /**
     * Obtiene movimientos por rango de fechas
     */
    List<MovimientoCapital> findByRangoFechas(LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Obtiene movimientos por admin
     */
    List<MovimientoCapital> findByAdmin(Long idAdmin);

    /**
     * Verifica si existe un movimiento con el ID dado
     */
    boolean exists(Long idMovimiento);
    
    /**
     * Obtiene todos los movimientos con información del cliente
     */
    List<MovimientoCapital> findAllWithCliente();
}
