package pe.crediactiva.app.view.asesor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import pe.crediactiva.app.model.Cliente;
import pe.crediactiva.app.model.Cronograma;
import pe.crediactiva.app.model.Prestamo;
import pe.crediactiva.app.service.ClienteService;
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
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Clase wrapper para Cronograma con funcionalidad de selección
 */
class CronogramaSeleccionable {
    private final Cronograma cronograma;
    private final javafx.beans.property.BooleanProperty seleccionado;
    
    public CronogramaSeleccionable(Cronograma cronograma) {
        this.cronograma = cronograma;
        this.seleccionado = new javafx.beans.property.SimpleBooleanProperty(false);
    }
    
    public Cronograma getCronograma() {
        return cronograma;
    }
    
    public javafx.beans.property.BooleanProperty seleccionadoProperty() {
        return seleccionado;
    }
    
    public boolean isSeleccionado() {
        return seleccionado.get();
    }
    
    public void setSeleccionado(boolean seleccionado) {
        this.seleccionado.set(seleccionado);
    }
}

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
    private TableView<CronogramaSeleccionable> tblCuotas;
    
    @FXML
    private TableColumn<CronogramaSeleccionable, Integer> colIdCuota;
    
    @FXML
    private TableColumn<CronogramaSeleccionable, Integer> colNumeroCuota;
    
    @FXML
    private TableColumn<CronogramaSeleccionable, String> colFechaVencimiento;
    
    @FXML
    private TableColumn<CronogramaSeleccionable, String> colMontoCuota;
    
    @FXML
    private TableColumn<CronogramaSeleccionable, String> colEstadoCuota;
    
    @FXML
    private TableColumn<CronogramaSeleccionable, String> colDiasVencido;
    
    @FXML
    private TableColumn<CronogramaSeleccionable, Boolean> colSeleccionar;
    
    @FXML
    private DatePicker dpFechaPago;
    
    @FXML
    private ComboBox<String> cmbMetodoPago;
    
    @FXML
    private TextField txtReferencia;
    
    @FXML
    private TextField txtObservaciones;
    
    @FXML
    private Button btnSeleccionarTodas;
    
    @FXML
    private Button btnDeseleccionarTodas;
    
    @FXML
    private Label lblCuotasSeleccionadas;
    
    @FXML
    private Label lblMontoTotal;
    
    @FXML
    private Label lblCantidadCuotas;
    
    @FXML
    private Label lblMontoTotalResumen;
    
    @FXML
    private Button btnAceptar;
    
    @FXML
    private Button btnCancelar;
    
    private ClienteService clienteService;
    private PrestamoService prestamoService;
    private RecaudacionService recaudacionService;
    private CronogramaDAO cronogramaDAO;
    private Cliente clienteSeleccionado;
    private Prestamo prestamoSeleccionado;
    private Long idAsesorActual;
    private ObservableList<Prestamo> prestamos;
    private ObservableList<CronogramaSeleccionable> cuotasSeleccionables;
    
    public RegistrarCobroController() {
        this.clienteService = new ClienteService();
        this.prestamoService = new PrestamoService();
        this.recaudacionService = new RecaudacionService();
        this.cronogramaDAO = new CronogramaDAOImpl();
        this.prestamos = FXCollections.observableArrayList();
        this.cuotasSeleccionables = FXCollections.observableArrayList();
        
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
        tblCuotas.setEditable(true);
        colSeleccionar.setEditable(true);
        colSeleccionar.setCellValueFactory(cellData -> cellData.getValue().seleccionadoProperty());
        colSeleccionar.setCellFactory(column -> {
            CheckBoxTableCell<CronogramaSeleccionable, Boolean> cell = new CheckBoxTableCell<>(index -> {
                if (index < 0 || index >= tblCuotas.getItems().size()) {
                    return new javafx.beans.property.SimpleBooleanProperty(false);
                }
                return tblCuotas.getItems().get(index).seleccionadoProperty();
            });
            cell.setEditable(true);
            return cell;
        });
        
        colIdCuota.setCellValueFactory(cellData -> {
            Cronograma cuota = cellData.getValue().getCronograma();
            return new javafx.beans.property.SimpleIntegerProperty(cuota.getIdCuota().intValue()).asObject();
        });
        
        colNumeroCuota.setCellValueFactory(cellData -> {
            Cronograma cuota = cellData.getValue().getCronograma();
            return new javafx.beans.property.SimpleIntegerProperty(cuota.getNumeroCuota()).asObject();
        });
        
        colFechaVencimiento.setCellValueFactory(cellData -> {
            Cronograma cuota = cellData.getValue().getCronograma();
            return new javafx.beans.property.SimpleStringProperty(FechaUtil.formatearFecha(cuota.getFechaProgramada()));
        });
        
        colMontoCuota.setCellValueFactory(cellData -> {
            Cronograma cuota = cellData.getValue().getCronograma();
            return new javafx.beans.property.SimpleStringProperty("S/ " + String.format("%.2f", cuota.getMontoCuota()));
        });
        
        colEstadoCuota.setCellValueFactory(cellData -> {
            Cronograma cuota = cellData.getValue().getCronograma();
            return new javafx.beans.property.SimpleStringProperty(cuota.getEstadoCuota().toString());
        });
        
        colDiasVencido.setCellValueFactory(cellData -> {
            Cronograma cuota = cellData.getValue().getCronograma();
            if (cuota.getEstadoCuota() == Cronograma.EstadoCuota.RETRASADA) {
                long diasVencido = ChronoUnit.DAYS.between(cuota.getFechaProgramada(), LocalDate.now());
                return new javafx.beans.property.SimpleStringProperty(String.valueOf(diasVencido));
            } else {
                return new javafx.beans.property.SimpleStringProperty("-");
            }
        });
        
        tblCuotas.setItems(cuotasSeleccionables);
        
        // Listener para actualizar cálculos cuando cambia la selección
        cuotasSeleccionables.addListener((javafx.collections.ListChangeListener<CronogramaSeleccionable>) change -> {
            while (change.next()) {
                if (change.wasUpdated()) {
                    actualizarCalculos();
                    logger.info("Lista de cuotas actualizada");
                }
            }
        });
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
            // Obtener cuotas disponibles para recaudación (filtra automáticamente por validacion_asesor)
            List<Cronograma> cuotasDisponibles = cronogramaDAO.findDisponiblesParaRecaudacion(prestamo.getIdPrestamo());
            cuotasSeleccionables.clear();
            
            // Convertir a CronogramaSeleccionable
            for (Cronograma cuota : cuotasDisponibles) {
                CronogramaSeleccionable cuotaSeleccionable = new CronogramaSeleccionable(cuota);
                cuotaSeleccionable.seleccionadoProperty().addListener((obs, oldVal, newVal) -> {
                    logger.info("=== LISTENER ACTIVADO ===");
                    logger.info("Cuota ID: {} - Cambio: {} -> {}", cuota.getIdCuota(), oldVal, newVal);
                    actualizarCalculos();
                });
                cuotasSeleccionables.add(cuotaSeleccionable);
            }
            
            actualizarCalculos();
            logger.info("Cargadas " + cuotasSeleccionables.size() + " cuotas para el préstamo: " + prestamo.getIdPrestamo());
            
        } catch (Exception e) {
            logger.error("Error al cargar cuotas del préstamo", e);
            mostrarError("Error al cargar las cuotas del préstamo");
        }
    }
    
    /**
     * Actualiza los cálculos basados en las cuotas seleccionadas
     */
    private void actualizarCalculos() {
        List<CronogramaSeleccionable> cuotasSeleccionadas = cuotasSeleccionables.stream()
                .filter(CronogramaSeleccionable::isSeleccionado)
                .collect(Collectors.toList());
        
        int cantidadCuotas = cuotasSeleccionadas.size();
        BigDecimal montoTotal = cuotasSeleccionadas.stream()
                .map(cs -> cs.getCronograma().getMontoCuota())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        lblCuotasSeleccionadas.setText("Cuotas seleccionadas: " + cantidadCuotas);
        lblMontoTotal.setText("S/ " + String.format("%.2f", montoTotal));
        lblCantidadCuotas.setText(String.valueOf(cantidadCuotas));
        lblMontoTotalResumen.setText("S/ " + String.format("%.2f", montoTotal));
    }
    
    
    /**
     * Maneja la selección de todas las cuotas
     */
    @FXML
    private void handleSeleccionarTodas() {
        cuotasSeleccionables.forEach(cs -> cs.setSeleccionado(true));
    }
    
    /**
     * Maneja la deselección de todas las cuotas
     */
    @FXML
    private void handleDeseleccionarTodas() {
        cuotasSeleccionables.forEach(cs -> cs.setSeleccionado(false));
    }
    
    /**
     * Maneja el cálculo del pago (ahora solo actualiza el cambio)
     */
    @FXML
    private void handleCalcular() {
        actualizarCalculos();
    }
    
    /**
     * Maneja el registro del cobro (siguiendo el flujo de negocio correcto)
     */
    @FXML
    private void handleRegistrarPago() {
        if (!validarDatosPago()) {
            return;
        }
        
        try {
            List<CronogramaSeleccionable> cuotasSeleccionadas = cuotasSeleccionables.stream()
                    .filter(CronogramaSeleccionable::isSeleccionado)
                    .collect(Collectors.toList());
            
            LocalDate fechaPago = dpFechaPago.getValue();
            String metodoPago = cmbMetodoPago.getValue();
            String referencia = txtReferencia.getText().trim();
            String observaciones = txtObservaciones.getText().trim();
            
            int cobrosRegistrados = 0;
            int cobrosFallidos = 0;
            StringBuilder detalles = new StringBuilder();
            
            // Procesar cada cuota seleccionada
            for (CronogramaSeleccionable cuotaSeleccionable : cuotasSeleccionadas) {
                Cronograma cuota = cuotaSeleccionable.getCronograma();
                BigDecimal montoCuota = cuota.getMontoCuota();
                
                try {
                    // PASO 1: Verificar si ya existe un registro para esta cuota específica
                    if (existeRecaudacionParaCuota(cuota.getIdCuota())) {
                        logger.warn("Ya existe una recaudación registrada para la cuota: " + cuota.getIdCuota());
                        cobrosFallidos++;
                        detalles.append("• Cuota #").append(cuota.getNumeroCuota())
                               .append(" - Ya registrada (duplicada)\n");
                        continue;
                    }
                    
                    // PASO 2: Registrar en tabla recaudacion_asesor (borrador con validado = 0)
                    boolean recaudacionRegistrada = recaudacionService.registrarBorradorParaCuota(
                        idAsesorActual,                    // ID del asesor actual
                        clienteSeleccionado.getIdCliente(), // ID del cliente
                        prestamoSeleccionado.getIdPrestamo(), // ID del préstamo
                        montoCuota,                        // Monto cobrado
                        cuota.getIdCuota(),                // ID de la cuota específica
                        fechaPago,                         // Fecha de pago
                        metodoPago,                        // Método de pago
                        referencia,                        // Referencia
                        observaciones                      // Observaciones
                    );
                    
                    if (recaudacionRegistrada) {
                        // Marcar la cuota como validada por el asesor (evita duplicados)
                        cronogramaDAO.marcarValidacionAsesor(cuota.getIdCuota(), true);
                        
                        // NO marcar la cuota como pagada - solo cuando el admin valide
                        cuotaSeleccionable.setSeleccionado(false); // Deseleccionar
                        cobrosRegistrados++;
                        
                        detalles.append("• Cuota #").append(cuota.getNumeroCuota())
                               .append(" - S/ ").append(String.format("%.2f", montoCuota)).append(" (Pendiente validación)\n");
                        
                        logger.info("Recaudación registrada para cuota: " + cuota.getIdCuota() + 
                                   " - Monto: " + montoCuota);
                    } else {
                        cobrosFallidos++;
                        logger.warn("No se pudo registrar recaudación para cuota: " + cuota.getIdCuota());
                    }
                    
                } catch (Exception e) {
                    cobrosFallidos++;
                    logger.error("Error al procesar cuota " + cuota.getIdCuota(), e);
                }
            }
            
            // Refrescar la tabla
            tblCuotas.refresh();
            
            // Mostrar resultado
            if (cobrosRegistrados > 0) {
                mostrarInfo("✅ Cobros registrados exitosamente\n\n" +
                    "📋 Resumen:\n" +
                    "• Cliente: " + clienteSeleccionado.getNombre() + " " + clienteSeleccionado.getApellido() + "\n" +
                    "• Cuotas procesadas: " + cobrosRegistrados + "\n" +
                    "• Fecha: " + FechaUtil.formatearFecha(fechaPago) + "\n" +
                    "• Método: " + metodoPago + "\n\n" +
                    "📋 Detalles de cuotas:\n" + detalles.toString() +
                    (cobrosFallidos > 0 ? "\n⚠️ " + cobrosFallidos + " cuotas no se pudieron procesar." : "") +
                    "\n\n⚠️ IMPORTANTE: Estas recaudaciones están registradas con validado = 0.\n" +
                    "Las cuotas se marcarán como pagadas SOLO cuando el administrador valide (validado = 1).");
                
                logger.info("Cobros registrados exitosamente - Cliente: " + clienteSeleccionado.getIdCliente() + 
                           ", Cuotas: " + cobrosRegistrados + ", Fallidas: " + cobrosFallidos);
                
                // Limpiar formulario después del registro exitoso
                limpiarFormulario();
            } else {
                mostrarError("No se pudo registrar ningún cobro. Contacte al administrador.");
            }
            
        } catch (Exception e) {
            logger.error("Error al registrar cobros", e);
            mostrarError("Error al registrar los cobros: " + e.getMessage());
        }
    }
    
    /**
     * Verifica si ya existe una recaudación para la cuota específica
     * Ahora es mucho más simple usando el campo validacion_asesor
     */
    private boolean existeRecaudacionParaCuota(Long idCuota) {
        try {
            // Buscar la cuota y verificar su estado de validación
            Optional<Cronograma> cuotaOpt = cronogramaDAO.findById(idCuota);
            if (cuotaOpt.isPresent()) {
                return cuotaOpt.get().isValidacionAsesor();
            }
            return false;
        } catch (Exception e) {
            logger.error("Error al verificar recaudación existente para cuota: " + idCuota, e);
            return false;
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
        
        // Validar que se hayan seleccionado cuotas
        List<CronogramaSeleccionable> cuotasSeleccionadas = cuotasSeleccionables.stream()
                .filter(CronogramaSeleccionable::isSeleccionado)
                .collect(Collectors.toList());
        
        if (cuotasSeleccionadas.isEmpty()) {
            mostrarAdvertencia("Por favor seleccione al menos una cuota para cobrar");
            return false;
        }
        
        // Validar que las cuotas no estén ya pagadas
        for (CronogramaSeleccionable cuotaSeleccionable : cuotasSeleccionadas) {
            Cronograma cuota = cuotaSeleccionable.getCronograma();
            if (cuota.getEstadoCuota() == Cronograma.EstadoCuota.PAGADA) {
                mostrarAdvertencia("La cuota #" + cuota.getNumeroCuota() + " ya está pagada");
                return false;
            }
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
        cuotasSeleccionables.clear();
        
        // Limpiar información del pago
        dpFechaPago.setValue(LocalDate.now());
        cmbMetodoPago.setValue("EFECTIVO");
        txtReferencia.clear();
        txtObservaciones.clear();
        
        // Limpiar resumen
        lblCuotasSeleccionadas.setText("Cuotas seleccionadas: 0");
        lblMontoTotal.setText("S/ 0.00");
        lblCantidadCuotas.setText("0");
        lblMontoTotalResumen.setText("S/ 0.00");
        
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
    
    /**
     * Maneja el botón Aceptar - Registra las cuotas seleccionadas
     */
    @FXML
    private void handleAceptar() {
        try {
            // Validar datos básicos
            if (cmbCliente.getValue() == null) {
                mostrarAlerta("Error", "Debe seleccionar un cliente", Alert.AlertType.ERROR);
                return;
            }
            
            if (tblPrestamos.getSelectionModel().getSelectedItem() == null) {
                mostrarAlerta("Error", "Debe seleccionar un préstamo", Alert.AlertType.ERROR);
                return;
            }
            
            List<CronogramaSeleccionable> cuotasSeleccionadas = cuotasSeleccionables.stream()
                    .filter(CronogramaSeleccionable::isSeleccionado)
                    .collect(Collectors.toList());
            
            if (cuotasSeleccionadas.isEmpty()) {
                mostrarAlerta("Error", "Debe seleccionar al menos una cuota", Alert.AlertType.ERROR);
                return;
            }
            
            if (cmbMetodoPago.getValue() == null) {
                mostrarAlerta("Error", "Debe seleccionar un método de pago", Alert.AlertType.ERROR);
                return;
            }
            
            // Obtener datos del formulario
            Cliente cliente = cmbCliente.getValue();
            Prestamo prestamo = tblPrestamos.getSelectionModel().getSelectedItem();
            Long idAsesor = SessionManager.getInstance().getAsesorId();
            String metodoPago = cmbMetodoPago.getValue();
            String referencia = txtReferencia.getText().trim();
            String observaciones = txtObservaciones.getText().trim();
            LocalDate fechaPago = dpFechaPago.getValue() != null ? dpFechaPago.getValue() : LocalDate.now();
            
            int recaudacionesRegistradas = 0;
            int errores = 0;
            
            // Registrar cada cuota seleccionada
            for (CronogramaSeleccionable cuotaSeleccionable : cuotasSeleccionadas) {
                Cronograma cuota = cuotaSeleccionable.getCronograma();
                
                try {
                    // Verificar si ya existe recaudación para esta cuota
                    if (existeRecaudacionParaCuota(cuota.getIdCuota())) {
                        logger.warn("Ya existe recaudación pendiente para la cuota: {}", cuota.getIdCuota());
                        errores++;
                        continue;
                    }
                    
                    // Registrar en recaudacion_asesor con validado = 0
                    boolean recaudacionRegistrada = recaudacionService.registrarBorradorParaCuota(
                        idAsesor, cliente.getIdCliente(), prestamo.getIdPrestamo(), 
                        cuota.getMontoCuota(), cuota.getIdCuota(), fechaPago, metodoPago, referencia, observaciones
                    );
                    
                    if (recaudacionRegistrada) {
                        // Marcar validacion_asesor = 1 en cronograma
                        cronogramaDAO.marcarValidacionAsesor(cuota.getIdCuota(), true);
                        recaudacionesRegistradas++;
                        logger.info("Recaudación registrada para cuota {} del préstamo {}", 
                                  cuota.getIdCuota(), prestamo.getIdPrestamo());
                    } else {
                        errores++;
                        logger.error("Error al registrar recaudación para cuota {}", cuota.getIdCuota());
                    }
                    
                } catch (Exception e) {
                    logger.error("Error al procesar cuota {}: {}", cuota.getIdCuota(), e.getMessage());
                    errores++;
                }
            }
            
            // Mostrar resultado
            if (recaudacionesRegistradas > 0) {
                String mensaje = String.format(
                    "✅ Registro exitoso!\n\n" +
                    "• Cuotas procesadas: %d\n" +
                    "• Registros creados: %d\n" +
                    "• Errores: %d\n\n" +
                    "Los registros están pendientes de validación por el administrador.",
                    cuotasSeleccionadas.size(), recaudacionesRegistradas, errores
                );
                
                mostrarAlerta("Registro Exitoso", mensaje, Alert.AlertType.INFORMATION);
                
                // Refrescar datos
                cargarCuotasPrestamo(prestamo);
                actualizarCalculos();
            } else {
                mostrarAlerta("Error", "No se pudo registrar ninguna recaudación", Alert.AlertType.ERROR);
            }
            
        } catch (Exception e) {
            logger.error("Error al procesar registro de cobro: {}", e.getMessage(), e);
            mostrarAlerta("Error", "Error interno al procesar el registro", Alert.AlertType.ERROR);
        }
    }
    
    /**
     * Maneja el botón Cancelar
     */
    @FXML
    private void handleCancelar() {
        limpiarFormulario();
    }
    
    /**
     * Muestra una alerta con el mensaje especificado
     */
    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alert = new Alert(tipo);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
