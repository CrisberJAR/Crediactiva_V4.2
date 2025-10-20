package pe.crediactiva.app.view.asesor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import pe.crediactiva.app.model.RecaudacionAsesor;
import pe.crediactiva.app.service.RecaudacionService;
import pe.crediactiva.app.config.SessionManager;
import pe.crediactiva.app.util.FechaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controlador para la recaudación del día del asesor
 */
public class RecaudacionDiaController {
    
    private static final Logger logger = LoggerFactory.getLogger(RecaudacionDiaController.class);
    
    @FXML
    private Label lblTotalRecaudado;
    
    @FXML
    private Label lblTotalCobros;
    
    @FXML
    private Label lblTotalValidados;
    
    @FXML
    private Label lblTotalPendientes;
    
    @FXML
    private TextField txtBuscar;
    
    @FXML
    private ComboBox<String> cmbEstado;
    
    @FXML
    private TableView<RecaudacionAsesor> tblRecaudacion;
    
    @FXML
    private TableColumn<RecaudacionAsesor, Integer> colIdRecaudacion;
    
    @FXML
    private TableColumn<RecaudacionAsesor, String> colCliente;
    
    @FXML
    private TableColumn<RecaudacionAsesor, String> colPrestamo;
    
    @FXML
    private TableColumn<RecaudacionAsesor, String> colMonto;
    
    @FXML
    private TableColumn<RecaudacionAsesor, String> colFechaRegistro;
    
    @FXML
    private TableColumn<RecaudacionAsesor, String> colEstado;
    
    @FXML
    private TableColumn<RecaudacionAsesor, String> colAcciones;
    
    private RecaudacionService recaudacionService;
    private ObservableList<RecaudacionAsesor> recaudaciones;
    private String busquedaActual = "";
    private String filtroActual = "";
    
    public RecaudacionDiaController() {
        this.recaudacionService = new RecaudacionService();
        this.recaudaciones = FXCollections.observableArrayList();
    }
    
    @FXML
    private void initialize() {
        try {
            configurarTabla();
            configurarFiltros();
            cargarRecaudacionDelDia();
            actualizarResumen();
            
        } catch (Exception e) {
            logger.error("Error al inicializar recaudación del día", e);
            mostrarError("Error al inicializar la recaudación del día");
        }
    }
    
