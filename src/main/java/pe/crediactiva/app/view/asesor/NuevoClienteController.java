package pe.crediactiva.app.view.asesor;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import pe.crediactiva.app.model.Cliente;
import pe.crediactiva.app.service.ClienteService;
import pe.crediactiva.app.config.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Controlador para el formulario de nuevo cliente del asesor
 */
public class NuevoClienteController {
    
    private static final Logger logger = LoggerFactory.getLogger(NuevoClienteController.class);
    
    @FXML
    private TextField txtNombres;
    
    @FXML
    private TextField txtApellidos;
    
    @FXML
    private TextField txtDni;
    
    @FXML
    private DatePicker dpFechaNacimiento;
    
    @FXML
    private ComboBox<String> cmbSexo;
    
    @FXML
    private ComboBox<String> cmbEstadoCivil;
    
    @FXML
    private TextField txtTelefono;
    
    @FXML
    private TextField txtEmail;
    
    @FXML
    private TextField txtDireccion;
    
    @FXML
    private TextField txtOcupacion;
    
    @FXML
    private TextField txtLugarTrabajo;
    
    @FXML
    private TextField txtIngresosMensuales;
    
    @FXML
    private TextField txtTiempoTrabajo;
    
    @FXML
    private TextField txtRef1Nombre;
    
    @FXML
    private TextField txtRef1Telefono;
    
    @FXML
    private TextField txtRef2Nombre;
    
    @FXML
    private TextField txtRef2Telefono;
    
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
    
    public NuevoClienteController() {
        this.clienteService = new ClienteService();
    }
    
    @FXML
    private void initialize() {
        try {
            configurarControles();
            configurarFechaNacimiento();
            
        } catch (Exception e) {
            logger.error("Error al inicializar nuevo cliente", e);
            mostrarError("Error al inicializar el formulario de nuevo cliente");
        }
    }
    
