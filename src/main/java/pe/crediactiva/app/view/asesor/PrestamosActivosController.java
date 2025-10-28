package pe.crediactiva.app.view.asesor;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pe.crediactiva.app.model.Cliente;
import pe.crediactiva.app.model.Prestamo;
import pe.crediactiva.app.model.Cronograma;
import pe.crediactiva.app.service.PrestamoService;
import pe.crediactiva.app.service.PagoService;
import pe.crediactiva.app.util.FechaUtil;
import pe.crediactiva.app.config.SessionManager;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Controlador para la vista de préstamos activos del asesor
 */
public class PrestamosActivosController {
    
    private static final Logger logger = LoggerFactory.getLogger(PrestamosActivosController.class);
    
    @FXML
    private Label lblTotalPrestamosActivos;
    
    @FXML
    private Label lblMontoTotalDesembolsado;
    
    @FXML
    private Label lblRecaudacionMes;
    
    @FXML
    private Label lblMorosidad;
    
    @FXML
    private TextField txtBuscar;
    
    @FXML
    private ComboBox<String> cmbFiltro;
    
    @FXML
    private TableView<Prestamo> tblPrestamosActivos;
    
    @FXML
    private TableColumn<Prestamo, Long> colIdPrestamo;
    
    @FXML
    private TableColumn<Prestamo, String> colCliente;
    
    @FXML
    private TableColumn<Prestamo, String> colMontoSolicitado;
    
    @FXML
    private TableColumn<Prestamo, String> colMontoDesembolsado;
    
    @FXML
    private TableColumn<Prestamo, String> colTasaInteres;
    
    @FXML
    private TableColumn<Prestamo, String> colNumeroCuotas;
    
    @FXML
    private TableColumn<Prestamo, String> colTipoPago;
    
    @FXML
    private TableColumn<Prestamo, String> colFechaInicio;
    
    @FXML
    private TableColumn<Prestamo, String> colFechaFin;
    
    @FXML
    private TableColumn<Prestamo, String> colEstado;
    
    @FXML
    private Label lblInfoPaginacion;
    
    @FXML
    private Button btnAnterior;
    
    @FXML
    private Button btnSiguiente;
    
    private PrestamoService prestamoService;
    private PagoService pagoService;
    private ObservableList<Prestamo> prestamosActivos;
    private int paginaActual = 1;
    private int elementosPorPagina = 20;
    private String filtroActual = "";
    private String busquedaActual = "";
    
    public PrestamosActivosController() {
        this.prestamoService = new PrestamoService();
        this.pagoService = new PagoService();
        this.prestamosActivos = FXCollections.observableArrayList();
    }
    
    @FXML
    private void initialize() {
        try {
            logger.info("Inicializando vista de préstamos activos...");
            configurarTabla();
            cargarPrestamosActivos();
            configurarFiltros();
            actualizarResumen();
            logger.info("Vista de préstamos activos inicializada correctamente");
            
        } catch (Exception e) {
            logger.error("Error al inicializar vista de préstamos activos", e);
            mostrarError("Error al inicializar la vista de préstamos activos: " + e.getMessage());
        }
    }
    
    /**
     * Configura las columnas de la tabla
     */
    private void configurarTabla() {
        colIdPrestamo.setCellValueFactory(new PropertyValueFactory<>("idPrestamo"));
        
        colCliente.setCellValueFactory(cellData -> {
            Prestamo prestamo = cellData.getValue();
            if (prestamo.getCliente() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    prestamo.getCliente().getNombre() + " " + prestamo.getCliente().getApellido()
                );
            }
            return new javafx.beans.property.SimpleStringProperty("N/A");
        });
        
