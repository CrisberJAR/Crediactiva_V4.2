package pe.crediactiva.app.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import pe.crediactiva.app.util.DateTimeUtil;

/**
 * Modelo para la entidad Pago
 */
public class Pago {
    private Long idPago;
    private Long idCuota;
    private Long idCliente;
    private Long idAsesor;
    private Long idPrestamo;
    private LocalDateTime fechaPago;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaValidacion;
    private BigDecimal montoPagado;
    private boolean validado;
    private String observaciones;
    
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
        this.fechaPago = DateTimeUtil.now();
        this.fechaRegistro = DateTimeUtil.now();
        this.validado = false;
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
    
    public Long getIdPrestamo() {
        return idPrestamo;
    }
    
    public void setIdPrestamo(Long idPrestamo) {
        this.idPrestamo = idPrestamo;
    }
    
    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }
    
    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
    
    public LocalDateTime getFechaValidacion() {
        return fechaValidacion;
    }
    
    public void setFechaValidacion(LocalDateTime fechaValidacion) {
        this.fechaValidacion = fechaValidacion;
    }
    
    public boolean isValidado() {
        return validado;
    }
    
    public void setValidado(boolean validado) {
        this.validado = validado;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    @Override
    public String toString() {
        return "Pago: " + montoPagado + " - " + fechaPago.toLocalDate();
    }
}
