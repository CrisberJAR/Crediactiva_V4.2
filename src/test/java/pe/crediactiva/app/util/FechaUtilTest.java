package pe.crediactiva.app.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para FechaUtil
 */
public class FechaUtilTest {

    @Test
    @DisplayName("Debe identificar correctamente los domingos")
    void testIsDomingo() {
        // Arrange
        LocalDate domingo = LocalDate.of(2024, 1, 7); // Domingo
        LocalDate lunes = LocalDate.of(2024, 1, 8); // Lunes

        // Act & Assert
        assertTrue(FechaUtil.isDomingo(domingo), "Debe identificar el domingo correctamente");
        assertFalse(FechaUtil.isDomingo(lunes), "No debe identificar el lunes como domingo");
    }

    @Test
    @DisplayName("Debe formatear fechas correctamente")
    void testFormatearFecha() {
        // Arrange
        LocalDate fecha = LocalDate.of(2024, 1, 15);

        // Act
        String fechaFormateada = FechaUtil.formatearFecha(fecha);

        // Assert
        assertEquals("15/01/2024", fechaFormateada, "Debe formatear la fecha en formato dd/MM/yyyy");
    }

    @Test
    @DisplayName("Debe formatear fechas ISO correctamente")
    void testFormatearFechaISO() {
        // Arrange
        LocalDate fecha = LocalDate.of(2024, 1, 15);

        // Act
        String fechaISO = FechaUtil.formatearFechaISO(fecha);

        // Assert
        assertEquals("2024-01-15", fechaISO, "Debe formatear la fecha en formato ISO");
    }

    @Test
    @DisplayName("Debe calcular días hábiles correctamente")
    void testCalcularDiasHabiles() {
        // Arrange
        LocalDate fechaInicio = LocalDate.of(2024, 1, 1); // Lunes
        LocalDate fechaFin = LocalDate.of(2024, 1, 7); // Domingo

        // Act
        int diasHabiles = FechaUtil.calcularDiasHabiles(fechaInicio, fechaFin);

        // Assert
        assertEquals(5, diasHabiles, "Debe calcular 5 días hábiles (lunes a viernes)");
    }

    @Test
    @DisplayName("Debe obtener el primer día del mes")
    void testPrimerDiaDelMes() {
        // Arrange
        LocalDate fecha = LocalDate.of(2024, 1, 15);

        // Act
        LocalDate primerDia = FechaUtil.primerDiaDelMes(fecha);

        // Assert
        assertEquals(LocalDate.of(2024, 1, 1), primerDia, "Debe retornar el primer día del mes");
    }

    @Test
    @DisplayName("Debe obtener el último día del mes")
    void testUltimoDiaDelMes() {
        // Arrange
        LocalDate fecha = LocalDate.of(2024, 1, 15);

        // Act
        LocalDate ultimoDia = FechaUtil.ultimoDiaDelMes(fecha);

        // Assert
        assertEquals(LocalDate.of(2024, 1, 31), ultimoDia, "Debe retornar el último día del mes");
    }

    @Test
    @DisplayName("Debe calcular diferencia en días")
    void testDiferenciaEnDias() {
        // Arrange
        LocalDate fechaInicio = LocalDate.of(2024, 1, 1);
        LocalDate fechaFin = LocalDate.of(2024, 1, 10);

        // Act
        long diferencia = FechaUtil.diferenciaEnDias(fechaInicio, fechaFin);

        // Assert
        assertEquals(9, diferencia, "Debe calcular 9 días de diferencia");
    }

    @Test
    @DisplayName("Debe verificar si una fecha está en rango")
    void testEstaEnRango() {
        // Arrange
        LocalDate fechaInicio = LocalDate.of(2024, 1, 1);
        LocalDate fechaFin = LocalDate.of(2024, 1, 31);
        LocalDate fechaEnRango = LocalDate.of(2024, 1, 15);
        LocalDate fechaFueraRango = LocalDate.of(2024, 2, 1);

        // Act & Assert
        assertTrue(FechaUtil.estaEnRango(fechaEnRango, fechaInicio, fechaFin),
            "Debe identificar fecha dentro del rango");
        assertFalse(FechaUtil.estaEnRango(fechaFueraRango, fechaInicio, fechaFin),
            "Debe identificar fecha fuera del rango");
    }

    @Test
    @DisplayName("Debe obtener nombre del día de la semana")
    void testNombreDiaSemana() {
        // Arrange
        LocalDate lunes = LocalDate.of(2024, 1, 1);
        LocalDate domingo = LocalDate.of(2024, 1, 7);

        // Act & Assert
        assertEquals("Lunes", FechaUtil.nombreDiaSemana(lunes), "Debe retornar 'Lunes'");
        assertEquals("Domingo", FechaUtil.nombreDiaSemana(domingo), "Debe retornar 'Domingo'");
    }

    @Test
    @DisplayName("Debe obtener nombre del mes")
    void testNombreMes() {
        // Arrange
        LocalDate enero = LocalDate.of(2024, 1, 15);
        LocalDate diciembre = LocalDate.of(2024, 12, 15);

        // Act & Assert
        assertEquals("Enero", FechaUtil.nombreMes(enero), "Debe retornar 'Enero'");
        assertEquals("Diciembre", FechaUtil.nombreMes(diciembre), "Debe retornar 'Diciembre'");
    }

    @Test
    @DisplayName("Debe manejar fechas nulas correctamente")
    void testFechasNulas() {
        // Act & Assert
        assertFalse(FechaUtil.isDomingo(null), "Debe manejar fecha nula en isDomingo");
        assertEquals("", FechaUtil.formatearFecha(null), "Debe retornar string vacío para fecha nula");
        assertEquals("", FechaUtil.nombreDiaSemana(null), "Debe retornar string vacío para fecha nula");
        assertEquals("", FechaUtil.nombreMes(null), "Debe retornar string vacío para fecha nula");
    }
}
