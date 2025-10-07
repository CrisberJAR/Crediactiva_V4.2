package pe.crediactiva.app.view.cliente;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pe.crediactiva.app.model.Prestamo;
import pe.crediactiva.app.service.AuthenticationService;
import pe.crediactiva.app.service.PrestamoService;
import pe.crediactiva.app.util.FechaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controlador para la pantalla de préstamos del cliente
 */
public class PrestamosClienteController {
    
    private static final Logger logger = LoggerFactory.getLogger(PrestamosClienteController.class);
    
    @FXML
    private Button btnVolver;
    
    @FXML
    private ComboBox<String> comboEstadoFiltro;
    
    @FXML
    private Button btnActualizar;
    
    @FXML
    private TableView<Prestamo> tablaPrestamos;
    
    @FXML
    private TableColumn<Prestamo, Long> colIdPrestamo;
    
    @FXML
    private TableColumn<Prestamo, BigDecimal> colMontoSolicitado;
    
    @FXML
    private TableColumn<Prestamo, BigDecimal> colMontoDesembolsado;
    
    @FXML
    private TableColumn<Prestamo, BigDecimal> colTasaInteres;
    
    @FXML
    private TableColumn<Prestamo, String> colEstado;
    
    @FXML
    private TableColumn<Prestamo, String> colEtiqueta;
    
    @FXML
    private TableColumn<Prestamo, String> colFechaInicio;
    
    @FXML
    private TableColumn<Prestamo, String> colFechaFin;
    
    @FXML
    private TableColumn<Prestamo, String> colObservacion;
    
    @FXML
    private VBox panelDetalles;
    
    @FXML
    private Label lblDetalleIdPrestamo;
    
    @FXML
    private Label lblDetalleMontoSolicitado;
    
    @FXML
    private Label lblDetalleMontoDesembolsado;
    
    @FXML
    private Label lblDetalleTasaInteres;
    
    @FXML
    private Label lblDetalleEstado;
    
    @FXML
    private Label lblDetalleEtiqueta;
    
    @FXML
    private Label lblDetalleFechaInicio;
    
    @FXML
    private Label lblDetalleFechaFin;
    
    @FXML
    private TextArea txtDetalleObservacion;
    
    @FXML
    private Label lblTotalPrestamos;
    
    private Stage primaryStage;
    private AuthenticationService authService;
    private PrestamoService prestamoService;
    private ObservableList<Prestamo> prestamosData;
    private DateTimeFormatter dateFormatter;
    
    public PrestamosClienteController() {
        this.authService = new AuthenticationService();
        this.prestamoService = new PrestamoService();
        this.dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    }
    
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    @FXML
    private void initialize() {
        try {
            // Configurar combo de filtros
            comboEstadoFiltro.getItems().addAll("Todos", "Pendiente", "Activo", "Suspendido", "Finalizado", "Rechazado");
            comboEstadoFiltro.setValue("Todos");
            
            // Configurar tabla
            configurarTabla();
            
            // Configurar selección de fila
            tablaPrestamos.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldSelection, newSelection) -> mostrarDetallesPrestamo(newSelection)
            );
            
            // Cargar datos iniciales
            cargarPrestamos();
            
        } catch (Exception e) {
            logger.error("Error al inicializar la pantalla de préstamos del cliente", e);
            mostrarError("Error al inicializar la pantalla");
        }
    }
    
    /**
     * Configura las columnas de la tabla
     */
    private void configurarTabla() {
        colIdPrestamo.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleLongProperty(cellData.getValue().getIdPrestamo()).asObject());
        
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
        
        colMontoDesembolsado.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getMontoDesembolsado()));
        colMontoDesembolsado.setCellFactory(column -> new TableCell<Prestamo, BigDecimal>() {
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
        
        colEtiqueta.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEtiqueta().name()));
        
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
        
        colObservacion.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getObservacion()));
    }
    
    /**
     * Carga los préstamos del cliente
     */
    private void cargarPrestamos() {
        try {
            if (authService.getCurrentUser() == null) {
                mostrarError("No hay usuario autenticado");
                return;
            }
            
            Long clienteId = authService.getClienteId();
            if (clienteId == null) {
                mostrarError("No se pudo obtener el ID del cliente");
                return;
            }
            
            // Obtener todos los préstamos del cliente
            List<Prestamo> prestamos = prestamoService.obtenerPrestamosPorCliente(clienteId);
            
            prestamosData = FXCollections.observableArrayList(prestamos);
            tablaPrestamos.setItems(prestamosData);
            
            // Actualizar contador
            lblTotalPrestamos.setText("Total de préstamos: " + prestamos.size());
            
            logger.info("Cargados " + prestamos.size() + " préstamos para el cliente " + clienteId);
            
        } catch (Exception e) {
            logger.error("Error al cargar préstamos del cliente", e);
            mostrarError("Error al cargar los préstamos");
        }
    }
    
    /**
     * Muestra los detalles del préstamo seleccionado
     */
    private void mostrarDetallesPrestamo(Prestamo prestamo) {
        if (prestamo == null) {
            panelDetalles.setVisible(false);
            return;
        }
        
        try {
            lblDetalleIdPrestamo.setText(prestamo.getIdPrestamo().toString());
            lblDetalleMontoSolicitado.setText("S/ " + String.format("%.2f", prestamo.getMontoSolicitado().doubleValue()));
            lblDetalleMontoDesembolsado.setText("S/ " + String.format("%.2f", prestamo.getMontoDesembolsado().doubleValue()));
            lblDetalleTasaInteres.setText(String.format("%.2f%%", prestamo.getTasaInteres().doubleValue()));
            lblDetalleEstado.setText(prestamo.getEstado().name());
            lblDetalleEtiqueta.setText(prestamo.getEtiqueta().name());
            lblDetalleFechaInicio.setText(prestamo.getFechaInicio() != null ? 
                prestamo.getFechaInicio().format(dateFormatter) : "--");
            lblDetalleFechaFin.setText(prestamo.getFechaFin() != null ? 
                prestamo.getFechaFin().format(dateFormatter) : "--");
            txtDetalleObservacion.setText(prestamo.getObservacion() != null ? 
                prestamo.getObservacion() : "Sin observaciones");
            
            panelDetalles.setVisible(true);
            
        } catch (Exception e) {
            logger.error("Error al mostrar detalles del préstamo", e);
        }
    }
    
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
    
    @FXML
    private void handleFiltrar() {
        try {
            String estadoFiltro = comboEstadoFiltro.getValue();
            
            if ("Todos".equals(estadoFiltro)) {
                cargarPrestamos();
            } else {
                if (prestamosData == null) {
                    cargarPrestamos();
                    return;
                }
                
                // Filtrar por estado
                ObservableList<Prestamo> prestamosFiltrados = prestamosData.filtered(prestamo -> 
                    prestamo.getEstado().name().equalsIgnoreCase(estadoFiltro)
                );
                
                tablaPrestamos.setItems(prestamosFiltrados);
                lblTotalPrestamos.setText("Total de préstamos (" + estadoFiltro + "): " + prestamosFiltrados.size());
            }
            
        } catch (Exception e) {
            logger.error("Error al filtrar préstamos", e);
            mostrarError("Error al filtrar préstamos");
        }
    }
    
    @FXML
    private void handleActualizar() {
        cargarPrestamos();
        comboEstadoFiltro.setValue("Todos");
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
