package pe.crediactiva.app.model;

import java.time.LocalDateTime;

/**
 * Modelo para la entidad Auditoria
 */
public class Auditoria {
    private Long idAuditoria;
    private Long idUsuario;
    private String tablaAfectada;
    private String idRegistroAfectado;
    private TipoAccion accion;
    private String valorAnterior;
    private String valorNuevo;
    private LocalDateTime fecha;
    
    // Campos de relación
    private Usuario usuario;
    
    public enum TipoAccion {
        INSERT("Insertar"),
        UPDATE("Actualizar"),
        DELETE("Eliminar");
        
        private final String descripcion;
        
        TipoAccion(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
    }
    
    public Auditoria() {}
    
    public Auditoria(Long idUsuario, String tablaAfectada, String idRegistroAfectado, TipoAccion accion) {
        this.idUsuario = idUsuario;
        this.tablaAfectada = tablaAfectada;
        this.idRegistroAfectado = idRegistroAfectado;
        this.accion = accion;
        this.fecha = LocalDateTime.now();
    }
    
    // Getters y Setters
    public Long getIdAuditoria() {
        return idAuditoria;
    }
    
    public void setIdAuditoria(Long idAuditoria) {
        this.idAuditoria = idAuditoria;
    }
    
    public Long getIdUsuario() {
        return idUsuario;
    }
    
    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }
    
    public String getTablaAfectada() {
        return tablaAfectada;
    }
    
    public void setTablaAfectada(String tablaAfectada) {
        this.tablaAfectada = tablaAfectada;
    }
    
    public String getIdRegistroAfectado() {
        return idRegistroAfectado;
    }
    
    public void setIdRegistroAfectado(String idRegistroAfectado) {
        this.idRegistroAfectado = idRegistroAfectado;
    }
    
    public TipoAccion getAccion() {
        return accion;
    }
    
    public void setAccion(TipoAccion accion) {
        this.accion = accion;
    }
    
    public String getValorAnterior() {
        return valorAnterior;
    }
    
    public void setValorAnterior(String valorAnterior) {
        this.valorAnterior = valorAnterior;
    }
    
    public String getValorNuevo() {
        return valorNuevo;
    }
    
    public void setValorNuevo(String valorNuevo) {
        this.valorNuevo = valorNuevo;
    }
    
    public LocalDateTime getFecha() {
        return fecha;
    }
    
    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
    
    public Usuario getUsuario() {
        return usuario;
    }
    
    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
    
    @Override
    public String toString() {
        return accion.getDescripcion() + " en " + tablaAfectada + " - " + fecha.toLocalDate();
    }
}
