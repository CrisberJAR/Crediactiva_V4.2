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
import pe.crediactiva.app.model.Pago;
import pe.crediactiva.app.model.Prestamo;
import pe.crediactiva.app.service.AuthenticationService;
import pe.crediactiva.app.service.PagoService;
import pe.crediactiva.app.service.PrestamoService;
import pe.crediactiva.app.util.FechaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para la pantalla de historial del cliente
 */
public class HistorialClienteController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(HistorialClienteController.class);
    
    @FXML
    private ComboBox<String> comboEstado;
    @FXML
    private DatePicker dateDesde;
    @FXML
    private DatePicker dateHasta;
    @FXML
    private TextField txtMontoMinimo;
    
    @FXML
    private Button btnBuscar;
    @FXML
    private Button btnLimpiar;
    @FXML
    private Button btnVolver;
    
    @FXML
    private Label lblTotalPrestamos;
    @FXML
    private Label lblPrestamosPagados;
    @FXML
    private Label lblTotalPagado;
    @FXML
    private Label lblCapitalAcumulado;
    @FXML
    private Label lblTotalResultados;
    @FXML
    private Label lblUltimaActualizacion;
    
    @FXML
    private TableView<Prestamo> tablaHistorial;
    @FXML
    private TableColumn<Prestamo, String> colNumeroPrestamo;
    @FXML
    private TableColumn<Prestamo, BigDecimal> colMontoSolicitado;
    @FXML
    private TableColumn<Prestamo, BigDecimal> colTasaInteres;
    @FXML
    private TableColumn<Prestamo, String> colEstado;
    @FXML
    private TableColumn<Prestamo, String> colFechaInicio;
    @FXML
    private TableColumn<Prestamo, String> colFechaFin;
    @FXML
    private TableColumn<Prestamo, String> colEtiqueta;
    @FXML
    private TableColumn<Prestamo, Void> colAcciones;
    
    @FXML
    private TableView<Pago> tablaPagos;
    @FXML
    private TableColumn<Pago, String> colFechaPago;
    @FXML
    private TableColumn<Pago, Integer> colNumeroCuota;
    @FXML
    private TableColumn<Pago, BigDecimal> colMontoPagado;
    @FXML
    private TableColumn<Pago, String> colPrestamoPago;
    @FXML
    private TableColumn<Pago, String> colEstadoPago;
    
    private Stage primaryStage;
    private AuthenticationService authService;
    private PrestamoService prestamoService;
    private PagoService pagoService;
    private ObservableList<Prestamo> historialData;
    private ObservableList<Pago> pagosData;
    private DateTimeFormatter dateFormatter;
    
    public HistorialClienteController() {
        this.authService = new AuthenticationService();
        this.prestamoService = new PrestamoService();
        this.pagoService = new PagoService();
        this.dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    }
    
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            configurarControles();
            configurarTablas();
            cargarDatosIniciales();
            establecerValoresPorDefecto();
            
            logger.info("Pantalla de historial del cliente inicializada correctamente");
            
        } catch (Exception e) {
            logger.error("Error al inicializar la pantalla de historial", e);
            mostrarError("Error al inicializar la pantalla");
        }
    }
    
    /**
     * Configura los controles de la interfaz
     */
    private void configurarControles() {
        // Configurar ComboBox de estados
        ObservableList<String> estados = FXCollections.observableArrayList(
            "Todos",
            "Pendiente",
            "Activo",
            "Suspendido",
            "Finalizado",
            "Rechazado"
        );
        comboEstado.setItems(estados);
        comboEstado.setValue("Todos");
        
        // Configurar DatePickers
        dateDesde.setValue(LocalDate.now().minusYears(1));
        dateHasta.setValue(LocalDate.now());
        
        // Configurar validación de entrada
        txtMontoMinimo.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*(\\.\\d*)?")) {
                txtMontoMinimo.setText(oldValue);
            }
        });
    }
    
    /**
     * Configura las tablas
     */
    private void configurarTablas() {
        // Tabla de historial de préstamos
        colNumeroPrestamo.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty("PR-" + cellData.getValue().getIdPrestamo()));
        
        colMontoSolicitado.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getMontoSolicitado()));
        colMontoSolicitado.setCellFactory(column -> new TableCell<Prestamo, BigDecimal>() {
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
        
        colTasaInteres.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getTasaInteres()));
        colTasaInteres.setCellFactory(column -> new TableCell<Prestamo, BigDecimal>() {
            @Override
            protected void updateItem(BigDecimal item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f%%", item.doubleValue()));
                }
            }
        });
        
        colEstado.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEstado().name()));
        
        colFechaInicio.setCellValueFactory(cellData -> {
            LocalDate fecha = cellData.getValue().getFechaInicio();
            return fecha != null ? 
                new javafx.beans.property.SimpleStringProperty(fecha.format(dateFormatter)) :
                new javafx.beans.property.SimpleStringProperty("--");
        });
        
        colFechaFin.setCellValueFactory(cellData -> {
            LocalDate fecha = cellData.getValue().getFechaFin();
            return fecha != null ? 
                new javafx.beans.property.SimpleStringProperty(fecha.format(dateFormatter)) :
                new javafx.beans.property.SimpleStringProperty("--");
        });
        
        colEtiqueta.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEtiqueta().name()));
        
        // Columna de acciones
        colAcciones.setCellFactory(column -> new TableCell<Prestamo, Void>() {
            private final Button btnVerDetalle = new Button("Ver Detalle");
            
            {
                btnVerDetalle.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                btnVerDetalle.setOnAction(event -> {
                    Prestamo prestamo = getTableView().getItems().get(getIndex());
                    verDetallePrestamo(prestamo);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnVerDetalle);
                }
            }
        });
        
        // Tabla de pagos
        colFechaPago.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getFechaPago().format(dateFormatter)
            ));
        
        colNumeroCuota.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getIdCuota().intValue()).asObject());
        
        colMontoPagado.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getMontoPagado()));
        colMontoPagado.setCellFactory(column -> new TableCell<Pago, BigDecimal>() {
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
        
        colPrestamoPago.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty("PR-" + cellData.getValue().getIdCuota()));
        
        colEstadoPago.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty("Pagado"));
    }
    
    /**
     * Carga los datos iniciales
     */
    private void cargarDatosIniciales() {
        try {
            Long idCliente = authService.getCurrentUser().getIdUsuario();
            
            // Cargar historial de préstamos
            cargarHistorialPrestamos(idCliente);
            
            // Cargar historial de pagos
            cargarHistorialPagos(idCliente);
            
            // Actualizar estadísticas
            actualizarEstadisticas();
            
        } catch (Exception e) {
            logger.error("Error al cargar datos iniciales", e);
            mostrarError("Error al cargar los datos del historial");
        }
    }
    
    /**
     * Establece valores por defecto
     */
    private void establecerValoresPorDefecto() {
        actualizarUltimaActualizacion();
    }
    
    /**
     * Carga el historial de préstamos
     */
    private void cargarHistorialPrestamos(Long idCliente) {
        try {
            List<Prestamo> prestamos = prestamoService.obtenerPrestamosPorCliente(idCliente);
            
            // Aplicar filtros
            prestamos = aplicarFiltrosPrestamos(prestamos);
            
            historialData = FXCollections.observableArrayList(prestamos);
            tablaHistorial.setItems(historialData);
            
            lblTotalResultados.setText("Total: " + prestamos.size() + " préstamos encontrados");
            
        } catch (Exception e) {
            logger.error("Error al cargar historial de préstamos", e);
            mostrarError("Error al cargar el historial de préstamos");
        }
    }
    
    /**
     * Carga el historial de pagos
     */
    private void cargarHistorialPagos(Long idCliente) {
        try {
            List<Pago> pagos = pagoService.obtenerPagosPorCliente(idCliente);
            
            pagosData = FXCollections.observableArrayList(pagos);
            tablaPagos.setItems(pagosData);
            
        } catch (Exception e) {
            logger.error("Error al cargar historial de pagos", e);
            // No mostrar error aquí porque es común que no haya pagos
        }
    }
    
    /**
     * Aplica filtros a la lista de préstamos
     */
    private List<Prestamo> aplicarFiltrosPrestamos(List<Prestamo> prestamos) {
        return prestamos.stream()
            .filter(p -> {
                // Filtro por estado
                if (!comboEstado.getValue().equals("Todos")) {
                    if (!p.getEstado().name().equalsIgnoreCase(comboEstado.getValue())) {
                        return false;
                    }
                }
                
                // Filtro por fecha desde
                if (dateDesde.getValue() != null && p.getFechaInicio() != null) {
                    if (p.getFechaInicio().isBefore(dateDesde.getValue())) {
                        return false;
                    }
                }
                
                // Filtro por fecha hasta
                if (dateHasta.getValue() != null && p.getFechaInicio() != null) {
                    if (p.getFechaInicio().isAfter(dateHasta.getValue())) {
                        return false;
                    }
                }
                
                // Filtro por monto mínimo
                if (!txtMontoMinimo.getText().trim().isEmpty()) {
                    try {
                        BigDecimal montoMinimo = new BigDecimal(txtMontoMinimo.getText());
                        if (p.getMontoSolicitado().compareTo(montoMinimo) < 0) {
                            return false;
                        }
                    } catch (NumberFormatException e) {
                        // Ignorar errores de formato
                    }
                }
                
                return true;
            })
            .toList();
    }
    
    /**
     * Actualiza las estadísticas
     */
    private void actualizarEstadisticas() {
        try {
            Long idCliente = authService.getCurrentUser().getIdUsuario();
            List<Prestamo> prestamos = prestamoService.obtenerPrestamosPorCliente(idCliente);
            
            int totalPrestamos = prestamos.size();
            long prestamosPagados = prestamos.stream()
                .filter(p -> p.getEstado() == Prestamo.EstadoPrestamo.FINALIZADO)
                .count();
            
            BigDecimal totalPagado = BigDecimal.ZERO;
            BigDecimal capitalAcumulado = BigDecimal.ZERO;
            
            // Calcular total pagado y capital acumulado
            for (Prestamo prestamo : prestamos) {
                if (prestamo.getEstado() == Prestamo.EstadoPrestamo.FINALIZADO) {
                    totalPagado = totalPagado.add(prestamo.getMontoSolicitado());
                }
                // Capital retenido es 10% del monto solicitado
                capitalAcumulado = capitalAcumulado.add(
                    prestamo.getMontoSolicitado().multiply(new BigDecimal("0.10"))
                );
            }
            
            lblTotalPrestamos.setText(String.valueOf(totalPrestamos));
            lblPrestamosPagados.setText(String.valueOf(prestamosPagados));
            lblTotalPagado.setText("S/ " + String.format("%.2f", totalPagado.doubleValue()));
            lblCapitalAcumulado.setText("S/ " + String.format("%.2f", capitalAcumulado.doubleValue()));
            
        } catch (Exception e) {
            logger.error("Error al actualizar estadísticas", e);
            // Establecer valores por defecto
            lblTotalPrestamos.setText("0");
            lblPrestamosPagados.setText("0");
            lblTotalPagado.setText("S/ 0.00");
            lblCapitalAcumulado.setText("S/ 0.00");
        }
    }
    
    /**
     * Ver detalle de un préstamo
     */
    private void verDetallePrestamo(Prestamo prestamo) {
        try {
            // TODO: Implementar vista de detalle del préstamo
            mostrarInfo("Detalle del préstamo PR-" + prestamo.getIdPrestamo() + 
                       "\nMonto: S/ " + String.format("%.2f", prestamo.getMontoSolicitado().doubleValue()) +
                       "\nEstado: " + prestamo.getEstado().name());
            
        } catch (Exception e) {
            logger.error("Error al mostrar detalle del préstamo", e);
            mostrarError("Error al mostrar el detalle del préstamo");
        }
    }
    
    /**
     * Maneja el evento de filtrar
     */
    @FXML
    private void handleFiltrar() {
        try {
            Long idCliente = authService.getCurrentUser().getIdUsuario();
            cargarHistorialPrestamos(idCliente);
        } catch (Exception e) {
            logger.error("Error al filtrar historial", e);
        }
    }
    
    /**
     * Maneja la búsqueda
     */
    @FXML
    private void handleBuscar() {
        try {
            Long idCliente = authService.getCurrentUser().getIdUsuario();
            cargarHistorialPrestamos(idCliente);
            mostrarInfo("Búsqueda completada");
        } catch (Exception e) {
            logger.error("Error al buscar en historial", e);
            mostrarError("Error al realizar la búsqueda");
        }
    }
    
    /**
     * Maneja la limpieza de filtros
     */
    @FXML
    private void handleLimpiar() {
        comboEstado.setValue("Todos");
        dateDesde.setValue(LocalDate.now().minusYears(1));
        dateHasta.setValue(LocalDate.now());
        txtMontoMinimo.clear();
        
        try {
            Long idCliente = authService.getCurrentUser().getIdUsuario();
            cargarHistorialPrestamos(idCliente);
            mostrarInfo("Filtros limpiados");
        } catch (Exception e) {
            logger.error("Error al limpiar filtros", e);
        }
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
     * Actualiza la etiqueta de última actualización
     */
    private void actualizarUltimaActualizacion() {
        lblUltimaActualizacion.setText("Última actualización: " + 
            FechaUtil.formatearFecha(LocalDate.now()));
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
}
