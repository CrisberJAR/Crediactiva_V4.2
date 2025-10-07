package pe.crediactiva.app.view.asesor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Controlador para el simulador de crédito del asesor
 */
public class SimuladorController {
    
    private static final Logger logger = LoggerFactory.getLogger(SimuladorController.class);
    
    @FXML
    private TextField txtMonto;
    
    @FXML
    private ComboBox<String> cmbPlazo;
    
    @FXML
    private ComboBox<String> cmbTipoPago;
    
    @FXML
    private TextField txtTasaInteres;
    
    @FXML
    private Label lblCuotaPeriodo;
    
    @FXML
    private Label lblTotalPagar;
    
    @FXML
    private Label lblInteresesTotales;
    
    @FXML
    private Label lblTasaEfectivaAnual;
    
    @FXML
    private TableView<CronogramaSimulacion> tblCronograma;
    
    @FXML
    private TableColumn<CronogramaSimulacion, Integer> colCuota;
    
    @FXML
    private TableColumn<CronogramaSimulacion, String> colFechaPago;
    
    @FXML
    private TableColumn<CronogramaSimulacion, String> colSaldoInicial;
    
    @FXML
    private TableColumn<CronogramaSimulacion, String> colCuotaMonto;
    
    @FXML
    private TableColumn<CronogramaSimulacion, String> colInteres;
    
    @FXML
    private TableColumn<CronogramaSimulacion, String> colAmortizacion;
    
    @FXML
    private TableColumn<CronogramaSimulacion, String> colSaldoFinal;
    
    @FXML
    private Label lblPorcentajeCapital;
    
    @FXML
    private Label lblPorcentajeIntereses;
    
    @FXML
    private TableView<EscenarioComparacion> tblComparacion;
    
    @FXML
    private TableColumn<EscenarioComparacion, String> colEscenario;
    
    @FXML
    private TableColumn<EscenarioComparacion, String> colCuotaComparacion;
    
    @FXML
    private TableColumn<EscenarioComparacion, String> colTotalComparacion;
    
    @FXML
    private TableColumn<EscenarioComparacion, String> colInteresesComparacion;
    
    @FXML
    private TableColumn<EscenarioComparacion, String> colAhorroComparacion;
    
    private ObservableList<CronogramaSimulacion> cronograma;
    private ObservableList<EscenarioComparacion> comparacion;
    
    public SimuladorController() {
        this.cronograma = FXCollections.observableArrayList();
        this.comparacion = FXCollections.observableArrayList();
    }
    
    @FXML
    private void initialize() {
        try {
            configurarControles();
            configurarTablas();
            
        } catch (Exception e) {
            logger.error("Error al inicializar simulador", e);
            mostrarError("Error al inicializar el simulador");
        }
    }
    
