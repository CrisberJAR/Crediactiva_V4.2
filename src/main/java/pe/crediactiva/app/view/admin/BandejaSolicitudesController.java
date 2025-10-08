package pe.crediactiva.app.view.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.crediactiva.app.model.Asesor;
import pe.crediactiva.app.model.Cliente;
import pe.crediactiva.app.model.Prestamo;
import pe.crediactiva.app.service.ClienteService;
import pe.crediactiva.app.service.PrestamoService;
import pe.crediactiva.app.service.AsesorService;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controlador para la bandeja de solicitudes de préstamo
 * Versión simplificada - solo muestra la lista de préstamos pendientes
 */
public class BandejaSolicitudesController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(BandejaSolicitudesController.class);
    
    // Servicios
    private final PrestamoService prestamoService;
    private final ClienteService clienteService;
    private final AsesorService asesorService;
    
    // Cache para nombres
    private final Map<Long, String> clienteNombresCache;
    private final Map<Long, String> asesorNombresCache;
    
    // Lista de solicitudes
    private final ObservableList<Prestamo> solicitudes;
    
    // Componentes de la interfaz
    @FXML
    private TableView<Prestamo> tablaSolicitudes;
    
    @FXML
    private TableColumn<Prestamo, Long> colId;
    
    @FXML
    private TableColumn<Prestamo, String> colCliente;
    
    @FXML
    private TableColumn<Prestamo, String> colAsesor;
    
    @FXML
    private TableColumn<Prestamo, String> colMonto;
    
    @FXML
    private TableColumn<Prestamo, String> colObservacion;
    
    @FXML
    private TableColumn<Prestamo, String> colFecha;
    
    @FXML
    private Label lblTotalSolicitudes;
    
    @FXML
    private Button btnActualizar;
    
    /**
     * Constructor
     */
    public BandejaSolicitudesController() {
        this.prestamoService = new PrestamoService();
        this.clienteService = new ClienteService();
        this.asesorService = new AsesorService();
        this.solicitudes = FXCollections.observableArrayList();
        this.clienteNombresCache = new HashMap<>();
        this.asesorNombresCache = new HashMap<>();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Configurar tabla
            configurarTabla();
            
            // Cargar datos iniciales
            cargarSolicitudes();
            
            logger.info("Bandeja de solicitudes inicializada correctamente");
            
        } catch (Exception e) {
            logger.error("Error al inicializar la bandeja de solicitudes", e);
            mostrarError("Error al inicializar la bandeja de solicitudes");
        }
    }
    
    /**
     * Configura la tabla de solicitudes
     */
    private void configurarTabla() {
        // Configurar columnas
        colId.setCellValueFactory(new PropertyValueFactory<>("idPrestamo"));
        
        // Columna de Cliente - con nombres reales desde cache
        colCliente.setCellValueFactory(cellData -> {
            Long idCliente = cellData.getValue().getIdCliente();
            String nombreCliente = obtenerNombreCliente(idCliente);
            return new javafx.beans.property.SimpleStringProperty(nombreCliente);
        });
        
        // Columna de Asesor - con nombres reales desde cache
        colAsesor.setCellValueFactory(cellData -> {
            Long idAsesor = cellData.getValue().getIdAsesor();
            String nombreAsesor = obtenerNombreAsesor(idAsesor);
            return new javafx.beans.property.SimpleStringProperty(nombreAsesor);
        });
        
        // Columna de Monto con formato
        colMonto.setCellValueFactory(cellData -> {
            java.math.BigDecimal monto = cellData.getValue().getMontoSolicitado();
            if (monto != null) {
                return new javafx.beans.property.SimpleStringProperty(String.format("S/ %.2f", monto));
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
        
        // Columna de Observación/Motivo
        colObservacion.setCellValueFactory(cellData -> {
            String observacion = cellData.getValue().getObservacion();
            return new javafx.beans.property.SimpleStringProperty(observacion != null ? observacion : "Sin observaciones");
        });
        
        // Columna de Fecha
        colFecha.setCellValueFactory(cellData -> {
            if (cellData.getValue().getCreadoEn() != null) {
                LocalDate fecha = cellData.getValue().getCreadoEn().toLocalDate();
                return new javafx.beans.property.SimpleStringProperty(fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
        
        // Configurar doble clic para abrir ventana de detalles
        tablaSolicitudes.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) { // Doble clic
                Prestamo prestamoSeleccionado = tablaSolicitudes.getSelectionModel().getSelectedItem();
                if (prestamoSeleccionado != null) {
                    abrirVentanaDetalles(prestamoSeleccionado);
                }
            }
        });
    }
    
    /**
     * Carga las solicitudes desde la base de datos
     */
    private void cargarSolicitudes() {
        try {
            // Obtener préstamos pendientes directamente de la base de datos
            List<Prestamo> prestamos = prestamoService.obtenerPrestamosPendientes();
            
            // Limpiar lista actual
            solicitudes.clear();
            solicitudes.addAll(prestamos);
            
            // Cachear nombres de clientes y asesores para mostrar nombres reales
            cargarNombresEnCache(prestamos);
            
            // Asignar datos a la tabla
            tablaSolicitudes.setItems(solicitudes);
            
            // Actualizar contadores
            actualizarContadores();
            
            logger.info("Cargadas " + prestamos.size() + " solicitudes pendientes desde la base de datos");
            
            // Mostrar mensaje si no hay solicitudes
            if (prestamos.isEmpty()) {
                mostrarInfo("✅ No hay solicitudes de préstamo pendientes en este momento.\n\n" +
                           "📋 Las solicitudes aparecerán aquí cuando los asesores registren nuevas solicitudes de préstamo.\n" +
                           "🔄 La tabla se actualiza automáticamente después de aprobar o rechazar préstamos.");
            }
            
        } catch (Exception e) {
            logger.error("Error al cargar solicitudes pendientes", e);
            mostrarError("Error al cargar las solicitudes: " + e.getMessage());
        }
    }
    
    /**
     * Actualiza los contadores de solicitudes
     */
    private void actualizarContadores() {
        int total = solicitudes.size();
        lblTotalSolicitudes.setText("Total de solicitudes: " + total);
    }
    
    /**
     * Cachea los nombres de clientes y asesores
     */
    private void cargarNombresEnCache(List<Prestamo> prestamos) {
        try {
            for (Prestamo prestamo : prestamos) {
                // Cachear nombre del cliente
                if (!clienteNombresCache.containsKey(prestamo.getIdCliente())) {
                    Optional<Cliente> clienteOpt = clienteService.obtenerClientePorId(prestamo.getIdCliente());
                    if (clienteOpt.isPresent()) {
                        Cliente cliente = clienteOpt.get();
                        String nombreCompleto = cliente.getNombreCompleto();
                        clienteNombresCache.put(prestamo.getIdCliente(), nombreCompleto);
                    }
                }
                
                // Cachear nombre del asesor
                if (!asesorNombresCache.containsKey(prestamo.getIdAsesor())) {
                    Optional<Asesor> asesorOpt = asesorService.obtenerAsesorPorId(prestamo.getIdAsesor());
                    if (asesorOpt.isPresent()) {
                        Asesor asesor = asesorOpt.get();
                        String nombreCompleto = asesor.getNombreCompleto();
                        asesorNombresCache.put(prestamo.getIdAsesor(), nombreCompleto);
                    } else {
                        asesorNombresCache.put(prestamo.getIdAsesor(), "Asesor #" + prestamo.getIdAsesor());
                    }
                }
            }
            
            logger.info("Cache de nombres cargado: " + clienteNombresCache.size() + " clientes, " + asesorNombresCache.size() + " asesores");
            
        } catch (Exception e) {
            logger.error("Error al cargar nombres en cache", e);
        }
    }
    
    /**
     * Obtiene el nombre del cliente desde el cache
     */
    private String obtenerNombreCliente(Long idCliente) {
        return clienteNombresCache.getOrDefault(idCliente, "Cliente #" + idCliente);
    }
    
    /**
     * Obtiene el nombre del asesor desde el cache
     */
    private String obtenerNombreAsesor(Long idAsesor) {
        return asesorNombresCache.getOrDefault(idAsesor, "Asesor #" + idAsesor);
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
     * Abre la ventana de detalles del préstamo
     */
    private void abrirVentanaDetalles(Prestamo prestamo) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/DetallePrestamoView.fxml"));
            Scene scene = new Scene(loader.load());
            
            // Obtener el controlador y cargar los datos
            DetallePrestamoController controller = loader.getController();
            controller.cargarPrestamo(prestamo);
            
            // Crear y configurar la ventana
            Stage detalleStage = new Stage();
            detalleStage.setTitle("Detalle de Préstamo - ID: " + prestamo.getIdPrestamo());
            detalleStage.setScene(scene);
            detalleStage.initModality(Modality.APPLICATION_MODAL);
            detalleStage.setResizable(true);
            
            // Mostrar la ventana
            detalleStage.showAndWait();
            
            // Refrescar la tabla después de cerrar la ventana de detalles
            cargarSolicitudes();
            
            // Mostrar mensaje de confirmación de actualización
            mostrarInfo("🔄 Tabla actualizada correctamente.\n\n" +
                       "📊 Se han recargado las solicitudes pendientes desde la base de datos.\n" +
                       "✅ Los préstamos aprobados o rechazados ya no aparecen en la lista.");
            
            logger.info("Ventana de detalles abierta para préstamo ID: " + prestamo.getIdPrestamo());
            
        } catch (IOException e) {
            logger.error("Error al abrir ventana de detalles", e);
            mostrarError("Error al abrir la ventana de detalles: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado al abrir ventana de detalles", e);
            mostrarError("Error inesperado al abrir la ventana de detalles");
        }
    }
    
    /**
     * Maneja el botón de actualizar
     */
    @FXML
    private void handleActualizar() {
        try {
            cargarSolicitudes();
            mostrarInfo("Datos actualizados correctamente");
        } catch (Exception e) {
            logger.error("Error al actualizar datos", e);
            mostrarError("Error al actualizar los datos");
        }
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