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
import pe.crediactiva.app.model.Cronograma;
import pe.crediactiva.app.model.Prestamo;
import pe.crediactiva.app.model.RecaudacionAsesor;
import pe.crediactiva.app.service.PrestamoService;
import pe.crediactiva.app.service.RecaudacionService;
import pe.crediactiva.app.service.AuditoriaService;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controlador para la administración de pagos y cierre del día
 */
public class AdministrarPagosController implements Initializable {
    
    private static final Logger logger = LoggerFactory.getLogger(AdministrarPagosController.class);
    
    @FXML
    private ComboBox<String> comboAsesor;
    
    @FXML
    private DatePicker dateFecha;
    
    @FXML
    private Button btnFiltrar;
    
    @FXML
    private Button btnLimpiarFiltros;
    
    @FXML
    private TableView<RecaudacionAsesor> tablaBorradores;
    
    @FXML
    private TableColumn<RecaudacionAsesor, Long> colBorradorId;
    
    @FXML
    private TableColumn<RecaudacionAsesor, String> colBorradorAsesor;
    
    @FXML
    private TableColumn<RecaudacionAsesor, String> colBorradorCliente;
    
    @FXML
    private TableColumn<RecaudacionAsesor, Long> colBorradorPrestamo;
    
    @FXML
    private TableColumn<RecaudacionAsesor, Double> colBorradorMonto;
    
    @FXML
    private TableColumn<RecaudacionAsesor, String> colBorradorFecha;
    
    @FXML
    private TableColumn<RecaudacionAsesor, Boolean> colBorradorValidado;
    
    @FXML
    private Button btnValidarBorrador;
    
    @FXML
    private Button btnValidarTodos;
    
    @FXML
    private Button btnEliminarBorrador;
    
    @FXML
    private Label lblCliente;
    
    @FXML
    private Label lblPrestamo;
    
    @FXML
    private Label lblMontoPagado;
    
    @FXML
    private Label lblSaldoPendiente;
    
    @FXML
    private TableView<Cronograma> tablaCuotas;
    
    @FXML
    private TableColumn<Cronograma, Integer> colCuotaNumero;
    
    @FXML
    private TableColumn<Cronograma, String> colCuotaFecha;
    
    @FXML
    private TableColumn<Cronograma, Double> colCuotaMonto;
    
    @FXML
    private TableColumn<Cronograma, String> colCuotaEstado;
    
    @FXML
    private TableColumn<Cronograma, Boolean> colCuotaAplicar;
    
    @FXML
    private Button btnAplicarPagos;
    
    @FXML
    private Button btnAplicarVencidas;
    
    @FXML
    private Button btnLimpiarSeleccion;
    
    @FXML
    private Label lblTotalBorradores;
    
    @FXML
    private Label lblTotalMonto;
    
    @FXML
    private Label lblBorradoresValidados;
    
    @FXML
    private Label lblBorradoresPendientes;
    
    @FXML
    private Button btnGenerarReporte;
    
    @FXML
    private Button btnCerrarDia;
    
    private PrestamoService prestamoService;
    private RecaudacionService recaudacionService;
    private AuditoriaService auditoriaService;
    private ObservableList<RecaudacionAsesor> borradores;
    private ObservableList<Cronograma> cuotas;
    private RecaudacionAsesor borradorSeleccionado;
    private Prestamo prestamoSeleccionado;
    
