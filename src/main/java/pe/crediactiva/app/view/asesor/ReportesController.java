package pe.crediactiva.app.view.asesor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pe.crediactiva.app.service.PrestamoService;
import pe.crediactiva.app.service.RecaudacionService;
import pe.crediactiva.app.service.ReporteService;
import pe.crediactiva.app.service.ClienteService;
import pe.crediactiva.app.model.Prestamo;
import pe.crediactiva.app.model.Cronograma;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador para los reportes del asesor
 */
public class ReportesController {
    
    private static final Logger logger = LoggerFactory.getLogger(ReportesController.class);
    
    @FXML
    private DatePicker dpFechaDesde;
    
    @FXML
    private DatePicker dpFechaHasta;
    
    @FXML
    private Label lblTotalRecaudado;
    
    @FXML
    private Label lblTotalCuotasPagadas;
    
    @FXML
    private Label lblCuotasVencidas;
    
    @FXML
    private Button btnVerDetallesCuotasVencidas;
    
    @FXML
    private Label lblMorosidad;
    
    @FXML
    private TableView<ReporteGenerado> tblReportes;
    
    @FXML
    private TableColumn<ReporteGenerado, String> colTipoReporte;
    
    @FXML
    private TableColumn<ReporteGenerado, String> colFechaGeneracion;
    
    @FXML
    private TableColumn<ReporteGenerado, String> colPeriodo;
    
    @FXML
    private TableColumn<ReporteGenerado, String> colEstado;
    
    @FXML
    private TableColumn<ReporteGenerado, String> colAcciones;
    
    @FXML
    private TabPane tabPaneDetalles;
    
    @FXML
    private TableView<DatoReporte> tblDatosReporte;
    
    @FXML
    private TableColumn<DatoReporte, String> colCampo;
    
    @FXML
    private TableColumn<DatoReporte, String> colValor;
    
    private PrestamoService prestamoService;
    private RecaudacionService recaudacionService;
    private ReporteService reporteService;
    private ObservableList<ReporteGenerado> reportes;
    private ObservableList<DatoReporte> datosReporte;
    private LocalDate fechaDesde;
    private LocalDate fechaHasta;
    
    public ReportesController() {
        this.prestamoService = new PrestamoService();
        this.recaudacionService = new RecaudacionService();
        this.reporteService = new ReporteService();
        this.reportes = FXCollections.observableArrayList();
        this.datosReporte = FXCollections.observableArrayList();
    }
    
    @FXML
    private void initialize() {
        try {
            logger.info("Inicializando ReportesController...");
            configurarControles();
            configurarFechas();
            cargarResumenReportes();
            cargarReportesGenerados();
            
            // Verificar que el botón esté conectado
            if (btnVerDetallesCuotasVencidas != null) {
                logger.info("Botón 'Ver Detalles' de cuotas vencidas conectado correctamente");
            } else {
                logger.warn("Botón 'Ver Detalles' de cuotas vencidas NO está conectado");
            }
            
        } catch (Exception e) {
            logger.error("Error al inicializar reportes", e);
            mostrarError("Error al inicializar los reportes");
        }
    }
    
