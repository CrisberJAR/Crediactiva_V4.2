package pe.crediactiva.app.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Modelo para la entidad Cliente
 */
public class Cliente {
    private Long idCliente;
    private String nombre;
    private String apellido;
    private String dni;
    private LocalDate fechaRegistro;
    private LocalDate fechaNacimiento;
    private String sexo;
    private String direccion;
    private String telefono;
    private String email;
    private String ocupacion;
    private String lugarTrabajo;
    private Long idAsesor;
    private BigDecimal saldoCapital;
    private EtiquetaCliente etiquetaCliente;
    private boolean activo;
    
    // Campos de relación
    private Asesor asesor;
    
    public enum EtiquetaCliente {
        EXCELENTE("Excelente"),
        DEFICIENTE("Deficiente"),
        PELIGROSO("Peligroso");
        
        private final String descripcion;
        
        EtiquetaCliente(String descripcion) {
            this.descripcion = descripcion;
        }
        
        public String getDescripcion() {
            return descripcion;
        }
    }
    
    public Cliente() {
        this.saldoCapital = BigDecimal.ZERO;
        this.etiquetaCliente = EtiquetaCliente.EXCELENTE;
        this.activo = true;
    }
    
    public Cliente(Long idCliente, String nombre, String apellido, LocalDate fechaRegistro, Long idAsesor) {
        this();
        this.idCliente = idCliente;
        this.nombre = nombre;
        this.apellido = apellido;
        this.fechaRegistro = fechaRegistro;
        this.idAsesor = idAsesor;
    }
    
    // Getters y Setters
    public Long getIdCliente() {
        return idCliente;
    }
    
    public void setIdCliente(Long idCliente) {
        this.idCliente = idCliente;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    public String getApellido() {
        return apellido;
    }
    
    public void setApellido(String apellido) {
        this.apellido = apellido;
    }
    
    public String getDni() {
        return dni;
    }
    
    public void setDni(String dni) {
        this.dni = dni;
    }
    
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
    
    public LocalDate getFechaRegistro() {
        return fechaRegistro;
    }
    
    public void setFechaRegistro(LocalDate fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }
    
    public String getDireccion() {
        return direccion;
    }
    
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    
    public String getTelefono() {
        return telefono;
    }
    
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public Long getIdAsesor() {
        return idAsesor;
    }
    
    public void setIdAsesor(Long idAsesor) {
        this.idAsesor = idAsesor;
    }
    
    public BigDecimal getSaldoCapital() {
        return saldoCapital;
    }
    
    public void setSaldoCapital(BigDecimal saldoCapital) {
        this.saldoCapital = saldoCapital;
    }
    
    public EtiquetaCliente getEtiquetaCliente() {
        return etiquetaCliente;
    }
    
    public void setEtiquetaCliente(EtiquetaCliente etiquetaCliente) {
        this.etiquetaCliente = etiquetaCliente;
    }
    
    public boolean isActivo() {
        return activo;
    }
    
    public void setActivo(boolean activo) {
        this.activo = activo;
    }
    
    public Asesor getAsesor() {
        return asesor;
    }
    
    public void setAsesor(Asesor asesor) {
        this.asesor = asesor;
    }
    
    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }
    
    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }
    
    public String getSexo() {
        return sexo;
    }
    
    public void setSexo(String sexo) {
        this.sexo = sexo;
    }
    
    public String getOcupacion() {
        return ocupacion;
    }
    
    public void setOcupacion(String ocupacion) {
        this.ocupacion = ocupacion;
    }
    
    public String getLugarTrabajo() {
        return lugarTrabajo;
    }
    
    public void setLugarTrabajo(String lugarTrabajo) {
        this.lugarTrabajo = lugarTrabajo;
    }
    
    @Override
    public String toString() {
        return getNombreCompleto();
    }
}
