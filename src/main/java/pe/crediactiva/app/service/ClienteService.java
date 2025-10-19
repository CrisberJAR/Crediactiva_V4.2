package pe.crediactiva.app.service;

import pe.crediactiva.app.dao.ClienteDAO;
import pe.crediactiva.app.dao.impl.ClienteDAOImpl;
import pe.crediactiva.app.model.Cliente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

/**
 * Servicio de gestión de clientes
 */
public class ClienteService {

    private static final Logger logger = LoggerFactory.getLogger(ClienteService.class);

    private final ClienteDAO clienteDAO;
    private final AuditoriaService auditoriaService;

    public ClienteService() {
        this.clienteDAO = new ClienteDAOImpl();
        this.auditoriaService = new AuditoriaService();
    }

    /**
     * Crea un nuevo cliente
     */
    public boolean crearCliente(Cliente cliente) {
        try {
            if (clienteDAO.exists(cliente.getIdCliente())) {
                logger.warn("Cliente con ID " + cliente.getIdCliente() + " ya existe.");
                return false;
            }
            boolean success = clienteDAO.create(cliente);
            if (success) {
                auditoriaService.registrarAuditoria("clientes", cliente.getIdCliente().toString(), "INSERT", null, cliente.toString());
            }
            return success;
        } catch (Exception e) {
            logger.error("Error al crear cliente: " + cliente.getIdCliente(), e);
            return false;
        }
    }

    /**
     * Actualiza un cliente existente
     */
    public boolean actualizarCliente(Cliente cliente) {
        try {
            Optional<Cliente> oldClienteOpt = clienteDAO.findById(cliente.getIdCliente());
            if (!oldClienteOpt.isPresent()) {
                logger.warn("Cliente con ID " + cliente.getIdCliente() + " no encontrado para actualizar.");
                return false;
            }
            Cliente oldCliente = oldClienteOpt.get();
            boolean success = clienteDAO.update(cliente);
            if (success) {
                auditoriaService.registrarAuditoria("clientes", cliente.getIdCliente().toString(), "UPDATE", oldCliente.toString(), cliente.toString());
            }
            return success;
        } catch (Exception e) {
            logger.error("Error al actualizar cliente: " + cliente.getIdCliente(), e);
            return false;
        }
    }

    /**
     * Elimina un cliente (soft delete)
     */
    public boolean eliminarCliente(Long idCliente) {
        try {
            Optional<Cliente> clienteOpt = clienteDAO.findById(idCliente);
            if (!clienteOpt.isPresent()) {
                logger.warn("Cliente con ID " + idCliente + " no encontrado para eliminar.");
                return false;
            }
            Cliente cliente = clienteOpt.get();
            boolean success = clienteDAO.delete(idCliente);
            if (success) {
                auditoriaService.registrarAuditoria("clientes", idCliente.toString(), "DELETE", cliente.toString(), null);
            }
            return success;
        } catch (Exception e) {
            logger.error("Error al eliminar cliente: " + idCliente, e);
            return false;
        }
    }

    /**
     * Obtiene un cliente por ID
     */
    public Optional<Cliente> obtenerClientePorId(Long idCliente) {
        try {
            return clienteDAO.findById(idCliente);
        } catch (Exception e) {
            logger.error("Error al obtener cliente por ID: " + idCliente, e);
            return Optional.empty();
        }
    }

    /**
     * Obtiene todos los clientes
     */
    public List<Cliente> obtenerTodosLosClientes() {
        try {
            return clienteDAO.findAll();
        } catch (Exception e) {
            logger.error("Error al obtener todos los clientes", e);
            return List.of();
        }
    }

    /**
     * Obtiene clientes por asesor
     */
    public List<Cliente> obtenerClientesPorAsesor(Long idAsesor) {
        try {
            return clienteDAO.findByAsesor(idAsesor);
        } catch (Exception e) {
            logger.error("Error al obtener clientes por asesor: " + idAsesor, e);
            return List.of();
        }
    }

