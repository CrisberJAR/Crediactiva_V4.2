package pe.crediactiva.app.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Modelo para la entidad Cronograma
 */
public class Cronograma {
    private Long idCuota;
    private Long idPrestamo;
    private int numeroCuota;
    private LocalDate fechaProgramada;
    private BigDecimal montoCuota;
    private EstadoCuota estadoCuota;
    private LocalDate fechaPagoReal;
    private boolean seleccionado;
    
    // Campos de relación
    private Prestamo prestamo;
    
    public enum EstadoCuota {
        PENDIENTE("Pendiente"),
        PAGADA("Pagada"),
        RETRASADA("Retrasada");
        
        private final String descripcion;
        
        EstadoCuota(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
    }
    
    public Cronograma() {
        this.estadoCuota = EstadoCuota.PENDIENTE;
    }
    
    public Cronograma(Long idPrestamo, int numeroCuota, LocalDate fechaProgramada, BigDecimal montoCuota) {
        this();
        this.idPrestamo = idPrestamo;
        this.numeroCuota = numeroCuota;
        this.fechaProgramada = fechaProgramada;
        this.montoCuota = montoCuota;
    }
    
    /**
     * Verifica si la cuota está vencida
     */
    public boolean isVencida() {
        if (estadoCuota == EstadoCuota.PAGADA) {
            return false;
        }
        return LocalDate.now().isAfter(fechaProgramada);
    }
    
    /**
     * Verifica si la cuota está próxima a vencer (dentro de 3 días)
     */
    public boolean isProximaVencer() {
        if (estadoCuota == EstadoCuota.PAGADA) {
            return false;
        }
        LocalDate hoy = LocalDate.now();
        LocalDate limite = fechaProgramada.minusDays(3);
        return hoy.isAfter(limite) && !hoy.isAfter(fechaProgramada);
    }
    
    /**
     * Calcula los días de atraso
     */
    public int getDiasAtraso() {
        if (estadoCuota == EstadoCuota.PAGADA || !isVencida()) {
            return 0;
        }
        return (int) (LocalDate.now().toEpochDay() - fechaProgramada.toEpochDay());
    }
    
    // Getters y Setters
    public Long getIdCuota() {
        return idCuota;
    }
    
    public void setIdCuota(Long idCuota) {
        this.idCuota = idCuota;
    }
    
    public Long getIdPrestamo() {
        return idPrestamo;
    }
    
    public void setIdPrestamo(Long idPrestamo) {
        this.idPrestamo = idPrestamo;
    }
    
    public int getNumeroCuota() {
        return numeroCuota;
    }
    
    public void setNumeroCuota(int numeroCuota) {
        this.numeroCuota = numeroCuota;
    }
    
    public LocalDate getFechaProgramada() {
        return fechaProgramada;
    }
    
    public void setFechaProgramada(LocalDate fechaProgramada) {
        this.fechaProgramada = fechaProgramada;
    }
    
    public BigDecimal getMontoCuota() {
        return montoCuota;
    }
    
    public void setMontoCuota(BigDecimal montoCuota) {
        this.montoCuota = montoCuota;
    }
    
    public EstadoCuota getEstadoCuota() {
        return estadoCuota;
    }
    
    public void setEstadoCuota(EstadoCuota estadoCuota) {
        this.estadoCuota = estadoCuota;
    }
    
    public LocalDate getFechaPagoReal() {
        return fechaPagoReal;
    }
    
    public void setFechaPagoReal(LocalDate fechaPagoReal) {
        this.fechaPagoReal = fechaPagoReal;
    }
    
    public Prestamo getPrestamo() {
        return prestamo;
    }
    
    public void setPrestamo(Prestamo prestamo) {
        this.prestamo = prestamo;
    }
    
    public boolean isSeleccionado() {
        return seleccionado;
    }
    
    public void setSeleccionado(boolean seleccionado) {
        this.seleccionado = seleccionado;
    }
    
    @Override
    public String toString() {
        return "Cuota #" + numeroCuota + " - " + fechaProgramada + " - " + montoCuota;
    }
}
