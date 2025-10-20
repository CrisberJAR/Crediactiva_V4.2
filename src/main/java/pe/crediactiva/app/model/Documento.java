package pe.crediactiva.app.model;

import java.time.LocalDateTime;
import pe.crediactiva.app.util.DateTimeUtil;

/**
 * Modelo para la entidad Documento
 */
public class Documento {
    private Long idDocumento;
    private Long idPrestamo;
    private String tipo;
    private String ruta;
    private LocalDateTime subidoEn;

    // Campos de relación
    private Prestamo prestamo;

    public Documento() {
    }

    public Documento(Long idPrestamo, String tipo, String ruta) {
        this.idPrestamo = idPrestamo;
        this.tipo = tipo;
        this.ruta = ruta;
        this.subidoEn = DateTimeUtil.now();
    }

    // Getters y Setters
    public Long getIdDocumento() {
        return idDocumento;
    }

    public void setIdDocumento(Long idDocumento) {
        this.idDocumento = idDocumento;
    }

    public Long getIdPrestamo() {
        return idPrestamo;
    }

    public void setIdPrestamo(Long idPrestamo) {
        this.idPrestamo = idPrestamo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }

    public LocalDateTime getSubidoEn() {
        return subidoEn;
    }

    public void setSubidoEn(LocalDateTime subidoEn) {
        this.subidoEn = subidoEn;
    }

    public Prestamo getPrestamo() {
        return prestamo;
    }

    public void setPrestamo(Prestamo prestamo) {
        this.prestamo = prestamo;
    }

    /**
     * Obtiene el nombre del archivo desde la ruta
     */
    public String getNombreArchivo() {
        if (ruta != null && !ruta.isEmpty()) {
            return ruta.substring(ruta.lastIndexOf("/") + 1);
        }
        return "";
    }

    /**
     * Obtiene la extensión del archivo
     */
    public String getExtension() {
        String nombreArchivo = getNombreArchivo();
        int lastDotIndex = nombreArchivo.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < nombreArchivo.length() - 1) {
            return nombreArchivo.substring(lastDotIndex);
        }
        return "";
    }

    /**
     * Verifica si es un archivo PDF
     */
    public boolean isPDF() {
        return ".pdf".equalsIgnoreCase(getExtension());
    }

    /**
     * Verifica si es una imagen
     */
    public boolean isImagen() {
        String extension = getExtension().toLowerCase();
        return extension.equals(".jpg") || extension.equals(".jpeg") || 
               extension.equals(".png") || extension.equals(".gif");
    }

    @Override
    public String toString() {
        return "Documento{" +
                "idDocumento=" + idDocumento +
                ", idPrestamo=" + idPrestamo +
                ", tipo='" + tipo + '\'' +
                ", ruta='" + ruta + '\'' +
                ", subidoEn=" + subidoEn +
                '}';
    }
}