    /**
     * Configura la tabla de recaudación
     */
    private void configurarTabla() {
        colIdRecaudacion.setCellValueFactory(new PropertyValueFactory<>("idRecaudacion"));
        
        colCliente.setCellValueFactory(cellData -> {
            RecaudacionAsesor recaudacion = cellData.getValue();
            // TODO: Obtener nombre del cliente desde el ID
            return new javafx.beans.property.SimpleStringProperty("Cliente " + recaudacion.getIdCliente());
        });
        
        colPrestamo.setCellValueFactory(cellData -> {
            RecaudacionAsesor recaudacion = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty("Préstamo " + recaudacion.getIdPrestamo());
        });
        
        colMonto.setCellValueFactory(cellData -> {
            RecaudacionAsesor recaudacion = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty("S/ " + String.format("%.2f", recaudacion.getMontoRegistrado()));
        });
        
        colFechaRegistro.setCellValueFactory(cellData -> {
            RecaudacionAsesor recaudacion = cellData.getValue();
            if (recaudacion.getFechaRegistro() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    FechaUtil.formatearFecha(recaudacion.getFechaRegistro().toLocalDate())
                );
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        
        colEstado.setCellValueFactory(cellData -> {
            RecaudacionAsesor recaudacion = cellData.getValue();
            String estado = recaudacion.isValidado() ? "Validado" : "Pendiente";
            return new javafx.beans.property.SimpleStringProperty(estado);
        });
        
        colAcciones.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty("Ver"));
        
        tblRecaudacion.setItems(recaudaciones);
    }
    
    /**
     * Configura los filtros
     */
    private void configurarFiltros() {
        cmbEstado.getItems().addAll("Todos", "Validados", "Pendientes");
        cmbEstado.setValue("Todos");
        
        cmbEstado.setOnAction(e -> handleFiltrar());
    }
    
    /**
     * Carga la recaudación del día filtrada por asesor
     */
    private void cargarRecaudacionDelDia() {
        try {
            // Obtener el ID del asesor actual de la sesión
            Long idAsesor = SessionManager.getInstance().getAsesorId();
            
            if (idAsesor == null) {
                logger.error("No se pudo obtener el ID del asesor de la sesión");
                mostrarError("Error: No se pudo identificar al asesor");
                return;
            }
            
            logger.info("Cargando recaudación del día para el asesor: " + idAsesor);
            
            // Obtener recaudación del día filtrada por asesor
            List<RecaudacionAsesor> recaudacionDelDia = recaudacionService.obtenerRecaudacionDelDiaPorAsesor(idAsesor);
            
            recaudaciones.clear();
            recaudaciones.addAll(recaudacionDelDia);
            
            logger.info("Cargadas " + recaudacionDelDia.size() + " recaudaciones del día para el asesor: " + idAsesor);
            
        } catch (Exception e) {
            logger.error("Error al cargar recaudación del día", e);
            mostrarError("Error al cargar la recaudación del día");
        }
    }
    
    /**
     * Actualiza el resumen de recaudación
     */
    private void actualizarResumen() {
        try {
            BigDecimal totalRecaudado = BigDecimal.ZERO;
            int totalCobros = recaudaciones.size();
            int totalValidados = 0;
            int totalPendientes = 0;
            
            for (RecaudacionAsesor recaudacion : recaudaciones) {
                totalRecaudado = totalRecaudado.add(recaudacion.getMontoRegistrado());
                
                if (recaudacion.isValidado()) {
                    totalValidados++;
                } else {
                    totalPendientes++;
                }
            }
            
            lblTotalRecaudado.setText("S/ " + String.format("%.2f", totalRecaudado));
            lblTotalCobros.setText(String.valueOf(totalCobros));
            lblTotalValidados.setText(String.valueOf(totalValidados));
            lblTotalPendientes.setText(String.valueOf(totalPendientes));
            
        } catch (Exception e) {
            logger.error("Error al actualizar resumen de recaudación", e);
        }
    }
    
    /**
     * Maneja la búsqueda de recaudación
     */
    @FXML
    private void handleBuscar() {
        String textoBusqueda = txtBuscar.getText().trim();
        
        if (textoBusqueda.isEmpty()) {
            busquedaActual = "";
            aplicarFiltros();
            return;
        }
        
        // Validar que el texto de búsqueda sea un número válido
        try {
            Long.parseLong(textoBusqueda);
            busquedaActual = textoBusqueda;
            
            // Verificar si el ID buscado pertenece al asesor actual
            if (!verificarPermisosBusqueda(textoBusqueda)) {
                mostrarError("El ID ingresado no pertenece a sus clientes asignados.");
                return;
            }
            
            aplicarFiltros();
            
        } catch (NumberFormatException e) {
            mostrarError("Por favor ingrese un ID válido (número).");
            txtBuscar.clear();
        }
    }
    
    /**
     * Maneja el filtrado por estado
     */
    @FXML
    private void handleFiltrar() {
        filtroActual = cmbEstado.getValue();
        aplicarFiltros();
    }
    
    /**
     * Verifica si el ID de búsqueda pertenece a los clientes del asesor actual
     */
    private boolean verificarPermisosBusqueda(String idBuscado) {
        try {
            Long idAsesor = SessionManager.getInstance().getAsesorId();
            if (idAsesor == null) {
                logger.error("No se pudo obtener el ID del asesor de la sesión");
                return false;
            }
            
            // Obtener todas las recaudaciones del asesor para verificar permisos
            List<RecaudacionAsesor> recaudacionesDelAsesor = recaudacionService.obtenerRecaudacionDelDiaPorAsesor(idAsesor);
            
            // Verificar si el ID buscado corresponde a algún cliente o préstamo del asesor
            Long idBuscadoLong = Long.parseLong(idBuscado);
            
            for (RecaudacionAsesor recaudacion : recaudacionesDelAsesor) {
                if (recaudacion.getIdCliente().equals(idBuscadoLong) || 
                    recaudacion.getIdPrestamo().equals(idBuscadoLong)) {
                    return true;
                }
            }
            
            logger.info("ID buscado " + idBuscado + " no pertenece al asesor " + idAsesor);
            return false;
            
        } catch (Exception e) {
            logger.error("Error al verificar permisos de búsqueda para ID: " + idBuscado, e);
            return false;
        }
    }
    
    /**
     * Aplica los filtros de búsqueda y estado
     */
    private void aplicarFiltros() {
        try {
            // Obtener el ID del asesor actual de la sesión
            Long idAsesor = SessionManager.getInstance().getAsesorId();
            
            if (idAsesor == null) {
                logger.error("No se pudo obtener el ID del asesor de la sesión");
                return;
            }
            
            // Obtener recaudación del día filtrada por asesor
            List<RecaudacionAsesor> recaudacionDelDia = recaudacionService.obtenerRecaudacionDelDiaPorAsesor(idAsesor);
            
            // Aplicar filtros
            ObservableList<RecaudacionAsesor> recaudacionesFiltradas = FXCollections.observableArrayList();
            
            for (RecaudacionAsesor recaudacion : recaudacionDelDia) {
                boolean cumpleBusqueda = busquedaActual.isEmpty() || 
                    String.valueOf(recaudacion.getIdCliente()).contains(busquedaActual) ||
                    String.valueOf(recaudacion.getIdPrestamo()).contains(busquedaActual);
                
                boolean cumpleFiltro = filtroActual.equals("Todos") ||
                    (filtroActual.equals("Validados") && recaudacion.isValidado()) ||
                    (filtroActual.equals("Pendientes") && !recaudacion.isValidado());
                
                if (cumpleBusqueda && cumpleFiltro) {
                    recaudacionesFiltradas.add(recaudacion);
                }
            }
            
            recaudaciones.clear();
            recaudaciones.addAll(recaudacionesFiltradas);
            actualizarResumen();
            
            // Mostrar mensaje informativo si hay búsqueda activa
            if (!busquedaActual.isEmpty()) {
                if (recaudacionesFiltradas.isEmpty()) {
                    mostrarInfo("No se encontraron registros para el ID: " + busquedaActual);
                } else {
                    mostrarInfo("Se encontraron " + recaudacionesFiltradas.size() + " registro(s) para el ID: " + busquedaActual);
                }
            }
            
        } catch (Exception e) {
            logger.error("Error al aplicar filtros", e);
            mostrarError("Error al aplicar filtros");
        }
    }
    
    /**
     * Maneja la actualización de la vista
     */
    @FXML
    private void handleActualizar() {
        // Limpiar búsqueda y filtros
        txtBuscar.clear();
        busquedaActual = "";
        cmbEstado.setValue("Todos");
        filtroActual = "Todos";
        
        cargarRecaudacionDelDia();
        actualizarResumen();
        mostrarInfo("Recaudación del día actualizada");
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
     * Maneja el botón de regresar al dashboard
     */
    @FXML
    private void handleRegresarDashboard() {
        try {
            // Obtener el stage actual
            Stage stage = (Stage) txtBuscar.getScene().getWindow();
            
            // Cargar la vista principal del asesor
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/asesor/AsesorMainView.fxml"));
            Parent root = loader.load();
            
            // Configurar la nueva escena
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            stage.setScene(scene);
            stage.setTitle("Dashboard - Asesor");
            stage.centerOnScreen();
            
        } catch (Exception e) {
            logger.error("Error al regresar al dashboard", e);
            mostrarError("Error al regresar al dashboard");
        }
    }
}
