package pe.crediactiva.app.view.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.crediactiva.app.model.Cliente;
import pe.crediactiva.app.model.Cronograma;
import pe.crediactiva.app.model.Prestamo;
import pe.crediactiva.app.service.ClienteService;
import pe.crediactiva.app.service.PrestamoService;
import pe.crediactiva.app.service.AuditoriaService;
import pe.crediactiva.app.util.FechaUtil;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controlador para la creación de nuevas solicitudes de préstamo
 */
public class NuevaSolicitudController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(NuevaSolicitudController.class);
    
    @FXML
    private TextField txtDniCliente;
    
    @FXML
    private Button btnBuscarCliente;
    
    @FXML
    private Label lblNombreCliente;
    
    @FXML
    private Label lblApellidoCliente;
    
    @FXML
    private Label lblTelefonoCliente;
    
    @FXML
    private Label lblEmailCliente;
    
    @FXML
    private Label lblDireccionCliente;
    
    @FXML
    private Label lblCapitalCliente;
    
    @FXML
    private Label lblEtiquetaCliente;
    
    @FXML
    private ComboBox<String> comboAsesor;
    
    @FXML
    private TextField txtMontoSolicitado;
    
    @FXML
    private TextField txtTasaInteres;
    
    @FXML
    private TextField txtPeriodo;
    
    @FXML
    private ComboBox<String> comboTipoPago;
    
    @FXML
    private Label lblMontoDesembolsado;
    
    @FXML
    private Label lblCapitalRetenido;
    
    @FXML
    private Label lblMontoTotal;
    
    @FXML
    private Label lblValorCuota;
    
    @FXML
    private DatePicker dateFechaInicio;
    
    @FXML
    private DatePicker dateFechaFin;
    
    @FXML
    private TextArea txtObservaciones;
    
    @FXML
    private TableView<Cronograma> tablaCronograma;
    
    @FXML
    private TableColumn<Cronograma, Integer> colNumeroCuota;
    
    @FXML
    private TableColumn<Cronograma, String> colFechaProgramada;
    
    @FXML
    private TableColumn<Cronograma, Double> colMontoCuota;
    
    @FXML
    private TableColumn<Cronograma, String> colEstadoCuota;
    
    @FXML
    private Button btnCalcular;
    
    @FXML
    private Button btnGuardar;
    
    @FXML
    private Button btnLimpiar;
    
    @FXML
    private Button btnCancelar;
    
    private ClienteService clienteService;
    private PrestamoService prestamoService;
    private AuditoriaService auditoriaService;
    private ObservableList<Cronograma> cronogramaPreview;
    private Cliente clienteSeleccionado;
    private Long asesorSeleccionado;
    
    public NuevaSolicitudController() {
        this.clienteService = new ClienteService();
        this.prestamoService = new PrestamoService();
        this.auditoriaService = new AuditoriaService();
        this.cronogramaPreview = FXCollections.observableArrayList();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Configurar tabla de cronograma
            configurarTablaCronograma();
            
            // Configurar combos
            configurarCombos();
            
            // Configurar eventos
            configurarEventos();
            
            // Configurar fechas por defecto
            configurarFechasPorDefecto();
            
            logger.info("Formulario de nueva solicitud inicializado correctamente");
            
        } catch (Exception e) {
            logger.error("Error al inicializar el formulario de nueva solicitud", e);
            mostrarError("Error al inicializar el formulario");
        }
    }
    
    /**
     * Configura la tabla de cronograma
     */
    private void configurarTablaCronograma() {
        colNumeroCuota.setCellValueFactory(new PropertyValueFactory<>("numeroCuota"));
        colFechaProgramada.setCellValueFactory(cellData -> {
            LocalDate fecha = cellData.getValue().getFechaProgramada();
            return new javafx.beans.property.SimpleStringProperty(fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        });
        colMontoCuota.setCellValueFactory(new PropertyValueFactory<>("montoCuota"));
        colEstadoCuota.setCellValueFactory(new PropertyValueFactory<>("estadoCuota"));
        
        tablaCronograma.setItems(cronogramaPreview);
    }
    
    /**
     * Configura los combos
     */
    private void configurarCombos() {
        // Tipos de pago
        comboTipoPago.setItems(FXCollections.observableArrayList(
            "diario", "semanal", "mensual"
        ));
        comboTipoPago.setValue("diario");
        
        // TODO: Cargar asesores desde la base de datos
        comboAsesor.setItems(FXCollections.observableArrayList("Asesor 1", "Asesor 2", "Asesor 3"));
        comboAsesor.setValue("Asesor 1");
    }
    
    /**
     * Configura eventos de la interfaz
     */
    private void configurarEventos() {
        // Validar campos numéricos
        txtMontoSolicitado.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                txtMontoSolicitado.setText(oldVal);
            } else {
                calcularMontos();
            }
        });
        
        txtTasaInteres.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                txtTasaInteres.setText(oldVal);
            } else {
                calcularMontos();
            }
        });
        
        txtPeriodo.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                txtPeriodo.setText(oldVal);
            } else {
                calcularMontos();
            }
        });
        
        // Cambios en tipo de pago
        comboTipoPago.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                calcularMontos();
            }
        });
        
        // Cambios en fechas
        dateFechaInicio.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                calcularFechas();
            }
        });
    }
    
    /**
     * Configura las fechas por defecto
     */
    private void configurarFechasPorDefecto() {
        dateFechaInicio.setValue(LocalDate.now().plusDays(1));
        dateFechaFin.setValue(LocalDate.now().plusDays(1));
    }
    
    /**
     * Maneja la búsqueda de cliente
     */
    @FXML
    private void handleBuscarCliente(ActionEvent event) {
        try {
            String dniText = txtDniCliente.getText().trim();
            if (dniText.isEmpty()) {
                mostrarError("Ingrese el DNI del cliente");
                return;
            }
            
            Long dni = Long.parseLong(dniText);
            Optional<Cliente> clienteOpt = clienteService.buscarPorDni(dni);
        if (clienteOpt.isPresent()) {
            Cliente cliente = clienteOpt.get();
            
            mostrarDatosCliente(cliente);
            clienteSeleccionado = cliente;
        } else {
            mostrarError("Cliente no encontrado con DNI: " + dni);
            limpiarDatosCliente();
        }
            
        } catch (NumberFormatException e) {
            mostrarError("El DNI debe ser un número válido");
        } catch (Exception e) {
            logger.error("Error al buscar cliente", e);
            mostrarError("Error al buscar el cliente: " + e.getMessage());
        }
    }
    
    /**
     * Muestra los datos del cliente encontrado
     */
    private void mostrarDatosCliente(Cliente cliente) {
        lblNombreCliente.setText(cliente.getNombre());
        lblApellidoCliente.setText(cliente.getApellido());
        lblTelefonoCliente.setText(cliente.getTelefono());
        lblEmailCliente.setText(cliente.getEmail());
        lblDireccionCliente.setText(cliente.getDireccion());
        lblCapitalCliente.setText(String.format("S/ %.2f", cliente.getSaldoCapital()));
        lblEtiquetaCliente.setText(cliente.getEtiquetaCliente().name());
        
        // Habilitar campos de préstamo
        habilitarCamposPrestamo(true);
    }
    
    /**
     * Limpia los datos del cliente
     */
    private void limpiarDatosCliente() {
        lblNombreCliente.setText("");
        lblApellidoCliente.setText("");
        lblTelefonoCliente.setText("");
        lblEmailCliente.setText("");
        lblDireccionCliente.setText("");
        lblCapitalCliente.setText("");
        lblEtiquetaCliente.setText("");
        
        clienteSeleccionado = null;
        habilitarCamposPrestamo(false);
    }
    
    /**
     * Habilita o deshabilita los campos de préstamo
     */
    private void habilitarCamposPrestamo(boolean habilitar) {
        txtMontoSolicitado.setDisable(!habilitar);
        txtTasaInteres.setDisable(!habilitar);
        txtPeriodo.setDisable(!habilitar);
        comboTipoPago.setDisable(!habilitar);
        dateFechaInicio.setDisable(!habilitar);
        txtObservaciones.setDisable(!habilitar);
        btnCalcular.setDisable(!habilitar);
        btnGuardar.setDisable(!habilitar);
    }
    
    /**
     * Calcula los montos del préstamo
     */
    private void calcularMontos() {
        try {
            if (txtMontoSolicitado.getText().trim().isEmpty() || 
                txtTasaInteres.getText().trim().isEmpty() || 
                txtPeriodo.getText().trim().isEmpty()) {
                return;
            }
            
            double montoSolicitado = Double.parseDouble(txtMontoSolicitado.getText());
            double tasaInteres = Double.parseDouble(txtTasaInteres.getText());
            int periodo = Integer.parseInt(txtPeriodo.getText());
            
            // Calcular montos
            double capitalRetenido = montoSolicitado * 0.1; // 10%
            double montoDesembolsado = montoSolicitado - capitalRetenido;
            double montoTotal = montoSolicitado * (1 + tasaInteres / 100);
            
            // Calcular valor de cuota según tipo de pago
            double valorCuota = calcularValorCuota(montoTotal, periodo, comboTipoPago.getValue());
            
            // Mostrar resultados
            lblCapitalRetenido.setText(String.format("S/ %.2f", capitalRetenido));
            lblMontoDesembolsado.setText(String.format("S/ %.2f", montoDesembolsado));
            lblMontoTotal.setText(String.format("S/ %.2f", montoTotal));
            lblValorCuota.setText(String.format("S/ %.2f", valorCuota));
            
        } catch (NumberFormatException e) {
            // Ignorar errores de formato durante la escritura
        }
    }
    
    /**
     * Calcula el valor de cuota según el tipo de pago
     */
    private double calcularValorCuota(double montoTotal, int periodo, String tipoPago) {
        int numeroCuotas = 0;
        
        switch (tipoPago) {
            case "diario":
                numeroCuotas = periodo * 26; // 26 días hábiles por mes
                break;
            case "semanal":
                numeroCuotas = periodo * 4; // 4 semanas por mes
                break;
            case "mensual":
                numeroCuotas = periodo; // 1 cuota por mes
                break;
        }
        
        return numeroCuotas > 0 ? montoTotal / numeroCuotas : 0;
    }
    
    /**
     * Calcula las fechas de inicio y fin
     */
    private void calcularFechas() {
        try {
            if (dateFechaInicio.getValue() != null && txtPeriodo.getText().trim().isEmpty() == false) {
                int periodo = Integer.parseInt(txtPeriodo.getText());
                LocalDate fechaInicio = dateFechaInicio.getValue();
                LocalDate fechaFin = fechaInicio.plusMonths(periodo);
                dateFechaFin.setValue(fechaFin);
            }
        } catch (NumberFormatException e) {
            // Ignorar errores de formato
        }
    }
    
    /**
     * Maneja el cálculo del cronograma
     */
    @FXML
    private void handleCalcular(ActionEvent event) {
        try {
            if (!validarCamposObligatorios()) {
                return;
            }
            
            // Generar preview del cronograma
            generarPreviewCronograma();
            
            mostrarInfo("Cronograma calculado correctamente");
            
        } catch (Exception e) {
            logger.error("Error al calcular cronograma", e);
            mostrarError("Error al calcular el cronograma: " + e.getMessage());
        }
    }
    
    /**
     * Valida los campos obligatorios
     */
    private boolean validarCamposObligatorios() {
        if (clienteSeleccionado == null) {
            mostrarError("Debe buscar y seleccionar un cliente");
            return false;
        }
        
        if (txtMontoSolicitado.getText().trim().isEmpty()) {
            mostrarError("Debe ingresar el monto solicitado");
            return false;
        }
        
        if (txtTasaInteres.getText().trim().isEmpty()) {
            mostrarError("Debe ingresar la tasa de interés");
            return false;
        }
        
        if (txtPeriodo.getText().trim().isEmpty()) {
            mostrarError("Debe ingresar el período");
            return false;
        }
        
        if (dateFechaInicio.getValue() == null) {
            mostrarError("Debe seleccionar la fecha de inicio");
            return false;
        }
        
        double monto = Double.parseDouble(txtMontoSolicitado.getText());
        double tasa = Double.parseDouble(txtTasaInteres.getText());
        int periodo = Integer.parseInt(txtPeriodo.getText());
        
        if (monto <= 0) {
            mostrarError("El monto debe ser mayor a 0");
            return false;
        }
        
        if (tasa < 0 || tasa > 30) {
            mostrarError("La tasa de interés debe estar entre 0% y 30%");
            return false;
        }
        
        if (periodo < 1 || periodo > 12) {
            mostrarError("El período debe estar entre 1 y 12 meses");
            return false;
        }
        
        return true;
    }
    
    /**
     * Genera el preview del cronograma
     */
    private void generarPreviewCronograma() {
        try {
            double montoTotal = Double.parseDouble(lblMontoTotal.getText().replace("S/ ", "").replace(",", ""));
            int periodo = Integer.parseInt(txtPeriodo.getText());
            String tipoPago = comboTipoPago.getValue();
            LocalDate fechaInicio = dateFechaInicio.getValue();
            
            // Generar cronograma usando el servicio
            List<Cronograma> cronograma = prestamoService.generarCronogramaPreview(
                montoTotal, periodo, tipoPago, fechaInicio
            );
            
            cronogramaPreview.clear();
            cronogramaPreview.addAll(cronograma);
            
        } catch (Exception e) {
            logger.error("Error al generar preview del cronograma", e);
            throw e;
        }
    }
    
    /**
     * Maneja el guardado de la solicitud
     */
    @FXML
    private void handleGuardar(ActionEvent event) {
        try {
            if (!validarCamposObligatorios()) {
                return;
            }
            
            if (cronogramaPreview.isEmpty()) {
                mostrarError("Debe calcular el cronograma antes de guardar");
                return;
            }
            
            // Crear la solicitud
            Prestamo solicitud = crearSolicitud();
            
            // Guardar en la base de datos
            prestamoService.crearSolicitud(solicitud);
            
            // Registrar auditoría
            auditoriaService.registrarAuditoria(
                solicitud.getIdPrestamo().toString(),
                "prestamos",
                "insert",
                null,
                "nueva solicitud creada"
            );
            
            mostrarInfo("Solicitud creada correctamente");
            limpiarFormulario();
            
        } catch (Exception e) {
            logger.error("Error al guardar solicitud", e);
            mostrarError("Error al guardar la solicitud: " + e.getMessage());
        }
    }
    
    /**
     * Crea el objeto solicitud con los datos del formulario
     */
    private Prestamo crearSolicitud() {
        Prestamo solicitud = new Prestamo();
        
        // Datos del cliente y asesor
        solicitud.setIdCliente(clienteSeleccionado.getIdCliente());
        solicitud.setIdAsesor(asesorSeleccionado != null ? asesorSeleccionado : 1L); // TODO: Obtener del combo
        
        // Datos del préstamo
        double montoSolicitado = Double.parseDouble(txtMontoSolicitado.getText());
        double montoDesembolsado = Double.parseDouble(lblMontoDesembolsado.getText().replace("S/ ", "").replace(",", ""));
        double tasaInteres = Double.parseDouble(txtTasaInteres.getText());
        int periodo = Integer.parseInt(txtPeriodo.getText());
        
        solicitud.setMontoSolicitado(new java.math.BigDecimal(montoSolicitado));
        solicitud.setMontoDesembolsado(new java.math.BigDecimal(montoDesembolsado));
        solicitud.setTasaInteres(new java.math.BigDecimal(tasaInteres));
        solicitud.setPeriodoMeses(periodo);
        solicitud.setTipoPago(Prestamo.TipoPago.valueOf(comboTipoPago.getValue().toUpperCase()));
        solicitud.setFechaInicio(dateFechaInicio.getValue());
        solicitud.setFechaFin(dateFechaFin.getValue());
        solicitud.setObservacion(txtObservaciones.getText());
        solicitud.setEstado(Prestamo.EstadoPrestamo.PENDIENTE);
        solicitud.setEtiqueta(Prestamo.EtiquetaPrestamo.PUNTUAL);
        
        return solicitud;
    }
    
    /**
     * Maneja la limpieza del formulario
     */
    @FXML
    private void handleLimpiar(ActionEvent event) {
        limpiarFormulario();
    }
    
    /**
     * Limpia todo el formulario
     */
    private void limpiarFormulario() {
        txtDniCliente.clear();
        limpiarDatosCliente();
        
        txtMontoSolicitado.clear();
        txtTasaInteres.clear();
        txtPeriodo.clear();
        comboTipoPago.setValue("diario");
        txtObservaciones.clear();
        
        lblCapitalRetenido.setText("");
        lblMontoDesembolsado.setText("");
        lblMontoTotal.setText("");
        lblValorCuota.setText("");
        
        dateFechaInicio.setValue(LocalDate.now().plusDays(1));
        dateFechaFin.setValue(LocalDate.now().plusDays(1));
        
        cronogramaPreview.clear();
    }
    
    /**
     * Maneja la cancelación
     */
    @FXML
    private void handleCancelar(ActionEvent event) {
        limpiarFormulario();
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
