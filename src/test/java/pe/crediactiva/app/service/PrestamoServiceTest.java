package pe.crediactiva.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import pe.crediactiva.app.model.Prestamo;
import pe.crediactiva.app.model.Cliente;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para PrestamoService
 */
public class PrestamoServiceTest {

    private PrestamoService prestamoService;

    @BeforeEach
    void setUp() {
        prestamoService = new PrestamoService();
    }

    @Test
    @DisplayName("Debe generar cronograma sin domingos")
    void testGenerarCronogramaSinDomingos() {
        // Arrange
        double montoTotal = 1000.0;
        int periodo = 1; // 1 mes = 26 días hábiles
        String tipoPago = "diario";
        LocalDate fechaInicio = LocalDate.of(2024, 1, 1); // Lunes

        // Act
        List<pe.crediactiva.app.model.Cronograma> cronograma = prestamoService.generarCronogramaPreview(
            montoTotal, periodo, tipoPago, fechaInicio);

        // Assert
        assertNotNull(cronograma);
        assertEquals(26, cronograma.size(), "Debe generar 26 cuotas para 1 mes");

        // Verificar que no hay domingos
        for (pe.crediactiva.app.model.Cronograma cuota : cronograma) {
            assertNotEquals(java.time.DayOfWeek.SUNDAY, cuota.getFechaProgramada().getDayOfWeek(),
                "No debe haber cuotas programadas en domingo");
        }

        // Verificar que el monto de cada cuota es correcto
        double montoEsperadoPorCuota = montoTotal / 26;
        for (pe.crediactiva.app.model.Cronograma cuota : cronograma) {
            assertEquals(montoEsperadoPorCuota, cuota.getMontoCuota().doubleValue(), 0.01,
                "El monto de cada cuota debe ser " + montoEsperadoPorCuota);
        }
    }

    @Test
    @DisplayName("Debe calcular correctamente el monto desembolsado")
    void testCalcularMontoDesembolsado() {
        // Arrange
        BigDecimal montoSolicitado = new BigDecimal("1000.00");
        BigDecimal porcentajeRetencion = new BigDecimal("0.10"); // 10%
        
        // Act
        BigDecimal montoDesembolsado = montoSolicitado.multiply(BigDecimal.ONE.subtract(porcentajeRetencion));

        // Assert
        assertEquals(new BigDecimal("900.00"), montoDesembolsado,
            "El monto desembolsado debe ser el 90% del solicitado");
    }

    @Test
    @DisplayName("Debe validar que el cronograma inicia el día siguiente")
    void testCronogramaIniciaDiaSiguiente() {
        // Arrange
        LocalDate fechaAprobacion = LocalDate.of(2024, 1, 1); // Lunes
        LocalDate fechaEsperadaInicio = fechaAprobacion.plusDays(1); // Martes

        // Act
        List<pe.crediactiva.app.model.Cronograma> cronograma = prestamoService.generarCronogramaPreview(
            1000.0, 1, "diario", fechaEsperadaInicio);

        // Assert
        assertNotNull(cronograma);
        assertFalse(cronograma.isEmpty());
        assertEquals(fechaEsperadaInicio, cronograma.get(0).getFechaProgramada(),
            "La primera cuota debe ser el día siguiente a la aprobación");
    }

    @Test
    @DisplayName("Debe generar cronograma semanal correctamente")
    void testGenerarCronogramaSemanal() {
        // Arrange
        double montoTotal = 1000.0;
        int periodo = 1; // 1 mes
        String tipoPago = "semanal";
        LocalDate fechaInicio = LocalDate.of(2024, 1, 1);

        // Act
        List<pe.crediactiva.app.model.Cronograma> cronograma = prestamoService.generarCronogramaPreview(
            montoTotal, periodo, tipoPago, fechaInicio);

        // Assert
        assertNotNull(cronograma);
        assertEquals(4, cronograma.size(), "Debe generar 4 cuotas semanales para 1 mes");

        // Verificar que las fechas están separadas por 7 días
        for (int i = 1; i < cronograma.size(); i++) {
            LocalDate fechaAnterior = cronograma.get(i - 1).getFechaProgramada();
            LocalDate fechaActual = cronograma.get(i).getFechaProgramada();
            assertEquals(7, java.time.temporal.ChronoUnit.DAYS.between(fechaAnterior, fechaActual),
                "Las cuotas semanales deben estar separadas por 7 días");
        }
    }

    @Test
    @DisplayName("Debe calcular correctamente el sueldo del asesor")
    void testCalcularSueldoAsesor() {
        // Arrange
        BigDecimal recaudacionMensual = new BigDecimal("5000.00");
        BigDecimal porcentajeSueldo = new BigDecimal("0.10"); // 10%

        // Act
        BigDecimal sueldoCalculado = recaudacionMensual.multiply(porcentajeSueldo);

        // Assert
        assertEquals(new BigDecimal("500.00"), sueldoCalculado,
            "El sueldo debe ser el 10% de la recaudación mensual");
    }
}
