package pe.crediactiva.app.service;

import pe.crediactiva.app.dao.AsesorDAO;
import pe.crediactiva.app.dao.impl.AsesorDAOImpl;
import pe.crediactiva.app.model.Asesor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de asesores
 */
public class AsesorService {

    private static final Logger logger = LoggerFactory.getLogger(AsesorService.class);

    private final AsesorDAO asesorDAO;
    private final AuditoriaService auditoriaService;

    public AsesorService() {
        this.asesorDAO = new AsesorDAOImpl();
        this.auditoriaService = new AuditoriaService();
    }

    /**
     * Crea un nuevo asesor
     */
    public boolean crearAsesor(Asesor asesor) {
        try {
            if (asesorDAO.exists(asesor.getIdAsesor())) {
                logger.warn("Asesor con ID " + asesor.getIdAsesor() + " ya existe.");
                return false;
            }
            boolean success = asesorDAO.create(asesor);
            if (success) {
                auditoriaService.registrarAuditoria("asesores", asesor.getIdAsesor().toString(), "INSERT", null, asesor.toString());
            }
            return success;
        } catch (Exception e) {
            logger.error("Error al crear asesor: " + asesor.getIdAsesor(), e);
            return false;
        }
    }

    /**
     * Actualiza un asesor existente
     */
    public boolean actualizarAsesor(Asesor asesor) {
        try {
            Optional<Asesor> oldAsesorOpt = asesorDAO.findById(asesor.getIdAsesor());
            if (!oldAsesorOpt.isPresent()) {
                logger.warn("Asesor con ID " + asesor.getIdAsesor() + " no encontrado para actualizar.");
                return false;
            }
            Asesor oldAsesor = oldAsesorOpt.get();
            boolean success = asesorDAO.update(asesor);
            if (success) {
                auditoriaService.registrarAuditoria("asesores", asesor.getIdAsesor().toString(), "UPDATE", oldAsesor.toString(), asesor.toString());
            }
            return success;
        } catch (Exception e) {
            logger.error("Error al actualizar asesor: " + asesor.getIdAsesor(), e);
            return false;
        }
    }

    /**
     * Elimina un asesor (soft delete)
     */
    public boolean eliminarAsesor(Long idAsesor) {
        try {
            Optional<Asesor> asesorOpt = asesorDAO.findById(idAsesor);
            if (!asesorOpt.isPresent()) {
                logger.warn("Asesor con ID " + idAsesor + " no encontrado para eliminar.");
                return false;
            }
            Asesor asesor = asesorOpt.get();
            boolean success = asesorDAO.delete(idAsesor);
            if (success) {
                auditoriaService.registrarAuditoria("asesores", idAsesor.toString(), "DELETE", asesor.toString(), null);
            }
            return success;
        } catch (Exception e) {
            logger.error("Error al eliminar asesor: " + idAsesor, e);
            return false;
        }
    }

    /**
     * Obtiene un asesor por ID
     */
    public Optional<Asesor> obtenerAsesorPorId(Long idAsesor) {
        try {
            return asesorDAO.findById(idAsesor);
        } catch (Exception e) {
            logger.error("Error al obtener asesor por ID: " + idAsesor, e);
            return Optional.empty();
        }
    }

    /**
     * Obtiene todos los asesores
     */
    public List<Asesor> obtenerTodosLosAsesores() {
        try {
            return asesorDAO.findAll();
        } catch (Exception e) {
            logger.error("Error al obtener todos los asesores", e);
            return List.of();
        }
    }

    /**
     * Obtiene asesores activos
     */
    public List<Asesor> obtenerAsesoresActivos() {
        try {
            return asesorDAO.findActivos();
        } catch (Exception e) {
            logger.error("Error al obtener asesores activos", e);
            return List.of();
        }
    }

    /**
     * Busca asesores por nombre
     */
    public List<Asesor> buscarAsesoresPorNombre(String termino) {
        try {
            return asesorDAO.searchByName(termino);
        } catch (Exception e) {
            logger.error("Error al buscar asesores por nombre: " + termino, e);
            return List.of();
        }
    }

    /**
     * Verifica si un asesor existe
     */
    public boolean existeAsesor(Long idAsesor) {
        try {
            return asesorDAO.exists(idAsesor);
        } catch (Exception e) {
            logger.error("Error al verificar existencia de asesor: " + idAsesor, e);
            return false;
        }
    }

    /**
     * Obtiene el nombre completo de un asesor
     */
    public String obtenerNombreCompletoAsesor(Long idAsesor) {
        try {
            Optional<Asesor> asesorOpt = asesorDAO.findById(idAsesor);
            if (asesorOpt.isPresent()) {
                Asesor asesor = asesorOpt.get();
                return asesor.getNombre() + " " + asesor.getApellido();
            }
            return "Asesor no encontrado";
        } catch (Exception e) {
            logger.error("Error al obtener nombre completo del asesor: " + idAsesor, e);
            return "Error";
        }
    }
}
