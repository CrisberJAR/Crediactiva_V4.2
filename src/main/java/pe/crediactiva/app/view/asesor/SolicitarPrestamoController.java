package pe.crediactiva.app.view.asesor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import pe.crediactiva.app.model.Cliente;
import pe.crediactiva.app.model.Prestamo;
import pe.crediactiva.app.service.ClienteService;
import pe.crediactiva.app.service.PrestamoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Controlador para la solicitud de préstamos del asesor
 */
public class SolicitarPrestamoController {
    
    private static final Logger logger = LoggerFactory.getLogger(SolicitarPrestamoController.class);
    
    @FXML
    private ComboBox<Cliente> cmbCliente;
    
    @FXML
    private TextField txtDniCliente;
    
    @FXML
    private TextField txtMonto;
    
    @FXML
    private ComboBox<String> cmbPlazo;
    
    @FXML
    private ComboBox<String> cmbTipoPago;
    
    @FXML
    private DatePicker dpFechaInicio;
    
    @FXML
    private TextField txtProposito;
    
    @FXML
    private Label lblTasaInteres;
    
    @FXML
    private Label lblCuotaMensual;
    
    @FXML
    private Label lblTotalPagar;
    
    @FXML
    private Label lblInteresesTotales;
    
    @FXML
    private CheckBox chkDni;
    
    @FXML
    private CheckBox chkComprobanteIngresos;
    
    @FXML
    private CheckBox chkReciboLuz;
    
    @FXML
    private CheckBox chkReciboAgua;
    
    @FXML
    private CheckBox chkReferencias;
    
    @FXML
    private CheckBox chkOtros;
    
    @FXML
    private TextArea txtObservaciones;
    
    private ClienteService clienteService;
    private PrestamoService prestamoService;
    private Cliente clienteSeleccionado;
    
    // Tasas de interés por plazo
    private static final double TASA_3_MESES = 0.15;
    private static final double TASA_6_MESES = 0.144;
    private static final double TASA_9_MESES = 0.20;
    private static final double TASA_12_MESES = 0.22;
    private static final double TASA_18_MESES = 0.25;
    private static final double TASA_24_MESES = 0.28;
    private static final double TASA_36_MESES = 0.30;
    
    public SolicitarPrestamoController() {
        this.clienteService = new ClienteService();
        this.prestamoService = new PrestamoService();
    }
    
    @FXML
    private void initialize() {
        try {
            logger.info("Inicializando solicitud de préstamo...");
            configurarControles();
            cargarClientes();
            configurarFechaInicio();
            logger.info("Solicitud de préstamo inicializada correctamente");
            
        } catch (Exception e) {
            logger.error("Error al inicializar solicitud de préstamo", e);
            mostrarError("Error al inicializar el formulario de solicitud: " + e.getMessage());
        }
    }
    
