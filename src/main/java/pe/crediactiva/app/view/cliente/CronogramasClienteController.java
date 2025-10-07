package pe.crediactiva.app.view.cliente;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pe.crediactiva.app.model.Cronograma;
import pe.crediactiva.app.model.Prestamo;
import pe.crediactiva.app.service.AuthenticationService;
import pe.crediactiva.app.dao.CronogramaDAO;
import pe.crediactiva.app.dao.impl.CronogramaDAOImpl;
import pe.crediactiva.app.service.PrestamoService;
import pe.crediactiva.app.util.FechaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Controlador para la pantalla de cronogramas del cliente
 */
public class CronogramasClienteController {
    
    private static final Logger logger = LoggerFactory.getLogger(CronogramasClienteController.class);
    
    @FXML
    private Button btnVolver;
    
    @FXML
    private ComboBox<String> comboPrestamos;
    
    @FXML
    private ComboBox<String> comboEstadoFiltro;
    
    @FXML
    private Button btnActualizar;
    
    @FXML
    private VBox panelInfoPrestamo;
    
    @FXML
    private Label lblInfoIdPrestamo;
    
    @FXML
    private Label lblInfoMontoTotal;
    
    @FXML
    private Label lblInfoEstado;
    
    @FXML
    private Label lblInfoTasaInteres;
    
    @FXML
    private Label lblInfoTotalCuotas;
    
    @FXML
    private Label lblInfoCuotasPagadas;
    
    @FXML
    private TableView<Cronograma> tablaCronograma;
    
    @FXML
    private TableColumn<Cronograma, Integer> colNumeroCuota;
    
    @FXML
    private TableColumn<Cronograma, String> colFechaProgramada;
    
    @FXML
    private TableColumn<Cronograma, BigDecimal> colMontoCuota;
    
    @FXML
    private TableColumn<Cronograma, String> colEstadoCuota;
    
    @FXML
    private TableColumn<Cronograma, String> colFechaPagoReal;
    
    @FXML
    private TableColumn<Cronograma, Integer> colDiasAtraso;
    
    @FXML
    private TableColumn<Cronograma, String> colObservaciones;
    
    @FXML
    private Label lblTotalPagado;
    
    @FXML
    private Label lblTotalPendiente;
    
    @FXML
    private Label lblTotalVencido;
    
    @FXML
    private Label lblProgreso;
    
    @FXML
    private Label lblUltimaActualizacion;
    
    private Stage primaryStage;
    private AuthenticationService authService;
    private PrestamoService prestamoService;
    private CronogramaDAO cronogramaDAO;
    private ObservableList<Prestamo> prestamosData;
    private ObservableList<Cronograma> cronogramaData;
    private DateTimeFormatter dateFormatter;
    
    public CronogramasClienteController() {
        this.authService = new AuthenticationService();
        this.prestamoService = new PrestamoService();
        this.cronogramaDAO = new CronogramaDAOImpl();
        this.dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    }
    
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    @FXML
    private void initialize() {
        try {
            // Configurar combo de estados
            comboEstadoFiltro.getItems().addAll("Todos", "Pendiente", "Pagada", "Retrasada");
            comboEstadoFiltro.setValue("Todos");
            
            // Configurar tabla
            configurarTabla();
            
            // Cargar datos iniciales
            cargarPrestamos();
            actualizarUltimaActualizacion();
            
        } catch (Exception e) {
            logger.error("Error al inicializar la pantalla de cronogramas del cliente", e);
            mostrarError("Error al inicializar la pantalla");
        }
    }
    
