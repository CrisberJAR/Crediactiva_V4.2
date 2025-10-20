package pe.crediactiva.app.view.admin;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.crediactiva.app.model.Asesor;
import pe.crediactiva.app.model.Cliente;
import pe.crediactiva.app.model.Prestamo;
import pe.crediactiva.app.service.ClienteService;
import pe.crediactiva.app.service.PrestamoService;
import pe.crediactiva.app.service.AsesorService;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import pe.crediactiva.app.util.DateTimeUtil;
import java.util.ResourceBundle;

/**
 * Controlador para la ventana de detalle de préstamo
 */
public class DetallePrestamoController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(DetallePrestamoController.class);
    
    // Servicios
    private final PrestamoService prestamoService;
    private final ClienteService clienteService;
    private final AsesorService asesorService;
    
    // Préstamo actual
    private Prestamo prestamoActual;
    private Cliente clienteActual;
    
    // Componentes de la interfaz
    @FXML
    private Label lblIdPrestamo;
    
    @FXML
    private Label lblCliente;
    
    @FXML
    private Label lblAsesor;
    
    @FXML
    private Label lblEstado;
    
    @FXML
    private TextField txtMontoSolicitado;
    
    @FXML
    private TextField txtMontoDesembolsado;
    
    @FXML
    private TextField txtTasaInteres;
    
    @FXML
    private TextField txtPeriodo;
    
    @FXML
    private ComboBox<String> comboTipoPago;
    
    @FXML
    private Label lblFechaSolicitud;
    
    @FXML
    private DatePicker dpFechaInicio;
    
    @FXML
    private TextField txtObservacion;
    
    @FXML
    private Label lblDniCliente;
    
    @FXML
    private Label lblTelefonoCliente;
    
    @FXML
    private Label lblDireccionCliente;
    
    @FXML
    private Label lblEtiquetaCliente;
    
    @FXML
    private Button btnVerHistorial;
    
    @FXML
    private Button btnAprobar;
    
    @FXML
    private Button btnRechazar;
    
    @FXML
    private Button btnGuardar;
    
    @FXML
    private Button btnCerrar;
    
    /**
     * Constructor
     */
    public DetallePrestamoController() {
        this.prestamoService = new PrestamoService();
        this.clienteService = new ClienteService();
        this.asesorService = new AsesorService();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Configurar combo de tipo de pago
            comboTipoPago.getItems().addAll("diario", "semanal", "mensual");
            comboTipoPago.setValue("diario");
            
            // Configurar validaciones
            configurarValidaciones();
            
            logger.info("Detalle de préstamo inicializado correctamente");
            
        } catch (Exception e) {
            logger.error("Error al inicializar detalle de préstamo", e);
            mostrarError("Error al inicializar la ventana de detalles");
        }
    }
    
    /**
     * Configura las validaciones de los campos
     */
    private void configurarValidaciones() {
        // Validar campos numéricos
        txtTasaInteres.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                txtTasaInteres.setText(oldVal);
            }
        });
        
        txtPeriodo.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtPeriodo.setText(oldVal);
            }
        });
        
        txtMontoSolicitado.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                txtMontoSolicitado.setText(oldVal);
            }
        });
        
        txtMontoDesembolsado.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                txtMontoDesembolsado.setText(oldVal);
            }
        });
    }
    
    /**
     * Carga los datos del préstamo en la interfaz
     */
    public void cargarPrestamo(Prestamo prestamo) {
        try {
            this.prestamoActual = prestamo;
            
            // Cargar información del préstamo
            lblIdPrestamo.setText(prestamo.getIdPrestamo().toString());
            lblEstado.setText(prestamo.getEstado().getDescripcion());
            txtMontoSolicitado.setText(prestamo.getMontoSolicitado().toString());
            lblFechaSolicitud.setText(prestamo.getCreadoEn() != null ? 
                prestamo.getCreadoEn().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A");
            
            // Campos editables
            txtMontoDesembolsado.setText(prestamo.getMontoDesembolsado() != null ? 
                prestamo.getMontoDesembolsado().toString() : "");
            txtTasaInteres.setText(prestamo.getTasaInteres() != null ? 
                prestamo.getTasaInteres().toString() : "");
            txtPeriodo.setText(prestamo.getPeriodoMeses() != 0 ? 
                String.valueOf(prestamo.getPeriodoMeses()) : "");
            
            // Cargar tipo de pago del préstamo
            if (prestamo.getTipoPago() != null) {
                comboTipoPago.setValue(prestamo.getTipoPago().name().toLowerCase());
            }
            
            // CORRECCIÓN: Cargar fecha de inicio del préstamo (por defecto la fecha que puso el asesor)
            if (prestamo.getFechaInicio() != null) {
                dpFechaInicio.setValue(prestamo.getFechaInicio());
            } else {
                // Si no hay fecha de inicio, usar la fecha actual
                dpFechaInicio.setValue(DateTimeUtil.today());
            }
            
            txtObservacion.setText(prestamo.getObservacion() != null ? prestamo.getObservacion() : "");
            
            // Cargar información del cliente
            cargarInformacionCliente(prestamo.getIdCliente());
            
            // Cargar información del asesor
            cargarInformacionAsesor(prestamo.getIdAsesor());
            
            logger.info("Datos del préstamo cargados: ID " + prestamo.getIdPrestamo());
            
        } catch (Exception e) {
            logger.error("Error al cargar datos del préstamo", e);
            mostrarError("Error al cargar los datos del préstamo");
        }
    }
    
    /**
     * Carga la información del cliente
     */
    private void cargarInformacionCliente(Long idCliente) {
        try {
            Optional<Cliente> clienteOpt = clienteService.obtenerClientePorId(idCliente);
            if (clienteOpt.isPresent()) {
                this.clienteActual = clienteOpt.get();
                
                lblCliente.setText(clienteActual.getNombreCompleto());
                lblDniCliente.setText(clienteActual.getDni());
                lblTelefonoCliente.setText(clienteActual.getTelefono() != null ? clienteActual.getTelefono() : "N/A");
                lblDireccionCliente.setText(clienteActual.getDireccion() != null ? clienteActual.getDireccion() : "N/A");
                lblEtiquetaCliente.setText(clienteActual.getEtiquetaCliente() != null ? 
                    clienteActual.getEtiquetaCliente().getDescripcion() : "N/A");
            } else {
                lblCliente.setText("Cliente no encontrado");
            }
        } catch (Exception e) {
            logger.error("Error al cargar información del cliente", e);
            lblCliente.setText("Error al cargar cliente");
        }
    }
    
    /**
     * Carga la información del asesor
     */
    private void cargarInformacionAsesor(Long idAsesor) {
        try {
            Optional<Asesor> asesorOpt = asesorService.obtenerAsesorPorId(idAsesor);
            if (asesorOpt.isPresent()) {
                Asesor asesor = asesorOpt.get();
                lblAsesor.setText(asesor.getNombreCompleto());
            } else {
                lblAsesor.setText("Asesor #" + idAsesor);
            }
        } catch (Exception e) {
            logger.error("Error al cargar información del asesor", e);
            lblAsesor.setText("Error al cargar asesor");
        }
    }
    
    /**
     * Maneja la aprobación del préstamo
     */
    @FXML
    private void handleAprobar() {
        try {
            if (validarCampos()) {
                // Validar que se haya seleccionado una fecha de inicio
                if (dpFechaInicio.getValue() == null) {
                    mostrarError("Debe seleccionar una fecha de inicio para el préstamo");
                    return;
                }
                
                // Actualizar datos del préstamo
                actualizarDatosPrestamo();
                
                // CORRECCIÓN: Usar la fecha de inicio seleccionada por el administrador
                LocalDate fechaInicio = dpFechaInicio.getValue();
                logger.info("Aprobando préstamo con fecha de inicio: " + fechaInicio);
                
                // Aprobar préstamo usando la fecha de inicio seleccionada
                prestamoService.aprobarPrestamo(
                    prestamoActual.getIdPrestamo(),
                    prestamoActual.getTasaInteres(),
                    prestamoActual.getPeriodoMeses(),
                    prestamoActual.getTipoPago(),
                    fechaInicio
                );
                
                mostrarInfo("Préstamo aprobado exitosamente con fecha de inicio: " + 
                           fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                cerrarVentana();
            }
        } catch (Exception e) {
            logger.error("Error al aprobar préstamo", e);
            mostrarError("Error al aprobar el préstamo: " + e.getMessage());
        }
    }
    
    /**
     * Maneja el rechazo del préstamo
     */
    @FXML
    private void handleRechazar() {
        try {
            // Mostrar diálogo de confirmación
            Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
            confirmDialog.setTitle("Confirmar Rechazo");
            confirmDialog.setHeaderText("¿Está seguro de rechazar este préstamo?");
            confirmDialog.setContentText("Esta acción no se puede deshacer.");
            
            Optional<ButtonType> result = confirmDialog.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                prestamoService.rechazarPrestamo(prestamoActual.getIdPrestamo(), "Rechazado por el administrador");
                mostrarInfo("Préstamo rechazado exitosamente");
                cerrarVentana();
            }
        } catch (Exception e) {
            logger.error("Error al rechazar préstamo", e);
            mostrarError("Error al rechazar el préstamo: " + e.getMessage());
        }
    }
    
    /**
     * Maneja el guardado de cambios
     */
    @FXML
    private void handleGuardar() {
        try {
            if (validarCampos()) {
                actualizarDatosPrestamo();
                prestamoService.actualizarPrestamo(prestamoActual);
                mostrarInfo("Cambios guardados exitosamente");
            }
        } catch (Exception e) {
            logger.error("Error al guardar cambios", e);
            mostrarError("Error al guardar los cambios: " + e.getMessage());
        }
    }
    
    /**
     * Maneja la visualización del historial del cliente
     */
    @FXML
    private void handleVerHistorial() {
        try {
            if (clienteActual != null) {
                // TODO: Implementar ventana de historial del cliente
                mostrarInfo("Funcionalidad de historial del cliente en desarrollo");
            }
        } catch (Exception e) {
            logger.error("Error al mostrar historial del cliente", e);
            mostrarError("Error al mostrar el historial del cliente");
        }
    }
    
    /**
     * Maneja el cierre de la ventana
     */
    @FXML
    private void handleCerrar() {
        cerrarVentana();
    }
    
    /**
     * Valida los campos requeridos
     */
    private boolean validarCampos() {
        if (txtMontoSolicitado.getText().trim().isEmpty()) {
            mostrarError("El monto solicitado es requerido");
            txtMontoSolicitado.requestFocus();
            return false;
        }
        
        if (txtMontoDesembolsado.getText().trim().isEmpty()) {
            mostrarError("El monto desembolsado es requerido");
            txtMontoDesembolsado.requestFocus();
            return false;
        }
        
        if (txtTasaInteres.getText().trim().isEmpty()) {
            mostrarError("La tasa de interés es requerida");
            txtTasaInteres.requestFocus();
            return false;
        }
        
        if (txtPeriodo.getText().trim().isEmpty()) {
            mostrarError("El período es requerido");
            txtPeriodo.requestFocus();
            return false;
        }
        
        try {
            Double.parseDouble(txtMontoSolicitado.getText());
            Double.parseDouble(txtMontoDesembolsado.getText());
            Double.parseDouble(txtTasaInteres.getText());
            Integer.parseInt(txtPeriodo.getText());
        } catch (NumberFormatException e) {
            mostrarError("Los valores numéricos no son válidos");
            return false;
        }
        
        return true;
    }
    
    /**
     * Actualiza los datos del préstamo con los valores de la interfaz
     */
    private void actualizarDatosPrestamo() {
        try {
            prestamoActual.setMontoSolicitado(new java.math.BigDecimal(txtMontoSolicitado.getText()));
            prestamoActual.setMontoDesembolsado(new java.math.BigDecimal(txtMontoDesembolsado.getText()));
            prestamoActual.setTasaInteres(new java.math.BigDecimal(txtTasaInteres.getText()));
            prestamoActual.setPeriodoMeses(Integer.parseInt(txtPeriodo.getText()));
            
            // Actualizar tipo de pago
            if (comboTipoPago.getValue() != null) {
                prestamoActual.setTipoPago(Prestamo.TipoPago.valueOf(comboTipoPago.getValue().toUpperCase()));
            }
            
            prestamoActual.setObservacion(txtObservacion.getText());
        } catch (Exception e) {
            logger.error("Error al actualizar datos del préstamo", e);
            throw new RuntimeException("Error al actualizar los datos del préstamo", e);
        }
    }
    
    /**
     * Cierra la ventana actual
     */
    private void cerrarVentana() {
        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
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
