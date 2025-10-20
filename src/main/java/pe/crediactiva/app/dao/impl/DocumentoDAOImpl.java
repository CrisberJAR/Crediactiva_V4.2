package pe.crediactiva.app.dao.impl;

import pe.crediactiva.app.config.DatabaseConfig;
import pe.crediactiva.app.dao.DocumentoDAO;
import pe.crediactiva.app.model.Documento;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import pe.crediactiva.app.util.DateTimeUtil;

/**
 * Implementación JDBC del DAO para Documento
 */
public class DocumentoDAOImpl implements DocumentoDAO {

    private static final Logger logger = LoggerFactory.getLogger(DocumentoDAOImpl.class);

    @Override
    public Optional<Documento> findById(Long idDocumento) {
        String sql = "SELECT * FROM documentos WHERE id_documento = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idDocumento);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDocumento(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error al buscar documento por ID: " + idDocumento, e);
        }

        return Optional.empty();
    }

    @Override
    public boolean create(Documento documento) {
        String sql = "INSERT INTO documentos (id_prestamo, tipo, ruta, subido_en) " +
                    "VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setLong(1, documento.getIdPrestamo());
            stmt.setString(2, documento.getTipo());
            stmt.setString(3, documento.getRuta());
            stmt.setTimestamp(4, DateTimeUtil.nowAsTimestamp());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        documento.setIdDocumento(rs.getLong(1));
                    }
                }
                logger.info("Documento creado exitosamente: " + documento.getIdDocumento());
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error al crear documento", e);
        }
        return false;
    }

    @Override
    public boolean update(Documento documento) {
        String sql = "UPDATE documentos SET id_prestamo = ?, tipo = ?, ruta = ?, subido_en = ? WHERE id_documento = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, documento.getIdPrestamo());
            stmt.setString(2, documento.getTipo());
            stmt.setString(3, documento.getRuta());
            stmt.setTimestamp(4, DateTimeUtil.nowAsTimestamp());
            stmt.setLong(5, documento.getIdDocumento());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Documento actualizado exitosamente: " + documento.getIdDocumento());
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error al actualizar documento: " + documento.getIdDocumento(), e);
        }
        return false;
    }

    @Override
    public boolean delete(Long idDocumento) {
        String sql = "DELETE FROM documentos WHERE id_documento = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idDocumento);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                logger.info("Documento eliminado exitosamente: " + idDocumento);
                return true;
            }

        } catch (SQLException e) {
            logger.error("Error al eliminar documento: " + idDocumento, e);
        }
        return false;
    }

    @Override
    public List<Documento> findAll() {
        String sql = "SELECT * FROM documentos ORDER BY subido_en DESC";
        List<Documento> documentos = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                documentos.add(mapResultSetToDocumento(rs));
            }

        } catch (SQLException e) {
            logger.error("Error al obtener todos los documentos", e);
        }

        return documentos;
    }

    @Override
    public List<Documento> findByPrestamo(Long idPrestamo) {
        String sql = "SELECT * FROM documentos WHERE id_prestamo = ? ORDER BY subido_en DESC";
        List<Documento> documentos = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idPrestamo);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    documentos.add(mapResultSetToDocumento(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error al buscar documentos por préstamo: " + idPrestamo, e);
        }

        return documentos;
    }

    @Override
    public List<Documento> findByTipo(String tipo) {
        String sql = "SELECT * FROM documentos WHERE tipo = ? ORDER BY subido_en DESC";
        List<Documento> documentos = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tipo);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    documentos.add(mapResultSetToDocumento(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error al buscar documentos por tipo: " + tipo, e);
        }

        return documentos;
    }

    @Override
    public List<Documento> findByPrestamoAndTipo(Long idPrestamo, String tipo) {
        String sql = "SELECT * FROM documentos WHERE id_prestamo = ? AND tipo = ? ORDER BY subido_en DESC";
        List<Documento> documentos = new ArrayList<>();

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idPrestamo);
            stmt.setString(2, tipo);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    documentos.add(mapResultSetToDocumento(rs));
                }
            }

        } catch (SQLException e) {
            logger.error("Error al buscar documentos por préstamo y tipo: " + idPrestamo + ", " + tipo, e);
        }

        return documentos;
    }

    @Override
    public boolean exists(Long idDocumento) {
        String sql = "SELECT COUNT(*) FROM documentos WHERE id_documento = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, idDocumento);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {
            logger.error("Error al verificar existencia de documento: " + idDocumento, e);
        }

        return false;
    }

    private Documento mapResultSetToDocumento(ResultSet rs) throws SQLException {
        Documento documento = new Documento();
        documento.setIdDocumento(rs.getLong("id_documento"));
        documento.setIdPrestamo(rs.getLong("id_prestamo"));
        documento.setTipo(rs.getString("tipo"));
        documento.setRuta(rs.getString("ruta"));
        documento.setSubidoEn(rs.getTimestamp("subido_en").toLocalDateTime());
        return documento;
    }
}
