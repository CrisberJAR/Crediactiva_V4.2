package pe.crediactiva.app.model;

/**
 * Modelo para la entidad Rol
 */
public class Rol {
    private int idRol;
    private String nombre;
    
    public Rol() {}
    
    public Rol(int idRol, String nombre) {
        this.idRol = idRol;
        this.nombre = nombre;
    }
    
    // Getters y Setters
    public int getIdRol() {
        return idRol;
    }
    
    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }
    
    public String getNombre() {
        return nombre;
    }
    
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    
    @Override
    public String toString() {
        return nombre;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Rol rol = (Rol) obj;
        return idRol == rol.idRol;
    }
    
    @Override
    public int hashCode() {
        return Integer.hashCode(idRol);
    }
}
