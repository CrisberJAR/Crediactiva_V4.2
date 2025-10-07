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
import pe.crediactiva.app.model.Prestamo;
import pe.crediactiva.app.service.PrestamoService;
import pe.crediactiva.app.service.AuditoriaService;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para la bandeja de solicitudes de préstamo
 */
public class BandejaSolicitudesController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(BandejaSolicitudesController.class);
    
    @FXML
    private ComboBox<String> comboAsesor;
    
    @FXML
    private ComboBox<String> comboEstado;
    
    @FXML
    private ComboBox<String> comboEtiqueta;
    
    @FXML
    private TextField txtMontoMin;
    
    @FXML
    private TextField txtMontoMax;
    
    @FXML
    private Button btnFiltrar;
    
    @FXML
    private Button btnLimpiarFiltros;
    
    @FXML
    private TableView<Prestamo> tablaSolicitudes;
    
    @FXML
    private TableColumn<Prestamo, Long> colId;
    
    @FXML
    private TableColumn<Prestamo, String> colCliente;
    
    @FXML
    private TableColumn<Prestamo, String> colAsesor;
    
    @FXML
    private TableColumn<Prestamo, Double> colMonto;
    
    @FXML
    private TableColumn<Prestamo, Double> colTasa;
    
    @FXML
    private TableColumn<Prestamo, Integer> colPeriodo;
    
    @FXML
    private TableColumn<Prestamo, String> colEstado;
    
    @FXML
    private TableColumn<Prestamo, String> colEtiqueta;
    
    @FXML
    private TableColumn<Prestamo, String> colFecha;
    
    @FXML
    private TableColumn<Prestamo, String> colObservacion;
    
    @FXML
    private Label lblCliente;
    
    @FXML
    private Label lblAsesor;
    
    @FXML
    private Label lblMontoSolicitado;
    
    @FXML
    private Label lblMontoDesembolsado;
    
    @FXML
    private TextField txtTasaInteres;
    
    @FXML
    private TextField txtPeriodo;
    
    @FXML
    private ComboBox<String> comboTipoPago;
    
    @FXML
    private TextField txtObservacion;
    
    @FXML
    private Button btnAprobar;
    
    @FXML
    private Button btnRechazar;
    
    @FXML
    private Button btnGuardar;
    
    @FXML
    private Button btnVerHistorial;
    
    @FXML
    private Label lblTotalSolicitudes;
    
    @FXML
    private Label lblSolicitudesPendientes;
    
    private PrestamoService prestamoService;
    private AuditoriaService auditoriaService;
    private ObservableList<Prestamo> solicitudes;
    private Prestamo solicitudSeleccionada;
    
    public BandejaSolicitudesController() {
        this.prestamoService = new PrestamoService();
        this.auditoriaService = new AuditoriaService();
        this.solicitudes = FXCollections.observableArrayList();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Configurar tabla
            configurarTabla();
            
            // Configurar combos
            configurarCombos();
            
            // Cargar datos iniciales
            cargarSolicitudes();
            
            // Configurar eventos
            configurarEventos();
            
            logger.info("Bandeja de solicitudes inicializada correctamente");
            
        } catch (Exception e) {
            logger.error("Error al inicializar la bandeja de solicitudes", e);
            mostrarError("Error al inicializar la bandeja de solicitudes");
        }
    }
    
    /**
     * Configura la tabla de solicitudes
     */
    private void configurarTabla() {
        // Configurar columnas
        colId.setCellValueFactory(new PropertyValueFactory<>("idPrestamo"));
        colCliente.setCellValueFactory(cellData -> {
            // TODO: Obtener nombre del cliente desde el servicio
            return new javafx.beans.property.SimpleStringProperty("Cliente " + cellData.getValue().getIdCliente());
        });
        colAsesor.setCellValueFactory(cellData -> {
            // TODO: Obtener nombre del asesor desde el servicio
            return new javafx.beans.property.SimpleStringProperty("Asesor " + cellData.getValue().getIdAsesor());
        });
        colMonto.setCellValueFactory(new PropertyValueFactory<>("montoSolicitado"));
        colTasa.setCellValueFactory(new PropertyValueFactory<>("tasaInteres"));
        colPeriodo.setCellValueFactory(new PropertyValueFactory<>("periodoMeses"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colEtiqueta.setCellValueFactory(new PropertyValueFactory<>("etiqueta"));
        colFecha.setCellValueFactory(cellData -> {
            LocalDate fecha = cellData.getValue().getCreadoEn().toLocalDate();
            return new javafx.beans.property.SimpleStringProperty(fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        });
        colObservacion.setCellValueFactory(new PropertyValueFactory<>("observacion"));
        
        // Configurar selección
        tablaSolicitudes.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                mostrarDetallesSolicitud(newSelection);
            }
        });
    }
    
    /**
     * Configura los combos de filtros
     */
    private void configurarCombos() {
        // Estados
        comboEstado.setItems(FXCollections.observableArrayList(
            "TODOS", "pendiente", "activo", "suspendido", "finalizado", "rechazado"
        ));
        comboEstado.setValue("TODOS");
        
        // Etiquetas
        comboEtiqueta.setItems(FXCollections.observableArrayList(
            "TODAS", "excelente", "deficiente", "peligroso"
        ));
        comboEtiqueta.setValue("TODAS");
        
        // Tipos de pago
        comboTipoPago.setItems(FXCollections.observableArrayList(
            "diario", "semanal", "mensual"
        ));
        comboTipoPago.setValue("diario");
        
        // TODO: Cargar asesores desde la base de datos
        comboAsesor.setItems(FXCollections.observableArrayList("TODOS"));
        comboAsesor.setValue("TODOS");
    }
    
    /**
     * Configura eventos de la interfaz
     */
    private void configurarEventos() {
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
        
        txtMontoMin.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                txtMontoMin.setText(oldVal);
            }
        });
        
        txtMontoMax.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                txtMontoMax.setText(oldVal);
            }
        });
    }
    
    /**
     * Carga las solicitudes desde la base de datos
     */
    private void cargarSolicitudes() {
        try {
            List<Prestamo> prestamos = prestamoService.obtenerPrestamosPendientes();
            solicitudes.clear();
            solicitudes.addAll(prestamos);
            
            tablaSolicitudes.setItems(solicitudes);
            actualizarContadores();
            
            logger.info("Cargadas " + prestamos.size() + " solicitudes");
            
        } catch (Exception e) {
            logger.error("Error al cargar solicitudes", e);
            mostrarError("Error al cargar las solicitudes");
        }
    }
    
    /**
     * Actualiza los contadores de solicitudes
     */
    private void actualizarContadores() {
        int total = solicitudes.size();
        int pendientes = (int) solicitudes.stream()
            .filter(p -> "pendiente".equals(p.getEstado()))
            .count();
        
        lblTotalSolicitudes.setText("Total: " + total + " solicitudes");
        lblSolicitudesPendientes.setText("Pendientes: " + pendientes);
    }
    
    /**
     * Muestra los detalles de la solicitud seleccionada
     */
    private void mostrarDetallesSolicitud(Prestamo prestamo) {
        try {
            solicitudSeleccionada = prestamo;
            
            // Mostrar información básica
            lblCliente.setText("Cliente " + prestamo.getIdCliente());
            lblAsesor.setText("Asesor " + prestamo.getIdAsesor());
            lblMontoSolicitado.setText(String.format("S/ %.2f", prestamo.getMontoSolicitado()));
            
            // Calcular monto desembolsado (90% del solicitado)
            double montoDesembolsado = prestamo.getMontoSolicitado().doubleValue() * 0.9;
            lblMontoDesembolsado.setText(String.format("S/ %.2f", montoDesembolsado));
            
            // Mostrar campos editables
            txtTasaInteres.setText(String.valueOf(prestamo.getTasaInteres()));
            txtPeriodo.setText(String.valueOf(prestamo.getPeriodoMeses()));
            comboTipoPago.setValue(prestamo.getTipoPago().name().toLowerCase());
            txtObservacion.setText(prestamo.getObservacion());
            
            // Habilitar botones según el estado
            boolean esPendiente = "pendiente".equals(prestamo.getEstado());
            btnAprobar.setDisable(!esPendiente);
            btnRechazar.setDisable(!esPendiente);
            btnGuardar.setDisable(!esPendiente);
            
        } catch (Exception e) {
            logger.error("Error al mostrar detalles de la solicitud", e);
        }
    }
    
    /**
     * Maneja el filtrado de solicitudes
     */
    @FXML
    private void handleFiltrar(ActionEvent event) {
        try {
            // TODO: Implementar filtrado
            mostrarInfo("Funcionalidad de filtrado en desarrollo");
            
        } catch (Exception e) {
            logger.error("Error al filtrar solicitudes", e);
            mostrarError("Error al filtrar las solicitudes");
        }
    }
    
    /**
     * Limpia los filtros aplicados
     */
    @FXML
    private void handleLimpiarFiltros(ActionEvent event) {
        try {
            comboAsesor.setValue("TODOS");
            comboEstado.setValue("TODOS");
            comboEtiqueta.setValue("TODAS");
            txtMontoMin.clear();
            txtMontoMax.clear();
            
            cargarSolicitudes();
            
        } catch (Exception e) {
            logger.error("Error al limpiar filtros", e);
            mostrarError("Error al limpiar los filtros");
        }
    }
    
    /**
     * Maneja la aprobación de una solicitud
     */
    @FXML
    private void handleAprobar(ActionEvent event) {
        if (solicitudSeleccionada == null) {
            mostrarError("Seleccione una solicitud para aprobar");
            return;
        }
        
        try {
            // Validar campos obligatorios
            if (txtTasaInteres.getText().trim().isEmpty() || txtPeriodo.getText().trim().isEmpty()) {
                mostrarError("Debe completar la tasa de interés y el período");
                return;
            }
            
            double tasaInteres = Double.parseDouble(txtTasaInteres.getText());
            int periodo = Integer.parseInt(txtPeriodo.getText());
            
            if (tasaInteres < 0 || tasaInteres > 30) {
                mostrarError("La tasa de interés debe estar entre 0% y 30%");
                return;
            }
            
            if (periodo < 1 || periodo > 12) {
                mostrarError("El período debe estar entre 1 y 12 meses");
                return;
            }
            
            // Actualizar la solicitud
            solicitudSeleccionada.setTasaInteres(new java.math.BigDecimal(tasaInteres));
            solicitudSeleccionada.setPeriodoMeses(periodo);
            solicitudSeleccionada.setTipoPago(Prestamo.TipoPago.valueOf(comboTipoPago.getValue().toUpperCase()));
            solicitudSeleccionada.setObservacion(txtObservacion.getText());
            
            // Aprobar y generar cronograma
            prestamoService.aprobarPrestamo(solicitudSeleccionada.getIdPrestamo(), 
                new java.math.BigDecimal(tasaInteres), periodo, 
                Prestamo.TipoPago.valueOf(comboTipoPago.getValue().toUpperCase()), 
                java.time.LocalDate.now().plusDays(1));
            
            // Registrar auditoría
            auditoriaService.registrarAuditoria(
                solicitudSeleccionada.getIdPrestamo().toString(),
                "prestamos",
                "update",
                "estado: pendiente",
                "estado: activo"
            );
            
            mostrarInfo("Préstamo aprobado y cronograma generado correctamente");
            cargarSolicitudes();
            
        } catch (Exception e) {
            logger.error("Error al aprobar solicitud", e);
            mostrarError("Error al aprobar la solicitud: " + e.getMessage());
        }
    }
    
    /**
     * Maneja el rechazo de una solicitud
     */
    @FXML
    private void handleRechazar(ActionEvent event) {
        if (solicitudSeleccionada == null) {
            mostrarError("Seleccione una solicitud para rechazar");
            return;
        }
        
        try {
            // Mostrar diálogo para motivo de rechazo
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Rechazar Solicitud");
            dialog.setHeaderText("Motivo del rechazo");
            dialog.setContentText("Ingrese el motivo del rechazo:");
            
            dialog.showAndWait().ifPresent(motivo -> {
                try {
                        // Rechazar la solicitud
                        prestamoService.rechazarPrestamo(solicitudSeleccionada.getIdPrestamo(), motivo);
                    
                    // Registrar auditoría
                    auditoriaService.registrarAuditoria(
                        solicitudSeleccionada.getIdPrestamo().toString(),
                        "prestamos",
                        "update",
                        "estado: pendiente",
                        "estado: rechazado - motivo: " + motivo
                    );
                    
                    mostrarInfo("Solicitud rechazada correctamente");
                    cargarSolicitudes();
                    
                } catch (Exception e) {
                    logger.error("Error al rechazar solicitud", e);
                    mostrarError("Error al rechazar la solicitud: " + e.getMessage());
                }
            });
            
        } catch (Exception e) {
            logger.error("Error al rechazar solicitud", e);
            mostrarError("Error al rechazar la solicitud");
        }
    }
    
    /**
     * Maneja el guardado de cambios en una solicitud
     */
    @FXML
    private void handleGuardar(ActionEvent event) {
        if (solicitudSeleccionada == null) {
            mostrarError("Seleccione una solicitud para editar");
            return;
        }
        
        try {
            // Validar campos obligatorios
            if (txtTasaInteres.getText().trim().isEmpty() || txtPeriodo.getText().trim().isEmpty()) {
                mostrarError("Debe completar la tasa de interés y el período");
                return;
            }
            
            double tasaInteres = Double.parseDouble(txtTasaInteres.getText());
            int periodo = Integer.parseInt(txtPeriodo.getText());
            
            if (tasaInteres < 0 || tasaInteres > 30) {
                mostrarError("La tasa de interés debe estar entre 0% y 30%");
                return;
            }
            
            if (periodo < 1 || periodo > 12) {
                mostrarError("El período debe estar entre 1 y 12 meses");
                return;
            }
            
            // Actualizar la solicitud
            solicitudSeleccionada.setTasaInteres(new java.math.BigDecimal(tasaInteres));
            solicitudSeleccionada.setPeriodoMeses(periodo);
            solicitudSeleccionada.setTipoPago(Prestamo.TipoPago.valueOf(comboTipoPago.getValue().toUpperCase()));
            solicitudSeleccionada.setObservacion(txtObservacion.getText());
            
            // Guardar cambios
            prestamoService.actualizarPrestamo(solicitudSeleccionada);
            
            // Registrar auditoría
            auditoriaService.registrarAuditoria(
                solicitudSeleccionada.getIdPrestamo().toString(),
                "prestamos",
                "update",
                "tasa: " + solicitudSeleccionada.getTasaInteres() + ", periodo: " + solicitudSeleccionada.getPeriodoMeses(),
                "tasa: " + tasaInteres + ", periodo: " + periodo
            );
            
            mostrarInfo("Cambios guardados correctamente");
            cargarSolicitudes();
            
        } catch (Exception e) {
            logger.error("Error al guardar cambios", e);
            mostrarError("Error al guardar los cambios: " + e.getMessage());
        }
    }
    
    /**
     * Maneja la visualización del historial del cliente
     */
    @FXML
    private void handleVerHistorial(ActionEvent event) {
        if (solicitudSeleccionada == null) {
            mostrarError("Seleccione una solicitud para ver el historial");
            return;
        }
        
        try {
            // TODO: Implementar visualización del historial del cliente
            mostrarInfo("Funcionalidad de historial del cliente en desarrollo");
            
        } catch (Exception e) {
            logger.error("Error al mostrar historial del cliente", e);
            mostrarError("Error al mostrar el historial del cliente");
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
