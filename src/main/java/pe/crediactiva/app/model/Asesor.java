package pe.crediactiva.app.model;

import java.time.LocalDate;

/**
 * Modelo para la entidad Asesor
 */
public class Asesor {
    private Long idAsesor;
    private String nombre;
    private String apellido;
    private LocalDate fechaContrato;
    private String direccion;
    private String telefono;
    private String email;
    private boolean activo;
    
    public Asesor() {}
    
    public Asesor(Long idAsesor, String nombre, String apellido, LocalDate fechaContrato) {
        this.idAsesor = idAsesor;
        this.nombre = nombre;
        this.apellido = apellido;
        this.fechaContrato = fechaContrato;
        this.activo = true;
    }
    
    // Getters y Setters
    public Long getIdAsesor() {
        return idAsesor;
    }
    
    public void setIdAsesor(Long idAsesor) {
        this.idAsesor = idAsesor;
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
    
    public String getNombreCompleto() {
        return nombre + " " + apellido;
    }
    
    public LocalDate getFechaContrato() {
        return fechaContrato;
    }
    
    public void setFechaContrato(LocalDate fechaContrato) {
        this.fechaContrato = fechaContrato;
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
    
    public boolean isActivo() {
        return activo;
    }
    
    public void setActivo(boolean activo) {
        this.activo = activo;
    }
    
    @Override
    public String toString() {
        return getNombreCompleto();
    }
}
