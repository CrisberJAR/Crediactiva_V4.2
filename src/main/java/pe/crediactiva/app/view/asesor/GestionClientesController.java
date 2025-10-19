package pe.crediactiva.app.view.asesor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import pe.crediactiva.app.model.Cliente;
import pe.crediactiva.app.service.ClienteService;
import pe.crediactiva.app.util.FechaUtil;
import pe.crediactiva.app.config.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Controlador para la gestión de clientes del asesor
 */
public class GestionClientesController {
    
    private static final Logger logger = LoggerFactory.getLogger(GestionClientesController.class);
    
    @FXML
    private TextField txtBuscar;
    
    @FXML
    private ComboBox<String> cmbFiltro;
    
    @FXML
    private TableView<Cliente> tblClientes;
    
    @FXML
    private TableColumn<Cliente, Integer> colId;
    
    @FXML
    private TableColumn<Cliente, String> colNombre;
    
    @FXML
    private TableColumn<Cliente, String> colDni;
    
    @FXML
    private TableColumn<Cliente, String> colTelefono;
    
    @FXML
    private TableColumn<Cliente, String> colEmail;
    
    @FXML
    private TableColumn<Cliente, String> colEstado;
    
    @FXML
    private TableColumn<Cliente, Integer> colPrestamos;
    
    @FXML
    private TableColumn<Cliente, String> colFechaRegistro;
    
    @FXML
    private Label lblInfoPaginacion;
    
    @FXML
    private Button btnAnterior;
    
    @FXML
    private Button btnSiguiente;
    
    private ClienteService clienteService;
    private ObservableList<Cliente> clientes;
    private int paginaActual = 1;
    private int elementosPorPagina = 20;
    private String filtroActual = "";
    private String busquedaActual = "";
    
    public GestionClientesController() {
        this.clienteService = new ClienteService();
        this.clientes = FXCollections.observableArrayList();
    }
    
    @FXML
    private void initialize() {
        try {
            logger.info("Inicializando gestión de clientes...");
            configurarTabla();
            cargarClientes();
            configurarFiltros();
            logger.info("Gestión de clientes inicializada correctamente");
            
        } catch (Exception e) {
            logger.error("Error al inicializar gestión de clientes", e);
            mostrarError("Error al inicializar la gestión de clientes: " + e.getMessage());
        }
    }
    
