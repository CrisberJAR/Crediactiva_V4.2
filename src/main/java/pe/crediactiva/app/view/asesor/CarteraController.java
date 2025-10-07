package pe.crediactiva.app.view.asesor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import pe.crediactiva.app.model.Cliente;
import pe.crediactiva.app.model.Cronograma;
import pe.crediactiva.app.model.Pago;
import pe.crediactiva.app.model.Prestamo;
import pe.crediactiva.app.service.ClienteService;
import pe.crediactiva.app.service.PagoService;
import pe.crediactiva.app.service.PrestamoService;
import pe.crediactiva.app.service.RecaudacionService;
import pe.crediactiva.app.util.FechaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador para la cartera del asesor
 */
public class CarteraController {
    
    private static final Logger logger = LoggerFactory.getLogger(CarteraController.class);
    
    @FXML
    private Label lblTotalClientes;
    
    @FXML
    private Label lblPrestamosActivos;
    
    @FXML
    private Label lblMontoTotalPrestado;
    
    @FXML
    private Label lblSaldoPorCobrar;
    
    @FXML
    private Label lblRecaudacionMes;
    
    @FXML
    private Label lblMorosidad;
    
    @FXML
    private TextField txtBuscar;
    
    @FXML
    private ComboBox<String> cmbFiltro;
    
    @FXML
    private TableView<Cliente> tblClientes;
    
    @FXML
    private TableColumn<Cliente, Integer> colIdCliente;
    
    @FXML
    private TableColumn<Cliente, String> colNombreCliente;
    
    @FXML
    private TableColumn<Cliente, String> colDniCliente;
    
    @FXML
    private TableColumn<Cliente, String> colTelefonoCliente;
    
    @FXML
    private TableColumn<Cliente, Integer> colPrestamosCliente;
    
    @FXML
    private TableColumn<Cliente, String> colSaldoCliente;
    
    @FXML
    private TableColumn<Cliente, String> colEstadoCliente;
    
    @FXML
    private TableColumn<Cliente, String> colUltimoPago;
    
    @FXML
    private TabPane tabPaneDetalles;
    
    @FXML
    private TableView<Prestamo> tblPrestamosCliente;
    
    @FXML
    private TableColumn<Prestamo, Integer> colIdPrestamo;
    
    @FXML
    private TableColumn<Prestamo, String> colMontoPrestamo;
    
    @FXML
    private TableColumn<Prestamo, String> colSaldoPrestamo;
    
    @FXML
    private TableColumn<Prestamo, String> colCuotasPrestamo;
    
    @FXML
    private TableColumn<Prestamo, String> colEstadoPrestamo;
    
    @FXML
    private TableColumn<Prestamo, String> colFechaInicioPrestamo;
    
    @FXML
    private TableView<Cronograma> tblCronogramaCliente;
    
    @FXML
    private TableColumn<Cronograma, Integer> colNumeroCuota;
    
    @FXML
    private TableColumn<Cronograma, String> colFechaVencimiento;
    
    @FXML
    private TableColumn<Cronograma, String> colMontoCuota;
    
    @FXML
    private TableColumn<Cronograma, String> colEstadoCuota;
    
    @FXML
    private TableColumn<Cronograma, String> colDiasVencido;
    
    @FXML
    private TableView<Pago> tblHistorialPagos;
    
    @FXML
    private TableColumn<Pago, String> colFechaPago;
    
    @FXML
    private TableColumn<Pago, String> colMontoPago;
    
    @FXML
    private TableColumn<Pago, String> colMetodoPago;
    
    @FXML
    private TableColumn<Pago, String> colReferenciaPago;
    
    @FXML
    private TableColumn<Pago, String> colObservacionesPago;
    
    private ClienteService clienteService;
    private PrestamoService prestamoService;
    private PagoService pagoService;
    private RecaudacionService recaudacionService;
    private ObservableList<Cliente> clientes;
    private ObservableList<Prestamo> prestamosCliente;
    private ObservableList<Cronograma> cronogramaCliente;
    private ObservableList<Pago> historialPagos;
    private Cliente clienteSeleccionado;
    
    public CarteraController() {
        this.clienteService = new ClienteService();
        this.prestamoService = new PrestamoService();
        this.pagoService = new PagoService();
        this.recaudacionService = new RecaudacionService();
        this.clientes = FXCollections.observableArrayList();
        this.prestamosCliente = FXCollections.observableArrayList();
        this.cronogramaCliente = FXCollections.observableArrayList();
        this.historialPagos = FXCollections.observableArrayList();
    }
    
    @FXML
    private void initialize() {
        try {
            configurarTablas();
            configurarFiltros();
            cargarResumenCartera();
            cargarClientes();
            
        } catch (Exception e) {
            logger.error("Error al inicializar cartera", e);
            mostrarError("Error al inicializar la cartera");
        }
    }
    