    /**
     * Busca clientes por nombre
     */
    public List<Cliente> buscarClientesPorNombre(String termino) {
        try {
            return clienteDAO.searchByName(termino);
        } catch (Exception e) {
            logger.error("Error al buscar clientes por nombre: " + termino, e);
            return List.of();
        }
    }

    /**
     * Actualiza el saldo de capital de un cliente
     */
    public boolean actualizarSaldoCapital(Long idCliente, java.math.BigDecimal nuevoSaldo) {
        try {
            boolean success = clienteDAO.updateSaldoCapital(idCliente, nuevoSaldo);
            if (success) {
                auditoriaService.registrarAuditoria("clientes", idCliente.toString(), "UPDATE", 
                    "saldo_capital", "saldo_capital=" + nuevoSaldo);
            }
            return success;
        } catch (Exception e) {
            logger.error("Error al actualizar saldo de capital: " + idCliente, e);
            return false;
        }
    }

    /**
     * Actualiza la etiqueta de un cliente
     */
    public boolean actualizarEtiquetaCliente(Long idCliente, Cliente.EtiquetaCliente etiqueta) {
        try {
            boolean success = clienteDAO.updateEtiqueta(idCliente, etiqueta);
            if (success) {
                auditoriaService.registrarAuditoria("clientes", idCliente.toString(), "UPDATE", 
                    "etiqueta_cliente", "etiqueta_cliente=" + etiqueta.name());
            }
            return success;
        } catch (Exception e) {
            logger.error("Error al actualizar etiqueta de cliente: " + idCliente, e);
            return false;
        }
    }

    /**
     * Verifica si un cliente existe
     */
    public boolean existeCliente(Long idCliente) {
        try {
            return clienteDAO.exists(idCliente);
        } catch (Exception e) {
            logger.error("Error al verificar existencia de cliente: " + idCliente, e);
            return false;
        }
    }

    /**
     * Obtiene clientes con préstamos activos
     */
    public List<Cliente> obtenerClientesConPrestamosActivos() {
        try {
            return clienteDAO.findWithActiveLoans();
        } catch (Exception e) {
            logger.error("Error al obtener clientes con préstamos activos", e);
            return List.of();
        }
    }

    /**
     * Obtiene clientes morosos
     */
    public List<Cliente> obtenerClientesMorosos() {
        try {
            return clienteDAO.findMorosos();
        } catch (Exception e) {
            logger.error("Error al obtener clientes morosos", e);
            return List.of();
        }
    }

    /**
     * Busca un cliente por DNI
     */
    public Optional<Cliente> buscarPorDni(Long dni) {
        try {
            return clienteDAO.findById(dni);
        } catch (Exception e) {
            logger.error("Error al buscar cliente por DNI: " + dni, e);
            return Optional.empty();
        }
    }

    /**
     * Obtiene un cliente por DNI (alias de buscarPorDni)
     */
    public Optional<Cliente> obtenerClientePorDni(Long dni) {
        return buscarPorDni(dni);
    }
    
    /**
     * Verifica si un cliente pertenece a un asesor específico
     */
    public boolean clientePerteneceAlAsesor(Long idCliente, Long idAsesor) {
        try {
            // Obtener todos los clientes del asesor
            List<Cliente> clientesDelAsesor = obtenerClientesPorAsesor(idAsesor);
            
            // Verificar si el cliente está en la lista
            boolean pertenece = clientesDelAsesor.stream()
                .anyMatch(cliente -> cliente.getIdCliente().equals(idCliente));
            
            logger.info("Verificación de pertenencia - Cliente: " + idCliente + 
                       ", Asesor: " + idAsesor + ", Pertenece: " + pertenece);
            
            return pertenece;
            
        } catch (Exception e) {
            logger.error("Error al verificar si el cliente pertenece al asesor: " + idCliente + ", " + idAsesor, e);
            return false; // Por seguridad, asumir que no pertenece
        }
    }
}