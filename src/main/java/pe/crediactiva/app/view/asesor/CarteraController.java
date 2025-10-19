package pe.crediactiva.app.view.asesor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import pe.crediactiva.app.model.Cliente;
import pe.crediactiva.app.model.Cronograma;
import pe.crediactiva.app.model.Pago;
import pe.crediactiva.app.model.Prestamo;
import pe.crediactiva.app.service.ClienteService;
import pe.crediactiva.app.service.PagoService;
import pe.crediactiva.app.service.PrestamoService;
import pe.crediactiva.app.service.RecaudacionService;
import pe.crediactiva.app.util.FechaUtil;
import pe.crediactiva.app.config.SessionManager;
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
    private Long asesorId;
    
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
            // Obtener ID del asesor desde el contexto
            asesorId = obtenerAsesorIdActual();
            
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
     * Obtiene el ID del asesor actual
     */
    private Long obtenerAsesorIdActual() {
        try {
            // TODO: Obtener desde el contexto de autenticación
            // Por ahora retornamos un ID por defecto
            return 1L;
        } catch (Exception e) {
            logger.error("Error al obtener ID del asesor", e);
            return null;
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
        colPrestamosCliente.setCellValueFactory(cellData -> {
            Cliente cliente = cellData.getValue();
            try {
                List<Prestamo> prestamos = prestamoService.obtenerPrestamosPorCliente(cliente.getIdCliente());
                return new javafx.beans.property.SimpleIntegerProperty(prestamos.size()).asObject();
            } catch (Exception e) {
                return new javafx.beans.property.SimpleIntegerProperty(0).asObject();
            }
        });
        colSaldoCliente.setCellValueFactory(cellData -> {
            Cliente cliente = cellData.getValue();
            try {
                double montoPendiente = prestamoService.obtenerMontoPendientePorCliente(cliente.getIdCliente());
                return new javafx.beans.property.SimpleStringProperty("S/ " + String.format("%.2f", montoPendiente));
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("S/ 0.00");
            }
        });
        colEstadoCliente.setCellValueFactory(cellData -> {
            Cliente cliente = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                cliente.isActivo() ? "Activo" : "Inactivo"
            );
        });
        colUltimoPago.setCellValueFactory(cellData -> {
            Cliente cliente = cellData.getValue();
            try {
                String ultimoPago = prestamoService.obtenerUltimoPagoPorCliente(cliente.getIdCliente());
                return new javafx.beans.property.SimpleStringProperty(ultimoPago != null ? ultimoPago : "-");
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("-");
            }
        });
        
        tblClientes.setItems(clientes);
        
        // Configurar tabla de préstamos del cliente
        colIdPrestamo.setCellValueFactory(new PropertyValueFactory<>("idPrestamo"));
        colMontoPrestamo.setCellValueFactory(cellData -> {
            Prestamo prestamo = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty("S/ " + String.format("%.2f", prestamo.getMontoSolicitado()));
        });
        colSaldoPrestamo.setCellValueFactory(cellData -> {
            Prestamo prestamo = cellData.getValue();
            try {
                // Obtener todos los préstamos del cliente para calcular el saldo total
                List<Prestamo> prestamosCliente = prestamoService.obtenerPrestamosPorCliente(prestamo.getIdCliente());
                BigDecimal montoTotalPrestado = prestamosCliente.stream()
                    .map(Prestamo::getMontoSolicitado)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                double totalPagado = prestamoService.obtenerTotalPagadoPorCliente(prestamo.getIdCliente());
                double saldoPendiente = montoTotalPrestado.doubleValue() - totalPagado;
                return new javafx.beans.property.SimpleStringProperty("S/ " + String.format("%.2f", Math.max(0.0, saldoPendiente)));
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("S/ 0.00");
            }
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
            // Obtener el ID del asesor actual de la sesión
            Long idAsesor = SessionManager.getInstance().getAsesorId();
            
            if (idAsesor == null) {
                logger.error("No se pudo obtener el ID del asesor de la sesión");
                mostrarError("Error: No se pudo identificar al asesor");
                return;
            }
            
            // Obtener solo los clientes del asesor actual
            List<Cliente> todosClientes = clienteService.obtenerClientesPorAsesor(idAsesor);
            
            // Total de clientes
            int totalClientes = todosClientes.size();
            lblTotalClientes.setText(String.valueOf(totalClientes));
            
            // Préstamos activos y monto total prestado
            int prestamosActivos = 0;
            BigDecimal montoTotalPrestado = BigDecimal.ZERO;
            BigDecimal saldoPorCobrar = BigDecimal.ZERO;
            
            for (Cliente cliente : todosClientes) {
                List<Prestamo> prestamos = prestamoService.obtenerPrestamosPorCliente(cliente.getIdCliente());
                for (Prestamo prestamo : prestamos) {
                    if (prestamo.getEstado() == Prestamo.EstadoPrestamo.ACTIVO) {
                        prestamosActivos++;
                    }
                    montoTotalPrestado = montoTotalPrestado.add(prestamo.getMontoSolicitado());
                    
                    // Calcular saldo pendiente (solo una vez por cliente)
                    if (prestamos.indexOf(prestamo) == 0) { // Solo para el primer préstamo de cada cliente
                        double saldoPendiente = prestamoService.obtenerMontoPendientePorCliente(cliente.getIdCliente());
                        saldoPorCobrar = saldoPorCobrar.add(BigDecimal.valueOf(saldoPendiente));
                    }
                }
            }
            
            lblPrestamosActivos.setText(String.valueOf(prestamosActivos));
            lblMontoTotalPrestado.setText("S/ " + String.format("%.2f", montoTotalPrestado));
            lblSaldoPorCobrar.setText("S/ " + String.format("%.2f", saldoPorCobrar));
            
            // Recaudación del mes (todos los pagos del mes actual)
            LocalDate fechaInicio = LocalDate.now().withDayOfMonth(1);
            LocalDate fechaFin = LocalDate.now();
            BigDecimal recaudacionMes = BigDecimal.ZERO;
            if (asesorId != null) {
                recaudacionMes = pagoService.calcularTotalPagosAsesor(asesorId, fechaInicio, fechaFin);
            }
            lblRecaudacionMes.setText("S/ " + String.format("%.2f", recaudacionMes));
            
            // Morosidad (simplificada)
            double morosidad = prestamosActivos > 0 ? 0.0 : 0.0; // TODO: Calcular morosidad real
            lblMorosidad.setText(String.format("%.1f%%", morosidad));
            
        } catch (Exception e) {
            logger.error("Error al cargar resumen de cartera", e);
            mostrarError("Error al cargar el resumen de la cartera");
        }
    }
    
    /**
     * Carga la lista de clientes del asesor
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
            
            logger.info("Cargados " + listaClientes.size() + " clientes para la cartera del asesor: " + idAsesor);
            
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
            // Cargar préstamos del cliente
            List<Prestamo> prestamos = prestamoService.obtenerPrestamosPorCliente(cliente.getIdCliente());
            prestamosCliente.clear();
            prestamosCliente.addAll(prestamos);
            
            // Cargar cronograma del cliente (del primer préstamo activo si existe)
            List<Cronograma> cronograma = new ArrayList<>();
            if (!prestamos.isEmpty()) {
                // Buscar el primer préstamo activo para cargar su cronograma
                prestamos.stream()
                    .filter(p -> p.getEstado() == Prestamo.EstadoPrestamo.ACTIVO)
                    .findFirst()
                    .ifPresent(prestamoActivo -> {
                        // TODO: Implementar obtenerCronogramaPorPrestamo en CronogramaService
                        // cronograma = cronogramaService.obtenerCronogramaPorPrestamo(prestamoActivo.getIdPrestamo());
                        logger.info("Cronograma del préstamo activo: " + prestamoActivo.getIdPrestamo());
                    });
            }
            cronogramaCliente.clear();
            cronogramaCliente.addAll(cronograma);
            
            // Cargar historial de pagos del cliente
            List<Pago> pagos = pagoService.obtenerPagosPorCliente(cliente.getIdCliente());
            historialPagos.clear();
            historialPagos.addAll(pagos);
            
            logger.info("Cargados detalles para cliente: " + cliente.getNombre() + " " + cliente.getApellido());
            
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
            // Obtener el ID del asesor actual de la sesión
            Long idAsesor = SessionManager.getInstance().getAsesorId();
            
            if (idAsesor == null) {
                logger.error("No se pudo obtener el ID del asesor de la sesión");
                mostrarError("Error: No se pudo identificar al asesor");
                return;
            }
            
            // Obtener solo los clientes del asesor actual
            List<Cliente> todosClientes = clienteService.obtenerClientesPorAsesor(idAsesor);
            List<Cliente> clientesEncontrados = new ArrayList<>();
            
            if (busqueda.isEmpty()) {
                clientesEncontrados = todosClientes;
            } else {
                for (Cliente cliente : todosClientes) {
                    String nombreCompleto = (cliente.getNombre() + " " + cliente.getApellido()).toLowerCase();
                    String dni = cliente.getDni() != null ? cliente.getDni() : "";
                    String telefono = cliente.getTelefono() != null ? cliente.getTelefono() : "";
                    
                    if (nombreCompleto.contains(busqueda.toLowerCase()) ||
                        dni.contains(busqueda) ||
                        telefono.contains(busqueda)) {
                        clientesEncontrados.add(cliente);
                    }
                }
            }
            
            clientes.clear();
            clientes.addAll(clientesEncontrados);
            
            logger.info("Búsqueda realizada: '" + busqueda + "' - Encontrados: " + clientesEncontrados.size());
            
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
            // Obtener el ID del asesor actual de la sesión
            Long idAsesor = SessionManager.getInstance().getAsesorId();
            
            if (idAsesor == null) {
                logger.error("No se pudo obtener el ID del asesor de la sesión");
                mostrarError("Error: No se pudo identificar al asesor");
                return;
            }
            
            // Obtener solo los clientes del asesor actual
            List<Cliente> todosClientes = clienteService.obtenerClientesPorAsesor(idAsesor);
            List<Cliente> clientesFiltrados = new ArrayList<>();
            
            for (Cliente cliente : todosClientes) {
                boolean incluir = false;
                
                switch (filtroSeleccionado) {
                    case "Todos":
                        incluir = true;
                        break;
                    case "Con préstamos activos":
                        List<Prestamo> prestamosActivos = prestamoService.obtenerPrestamosPorCliente(cliente.getIdCliente());
                        incluir = prestamosActivos.stream().anyMatch(p -> p.getEstado() == Prestamo.EstadoPrestamo.ACTIVO);
                        break;
                    case "Con cuotas vencidas":
                        List<Prestamo> prestamosVencidos = prestamoService.obtenerPrestamosPorCliente(cliente.getIdCliente());
                        incluir = prestamosVencidos.stream().anyMatch(p -> p.getEstado() == Prestamo.EstadoPrestamo.ACTIVO);
                        break;
                    case "Al día":
                        List<Prestamo> prestamosAlDia = prestamoService.obtenerPrestamosPorCliente(cliente.getIdCliente());
                        incluir = prestamosAlDia.stream().anyMatch(p -> p.getEstado() == Prestamo.EstadoPrestamo.ACTIVO);
                        break;
                    case "Morosos":
                        List<Prestamo> prestamosMorosos = prestamoService.obtenerPrestamosPorCliente(cliente.getIdCliente());
                        incluir = prestamosMorosos.stream().anyMatch(p -> p.getEstado() == Prestamo.EstadoPrestamo.ACTIVO);
                        break;
                }
                
                if (incluir) {
                    clientesFiltrados.add(cliente);
                }
            }
            
            clientes.clear();
            clientes.addAll(clientesFiltrados);
            
            logger.info("Filtro aplicado: " + filtroSeleccionado + " - Resultados: " + clientesFiltrados.size());
            
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
            // Crear diálogo de registro de pago
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Registrar Pago");
            dialog.setHeaderText("Registrar pago para: " + clienteSeleccionado.getNombre() + " " + clienteSeleccionado.getApellido());
            
            // Crear campos del formulario
            TextField txtMonto = new TextField();
            txtMonto.setPromptText("Monto del pago");
            
            ComboBox<String> cmbMetodoPago = new ComboBox<>();
            cmbMetodoPago.getItems().addAll("Efectivo", "Transferencia", "Yape", "Plin");
            cmbMetodoPago.setValue("Efectivo");
            
            TextArea txtObservaciones = new TextArea();
            txtObservaciones.setPromptText("Observaciones (opcional)");
            txtObservaciones.setPrefRowCount(3);
            
            GridPane grid = new GridPane();
            grid.add(new Label("Monto:"), 0, 0);
            grid.add(txtMonto, 1, 0);
            grid.add(new Label("Método:"), 0, 1);
            grid.add(cmbMetodoPago, 1, 1);
            grid.add(new Label("Observaciones:"), 0, 2);
            grid.add(txtObservaciones, 1, 2);
            grid.setHgap(10);
            grid.setVgap(10);
            
            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    try {
                        BigDecimal monto = new BigDecimal(txtMonto.getText());
                        String metodo = cmbMetodoPago.getValue();
                        String observaciones = txtObservaciones.getText();
                        
                        // Obtener el primer préstamo activo del cliente
                        List<Prestamo> prestamos = prestamoService.obtenerPrestamosPorCliente(clienteSeleccionado.getIdCliente());
                        Prestamo prestamoActivo = prestamos.stream()
                            .filter(p -> p.getEstado() == Prestamo.EstadoPrestamo.ACTIVO)
                            .findFirst()
                            .orElse(null);
                        
                        if (prestamoActivo == null) {
                            mostrarError("El cliente no tiene préstamos activos");
                            return buttonType;
                        }
                        
                        // Obtener la primera cuota pendiente
                        // TODO: Implementar obtención de cuota pendiente
                        Long idCuota = 1L; // Temporal
                        
                        // Guardar el pago usando el método correcto
                        boolean success = pagoService.registrarPago(idCuota, clienteSeleccionado.getIdCliente(), asesorId, monto);
                        if (success) {
                            mostrarInfo("Pago registrado exitosamente");
                            cargarDetallesCliente(clienteSeleccionado);
                            cargarResumenCartera();
                        } else {
                            mostrarError("Error al registrar el pago");
                        }
                        
                    } catch (NumberFormatException e) {
                        mostrarError("Por favor ingrese un monto válido");
                        return buttonType;
                    }
                }
                return buttonType;
            });
            
            dialog.showAndWait();
            
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
            // Mostrar información de contacto del cliente
            StringBuilder info = new StringBuilder();
            info.append("Información de contacto:\n\n");
            info.append("Nombre: ").append(clienteSeleccionado.getNombre()).append(" ").append(clienteSeleccionado.getApellido()).append("\n");
            info.append("DNI: ").append(clienteSeleccionado.getDni()).append("\n");
            info.append("Teléfono: ").append(clienteSeleccionado.getTelefono()).append("\n");
            info.append("Dirección: ").append(clienteSeleccionado.getDireccion()).append("\n\n");
            info.append("Préstamos activos: ");
            
            List<Prestamo> prestamos = prestamoService.obtenerPrestamosPorCliente(clienteSeleccionado.getIdCliente());
            List<Prestamo> prestamosActivos = prestamos.stream()
                .filter(p -> p.getEstado() == Prestamo.EstadoPrestamo.ACTIVO)
                .collect(java.util.stream.Collectors.toList());
            
            info.append(prestamosActivos.size()).append("\n");
            
            for (Prestamo prestamo : prestamosActivos) {
                double saldoPendiente = prestamo.getMontoSolicitado().doubleValue() - 
                    prestamoService.obtenerTotalPagadoPorCliente(clienteSeleccionado.getIdCliente());
                info.append("- Préstamo ID: ").append(prestamo.getIdPrestamo())
                    .append(" - Saldo: S/ ").append(String.format("%.2f", saldoPendiente)).append("\n");
            }
            
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Información del Cliente");
            alert.setHeaderText("Datos de contacto");
            alert.setContentText(info.toString());
            alert.showAndWait();
            
        } catch (Exception e) {
            logger.error("Error al contactar cliente", e);
            mostrarError("Error al obtener información del cliente");
        }
    }
    
    /**
     * Maneja la generación de reporte
     */
    @FXML
    private void handleGenerarReporte() {
        try {
            // Crear diálogo para seleccionar tipo de reporte
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Generar Reporte");
            dialog.setHeaderText("Seleccione el tipo de reporte a generar");
            
            ComboBox<String> cmbTipoReporte = new ComboBox<>();
            cmbTipoReporte.getItems().addAll(
                "Reporte de Cartera General",
                "Reporte de Préstamos Activos",
                "Reporte de Morosidad",
                "Reporte de Recaudación"
            );
            cmbTipoReporte.setValue("Reporte de Cartera General");
            
            DatePicker dpFechaInicio = new DatePicker();
            dpFechaInicio.setValue(LocalDate.now().minusMonths(1));
            
            DatePicker dpFechaFin = new DatePicker();
            dpFechaFin.setValue(LocalDate.now());
            
            GridPane grid = new GridPane();
            grid.add(new Label("Tipo de reporte:"), 0, 0);
            grid.add(cmbTipoReporte, 1, 0);
            grid.add(new Label("Fecha inicio:"), 0, 1);
            grid.add(dpFechaInicio, 1, 1);
            grid.add(new Label("Fecha fin:"), 0, 2);
            grid.add(dpFechaFin, 1, 2);
            grid.setHgap(10);
            grid.setVgap(10);
            
            dialog.getDialogPane().setContent(grid);
            dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
            
            dialog.setResultConverter(buttonType -> {
                if (buttonType == ButtonType.OK) {
                    String tipoReporte = cmbTipoReporte.getValue();
                    LocalDate fechaInicio = dpFechaInicio.getValue();
                    LocalDate fechaFin = dpFechaFin.getValue();
                    
                    // Generar reporte básico
                    generarReporteBasico(tipoReporte, fechaInicio, fechaFin);
                }
                return buttonType;
            });
            
            dialog.showAndWait();
            
        } catch (Exception e) {
            logger.error("Error al generar reporte", e);
            mostrarError("Error al generar reporte");
        }
    }
    
    /**
     * Genera un reporte básico
     */
    private void generarReporteBasico(String tipoReporte, LocalDate fechaInicio, LocalDate fechaFin) {
        try {
            // Obtener el ID del asesor actual de la sesión
            Long idAsesor = SessionManager.getInstance().getAsesorId();
            
            if (idAsesor == null) {
                logger.error("No se pudo obtener el ID del asesor de la sesión");
                mostrarError("Error: No se pudo identificar al asesor");
                return;
            }
            
            StringBuilder reporte = new StringBuilder();
            reporte.append("REPORTE: ").append(tipoReporte).append("\n");
            reporte.append("Período: ").append(fechaInicio).append(" - ").append(fechaFin).append("\n\n");
            
            // Obtener solo los clientes del asesor actual
            List<Cliente> todosClientes = clienteService.obtenerClientesPorAsesor(idAsesor);
            
            switch (tipoReporte) {
                case "Reporte de Cartera General":
                    reporte.append("Total de clientes: ").append(todosClientes.size()).append("\n");
                    
                    int prestamosActivos = 0;
                    BigDecimal montoTotal = BigDecimal.ZERO;
                    
                    for (Cliente cliente : todosClientes) {
                        List<Prestamo> prestamos = prestamoService.obtenerPrestamosPorCliente(cliente.getIdCliente());
                        prestamosActivos += prestamos.stream()
                            .mapToInt(p -> p.getEstado() == Prestamo.EstadoPrestamo.ACTIVO ? 1 : 0)
                            .sum();
                        montoTotal = prestamos.stream()
                            .map(Prestamo::getMontoSolicitado)
                            .reduce(montoTotal, BigDecimal::add);
                    }
                    
                    reporte.append("Préstamos activos: ").append(prestamosActivos).append("\n");
                    reporte.append("Monto total prestado: S/ ").append(String.format("%.2f", montoTotal)).append("\n");
                    break;
                    
                case "Reporte de Recaudación":
                    if (asesorId != null) {
                        BigDecimal recaudacion = pagoService.calcularTotalPagosAsesor(asesorId, fechaInicio, fechaFin);
                        reporte.append("Recaudación total: S/ ").append(String.format("%.2f", recaudacion)).append("\n");
                    }
                    break;
            }
            
            // Mostrar el reporte
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Reporte Generado");
            alert.setHeaderText(tipoReporte);
            alert.setContentText(reporte.toString());
            alert.getDialogPane().setPrefSize(500, 400);
            alert.showAndWait();
            
        } catch (Exception e) {
            logger.error("Error al generar reporte básico", e);
            mostrarError("Error al generar el reporte");
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
