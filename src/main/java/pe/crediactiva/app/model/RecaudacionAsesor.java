package pe.crediactiva.app.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Modelo para la entidad RecaudacionAsesor
 */
public class RecaudacionAsesor {
    private Long idRecaudacion;
    private Long idAsesor;
    private Long idCliente;
    private Long idPrestamo;
    private Long idCuota;
    private LocalDateTime fechaRegistro;
    private BigDecimal montoRegistrado;
    private boolean validado;
    private String observaciones;
    
    // Campos de relación
    private Asesor asesor;
    private Cliente cliente;
    private Prestamo prestamo;
    
    public RecaudacionAsesor() {
        this.validado = false;
    }
    
    public RecaudacionAsesor(Long idAsesor, Long idCliente, Long idPrestamo, BigDecimal montoRegistrado) {
        this();
        this.idAsesor = idAsesor;
        this.idCliente = idCliente;
        this.idPrestamo = idPrestamo;
        this.montoRegistrado = montoRegistrado;
        this.fechaRegistro = LocalDateTime.now();
    }
    
    public RecaudacionAsesor(Long idAsesor, Long idCliente, Long idPrestamo, Long idCuota, BigDecimal montoRegistrado) {
        this();
        this.idAsesor = idAsesor;
        this.idCliente = idCliente;
        this.idPrestamo = idPrestamo;
        this.idCuota = idCuota;
        this.montoRegistrado = montoRegistrado;
        this.fechaRegistro = LocalDateTime.now();
    }
    
    // Getters y Setters
    public Long getIdRecaudacion() {
        return idRecaudacion;
    }
    
    public void setIdRecaudacion(Long idRecaudacion) {
        this.idRecaudacion = idRecaudacion;
    }
    
    public Long getIdAsesor() {
        return idAsesor;
    }
    
    public void setIdAsesor(Long idAsesor) {
        this.idAsesor = idAsesor;
    }
    
    public Long getIdCliente() {
        return idCliente;
    }
    
    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
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
    
    public BigDecimal getMontoRegistrado() {
        return montoRegistrado;
    }
    
    public void setMontoRegistrado(BigDecimal montoRegistrado) {
        this.montoRegistrado = montoRegistrado;
    }
    
    public boolean isValidado() {
        return validado;
    }
    
    public void setValidado(boolean validado) {
        this.validado = validado;
    }
    
    public Long getIdCuota() {
        return idCuota;
    }
    
    public void setIdCuota(Long idCuota) {
        this.idCuota = idCuota;
    }
    
    public String getObservaciones() {
        return observaciones;
    }
    
    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
    
    public Asesor getAsesor() {
        return asesor;
    }
    
    public void setAsesor(Asesor asesor) {
        this.asesor = asesor;
    }
    
    public Cliente getCliente() {
        return cliente;
    }
    
    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }
    
    public Prestamo getPrestamo() {
        return prestamo;
    }
    
    public void setPrestamo(Prestamo prestamo) {
        this.prestamo = prestamo;
    }
    
    @Override
    public String toString() {
        return "Recaudación: " + montoRegistrado + " - " + (cliente != null ? cliente.getNombreCompleto() : "Cliente");
    }
}
