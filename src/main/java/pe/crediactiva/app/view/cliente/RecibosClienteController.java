package pe.crediactiva.app.view.cliente;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pe.crediactiva.app.model.DocumentoDisponible;
import pe.crediactiva.app.model.Prestamo;
import pe.crediactiva.app.service.AuthenticationService;
import pe.crediactiva.app.service.PrestamoService;
import pe.crediactiva.app.service.DocumentoService;
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
 * Controlador para la pantalla de descarga de recibos del cliente
 */
public class RecibosClienteController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(RecibosClienteController.class);
    
    @FXML
    private ComboBox<Prestamo> comboPrestamo;
    @FXML
    private ComboBox<String> comboTipoDocumento;
    @FXML
    private DatePicker dateDesde;
    @FXML
    private DatePicker dateHasta;
    
    @FXML
    private Button btnBuscar;
    @FXML
    private Button btnLimpiar;
    @FXML
    private Button btnDescargarSeleccionados;
    @FXML
    private Button btnGenerarPaquete;
    @FXML
    private Button btnVolver;
    
    @FXML
    private CheckBox checkSeleccionarTodos;
    
    @FXML
    private Label lblTotalDocumentos;
    
    @FXML
    private TableView<DocumentoDisponible> tablaDocumentos;
    @FXML
    private TableColumn<DocumentoDisponible, String> colTipoDocumento;
    @FXML
    private TableColumn<DocumentoDisponible, String> colPrestamo;
    @FXML
    private TableColumn<DocumentoDisponible, String> colFechaDocumento;
    @FXML
    private TableColumn<DocumentoDisponible, BigDecimal> colMonto;
    @FXML
    private TableColumn<DocumentoDisponible, String> colEstado;
    @FXML
    private TableColumn<DocumentoDisponible, Void> colAcciones;
    
    private Stage primaryStage;
    private AuthenticationService authService;
    private PrestamoService prestamoService;
    private DocumentoService documentoService;
    private ObservableList<Prestamo> prestamosData;
    private ObservableList<DocumentoDisponible> documentosData;
    private DateTimeFormatter dateFormatter;
    
    public RecibosClienteController() {
        this.authService = new AuthenticationService();
        this.prestamoService = new PrestamoService();
        this.documentoService = new DocumentoService();
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
            cargarDatosIniciales();
            establecerValoresPorDefecto();
            
            logger.info("Pantalla de recibos del cliente inicializada correctamente");
            
        } catch (Exception e) {
            logger.error("Error al inicializar la pantalla de recibos", e);
            mostrarError("Error al inicializar la pantalla");
        }
    }
    
    /**
     * Configura los controles de la interfaz
     */
    private void configurarControles() {
        // Configurar ComboBox de tipos de documento
        ObservableList<String> tiposDocumento = FXCollections.observableArrayList(
            "Todos",
            "Recibo de Pago",
            "Constancia de Cancelación",
            "Cronograma de Pagos",
            "Contrato de Préstamo"
        );
        comboTipoDocumento.setItems(tiposDocumento);
        comboTipoDocumento.setValue("Todos");
        
        // Configurar DatePickers
        dateDesde.setValue(LocalDate.now().minusMonths(1));
        dateHasta.setValue(LocalDate.now());
    }
    
    /**
     * Configura la tabla de documentos
     */
    private void configurarTabla() {
        colTipoDocumento.setCellValueFactory(new PropertyValueFactory<>("tipoDocumento"));
        colPrestamo.setCellValueFactory(new PropertyValueFactory<>("numeroPrestamo"));
        colFechaDocumento.setCellValueFactory(new PropertyValueFactory<>("fechaDocumento"));
        
        colMonto.setCellValueFactory(new PropertyValueFactory<>("monto"));
        colMonto.setCellFactory(column -> new TableCell<DocumentoDisponible, BigDecimal>() {
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
        
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        
        // Columna de acciones
        colAcciones.setCellFactory(column -> new TableCell<DocumentoDisponible, Void>() {
            private final Button btnDescargar = new Button("Descargar");
            private final CheckBox checkSeleccionar = new CheckBox();
            
            {
                btnDescargar.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
                btnDescargar.setOnAction(event -> {
                    DocumentoDisponible documento = getTableView().getItems().get(getIndex());
                    descargarDocumento(documento);
                });
                
                checkSeleccionar.setOnAction(event -> {
                    DocumentoDisponible documento = getTableView().getItems().get(getIndex());
                    documento.setSeleccionado(checkSeleccionar.isSelected());
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(5);
                    hbox.getChildren().addAll(checkSeleccionar, btnDescargar);
                    setGraphic(hbox);
                }
            }
        });
        
        // Configurar selección múltiple
        tablaDocumentos.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }
    
    /**
     * Carga los datos iniciales
     */
    private void cargarDatosIniciales() {
        try {
            // Cargar préstamos del cliente
            Long idCliente = authService.getCurrentUser().getIdUsuario();
            List<Prestamo> prestamos = prestamoService.obtenerPrestamosPorCliente(idCliente);
            prestamosData = FXCollections.observableArrayList(prestamos);
            comboPrestamo.setItems(prestamosData);
            
            // Cargar documentos disponibles
            cargarDocumentosDisponibles();
            
        } catch (Exception e) {
            logger.error("Error al cargar datos iniciales", e);
            mostrarError("Error al cargar los datos");
        }
    }
    
    /**
     * Establece valores por defecto
     */
    private void establecerValoresPorDefecto() {
        // Seleccionar el primer préstamo si existe
        if (!prestamosData.isEmpty()) {
            comboPrestamo.setValue(prestamosData.get(0));
        }
    }
    
    /**
     * Maneja el evento de filtrar
     */
    @FXML
    private void handleFiltrar() {
        cargarDocumentosDisponibles();
    }
    
    /**
     * Maneja la búsqueda de documentos
     */
    @FXML
    private void handleBuscar() {
        cargarDocumentosDisponibles();
        mostrarInfo("Búsqueda completada");
    }
    
    /**
     * Carga los documentos disponibles según los filtros
     */
    private void cargarDocumentosDisponibles() {
        try {
            Long idCliente = authService.getCurrentUser().getIdUsuario();
            List<DocumentoDisponible> documentos = documentoService.obtenerDocumentosDisponiblesCliente(
                idCliente,
                comboPrestamo.getValue(),
                comboTipoDocumento.getValue(),
                dateDesde.getValue(),
                dateHasta.getValue()
            );
            
            documentosData = FXCollections.observableArrayList(documentos);
            tablaDocumentos.setItems(documentosData);
            
            lblTotalDocumentos.setText("Total: " + documentos.size() + " documentos");
            
        } catch (Exception e) {
            logger.error("Error al cargar documentos disponibles", e);
            mostrarError("Error al cargar los documentos");
        }
    }
    
    /**
     * Descarga un documento específico
     */
    private void descargarDocumento(DocumentoDisponible documento) {
        try {
            // TODO: Implementar descarga real del documento
            mostrarInfo("Descargando documento: " + documento.getTipoDocumento());
            
            // Simular descarga
            Thread.sleep(1000);
            mostrarInfo("Documento descargado exitosamente");
            
        } catch (Exception e) {
            logger.error("Error al descargar documento", e);
            mostrarError("Error al descargar el documento");
        }
    }
    
    /**
     * Maneja la selección/deselección de todos los documentos
     */
    @FXML
    private void handleSeleccionarTodos() {
        boolean seleccionar = checkSeleccionarTodos.isSelected();
        
        for (DocumentoDisponible documento : documentosData) {
            documento.setSeleccionado(seleccionar);
        }
        
        // Refrescar la tabla
        tablaDocumentos.refresh();
    }
    
    /**
     * Maneja la descarga de documentos seleccionados
     */
    @FXML
    private void handleDescargarSeleccionados() {
        List<DocumentoDisponible> seleccionados = documentosData.stream()
            .filter(DocumentoDisponible::isSeleccionado)
            .toList();
        
        if (seleccionados.isEmpty()) {
            mostrarError("Por favor seleccione al menos un documento");
            return;
        }
        
        try {
            // TODO: Implementar descarga masiva
            mostrarInfo("Descargando " + seleccionados.size() + " documentos seleccionados...");
            
            // Simular descarga masiva
            Thread.sleep(2000);
            mostrarInfo("Todos los documentos han sido descargados exitosamente");
            
        } catch (Exception e) {
            logger.error("Error al descargar documentos seleccionados", e);
            mostrarError("Error al descargar los documentos");
        }
    }
    
    /**
     * Maneja la generación de un paquete ZIP
     */
    @FXML
    private void handleGenerarPaquete() {
        List<DocumentoDisponible> seleccionados = documentosData.stream()
            .filter(DocumentoDisponible::isSeleccionado)
            .toList();
        
        if (seleccionados.isEmpty()) {
            mostrarError("Por favor seleccione al menos un documento");
            return;
        }
        
        try {
            // TODO: Implementar generación de ZIP
            mostrarInfo("Generando paquete ZIP con " + seleccionados.size() + " documentos...");
            
            // Simular generación de ZIP
            Thread.sleep(1500);
            mostrarInfo("Paquete ZIP generado exitosamente");
            
        } catch (Exception e) {
            logger.error("Error al generar paquete ZIP", e);
            mostrarError("Error al generar el paquete ZIP");
        }
    }
    
    /**
     * Maneja la limpieza de filtros
     */
    @FXML
    private void handleLimpiar() {
        comboPrestamo.setValue(null);
        comboTipoDocumento.setValue("Todos");
        dateDesde.setValue(LocalDate.now().minusMonths(1));
        dateHasta.setValue(LocalDate.now());
        checkSeleccionarTodos.setSelected(false);
        
        cargarDocumentosDisponibles();
        mostrarInfo("Filtros limpiados");
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
