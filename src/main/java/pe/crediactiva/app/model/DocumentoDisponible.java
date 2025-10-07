package pe.crediactiva.app.model;

import java.math.BigDecimal;

/**
 * Modelo para representar un documento disponible para descarga
 */
public class DocumentoDisponible {
    private String tipoDocumento;
    private String numeroPrestamo;
    private String fechaDocumento;
    private BigDecimal monto;
    private String estado;
    private boolean seleccionado;
    
    public DocumentoDisponible() {
        this.seleccionado = false;
    }
    
    public DocumentoDisponible(String tipoDocumento, String numeroPrestamo, 
                             String fechaDocumento, BigDecimal monto, String estado) {
        this.tipoDocumento = tipoDocumento;
        this.numeroPrestamo = numeroPrestamo;
        this.fechaDocumento = fechaDocumento;
        this.monto = monto;
        this.estado = estado;
        this.seleccionado = false;
    }
    
    // Getters
    public String getTipoDocumento() { return tipoDocumento; }
    public String getNumeroPrestamo() { return numeroPrestamo; }
    public String getFechaDocumento() { return fechaDocumento; }
    public BigDecimal getMonto() { return monto; }
    public String getEstado() { return estado; }
    public boolean isSeleccionado() { return seleccionado; }
    
    // Setters
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public void setNumeroPrestamo(String numeroPrestamo) { this.numeroPrestamo = numeroPrestamo; }
    public void setFechaDocumento(String fechaDocumento) { this.fechaDocumento = fechaDocumento; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setSeleccionado(boolean seleccionado) { this.seleccionado = seleccionado; }
    
    @Override
    public String toString() {
        return "DocumentoDisponible{" +
                "tipoDocumento='" + tipoDocumento + '\'' +
                ", numeroPrestamo='" + numeroPrestamo + '\'' +
                ", fechaDocumento='" + fechaDocumento + '\'' +
                ", monto=" + monto +
                ", estado='" + estado + '\'' +
                ", seleccionado=" + seleccionado +
                '}';
    }
}