    public AdministrarPagosController() {
        this.prestamoService = new PrestamoService();
        this.recaudacionService = new RecaudacionService();
        this.auditoriaService = new AuditoriaService();
        this.borradores = FXCollections.observableArrayList();
        this.cuotas = FXCollections.observableArrayList();
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            // Configurar tabla de borradores
            configurarTablaBorradores();
            
            // Configurar tabla de cuotas
            configurarTablaCuotas();
            
            // Configurar combos
            configurarCombos();
            
            // Configurar eventos
            configurarEventos();
            
            // Configurar fecha por defecto
            dateFecha.setValue(LocalDate.now());
            
            // Cargar datos iniciales
            cargarBorradores();
            
            logger.info("Administración de pagos inicializada correctamente");
            
        } catch (Exception e) {
            logger.error("Error al inicializar la administración de pagos", e);
            mostrarError("Error al inicializar la administración de pagos");
        }
    }
    
    /**
     * Configura la tabla de borradores
     */
    private void configurarTablaBorradores() {
        colBorradorId.setCellValueFactory(new PropertyValueFactory<>("idRecaudacion"));
        colBorradorAsesor.setCellValueFactory(cellData -> {
            // TODO: Obtener nombre del asesor desde el servicio
            return new javafx.beans.property.SimpleStringProperty("Asesor " + cellData.getValue().getIdAsesor());
        });
        colBorradorCliente.setCellValueFactory(cellData -> {
            // TODO: Obtener nombre del cliente desde el servicio
            return new javafx.beans.property.SimpleStringProperty("Cliente " + cellData.getValue().getIdCliente());
        });
        colBorradorPrestamo.setCellValueFactory(new PropertyValueFactory<>("idPrestamo"));
        colBorradorMonto.setCellValueFactory(new PropertyValueFactory<>("montoRegistrado"));
        colBorradorFecha.setCellValueFactory(cellData -> {
            LocalDate fecha = cellData.getValue().getFechaRegistro().toLocalDate();
            return new javafx.beans.property.SimpleStringProperty(fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        });
        colBorradorValidado.setCellValueFactory(new PropertyValueFactory<>("validado"));
        
        // Configurar selección
        tablaBorradores.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                mostrarDetallesBorrador(newSelection);
            }
        });
    }
    
    /**
     * Configura la tabla de cuotas
     */
    private void configurarTablaCuotas() {
        colCuotaNumero.setCellValueFactory(new PropertyValueFactory<>("numeroCuota"));
        colCuotaFecha.setCellValueFactory(cellData -> {
            LocalDate fecha = cellData.getValue().getFechaProgramada();
            return new javafx.beans.property.SimpleStringProperty(fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        });
        colCuotaMonto.setCellValueFactory(new PropertyValueFactory<>("montoCuota"));
        colCuotaEstado.setCellValueFactory(new PropertyValueFactory<>("estadoCuota"));
        colCuotaAplicar.setCellValueFactory(new PropertyValueFactory<>("seleccionado"));
        
        // Configurar checkboxes para selección
        colCuotaAplicar.setCellFactory(column -> new TableCell<Cronograma, Boolean>() {
            private final CheckBox checkBox = new CheckBox();
            
            {
                checkBox.setOnAction(event -> {
                    Cronograma cuota = getTableView().getItems().get(getIndex());
                    cuota.setSeleccionado(checkBox.isSelected());
                });
            }
            
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    checkBox.setSelected(item);
                    setGraphic(checkBox);
                }
            }
        });
        
        tablaCuotas.setItems(cuotas);
    }
    
    /**
     * Configura los combos
     */
    private void configurarCombos() {
        // TODO: Cargar asesores desde la base de datos
        comboAsesor.setItems(FXCollections.observableArrayList("TODOS", "Asesor 1", "Asesor 2", "Asesor 3"));
        comboAsesor.setValue("TODOS");
    }
    
    /**
     * Configura eventos de la interfaz
     */
    private void configurarEventos() {
        // Cambios en fecha
        dateFecha.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                cargarBorradores();
            }
        });
    }
    
    /**
     * Carga los borradores desde la base de datos
     */
    private void cargarBorradores() {
        try {
            LocalDate fecha = dateFecha.getValue() != null ? dateFecha.getValue() : LocalDate.now();
            List<RecaudacionAsesor> recaudaciones = recaudacionService.obtenerBorradoresPorFecha(fecha);
            
            borradores.clear();
            borradores.addAll(recaudaciones);
            
            tablaBorradores.setItems(borradores);
            actualizarResumen();
            
            logger.info("Cargados " + recaudaciones.size() + " borradores para la fecha " + fecha);
            
        } catch (Exception e) {
            logger.error("Error al cargar borradores", e);
            mostrarError("Error al cargar los borradores");
        }
    }
    
    /**
     * Actualiza el resumen del día
     */
    private void actualizarResumen() {
        int total = borradores.size();
        double totalMonto = borradores.stream().mapToDouble(r -> r.getMontoRegistrado().doubleValue()).sum();
        int validados = (int) borradores.stream().filter(RecaudacionAsesor::isValidado).count();
        int pendientes = total - validados;
        
        lblTotalBorradores.setText("Total Borradores: " + total);
        lblTotalMonto.setText("Total Monto: S/ " + String.format("%.2f", totalMonto));
        lblBorradoresValidados.setText("Validados: " + validados);
        lblBorradoresPendientes.setText("Pendientes: " + pendientes);
    }
    
    /**
     * Muestra los detalles del borrador seleccionado
     */
    private void mostrarDetallesBorrador(RecaudacionAsesor borrador) {
        try {
            borradorSeleccionado = borrador;
            
            // Mostrar información básica
            lblCliente.setText("Cliente " + borrador.getIdCliente());
            lblPrestamo.setText("Préstamo " + borrador.getIdPrestamo());
            lblMontoPagado.setText("S/ " + String.format("%.2f", borrador.getMontoRegistrado()));
            
            // Obtener préstamo y cuotas
            prestamoSeleccionado = prestamoService.obtenerPrestamoPorId(borrador.getIdPrestamo());
            if (prestamoSeleccionado != null) {
                List<Cronograma> cuotasPrestamo = prestamoService.obtenerCuotasPorPrestamo(borrador.getIdPrestamo());
                cuotas.clear();
                cuotas.addAll(cuotasPrestamo);
                
                // Calcular saldo pendiente
                double saldoPendiente = cuotasPrestamo.stream()
                    .filter(c -> "pendiente".equals(c.getEstadoCuota().name().toLowerCase()))
                    .mapToDouble(c -> c.getMontoCuota().doubleValue())
                    .sum();
                lblSaldoPendiente.setText("S/ " + String.format("%.2f", saldoPendiente));
            }
            
            // Habilitar botones según el estado
            boolean esValido = borrador.isValidado();
            btnValidarBorrador.setDisable(esValido);
            btnEliminarBorrador.setDisable(esValido);
            
        } catch (Exception e) {
            logger.error("Error al mostrar detalles del borrador", e);
        }
    }
    
    /**
     * Maneja el filtrado de borradores
     */
    @FXML
    private void handleFiltrar(ActionEvent event) {
        try {
            // TODO: Implementar filtrado por asesor
            cargarBorradores();
            
        } catch (Exception e) {
            logger.error("Error al filtrar borradores", e);
            mostrarError("Error al filtrar los borradores");
        }
    }
    
    /**
     * Limpia los filtros aplicados
     */
    @FXML
    private void handleLimpiarFiltros(ActionEvent event) {
        try {
            comboAsesor.setValue("TODOS");
            dateFecha.setValue(LocalDate.now());
            
            cargarBorradores();
            
        } catch (Exception e) {
            logger.error("Error al limpiar filtros", e);
            mostrarError("Error al limpiar los filtros");
        }
    }
    
    /**
     * Maneja la validación de un borrador
     */
    @FXML
    private void handleValidarBorrador(ActionEvent event) {
        if (borradorSeleccionado == null) {
            mostrarError("Seleccione un borrador para validar");
            return;
        }
        
        try {
            // Validar el borrador
            recaudacionService.validarBorrador(borradorSeleccionado);
            
            // Registrar auditoría
            auditoriaService.registrarAuditoria(
                borradorSeleccionado.getIdRecaudacion().toString(),
                "recaudacion_asesor",
                "update",
                "validado: false",
                "validado: true"
            );
            
            mostrarInfo("Borrador validado correctamente");
            cargarBorradores();
            
        } catch (Exception e) {
            logger.error("Error al validar borrador", e);
            mostrarError("Error al validar el borrador: " + e.getMessage());
        }
    }
    
    /**
     * Maneja la validación de todos los borradores
     */
    @FXML
    private void handleValidarTodos(ActionEvent event) {
        try {
            // Mostrar diálogo de confirmación
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Validación");
            alert.setHeaderText("Validar todos los borradores");
            alert.setContentText("¿Está seguro de que desea validar todos los borradores del día?");
            
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        // Validar todos los borradores
                        boolean todosValidados = recaudacionService.validarTodosLosBorradores(dateFecha.getValue());
                        int validados = todosValidados ? borradores.size() : 0;
                        
                        // Registrar auditoría
                        auditoriaService.registrarAuditoria(
                            "MULTIPLE",
                            "recaudacion_asesor",
                            "update",
                            "validado: false",
                            "validado: true - cantidad: " + validados
                        );
                        
                        mostrarInfo("Se validaron " + validados + " borradores correctamente");
                        cargarBorradores();
                        
                    } catch (Exception e) {
                        logger.error("Error al validar todos los borradores", e);
                        mostrarError("Error al validar los borradores: " + e.getMessage());
                    }
                }
            });
            
        } catch (Exception e) {
            logger.error("Error al validar todos los borradores", e);
            mostrarError("Error al validar los borradores");
        }
    }
    
    /**
     * Maneja la eliminación de un borrador
     */
    @FXML
    private void handleEliminarBorrador(ActionEvent event) {
        if (borradorSeleccionado == null) {
            mostrarError("Seleccione un borrador para eliminar");
            return;
        }
        
        try {
            // Mostrar diálogo de confirmación
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Eliminación");
            alert.setHeaderText("Eliminar borrador");
            alert.setContentText("¿Está seguro de que desea eliminar este borrador?");
            
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        // Eliminar el borrador
                        recaudacionService.eliminarBorrador(borradorSeleccionado);
                        
                        // Registrar auditoría
                        auditoriaService.registrarAuditoria(
                            borradorSeleccionado.getIdRecaudacion().toString(),
                            "recaudacion_asesor",
                            "delete",
                            "borrador eliminado",
                            null
                        );
                        
                        mostrarInfo("Borrador eliminado correctamente");
                        cargarBorradores();
                        
                    } catch (Exception e) {
                        logger.error("Error al eliminar borrador", e);
                        mostrarError("Error al eliminar el borrador: " + e.getMessage());
                    }
                }
            });
            
        } catch (Exception e) {
            logger.error("Error al eliminar borrador", e);
            mostrarError("Error al eliminar el borrador");
        }
    }
    
    /**
     * Maneja la aplicación de pagos a cuotas
     */
    @FXML
    private void handleAplicarPagos(ActionEvent event) {
        if (borradorSeleccionado == null) {
            mostrarError("Seleccione un borrador para aplicar pagos");
            return;
        }
        
        try {
            // Obtener cuotas seleccionadas
            List<Cronograma> cuotasSeleccionadas = cuotas.stream()
                .filter(c -> c.isSeleccionado())
                .toList();
            
            if (cuotasSeleccionadas.isEmpty()) {
                mostrarError("Seleccione al menos una cuota para aplicar el pago");
                return;
            }
            
            // Aplicar pagos
            recaudacionService.aplicarPagosACuotas(borradorSeleccionado, cuotasSeleccionadas);
            
            // Registrar auditoría
            auditoriaService.registrarAuditoria(
                borradorSeleccionado.getIdRecaudacion().toString(),
                "pagos",
                "insert",
                null,
                "pagos aplicados a cuotas: " + cuotasSeleccionadas.size()
            );
            
            mostrarInfo("Pagos aplicados correctamente a " + cuotasSeleccionadas.size() + " cuotas");
            cargarBorradores();
            
        } catch (Exception e) {
            logger.error("Error al aplicar pagos", e);
            mostrarError("Error al aplicar los pagos: " + e.getMessage());
        }
    }
    
    /**
     * Maneja la aplicación de pagos a cuotas vencidas
     */
    @FXML
    private void handleAplicarVencidas(ActionEvent event) {
        if (borradorSeleccionado == null) {
            mostrarError("Seleccione un borrador para aplicar pagos");
            return;
        }
        
        try {
            // Obtener cuotas vencidas
            List<Cronograma> cuotasVencidas = cuotas.stream()
                .filter(c -> "retrasada".equals(c.getEstadoCuota().name().toLowerCase()))
                .toList();
            
            if (cuotasVencidas.isEmpty()) {
                mostrarInfo("No hay cuotas vencidas para aplicar el pago");
                return;
            }
            
            // Aplicar pagos a cuotas vencidas
            recaudacionService.aplicarPagosACuotas(borradorSeleccionado, cuotasVencidas);
            
            // Registrar auditoría
            auditoriaService.registrarAuditoria(
                borradorSeleccionado.getIdRecaudacion().toString(),
                "pagos",
                "insert",
                null,
                "pagos aplicados a cuotas vencidas: " + cuotasVencidas.size()
            );
            
            mostrarInfo("Pagos aplicados correctamente a " + cuotasVencidas.size() + " cuotas vencidas");
            cargarBorradores();
            
        } catch (Exception e) {
            logger.error("Error al aplicar pagos a cuotas vencidas", e);
            mostrarError("Error al aplicar los pagos: " + e.getMessage());
        }
    }
    
    /**
     * Limpia la selección de cuotas
     */
    @FXML
    private void handleLimpiarSeleccion(ActionEvent event) {
        cuotas.forEach(cuota -> cuota.setSeleccionado(false));
        tablaCuotas.refresh();
    }
    
    /**
     * Maneja la generación de reporte
     */
    @FXML
    private void handleGenerarReporte(ActionEvent event) {
        try {
            // TODO: Implementar generación de reporte
            mostrarInfo("Funcionalidad de generación de reporte en desarrollo");
            
        } catch (Exception e) {
            logger.error("Error al generar reporte", e);
            mostrarError("Error al generar el reporte");
        }
    }
    
    /**
     * Maneja el cierre del día
     */
    @FXML
    private void handleCerrarDia(ActionEvent event) {
        try {
            // Mostrar diálogo de confirmación
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmar Cierre");
            alert.setHeaderText("Cerrar día");
            alert.setContentText("¿Está seguro de que desea cerrar el día? Esta acción no se puede deshacer.");
            
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    try {
                        // Cerrar el día
                        recaudacionService.cerrarDia(dateFecha.getValue());
                        
                        // Registrar auditoría
                        auditoriaService.registrarAuditoria(
                            "SISTEMA",
                            "recaudacion_asesor",
                            "update",
                            "día abierto",
                            "día cerrado: " + dateFecha.getValue()
                        );
                        
                        mostrarInfo("Día cerrado correctamente");
                        cargarBorradores();
                        
                    } catch (Exception e) {
                        logger.error("Error al cerrar día", e);
                        mostrarError("Error al cerrar el día: " + e.getMessage());
                    }
                }
            });
            
        } catch (Exception e) {
            logger.error("Error al cerrar día", e);
            mostrarError("Error al cerrar el día");
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