    /**
     * Configura los controles del formulario
     */
    private void configurarControles() {
        // Configurar combo de sexo
        cmbSexo.getItems().addAll("M", "F");
        
        // Configurar combo de estado civil
        cmbEstadoCivil.getItems().addAll("SOLTERO", "CASADO", "DIVORCIADO", "VIUDO");
        
        // TODO: Agregar campo DNI al modelo Cliente
        
        // Configurar validación de ingresos
        txtIngresosMensuales.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                txtIngresosMensuales.setText(oldValue);
            }
        });
        
        // Configurar validación de tiempo de trabajo
        txtTiempoTrabajo.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtTiempoTrabajo.setText(oldValue);
            }
        });
        
        // Configurar validación de teléfono
        txtTelefono.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtTelefono.setText(oldValue);
            }
        });
        
        // Configurar validación de teléfonos de referencia
        txtRef1Telefono.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtRef1Telefono.setText(oldValue);
            }
        });
        
        txtRef2Telefono.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtRef2Telefono.setText(oldValue);
            }
        });
    }
    
    /**
     * Configura la fecha de nacimiento por defecto
     */
    private void configurarFechaNacimiento() {
        dpFechaNacimiento.setValue(LocalDate.now().minusYears(25));
    }
    
    /**
     * Maneja el guardado del cliente
     */
    @FXML
    private void handleGuardarCliente() {
        if (!validarDatosCompletos()) {
            return;
        }
        
        try {
            Cliente cliente = crearCliente();
            clienteService.crearCliente(cliente);
            
            mostrarInfo("Cliente creado exitosamente");
            limpiarFormulario();
            
        } catch (Exception e) {
            logger.error("Error al guardar cliente", e);
            mostrarError("Error al guardar el cliente");
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
     * Valida los datos básicos del formulario
     */
    private boolean validarDatosBasicos() {
        if (txtNombres.getText().trim().isEmpty()) {
            mostrarAdvertencia("Por favor ingrese los nombres del cliente");
            return false;
        }
        
        if (txtApellidos.getText().trim().isEmpty()) {
            mostrarAdvertencia("Por favor ingrese los apellidos del cliente");
            return false;
        }
        
        // CORRECCIÓN: Validar DNI como campo obligatorio
        if (txtDni.getText().trim().isEmpty()) {
            mostrarAdvertencia("Por favor ingrese el DNI del cliente");
            return false;
        }
        
        // Validar que el DNI sea un número válido
        try {
            Long.parseLong(txtDni.getText().trim());
        } catch (NumberFormatException e) {
            mostrarAdvertencia("El DNI debe ser un número válido");
            return false;
        }
        
        if (txtTelefono.getText().trim().isEmpty()) {
            mostrarAdvertencia("Por favor ingrese el teléfono del cliente");
            return false;
        }
        
        return true;
    }
    
    /**
     * Valida todos los datos del formulario
     */
    private boolean validarDatosCompletos() {
        if (!validarDatosBasicos()) {
            return false;
        }
        
        if (dpFechaNacimiento.getValue() == null) {
            mostrarAdvertencia("Por favor seleccione la fecha de nacimiento");
            return false;
        }
        
        if (cmbSexo.getValue() == null) {
            mostrarAdvertencia("Por favor seleccione el sexo del cliente");
            return false;
        }
        
        if (txtEmail.getText().trim().isEmpty()) {
            mostrarAdvertencia("Por favor ingrese el email del cliente");
            return false;
        }
        
        if (!txtEmail.getText().trim().contains("@")) {
            mostrarAdvertencia("Por favor ingrese un email válido");
            return false;
        }
        
        if (txtDireccion.getText().trim().isEmpty()) {
            mostrarAdvertencia("Por favor ingrese la dirección del cliente");
            return false;
        }
        
        if (txtOcupacion.getText().trim().isEmpty()) {
            mostrarAdvertencia("Por favor ingrese la ocupación del cliente");
            return false;
        }
        
        if (txtLugarTrabajo.getText().trim().isEmpty()) {
            mostrarAdvertencia("Por favor ingrese el lugar de trabajo del cliente");
            return false;
        }
        
        if (txtIngresosMensuales.getText().trim().isEmpty()) {
            mostrarAdvertencia("Por favor ingrese los ingresos mensuales del cliente");
            return false;
        }
        
        try {
            BigDecimal ingresos = new BigDecimal(txtIngresosMensuales.getText());
            if (ingresos.compareTo(BigDecimal.ZERO) <= 0) {
                mostrarAdvertencia("Los ingresos deben ser mayores a cero");
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarAdvertencia("Por favor ingrese un monto válido para los ingresos");
            return false;
        }
        
        if (txtTiempoTrabajo.getText().trim().isEmpty()) {
            mostrarAdvertencia("Por favor ingrese el tiempo en el trabajo");
            return false;
        }
        
        try {
            int tiempoTrabajo = Integer.parseInt(txtTiempoTrabajo.getText());
            if (tiempoTrabajo < 0) {
                mostrarAdvertencia("El tiempo en el trabajo debe ser mayor o igual a cero");
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarAdvertencia("Por favor ingrese un tiempo válido para el trabajo");
            return false;
        }
        
        if (txtRef1Nombre.getText().trim().isEmpty()) {
            mostrarAdvertencia("Por favor ingrese al menos una referencia personal");
            return false;
        }
        
        if (txtRef1Telefono.getText().trim().isEmpty()) {
            mostrarAdvertencia("Por favor ingrese el teléfono de la primera referencia");
            return false;
        }
        
        if (!chkDni.isSelected()) {
            mostrarAdvertencia("El DNI del cliente es obligatorio");
            return false;
        }
        
        return true;
    }
    
    /**
     * Crea un objeto Cliente con los datos del formulario
     */
    private Cliente crearCliente() {
        Cliente cliente = new Cliente();
        
        // Obtener el ID del asesor actual desde la sesión
        Long idAsesor = SessionManager.getInstance().getAsesorId();
        if (idAsesor == null) {
            logger.error("No se pudo obtener el ID del asesor de la sesión");
            throw new RuntimeException("Error: No se pudo identificar al asesor");
        }
        
        // CORRECCIÓN CRÍTICA: Generar ID único para el cliente usando el DNI
        String dni = txtDni.getText().trim();
        if (dni.isEmpty()) {
            logger.error("El DNI es obligatorio para crear un cliente");
            throw new RuntimeException("Error: El DNI es obligatorio");
        }
        
        try {
            Long idCliente = Long.parseLong(dni);
            cliente.setIdCliente(idCliente);
            logger.info("Cliente será registrado con id_cliente: " + idCliente);
        } catch (NumberFormatException e) {
            logger.error("El DNI debe ser un número válido: " + dni);
            throw new RuntimeException("Error: El DNI debe ser un número válido");
        }
        
        cliente.setNombre(txtNombres.getText().trim());
        cliente.setApellido(txtApellidos.getText().trim());
        cliente.setDni(dni);
        // TODO: Agregar campos faltantes al modelo Cliente
        // cliente.setFechaNacimiento(dpFechaNacimiento.getValue());
        // cliente.setSexo(cmbSexo.getValue());
        // cliente.setEstadoCivil(cmbEstadoCivil.getValue());
        cliente.setTelefono(txtTelefono.getText().trim());
        cliente.setEmail(txtEmail.getText().trim());
        cliente.setDireccion(txtDireccion.getText().trim());
        // cliente.setOcupacion(txtOcupacion.getText().trim());
        // cliente.setLugarTrabajo(txtLugarTrabajo.getText().trim());
        // cliente.setIngresosMensuales(new BigDecimal(txtIngresosMensuales.getText()));
        // cliente.setTiempoTrabajo(Integer.parseInt(txtTiempoTrabajo.getText()));
        // cliente.setReferencia1Nombre(txtRef1Nombre.getText().trim());
        // cliente.setReferencia1Telefono(txtRef1Telefono.getText().trim());
        // cliente.setReferencia2Nombre(txtRef2Nombre.getText().trim());
        // cliente.setReferencia2Telefono(txtRef2Telefono.getText().trim());
        // cliente.setObservaciones(txtObservaciones.getText().trim());
        cliente.setActivo(true);
        cliente.setFechaRegistro(LocalDate.now());
        
        // CORRECCIÓN CRÍTICA: Asignar el ID del asesor que está registrando el cliente
        cliente.setIdAsesor(idAsesor);
        logger.info("Cliente será registrado con id_asesor: " + idAsesor);
        
        return cliente;
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
        txtNombres.clear();
        txtApellidos.clear();
        txtDni.clear();
        dpFechaNacimiento.setValue(LocalDate.now().minusYears(25));
        cmbSexo.setValue(null);
        cmbEstadoCivil.setValue(null);
        txtTelefono.clear();
        txtEmail.clear();
        txtDireccion.clear();
        txtOcupacion.clear();
        txtLugarTrabajo.clear();
        txtIngresosMensuales.clear();
        txtTiempoTrabajo.clear();
        txtRef1Nombre.clear();
        txtRef1Telefono.clear();
        txtRef2Nombre.clear();
        txtRef2Telefono.clear();
        txtObservaciones.clear();
        
        // Limpiar checkboxes
        chkDni.setSelected(false);
        chkComprobanteIngresos.setSelected(false);
        chkReciboLuz.setSelected(false);
        chkReciboAgua.setSelected(false);
        chkReferencias.setSelected(false);
        chkOtros.setSelected(false);
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