    /**
     * Configura los controles del formulario
     */
    private void configurarControles() {
        // Configurar combo de plazo
        cmbPlazo.getItems().addAll("3", "6", "9", "12", "18", "24", "36");
        
        // Configurar combo de tipo de pago
        cmbTipoPago.getItems().addAll("MENSUAL", "QUINCENAL", "SEMANAL");
        cmbTipoPago.setValue("MENSUAL");
        
        // Configurar validación de monto
        txtMonto.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                txtMonto.setText(oldValue);
            }
        });
        
        // Configurar validación de tasa de interés
        txtTasaInteres.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                txtTasaInteres.setText(oldValue);
            }
        });
    }
    
    /**
     * Configura las tablas
     */
    private void configurarTablas() {
        // Configurar tabla de cronograma
        colCuota.setCellValueFactory(new PropertyValueFactory<>("numeroCuota"));
        colFechaPago.setCellValueFactory(new PropertyValueFactory<>("fechaPago"));
        colSaldoInicial.setCellValueFactory(new PropertyValueFactory<>("saldoInicial"));
        colCuotaMonto.setCellValueFactory(new PropertyValueFactory<>("cuotaMonto"));
        colInteres.setCellValueFactory(new PropertyValueFactory<>("interes"));
        colAmortizacion.setCellValueFactory(new PropertyValueFactory<>("amortizacion"));
        colSaldoFinal.setCellValueFactory(new PropertyValueFactory<>("saldoFinal"));
        
        tblCronograma.setItems(cronograma);
        
        // Configurar tabla de comparación
        colEscenario.setCellValueFactory(new PropertyValueFactory<>("escenario"));
        colCuotaComparacion.setCellValueFactory(new PropertyValueFactory<>("cuota"));
        colTotalComparacion.setCellValueFactory(new PropertyValueFactory<>("totalPagar"));
        colInteresesComparacion.setCellValueFactory(new PropertyValueFactory<>("intereses"));
        colAhorroComparacion.setCellValueFactory(new PropertyValueFactory<>("ahorro"));
        
        tblComparacion.setItems(comparacion);
    }
    
    /**
     * Maneja el cálculo de la simulación
     */
    @FXML
    private void handleCalcular() {
        if (!validarDatos()) {
            return;
        }
        
        try {
            BigDecimal monto = new BigDecimal(txtMonto.getText());
            int plazo = Integer.parseInt(cmbPlazo.getValue());
            String tipoPago = cmbTipoPago.getValue();
            double tasaInteres = Double.parseDouble(txtTasaInteres.getText()) / 100;
            
            // Calcular cuota
            BigDecimal cuota = calcularCuota(monto, tasaInteres, plazo);
            lblCuotaPeriodo.setText("S/ " + String.format("%.2f", cuota));
            
            // Calcular total a pagar
            BigDecimal totalPagar = cuota.multiply(BigDecimal.valueOf(plazo));
            lblTotalPagar.setText("S/ " + String.format("%.2f", totalPagar));
            
            // Calcular intereses totales
            BigDecimal interesesTotales = totalPagar.subtract(monto);
            lblInteresesTotales.setText("S/ " + String.format("%.2f", interesesTotales));
            
            // Calcular tasa efectiva anual
            double tasaEfectivaAnual = Math.pow(1 + tasaInteres, 12) - 1;
            lblTasaEfectivaAnual.setText(String.format("%.2f%%", tasaEfectivaAnual * 100));
            
            // Generar cronograma
            generarCronograma(monto, cuota, tasaInteres, plazo);
            
            // Calcular distribución
            calcularDistribucion(monto, interesesTotales);
            
            logger.info("Simulación calculada - Monto: " + monto + ", Plazo: " + plazo + " meses");
            
        } catch (Exception e) {
            logger.error("Error al calcular simulación", e);
            mostrarError("Error al calcular la simulación");
        }
    }
    
    /**
     * Valida los datos de entrada
     */
    private boolean validarDatos() {
        if (txtMonto.getText().trim().isEmpty()) {
            mostrarAdvertencia("Por favor ingrese el monto del préstamo");
            return false;
        }
        
        if (cmbPlazo.getValue() == null) {
            mostrarAdvertencia("Por favor seleccione el plazo del préstamo");
            return false;
        }
        
        if (txtTasaInteres.getText().trim().isEmpty()) {
            mostrarAdvertencia("Por favor ingrese la tasa de interés");
            return false;
        }
        
        try {
            BigDecimal monto = new BigDecimal(txtMonto.getText());
            if (monto.compareTo(BigDecimal.ZERO) <= 0) {
                mostrarAdvertencia("El monto debe ser mayor a cero");
                return false;
            }
            
            double tasa = Double.parseDouble(txtTasaInteres.getText());
            if (tasa < 0 || tasa > 100) {
                mostrarAdvertencia("La tasa de interés debe estar entre 0 y 100%");
                return false;
            }
            
        } catch (NumberFormatException e) {
            mostrarAdvertencia("Por favor ingrese valores numéricos válidos");
            return false;
        }
        
        return true;
    }
    
    /**
     * Calcula la cuota usando la fórmula de cuota fija
     */
    private BigDecimal calcularCuota(BigDecimal monto, double tasaInteres, int plazo) {
        double tasaMensual = tasaInteres / 12;
        double factor = Math.pow(1 + tasaMensual, plazo);
        double cuota = monto.doubleValue() * (tasaMensual * factor) / (factor - 1);
        
        return BigDecimal.valueOf(cuota).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Genera el cronograma de pagos
     */
    private void generarCronograma(BigDecimal monto, BigDecimal cuota, double tasaInteres, int plazo) {
        cronograma.clear();
        
        BigDecimal saldoInicial = monto;
        LocalDate fechaPago = LocalDate.now().plusMonths(1);
        
        for (int i = 1; i <= plazo; i++) {
            BigDecimal interes = saldoInicial.multiply(BigDecimal.valueOf(tasaInteres / 12));
            BigDecimal amortizacion = cuota.subtract(interes);
            BigDecimal saldoFinal = saldoInicial.subtract(amortizacion);
            
            // Ajustar la última cuota para que el saldo final sea exactamente cero
            if (i == plazo) {
                cuota = saldoInicial.add(interes);
                amortizacion = saldoInicial;
                saldoFinal = BigDecimal.ZERO;
            }
            
            CronogramaSimulacion fila = new CronogramaSimulacion(
                i,
                fechaPago.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                "S/ " + String.format("%.2f", saldoInicial),
                "S/ " + String.format("%.2f", cuota),
                "S/ " + String.format("%.2f", interes),
                "S/ " + String.format("%.2f", amortizacion),
                "S/ " + String.format("%.2f", saldoFinal)
            );
            
            cronograma.add(fila);
            
            saldoInicial = saldoFinal;
            fechaPago = fechaPago.plusMonths(1);
        }
    }
    
    /**
     * Calcula la distribución del pago
     */
    private void calcularDistribucion(BigDecimal monto, BigDecimal interesesTotales) {
        BigDecimal total = monto.add(interesesTotales);
        
        double porcentajeCapital = monto.divide(total, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100)).doubleValue();
        double porcentajeIntereses = interesesTotales.divide(total, 4, RoundingMode.HALF_UP)
            .multiply(BigDecimal.valueOf(100)).doubleValue();
        
        lblPorcentajeCapital.setText(String.format("%.1f%%", porcentajeCapital));
        lblPorcentajeIntereses.setText(String.format("%.1f%%", porcentajeIntereses));
    }
    
    /**
     * Maneja la comparación de escenarios
     */
    @FXML
    private void handleCompararEscenarios() {
        if (!validarDatos()) {
            return;
        }
        
        try {
            BigDecimal monto = new BigDecimal(txtMonto.getText());
            double tasaInteres = Double.parseDouble(txtTasaInteres.getText()) / 100;
            
            comparacion.clear();
            
            // Escenario actual
            int plazoActual = Integer.parseInt(cmbPlazo.getValue());
            BigDecimal cuotaActual = calcularCuota(monto, tasaInteres, plazoActual);
            BigDecimal totalActual = cuotaActual.multiply(BigDecimal.valueOf(plazoActual));
            BigDecimal interesesActual = totalActual.subtract(monto);
            
            comparacion.add(new EscenarioComparacion(
                "Escenario Actual (" + plazoActual + " meses)",
                "S/ " + String.format("%.2f", cuotaActual),
                "S/ " + String.format("%.2f", totalActual),
                "S/ " + String.format("%.2f", interesesActual),
                "S/ 0.00"
            ));
            
            // Escenarios alternativos
            int[] plazosAlternativos = {6, 12, 18, 24};
            for (int plazo : plazosAlternativos) {
                if (plazo != plazoActual) {
                    BigDecimal cuota = calcularCuota(monto, tasaInteres, plazo);
                    BigDecimal total = cuota.multiply(BigDecimal.valueOf(plazo));
                    BigDecimal intereses = total.subtract(monto);
                    BigDecimal ahorro = interesesActual.subtract(intereses);
                    
                    comparacion.add(new EscenarioComparacion(
                        "Escenario " + plazo + " meses",
                        "S/ " + String.format("%.2f", cuota),
                        "S/ " + String.format("%.2f", total),
                        "S/ " + String.format("%.2f", intereses),
                        "S/ " + String.format("%.2f", ahorro)
                    ));
                }
            }
            
        } catch (Exception e) {
            logger.error("Error al comparar escenarios", e);
            mostrarError("Error al comparar escenarios");
        }
    }
    
    /**
     * Maneja la exportación de la simulación
     */
    @FXML
    private void handleExportarSimulacion() {
        try {
            // TODO: Implementar exportación a Excel/PDF
            mostrarInfo("Funcionalidad de exportación en desarrollo");
            
        } catch (Exception e) {
            logger.error("Error al exportar simulación", e);
            mostrarError("Error al exportar la simulación");
        }
    }
    
    /**
     * Maneja el guardado de la simulación
     */
    @FXML
    private void handleGuardarSimulacion() {
        if (!validarDatos()) {
            return;
        }
        
        try {
            // TODO: Implementar guardado de simulación
            mostrarInfo("Funcionalidad de guardado de simulación en desarrollo");
            
        } catch (Exception e) {
            logger.error("Error al guardar simulación", e);
            mostrarError("Error al guardar la simulación");
        }
    }
    
    /**
     * Maneja la limpieza del formulario
     */
    @FXML
    private void handleLimpiar() {
        txtMonto.clear();
        cmbPlazo.setValue(null);
        cmbTipoPago.setValue("MENSUAL");
        txtTasaInteres.clear();
        
        // Limpiar resultados
        lblCuotaPeriodo.setText("S/ 0.00");
        lblTotalPagar.setText("S/ 0.00");
        lblInteresesTotales.setText("S/ 0.00");
        lblTasaEfectivaAnual.setText("0.00%");
        lblPorcentajeCapital.setText("0%");
        lblPorcentajeIntereses.setText("0%");
        
        // Limpiar tablas
        cronograma.clear();
        comparacion.clear();
        
        mostrarInfo("Formulario limpiado");
    }
    
    /**
     * Clase para representar una fila del cronograma
     */
    public static class CronogramaSimulacion {
        private int numeroCuota;
        private String fechaPago;
        private String saldoInicial;
        private String cuotaMonto;
        private String interes;
        private String amortizacion;
        private String saldoFinal;
        
        public CronogramaSimulacion(int numeroCuota, String fechaPago, String saldoInicial, 
                                   String cuotaMonto, String interes, String amortizacion, String saldoFinal) {
            this.numeroCuota = numeroCuota;
            this.fechaPago = fechaPago;
            this.saldoInicial = saldoInicial;
            this.cuotaMonto = cuotaMonto;
            this.interes = interes;
            this.amortizacion = amortizacion;
            this.saldoFinal = saldoFinal;
        }
        
        // Getters
        public int getNumeroCuota() { return numeroCuota; }
        public String getFechaPago() { return fechaPago; }
        public String getSaldoInicial() { return saldoInicial; }
        public String getCuotaMonto() { return cuotaMonto; }
        public String getInteres() { return interes; }
        public String getAmortizacion() { return amortizacion; }
        public String getSaldoFinal() { return saldoFinal; }
    }
    
    /**
     * Clase para representar un escenario de comparación
     */
    public static class EscenarioComparacion {
        private String escenario;
        private String cuota;
        private String totalPagar;
        private String intereses;
        private String ahorro;
        
        public EscenarioComparacion(String escenario, String cuota, String totalPagar, 
                                  String intereses, String ahorro) {
            this.escenario = escenario;
            this.cuota = cuota;
            this.totalPagar = totalPagar;
            this.intereses = intereses;
            this.ahorro = ahorro;
        }
        
        // Getters
        public String getEscenario() { return escenario; }
        public String getCuota() { return cuota; }
        public String getTotalPagar() { return totalPagar; }
        public String getIntereses() { return intereses; }
        public String getAhorro() { return ahorro; }
    }
    
    /**
     * Muestra un mensaje de información
     */
    private void mostrarInfo(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    /**
     * Muestra un mensaje de advertencia
     */
    private void mostrarAdvertencia(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Advertencia");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    /**
     * Muestra un mensaje de error
     */
    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
