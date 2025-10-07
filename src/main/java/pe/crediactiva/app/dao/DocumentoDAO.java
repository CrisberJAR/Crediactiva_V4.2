package pe.crediactiva.app.dao;

import pe.crediactiva.app.model.Documento;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO para la entidad Documento
 */
public interface DocumentoDAO {

    /**
     * Busca un documento por su ID
     */
    Optional<Documento> findById(Long idDocumento);

    /**
     * Crea un nuevo documento
     */
    boolean create(Documento documento);

    /**
     * Actualiza un documento existente
     */
    boolean update(Documento documento);

    /**
     * Elimina un documento
     */
    boolean delete(Long idDocumento);

    /**
     * Obtiene todos los documentos
     */
    List<Documento> findAll();

    /**
     * Obtiene documentos por préstamo
     */
    List<Documento> findByPrestamo(Long idPrestamo);

    /**
     * Obtiene documentos por tipo
     */
    List<Documento> findByTipo(String tipo);

    /**
     * Obtiene documentos por préstamo y tipo
     */
    List<Documento> findByPrestamoAndTipo(Long idPrestamo, String tipo);

    /**
     * Verifica si existe un documento con el ID dado
     */
    boolean exists(Long idDocumento);
}