    /**
     * Configura las columnas de la tabla
     */
    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idCliente"));
        colNombre.setCellValueFactory(cellData -> {
            Cliente cliente = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                cliente.getNombre() + " " + cliente.getApellido()
            );
        });
        colDni.setCellValueFactory(new PropertyValueFactory<>("dni"));
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEstado.setCellValueFactory(cellData -> {
            Cliente cliente = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                cliente.isActivo() ? "Activo" : "Inactivo"
            );
        });
        colPrestamos.setCellValueFactory(new PropertyValueFactory<>("prestamos"));
        colFechaRegistro.setCellValueFactory(cellData -> {
            Cliente cliente = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                FechaUtil.formatearFecha(cliente.getFechaRegistro())
            );
        });
        
        // Configurar selección de fila
        tblClientes.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }
    
    /**
     * Configura los filtros disponibles
     */
    private void configurarFiltros() {
        cmbFiltro.getItems().addAll("Todos", "Activos", "Inactivos", "Con préstamos", "Sin préstamos");
        cmbFiltro.setValue("Todos");
    }
    
    /**
     * Carga los clientes del asesor según los filtros actuales
     */
    private void cargarClientes() {
        try {
            // Obtener el ID del asesor actual de la sesión
            Long idAsesor = SessionManager.getInstance().getAsesorId();
            
            if (idAsesor == null) {
                logger.error("No se pudo obtener el ID del asesor de la sesión");
                mostrarError("Error: No se pudo identificar al asesor");
                return;
            }
            
            // Obtener solo los clientes del asesor actual
            List<Cliente> listaClientes = clienteService.obtenerClientesPorAsesor(idAsesor);
            
            clientes.clear();
            clientes.addAll(listaClientes);
            tblClientes.setItems(clientes);
            
            actualizarInfoPaginacion();
            
            logger.info("Cargados " + listaClientes.size() + " clientes para el asesor: " + idAsesor);
            
        } catch (Exception e) {
            logger.error("Error al cargar clientes", e);
            mostrarError("Error al cargar la lista de clientes");
        }
    }
    
    /**
     * Actualiza la información de paginación
     */
    private void actualizarInfoPaginacion() {
        try {
            // TODO: Implementar conteo en ClienteService
            int totalClientes = clientes.size();
            int inicio = (paginaActual - 1) * elementosPorPagina + 1;
            int fin = Math.min(paginaActual * elementosPorPagina, totalClientes);
            
            lblInfoPaginacion.setText(
                String.format("Mostrando %d-%d de %d clientes", inicio, fin, totalClientes)
            );
            
            // Habilitar/deshabilitar botones de paginación
            btnAnterior.setDisable(paginaActual <= 1);
            btnSiguiente.setDisable(fin >= totalClientes);
            
        } catch (Exception e) {
            logger.error("Error al actualizar información de paginación", e);
        }
    }
    
    /**
     * Maneja la búsqueda de clientes
     */
    @FXML
    private void handleBuscar() {
        busquedaActual = txtBuscar.getText().trim();
        paginaActual = 1;
        // TODO: Implementar búsqueda en ClienteService
        cargarClientes();
        logger.info("Búsqueda realizada: " + busquedaActual);
    }
    
    /**
     * Limpia los filtros de búsqueda
     */
    @FXML
    private void handleLimpiar() {
        txtBuscar.clear();
        cmbFiltro.setValue("Todos");
        busquedaActual = "";
        filtroActual = "";
        paginaActual = 1;
        cargarClientes();
        logger.info("Filtros limpiados");
    }
    
    /**
     * Maneja el filtrado de clientes
     */
    @FXML
    private void handleFiltrar() {
        String filtroSeleccionado = cmbFiltro.getValue();
        if (filtroSeleccionado != null) {
            switch (filtroSeleccionado) {
                case "Activos":
                    filtroActual = "activos";
                    break;
                case "Inactivos":
                    filtroActual = "inactivos";
                    break;
                case "Con préstamos":
                    filtroActual = "con_prestamos";
                    break;
                case "Sin préstamos":
                    filtroActual = "sin_prestamos";
                    break;
                default:
                    filtroActual = "";
                    break;
            }
        } else {
            filtroActual = "";
        }
        
        paginaActual = 1;
        cargarClientes();
        logger.info("Filtro aplicado: " + filtroActual);
    }
    
    /**
     * Maneja la creación de un nuevo cliente
     */
    @FXML
    private void handleNuevoCliente() {
        try {
            // TODO: Implementar formulario de nuevo cliente
            mostrarInfo("Funcionalidad de nuevo cliente en desarrollo");
            
        } catch (Exception e) {
            logger.error("Error al crear nuevo cliente", e);
            mostrarError("Error al abrir el formulario de nuevo cliente");
        }
    }
    
    /**
     * Maneja la edición de un cliente seleccionado
     */
    @FXML
    private void handleEditarCliente() {
        Cliente clienteSeleccionado = tblClientes.getSelectionModel().getSelectedItem();
        
        if (clienteSeleccionado == null) {
            mostrarAdvertencia("Por favor seleccione un cliente para editar");
            return;
        }
        
        try {
            // TODO: Implementar formulario de edición de cliente
            mostrarInfo("Funcionalidad de edición de cliente en desarrollo");
            
        } catch (Exception e) {
            logger.error("Error al editar cliente", e);
            mostrarError("Error al abrir el formulario de edición");
        }
    }
    
    /**
     * Maneja la visualización de detalles del cliente
     */
    @FXML
    private void handleVerDetalles() {
        Cliente clienteSeleccionado = tblClientes.getSelectionModel().getSelectedItem();
        
        if (clienteSeleccionado == null) {
            mostrarAdvertencia("Por favor seleccione un cliente para ver sus detalles");
            return;
        }
        
        try {
            // TODO: Implementar vista de detalles del cliente
            mostrarInfo("Funcionalidad de detalles de cliente en desarrollo");
            
        } catch (Exception e) {
            logger.error("Error al mostrar detalles del cliente", e);
            mostrarError("Error al mostrar los detalles del cliente");
        }
    }
    
    /**
     * Maneja la visualización del historial del cliente
     */
    @FXML
    private void handleHistorial() {
        Cliente clienteSeleccionado = tblClientes.getSelectionModel().getSelectedItem();
        
        if (clienteSeleccionado == null) {
            mostrarAdvertencia("Por favor seleccione un cliente para ver su historial");
            return;
        }
        
        try {
            // TODO: Implementar vista de historial del cliente
            mostrarInfo("Funcionalidad de historial de cliente en desarrollo");
            
        } catch (Exception e) {
            logger.error("Error al mostrar historial del cliente", e);
            mostrarError("Error al mostrar el historial del cliente");
        }
    }
    
    /**
     * Actualiza la lista de clientes
     */
    @FXML
    private void handleActualizar() {
        cargarClientes();
        mostrarInfo("Lista de clientes actualizada");
    }
    
    /**
     * Navega a la página anterior
     */
    @FXML
    private void handleAnterior() {
        if (paginaActual > 1) {
            paginaActual--;
            cargarClientes();
        }
    }
    
    /**
     * Navega a la página siguiente
     */
    @FXML
    private void handleSiguiente() {
        try {
            // TODO: Implementar paginación real
            int totalClientes = clientes.size();
            int totalPaginas = (int) Math.ceil((double) totalClientes / elementosPorPagina);
            
            if (paginaActual < totalPaginas) {
                paginaActual++;
                cargarClientes();
            }
        } catch (Exception e) {
            logger.error("Error al navegar a la página siguiente", e);
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