        colMontoSolicitado.setCellValueFactory(cellData -> {
            Prestamo prestamo = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                String.format("S/ %.2f", prestamo.getMontoSolicitado())
            );
        });
        
        colMontoDesembolsado.setCellValueFactory(cellData -> {
            Prestamo prestamo = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                String.format("S/ %.2f", prestamo.getMontoDesembolsado())
            );
        });
        
        colTasaInteres.setCellValueFactory(cellData -> {
            Prestamo prestamo = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                String.format("%.2f%%", prestamo.getTasaInteres())
            );
        });
        
        colNumeroCuotas.setCellValueFactory(cellData -> {
            Prestamo prestamo = cellData.getValue();
            int numeroCuotas = calcularNumeroCuotas(prestamo.getPeriodoMeses(), prestamo.getTipoPago());
            return new javafx.beans.property.SimpleStringProperty(String.valueOf(numeroCuotas));
        });
        
        colTipoPago.setCellValueFactory(cellData -> {
            Prestamo prestamo = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                prestamo.getTipoPago().toString()
            );
        });
        
        colFechaInicio.setCellValueFactory(cellData -> {
            Prestamo prestamo = cellData.getValue();
            if (prestamo.getFechaInicio() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    FechaUtil.formatearFecha(prestamo.getFechaInicio())
                );
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        
        colFechaFin.setCellValueFactory(cellData -> {
            Prestamo prestamo = cellData.getValue();
            if (prestamo.getFechaFin() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    FechaUtil.formatearFecha(prestamo.getFechaFin())
                );
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        
        colEstado.setCellValueFactory(cellData -> {
            Prestamo prestamo = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                prestamo.getEstado().toString()
            );
        });
        
        // Configurar selección de fila
        tblPrestamosActivos.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }
    
    /**
     * Configura los filtros disponibles
     */
    private void configurarFiltros() {
        cmbFiltro.getItems().addAll("Todos", "Diario", "Semanal", "Mensual");
        cmbFiltro.setValue("Todos");
    }
    
    /**
     * Carga los préstamos activos del asesor
     */
    private void cargarPrestamosActivos() {
        try {
            // Obtener el ID del asesor actual de la sesión
            Long idAsesor = SessionManager.getInstance().getAsesorId();
            
            if (idAsesor == null) {
                logger.error("No se pudo obtener el ID del asesor de la sesión");
                mostrarError("Error: No se pudo identificar al asesor");
                return;
            }
            
            // Obtener préstamos activos del asesor
            List<Prestamo> listaPrestamos = prestamoService.obtenerPrestamosPorAsesor(idAsesor);
            
            // Filtrar solo los activos
            listaPrestamos = listaPrestamos.stream()
                .filter(prestamo -> prestamo.getEstado() == Prestamo.EstadoPrestamo.ACTIVO)
                .collect(java.util.stream.Collectors.toList());
            
            // Aplicar búsqueda si hay texto de búsqueda
            if (busquedaActual != null && !busquedaActual.isEmpty()) {
                listaPrestamos = filtrarPorBusqueda(listaPrestamos, busquedaActual);
            }
            
            // Aplicar filtro si hay uno seleccionado
            if (filtroActual != null && !filtroActual.isEmpty()) {
                listaPrestamos = aplicarFiltro(listaPrestamos, filtroActual);
            }
            
            prestamosActivos.clear();
            prestamosActivos.addAll(listaPrestamos);
            tblPrestamosActivos.setItems(prestamosActivos);
            
            actualizarInfoPaginacion();
            actualizarResumen();
            
            logger.info("Cargados " + listaPrestamos.size() + " préstamos activos para el asesor: " + idAsesor);
            
        } catch (Exception e) {
            logger.error("Error al cargar préstamos activos", e);
            mostrarError("Error al cargar la lista de préstamos activos");
        }
    }
    
    /**
     * Actualiza el resumen de estadísticas
     */
    private void actualizarResumen() {
        try {
            Long idAsesor = SessionManager.getInstance().getAsesorId();
            if (idAsesor == null) return;
            
            // Total de préstamos activos
            int totalPrestamos = prestamosActivos.size();
            lblTotalPrestamosActivos.setText(String.valueOf(totalPrestamos));
            
            // Monto total desembolsado
            BigDecimal montoTotal = prestamosActivos.stream()
                .map(Prestamo::getMontoDesembolsado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            lblMontoTotalDesembolsado.setText(String.format("S/ %.2f", montoTotal));
            
            // Recaudación del mes (usando método existente)
            double recaudacionMes = prestamoService.obtenerTotalPagadoPorCliente(idAsesor);
            lblRecaudacionMes.setText(String.format("S/ %.2f", recaudacionMes));
            
            // Morosidad
            double morosidad = prestamoService.calcularMorosidadPorAsesor(idAsesor);
            lblMorosidad.setText(String.format("%.2f%%", morosidad));
            
        } catch (Exception e) {
            logger.error("Error al actualizar resumen", e);
        }
    }
    
    /**
     * Filtra la lista de préstamos según el texto de búsqueda
     */
    private List<Prestamo> filtrarPorBusqueda(List<Prestamo> prestamos, String textoBusqueda) {
        String busquedaLower = textoBusqueda.toLowerCase().trim();
        
        return prestamos.stream()
            .filter(prestamo -> {
                if (prestamo.getCliente() != null) {
                    // Buscar por nombre del cliente
                    String nombreCompleto = (prestamo.getCliente().getNombre() + " " + prestamo.getCliente().getApellido()).toLowerCase();
                    if (nombreCompleto.contains(busquedaLower)) {
                        return true;
                    }
                    
                    // Buscar por DNI del cliente
                    String dni = String.valueOf(prestamo.getCliente().getIdCliente());
                    if (dni.contains(busquedaLower)) {
                        return true;
                    }
                }
                
                // Buscar por ID del préstamo
                String idPrestamo = String.valueOf(prestamo.getIdPrestamo());
                if (idPrestamo.contains(busquedaLower)) {
                    return true;
                }
                
                return false;
            })
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Aplica el filtro seleccionado a la lista de préstamos
     */
    private List<Prestamo> aplicarFiltro(List<Prestamo> prestamos, String filtro) {
        switch (filtro.toLowerCase()) {
            case "diario":
                return prestamos.stream()
                    .filter(prestamo -> prestamo.getTipoPago() == Prestamo.TipoPago.DIARIO)
                    .collect(java.util.stream.Collectors.toList());
                    
            case "semanal":
                return prestamos.stream()
                    .filter(prestamo -> prestamo.getTipoPago() == Prestamo.TipoPago.SEMANAL)
                    .collect(java.util.stream.Collectors.toList());
                    
            case "mensual":
                return prestamos.stream()
                    .filter(prestamo -> prestamo.getTipoPago() == Prestamo.TipoPago.MENSUAL)
                    .collect(java.util.stream.Collectors.toList());
                    
            default:
                return prestamos;
        }
    }
    
    /**
     * Actualiza la información de paginación
     */
    private void actualizarInfoPaginacion() {
        try {
            int totalPrestamos = prestamosActivos.size();
            int inicio = (paginaActual - 1) * elementosPorPagina + 1;
            int fin = Math.min(paginaActual * elementosPorPagina, totalPrestamos);
            
            lblInfoPaginacion.setText(
                String.format("Mostrando %d-%d de %d préstamos", inicio, fin, totalPrestamos)
            );
            
            // Habilitar/deshabilitar botones de paginación
            btnAnterior.setDisable(paginaActual <= 1);
            btnSiguiente.setDisable(fin >= totalPrestamos);
            
        } catch (Exception e) {
            logger.error("Error al actualizar información de paginación", e);
        }
    }
    
    /**
     * Maneja la búsqueda de préstamos
     */
    @FXML
    private void handleBuscar() {
        busquedaActual = txtBuscar.getText().trim();
        paginaActual = 1;
        cargarPrestamosActivos();
        
        if (busquedaActual.isEmpty()) {
            logger.info("Búsqueda vacía - mostrando todos los préstamos activos");
        } else {
            logger.info("Búsqueda realizada: '" + busquedaActual + "' - Resultados: " + prestamosActivos.size());
        }
    }
    
    /**
     * Limpia los filtros de búsqueda
     */
    @FXML
    private void handleLimpiar() {
        txtBuscar.clear();
        cmbFiltro.setValue("Todos");
        busquedaActual = "";
        filtroActual = "";
        paginaActual = 1;
        cargarPrestamosActivos();
        logger.info("Filtros limpiados");
    }
    
    /**
     * Maneja el filtrado de préstamos
     */
    @FXML
    private void handleFiltrar() {
        String filtroSeleccionado = cmbFiltro.getValue();
        if (filtroSeleccionado != null) {
            filtroActual = filtroSeleccionado.toLowerCase();
        } else {
            filtroActual = "";
        }
        
        paginaActual = 1;
        cargarPrestamosActivos();
        
        if (filtroActual.isEmpty()) {
            logger.info("Filtro 'Todos' aplicado - Resultados: " + prestamosActivos.size());
        } else {
            logger.info("Filtro aplicado: '" + filtroActual + "' - Resultados: " + prestamosActivos.size());
        }
    }
    
    /**
     * Maneja la visualización de detalles del préstamo
     */
    @FXML
    private void handleVerDetalles() {
        Prestamo prestamoSeleccionado = tblPrestamosActivos.getSelectionModel().getSelectedItem();
        
        if (prestamoSeleccionado == null) {
            mostrarAdvertencia("Por favor seleccione un préstamo para ver sus detalles");
            return;
        }
        
        try {
            // Crear una ventana modal para mostrar los detalles del préstamo
            Stage detallesStage = new Stage();
            
            // Crear el contenido de la ventana de detalles
            VBox contenido = crearVentanaDetallesPrestamo(prestamoSeleccionado);
            
            // Crear un ScrollPane para hacer el contenido scrolleable
            ScrollPane scrollPane = new ScrollPane(contenido);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setStyle("-fx-background-color: #f8fafc;");
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            
            Scene scene = new Scene(scrollPane, 800, 600);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            // Configurar la ventana
            detallesStage.setTitle("CrediActiva - Detalles del Préstamo");
            detallesStage.setScene(scene);
            detallesStage.setResizable(true);
            detallesStage.setMinWidth(600);
            detallesStage.setMinHeight(400);
            detallesStage.centerOnScreen();
            
            // Mostrar la ventana modal
            detallesStage.showAndWait();
            
            logger.info("Ventana de detalles del préstamo cerrada para préstamo: " + prestamoSeleccionado.getIdPrestamo());
            
        } catch (Exception e) {
            logger.error("Error al mostrar detalles del préstamo", e);
            mostrarError("Error al mostrar los detalles del préstamo");
        }
    }
    
    /**
     * Crea la ventana de detalles del préstamo
     */
    private VBox crearVentanaDetallesPrestamo(Prestamo prestamo) {
        VBox contenido = new VBox(20);
        contenido.setPadding(new javafx.geometry.Insets(30));
        contenido.setStyle("-fx-background-color: #f8fafc;");
        contenido.setMinWidth(600);
        
        // Título
        Label titulo = new Label("💰 Detalles del Préstamo #" + prestamo.getIdPrestamo());
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        // Información del préstamo
        VBox infoPrestamo = new VBox(15);
        infoPrestamo.setStyle("-fx-background-color: white; -fx-background-radius: 10px; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        
        Label tituloInfo = new Label("📋 Información del Préstamo");
        tituloInfo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        
        // Crear grid para información del préstamo
        GridPane gridPrestamo = new GridPane();
        gridPrestamo.setHgap(20);
        gridPrestamo.setVgap(10);
        
        // Información básica
        gridPrestamo.add(new Label("ID Préstamo:"), 0, 0);
        gridPrestamo.add(new Label(String.valueOf(prestamo.getIdPrestamo())), 1, 0);
        
        gridPrestamo.add(new Label("Cliente:"), 0, 1);
        String nombreCliente = "N/A";
        if (prestamo.getCliente() != null) {
            nombreCliente = prestamo.getCliente().getNombre() + " " + prestamo.getCliente().getApellido();
        }
        gridPrestamo.add(new Label(nombreCliente), 1, 1);
        
        gridPrestamo.add(new Label("Monto Solicitado:"), 0, 2);
        gridPrestamo.add(new Label(String.format("S/ %.2f", prestamo.getMontoSolicitado())), 1, 2);
        
        gridPrestamo.add(new Label("Monto Desembolsado:"), 0, 3);
        gridPrestamo.add(new Label(String.format("S/ %.2f", prestamo.getMontoDesembolsado())), 1, 3);
        
        gridPrestamo.add(new Label("Tasa de Interés:"), 0, 4);
        gridPrestamo.add(new Label(String.format("%.2f%%", prestamo.getTasaInteres())), 1, 4);
        
        gridPrestamo.add(new Label("Número de Cuotas:"), 0, 5);
        int numeroCuotas = calcularNumeroCuotas(prestamo.getPeriodoMeses(), prestamo.getTipoPago());
        gridPrestamo.add(new Label(String.valueOf(numeroCuotas)), 1, 5);
        
        gridPrestamo.add(new Label("Tipo de Pago:"), 0, 6);
        gridPrestamo.add(new Label(prestamo.getTipoPago().toString()), 1, 6);
        
        gridPrestamo.add(new Label("Fecha de Inicio:"), 0, 7);
        String fechaInicio = prestamo.getFechaInicio() != null ? FechaUtil.formatearFecha(prestamo.getFechaInicio()) : "-";
        gridPrestamo.add(new Label(fechaInicio), 1, 7);
        
        gridPrestamo.add(new Label("Fecha de Fin:"), 0, 8);
        String fechaFin = prestamo.getFechaFin() != null ? FechaUtil.formatearFecha(prestamo.getFechaFin()) : "-";
        gridPrestamo.add(new Label(fechaFin), 1, 8);
        
        gridPrestamo.add(new Label("Estado:"), 0, 9);
        Label estadoLabel = new Label(prestamo.getEstado().toString());
        estadoLabel.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
        gridPrestamo.add(estadoLabel, 1, 9);
        
        infoPrestamo.getChildren().addAll(tituloInfo, gridPrestamo);
        
        // Cronograma de pagos
        VBox cronograma = crearCronogramaPrestamo(prestamo);
        
        // Botón de cerrar
        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5px;");
        btnCerrar.setOnAction(e -> ((Stage) btnCerrar.getScene().getWindow()).close());
        
        // Contenedor para el botón centrado
        HBox botonContainer = new HBox();
        botonContainer.setAlignment(javafx.geometry.Pos.CENTER);
        botonContainer.getChildren().add(btnCerrar);
        
        contenido.getChildren().addAll(titulo, infoPrestamo, cronograma, botonContainer);
        
        return contenido;
    }
    
    /**
     * Crea la sección de cronograma del préstamo
     */
    private VBox crearCronogramaPrestamo(Prestamo prestamo) {
        VBox seccion = new VBox(15);
        seccion.setStyle("-fx-background-color: white; -fx-background-radius: 10px; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        
        Label tituloSeccion = new Label("📅 Cronograma de Pagos");
        tituloSeccion.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        
        // Crear tabla de cronograma
        TableView<Cronograma> tablaCronograma = new TableView<>();
        tablaCronograma.setPrefHeight(200);
        tablaCronograma.setMaxHeight(200);
        
        TableColumn<Cronograma, Integer> colNumeroCuota = new TableColumn<>("N° Cuota");
        colNumeroCuota.setCellValueFactory(new PropertyValueFactory<>("numeroCuota"));
        colNumeroCuota.setPrefWidth(80);
        
        TableColumn<Cronograma, String> colFechaProgramada = new TableColumn<>("Fecha Programada");
        colFechaProgramada.setCellValueFactory(cellData -> {
            Cronograma cuota = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                FechaUtil.formatearFecha(cuota.getFechaProgramada())
            );
        });
        colFechaProgramada.setPrefWidth(130);
        
        TableColumn<Cronograma, String> colMontoCuota = new TableColumn<>("Monto Cuota");
        colMontoCuota.setCellValueFactory(cellData -> {
            Cronograma cuota = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                String.format("S/ %.2f", cuota.getMontoCuota())
            );
        });
        colMontoCuota.setPrefWidth(120);
        
        TableColumn<Cronograma, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(cellData -> {
            Cronograma cuota = cellData.getValue();
            String estado = cuota.getEstadoCuota().toString();
            return new javafx.beans.property.SimpleStringProperty(estado);
        });
        colEstado.setPrefWidth(100);
        
        TableColumn<Cronograma, String> colFechaPago = new TableColumn<>("Fecha Pago");
        colFechaPago.setCellValueFactory(cellData -> {
            Cronograma cuota = cellData.getValue();
            if (cuota.getFechaPagoReal() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    FechaUtil.formatearFecha(cuota.getFechaPagoReal())
                );
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        colFechaPago.setPrefWidth(120);
        
        tablaCronograma.getColumns().addAll(colNumeroCuota, colFechaProgramada, colMontoCuota, colEstado, colFechaPago);
        
        try {
            // Obtener cronograma del préstamo
            List<Cronograma> cronogramaList = prestamoService.obtenerCuotasPorPrestamo(prestamo.getIdPrestamo());
            tablaCronograma.getItems().addAll(cronogramaList);
            
            if (cronogramaList.isEmpty()) {
                Label lblSinDatos = new Label("No hay cronograma disponible");
                lblSinDatos.setStyle("-fx-text-fill: #6b7280;");
                seccion.getChildren().addAll(tituloSeccion, lblSinDatos);
            } else {
                seccion.getChildren().addAll(tituloSeccion, tablaCronograma);
            }
            
        } catch (Exception e) {
            logger.error("Error al cargar cronograma del préstamo", e);
            seccion.getChildren().addAll(tituloSeccion, new Label("Error al cargar cronograma"));
        }
        
        return seccion;
    }
    
    /**
     * Maneja la visualización del cronograma
     */
    @FXML
    private void handleVerCronograma() {
        Prestamo prestamoSeleccionado = tblPrestamosActivos.getSelectionModel().getSelectedItem();
        
        if (prestamoSeleccionado == null) {
            mostrarAdvertencia("Por favor seleccione un préstamo para ver su cronograma");
            return;
        }
        
        // Por ahora, mostrar un mensaje informativo
        mostrarInfo("Funcionalidad de cronograma detallado en desarrollo");
    }
    
    /**
     * Actualiza la lista de préstamos activos
     */
    @FXML
    private void handleActualizar() {
        cargarPrestamosActivos();
        mostrarInfo("Lista de préstamos activos actualizada");
    }
    
    /**
     * Navega a la página anterior
     */
    @FXML
    private void handleAnterior() {
        if (paginaActual > 1) {
            paginaActual--;
            cargarPrestamosActivos();
        }
    }
    
    /**
     * Navega a la página siguiente
     */
    @FXML
    private void handleSiguiente() {
        try {
            int totalPrestamos = prestamosActivos.size();
            int totalPaginas = (int) Math.ceil((double) totalPrestamos / elementosPorPagina);
            
            if (paginaActual < totalPaginas) {
                paginaActual++;
                cargarPrestamosActivos();
            }
        } catch (Exception e) {
            logger.error("Error al navegar a la página siguiente", e);
        }
    }
    
    /**
     * Calcula el número de cuotas según el tipo de pago
     */
    private int calcularNumeroCuotas(int periodoMeses, Prestamo.TipoPago tipoPago) {
        switch (tipoPago) {
            case DIARIO:
                return periodoMeses * 26; // 26 días hábiles por mes
            case SEMANAL:
                return periodoMeses * 4; // 4 semanas por mes
            case MENSUAL:
                return periodoMeses; // 1 cuota por mes
            default:
                return periodoMeses * 26;
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
