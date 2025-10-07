package pe.crediactiva.app.view.asesor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import pe.crediactiva.app.model.Cronograma;
import pe.crediactiva.app.service.PrestamoService;
import pe.crediactiva.app.util.FechaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controlador para las cuotas del día del asesor
 */
public class CuotasDiaController {
    
    private static final Logger logger = LoggerFactory.getLogger(CuotasDiaController.class);
    
    @FXML
    private Label lblTotalCuotas;
    
    @FXML
    private Label lblMontoTotal;
    
    @FXML
    private Label lblCuotasPagadas;
    
    @FXML
    private Label lblCuotasPendientes;
    
    @FXML
    private TextField txtBuscar;
    
    @FXML
    private ComboBox<String> cmbEstado;
    
    @FXML
    private TableView<Cronograma> tblCuotas;
    
    @FXML
    private TableColumn<Cronograma, Integer> colIdCuota;
    
    @FXML
    private TableColumn<Cronograma, String> colCliente;
    
    @FXML
    private TableColumn<Cronograma, String> colDni;
    
    @FXML
    private TableColumn<Cronograma, String> colTelefono;
    
    @FXML
    private TableColumn<Cronograma, Integer> colNumeroCuota;
    
    @FXML
    private TableColumn<Cronograma, String> colMontoCuota;
    
    @FXML
    private TableColumn<Cronograma, String> colEstadoCuota;
    
    @FXML
    private TableColumn<Cronograma, String> colFechaVencimiento;
    
    @FXML
    private TableColumn<Cronograma, String> colAcciones;
    
    private PrestamoService prestamoService;
    private ObservableList<Cronograma> cuotas;
    private String busquedaActual = "";
    private String filtroActual = "";
    
    public CuotasDiaController() {
        this.prestamoService = new PrestamoService();
        this.cuotas = FXCollections.observableArrayList();
    }
    
    @FXML
    private void initialize() {
        try {
            configurarTabla();
            configurarFiltros();
            cargarCuotasDelDia();
            actualizarResumen();
            
        } catch (Exception e) {
            logger.error("Error al inicializar cuotas del día", e);
            mostrarError("Error al inicializar las cuotas del día");
        }
    }
    
