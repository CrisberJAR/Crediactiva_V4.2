package pe.crediactiva.app.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Modelo para la entidad Préstamo
 */
public class Prestamo {
    private Long idPrestamo;
    private Long idCliente;
    private Long idAsesor;
    private BigDecimal montoSolicitado;
    private BigDecimal montoDesembolsado;
    private BigDecimal tasaInteres;
    private EstadoPrestamo estado;
    private EtiquetaPrestamo etiqueta;
    private int periodoMeses;
    private TipoPago tipoPago;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String observacion;
    private LocalDateTime creadoEn;
    
    // Campos de relación
    private Cliente cliente;
    private Asesor asesor;
    
    public enum EstadoPrestamo {
        PENDIENTE("Pendiente"),
        ACTIVO("Activo"),
        SUSPENDIDO("Suspendido"),
        FINALIZADO("Finalizado"),
        RECHAZADO("Rechazado");
        
        private final String descripcion;
        
        EstadoPrestamo(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
    }
    
    public enum EtiquetaPrestamo {
        PUNTUAL("Puntual"),
        MOROSO("Moroso"),
        PELIGROSO("Peligroso");
        
        private final String descripcion;
        
        EtiquetaPrestamo(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
    }
    
    public enum TipoPago {
        DIARIO("Diario"),
        SEMANAL("Semanal"),
        MENSUAL("Mensual");
        
        private final String descripcion;
        
        TipoPago(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
    }
    
    public Prestamo() {
        this.estado = EstadoPrestamo.PENDIENTE;
        this.etiqueta = EtiquetaPrestamo.PUNTUAL;
        this.periodoMeses = 1;
        this.tipoPago = TipoPago.DIARIO;
    }
    
    public Prestamo(Long idCliente, Long idAsesor, BigDecimal montoSolicitado, BigDecimal tasaInteres) {
        this();
        this.idCliente = idCliente;
        this.idAsesor = idAsesor;
        this.montoSolicitado = montoSolicitado;
        this.tasaInteres = tasaInteres;
        this.montoDesembolsado = calcularMontoDesembolsado();
    }
    
    /**
     * Calcula el monto desembolsado (monto solicitado - 10%)
     */
    public BigDecimal calcularMontoDesembolsado() {
        if (montoSolicitado == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal capitalRetenido = montoSolicitado.multiply(new BigDecimal("0.10"));
        return montoSolicitado.subtract(capitalRetenido);
    }
    
    /**
     * Calcula el monto total a pagar (monto solicitado + intereses)
     */
    public BigDecimal calcularMontoTotal() {
        if (montoSolicitado == null || tasaInteres == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal intereses = montoSolicitado.multiply(tasaInteres.divide(new BigDecimal("100")));
        return montoSolicitado.add(intereses);
    }
    
    /**
     * Calcula el monto de la cuota diaria
     */
    public BigDecimal calcularCuotaDiaria() {
        BigDecimal montoTotal = calcularMontoTotal();
        int diasHabiles = calcularDiasHabiles();
        if (diasHabiles == 0) {
            return BigDecimal.ZERO;
        }
        return montoTotal.divide(new BigDecimal(diasHabiles), 2, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * Calcula los días hábiles para el cronograma (excluyendo domingos)
     */
    private int calcularDiasHabiles() {
        if (periodoMeses == 1) {
            return 26; // 26 días hábiles por defecto para préstamos de 1 mes
        }
        return periodoMeses * 26; // Aproximación para períodos mayores
    }
    
    // Getters y Setters
    public Long getIdPrestamo() {
        return idPrestamo;
    }
    
    public void setIdPrestamo(Long idPrestamo) {
        this.idPrestamo = idPrestamo;
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
    
    public BigDecimal getMontoSolicitado() {
        return montoSolicitado;
    }
    
    public void setMontoSolicitado(BigDecimal montoSolicitado) {
        this.montoSolicitado = montoSolicitado;
    }
    
    public BigDecimal getMontoDesembolsado() {
        return montoDesembolsado;
    }
    
    public void setMontoDesembolsado(BigDecimal montoDesembolsado) {
        this.montoDesembolsado = montoDesembolsado;
    }
    
    public BigDecimal getTasaInteres() {
        return tasaInteres;
    }
    
    public void setTasaInteres(BigDecimal tasaInteres) {
        this.tasaInteres = tasaInteres;
    }
    
    public EstadoPrestamo getEstado() {
        return estado;
    }
    
    public void setEstado(EstadoPrestamo estado) {
        this.estado = estado;
    }
    
    public EtiquetaPrestamo getEtiqueta() {
        return etiqueta;
    }
    
    public void setEtiqueta(EtiquetaPrestamo etiqueta) {
        this.etiqueta = etiqueta;
    }
    
    public int getPeriodoMeses() {
        return periodoMeses;
    }
    
    public void setPeriodoMeses(int periodoMeses) {
        this.periodoMeses = periodoMeses;
    }
    
    public TipoPago getTipoPago() {
        return tipoPago;
    }
    
    public void setTipoPago(TipoPago tipoPago) {
        this.tipoPago = tipoPago;
    }
    
    public LocalDate getFechaInicio() {
        return fechaInicio;
    }
    
    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }
    
    public LocalDate getFechaFin() {
        return fechaFin;
    }
    
    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }
    
    public String getObservacion() {
        return observacion;
    }
    
    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }
    
    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }
    
    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
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
        return "Préstamo #" + idPrestamo + " - " + (cliente != null ? cliente.getNombreCompleto() : "Cliente");
    }
}
