package pe.crediactiva.app.util;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * Utilidad para diagnosticar problemas de fecha y hora
 */
public class DateTimeDiagnostic {
    
    public static void printDateTimeInfo() {
        System.out.println("=== DIAGNÓSTICO DE FECHA Y HORA ===");
        
        // Fecha y hora local
        LocalDateTime now = LocalDateTime.now();
        System.out.println("LocalDateTime.now(): " + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        
        // Zona horaria del sistema
        ZoneId systemZone = ZoneId.systemDefault();
        System.out.println("Zona horaria del sistema: " + systemZone);
        
        // Zona horaria de Java
        TimeZone javaTimeZone = TimeZone.getDefault();
        System.out.println("TimeZone de Java: " + javaTimeZone.getID());
        System.out.println("Offset de TimeZone: " + javaTimeZone.getRawOffset() / (1000 * 60 * 60) + " horas");
        
        // Fecha y hora con zona horaria
        ZonedDateTime zonedNow = ZonedDateTime.now();
        System.out.println("ZonedDateTime.now(): " + zonedNow.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")));
        
        // Fecha y hora específica para Perú
        ZonedDateTime peruTime = ZonedDateTime.now(ZoneId.of("America/Lima"));
        System.out.println("Hora de Perú: " + peruTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z")));
        
        System.out.println("=====================================");
    }
    
    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now();
    }
    
    public static LocalDateTime getCurrentDateTimePeru() {
        return ZonedDateTime.now(ZoneId.of("America/Lima")).toLocalDateTime();
    }
}
