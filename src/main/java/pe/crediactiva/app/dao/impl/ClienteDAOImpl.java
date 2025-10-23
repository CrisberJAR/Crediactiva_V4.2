package pe.crediactiva.app.dao.impl;

import pe.crediactiva.app.config.DatabaseConfig;
import pe.crediactiva.app.dao.ClienteDAO;
import pe.crediactiva.app.model.Asesor;
import pe.crediactiva.app.model.Cliente;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC del DAO para Cliente
 */
public class ClienteDAOImpl implements ClienteDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(ClienteDAOImpl.class);
    
    @Override
    public Optional<Cliente> findById(Long idCliente) {
        String sql = "SELECT c.*, a.nombre as asesor_nombre, a.apellido as asesor_apellido " +
                    "FROM clientes c " +
                    "LEFT JOIN asesores a ON c.id_asesor = a.id_asesor " +
                    "WHERE c.id_cliente = ? AND c.activo = true";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idCliente);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToCliente(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar cliente por ID: " + idCliente, e);
        }
        
        return Optional.empty();
    }
    
    @Override
    public List<Cliente> findAll() {
        String sql = "SELECT c.*, a.nombre as asesor_nombre, a.apellido as asesor_apellido " +
                    "FROM clientes c " +
                    "LEFT JOIN asesores a ON c.id_asesor = a.id_asesor " +
                    "WHERE c.activo = true " +
                    "ORDER BY c.apellido, c.nombre";
        
        List<Cliente> clientes = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                clientes.add(mapResultSetToCliente(rs));
            }
            
        } catch (SQLException e) {
            logger.error("Error al obtener todos los clientes", e);
        }
        
        return clientes;
    }
    
    @Override
    public List<Cliente> findByAsesor(Long idAsesor) {
        String sql = "SELECT c.*, a.nombre as asesor_nombre, a.apellido as asesor_apellido " +
                    "FROM clientes c " +
                    "LEFT JOIN asesores a ON c.id_asesor = a.id_asesor " +
                    "WHERE c.id_asesor = ? AND c.activo = true " +
                    "ORDER BY c.apellido, c.nombre";
        
        List<Cliente> clientes = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idAsesor);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    clientes.add(mapResultSetToCliente(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar clientes por asesor: " + idAsesor, e);
        }
        
        return clientes;
    }
    
    @Override
    public List<Cliente> findByEtiqueta(Cliente.EtiquetaCliente etiqueta) {
        String sql = "SELECT c.*, a.nombre as asesor_nombre, a.apellido as asesor_apellido " +
                    "FROM clientes c " +
                    "LEFT JOIN asesores a ON c.id_asesor = a.id_asesor " +
                    "WHERE c.etiqueta_cliente = ? AND c.activo = true " +
                    "ORDER BY c.apellido, c.nombre";
        
        List<Cliente> clientes = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, etiqueta.name().toLowerCase());
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    clientes.add(mapResultSetToCliente(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar clientes por etiqueta: " + etiqueta, e);
        }
        
        return clientes;
    }
    
    @Override
    public List<Cliente> searchByName(String searchTerm) {
        String sql = "SELECT c.*, a.nombre as asesor_nombre, a.apellido as asesor_apellido " +
                    "FROM clientes c " +
                    "LEFT JOIN asesores a ON c.id_asesor = a.id_asesor " +
                    "WHERE c.activo = true AND " +
                    "(c.nombre LIKE ? OR c.apellido LIKE ? OR CONCAT(c.nombre, ' ', c.apellido) LIKE ?) " +
                    "ORDER BY c.apellido, c.nombre";
        
        List<Cliente> clientes = new ArrayList<>();
        String searchPattern = "%" + searchTerm + "%";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    clientes.add(mapResultSetToCliente(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar clientes por nombre: " + searchTerm, e);
        }
        
        return clientes;
    }
    
    @Override
    public boolean create(Cliente cliente) {
        String sql = "INSERT INTO clientes (id_cliente, nombre, apellido, fecha_registro, fecha_nacimiento, sexo, direccion, " +
                    "telefono, email, ocupacion, lugar_trabajo, id_asesor, saldo_capital, etiqueta_cliente, activo) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, cliente.getIdCliente());
            stmt.setString(2, cliente.getNombre());
            stmt.setString(3, cliente.getApellido());
            stmt.setDate(4, Date.valueOf(cliente.getFechaRegistro()));
            
            // Fecha de nacimiento
            if (cliente.getFechaNacimiento() != null) {
                stmt.setDate(5, Date.valueOf(cliente.getFechaNacimiento()));
            } else {
                stmt.setNull(5, java.sql.Types.DATE);
            }
            
            // Sexo
            stmt.setString(6, cliente.getSexo());
            
            stmt.setString(7, cliente.getDireccion());
            stmt.setString(8, cliente.getTelefono());
            stmt.setString(9, cliente.getEmail());
            
            // Ocupación
            stmt.setString(10, cliente.getOcupacion());
            
            // Lugar de trabajo
            stmt.setString(11, cliente.getLugarTrabajo());
            
            stmt.setLong(12, cliente.getIdAsesor());
            stmt.setBigDecimal(13, cliente.getSaldoCapital());
            stmt.setString(14, cliente.getEtiquetaCliente().name().toLowerCase());
            stmt.setBoolean(15, cliente.isActivo());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Cliente creado exitosamente: " + cliente.getIdCliente() + " - " + cliente.getNombreCompleto());
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al crear cliente: " + cliente.getIdCliente(), e);
        }
        
        return false;
    }
    
    @Override
    public boolean save(Cliente cliente) {
        // Si existe, actualizar; si no existe, crear
        if (exists(cliente.getIdCliente())) {
            return update(cliente);
        } else {
            return create(cliente);
        }
    }
    
    @Override
    public boolean update(Cliente cliente) {
        String sql = "UPDATE clientes SET nombre = ?, apellido = ?, direccion = ?, telefono = ?, " +
                    "email = ?, id_asesor = ?, saldo_capital = ?, etiqueta_cliente = ?, activo = ? " +
                    "WHERE id_cliente = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, cliente.getNombre());
            stmt.setString(2, cliente.getApellido());
            stmt.setString(3, cliente.getDireccion());
            stmt.setString(4, cliente.getTelefono());
            stmt.setString(5, cliente.getEmail());
            stmt.setLong(6, cliente.getIdAsesor());
            stmt.setBigDecimal(7, cliente.getSaldoCapital());
            stmt.setString(8, cliente.getEtiquetaCliente().name().toLowerCase());
            stmt.setBoolean(9, cliente.isActivo());
            stmt.setLong(10, cliente.getIdCliente());
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Cliente actualizado exitosamente: " + cliente.getIdCliente());
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al actualizar cliente: " + cliente.getIdCliente(), e);
        }
        
        return false;
    }
    
    @Override
    public boolean delete(Long idCliente) {
        // Soft delete
        String sql = "UPDATE clientes SET activo = false WHERE id_cliente = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idCliente);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Cliente eliminado (soft delete): " + idCliente);
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al eliminar cliente: " + idCliente, e);
        }
        
        return false;
    }
    
    @Override
    public boolean exists(Long idCliente) {
        String sql = "SELECT COUNT(*) FROM clientes WHERE id_cliente = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setLong(1, idCliente);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al verificar existencia de cliente: " + idCliente, e);
        }
        
        return false;
    }
    
    @Override
    public boolean updateSaldoCapital(Long idCliente, BigDecimal nuevoSaldo) {
        String sql = "UPDATE clientes SET saldo_capital = ? WHERE id_cliente = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setBigDecimal(1, nuevoSaldo);
            stmt.setLong(2, idCliente);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Saldo de capital actualizado para cliente: " + idCliente + " -> " + nuevoSaldo);
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al actualizar saldo de capital del cliente: " + idCliente, e);
        }
        
        return false;
    }
    
    @Override
    public boolean updateEtiqueta(Long idCliente, Cliente.EtiquetaCliente etiqueta) {
        String sql = "UPDATE clientes SET etiqueta_cliente = ? WHERE id_cliente = ?";
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, etiqueta.name().toLowerCase());
            stmt.setLong(2, idCliente);
            
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Etiqueta actualizada para cliente: " + idCliente + " -> " + etiqueta);
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Error al actualizar etiqueta del cliente: " + idCliente, e);
        }
        
        return false;
    }
    
    @Override
    public List<Cliente> findWithActiveLoans() {
        String sql = "SELECT DISTINCT c.*, a.nombre as asesor_nombre, a.apellido as asesor_apellido " +
                    "FROM clientes c " +
                    "LEFT JOIN asesores a ON c.id_asesor = a.id_asesor " +
                    "JOIN prestamos p ON c.id_cliente = p.id_cliente " +
                    "WHERE c.activo = true AND p.estado = 'activo' " +
                    "ORDER BY c.apellido, c.nombre";
        
        List<Cliente> clientes = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                clientes.add(mapResultSetToCliente(rs));
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar clientes con préstamos activos", e);
        }
        
        return clientes;
    }
    
    @Override
    public List<Cliente> findMorosos() {
        String sql = "SELECT DISTINCT c.*, a.nombre as asesor_nombre, a.apellido as asesor_apellido " +
                    "FROM clientes c " +
                    "LEFT JOIN asesores a ON c.id_asesor = a.id_asesor " +
                    "JOIN prestamos p ON c.id_cliente = p.id_cliente " +
                    "JOIN cronograma cr ON p.id_prestamo = cr.id_prestamo " +
                    "WHERE c.activo = true AND cr.estado_cuota = 'retrasada' " +
                    "ORDER BY c.apellido, c.nombre";
        
        List<Cliente> clientes = new ArrayList<>();
        
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                clientes.add(mapResultSetToCliente(rs));
            }
            
        } catch (SQLException e) {
            logger.error("Error al buscar clientes morosos", e);
        }
        
        return clientes;
    }
    
    /**
     * Mapea un ResultSet a un objeto Cliente
     */
    private Cliente mapResultSetToCliente(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setIdCliente(rs.getLong("id_cliente"));
        cliente.setNombre(rs.getString("nombre"));
        cliente.setApellido(rs.getString("apellido"));
        cliente.setFechaRegistro(rs.getDate("fecha_registro").toLocalDate());
        
        // hasta aquí ya estaba, ahora agregamos los nuevos campos:
        
        // Fecha de nacimiento
        Date fechaNacimiento = rs.getDate("fecha_nacimiento");
        if (fechaNacimiento != null) {
            cliente.setFechaNacimiento(fechaNacimiento.toLocalDate());
        }
        
        // Sexo
        cliente.setSexo(rs.getString("sexo"));
        
        cliente.setDireccion(rs.getString("direccion"));
        cliente.setTelefono(rs.getString("telefono"));
        cliente.setEmail(rs.getString("email"));
        
        // Ocupación
        cliente.setOcupacion(rs.getString("ocupacion"));
        
        // Lugar de trabajo
        cliente.setLugarTrabajo(rs.getString("lugar_trabajo"));
        
        cliente.setIdAsesor(rs.getLong("id_asesor"));
        cliente.setSaldoCapital(rs.getBigDecimal("saldo_capital"));
        cliente.setEtiquetaCliente(Cliente.EtiquetaCliente.valueOf(
            rs.getString("etiqueta_cliente").toUpperCase()));
        cliente.setActivo(rs.getBoolean("activo"));
        
        // Crear el asesor si existe
        String asesorNombre = rs.getString("asesor_nombre");
        if (asesorNombre != null) {
            Asesor asesor = new Asesor();
            asesor.setIdAsesor(rs.getLong("id_asesor"));
            asesor.setNombre(asesorNombre);
            asesor.setApellido(rs.getString("asesor_apellido"));
            cliente.setAsesor(asesor);
        }
        
        return cliente;
    }
}
