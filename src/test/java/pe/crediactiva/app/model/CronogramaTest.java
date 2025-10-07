package pe.crediactiva.app.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para el modelo Cronograma
 */
public class CronogramaTest {

    private Cronograma cronograma;

    @BeforeEach
    void setUp() {
        cronograma = new Cronograma();
        cronograma.setIdCuota(1L);
        cronograma.setIdPrestamo(1L);
        cronograma.setNumeroCuota(1);
        cronograma.setFechaProgramada(LocalDate.of(2024, 1, 1));
        cronograma.setMontoCuota(new BigDecimal("100.00"));
        cronograma.setEstadoCuota(Cronograma.EstadoCuota.PENDIENTE);
    }

    @Test
    @DisplayName("Debe identificar cuota vencida correctamente")
    void testIsVencida() {
        // Arrange - Cuota con fecha pasada
        cronograma.setFechaProgramada(LocalDate.now().minusDays(1));
        cronograma.setEstadoCuota(Cronograma.EstadoCuota.PENDIENTE);

        // Act & Assert
        assertTrue(cronograma.isVencida(), "Debe identificar cuota vencida");

        // Arrange - Cuota pagada no debe ser vencida
        cronograma.setEstadoCuota(Cronograma.EstadoCuota.PAGADA);

        // Act & Assert
        assertFalse(cronograma.isVencida(), "Cuota pagada no debe ser vencida");
    }

    @Test
    @DisplayName("Debe identificar cuota próxima a vencer")
    void testIsProximaVencer() {
        // Arrange - Cuota que vence en 2 días
        cronograma.setFechaProgramada(LocalDate.now().plusDays(2));
        cronograma.setEstadoCuota(Cronograma.EstadoCuota.PENDIENTE);

        // Act & Assert
        assertTrue(cronograma.isProximaVencer(), "Debe identificar cuota próxima a vencer");

        // Arrange - Cuota que vence en 5 días
        cronograma.setFechaProgramada(LocalDate.now().plusDays(5));

        // Act & Assert
        assertFalse(cronograma.isProximaVencer(), "No debe identificar cuota que vence en 5 días");

        // Arrange - Cuota pagada no debe ser próxima a vencer
        cronograma.setFechaProgramada(LocalDate.now().plusDays(1));
        cronograma.setEstadoCuota(Cronograma.EstadoCuota.PAGADA);

        // Act & Assert
        assertFalse(cronograma.isProximaVencer(), "Cuota pagada no debe ser próxima a vencer");
    }

    @Test
    @DisplayName("Debe calcular días de atraso correctamente")
    void testGetDiasAtraso() {
        // Arrange - Cuota vencida hace 5 días
        cronograma.setFechaProgramada(LocalDate.now().minusDays(5));
        cronograma.setEstadoCuota(Cronograma.EstadoCuota.PENDIENTE);

        // Act
        int diasAtraso = cronograma.getDiasAtraso();

        // Assert
        assertEquals(5, diasAtraso, "Debe calcular 5 días de atraso");

        // Arrange - Cuota pagada no debe tener atraso
        cronograma.setEstadoCuota(Cronograma.EstadoCuota.PAGADA);

        // Act
        diasAtraso = cronograma.getDiasAtraso();

        // Assert
        assertEquals(0, diasAtraso, "Cuota pagada no debe tener días de atraso");

        // Arrange - Cuota no vencida no debe tener atraso
        cronograma.setFechaProgramada(LocalDate.now().plusDays(1));
        cronograma.setEstadoCuota(Cronograma.EstadoCuota.PENDIENTE);

        // Act
        diasAtraso = cronograma.getDiasAtraso();

        // Assert
        assertEquals(0, diasAtraso, "Cuota no vencida no debe tener días de atraso");
    }

    @Test
    @DisplayName("Debe manejar estado de cuota correctamente")
    void testEstadoCuota() {
        // Test enum values
        assertEquals("Pendiente", Cronograma.EstadoCuota.PENDIENTE.getDescripcion());
        assertEquals("Pagada", Cronograma.EstadoCuota.PAGADA.getDescripcion());
        assertEquals("Retrasada", Cronograma.EstadoCuota.RETRASADA.getDescripcion());

        // Test setting and getting
        cronograma.setEstadoCuota(Cronograma.EstadoCuota.RETRASADA);
        assertEquals(Cronograma.EstadoCuota.RETRASADA, cronograma.getEstadoCuota());
    }

    @Test
    @DisplayName("Debe manejar campo seleccionado")
    void testSeleccionado() {
        // Arrange & Act
        cronograma.setSeleccionado(true);

        // Assert
        assertTrue(cronograma.isSeleccionado(), "Debe poder marcar cuota como seleccionada");

        // Arrange & Act
        cronograma.setSeleccionado(false);

        // Assert
        assertFalse(cronograma.isSeleccionado(), "Debe poder desmarcar cuota");
    }

    @Test
    @DisplayName("Debe crear cronograma con constructor")
    void testConstructorConParametros() {
        // Arrange
        Long idPrestamo = 1L;
        int numeroCuota = 5;
        LocalDate fechaProgramada = LocalDate.of(2024, 1, 15);
        BigDecimal montoCuota = new BigDecimal("150.00");

        // Act
        Cronograma nuevaCuota = new Cronograma(idPrestamo, numeroCuota, fechaProgramada, montoCuota);

        // Assert
        assertEquals(idPrestamo, nuevaCuota.getIdPrestamo());
        assertEquals(numeroCuota, nuevaCuota.getNumeroCuota());
        assertEquals(fechaProgramada, nuevaCuota.getFechaProgramada());
        assertEquals(montoCuota, nuevaCuota.getMontoCuota());
        assertEquals(Cronograma.EstadoCuota.PENDIENTE, nuevaCuota.getEstadoCuota());
    }

    @Test
    @DisplayName("Debe retornar toString correctamente")
    void testToString() {
        // Arrange
        cronograma.setNumeroCuota(3);
        cronograma.setFechaProgramada(LocalDate.of(2024, 1, 15));
        cronograma.setMontoCuota(new BigDecimal("125.50"));

        // Act
        String toString = cronograma.toString();

        // Assert
        assertTrue(toString.contains("Cuota #3"));
        assertTrue(toString.contains("2024-01-15"));
        assertTrue(toString.contains("125.50"));
    }
}
