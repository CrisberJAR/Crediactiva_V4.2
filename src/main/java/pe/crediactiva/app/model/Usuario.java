package pe.crediactiva.app.model;

import java.time.LocalDateTime;

/**
 * Modelo para la entidad Usuario
 */
public class Usuario {
    private Long idUsuario;
    private String passwordHash;
    private int idRol;
    private LocalDateTime creadoEn;
    private boolean activo;
    
    // Campos de relación
    private Rol rol;
    
    public Usuario() {}
    
    public Usuario(Long idUsuario, String passwordHash, int idRol) {
        this.idUsuario = idUsuario;
        this.passwordHash = passwordHash;
        this.idRol = idRol;
        this.activo = true;
    }
    
    // Getters y Setters
    public Long getIdUsuario() {
        return idUsuario;
    }
    
    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public int getIdRol() {
        return idRol;
    }
    
    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }
    
    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }
    
    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }
    
    public boolean isActivo() {
        return activo;
    }
    
    public void setActivo(boolean activo) {
        this.activo = activo;
    }
    
    public Rol getRol() {
        return rol;
    }
    
    public void setRol(Rol rol) {
        this.rol = rol;
    }
    
    @Override
    public String toString() {
        return "Usuario{" +
                "idUsuario=" + idUsuario +
                ", idRol=" + idRol +
                ", activo=" + activo +
                '}';
    }
}
