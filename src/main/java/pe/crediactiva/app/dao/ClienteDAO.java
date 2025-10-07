package pe.crediactiva.app.dao;

import pe.crediactiva.app.model.Cliente;
import java.util.List;
import java.util.Optional;

/**
 * Interfaz DAO para la entidad Cliente
 */
public interface ClienteDAO {
    
    /**
     * Busca un cliente por su ID
     */
    Optional<Cliente> findById(Long idCliente);
    
    /**
     * Obtiene todos los clientes
     */
    List<Cliente> findAll();
    
    /**
     * Obtiene clientes por asesor
     */
    List<Cliente> findByAsesor(Long idAsesor);
    
    /**
     * Obtiene clientes por etiqueta
     */
    List<Cliente> findByEtiqueta(Cliente.EtiquetaCliente etiqueta);
    
    /**
     * Busca clientes por nombre o apellido
     */
    List<Cliente> searchByName(String searchTerm);
    
    /**
     * Crea un nuevo cliente
     */
    boolean create(Cliente cliente);
    
    /**
     * Guarda un cliente (create o update)
     */
    boolean save(Cliente cliente);
    
    /**
     * Actualiza un cliente existente
     */
    boolean update(Cliente cliente);
    
    /**
     * Elimina un cliente (soft delete)
     */
    boolean delete(Long idCliente);
    
    /**
     * Verifica si existe un cliente con el ID dado
     */
    boolean exists(Long idCliente);
    
    /**
     * Actualiza el saldo de capital de un cliente
     */
    boolean updateSaldoCapital(Long idCliente, java.math.BigDecimal nuevoSaldo);
    
    /**
     * Actualiza la etiqueta de un cliente
     */
    boolean updateEtiqueta(Long idCliente, Cliente.EtiquetaCliente etiqueta);
    
    /**
     * Obtiene clientes con préstamos activos
     */
    List<Cliente> findWithActiveLoans();
    
    /**
     * Obtiene clientes morosos
     */
    List<Cliente> findMorosos();
}