    /**
     * Configura las columnas de la tabla
     */
    private void configurarTabla() {
        colNumeroCuota.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getNumeroCuota()).asObject());
        
        colFechaProgramada.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(
                cellData.getValue().getFechaProgramada().format(dateFormatter)
            ));
        
        colMontoCuota.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getMontoCuota()));
        colMontoCuota.setCellFactory(column -> new TableCell<Cronograma, BigDecimal>() {
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
        
        colEstadoCuota.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEstadoCuota().name()));
        
        colFechaPagoReal.setCellValueFactory(cellData -> {
            LocalDate fecha = cellData.getValue().getFechaPagoReal();
            return fecha != null ? 
                new javafx.beans.property.SimpleStringProperty(fecha.format(dateFormatter)) :
                new javafx.beans.property.SimpleStringProperty("--");
        });
        
        colDiasAtraso.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getDiasAtraso()).asObject());
        
        colObservaciones.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty("--"));
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
            comboPrestamos.setItems(FXCollections.observableArrayList());
            
            // Llenar combo con préstamos que tienen cronograma
            for (Prestamo prestamo : prestamos) {
                if (prestamo.getEstado() == Prestamo.EstadoPrestamo.ACTIVO || 
                    prestamo.getEstado() == Prestamo.EstadoPrestamo.FINALIZADO) {
                    String descripcion = String.format("ID: %d - S/ %.2f (%s)", 
                        prestamo.getIdPrestamo(),
                        prestamo.getMontoSolicitado().doubleValue(),
                        prestamo.getEstado().name());
                    comboPrestamos.getItems().add(descripcion);
                }
            }
            
            // Seleccionar el primer préstamo si existe
            if (!comboPrestamos.getItems().isEmpty()) {
                comboPrestamos.setValue(comboPrestamos.getItems().get(0));
                handleCambiarPrestamo();
            }
            
            logger.info("Cargados " + prestamos.size() + " préstamos para el cliente " + clienteId);
            
        } catch (Exception e) {
            logger.error("Error al cargar préstamos del cliente", e);
            mostrarError("Error al cargar los préstamos");
        }
    }
    
    /**
     * Maneja el cambio de préstamo seleccionado
     */
    @FXML
    private void handleCambiarPrestamo() {
        try {
            String seleccion = comboPrestamos.getValue();
            if (seleccion == null || seleccion.isEmpty()) {
                limpiarCronograma();
                return;
            }
            
            // Extraer ID del préstamo de la descripción
            String[] partes = seleccion.split(" - ");
            String idParte = partes[0].replace("ID: ", "");
            Long idPrestamo = Long.parseLong(idParte);
            
            // Buscar el préstamo
            Optional<Prestamo> prestamoOpt = prestamosData.stream()
                .filter(p -> p.getIdPrestamo().equals(idPrestamo))
                .findFirst();
            
            if (prestamoOpt.isPresent()) {
                Prestamo prestamo = prestamoOpt.get();
                mostrarInformacionPrestamo(prestamo);
                cargarCronograma(idPrestamo);
            } else {
                limpiarCronograma();
            }
            
        } catch (Exception e) {
            logger.error("Error al cambiar préstamo", e);
            mostrarError("Error al cargar el cronograma");
        }
    }
    
    /**
     * Muestra la información del préstamo seleccionado
     */
    private void mostrarInformacionPrestamo(Prestamo prestamo) {
        try {
            lblInfoIdPrestamo.setText(prestamo.getIdPrestamo().toString());
            lblInfoMontoTotal.setText("S/ " + String.format("%.2f", prestamo.getMontoSolicitado().doubleValue()));
            lblInfoEstado.setText(prestamo.getEstado().name());
            lblInfoTasaInteres.setText(String.format("%.2f%%", prestamo.getTasaInteres().doubleValue()));
            
            panelInfoPrestamo.setVisible(true);
            
        } catch (Exception e) {
            logger.error("Error al mostrar información del préstamo", e);
        }
    }
    
    /**
     * Carga el cronograma del préstamo seleccionado
     */
    private void cargarCronograma(Long idPrestamo) {
        try {
            List<Cronograma> cronograma = cronogramaDAO.findByPrestamo(idPrestamo);
            
            cronogramaData = FXCollections.observableArrayList(cronograma);
            tablaCronograma.setItems(cronogramaData);
            
            // Actualizar información de cuotas
            actualizarInformacionCuotas(cronograma);
            
            // Actualizar resumen de pagos
            actualizarResumenPagos(cronograma);
            
            logger.info("Cargado cronograma con " + cronograma.size() + " cuotas para préstamo " + idPrestamo);
            
        } catch (Exception e) {
            logger.error("Error al cargar cronograma del préstamo " + idPrestamo, e);
            mostrarError("Error al cargar el cronograma");
        }
    }
    
    /**
     * Actualiza la información de cuotas en el panel de información
     */
    private void actualizarInformacionCuotas(List<Cronograma> cronograma) {
        if (cronograma.isEmpty()) {
            lblInfoTotalCuotas.setText("0");
            lblInfoCuotasPagadas.setText("0");
            return;
        }
        
        int totalCuotas = cronograma.size();
        int cuotasPagadas = (int) cronograma.stream()
            .filter(c -> c.getEstadoCuota() == Cronograma.EstadoCuota.PAGADA)
            .count();
        
        lblInfoTotalCuotas.setText(String.valueOf(totalCuotas));
        lblInfoCuotasPagadas.setText(String.valueOf(cuotasPagadas));
    }
    
    /**
     * Actualiza el resumen de pagos
     */
    private void actualizarResumenPagos(List<Cronograma> cronograma) {
        if (cronograma.isEmpty()) {
            lblTotalPagado.setText("S/ 0.00");
            lblTotalPendiente.setText("S/ 0.00");
            lblTotalVencido.setText("S/ 0.00");
            lblProgreso.setText("0%");
            return;
        }
        
        BigDecimal totalPagado = cronograma.stream()
            .filter(c -> c.getEstadoCuota() == Cronograma.EstadoCuota.PAGADA)
            .map(Cronograma::getMontoCuota)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalPendiente = cronograma.stream()
            .filter(c -> c.getEstadoCuota() == Cronograma.EstadoCuota.PENDIENTE)
            .map(Cronograma::getMontoCuota)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalVencido = cronograma.stream()
            .filter(c -> c.getEstadoCuota() == Cronograma.EstadoCuota.RETRASADA)
            .map(Cronograma::getMontoCuota)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal montoTotal = cronograma.stream()
            .map(Cronograma::getMontoCuota)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        double progreso = montoTotal.compareTo(BigDecimal.ZERO) > 0 ? 
            totalPagado.divide(montoTotal, 4, java.math.RoundingMode.HALF_UP).doubleValue() * 100 : 0;
        
        lblTotalPagado.setText("S/ " + String.format("%.2f", totalPagado.doubleValue()));
        lblTotalPendiente.setText("S/ " + String.format("%.2f", totalPendiente.doubleValue()));
        lblTotalVencido.setText("S/ " + String.format("%.2f", totalVencido.doubleValue()));
        lblProgreso.setText(String.format("%.1f%%", progreso));
    }
    
    /**
     * Limpia el cronograma cuando no hay préstamo seleccionado
     */
    private void limpiarCronograma() {
        tablaCronograma.getItems().clear();
        panelInfoPrestamo.setVisible(false);
        lblTotalPagado.setText("S/ 0.00");
        lblTotalPendiente.setText("S/ 0.00");
        lblTotalVencido.setText("S/ 0.00");
        lblProgreso.setText("0%");
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
                handleCambiarPrestamo();
            } else {
                if (cronogramaData == null) {
                    return;
                }
                
                // Filtrar por estado
                ObservableList<Cronograma> cronogramaFiltrado = cronogramaData.filtered(cuota -> 
                    cuota.getEstadoCuota().name().equalsIgnoreCase(estadoFiltro)
                );
                
                tablaCronograma.setItems(cronogramaFiltrado);
            }
            
        } catch (Exception e) {
            logger.error("Error al filtrar cronograma", e);
            mostrarError("Error al filtrar el cronograma");
        }
    }
    
    @FXML
    private void handleActualizar() {
        cargarPrestamos();
        comboEstadoFiltro.setValue("Todos");
        actualizarUltimaActualizacion();
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
