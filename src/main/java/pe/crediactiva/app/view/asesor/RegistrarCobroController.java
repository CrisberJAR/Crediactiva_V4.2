package pe.crediactiva.app.view.asesor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import pe.crediactiva.app.model.Cliente;
import pe.crediactiva.app.model.Cronograma;
import pe.crediactiva.app.model.Prestamo;
import pe.crediactiva.app.service.ClienteService;
import pe.crediactiva.app.service.PagoService;
import pe.crediactiva.app.service.PrestamoService;
import pe.crediactiva.app.service.RecaudacionService;
import pe.crediactiva.app.config.SessionManager;
import pe.crediactiva.app.dao.CronogramaDAO;
import pe.crediactiva.app.dao.impl.CronogramaDAOImpl;
import pe.crediactiva.app.util.FechaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Controlador para el registro de cobros del asesor
 */
public class RegistrarCobroController {
    
    private static final Logger logger = LoggerFactory.getLogger(RegistrarCobroController.class);
    
    @FXML
    private ComboBox<Cliente> cmbCliente;
    
    @FXML
    private TextField txtDniCliente;
    
    @FXML
    private Label lblNombreCliente;
    
    @FXML
    private Label lblDniCliente;
    
    @FXML
    private Label lblTelefonoCliente;
    
    @FXML
    private Label lblEmailCliente;
    
    @FXML
    private TableView<Prestamo> tblPrestamos;
    
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
    private TableView<Cronograma> tblCuotas;
    
    @FXML
    private TableColumn<Cronograma, Integer> colIdCuota;
    
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
    private ComboBox<Cronograma> cmbCuotaPagar;
    
    @FXML
    private TextField txtMontoPagar;
    
    @FXML
    private DatePicker dpFechaPago;
    
    @FXML
    private ComboBox<String> cmbMetodoPago;
    
    @FXML
    private TextField txtReferencia;
    
    @FXML
    private TextField txtObservaciones;
    
    @FXML
    private Label lblMontoCuota;
    
    @FXML
    private Label lblMontoPagar;
    
    @FXML
    private Label lblCambio;
    
    @FXML
    private Label lblNuevoSaldo;
    
    private ClienteService clienteService;
    private PrestamoService prestamoService;
    private PagoService pagoService;
    private RecaudacionService recaudacionService;
    private CronogramaDAO cronogramaDAO;
    private Cliente clienteSeleccionado;
    private Prestamo prestamoSeleccionado;
    private Long idAsesorActual;
    private ObservableList<Prestamo> prestamos;
    private ObservableList<Cronograma> cuotas;
    
    public RegistrarCobroController() {
        this.clienteService = new ClienteService();
        this.prestamoService = new PrestamoService();
        this.pagoService = new PagoService();
        this.recaudacionService = new RecaudacionService();
        this.cronogramaDAO = new CronogramaDAOImpl();
        this.prestamos = FXCollections.observableArrayList();
        this.cuotas = FXCollections.observableArrayList();
        
        // Obtener ID del asesor actual desde la sesión
        this.idAsesorActual = SessionManager.getInstance().getAsesorId();
    }
    
    @FXML
    private void initialize() {
        try {
            configurarControles();
            cargarClientes();
            configurarTablas();
            configurarFechaPago();
            
        } catch (Exception e) {
            logger.error("Error al inicializar registro de cobro", e);
            mostrarError("Error al inicializar el registro de cobro");
        }
    }
    
