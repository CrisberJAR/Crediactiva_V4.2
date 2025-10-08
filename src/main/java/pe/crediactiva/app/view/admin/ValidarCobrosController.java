package pe.crediactiva.app.view.admin;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import pe.crediactiva.app.model.*;
import pe.crediactiva.app.service.*;
import pe.crediactiva.app.util.FechaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Optional;

/**
 * Controlador para validar cobros pendientes del administrador
 */
public class ValidarCobrosController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(ValidarCobrosController.class);
    
    @FXML
    private ComboBox<String> cmbAsesor;
    
    @FXML
    private DatePicker dpFecha;
    
    @FXML
    private Label lblTotalCobros;
    
    @FXML
    private Label lblMontoTotal;
    
    @FXML
    private Label lblCobrosValidados;
    
    @FXML
    private Label lblCobrosPendientes;
    
    @FXML
    private TableView<RecaudacionAsesor> tblCobrosPendientes;
    
    @FXML
    private TableColumn<RecaudacionAsesor, Long> colIdCobro;
    
    @FXML
    private TableColumn<RecaudacionAsesor, String> colAsesor;
    
    @FXML
    private TableColumn<RecaudacionAsesor, String> colCliente;
    
    @FXML
    private TableColumn<RecaudacionAsesor, Long> colPrestamo;
    
    @FXML
    private TableColumn<RecaudacionAsesor, String> colMonto;
    
    @FXML
    private TableColumn<RecaudacionAsesor, String> colFechaRegistro;
    
    @FXML
    private TableColumn<RecaudacionAsesor, String> colEstado;
    
    @FXML
    private Label lblClienteDetalle;
    
    @FXML
    private Label lblAsesorDetalle;
    
    @FXML
    private Label lblPrestamoDetalle;
    
    @FXML
    private Label lblNumeroCuotaDetalle;
    
    @FXML
    private Label lblMontoDetalle;
    
    @FXML
    private Label lblSaldoPendienteDetalle;
    
    @FXML
    private Label lblFechaPagoDetalle;
    
    @FXML
    private TextArea txtObservaciones;
    
    private RecaudacionService recaudacionService;
    private ClienteService clienteService;
    private AsesorService asesorService;
    private PrestamoService prestamoService;
    private PagoService pagoService;
    private AuditoriaService auditoriaService;
    
    private ObservableList<RecaudacionAsesor> cobrosPendientes;
    private RecaudacionAsesor cobroSeleccionado;
    
    public ValidarCobrosController() {
        this.recaudacionService = new RecaudacionService();
        this.clienteService = new ClienteService();
        this.asesorService = new AsesorService();
        this.prestamoService = new PrestamoService();
        this.pagoService = new PagoService();
        this.auditoriaService = new AuditoriaService();
        this.cobrosPendientes = FXCollections.observableArrayList();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            configurarTabla();
            configurarFiltros();
            cargarCobrosPendientes();
            actualizarResumen();
            
            logger.info("Validar cobros inicializado correctamente");
            
        } catch (Exception e) {
            logger.error("Error al inicializar validar cobros", e);
            mostrarError("Error al inicializar la validación de cobros");
        }
    }
    
    /**
     * Configura la tabla de cobros pendientes
     */
    private void configurarTabla() {
        // Configurar columnas
        colIdCobro.setCellValueFactory(new PropertyValueFactory<>("idRecaudacion"));
        
        colAsesor.setCellValueFactory(cellData -> {
            RecaudacionAsesor recaudacion = cellData.getValue();
            try {
                Optional<Asesor> asesorOpt = asesorService.obtenerAsesorPorId(recaudacion.getIdAsesor());
                return new javafx.beans.property.SimpleStringProperty(
                    asesorOpt.isPresent() ? asesorOpt.get().getNombre() + " " + asesorOpt.get().getApellido() : "N/A"
                );
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("N/A");
            }
        });
        
        colCliente.setCellValueFactory(cellData -> {
            RecaudacionAsesor recaudacion = cellData.getValue();
            try {
                Optional<Cliente> clienteOpt = clienteService.obtenerClientePorId(recaudacion.getIdCliente());
                return new javafx.beans.property.SimpleStringProperty(
                    clienteOpt.isPresent() ? clienteOpt.get().getNombre() + " " + clienteOpt.get().getApellido() : "N/A"
                );
            } catch (Exception e) {
                return new javafx.beans.property.SimpleStringProperty("N/A");
            }
        });
        
        colPrestamo.setCellValueFactory(cellData -> {
            RecaudacionAsesor recaudacion = cellData.getValue();
            return new javafx.beans.property.SimpleLongProperty(
                recaudacion.getIdPrestamo() != null ? recaudacion.getIdPrestamo() : 0L
            ).asObject();
        });
        
        colMonto.setCellValueFactory(cellData -> {
            RecaudacionAsesor recaudacion = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty("S/ " + String.format("%.2f", recaudacion.getMontoRegistrado()));
        });
        
        colFechaRegistro.setCellValueFactory(cellData -> {
            RecaudacionAsesor recaudacion = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                recaudacion.getFechaRegistro() != null ? FechaUtil.formatearFecha(recaudacion.getFechaRegistro().toLocalDate()) : "-"
            );
        });
        
        colEstado.setCellValueFactory(cellData -> {
            RecaudacionAsesor recaudacion = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                recaudacion.isValidado() ? "Validado" : "Pendiente"
            );
        });
        
        // Configurar tabla
        tblCobrosPendientes.setItems(cobrosPendientes);
        
        // Configurar selección
        tblCobrosPendientes.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    cobroSeleccionado = newValue;
                    mostrarDetallesCobro(newValue);
                }
            }
        );
    }
    
    /**
     * Configura los filtros
     */
    private void configurarFiltros() {
        // Configurar fecha por defecto
        dpFecha.setValue(LocalDate.now());
        
        // Cargar lista de asesores desde la base de datos
        try {
            List<Asesor> asesores = asesorService.obtenerAsesoresActivos();
            cmbAsesor.getItems().clear();
            cmbAsesor.getItems().add("Todos");
            
            for (Asesor asesor : asesores) {
                cmbAsesor.getItems().add(asesor.getNombreCompleto() + " (ID: " + asesor.getIdAsesor() + ")");
            }
            
            cmbAsesor.setValue("Todos");
            logger.info("Cargados " + asesores.size() + " asesores activos");
            
        } catch (Exception e) {
            logger.error("Error al cargar asesores", e);
            cmbAsesor.getItems().add("Error al cargar asesores");
            cmbAsesor.setValue("Error al cargar asesores");
        }
    }
    
    /**
     * Carga los cobros pendientes de validación
     */
    private void cargarCobrosPendientes() {
        try {
            // Obtener recaudaciones pendientes de validación
            List<RecaudacionAsesor> recaudacionesPendientes = recaudacionService.obtenerBorradoresPendientes();
            
            cobrosPendientes.clear();
            cobrosPendientes.addAll(recaudacionesPendientes);
            
            logger.info("Cargados " + recaudacionesPendientes.size() + " cobros pendientes");
            
        } catch (Exception e) {
            logger.error("Error al cargar cobros pendientes", e);
            mostrarError("Error al cargar los cobros pendientes");
        }
    }
    
    /**
     * Actualiza el resumen de cobros
     */
    private void actualizarResumen() {
        try {
            int totalCobros = cobrosPendientes.size();
            int cobrosValidados = 0;
            BigDecimal montoTotal = BigDecimal.ZERO;
            
            for (RecaudacionAsesor recaudacion : cobrosPendientes) {
                if (recaudacion.isValidado()) {
                    cobrosValidados++;
                }
                montoTotal = montoTotal.add(recaudacion.getMontoRegistrado());
            }
            
            int cobrosPendientesCount = totalCobros - cobrosValidados;
            
            lblTotalCobros.setText(String.valueOf(totalCobros));
            lblMontoTotal.setText("S/ " + String.format("%.2f", montoTotal));
            lblCobrosValidados.setText(String.valueOf(cobrosValidados));
            lblCobrosPendientes.setText(String.valueOf(cobrosPendientesCount));
            
        } catch (Exception e) {
            logger.error("Error al actualizar resumen", e);
        }
    }
    
    /**
     * Muestra los detalles del cobro seleccionado
     */
    private void mostrarDetallesCobro(RecaudacionAsesor recaudacion) {
        try {
            // Obtener información del cliente
            Optional<Cliente> clienteOpt = clienteService.obtenerClientePorId(recaudacion.getIdCliente());
            lblClienteDetalle.setText(clienteOpt.isPresent() ? clienteOpt.get().getNombre() + " " + clienteOpt.get().getApellido() : "N/A");
            
            // Obtener información del asesor
            Optional<Asesor> asesorOpt = asesorService.obtenerAsesorPorId(recaudacion.getIdAsesor());
            lblAsesorDetalle.setText(asesorOpt.isPresent() ? asesorOpt.get().getNombre() + " " + asesorOpt.get().getApellido() : "N/A");
            
            // Información del préstamo
            lblPrestamoDetalle.setText(recaudacion.getIdPrestamo() != null ? recaudacion.getIdPrestamo().toString() : "-");
            
            // Obtener número de cuota pendiente
            String numeroCuota = obtenerNumeroCuotaPendiente(recaudacion.getIdPrestamo());
            lblNumeroCuotaDetalle.setText(numeroCuota);
            
            // Obtener saldo pendiente del préstamo
            double saldoPendiente = prestamoService.obtenerMontoPendientePorCliente(recaudacion.getIdCliente());
            lblSaldoPendienteDetalle.setText("S/ " + String.format("%.2f", saldoPendiente));
            
            // Información de la recaudación
            lblMontoDetalle.setText("S/ " + String.format("%.2f", recaudacion.getMontoRegistrado()));
            lblFechaPagoDetalle.setText(recaudacion.getFechaRegistro() != null ? FechaUtil.formatearFecha(recaudacion.getFechaRegistro().toLocalDate()) : "-");
            
            // Limpiar observaciones
            txtObservaciones.clear();
            
        } catch (Exception e) {
            logger.error("Error al mostrar detalles del cobro", e);
            mostrarError("Error al cargar los detalles del cobro");
        }
    }
    
    /**
     * Maneja el filtrado de cobros
     */
    @FXML
    private void handleFiltrar() {
        try {
            String asesorSeleccionado = cmbAsesor.getValue();
            
            if ("Todos".equals(asesorSeleccionado) || asesorSeleccionado == null) {
                // Cargar todos los cobros pendientes
                cargarCobrosPendientes();
            } else {
                // Extraer ID del asesor del texto seleccionado
                Long idAsesor = extraerIdAsesor(asesorSeleccionado);
                if (idAsesor != null) {
                    List<RecaudacionAsesor> recaudacionesAsesor = recaudacionService.obtenerBorradoresPorAsesor(idAsesor);
                    cobrosPendientes.clear();
                    cobrosPendientes.addAll(recaudacionesAsesor);
                    logger.info("Cargados " + recaudacionesAsesor.size() + " cobros del asesor ID: " + idAsesor);
                } else {
                    cargarCobrosPendientes();
                }
            }
            
            actualizarResumen();
            logger.info("Filtros aplicados");
            
        } catch (Exception e) {
            logger.error("Error al filtrar cobros", e);
            mostrarError("Error al aplicar filtros");
        }
    }
    
    /**
     * Extrae el ID del asesor del texto seleccionado
     */
    private Long extraerIdAsesor(String textoAsesor) {
        try {
            if (textoAsesor.contains("(ID: ")) {
                String idStr = textoAsesor.substring(textoAsesor.lastIndexOf("(ID: ") + 5);
                idStr = idStr.replace(")", "");
                return Long.parseLong(idStr.trim());
            }
        } catch (Exception e) {
            logger.error("Error al extraer ID del asesor: " + textoAsesor, e);
        }
        return null;
    }
    
    /**
     * Obtiene el número de la próxima cuota pendiente del préstamo
     */
    private String obtenerNumeroCuotaPendiente(Long idPrestamo) {
        try {
            if (idPrestamo == null) {
                return "-";
            }
            
            // Obtener todas las cuotas del préstamo
            List<Cronograma> cuotas = prestamoService.obtenerCuotasPorPrestamo(idPrestamo);
            
            // Buscar la primera cuota pendiente
            for (Cronograma cuota : cuotas) {
                if (cuota.getEstadoCuota() == Cronograma.EstadoCuota.PENDIENTE) {
                    return String.valueOf(cuota.getNumeroCuota());
                }
            }
            
            // Si no hay cuotas pendientes, mostrar la última cuota
            if (!cuotas.isEmpty()) {
                Cronograma ultimaCuota = cuotas.get(cuotas.size() - 1);
                return String.valueOf(ultimaCuota.getNumeroCuota());
            }
            
            return "-";
            
        } catch (Exception e) {
            logger.error("Error al obtener número de cuota pendiente para préstamo: " + idPrestamo, e);
            return "-";
        }
    }
    
    /**
     * Obtiene el ID de la primera cuota pendiente del préstamo
     */
    private Long obtenerCuotaPendienteId(Long idPrestamo) {
        try {
            if (idPrestamo == null) {
                return null;
            }
            
            // Obtener todas las cuotas del préstamo
            List<Cronograma> cuotas = prestamoService.obtenerCuotasPorPrestamo(idPrestamo);
            
            // Buscar la primera cuota pendiente
            for (Cronograma cuota : cuotas) {
                if (cuota.getEstadoCuota() == Cronograma.EstadoCuota.PENDIENTE) {
                    return cuota.getIdCuota();
                }
            }
            
            // Si no hay cuotas pendientes, retornar null
            logger.warn("No se encontraron cuotas pendientes para el préstamo: " + idPrestamo);
            return null;
            
        } catch (Exception e) {
            logger.error("Error al obtener ID de cuota pendiente para préstamo: " + idPrestamo, e);
            return null;
        }
    }
    
    /**
     * Limpia los filtros
     */
    @FXML
    private void handleLimpiarFiltros() {
        cmbAsesor.setValue("Todos");
        dpFecha.setValue(LocalDate.now());
        cargarCobrosPendientes();
        actualizarResumen();
        
        logger.info("Filtros limpiados");
    }
    
    /**
     * Valida un cobro individual
     */
    @FXML
    private void handleValidarCobro() {
        if (cobroSeleccionado == null) {
            mostrarAdvertencia("Por favor seleccione un cobro para validar");
            return;
        }
        
        try {
            // Mostrar diálogo de confirmación
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Validación");
            alert.setHeaderText("Validar Cobro");
            alert.setContentText("¿Está seguro de que desea validar este cobro?\n\n" +
                               "Cliente: " + lblClienteDetalle.getText() + "\n" +
                               "Monto: " + lblMontoDetalle.getText());
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Validar el cobro
                boolean success = validarCobro(cobroSeleccionado, txtObservaciones.getText());
                
                if (success) {
                    mostrarInfo("Cobro validado exitosamente");
                    cargarCobrosPendientes();
                    actualizarResumen();
                    limpiarDetalles();
                } else {
                    mostrarError("Error al validar el cobro");
                }
            }
            
        } catch (Exception e) {
            logger.error("Error al validar cobro", e);
            mostrarError("Error al validar el cobro");
        }
    }
    
    /**
     * Rechaza un cobro individual
     */
    @FXML
    private void handleRechazarCobro() {
        if (cobroSeleccionado == null) {
            mostrarAdvertencia("Por favor seleccione un cobro para rechazar");
            return;
        }
        
        try {
            // Mostrar diálogo de confirmación
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Rechazo");
            alert.setHeaderText("Rechazar Cobro");
            alert.setContentText("¿Está seguro de que desea rechazar este cobro?\n\n" +
                               "Cliente: " + lblClienteDetalle.getText() + "\n" +
                               "Monto: " + lblMontoDetalle.getText() + "\n\n" +
                               "Esta acción eliminará el registro del pago y la cuota quedará pendiente.");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                // Rechazar el cobro
                boolean success = rechazarCobro(cobroSeleccionado, txtObservaciones.getText());
                
                if (success) {
                    mostrarInfo("Cobro rechazado exitosamente");
                    cargarCobrosPendientes();
                    actualizarResumen();
                    limpiarDetalles();
                } else {
                    mostrarError("Error al rechazar el cobro");
                }
            }
            
        } catch (Exception e) {
            logger.error("Error al rechazar cobro", e);
            mostrarError("Error al rechazar el cobro");
        }
    }
    
    /**
     * Valida todos los cobros pendientes
     */
    @FXML
    private void handleValidarTodos() {
        try {
            List<RecaudacionAsesor> cobrosPendientesList = cobrosPendientes.stream()
                .filter(recaudacion -> !recaudacion.isValidado())
                .collect(java.util.stream.Collectors.toList());
            
            if (cobrosPendientesList.isEmpty()) {
                mostrarInfo("No hay cobros pendientes para validar");
                return;
            }
            
            // Mostrar diálogo de confirmación
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Validación Masiva");
            alert.setHeaderText("Validar Todos los Cobros");
            alert.setContentText("¿Está seguro de que desea validar " + cobrosPendientesList.size() + " cobros pendientes?");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                
                int validados = 0;
                for (RecaudacionAsesor recaudacion : cobrosPendientesList) {
                    if (validarCobro(recaudacion, "Validación masiva")) {
                        validados++;
                    }
                }
                
                mostrarInfo("Se validaron " + validados + " de " + cobrosPendientesList.size() + " cobros");
                cargarCobrosPendientes();
                actualizarResumen();
                limpiarDetalles();
            }
            
        } catch (Exception e) {
            logger.error("Error al validar todos los cobros", e);
            mostrarError("Error al validar los cobros");
        }
    }
    
    /**
     * Rechaza todos los cobros pendientes
     */
    @FXML
    private void handleRechazarTodos() {
        try {
            List<RecaudacionAsesor> cobrosPendientesList = cobrosPendientes.stream()
                .filter(recaudacion -> !recaudacion.isValidado())
                .collect(java.util.stream.Collectors.toList());
            
            if (cobrosPendientesList.isEmpty()) {
                mostrarInfo("No hay cobros pendientes para rechazar");
                return;
            }
            
            // Mostrar diálogo de confirmación
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Rechazo Masivo");
            alert.setHeaderText("Rechazar Todos los Cobros");
            alert.setContentText("¿Está seguro de que desea rechazar " + cobrosPendientesList.size() + " cobros pendientes?\n\n" +
                               "Esta acción eliminará todos los registros de recaudación.");
            
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                
                int rechazados = 0;
                for (RecaudacionAsesor recaudacion : cobrosPendientesList) {
                    if (rechazarCobro(recaudacion, "Rechazo masivo")) {
                        rechazados++;
                    }
                }
                
                mostrarInfo("Se rechazaron " + rechazados + " de " + cobrosPendientesList.size() + " cobros");
                cargarCobrosPendientes();
                actualizarResumen();
                limpiarDetalles();
            }
            
        } catch (Exception e) {
            logger.error("Error al rechazar todos los cobros", e);
            mostrarError("Error al rechazar los cobros");
        }
    }
    
    /**
     * Actualiza la vista
     */
    @FXML
    private void handleActualizar() {
        cargarCobrosPendientes();
        actualizarResumen();
        mostrarInfo("Vista actualizada");
    }
    
    /**
     * Valida un cobro específico
     */
    private boolean validarCobro(RecaudacionAsesor recaudacion, String observaciones) {
        try {
            // Obtener la cuota pendiente que se está pagando
            Long idCuota = obtenerCuotaPendienteId(recaudacion.getIdPrestamo());
            
            if (idCuota == null) {
                logger.error("No se encontró cuota pendiente para el préstamo: " + recaudacion.getIdPrestamo());
                mostrarError("No se encontró cuota pendiente para este préstamo");
                return false;
            }
            
            // Obtener la fecha real del cobro registrado por el asesor
            LocalDate fechaRealCobro = recaudacion.getFechaRegistro().toLocalDate();
            
            // Registrar el pago en la tabla pagos con la fecha real del cobro
            boolean pagoRegistrado = pagoService.registrarPagoConFecha(
                idCuota,
                recaudacion.getIdCliente(),
                recaudacion.getIdAsesor(),
                recaudacion.getMontoRegistrado(),
                fechaRealCobro
            );
            
            if (!pagoRegistrado) {
                logger.error("Error al registrar el pago en la tabla pagos");
                mostrarError("Error al registrar el pago");
                return false;
            }
            
            // Validar la recaudación usando el servicio
            boolean success = recaudacionService.validarBorrador(recaudacion.getIdRecaudacion());
            
            if (success) {
                // Registrar auditoría
                auditoriaService.registrarAuditoria(
                    "recaudacion_asesor",
                    recaudacion.getIdRecaudacion().toString(),
                    "UPDATE",
                    "validado: false",
                    "validado: true - " + observaciones
                );
                
                logger.info("Cobro validado exitosamente: " + recaudacion.getIdRecaudacion() + 
                           " - Pago registrado para cuota: " + idCuota);
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            logger.error("Error al validar cobro: " + recaudacion.getIdRecaudacion(), e);
            return false;
        }
    }
    
    /**
     * Rechaza un cobro específico
     */
    private boolean rechazarCobro(RecaudacionAsesor recaudacion, String observaciones) {
        try {
            // Eliminar la recaudación de la base de datos
            boolean success = recaudacionService.eliminarRecaudacion(recaudacion.getIdRecaudacion());
            
            if (success) {
                // Registrar auditoría
                auditoriaService.registrarAuditoria(
                    "recaudacion_asesor",
                    recaudacion.getIdRecaudacion().toString(),
                    "DELETE",
                    "recaudación registrada",
                    "recaudación rechazada - " + observaciones
                );
                
                logger.info("Cobro rechazado exitosamente: " + recaudacion.getIdRecaudacion());
                return true;
            }
            
            return false;
            
        } catch (Exception e) {
            logger.error("Error al rechazar cobro: " + recaudacion.getIdRecaudacion(), e);
            return false;
        }
    }
    
    /**
     * Limpia los detalles mostrados
     */
    private void limpiarDetalles() {
        lblClienteDetalle.setText("-");
        lblAsesorDetalle.setText("-");
        lblPrestamoDetalle.setText("-");
        lblNumeroCuotaDetalle.setText("-");
        lblMontoDetalle.setText("-");
        lblSaldoPendienteDetalle.setText("-");
        lblFechaPagoDetalle.setText("-");
        txtObservaciones.clear();
        cobroSeleccionado = null;
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
