package pe.crediactiva.app.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import pe.crediactiva.app.util.DateTimeUtil;

/**
 * Modelo para la entidad MovimientosCapital
 */
public class MovimientoCapital {
    private Long idMovimiento;
    private Long idCliente;
    private TipoMovimiento tipoMovimiento;
    private BigDecimal monto;
    private LocalDateTime fecha;
    private Long idAdmin;
    private String observacion;
    
    // Campos de relación
    private Cliente cliente;
    private Usuario admin;
    
    public enum TipoMovimiento {
        ABONO("Abono"),
        DESEMBOLSO("Desembolso");
        
        private final String descripcion;
        
        TipoMovimiento(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
    }
    
    public MovimientoCapital() {}
    
    public MovimientoCapital(Long idCliente, TipoMovimiento tipoMovimiento, BigDecimal monto, Long idAdmin) {
        this.idCliente = idCliente;
        this.tipoMovimiento = tipoMovimiento;
        this.monto = monto;
        this.idAdmin = idAdmin;
        this.fecha = DateTimeUtil.now();
    }
    
    public MovimientoCapital(Long idCliente, TipoMovimiento tipoMovimiento, BigDecimal monto, Long idAdmin, String observacion) {
        this.idCliente = idCliente;
        this.tipoMovimiento = tipoMovimiento;
        this.monto = monto;
        this.idAdmin = idAdmin;
        this.observacion = observacion;
        this.fecha = DateTimeUtil.now();
    }
    
    // Getters y Setters
    public Long getIdMovimiento() {
        return idMovimiento;
    }
    
    public void setIdMovimiento(Long idMovimiento) {
        this.idMovimiento = idMovimiento;
    }
    
    public Long getIdCliente() {
        return idCliente;
    }
    
    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }
    
    public TipoMovimiento getTipoMovimiento() {
        return tipoMovimiento;
    }
    
    public void setTipoMovimiento(TipoMovimiento tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
    }
    
    public BigDecimal getMonto() {
        return monto;
    }
    
    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }
    
    public LocalDateTime getFecha() {
        return fecha;
    }
    
    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
    
    public Long getIdAdmin() {
        return idAdmin;
    }
    
    public void setIdAdmin(Long idAdmin) {
        this.idAdmin = idAdmin;
    }
    
    public String getObservacion() {
        return observacion;
    }
    
    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
    
    public Cliente getCliente() {
        return cliente;
    }
    
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }
    
    public Usuario getAdmin() {
        return admin;
    }
    
    public void setAdmin(Usuario admin) {
        this.admin = admin;
    }
    
    /**
     * Obtiene el nombre completo del cliente
     */
    public String getNombreCliente() {
        if (cliente != null && cliente.getNombre() != null && cliente.getApellido() != null) {
            return cliente.getNombre() + " " + cliente.getApellido();
        }
        return "Cliente #" + idCliente;
    }
    
    /**
     * Obtiene la fecha formateada como string
     */
    public String getFechaFormateada() {
        if (fecha != null) {
            return fecha.toLocalDate().toString();
        }
        return "";
    }
    
    /**
     * Obtiene el tipo de movimiento como string
     */
    public String getTipoMovimientoString() {
        return tipoMovimiento != null ? tipoMovimiento.getDescripcion() : "";
    }
    
    @Override
    public String toString() {
        return tipoMovimiento.getDescripcion() + ": " + monto + " - " + fecha.toLocalDate();
    }
}
