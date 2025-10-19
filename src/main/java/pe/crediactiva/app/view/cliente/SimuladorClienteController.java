package pe.crediactiva.app.view.cliente;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pe.crediactiva.app.model.Prestamo;
import pe.crediactiva.app.service.AuthenticationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para la pantalla de simulador de crédito del cliente
 */
public class SimuladorClienteController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(SimuladorClienteController.class);
    
    @FXML
    private TextField txtMonto;
    @FXML
    private TextField txtTasaInteres;
    @FXML
    private ComboBox<Integer> comboPeriodo;
    @FXML
    private ComboBox<Prestamo.TipoPago> comboTipoPago;
    @FXML
    private DatePicker dateFechaInicio;
    @FXML
    private TextField txtObservacion;
    
    @FXML
    private Button btnSimular;
    @FXML
    private Button btnLimpiar;
    @FXML
    private Button btnExportarCronograma;
    @FXML
    private Button btnVolver;
    
    @FXML
    private VBox panelResultados;
    @FXML
    private VBox panelCronograma;
    
    @FXML
    private Label lblMontoSolicitado;
    @FXML
    private Label lblCapitalRetenido;
    @FXML
    private Label lblMontoDesembolsado;
    @FXML
    private Label lblMontoTotalPagar;
    @FXML
    private Label lblNumeroCuotas;
    @FXML
    private Label lblMontoCuota;
    @FXML
    private Label lblInteresTotal;
    @FXML
    private Label lblFechaFinalizacion;
    
    @FXML
    private TableView<CronogramaSimulado> tablaCronogramaSimulado;
    @FXML
    private TableColumn<CronogramaSimulado, Integer> colSimNumeroCuota;
    @FXML
    private TableColumn<CronogramaSimulado, String> colSimFechaProgramada;
    @FXML
    private TableColumn<CronogramaSimulado, BigDecimal> colSimMontoCuota;
    @FXML
    private TableColumn<CronogramaSimulado, BigDecimal> colSimSaldoRestante;
    
    private Stage primaryStage;
    private AuthenticationService authService;
    private ObservableList<CronogramaSimulado> cronogramaSimuladoData;
    private DateTimeFormatter dateFormatter;
    
    public SimuladorClienteController() {
        this.authService = new AuthenticationService();
        this.dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    }
    
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            configurarControles();
            configurarTabla();
            establecerValoresPorDefecto();
            
            logger.info("Simulador de crédito inicializado correctamente");
            
        } catch (Exception e) {
            logger.error("Error al inicializar el simulador de crédito", e);
            mostrarError("Error al inicializar el simulador");
        }
    }
    
    /**
     * Configura los controles de la interfaz
     */
    private void configurarControles() {
        // Configurar ComboBox de períodos
        ObservableList<Integer> periodos = FXCollections.observableArrayList(1, 2, 3, 6, 12, 18, 24);
        comboPeriodo.setItems(periodos);
        
        // Configurar ComboBox de tipos de pago
        ObservableList<Prestamo.TipoPago> tiposPago = FXCollections.observableArrayList(
            Prestamo.TipoPago.DIARIO,
            Prestamo.TipoPago.SEMANAL,
            Prestamo.TipoPago.MENSUAL
        );
        comboTipoPago.setItems(tiposPago);
        
        // Configurar validación de entrada
        txtMonto.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                txtMonto.setText(oldValue);
            }
        });
        
        txtTasaInteres.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                txtTasaInteres.setText(oldValue);
            }
        });
    }
    
    /**
     * Configura la tabla del cronograma simulado
     */
    private void configurarTabla() {
        logger.info("Configurando tabla del cronograma");
        
        colSimNumeroCuota.setCellValueFactory(new PropertyValueFactory<>("numeroCuota"));
        colSimFechaProgramada.setCellValueFactory(new PropertyValueFactory<>("fechaProgramada"));
        
        colSimMontoCuota.setCellValueFactory(new PropertyValueFactory<>("montoCuota"));
        colSimMontoCuota.setCellFactory(column -> new TableCell<CronogramaSimulado, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("S/ " + String.format("%.2f", item.doubleValue()));
                }
            }
        });
        
        colSimSaldoRestante.setCellValueFactory(new PropertyValueFactory<>("saldoRestante"));
        colSimSaldoRestante.setCellFactory(column -> new TableCell<CronogramaSimulado, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("S/ " + String.format("%.2f", item.doubleValue()));
                }
            }
        });
        
        logger.info("Tabla del cronograma configurada correctamente");
    }
    
    /**
     * Establece valores por defecto
     */
    private void establecerValoresPorDefecto() {
        dateFechaInicio.setValue(LocalDate.now().plusDays(1));
        comboPeriodo.setValue(1);
        comboTipoPago.setValue(Prestamo.TipoPago.DIARIO);
        txtTasaInteres.setText("14.40");
    }
    
    /**
     * Maneja el evento de calcular automático
     */
    @FXML
    private void handleCalcular() {
        if (esFormularioValido()) {
            simularPrestamo();
        }
    }
    
    /**
     * Maneja la simulación del préstamo
     */
    @FXML
    private void handleSimular() {
        if (!esFormularioValido()) {
            mostrarError("Por favor complete todos los campos obligatorios");
            return;
        }
        
        simularPrestamo();
        mostrarInfo("Simulación completada exitosamente");
    }
    
    /**
     * Simula el préstamo con los parámetros ingresados
     */
    private void simularPrestamo() {
        try {
            // Validar campos obligatorios
            if (txtMonto.getText().trim().isEmpty() || txtTasaInteres.getText().trim().isEmpty() || 
                comboPeriodo.getValue() == null || comboTipoPago.getValue() == null || 
                dateFechaInicio.getValue() == null) {
                mostrarError("Todos los campos son obligatorios para la simulación.");
                return;
            }
            
            // Validar monto
            BigDecimal montoSolicitado;
            try {
                montoSolicitado = new BigDecimal(txtMonto.getText().trim());
                if (montoSolicitado.compareTo(BigDecimal.ZERO) <= 0) {
                    mostrarError("El monto debe ser mayor a 0.");
                    return;
                }
            } catch (NumberFormatException e) {
                mostrarError("El monto debe ser un número válido.");
                return;
            }
            
            // Validar interés
            BigDecimal tasaInteres;
            try {
                tasaInteres = new BigDecimal(txtTasaInteres.getText().trim());
                if (tasaInteres.compareTo(BigDecimal.ZERO) < 0 || tasaInteres.compareTo(new BigDecimal("100")) > 0) {
                    mostrarError("La tasa de interés debe estar entre 0 y 100.");
                    return;
                }
            } catch (NumberFormatException e) {
                mostrarError("La tasa de interés debe ser un número válido.");
                return;
            }
            
            int periodo = comboPeriodo.getValue();
            Prestamo.TipoPago tipoPago = comboTipoPago.getValue();
            LocalDate fechaInicio = dateFechaInicio.getValue();
            
            // Calcular capital retenido (10%)
            BigDecimal capitalRetenido = montoSolicitado.multiply(new BigDecimal("0.10"));
            
            // Calcular monto desembolsado
            BigDecimal montoDesembolsado = montoSolicitado.subtract(capitalRetenido);
            
            // Calcular monto total a pagar
            BigDecimal montoTotalPagar = montoSolicitado.multiply(
                BigDecimal.ONE.add(tasaInteres.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP))
            );
            
            // Calcular interés total
            BigDecimal interesTotal = montoTotalPagar.subtract(montoSolicitado);
            
            // Calcular número de cuotas según el tipo de pago
            int numeroCuotas = calcularNumeroCuotas(periodo, tipoPago);
            
            // Calcular monto por cuota
            BigDecimal montoCuota = montoTotalPagar.divide(
                new BigDecimal(numeroCuotas), 2, RoundingMode.HALF_UP
            );
            
            // Redondear siempre a favor (hacia arriba) - igual que en el asesor
            double valorCuotaDouble = montoCuota.doubleValue();
            valorCuotaDouble = Math.ceil(valorCuotaDouble * 10.0) / 10.0;
            montoCuota = new BigDecimal(valorCuotaDouble).setScale(2, RoundingMode.HALF_UP);
            
            // Calcular fecha de finalización
            LocalDate fechaFinalizacion = calcularFechaFinalizacion(fechaInicio, numeroCuotas, tipoPago);
            
            // Mostrar resultados
            mostrarResultados(montoSolicitado, capitalRetenido, montoDesembolsado, 
                            montoTotalPagar, numeroCuotas, montoCuota, interesTotal, fechaFinalizacion);
            
            // Generar cronograma simulado
            logger.info("Iniciando generación de cronograma con " + numeroCuotas + " cuotas");
            generarCronogramaSimulado(montoTotalPagar, montoCuota, numeroCuotas, fechaInicio, tipoPago);
            
        } catch (Exception e) {
            logger.error("Error al simular préstamo", e);
            mostrarError("Error al realizar la simulación");
        }
    }
    
    /**
     * Calcula el número de cuotas según el período y tipo de pago
     */
    private int calcularNumeroCuotas(int periodo, Prestamo.TipoPago tipoPago) {
        switch (tipoPago) {
            case DIARIO:
                return periodo * 26; // 26 días hábiles por mes
            case SEMANAL:
                return periodo * 4; // 4 semanas por mes
            case MENSUAL:
                return periodo;
            default:
                return periodo * 26;
        }
    }
    
    /**
     * Calcula la fecha de finalización del préstamo
     */
    private LocalDate calcularFechaFinalizacion(LocalDate fechaInicio, int numeroCuotas, Prestamo.TipoPago tipoPago) {
        LocalDate fecha = fechaInicio;
        int cuotasAgregadas = 0;
        
        while (cuotasAgregadas < numeroCuotas) {
            fecha = fecha.plusDays(1);
            
            // Excluir domingos para pagos diarios
            if (tipoPago == Prestamo.TipoPago.DIARIO) {
                if (fecha.getDayOfWeek().getValue() != 7) { // No es domingo
                    cuotasAgregadas++;
                }
            } else if (tipoPago == Prestamo.TipoPago.SEMANAL) {
                if (cuotasAgregadas == 0 || cuotasAgregadas % 7 == 0) {
                    cuotasAgregadas++;
                }
            } else { // MENSUAL
                if (cuotasAgregadas == 0 || fecha.getDayOfMonth() == fechaInicio.getDayOfMonth()) {
                    cuotasAgregadas++;
                }
            }
        }
        
        return fecha;
    }
    
    /**
     * Muestra los resultados de la simulación
     */
    private void mostrarResultados(BigDecimal montoSolicitado, BigDecimal capitalRetenido, 
                                 BigDecimal montoDesembolsado, BigDecimal montoTotalPagar, 
                                 int numeroCuotas, BigDecimal montoCuota, BigDecimal interesTotal, 
                                 LocalDate fechaFinalizacion) {
        
        lblMontoSolicitado.setText("S/ " + String.format("%.2f", montoSolicitado.doubleValue()));
        lblCapitalRetenido.setText("S/ " + String.format("%.2f", capitalRetenido.doubleValue()));
        lblMontoDesembolsado.setText("S/ " + String.format("%.2f", montoDesembolsado.doubleValue()));
        lblMontoTotalPagar.setText("S/ " + String.format("%.2f", montoTotalPagar.doubleValue()));
        lblNumeroCuotas.setText(String.valueOf(numeroCuotas));
        lblMontoCuota.setText("S/ " + String.format("%.2f", montoCuota.doubleValue()));
        lblInteresTotal.setText("S/ " + String.format("%.2f", interesTotal.doubleValue()));
        lblFechaFinalizacion.setText(fechaFinalizacion.format(dateFormatter));
        
        panelResultados.setVisible(true);
    }
    
    /**
     * Genera el cronograma simulado - Mejorado con la lógica del asesor
     */
    private void generarCronogramaSimulado(BigDecimal montoTotal, BigDecimal montoCuota, 
                                         int numeroCuotas, LocalDate fechaInicio, Prestamo.TipoPago tipoPago) {
        
        List<CronogramaSimulado> cronograma = new ArrayList<>();
        LocalDate fechaPago = fechaInicio;
        BigDecimal saldoRestante = montoTotal;
        
        for (int i = 1; i <= numeroCuotas; i++) {
            // Calcular fecha de pago según tipo - usando la misma lógica del asesor
            switch (tipoPago) {
                case DIARIO:
                    // Buscar el siguiente día hábil (excluyendo domingos)
                    do {
                        fechaPago = fechaPago.plusDays(1);
                    } while (fechaPago.getDayOfWeek() == java.time.DayOfWeek.SUNDAY);
                    break;
                case SEMANAL:
                    // Cada 7 días desde la fecha de inicio
                    fechaPago = fechaInicio.plusWeeks(i - 1);
                    break;
                case MENSUAL:
                    // Cada mes desde la fecha de inicio
                    fechaPago = fechaInicio.plusMonths(i - 1);
                    break;
            }
            
            // Calcular monto de esta cuota
            BigDecimal montoCuotaActual = montoCuota;
            if (i == numeroCuotas) {
                // Última cuota: ajustar para que el saldo quede en 0
                montoCuotaActual = saldoRestante;
            }
            
            // Actualizar saldo restante
            saldoRestante = saldoRestante.subtract(montoCuotaActual);
            if (saldoRestante.compareTo(BigDecimal.ZERO) < 0) {
                saldoRestante = BigDecimal.ZERO;
            }
            
            // Crear entrada del cronograma
            CronogramaSimulado cuota = new CronogramaSimulado(
                i,
                fechaPago.format(dateFormatter),
                montoCuotaActual,
                saldoRestante
            );
            cronograma.add(cuota);
            
            // Para pagos diarios, avanzar un día para la siguiente iteración
            if (tipoPago == Prestamo.TipoPago.DIARIO) {
                fechaPago = fechaPago.plusDays(1);
            }
        }
        
        cronogramaSimuladoData = FXCollections.observableArrayList(cronograma);
        tablaCronogramaSimulado.setItems(cronogramaSimuladoData);
        
        // Debug: verificar que se generó el cronograma
        logger.info("Cronograma generado con " + cronograma.size() + " cuotas");
        logger.info("Primera cuota: " + (cronograma.isEmpty() ? "vacío" : cronograma.get(0).getFechaProgramada()));
        
        panelCronograma.setVisible(true);
        
        // Forzar actualización de la tabla
        tablaCronogramaSimulado.refresh();
    }
    
    /**
     * Maneja la exportación del cronograma a PDF
     */
    @FXML
    private void handleExportarCronograma() {
        // TODO: Implementar exportación a PDF
        mostrarInfo("Funcionalidad de exportación a PDF próximamente disponible");
    }
    
    /**
     * Maneja la limpieza del formulario
     */
    @FXML
    private void handleLimpiar() {
        txtMonto.clear();
        txtTasaInteres.setText("14.40");
        comboPeriodo.setValue(1);
        comboTipoPago.setValue(Prestamo.TipoPago.DIARIO);
        dateFechaInicio.setValue(LocalDate.now().plusDays(1));
        txtObservacion.clear();
        
        panelResultados.setVisible(false);
        panelCronograma.setVisible(false);
        
        mostrarInfo("Formulario limpiado");
    }
    
    /**
     * Maneja el evento de volver
     */
    @FXML
    private void handleVolver() {
        try {
            // Cerrar la ventana actual y volver a la principal
            primaryStage.close();
            
            // Crear una nueva ventana principal del cliente
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cliente/ClienteMainView.fxml"));
            Stage newStage = new Stage();
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            ClienteMainController controller = loader.getController();
            controller.setPrimaryStage(newStage);
            
            newStage.setTitle("CrediActiva - Cliente");
            newStage.setScene(scene);
            newStage.setMaximized(true);
            newStage.show();
            
        } catch (Exception e) {
            logger.error("Error al volver a la pantalla principal", e);
            mostrarError("Error al volver a la pantalla principal");
        }
    }
    
    /**
     * Valida que el formulario esté completo
     */
    private boolean esFormularioValido() {
        return txtMonto.getText() != null && !txtMonto.getText().trim().isEmpty() &&
               txtTasaInteres.getText() != null && !txtTasaInteres.getText().trim().isEmpty() &&
               comboPeriodo.getValue() != null &&
               comboTipoPago.getValue() != null &&
               dateFechaInicio.getValue() != null;
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
     * Muestra un mensaje de error
     */
    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
    
    /**
     * Clase interna para representar una cuota simulada
     */
    public static class CronogramaSimulado {
        private int numeroCuota;
        private String fechaProgramada;
        private BigDecimal montoCuota;
        private BigDecimal saldoRestante;
        
        public CronogramaSimulado(int numeroCuota, String fechaProgramada, 
                                BigDecimal montoCuota, BigDecimal saldoRestante) {
            this.numeroCuota = numeroCuota;
            this.fechaProgramada = fechaProgramada;
            this.montoCuota = montoCuota;
            this.saldoRestante = saldoRestante;
        }
        
        // Getters
        public int getNumeroCuota() { return numeroCuota; }
        public String getFechaProgramada() { return fechaProgramada; }
        public BigDecimal getMontoCuota() { return montoCuota; }
        public BigDecimal getSaldoRestante() { return saldoRestante; }
        
        // Setters
        public void setNumeroCuota(int numeroCuota) { this.numeroCuota = numeroCuota; }
        public void setFechaProgramada(String fechaProgramada) { this.fechaProgramada = fechaProgramada; }
        public void setMontoCuota(BigDecimal montoCuota) { this.montoCuota = montoCuota; }
        public void setSaldoRestante(BigDecimal saldoRestante) { this.saldoRestante = saldoRestante; }
    }
}
