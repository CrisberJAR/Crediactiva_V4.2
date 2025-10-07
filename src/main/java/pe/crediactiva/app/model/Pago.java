package pe.crediactiva.app.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Modelo para la entidad Pago
 */
public class Pago {
    private Long idPago;
    private Long idCuota;
    private Long idCliente;
    private Long idAsesor;
    private LocalDateTime fechaPago;
    private BigDecimal montoPagado;
    
    // Campos de relación
    private Cronograma cuota;
    private Cliente cliente;
    private Asesor asesor;
    
    public Pago() {}
    
    public Pago(Long idCuota, Long idCliente, Long idAsesor, BigDecimal montoPagado) {
        this.idCuota = idCuota;
        this.idCliente = idCliente;
        this.idAsesor = idAsesor;
        this.montoPagado = montoPagado;
        this.fechaPago = LocalDateTime.now();
    }
    
    // Getters y Setters
    public Long getIdPago() {
        return idPago;
    }
    
    public void setIdPago(Long idPago) {
        this.idPago = idPago;
    }
    
    public Long getIdCuota() {
        return idCuota;
    }
    
    public void setIdCuota(Long idCuota) {
        this.idCuota = idCuota;
    }
    
    public Long getIdCliente() {
        return idCliente;
    }
    
    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }
    
    public Long getIdAsesor() {
        return idAsesor;
    }
    
    public void setIdAsesor(Long idAsesor) {
        this.idAsesor = idAsesor;
    }
    
    public LocalDateTime getFechaPago() {
        return fechaPago;
    }
    
    public void setFechaPago(LocalDateTime fechaPago) {
        this.fechaPago = fechaPago;
    }
    
    public BigDecimal getMontoPagado() {
        return montoPagado;
    }
    
    public void setMontoPagado(BigDecimal montoPagado) {
        this.montoPagado = montoPagado;
    }
    
    public Cronograma getCuota() {
        return cuota;
    }
    
    public void setCuota(Cronograma cuota) {
        this.cuota = cuota;
    }
    
    public Cliente getCliente() {
        return cliente;
    }
    
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }
    
    public Asesor getAsesor() {
        return asesor;
    }
    
    public void setAsesor(Asesor asesor) {
        this.asesor = asesor;
    }
    
    @Override
    public String toString() {
        return "Pago: " + montoPagado + " - " + fechaPago.toLocalDate();
    }
}