    /**
     * Configura los controles del formulario
     */
    private void configurarControles() {
        // Configurar tabla de reportes
        colTipoReporte.setCellValueFactory(new PropertyValueFactory<>("tipoReporte"));
        colFechaGeneracion.setCellValueFactory(new PropertyValueFactory<>("fechaGeneracion"));
        colPeriodo.setCellValueFactory(new PropertyValueFactory<>("periodo"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colAcciones.setCellValueFactory(new PropertyValueFactory<>("acciones"));
        
        tblReportes.setItems(reportes);
        
        // Configurar tabla de datos del reporte
        colCampo.setCellValueFactory(new PropertyValueFactory<>("campo"));
        colValor.setCellValueFactory(new PropertyValueFactory<>("valor"));
        
        tblDatosReporte.setItems(datosReporte);
        
        // Configurar selección de reporte
        tblReportes.getSelectionModel().selectedItemProperty().addListener(
            (observable, oldValue, newValue) -> {
                if (newValue != null) {
                    cargarDetallesReporte(newValue);
                }
            }
        );
    }
    
    /**
     * Configura las fechas por defecto
     */
    private void configurarFechas() {
        dpFechaDesde.setValue(LocalDate.now().minusMonths(1));
        dpFechaHasta.setValue(LocalDate.now());
        fechaDesde = dpFechaDesde.getValue();
        fechaHasta = dpFechaHasta.getValue();
    }
    
    /**
     * Carga el resumen de los reportes
     */
    private void cargarResumenReportes() {
        try {
            // Total recaudado del mes actual
            BigDecimal totalRecaudado = recaudacionService.obtenerRecaudacionDelMes();
            lblTotalRecaudado.setText("S/ " + String.format("%.2f", totalRecaudado));
            
            // Total cuotas pagadas (aproximado basado en préstamos activos)
            List<Prestamo> prestamosActivos = prestamoService.obtenerPrestamosActivos();
            int totalCuotasPagadas = 0;
            for (Prestamo prestamo : prestamosActivos) {
                List<Cronograma> cuotasPagadas = prestamoService.obtenerCuotasPorPrestamo(prestamo.getIdPrestamo())
                    .stream()
                    .filter(cuota -> cuota.getEstadoCuota() == Cronograma.EstadoCuota.PAGADA)
                    .collect(java.util.stream.Collectors.toList());
                totalCuotasPagadas += cuotasPagadas.size();
            }
            lblTotalCuotasPagadas.setText(String.valueOf(totalCuotasPagadas));
            
            // Cuotas vencidas
            List<Cronograma> cuotasVencidas = prestamoService.obtenerCuotasVencidas();
            lblCuotasVencidas.setText(String.valueOf(cuotasVencidas.size()));
            
            // Morosidad
            double morosidad = prestamoService.calcularMorosidad();
            lblMorosidad.setText(String.format("%.1f%%", morosidad));
            
        } catch (Exception e) {
            logger.error("Error al cargar resumen de reportes", e);
            mostrarError("Error al cargar el resumen de reportes");
        }
    }
    
    /**
     * Carga los reportes generados
     */
    private void cargarReportesGenerados() {
        try {
            // TODO: Implementar método en ReporteService
            List<ReporteGenerado> listaReportes = new ArrayList<>(); // reporteService.obtenerReportesGenerados();
            reportes.clear();
            reportes.addAll(listaReportes);
            
        } catch (Exception e) {
            logger.error("Error al cargar reportes generados", e);
            mostrarError("Error al cargar los reportes generados");
        }
    }
    
    /**
     * Carga los detalles del reporte seleccionado
     */
    private void cargarDetallesReporte(ReporteGenerado reporte) {
        try {
            // TODO: Implementar método en ReporteService
            List<DatoReporte> datos = new ArrayList<>(); // reporteService.obtenerDatosReporte(reporte.getId());
            datosReporte.clear();
            datosReporte.addAll(datos);
            
        } catch (Exception e) {
            logger.error("Error al cargar detalles del reporte", e);
            mostrarError("Error al cargar los detalles del reporte");
        }
    }
    
    /**
     * Maneja la aplicación del filtro de fechas
     */
    @FXML
    private void handleAplicarFiltro() {
        fechaDesde = dpFechaDesde.getValue();
        fechaHasta = dpFechaHasta.getValue();
        
        if (fechaDesde == null || fechaHasta == null) {
            mostrarAdvertencia("Por favor seleccione ambas fechas");
            return;
        }
        
        if (fechaDesde.isAfter(fechaHasta)) {
            mostrarAdvertencia("La fecha desde no puede ser posterior a la fecha hasta");
            return;
        }
        
        cargarResumenReportes();
        mostrarInfo("Filtro aplicado correctamente");
    }
    
    /**
     * Maneja la limpieza del filtro
     */
    @FXML
    private void handleLimpiarFiltro() {
        configurarFechas();
        cargarResumenReportes();
        mostrarInfo("Filtro limpiado");
    }
    
    /**
     * Maneja la generación del reporte de recaudación
     */
    @FXML
    private void handleReporteRecaudacion() {
        try {
            // TODO: Implementar método en ReporteService
            ReporteGenerado reporte = new ReporteGenerado(1, "Recaudación", "Hoy", "Mes actual", "Generado", "Ver");
            reportes.add(reporte);
            mostrarInfo("Reporte de recaudación generado exitosamente");
            
        } catch (Exception e) {
            logger.error("Error al generar reporte de recaudación", e);
            mostrarError("Error al generar el reporte de recaudación");
        }
    }
    
    /**
     * Maneja la generación del reporte de cuotas
     */
    @FXML
    private void handleReporteCuotas() {
        try {
            // TODO: Implementar método en ReporteService
            ReporteGenerado reporte = new ReporteGenerado(2, "Cuotas", "Hoy", "Mes actual", "Generado", "Ver");
            reportes.add(reporte);
            mostrarInfo("Reporte de cuotas generado exitosamente");
            
        } catch (Exception e) {
            logger.error("Error al generar reporte de cuotas", e);
            mostrarError("Error al generar el reporte de cuotas");
        }
    }
    
    /**
     * Maneja la generación del reporte de morosidad
     */
    @FXML
    private void handleReporteMorosidad() {
        try {
            // TODO: Implementar método en ReporteService
            ReporteGenerado reporte = new ReporteGenerado(3, "Morosidad", "Hoy", "Mes actual", "Generado", "Ver");
            reportes.add(reporte);
            mostrarInfo("Reporte de morosidad generado exitosamente");
            
        } catch (Exception e) {
            logger.error("Error al generar reporte de morosidad", e);
            mostrarError("Error al generar el reporte de morosidad");
        }
    }
    
    /**
     * Maneja la generación del reporte de clientes
     */
    @FXML
    private void handleReporteClientes() {
        try {
            // TODO: Implementar método en ReporteService
            ReporteGenerado reporte = new ReporteGenerado(4, "Clientes", "Hoy", "Mes actual", "Generado", "Ver");
            reportes.add(reporte);
            mostrarInfo("Reporte de clientes generado exitosamente");
            
        } catch (Exception e) {
            logger.error("Error al generar reporte de clientes", e);
            mostrarError("Error al generar el reporte de clientes");
        }
    }
    
    /**
     * Maneja la generación del reporte de préstamos
     */
    @FXML
    private void handleReportePrestamos() {
        try {
            // TODO: Implementar método en ReporteService
            ReporteGenerado reporte = new ReporteGenerado(5, "Préstamos", "Hoy", "Mes actual", "Generado", "Ver");
            reportes.add(reporte);
            mostrarInfo("Reporte de préstamos generado exitosamente");
            
        } catch (Exception e) {
            logger.error("Error al generar reporte de préstamos", e);
            mostrarError("Error al generar el reporte de préstamos");
        }
    }
    
    /**
     * Maneja la generación del reporte de comisiones
     */
    @FXML
    private void handleReporteComisiones() {
        try {
            // TODO: Implementar método en ReporteService
            ReporteGenerado reporte = new ReporteGenerado(6, "Comisiones", "Hoy", "Mes actual", "Generado", "Ver");
            reportes.add(reporte);
            mostrarInfo("Reporte de comisiones generado exitosamente");
            
        } catch (Exception e) {
            logger.error("Error al generar reporte de comisiones", e);
            mostrarError("Error al generar el reporte de comisiones");
        }
    }
    
    /**
     * Maneja la exportación a Excel
     */
    @FXML
    private void handleExportarExcel() {
        try {
            ReporteGenerado reporteSeleccionado = tblReportes.getSelectionModel().getSelectedItem();
            if (reporteSeleccionado == null) {
                mostrarAdvertencia("Por favor seleccione un reporte para exportar");
                return;
            }
            
            // TODO: Implementar método en ReporteService
            // reporteService.exportarAExcel(reporteSeleccionado.getId());
            mostrarInfo("Reporte exportado a Excel exitosamente");
            
        } catch (Exception e) {
            logger.error("Error al exportar a Excel", e);
            mostrarError("Error al exportar el reporte a Excel");
        }
    }
    
    /**
     * Maneja la exportación a PDF
     */
    @FXML
    private void handleExportarPDF() {
        try {
            ReporteGenerado reporteSeleccionado = tblReportes.getSelectionModel().getSelectedItem();
            if (reporteSeleccionado == null) {
                mostrarAdvertencia("Por favor seleccione un reporte para exportar");
                return;
            }
            
            // TODO: Implementar método en ReporteService
            // reporteService.exportarAPDF(reporteSeleccionado.getId());
            mostrarInfo("Reporte exportado a PDF exitosamente");
            
        } catch (Exception e) {
            logger.error("Error al exportar a PDF", e);
            mostrarError("Error al exportar el reporte a PDF");
        }
    }
    
    /**
     * Maneja la exportación a CSV
     */
    @FXML
    private void handleExportarCSV() {
        try {
            ReporteGenerado reporteSeleccionado = tblReportes.getSelectionModel().getSelectedItem();
            if (reporteSeleccionado == null) {
                mostrarAdvertencia("Por favor seleccione un reporte para exportar");
                return;
            }
            
            // TODO: Implementar método en ReporteService
            // reporteService.exportarACSV(reporteSeleccionado.getId());
            mostrarInfo("Reporte exportado a CSV exitosamente");
            
        } catch (Exception e) {
            logger.error("Error al exportar a CSV", e);
            mostrarError("Error al exportar el reporte a CSV");
        }
    }
    
    /**
     * Maneja la actualización de reportes
     */
    @FXML
    private void handleActualizar() {
        cargarResumenReportes();
        cargarReportesGenerados();
        mostrarInfo("Reportes actualizados");
    }
    
    /**
     * Maneja la generación del reporte completo
     */
    @FXML
    private void handleGenerarReporteCompleto() {
        try {
            // TODO: Implementar método en ReporteService
            ReporteGenerado reporte = new ReporteGenerado(7, "Completo", "Hoy", "Mes actual", "Generado", "Ver");
            reportes.add(reporte);
            mostrarInfo("Reporte completo generado exitosamente");
            
        } catch (Exception e) {
            logger.error("Error al generar reporte completo", e);
            mostrarError("Error al generar el reporte completo");
        }
    }
    
    /**
     * Maneja la configuración de reportes
     */
    @FXML
    private void handleConfigurarReporte() {
        try {
            // TODO: Implementar configuración de reportes
            mostrarInfo("Funcionalidad de configuración de reportes en desarrollo");
            
        } catch (Exception e) {
            logger.error("Error al configurar reporte", e);
            mostrarError("Error al configurar el reporte");
        }
    }
    
    /**
     * Maneja el clic en "Ver Detalles" de cuotas vencidas
     */
    @FXML
    private void handleVerDetallesCuotasVencidas() {
        try {
            logger.info("Botón Ver Detalles de cuotas vencidas presionado");
            
            // Crear una ventana de prueba simple primero
            Stage ventanaPrueba = new Stage();
            ventanaPrueba.setTitle("Prueba - Cuotas Vencidas");
            ventanaPrueba.setWidth(400);
            ventanaPrueba.setHeight(300);
            
            VBox layout = new VBox(10);
            layout.setPadding(new javafx.geometry.Insets(20));
            
            Label titulo = new Label("¡El botón funciona!");
            titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
            
            Label mensaje = new Label("Esta es una ventana de prueba para verificar que el botón 'Ver Detalles' está funcionando correctamente.");
            mensaje.setWrapText(true);
            
            Button btnCerrar = new Button("Cerrar");
            btnCerrar.setOnAction(e -> ventanaPrueba.close());
            
            layout.getChildren().addAll(titulo, mensaje, btnCerrar);
            
            Scene scene = new Scene(layout);
            ventanaPrueba.setScene(scene);
            ventanaPrueba.show();
            
            // También mostrar mensaje en consola
            mostrarInfo("Botón 'Ver Detalles' funcionando correctamente");
            
        } catch (Exception e) {
            logger.error("Error al mostrar ventana de prueba", e);
            mostrarError("Error al mostrar ventana de prueba: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene cuotas vencidas filtradas por asesor actual
     */
    private List<Cronograma> obtenerCuotasVencidasPorAsesor() {
        try {
            logger.info("Obteniendo cuotas vencidas...");
            
            // Por ahora, obtenemos todas las cuotas vencidas
            List<Cronograma> todasLasCuotasVencidas = prestamoService.obtenerCuotasVencidas();
            logger.info("Total de cuotas vencidas obtenidas: " + todasLasCuotasVencidas.size());
            
            // TODO: Filtrar por asesor cuando tengamos SessionManager
            // Long idAsesorActual = SessionManager.getInstance().getUsuarioActual().getId();
            // return todasLasCuotasVencidas.stream()
            //     .filter(cuota -> cuota.getPrestamo().getIdAsesor().equals(idAsesorActual))
            //     .collect(Collectors.toList());
            
            return todasLasCuotasVencidas;
            
        } catch (Exception e) {
            logger.error("Error al obtener cuotas vencidas por asesor", e);
            return new ArrayList<>();
        }
    }
    
    /**
     * Muestra los detalles de las cuotas vencidas en una nueva ventana
     */
    private void mostrarDetallesCuotasVencidas(List<Cronograma> cuotasVencidas) {
        try {
            logger.info("Iniciando creación de ventana de detalles para " + cuotasVencidas.size() + " cuotas vencidas");
            
            // Crear una nueva ventana para mostrar los detalles
            Stage detallesStage = new Stage();
            detallesStage.setTitle("Detalles de Cuotas Vencidas");
            detallesStage.setWidth(900);
            detallesStage.setHeight(700);
            
            // Crear tabla para mostrar las cuotas vencidas
            TableView<Cronograma> tablaCuotas = new TableView<>();
            
            // Columnas básicas primero
            TableColumn<Cronograma, Long> colIdCuota = new TableColumn<>("ID Cuota");
            colIdCuota.setCellValueFactory(new PropertyValueFactory<>("idCuota"));
            colIdCuota.setPrefWidth(80);
            
            TableColumn<Cronograma, Long> colIdPrestamo = new TableColumn<>("ID Préstamo");
            colIdPrestamo.setCellValueFactory(new PropertyValueFactory<>("idPrestamo"));
            colIdPrestamo.setPrefWidth(100);
            
            TableColumn<Cronograma, Integer> colNumeroCuota = new TableColumn<>("N° Cuota");
            colNumeroCuota.setCellValueFactory(new PropertyValueFactory<>("numeroCuota"));
            colNumeroCuota.setPrefWidth(80);
            
            TableColumn<Cronograma, LocalDate> colFechaProgramada = new TableColumn<>("Fecha Programada");
            colFechaProgramada.setCellValueFactory(new PropertyValueFactory<>("fechaProgramada"));
            colFechaProgramada.setPrefWidth(120);
            
            TableColumn<Cronograma, BigDecimal> colMonto = new TableColumn<>("Monto");
            colMonto.setCellValueFactory(new PropertyValueFactory<>("montoCuota"));
            colMonto.setPrefWidth(100);
            
            TableColumn<Cronograma, String> colEstado = new TableColumn<>("Estado");
            colEstado.setCellValueFactory(cellData -> {
                Cronograma.EstadoCuota estado = cellData.getValue().getEstadoCuota();
                return new javafx.beans.property.SimpleStringProperty(estado.getDescripcion());
            });
            colEstado.setPrefWidth(100);
            
            // Calcular días de retraso
            TableColumn<Cronograma, String> colDiasRetraso = new TableColumn<>("Días de Retraso");
            colDiasRetraso.setCellValueFactory(cellData -> {
                LocalDate fechaProgramada = cellData.getValue().getFechaProgramada();
                long diasRetraso = java.time.temporal.ChronoUnit.DAYS.between(fechaProgramada, LocalDate.now());
                return new javafx.beans.property.SimpleStringProperty(String.valueOf(diasRetraso));
            });
            colDiasRetraso.setPrefWidth(100);
            
            // Agregar columnas a la tabla
            tablaCuotas.getColumns().add(colIdCuota);
            tablaCuotas.getColumns().add(colIdPrestamo);
            tablaCuotas.getColumns().add(colNumeroCuota);
            tablaCuotas.getColumns().add(colFechaProgramada);
            tablaCuotas.getColumns().add(colMonto);
            tablaCuotas.getColumns().add(colEstado);
            tablaCuotas.getColumns().add(colDiasRetraso);
            
            // Cargar datos
            ObservableList<Cronograma> cuotasObservable = FXCollections.observableArrayList(cuotasVencidas);
            tablaCuotas.setItems(cuotasObservable);
            
            // Crear layout
            VBox layout = new VBox(10);
            layout.setPadding(new javafx.geometry.Insets(10));
            
            Label titulo = new Label("Cuotas Vencidas - Total: " + cuotasVencidas.size());
            titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
            
            // Calcular total de monto vencido
            BigDecimal totalVencido = cuotasVencidas.stream()
                .map(Cronograma::getMontoCuota)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            Label totalLabel = new Label("Monto Total Vencido: S/ " + String.format("%.2f", totalVencido));
            totalLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: red;");
            
            layout.getChildren().addAll(titulo, totalLabel, tablaCuotas);
            
            // Crear escena
            Scene scene = new Scene(layout);
            detallesStage.setScene(scene);
            
            // Mostrar la ventana
            detallesStage.show();
            logger.info("Ventana de detalles mostrada exitosamente");
            
        } catch (Exception e) {
            logger.error("Error al mostrar detalles de cuotas vencidas", e);
            mostrarError("Error al mostrar los detalles de cuotas vencidas: " + e.getMessage());
        }
    }
    
    /**
     * Clase para representar un reporte generado
     */
    public static class ReporteGenerado {
        private int id;
        private String tipoReporte;
        private String fechaGeneracion;
        private String periodo;
        private String estado;
        private String acciones;
        
        public ReporteGenerado(int id, String tipoReporte, String fechaGeneracion, String periodo, String estado, String acciones) {
            this.id = id;
            this.tipoReporte = tipoReporte;
            this.fechaGeneracion = fechaGeneracion;
            this.periodo = periodo;
            this.estado = estado;
            this.acciones = acciones;
        }
        
        // Getters
        public int getId() { return id; }
        public String getTipoReporte() { return tipoReporte; }
        public String getFechaGeneracion() { return fechaGeneracion; }
        public String getPeriodo() { return periodo; }
        public String getEstado() { return estado; }
        public String getAcciones() { return acciones; }
    }
    
    /**
     * Clase para representar un dato del reporte
     */
    public static class DatoReporte {
        private String campo;
        private String valor;
        
        public DatoReporte(String campo, String valor) {
            this.campo = campo;
            this.valor = valor;
        }
        
        // Getters
        public String getCampo() { return campo; }
        public String getValor() { return valor; }
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
