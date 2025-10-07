package pe.crediactiva.app.dao.impl;

import pe.crediactiva.app.config.DatabaseConfig;
import pe.crediactiva.app.dao.AsesorDAO;
import pe.crediactiva.app.model.Asesor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC del DAO para Asesor
 */
public class AsesorDAOImpl implements AsesorDAO {

    private static final Logger logger = LoggerFactory.getLogger(AsesorDAOImpl.class);

    @Override
    public Optional<Asesor> findById(Long idAsesor) {
        String sql = "SELECT * FROM asesores WHERE id_asesor = ? AND activo = TRUE";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idAsesor);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToAsesor(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error al buscar asesor por ID: " + idAsesor, e);
        }

        return Optional.empty();
    }

    @Override
    public List<Asesor> findAll() {
        String sql = "SELECT * FROM asesores WHERE activo = TRUE ORDER BY nombre, apellido";
        List<Asesor> asesores = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                asesores.add(mapResultSetToAsesor(rs));
            }

        } catch (SQLException e) {
            logger.error("Error al obtener todos los asesores", e);
        }

        return asesores;
    }

    @Override
    public List<Asesor> findActivos() {
        return findAll(); // Mismo comportamiento
    }

    @Override
    public List<Asesor> searchByName(String searchTerm) {
        String sql = "SELECT * FROM asesores WHERE activo = TRUE AND " +
                    "(nombre LIKE ? OR apellido LIKE ?) ORDER BY nombre, apellido";
        List<Asesor> asesores = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String searchPattern = "%" + searchTerm + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    asesores.add(mapResultSetToAsesor(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error al buscar asesores por nombre: " + searchTerm, e);
        }

        return asesores;
    }

    @Override
    public boolean create(Asesor asesor) {
        String sql = "INSERT INTO asesores (id_asesor, nombre, apellido, fecha_contrato, direccion, telefono, email, activo) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, asesor.getIdAsesor());
            stmt.setString(2, asesor.getNombre());
            stmt.setString(3, asesor.getApellido());
            stmt.setDate(4, Date.valueOf(asesor.getFechaContrato()));
            stmt.setString(5, asesor.getDireccion());
            stmt.setString(6, asesor.getTelefono());
            stmt.setString(7, asesor.getEmail());
            stmt.setBoolean(8, asesor.isActivo());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Asesor creado exitosamente: " + asesor.getIdAsesor() + " - " + asesor.getNombreCompleto());
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error al crear asesor: " + asesor.getIdAsesor(), e);
        }

        return false;
    }

    @Override
    public boolean update(Asesor asesor) {
        String sql = "UPDATE asesores SET nombre = ?, apellido = ?, fecha_contrato = ?, direccion = ?, " +
                    "telefono = ?, email = ?, activo = ? WHERE id_asesor = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, asesor.getNombre());
            stmt.setString(2, asesor.getApellido());
            stmt.setDate(3, Date.valueOf(asesor.getFechaContrato()));
            stmt.setString(4, asesor.getDireccion());
            stmt.setString(5, asesor.getTelefono());
            stmt.setString(6, asesor.getEmail());
            stmt.setBoolean(7, asesor.isActivo());
            stmt.setLong(8, asesor.getIdAsesor());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Asesor actualizado exitosamente: " + asesor.getIdAsesor() + " - " + asesor.getNombreCompleto());
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error al actualizar asesor: " + asesor.getIdAsesor(), e);
        }

        return false;
    }

    @Override
    public boolean delete(Long idAsesor) {
        String sql = "UPDATE asesores SET activo = FALSE WHERE id_asesor = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idAsesor);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Asesor eliminado exitosamente: " + idAsesor);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error al eliminar asesor: " + idAsesor, e);
        }

        return false;
    }

    @Override
    public boolean exists(Long idAsesor) {
        String sql = "SELECT COUNT(*) FROM asesores WHERE id_asesor = ? AND activo = TRUE";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idAsesor);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            logger.error("Error al verificar existencia de asesor: " + idAsesor, e);
        }

        return false;
    }

    private Asesor mapResultSetToAsesor(ResultSet rs) throws SQLException {
        Asesor asesor = new Asesor();
        asesor.setIdAsesor(rs.getLong("id_asesor"));
        asesor.setNombre(rs.getString("nombre"));
        asesor.setApellido(rs.getString("apellido"));
        asesor.setFechaContrato(rs.getDate("fecha_contrato").toLocalDate());
        asesor.setDireccion(rs.getString("direccion"));
        asesor.setTelefono(rs.getString("telefono"));
        asesor.setEmail(rs.getString("email"));
        asesor.setActivo(rs.getBoolean("activo"));
        return asesor;
    }
}