    /**
     * Configura la tabla de cuotas
     */
    private void configurarTabla() {
        colIdCuota.setCellValueFactory(new PropertyValueFactory<>("idCronograma"));
        colCliente.setCellValueFactory(cellData -> {
            Cronograma cuota = cellData.getValue();
            if (cuota.getPrestamo() != null && cuota.getPrestamo().getCliente() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    cuota.getPrestamo().getCliente().getNombre() + " " + 
                    cuota.getPrestamo().getCliente().getApellido()
                );
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        colDni.setCellValueFactory(cellData -> {
            Cronograma cuota = cellData.getValue();
            if (cuota.getPrestamo() != null && cuota.getPrestamo().getCliente() != null) {
                return new javafx.beans.property.SimpleStringProperty(cuota.getPrestamo().getCliente().getTelefono());
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        colTelefono.setCellValueFactory(cellData -> {
            Cronograma cuota = cellData.getValue();
            if (cuota.getPrestamo() != null && cuota.getPrestamo().getCliente() != null) {
                return new javafx.beans.property.SimpleStringProperty(cuota.getPrestamo().getCliente().getTelefono());
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        colNumeroCuota.setCellValueFactory(new PropertyValueFactory<>("numeroCuota"));
        colMontoCuota.setCellValueFactory(cellData -> {
            Cronograma cuota = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty("S/ " + String.format("%.2f", cuota.getMontoCuota()));
        });
        colEstadoCuota.setCellValueFactory(cellData -> {
            Cronograma cuota = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(cuota.getEstadoCuota().toString());
        });
        colFechaVencimiento.setCellValueFactory(cellData -> {
            Cronograma cuota = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(FechaUtil.formatearFecha(cuota.getFechaProgramada()));
        });
        colAcciones.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty("Ver | Pagar | Contactar");
        });
        
        tblCuotas.setItems(cuotas);
        
        // Configurar selección múltiple
        tblCuotas.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
    }
    
    /**
     * Configura los filtros disponibles
     */
    private void configurarFiltros() {
        cmbEstado.getItems().addAll("Todos", "Pendientes", "Pagadas", "Vencidas");
        cmbEstado.setValue("Todos");
    }
    
    /**
     * Carga las cuotas del día
     */
    private void cargarCuotasDelDia() {
        try {
            List<Cronograma> cuotasDelDia = prestamoService.obtenerCuotasDelDia();
            cuotas.clear();
            cuotas.addAll(cuotasDelDia);
            
        } catch (Exception e) {
            logger.error("Error al cargar cuotas del día", e);
            mostrarError("Error al cargar las cuotas del día");
        }
    }
    
    /**
     * Actualiza el resumen de cuotas
     */
    private void actualizarResumen() {
        try {
            int totalCuotas = cuotas.size();
            lblTotalCuotas.setText(String.valueOf(totalCuotas));
            
            BigDecimal montoTotal = BigDecimal.ZERO;
            int cuotasPagadas = 0;
            int cuotasPendientes = 0;
            
            for (Cronograma cuota : cuotas) {
                montoTotal = montoTotal.add(cuota.getMontoCuota());
                
                if (cuota.getEstadoCuota() == Cronograma.EstadoCuota.PAGADA) {
                    cuotasPagadas++;
                } else if (cuota.getEstadoCuota() == Cronograma.EstadoCuota.PENDIENTE) {
                    cuotasPendientes++;
                }
            }
            
            lblMontoTotal.setText("S/ " + String.format("%.2f", montoTotal));
            lblCuotasPagadas.setText(String.valueOf(cuotasPagadas));
            lblCuotasPendientes.setText(String.valueOf(cuotasPendientes));
            
        } catch (Exception e) {
            logger.error("Error al actualizar resumen", e);
        }
    }
    
    /**
     * Maneja la búsqueda de cuotas
     */
    @FXML
    private void handleBuscar() {
        busquedaActual = txtBuscar.getText().trim();
        
        try {
            // TODO: Implementar búsqueda en PrestamoService
            List<Cronograma> cuotasEncontradas = prestamoService.obtenerCuotasDelDia();
            cuotas.clear();
            cuotas.addAll(cuotasEncontradas);
            actualizarResumen();
            
            logger.info("Búsqueda realizada: " + busquedaActual);
            
        } catch (Exception e) {
            logger.error("Error al buscar cuotas", e);
            mostrarError("Error al buscar cuotas");
        }
    }
    
    /**
     * Maneja la limpieza de filtros
     */
    @FXML
    private void handleLimpiar() {
        txtBuscar.clear();
        cmbEstado.setValue("Todos");
        busquedaActual = "";
        filtroActual = "";
        cargarCuotasDelDia();
        actualizarResumen();
        logger.info("Filtros limpiados");
    }
    
    /**
     * Maneja el filtrado por estado
     */
    @FXML
    private void handleFiltrar() {
        String estadoSeleccionado = cmbEstado.getValue();
        
        try {
            // TODO: Implementar filtros en PrestamoService
            List<Cronograma> cuotasFiltradas = prestamoService.obtenerCuotasDelDia();
            
            cuotas.clear();
            cuotas.addAll(cuotasFiltradas);
            actualizarResumen();
            
            logger.info("Filtro aplicado: " + estadoSeleccionado);
            
        } catch (Exception e) {
            logger.error("Error al filtrar cuotas", e);
            mostrarError("Error al filtrar cuotas");
        }
    }
    
    /**
     * Maneja la actualización de cuotas
     */
    @FXML
    private void handleActualizar() {
        // TODO: Implementar actualización en PrestamoService
        cargarCuotasDelDia();
        actualizarResumen();
        mostrarInfo("Cuotas actualizadas");
    }
    
    /**
     * Maneja el registro de pago
     */
    @FXML
    private void handleRegistrarPago() {
        Cronograma cuotaSeleccionada = tblCuotas.getSelectionModel().getSelectedItem();
        
        if (cuotaSeleccionada == null) {
            mostrarAdvertencia("Por favor seleccione una cuota para registrar el pago");
            return;
        }
        
        try {
            // TODO: Implementar vista de registro de pago
            mostrarInfo("Funcionalidad de registro de pago en desarrollo");
            
        } catch (Exception e) {
            logger.error("Error al registrar pago", e);
            mostrarError("Error al registrar pago");
        }
    }
    
    /**
     * Maneja el contacto con el cliente
     */
    @FXML
    private void handleContactarCliente() {
        Cronograma cuotaSeleccionada = tblCuotas.getSelectionModel().getSelectedItem();
        
        if (cuotaSeleccionada == null) {
            mostrarAdvertencia("Por favor seleccione una cuota para contactar al cliente");
            return;
        }
        
        try {
            // TODO: Implementar funcionalidad de contacto
            mostrarInfo("Funcionalidad de contacto en desarrollo");
            
        } catch (Exception e) {
            logger.error("Error al contactar cliente", e);
            mostrarError("Error al contactar cliente");
        }
    }
    
    /**
     * Maneja la exportación de datos
     */
    @FXML
    private void handleExportar() {
        try {
            // TODO: Implementar exportación
            mostrarInfo("Funcionalidad de exportación en desarrollo");
            
        } catch (Exception e) {
            logger.error("Error al exportar datos", e);
            mostrarError("Error al exportar datos");
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