    /**
     * Configura los controles del formulario
     */
    private void configurarControles() {
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
        
        // Configurar combo de cuota a pagar
        cmbCuotaPagar.setCellFactory(listView -> new ListCell<Cronograma>() {
            @Override
            protected void updateItem(Cronograma cuota, boolean empty) {
                super.updateItem(cuota, empty);
                if (empty || cuota == null) {
                    setText(null);
                } else {
                    setText("Cuota #" + cuota.getNumeroCuota() + " - S/ " + String.format("%.2f", cuota.getMontoCuota()));
                }
            }
        });
        
        cmbCuotaPagar.setButtonCell(new ListCell<Cronograma>() {
            @Override
            protected void updateItem(Cronograma cuota, boolean empty) {
                super.updateItem(cuota, empty);
                if (empty || cuota == null) {
                    setText(null);
                } else {
                    setText("Cuota #" + cuota.getNumeroCuota() + " - S/ " + String.format("%.2f", cuota.getMontoCuota()));
                }
            }
        });
        
        // Configurar validación de monto
        txtMontoPagar.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*\\.?\\d*")) {
                txtMontoPagar.setText(oldValue);
            }
        });
        
        // Configurar combo de método de pago
        cmbMetodoPago.getItems().addAll("EFECTIVO", "TRANSFERENCIA", "YAPE", "PLIN", "TARJETA");
        cmbMetodoPago.setValue("EFECTIVO");
    }
    
    /**
     * Configura las tablas
     */
    private void configurarTablas() {
        // Configurar tabla de préstamos
        colIdPrestamo.setCellValueFactory(new PropertyValueFactory<>("idPrestamo"));
        colMontoPrestamo.setCellValueFactory(cellData -> {
            Prestamo prestamo = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty("S/ " + String.format("%.2f", prestamo.getMontoSolicitado()));
        });
        colSaldoPrestamo.setCellValueFactory(cellData -> {
            Prestamo prestamo = cellData.getValue();
            // TODO: Calcular saldo real
            return new javafx.beans.property.SimpleStringProperty("S/ " + String.format("%.2f", prestamo.getMontoSolicitado()));
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
        
        tblPrestamos.setItems(prestamos);
        
        // Configurar tabla de cuotas
        colIdCuota.setCellValueFactory(new PropertyValueFactory<>("idCronograma"));
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
        
        tblCuotas.setItems(cuotas);
    }
    
    /**
     * Carga la lista de clientes del asesor actual
     */
    private void cargarClientes() {
        try {
            // Solo cargar clientes del asesor actual
            List<Cliente> clientes = clienteService.obtenerClientesPorAsesor(idAsesorActual);
            ObservableList<Cliente> clientesObservable = FXCollections.observableArrayList(clientes);
            cmbCliente.setItems(clientesObservable);
            
            logger.info("Cargados " + clientes.size() + " clientes para el asesor: " + idAsesorActual);
            
        } catch (Exception e) {
            logger.error("Error al cargar clientes", e);
            mostrarError("Error al cargar la lista de clientes");
        }
    }
    
    /**
     * Configura la fecha de pago por defecto
     */
    private void configurarFechaPago() {
        dpFechaPago.setValue(LocalDate.now());
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
                mostrarInfoCliente(cliente);
                cargarPrestamosCliente(cliente);
            } else {
                mostrarAdvertencia("No se encontró un cliente con el ID: " + dni);
            }
            
        } catch (Exception e) {
            logger.error("Error al buscar cliente por DNI", e);
            mostrarError("Error al buscar cliente por DNI");
        }
    }
    
    /**
     * Muestra la información del cliente seleccionado
     */
    private void mostrarInfoCliente(Cliente cliente) {
        lblNombreCliente.setText(cliente.getNombre() + " " + cliente.getApellido());
        lblDniCliente.setText(cliente.getTelefono()); // Usar teléfono como identificador
        lblTelefonoCliente.setText(cliente.getTelefono());
        lblEmailCliente.setText(cliente.getEmail());
    }
    
    /**
     * Carga los préstamos del cliente seleccionado
     */
    private void cargarPrestamosCliente(Cliente cliente) {
        try {
            List<Prestamo> prestamosCliente = prestamoService.obtenerPrestamosPorCliente(cliente.getIdCliente());
            prestamos.clear();
            prestamos.addAll(prestamosCliente);
            
            // Seleccionar el primer préstamo si existe
            if (!prestamosCliente.isEmpty()) {
                prestamoSeleccionado = prestamosCliente.get(0);
                cargarCuotasPrestamo(prestamoSeleccionado);
            }
            
        } catch (Exception e) {
            logger.error("Error al cargar préstamos del cliente", e);
            mostrarError("Error al cargar los préstamos del cliente");
        }
    }
    
    /**
     * Carga las cuotas del préstamo seleccionado
     */
    private void cargarCuotasPrestamo(Prestamo prestamo) {
        try {
            // Obtener cronograma del préstamo
            List<Cronograma> cuotasPrestamo = cronogramaDAO.findByPrestamo(prestamo.getIdPrestamo());
            cuotas.clear();
            cuotas.addAll(cuotasPrestamo);
            
            // Actualizar combo de cuotas a pagar (solo pendientes y retrasadas)
            ObservableList<Cronograma> cuotasPendientes = FXCollections.observableArrayList();
            for (Cronograma cuota : cuotasPrestamo) {
                if (cuota.getEstadoCuota() == Cronograma.EstadoCuota.PENDIENTE || 
                    cuota.getEstadoCuota() == Cronograma.EstadoCuota.RETRASADA) {
                    cuotasPendientes.add(cuota);
                }
            }
            cmbCuotaPagar.setItems(cuotasPendientes);
            
            logger.info("Cargadas " + cuotasPrestamo.size() + " cuotas para el préstamo: " + prestamo.getIdPrestamo());
            
        } catch (Exception e) {
            logger.error("Error al cargar cuotas del préstamo", e);
            mostrarError("Error al cargar las cuotas del préstamo");
        }
    }
    
    /**
     * Maneja el cálculo del pago
     */
    @FXML
    private void handleCalcular() {
        if (cmbCuotaPagar.getValue() == null) {
            mostrarAdvertencia("Por favor seleccione una cuota para pagar");
            return;
        }
        
        try {
            Cronograma cuotaSeleccionada = cmbCuotaPagar.getValue();
            BigDecimal montoCuota = cuotaSeleccionada.getMontoCuota();
            
            lblMontoCuota.setText("S/ " + String.format("%.2f", montoCuota));
            
            String montoPagarStr = txtMontoPagar.getText().trim();
            if (montoPagarStr.isEmpty()) {
                txtMontoPagar.setText(String.format("%.2f", montoCuota));
                lblMontoPagar.setText("S/ " + String.format("%.2f", montoCuota));
                lblCambio.setText("S/ 0.00");
            } else {
                BigDecimal montoPagar = new BigDecimal(montoPagarStr);
                lblMontoPagar.setText("S/ " + String.format("%.2f", montoPagar));
                
                if (montoPagar.compareTo(montoCuota) >= 0) {
                    BigDecimal cambio = montoPagar.subtract(montoCuota);
                    lblCambio.setText("S/ " + String.format("%.2f", cambio));
                } else {
                    lblCambio.setText("Pago insuficiente");
                }
            }
            
            // Calcular nuevo saldo (simplificado)
            BigDecimal saldoActual = prestamoSeleccionado.getMontoSolicitado(); // TODO: Calcular saldo real
            BigDecimal nuevoSaldo = saldoActual.subtract(montoCuota);
            lblNuevoSaldo.setText("S/ " + String.format("%.2f", nuevoSaldo));
            
        } catch (NumberFormatException e) {
            mostrarAdvertencia("Por favor ingrese un monto válido");
        } catch (Exception e) {
            logger.error("Error al calcular pago", e);
            mostrarError("Error al calcular el pago");
        }
    }
    
    /**
     * Maneja el registro del cobro (siguiendo el flujo de negocio)
     */
    @FXML
    private void handleRegistrarPago() {
        if (!validarDatosPago()) {
            return;
        }
        
        try {
            Cronograma cuotaSeleccionada = cmbCuotaPagar.getValue();
            BigDecimal montoPagar = new BigDecimal(txtMontoPagar.getText());
            LocalDate fechaPago = dpFechaPago.getValue();
            String metodoPago = cmbMetodoPago.getValue();
            String referencia = txtReferencia.getText().trim();
            String observaciones = txtObservaciones.getText().trim();
            
            // PASO 1: Registrar en tabla recaudacion_asesor (borrador)
            boolean recaudacionRegistrada = recaudacionService.registrarBorrador(
                idAsesorActual,                    // ID del asesor actual
                clienteSeleccionado.getIdCliente(), // ID del cliente
                prestamoSeleccionado.getIdPrestamo(), // ID del préstamo
                montoPagar                         // Monto cobrado
            );
            
            if (!recaudacionRegistrada) {
                mostrarError("Error al registrar el cobro en el sistema");
                return;
            }
            
            // PASO 2: Actualizar el estado de la cuota en el cronograma
            boolean cuotaActualizada = cronogramaDAO.marcarComoPagada(
                cuotaSeleccionada.getIdCuota(), 
                fechaPago
            );
            
            if (!cuotaActualizada) {
                logger.warn("Recaudación registrada pero no se pudo actualizar la cuota: " + cuotaSeleccionada.getIdCuota());
                mostrarAdvertencia("Cobro registrado pero no se pudo actualizar el estado de la cuota. Contacte al administrador.");
            } else {
                // Actualizar el estado localmente para reflejar el cambio
                cuotaSeleccionada.setEstadoCuota(Cronograma.EstadoCuota.PAGADA);
                cuotaSeleccionada.setFechaPagoReal(fechaPago);
                
                // Refrescar la tabla de cuotas
                tblCuotas.refresh();
                
                mostrarInfo("✅ Cobro registrado exitosamente\n\n" +
                    "📋 Detalles del cobro:\n" +
                    "• Cliente: " + clienteSeleccionado.getNombre() + " " + clienteSeleccionado.getApellido() + "\n" +
                    "• Cuota #" + cuotaSeleccionada.getNumeroCuota() + "\n" +
                    "• Monto: S/ " + String.format("%.2f", montoPagar) + "\n" +
                    "• Fecha: " + FechaUtil.formatearFecha(fechaPago) + "\n" +
                    "• Método: " + metodoPago + "\n\n" +
                    "⚠️ Nota: Este cobro está pendiente de validación por el administrador.");
            }
            
            // Limpiar formulario después del registro exitoso
            limpiarFormulario();
            
            logger.info("Cobro registrado exitosamente - Cliente: " + clienteSeleccionado.getIdCliente() + 
                       ", Cuota: " + cuotaSeleccionada.getNumeroCuota() + 
                       ", Monto: " + montoPagar);
            
        } catch (Exception e) {
            logger.error("Error al registrar cobro", e);
            mostrarError("Error al registrar el cobro: " + e.getMessage());
        }
    }
    
    /**
     * Valida los datos del cobro
     */
    private boolean validarDatosPago() {
        // Validar que se haya seleccionado un cliente
        if (clienteSeleccionado == null) {
            mostrarAdvertencia("Por favor seleccione un cliente");
            return false;
        }
        
        // Validar que se haya seleccionado un préstamo
        if (prestamoSeleccionado == null) {
            mostrarAdvertencia("Por favor seleccione un préstamo");
            return false;
        }
        
        // Validar que se haya seleccionado una cuota
        if (cmbCuotaPagar.getValue() == null) {
            mostrarAdvertencia("Por favor seleccione una cuota para cobrar");
            return false;
        }
        
        // Validar que la cuota no esté ya pagada
        Cronograma cuotaSeleccionada = cmbCuotaPagar.getValue();
        if (cuotaSeleccionada.getEstadoCuota() == Cronograma.EstadoCuota.PAGADA) {
            mostrarAdvertencia("Esta cuota ya está pagada");
            return false;
        }
        
        // Validar monto
        if (txtMontoPagar.getText().trim().isEmpty()) {
            mostrarAdvertencia("Por favor ingrese el monto a cobrar");
            return false;
        }
        
        // Validar fecha de pago
        if (dpFechaPago.getValue() == null) {
            mostrarAdvertencia("Por favor seleccione la fecha de cobro");
            return false;
        }
        
        // Validar método de pago
        if (cmbMetodoPago.getValue() == null) {
            mostrarAdvertencia("Por favor seleccione el método de pago");
            return false;
        }
        
        try {
            BigDecimal montoPagar = new BigDecimal(txtMontoPagar.getText());
            if (montoPagar.compareTo(BigDecimal.ZERO) <= 0) {
                mostrarAdvertencia("El monto debe ser mayor a cero");
                return false;
            }
            
            // Validar que el monto coincida con el monto de la cuota (con tolerancia)
            BigDecimal montoCuota = cuotaSeleccionada.getMontoCuota();
            BigDecimal diferencia = montoPagar.subtract(montoCuota).abs();
            if (diferencia.compareTo(new BigDecimal("0.01")) > 0) {
                int respuesta = mostrarConfirmacion(
                    "⚠️ Monto no coincide\n\n" +
                    "Monto de la cuota: S/ " + String.format("%.2f", montoCuota) + "\n" +
                    "Monto a cobrar: S/ " + String.format("%.2f", montoPagar) + "\n\n" +
                    "¿Desea continuar con este monto?"
                );
                if (respuesta != 1) {
                    return false;
                }
            }
            
        } catch (NumberFormatException e) {
            mostrarAdvertencia("Por favor ingrese un monto válido");
            return false;
        }
        
        return true;
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
     * Maneja la selección de cliente
     */
    @FXML
    private void handleSeleccionarCliente() {
        Cliente cliente = cmbCliente.getValue();
        if (cliente != null) {
            clienteSeleccionado = cliente;
            mostrarInfoCliente(cliente);
            cargarPrestamosCliente(cliente);
        }
    }
    
    /**
     * Maneja la selección de préstamo
     */
    @FXML
    private void handleSeleccionarPrestamo() {
        Prestamo prestamo = tblPrestamos.getSelectionModel().getSelectedItem();
        if (prestamo != null) {
            prestamoSeleccionado = prestamo;
            cargarCuotasPrestamo(prestamo);
            logger.info("Préstamo seleccionado: " + prestamo.getIdPrestamo());
        }
    }
    
    /**
     * Maneja la selección de cuota
     */
    @FXML
    private void handleSeleccionarCuota() {
        Cronograma cuota = cmbCuotaPagar.getValue();
        if (cuota != null) {
            // Auto-completar el monto con el monto de la cuota
            txtMontoPagar.setText(String.format("%.2f", cuota.getMontoCuota()));
            
            // Calcular automáticamente
            handleCalcular();
            
            logger.info("Cuota seleccionada: #" + cuota.getNumeroCuota() + " - S/ " + cuota.getMontoCuota());
        }
    }
    
    /**
     * Limpia todos los campos del formulario
     */
    private void limpiarFormulario() {
        cmbCliente.setValue(null);
        txtDniCliente.clear();
        
        // Limpiar información del cliente
        lblNombreCliente.setText("-");
        lblDniCliente.setText("-");
        lblTelefonoCliente.setText("-");
        lblEmailCliente.setText("-");
        
        // Limpiar tablas
        prestamos.clear();
        cuotas.clear();
        cmbCuotaPagar.getItems().clear();
        
        // Limpiar información del pago
        txtMontoPagar.clear();
        dpFechaPago.setValue(LocalDate.now());
        cmbMetodoPago.setValue("EFECTIVO");
        txtReferencia.clear();
        txtObservaciones.clear();
        
        // Limpiar resumen
        lblMontoCuota.setText("S/ 0.00");
        lblMontoPagar.setText("S/ 0.00");
        lblCambio.setText("S/ 0.00");
        lblNuevoSaldo.setText("S/ 0.00");
        
        clienteSeleccionado = null;
        prestamoSeleccionado = null;
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
    
    /**
     * Muestra un mensaje de confirmación
     */
    private int mostrarConfirmacion(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        
        ButtonType btnSi = new ButtonType("Sí");
        ButtonType btnNo = new ButtonType("No");
        alert.getButtonTypes().setAll(btnSi, btnNo);
        
        java.util.Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == btnSi) {
            return 1; // Sí
        } else {
            return 0; // No
        }
    }
}
