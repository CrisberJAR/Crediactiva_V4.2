package pe.crediactiva.app.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Utilidades para manejo de fechas
 */
public class FechaUtil {
    
    private static final DateTimeFormatter FORMATTER_DD_MM_YYYY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATTER_YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * Formatea una fecha como dd/MM/yyyy
     */
    public static String formatearFecha(LocalDate fecha) {
        if (fecha == null) {
            return "";
        }
        return fecha.format(FORMATTER_DD_MM_YYYY);
    }
    
    /**
     * Formatea una fecha como yyyy-MM-dd
     */
    public static String formatearFechaISO(LocalDate fecha) {
        if (fecha == null) {
            return "";
        }
        return fecha.format(FORMATTER_YYYY_MM_DD);
    }
    
    /**
     * Verifica si una fecha es domingo
     */
    public static boolean esDomingo(LocalDate fecha) {
        return fecha.getDayOfWeek().getValue() == 7;
    }
    
    /**
     * Calcula los días hábiles entre dos fechas (excluyendo domingos)
     */
    public static int calcularDiasHabiles(LocalDate fechaInicio, LocalDate fechaFin) {
        int diasHabiles = 0;
        LocalDate fechaActual = fechaInicio;
        
        while (!fechaActual.isAfter(fechaFin)) {
            if (!esDomingo(fechaActual)) {
                diasHabiles++;
            }
            fechaActual = fechaActual.plusDays(1);
        }
        
        return diasHabiles;
    }
    
    /**
     * Obtiene la fecha de mañana
     */
    public static LocalDate obtenerManana() {
        return LocalDate.now().plusDays(1);
    }
    
    /**
     * Verifica si una fecha está en el pasado
     */
    public static boolean esFechaPasada(LocalDate fecha) {
        return fecha.isBefore(LocalDate.now());
    }
    
    /**
     * Verifica si una fecha está en el futuro
     */
    public static boolean esFechaFutura(LocalDate fecha) {
        return fecha.isAfter(LocalDate.now());
    }
    
    /**
     * Obtiene la fecha actual
     */
    public static LocalDate obtenerFechaActual() {
        return LocalDate.now();
    }
    
    /**
     * Verifica si una fecha es domingo (método alternativo)
     */
    public static boolean isDomingo(LocalDate fecha) {
        return esDomingo(fecha);
    }
    
    /**
     * Formatea una fecha y hora en formato dd/MM/yyyy HH:mm:ss
     */
    public static String formatearFechaHora(LocalDateTime fechaHora) {
        if (fechaHora == null) {
            return "";
        }
        return fechaHora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }
    
    /**
     * Obtiene el primer día del mes
     */
    public static LocalDate primerDiaDelMes(LocalDate fecha) {
        return fecha.withDayOfMonth(1);
    }
    
    /**
     * Obtiene el último día del mes
     */
    public static LocalDate ultimoDiaDelMes(LocalDate fecha) {
        return fecha.withDayOfMonth(fecha.lengthOfMonth());
    }
    
    /**
     * Calcula la diferencia en días entre dos fechas
     */
    public static long diferenciaEnDias(LocalDate fechaInicio, LocalDate fechaFin) {
        return ChronoUnit.DAYS.between(fechaInicio, fechaFin);
    }
    
    /**
     * Verifica si una fecha está en el rango especificado
     */
    public static boolean estaEnRango(LocalDate fecha, LocalDate fechaInicio, LocalDate fechaFin) {
        return !fecha.isBefore(fechaInicio) && !fecha.isAfter(fechaFin);
    }
    
    /**
     * Obtiene el nombre del día de la semana en español
     */
    public static String nombreDiaSemana(LocalDate fecha) {
        if (fecha == null) {
            return "";
        }
        
        switch (fecha.getDayOfWeek()) {
            case MONDAY: return "Lunes";
            case TUESDAY: return "Martes";
            case WEDNESDAY: return "Miércoles";
            case THURSDAY: return "Jueves";
            case FRIDAY: return "Viernes";
            case SATURDAY: return "Sábado";
            case SUNDAY: return "Domingo";
            default: return "";
        }
    }
    
    /**
     * Obtiene el nombre del mes en español
     */
    public static String nombreMes(LocalDate fecha) {
        if (fecha == null) {
            return "";
        }
        
        switch (fecha.getMonth()) {
            case JANUARY: return "Enero";
            case FEBRUARY: return "Febrero";
            case MARCH: return "Marzo";
            case APRIL: return "Abril";
            case MAY: return "Mayo";
            case JUNE: return "Junio";
            case JULY: return "Julio";
            case AUGUST: return "Agosto";
            case SEPTEMBER: return "Septiembre";
            case OCTOBER: return "Octubre";
            case NOVEMBER: return "Noviembre";
            case DECEMBER: return "Diciembre";
            default: return "";
        }
    }
}