    /**
     * Configura los controles del formulario
     */
    private void configurarControles() {
        // Configurar combo de plazo
        cmbPlazo.getItems().addAll("3", "6", "9", "12", "18", "24", "36");
        
        // Configurar combo de tipo de pago
        cmbTipoPago.getItems().addAll("MENSUAL", "QUINCENAL", "SEMANAL");
        cmbTipoPago.setValue("MENSUAL");
        
        // Configurar validación de monto
        txtMonto.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                txtMonto.setText(oldValue);
            }
        });
        
        // Configurar combo de cliente
        cmbCliente.setCellFactory(listView -> new ListCell<Cliente>() {
            @Override
            protected void updateItem(Cliente cliente, boolean empty) {
                super.updateItem(cliente, empty);
                if (empty || cliente == null) {
                    setText(null);
                } else {
                    setText(cliente.getNombre() + " " + cliente.getApellido() + " - " + cliente.getTelefono());
                }
            }
        });
        
        cmbCliente.setButtonCell(new ListCell<Cliente>() {
            @Override
            protected void updateItem(Cliente cliente, boolean empty) {
                super.updateItem(cliente, empty);
                if (empty || cliente == null) {
                    setText(null);
                } else {
                    setText(cliente.getNombre() + " " + cliente.getApellido() + " - " + cliente.getTelefono());
                }
            }
        });
    }
    
    /**
     * Carga la lista de clientes
     */
    private void cargarClientes() {
        try {
            List<Cliente> clientes = clienteService.obtenerTodosLosClientes();
            ObservableList<Cliente> clientesObservable = FXCollections.observableArrayList(clientes);
            cmbCliente.setItems(clientesObservable);
            
        } catch (Exception e) {
            logger.error("Error al cargar clientes", e);
            mostrarError("Error al cargar la lista de clientes");
        }
    }
    
    /**
     * Configura la fecha de inicio por defecto
     */
    private void configurarFechaInicio() {
        dpFechaInicio.setValue(LocalDate.now().plusDays(1));
    }
    
    /**
     * Maneja la búsqueda de cliente
     */
    @FXML
    private void handleBuscarCliente() {
        try {
            // TODO: Implementar búsqueda avanzada de clientes
            mostrarInfo("Funcionalidad de búsqueda de clientes en desarrollo");
            
        } catch (Exception e) {
            logger.error("Error al buscar cliente", e);
            mostrarError("Error al buscar cliente");
        }
    }
    
    /**
     * Maneja la búsqueda de cliente por DNI
     */
    @FXML
    private void handleBuscarPorDni() {
        String dni = txtDniCliente.getText().trim();
        
        if (dni.isEmpty()) {
            mostrarAdvertencia("Por favor ingrese el DNI del cliente");
            return;
        }
        
        try {
            // TODO: Implementar búsqueda por DNI en ClienteService
            java.util.Optional<Cliente> clienteOpt = clienteService.obtenerClientePorId(Long.parseLong(dni));
            if (clienteOpt.isPresent()) {
                Cliente cliente = clienteOpt.get();
                cmbCliente.setValue(cliente);
                clienteSeleccionado = cliente;
                mostrarInfo("Cliente encontrado: " + cliente.getNombre() + " " + cliente.getApellido());
            } else {
                mostrarAdvertencia("No se encontró un cliente con el ID: " + dni);
            }
            
        } catch (Exception e) {
            logger.error("Error al buscar cliente por DNI", e);
            mostrarError("Error al buscar cliente por DNI");
        }
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
     * Maneja el cálculo del préstamo
     */
    @FXML
    private void handleCalcular() {
        if (!validarDatosBasicos()) {
            return;
        }
        
        try {
            BigDecimal monto = new BigDecimal(txtMonto.getText());
            int plazo = Integer.parseInt(cmbPlazo.getValue());
            // String tipoPago = cmbTipoPago.getValue(); // No se usa en el cálculo
            
            // Calcular tasa de interés según el plazo
            double tasaInteres = calcularTasaInteres(plazo);
            lblTasaInteres.setText(String.format("%.2f%%", tasaInteres * 100));
            
            // Calcular cuota mensual
            BigDecimal cuotaMensual = calcularCuotaMensual(monto, tasaInteres, plazo);
            lblCuotaMensual.setText("S/ " + String.format("%.2f", cuotaMensual));
            
            // Calcular total a pagar
            BigDecimal totalPagar = cuotaMensual.multiply(BigDecimal.valueOf(plazo));
            lblTotalPagar.setText("S/ " + String.format("%.2f", totalPagar));
            
            // Calcular intereses totales
            BigDecimal interesesTotales = totalPagar.subtract(monto);
            lblInteresesTotales.setText("S/ " + String.format("%.2f", interesesTotales));
            
            logger.info("Cálculo de préstamo realizado - Monto: " + monto + ", Plazo: " + plazo + " meses");
            
        } catch (Exception e) {
            logger.error("Error al calcular préstamo", e);
            mostrarError("Error al calcular el préstamo");
        }
    }
    
    /**
     * Valida los datos básicos del formulario
     */
    private boolean validarDatosBasicos() {
        if (cmbCliente.getValue() == null) {
            mostrarAdvertencia("Por favor seleccione un cliente");
            return false;
        }
        
        if (txtMonto.getText().trim().isEmpty()) {
            mostrarAdvertencia("Por favor ingrese el monto del préstamo");
            return false;
        }
        
        if (cmbPlazo.getValue() == null) {
            mostrarAdvertencia("Por favor seleccione el plazo del préstamo");
            return false;
        }
        
        try {
            BigDecimal monto = new BigDecimal(txtMonto.getText());
            if (monto.compareTo(BigDecimal.ZERO) <= 0) {
                mostrarAdvertencia("El monto debe ser mayor a cero");
                return false;
            }
            
            if (monto.compareTo(new BigDecimal("50000")) > 0) {
                mostrarAdvertencia("El monto máximo permitido es S/ 50,000");
                return false;
            }
            
        } catch (NumberFormatException e) {
            mostrarAdvertencia("Por favor ingrese un monto válido");
            return false;
        }
        
        return true;
    }
    
    /**
     * Calcula la tasa de interés según el plazo
     */
    private double calcularTasaInteres(int plazo) {
        switch (plazo) {
            case 3: return TASA_3_MESES;
            case 6: return TASA_6_MESES;
            case 9: return TASA_9_MESES;
            case 12: return TASA_12_MESES;
            case 18: return TASA_18_MESES;
            case 24: return TASA_24_MESES;
            case 36: return TASA_36_MESES;
            default: return TASA_12_MESES;
        }
    }
    
    /**
     * Calcula la cuota mensual usando la fórmula de cuota fija
     */
    private BigDecimal calcularCuotaMensual(BigDecimal monto, double tasaInteres, int plazo) {
        double tasaMensual = tasaInteres / 12;
        double factor = Math.pow(1 + tasaMensual, plazo);
        double cuota = monto.doubleValue() * (tasaMensual * factor) / (factor - 1);
        
        return BigDecimal.valueOf(cuota).setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * Maneja la simulación del préstamo
     */
    @FXML
    private void handleSimular() {
        if (!validarDatosBasicos()) {
            return;
        }
        
        try {
            // TODO: Implementar vista de simulación detallada
            mostrarInfo("Funcionalidad de simulación en desarrollo");
            
        } catch (Exception e) {
            logger.error("Error al simular préstamo", e);
            mostrarError("Error al simular el préstamo");
        }
    }
    
    /**
     * Maneja el guardado del borrador
     */
    @FXML
    private void handleGuardarBorrador() {
        if (!validarDatosBasicos()) {
            return;
        }
        
        try {
            // TODO: Implementar guardado de borrador
            mostrarInfo("Funcionalidad de guardado de borrador en desarrollo");
            
        } catch (Exception e) {
            logger.error("Error al guardar borrador", e);
            mostrarError("Error al guardar el borrador");
        }
    }
    
    /**
     * Maneja el envío de la solicitud
     */
    @FXML
    private void handleEnviarSolicitud() {
        if (!validarDatosCompletos()) {
            return;
        }
        
        try {
            // Crear el préstamo
            Prestamo prestamo = crearPrestamo();
            
            // TODO: Implementar creación de préstamo en PrestamoService
            // prestamoService.crearPrestamo(prestamo);
            
            mostrarInfo("Solicitud de préstamo enviada exitosamente");
            limpiarFormulario();
            
        } catch (Exception e) {
            logger.error("Error al enviar solicitud", e);
            mostrarError("Error al enviar la solicitud de préstamo");
        }
    }
    
    /**
     * Valida todos los datos del formulario
     */
    private boolean validarDatosCompletos() {
        if (!validarDatosBasicos()) {
            return false;
        }
        
        if (dpFechaInicio.getValue() == null) {
            mostrarAdvertencia("Por favor seleccione la fecha de inicio");
            return false;
        }
        
        if (txtProposito.getText().trim().isEmpty()) {
            mostrarAdvertencia("Por favor ingrese el propósito del préstamo");
            return false;
        }
        
        if (!chkDni.isSelected()) {
            mostrarAdvertencia("El DNI del cliente es obligatorio");
            return false;
        }
        
        return true;
    }
    
    /**
     * Crea un objeto Préstamo con los datos del formulario
     */
    private Prestamo crearPrestamo() {
        Prestamo prestamo = new Prestamo();
        
        prestamo.setCliente(clienteSeleccionado);
        prestamo.setMontoSolicitado(new BigDecimal(txtMonto.getText()));
        prestamo.setPeriodoMeses(Integer.parseInt(cmbPlazo.getValue()));
        prestamo.setTipoPago(Prestamo.TipoPago.valueOf(cmbTipoPago.getValue()));
        prestamo.setFechaInicio(dpFechaInicio.getValue());
        prestamo.setObservacion(txtProposito.getText().trim() + " - " + txtObservaciones.getText().trim());
        prestamo.setEstado(Prestamo.EstadoPrestamo.PENDIENTE);
        prestamo.setEtiqueta(Prestamo.EtiquetaPrestamo.PUNTUAL);
        
        return prestamo;
    }
    
    /**
     * Maneja la limpieza del formulario
     */
    @FXML
    private void handleLimpiar() {
        limpiarFormulario();
        mostrarInfo("Formulario limpiado");
    }
    
    /**
     * Limpia todos los campos del formulario
     */
    private void limpiarFormulario() {
        cmbCliente.setValue(null);
        txtDniCliente.clear();
        txtMonto.clear();
        cmbPlazo.setValue(null);
        cmbTipoPago.setValue("MENSUAL");
        dpFechaInicio.setValue(LocalDate.now().plusDays(1));
        txtProposito.clear();
        txtObservaciones.clear();
        
        // Limpiar cálculos
        lblTasaInteres.setText("0.00%");
        lblCuotaMensual.setText("S/ 0.00");
        lblTotalPagar.setText("S/ 0.00");
        lblInteresesTotales.setText("S/ 0.00");
        
        // Limpiar checkboxes
        chkDni.setSelected(false);
        chkComprobanteIngresos.setSelected(false);
        chkReciboLuz.setSelected(false);
        chkReciboAgua.setSelected(false);
        chkReferencias.setSelected(false);
        chkOtros.setSelected(false);
        
        clienteSeleccionado = null;
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
