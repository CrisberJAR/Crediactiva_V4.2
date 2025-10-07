package pe.crediactiva.app.dao;

import pe.crediactiva.app.model.Asesor;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO para la entidad Asesor
 */
public interface AsesorDAO {

    /**
     * Busca un asesor por su ID
     */
    Optional<Asesor> findById(Long idAsesor);

    /**
     * Obtiene todos los asesores
     */
    List<Asesor> findAll();

    /**
     * Obtiene asesores activos
     */
    List<Asesor> findActivos();

    /**
     * Busca asesores por nombre o apellido
     */
    List<Asesor> searchByName(String searchTerm);

    /**
     * Crea un nuevo asesor
     */
    boolean create(Asesor asesor);

    /**
     * Actualiza un asesor existente
     */
    boolean update(Asesor asesor);

    /**
     * Elimina un asesor (soft delete)
     */
    boolean delete(Long idAsesor);

    /**
     * Verifica si existe un asesor con el ID dado
     */
    boolean exists(Long idAsesor);
}
