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
import pe.crediactiva.app.util.DateTimeUtil;

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
    private TextField txtTelefono;
    
    @FXML
    private TextField txtEmail;
    
    @FXML
    private TextField txtDireccion;
    
    @FXML
    private TextField txtOcupacion;
    
    @FXML
    private TextField txtLugarTrabajo;
    
    
    
    
    
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
        // Configurar combo de sexo (según la base de datos: 'f', 'm')
        cmbSexo.getItems().addAll("m", "f");
        
        
        // TODO: Agregar campo DNI al modelo Cliente
        
        
        
        // Configurar validación de teléfono
        txtTelefono.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtTelefono.setText(oldValue);
            }
        });
        
    }
    
    /**
     * Configura la fecha de nacimiento por defecto
     */
    private void configurarFechaNacimiento() {
        dpFechaNacimiento.setValue(DateTimeUtil.today().minusYears(25));
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
        
        // Configurar campos básicos del cliente
        cliente.setNombre(txtNombres.getText().trim());
        cliente.setApellido(txtApellidos.getText().trim());
        cliente.setDni(dni);
        cliente.setTelefono(txtTelefono.getText().trim());
        cliente.setEmail(txtEmail.getText().trim());
        cliente.setDireccion(txtDireccion.getText().trim());
        
        // Configurar nuevos campos
        cliente.setFechaNacimiento(dpFechaNacimiento.getValue());
        cliente.setSexo(cmbSexo.getValue());
        cliente.setOcupacion(txtOcupacion.getText().trim());
        cliente.setLugarTrabajo(txtLugarTrabajo.getText().trim());
        
        // Configurar campos por defecto para nuevo cliente
        cliente.setFechaRegistro(DateTimeUtil.today()); // Fecha actual
        cliente.setSaldoCapital(BigDecimal.ZERO); // Saldo inicial en 0
        cliente.setActivo(true); // Estado activo = 1
        cliente.setEtiquetaCliente(Cliente.EtiquetaCliente.EXCELENTE); // Etiqueta por defecto
        
        // Asignar el ID del asesor que está registrando el cliente
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
        dpFechaNacimiento.setValue(DateTimeUtil.today().minusYears(25));
        cmbSexo.setValue(null);
        txtTelefono.clear();
        txtEmail.clear();
        txtDireccion.clear();
        txtOcupacion.clear();
        txtLugarTrabajo.clear();
        
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
