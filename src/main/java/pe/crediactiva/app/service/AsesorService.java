package pe.crediactiva.app.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.crediactiva.app.model.Asesor;
import pe.crediactiva.app.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Servicio para manejar operaciones relacionadas con asesores
 */
public class AsesorService {
    
    private static final Logger logger = LoggerFactory.getLogger(AsesorService.class);
    
    /**
     * Obtiene un asesor por su ID
     */
    public Optional<Asesor> obtenerAsesorPorId(Long idAsesor) {
        String sql = "SELECT id_asesor, nombre, apellido, telefono, email, activo " +
                     "FROM asesores WHERE id_asesor = ? AND activo = 1";
        
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            statement.setLong(1, idAsesor);
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    Asesor asesor = new Asesor();
                    asesor.setIdAsesor(resultSet.getLong("id_asesor"));
                    asesor.setNombre(resultSet.getString("nombre"));
                    asesor.setApellido(resultSet.getString("apellido"));
                    asesor.setTelefono(resultSet.getString("telefono"));
                    asesor.setEmail(resultSet.getString("email"));
                    asesor.setActivo(resultSet.getBoolean("activo"));
                    
                    logger.info("Asesor encontrado: {} {}", asesor.getNombre(), asesor.getApellido());
                    return Optional.of(asesor);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Error al obtener asesor por ID: " + idAsesor, e);
        }
        
        return Optional.empty();
    }
    
    /**
     * Obtiene todos los asesores activos
     */
    public java.util.List<Asesor> obtenerAsesoresActivos() {
        String sql = "SELECT id_asesor, nombre, apellido, telefono, email, activo " +
                     "FROM asesores WHERE activo = 1 ORDER BY nombre, apellido";
        
        java.util.List<Asesor> asesores = new java.util.ArrayList<>();
        
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            
            while (resultSet.next()) {
                Asesor asesor = new Asesor();
                asesor.setIdAsesor(resultSet.getLong("id_asesor"));
                asesor.setNombre(resultSet.getString("nombre"));
                asesor.setApellido(resultSet.getString("apellido"));
                asesor.setTelefono(resultSet.getString("telefono"));
                asesor.setEmail(resultSet.getString("email"));
                asesor.setActivo(resultSet.getBoolean("activo"));
                
                asesores.add(asesor);
            }
            
            logger.info("Se encontraron {} asesores activos", asesores.size());
            
        } catch (SQLException e) {
            logger.error("Error al obtener asesores activos", e);
        }
        
        return asesores;
    }
}