    /**
     * Configura las tablas
     */
    private void configurarTablas() {
        // Configurar tabla de clientes
        colIdCliente.setCellValueFactory(new PropertyValueFactory<>("idCliente"));
        colNombreCliente.setCellValueFactory(cellData -> {
            Cliente cliente = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                cliente.getNombre() + " " + cliente.getApellido()
            );
        });
        colDniCliente.setCellValueFactory(new PropertyValueFactory<>("dni"));
        colTelefonoCliente.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colPrestamosCliente.setCellValueFactory(new PropertyValueFactory<>("prestamos"));
        colSaldoCliente.setCellValueFactory(cellData -> {
            // TODO: Calcular saldo real
            return new javafx.beans.property.SimpleStringProperty("S/ 0.00");
        });
        colEstadoCliente.setCellValueFactory(cellData -> {
            Cliente cliente = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                cliente.isActivo() ? "Activo" : "Inactivo"
            );
        });
        colUltimoPago.setCellValueFactory(cellData -> {
            // TODO: Obtener fecha del último pago
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        
        tblClientes.setItems(clientes);
        
        // Configurar tabla de préstamos del cliente
        colIdPrestamo.setCellValueFactory(new PropertyValueFactory<>("idPrestamo"));
        colMontoPrestamo.setCellValueFactory(cellData -> {
            Prestamo prestamo = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty("S/ " + String.format("%.2f", prestamo.getMontoSolicitado()));
        });
        colSaldoPrestamo.setCellValueFactory(cellData -> {
            // TODO: Calcular saldo real
            return new javafx.beans.property.SimpleStringProperty("S/ 0.00");
        });
        colCuotasPrestamo.setCellValueFactory(cellData -> {
            Prestamo prestamo = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(prestamo.getPeriodoMeses() + " cuotas");
        });
        colEstadoPrestamo.setCellValueFactory(cellData -> {
            Prestamo prestamo = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(prestamo.getEstado().toString());
        });
        colFechaInicioPrestamo.setCellValueFactory(cellData -> {
            Prestamo prestamo = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(FechaUtil.formatearFecha(prestamo.getFechaInicio()));
        });
        
        tblPrestamosCliente.setItems(prestamosCliente);
        
        // Configurar tabla de cronograma
        colNumeroCuota.setCellValueFactory(new PropertyValueFactory<>("numeroCuota"));
        colFechaVencimiento.setCellValueFactory(cellData -> {
            Cronograma cuota = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(FechaUtil.formatearFecha(cuota.getFechaProgramada()));
        });
        colMontoCuota.setCellValueFactory(cellData -> {
            Cronograma cuota = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty("S/ " + String.format("%.2f", cuota.getMontoCuota()));
        });
        colEstadoCuota.setCellValueFactory(cellData -> {
            Cronograma cuota = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(cuota.getEstadoCuota().toString());
        });
        colDiasVencido.setCellValueFactory(cellData -> {
            Cronograma cuota = cellData.getValue();
            if (cuota.getEstadoCuota() == Cronograma.EstadoCuota.RETRASADA) {
                long diasVencido = ChronoUnit.DAYS.between(cuota.getFechaProgramada(), LocalDate.now());
                return new javafx.beans.property.SimpleStringProperty(String.valueOf(diasVencido));
            } else {
                return new javafx.beans.property.SimpleStringProperty("-");
            }
        });
        
        tblCronogramaCliente.setItems(cronogramaCliente);
        
        // Configurar tabla de historial de pagos
        colFechaPago.setCellValueFactory(cellData -> {
            Pago pago = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(FechaUtil.formatearFecha(pago.getFechaPago().toLocalDate()));
        });
        colMontoPago.setCellValueFactory(cellData -> {
            Pago pago = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty("S/ " + String.format("%.2f", pago.getMontoPagado()));
        });
        colMetodoPago.setCellValueFactory(cellData -> {
            // TODO: Agregar campo método de pago al modelo Pago
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        colReferenciaPago.setCellValueFactory(cellData -> {
            // TODO: Agregar campo referencia al modelo Pago
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        colObservacionesPago.setCellValueFactory(cellData -> {
            // TODO: Agregar campo observaciones al modelo Pago
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        
        tblHistorialPagos.setItems(historialPagos);
        
        // Configurar selección de cliente
        tblClientes.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    clienteSeleccionado = newValue;
                    cargarDetallesCliente(newValue);
                }
            }
        );
    }
    
    /**
     * Configura los filtros disponibles
     */
    private void configurarFiltros() {
        cmbFiltro.getItems().addAll("Todos", "Con préstamos activos", "Con cuotas vencidas", "Al día", "Morosos");
        cmbFiltro.setValue("Todos");
    }
    
    /**
     * Carga el resumen de la cartera
     */
    private void cargarResumenCartera() {
        try {
            // TODO: Implementar métodos en servicios
            // Total de clientes
            int totalClientes = clientes.size();
            lblTotalClientes.setText(String.valueOf(totalClientes));
            
            // Préstamos activos
            int prestamosActivos = prestamosCliente.size();
            lblPrestamosActivos.setText(String.valueOf(prestamosActivos));
            
            // Monto total prestado
            BigDecimal montoTotalPrestado = BigDecimal.ZERO; // TODO: Calcular monto real
            lblMontoTotalPrestado.setText("S/ " + String.format("%.2f", montoTotalPrestado));
            
            // Saldo por cobrar
            BigDecimal saldoPorCobrar = BigDecimal.ZERO; // TODO: Calcular saldo real
            lblSaldoPorCobrar.setText("S/ " + String.format("%.2f", saldoPorCobrar));
            
            // Recaudación del mes
            BigDecimal recaudacionMes = BigDecimal.ZERO; // TODO: Calcular recaudación real
            lblRecaudacionMes.setText("S/ " + String.format("%.2f", recaudacionMes));
            
            // Morosidad
            double morosidad = 0.0; // TODO: Calcular morosidad real
            lblMorosidad.setText(String.format("%.1f%%", morosidad));
            
        } catch (Exception e) {
            logger.error("Error al cargar resumen de cartera", e);
            mostrarError("Error al cargar el resumen de la cartera");
        }
    }
    
    /**
     * Carga la lista de clientes
     */
    private void cargarClientes() {
        try {
            List<Cliente> listaClientes = clienteService.obtenerTodosLosClientes();
            clientes.clear();
            clientes.addAll(listaClientes);
            
        } catch (Exception e) {
            logger.error("Error al cargar clientes", e);
            mostrarError("Error al cargar la lista de clientes");
        }
    }
    
    /**
     * Carga los detalles del cliente seleccionado
     */
    private void cargarDetallesCliente(Cliente cliente) {
        try {
            // TODO: Implementar métodos en servicios
            // Cargar préstamos del cliente
            List<Prestamo> prestamos = new ArrayList<>(); // prestamoService.obtenerPrestamosPorCliente(cliente.getIdCliente());
            prestamosCliente.clear();
            prestamosCliente.addAll(prestamos);
            
            // Cargar cronograma del cliente
            List<Cronograma> cronograma = new ArrayList<>(); // prestamoService.obtenerCronogramaCliente(cliente.getIdCliente());
            cronogramaCliente.clear();
            cronogramaCliente.addAll(cronograma);
            
            // Cargar historial de pagos del cliente
            List<Pago> pagos = new ArrayList<>(); // pagoService.obtenerHistorialPagosCliente(cliente.getIdCliente());
            historialPagos.clear();
            historialPagos.addAll(pagos);
            
        } catch (Exception e) {
            logger.error("Error al cargar detalles del cliente", e);
            mostrarError("Error al cargar los detalles del cliente");
        }
    }
    
    /**
     * Maneja la búsqueda de clientes
     */
    @FXML
    private void handleBuscar() {
        String busqueda = txtBuscar.getText().trim();
        
        try {
            // TODO: Implementar búsqueda en ClienteService
            List<Cliente> clientesEncontrados = clienteService.obtenerTodosLosClientes();
            clientes.clear();
            clientes.addAll(clientesEncontrados);
            
            logger.info("Búsqueda realizada: " + busqueda);
            
        } catch (Exception e) {
            logger.error("Error al buscar clientes", e);
            mostrarError("Error al buscar clientes");
        }
    }
    
    /**
     * Maneja la limpieza de filtros
     */
    @FXML
    private void handleLimpiar() {
        txtBuscar.clear();
        cmbFiltro.setValue("Todos");
        cargarClientes();
        logger.info("Filtros limpiados");
    }
    
    /**
     * Maneja el filtrado de clientes
     */
    @FXML
    private void handleFiltrar() {
        String filtroSeleccionado = cmbFiltro.getValue();
        
        try {
            // TODO: Implementar filtros en ClienteService
            List<Cliente> clientesFiltrados = clienteService.obtenerTodosLosClientes();
            
            clientes.clear();
            clientes.addAll(clientesFiltrados);
            
            logger.info("Filtro aplicado: " + filtroSeleccionado);
            
        } catch (Exception e) {
            logger.error("Error al filtrar clientes", e);
            mostrarError("Error al filtrar clientes");
        }
    }
    
    /**
     * Maneja la actualización de la cartera
     */
    @FXML
    private void handleActualizar() {
        cargarResumenCartera();
        cargarClientes();
        mostrarInfo("Cartera actualizada");
    }
    
    /**
     * Maneja el registro de pago
     */
    @FXML
    private void handleRegistrarPago() {
        if (clienteSeleccionado == null) {
            mostrarAdvertencia("Por favor seleccione un cliente para registrar un pago");
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
        if (clienteSeleccionado == null) {
            mostrarAdvertencia("Por favor seleccione un cliente para contactar");
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
     * Maneja la generación de reporte
     */
    @FXML
    private void handleGenerarReporte() {
        try {
            // TODO: Implementar generación de reportes
            mostrarInfo("Funcionalidad de reportes en desarrollo");
            
        } catch (Exception e) {
            logger.error("Error al generar reporte", e);
            mostrarError("Error al generar reporte");
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
