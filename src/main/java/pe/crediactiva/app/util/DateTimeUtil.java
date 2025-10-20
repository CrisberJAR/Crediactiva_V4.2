package pe.crediactiva.app.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utilidad centralizada para manejo de fechas y horas con zona horaria de Perú
 */
public class DateTimeUtil {
    
    private static final ZoneId PERU_ZONE = ZoneId.of("America/Lima");
    
    /**
     * Obtiene la fecha y hora actual de Perú
     */
    public static LocalDateTime now() {
        return ZonedDateTime.now(PERU_ZONE).toLocalDateTime();
    }
    
    /**
     * Obtiene la fecha y hora actual de Perú como ZonedDateTime
     */
    public static ZonedDateTime nowZoned() {
        return ZonedDateTime.now(PERU_ZONE);
    }
    
    /**
     * Formatea una fecha y hora con el patrón estándar
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }
    
    /**
     * Formatea una fecha y hora con el patrón para base de datos
     */
    public static String formatDateTimeForDB(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
    
    /**
     * Obtiene la fecha actual de Perú
     */
    public static java.time.LocalDate today() {
        return now().toLocalDate();
    }
    
    /**
     * Convierte LocalDateTime a Timestamp para base de datos
     */
    public static java.sql.Timestamp toTimestamp(LocalDateTime dateTime) {
        return java.sql.Timestamp.valueOf(dateTime);
    }
    
    /**
     * Obtiene la fecha y hora actual como Timestamp para base de datos
     */
    public static java.sql.Timestamp nowAsTimestamp() {
        return toTimestamp(now());
    }
    
    /**
     * Imprime información de diagnóstico de fecha y hora
     */
    public static void printDiagnostic() {
        LocalDateTime systemNow = LocalDateTime.now();
        LocalDateTime peruNow = now();
        ZonedDateTime peruZoned = nowZoned();
        
        System.out.println("=== DIAGNÓSTICO DE FECHA Y HORA ===");
        System.out.println("Sistema LocalDateTime.now(): " + systemNow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("Perú LocalDateTime: " + peruNow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("Perú ZonedDateTime: " + peruZoned.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")));
        System.out.println("Zona horaria del sistema: " + ZoneId.systemDefault());
        System.out.println("Zona horaria de Perú: " + PERU_ZONE);
        System.out.println("=====================================");
    }
}
