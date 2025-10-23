package pe.crediactiva.app.view.asesor;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Orientation;
import pe.crediactiva.app.service.AuthenticationService;
import pe.crediactiva.app.service.ClienteService;
import pe.crediactiva.app.service.PrestamoService;
import pe.crediactiva.app.service.RecaudacionService;
import pe.crediactiva.app.service.PagoService;
import pe.crediactiva.app.config.SessionManager;
import pe.crediactiva.app.dao.CronogramaDAO;
import pe.crediactiva.app.dao.impl.CronogramaDAOImpl;
import pe.crediactiva.app.view.LoginController;
import pe.crediactiva.app.model.Cliente;
import pe.crediactiva.app.model.Prestamo;
import pe.crediactiva.app.model.RecaudacionAsesor;
import pe.crediactiva.app.model.Cronograma;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import pe.crediactiva.app.util.DateTimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Controlador principal para la interfaz del asesor
 */
public class AsesorMainController {
    
    private static final Logger logger = LoggerFactory.getLogger(AsesorMainController.class);
    
    @FXML
    private Label lblAsesorInfo;
    
    @FXML
    private Label lblFechaHoy;
    
    @FXML
    private Label lblRecaudacionMes;
    
    @FXML
    private Label lblSueldoEstimado;
    
    @FXML
    private VBox contentArea;
    
    @FXML
    private Button btnLogout;
    
    @FXML
    private Label lblCuotasDia;
    
    @FXML
    private Label lblCuotasVencidas;
    
    @FXML
    private Label lblRecaudacionDia;
    
    @FXML
    private Label lblRecaudacionMesCard;
    
    @FXML
    private Label lblClientesActivos;
    
    @FXML
    private Label lblPrestamosActivos;
    
    @FXML
    private Label lblMorosidad;
    
    @FXML
    private Label lblSueldoEstimadoCard;
    
    @FXML
    private Label lblUltimaActualizacion;
    
    private Stage primaryStage;
    private AuthenticationService authService;
    private PrestamoService prestamoService;
    private ClienteService clienteService;
    private RecaudacionService recaudacionService;
    private PagoService pagoService;
    private CronogramaDAO cronogramaDAO;
    
    public AsesorMainController() {
        this.authService = new AuthenticationService();
        this.prestamoService = new PrestamoService();
        this.clienteService = new ClienteService();
        this.recaudacionService = new RecaudacionService();
        this.pagoService = new PagoService();
        this.cronogramaDAO = new CronogramaDAOImpl();
    }
    
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    @FXML
    private void initialize() {
        try {
            // Configurar información del asesor
            try {
                if (authService.getCurrentUser() != null) {
                    lblAsesorInfo.setText("Asesor: " + authService.getCurrentUser().getIdUsuario());
                } else {
                    lblAsesorInfo.setText("Asesor: Usuario actual");
                }
            } catch (Exception e) {
                logger.warn("No se pudo obtener información del usuario actual", e);
                lblAsesorInfo.setText("Asesor: Usuario actual");
            }
            
            // Configurar fecha actual
            lblFechaHoy.setText("Fecha: " + DateTimeUtil.today().toString());
            
            // Cargar estadísticas del dashboard
            cargarEstadisticasDashboard();
            
            // Configurar última actualización
            lblUltimaActualizacion.setText("Última actualización: " + 
                DateTimeUtil.formatDateTime(DateTimeUtil.now()));
            
        } catch (Exception e) {
            logger.error("Error al inicializar la pantalla del asesor", e);
        }
    }
    
    /**
     * Carga las estadísticas del dashboard
     */
    private void cargarEstadisticasDashboard() {
        try {
            // Obtener el ID del asesor actual de la sesión
            Long idAsesor = SessionManager.getInstance().getAsesorId();
            
            if (idAsesor == null) {
                logger.error("No se pudo obtener el ID del asesor de la sesión");
                return;
            }
            
            // Cuotas del día filtradas por asesor
            int cuotasDia = prestamoService.obtenerCuotasDelDiaPorAsesor(idAsesor).size();
            actualizarLabelDashboard("lblCuotasDia", String.valueOf(cuotasDia));
            
            // Cuotas vencidas filtradas por asesor
            int cuotasVencidas = prestamoService.obtenerCuotasVencidasPorAsesor(idAsesor).size();
            actualizarLabelDashboard("lblCuotasVencidas", String.valueOf(cuotasVencidas));
            
            // Recaudación del día filtrada por asesor
            BigDecimal recaudacionDia = recaudacionService.obtenerRecaudacionDelDiaPorAsesor(idAsesor)
                .stream()
                .map(RecaudacionAsesor::getMontoRegistrado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            actualizarLabelDashboard("lblRecaudacionDia", "S/ " + String.format("%.2f", recaudacionDia));
            
            // Recaudación del mes actual filtrada por asesor (desde tabla pagos)
            BigDecimal recaudacionMes = pagoService.calcularRecaudacionMesActual(idAsesor);
            if (lblRecaudacionMes != null) {
                lblRecaudacionMes.setText("Recaudación Mes: S/ " + String.format("%.2f", recaudacionMes));
            }
            actualizarLabelDashboard("lblRecaudacionMesCard", "S/ " + String.format("%.2f", recaudacionMes));
            
            // Sueldo estimado (10% de la recaudación del mes)
            double sueldoEstimado = recaudacionMes.doubleValue() * 0.10;
            if (lblSueldoEstimado != null) {
                lblSueldoEstimado.setText("Sueldo Estimado: S/ " + String.format("%.2f", sueldoEstimado));
            }
            actualizarLabelDashboard("lblSueldoEstimadoCard", "S/ " + String.format("%.2f", sueldoEstimado));
            
            // Clientes activos del asesor
            int clientesActivos = clienteService.obtenerClientesPorAsesor(idAsesor).size();
            actualizarLabelDashboard("lblClientesActivos", String.valueOf(clientesActivos));
            
            // Préstamos activos del asesor
            int prestamosActivos = prestamoService.obtenerPrestamosPorAsesor(idAsesor).size();
            actualizarLabelDashboard("lblPrestamosActivos", String.valueOf(prestamosActivos));
            
            // Morosidad (porcentaje de cuotas vencidas del asesor)
            double morosidad = prestamoService.calcularMorosidadPorAsesor(idAsesor);
            actualizarLabelDashboard("lblMorosidad", String.format("%.1f%%", morosidad));
            
        } catch (Exception e) {
            logger.error("Error al cargar estadísticas del dashboard", e);
        }
    }
    
    /**
     * Actualiza un label del dashboard por su ID
     */
    private void actualizarLabelDashboard(String id, String texto) {
        try {
            // Buscar el label en el contentArea
            Label label = buscarLabelPorId(contentArea, id);
            if (label != null) {
                label.setText(texto);
            }
        } catch (Exception e) {
            logger.warn("No se pudo actualizar el label con ID: " + id, e);
        }
    }
    
    /**
     * Busca un label por su ID en un contenedor
     */
    private Label buscarLabelPorId(Parent contenedor, String id) {
        if (contenedor instanceof VBox) {
            VBox vbox = (VBox) contenedor;
            for (javafx.scene.Node nodo : vbox.getChildren()) {
                if (nodo.getId() != null && nodo.getId().equals(id) && nodo instanceof Label) {
                    return (Label) nodo;
                }
                if (nodo instanceof Parent) {
                    Label encontrado = buscarLabelPorId((Parent) nodo, id);
                    if (encontrado != null) {
                        return encontrado;
                    }
                }
            }
        } else if (contenedor instanceof GridPane) {
            GridPane grid = (GridPane) contenedor;
            for (javafx.scene.Node nodo : grid.getChildren()) {
                if (nodo.getId() != null && nodo.getId().equals(id) && nodo instanceof Label) {
                    return (Label) nodo;
                }
                if (nodo instanceof Parent) {
                    Label encontrado = buscarLabelPorId((Parent) nodo, id);
                    if (encontrado != null) {
                        return encontrado;
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * Maneja la opción de dashboard
     */
    @FXML
    private void handleDashboard() {
        try {
            logger.info("Intentando cargar dashboard...");
            
            VBox dashboardView = crearDashboardView();
            
            // Reemplazar contenido
            contentArea.getChildren().clear();
            contentArea.getChildren().add(dashboardView);
            
            // Recargar estadísticas
            cargarEstadisticasDashboard();
            
            logger.info("Dashboard cargado exitosamente");
            
        } catch (Exception e) {
            logger.error("Error al cargar dashboard", e);
            mostrarError("Error al cargar el dashboard");
        }
    }
    
    /**
     * Crea la vista del dashboard
     */
    private VBox crearDashboardView() {
        VBox dashboard = new VBox(20);
        dashboard.setPadding(new Insets(25));
        
        // Título del dashboard
        Label titulo = new Label("📊 Dashboard del Asesor");
        titulo.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 10 0;");
        
        // Estadísticas del día en grid moderno
        GridPane gridEstadisticas = new GridPane();
        gridEstadisticas.setHgap(20);
        gridEstadisticas.setVgap(20);
        
        // Configurar columnas
        for (int i = 0; i < 4; i++) {
            ColumnConstraints col = new ColumnConstraints();
            col.setHgrow(Priority.ALWAYS);
            col.setMinWidth(200);
            gridEstadisticas.getColumnConstraints().add(col);
        }
        
        // Configurar filas
        for (int i = 0; i < 2; i++) {
            RowConstraints row = new RowConstraints();
            row.setMinHeight(140);
            row.setVgrow(Priority.ALWAYS);
            gridEstadisticas.getRowConstraints().add(row);
        }
        
        // Cuotas del día
        VBox cuotasDia = crearTarjetaEstadistica("📅", "Cuotas del Día", "0", "Ver Detalles");
        cuotasDia.getChildren().get(2).setId("lblCuotasDia");
        ((Button)cuotasDia.getChildren().get(3)).setOnAction(e -> handleVerCuotasDia());
        gridEstadisticas.add(cuotasDia, 0, 0);
        
        // Cuotas vencidas
        VBox cuotasVencidas = crearTarjetaEstadistica("⚠️", "Cuotas Vencidas", "0", "Ver Detalles");
        cuotasVencidas.getChildren().get(2).setId("lblCuotasVencidas");
        ((Button)cuotasVencidas.getChildren().get(3)).setOnAction(e -> handleVerCuotasVencidas());
        gridEstadisticas.add(cuotasVencidas, 1, 0);
        
        // Recaudación del día
        VBox recaudacionDia = crearTarjetaEstadistica("💰", "Recaudación del Día", "S/ 0.00", "Ver Detalles");
        recaudacionDia.getChildren().get(2).setId("lblRecaudacionDia");
        ((Button)recaudacionDia.getChildren().get(3)).setOnAction(e -> handleVerRecaudacionDia());
        gridEstadisticas.add(recaudacionDia, 2, 0);
        
        // Recaudación del mes
        VBox recaudacionMes = crearTarjetaEstadistica("📊", "Recaudación del Mes", "S/ 0.00", "Ver Detalles");
        recaudacionMes.getChildren().get(2).setId("lblRecaudacionMesCard");
        ((Button)recaudacionMes.getChildren().get(3)).setOnAction(e -> handleVerRecaudacionMes());
        gridEstadisticas.add(recaudacionMes, 3, 0);
        
        // Clientes activos
        VBox clientesActivos = crearTarjetaEstadistica("👥", "Clientes Activos", "0", "Ver Detalles");
        clientesActivos.getChildren().get(2).setId("lblClientesActivos");
        ((Button)clientesActivos.getChildren().get(3)).setOnAction(e -> handleVerClientesActivos());
        gridEstadisticas.add(clientesActivos, 0, 1);
        
        // Préstamos activos
        VBox prestamosActivos = crearTarjetaEstadistica("📋", "Préstamos Activos", "0", "Ver Detalles");
        prestamosActivos.getChildren().get(2).setId("lblPrestamosActivos");
        ((Button)prestamosActivos.getChildren().get(3)).setOnAction(e -> handleVerPrestamosActivos());
        gridEstadisticas.add(prestamosActivos, 1, 1);
        
        // Morosidad
        VBox morosidad = crearTarjetaEstadistica("📉", "Morosidad", "0%", "Ver Detalles");
        morosidad.getChildren().get(2).setId("lblMorosidad");
        ((Button)morosidad.getChildren().get(3)).setOnAction(e -> handleVerMorosidad());
        gridEstadisticas.add(morosidad, 2, 1);
        
        // Sueldo estimado
        VBox sueldoEstimado = crearTarjetaEstadistica("💵", "Sueldo Estimado", "S/ 0.00", "Ver Detalles");
        sueldoEstimado.getChildren().get(2).setId("lblSueldoEstimadoCard");
        ((Button)sueldoEstimado.getChildren().get(3)).setOnAction(e -> handleVerSueldoEstimado());
        gridEstadisticas.add(sueldoEstimado, 3, 1);
        
        // Acciones rápidas
        VBox accionesRapidas = new VBox(15);
        accionesRapidas.setStyle("-fx-background-color: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%); -fx-background-radius: 16px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 15, 0, 0, 5); -fx-border-color: rgba(226,232,240,0.5); -fx-border-width: 1px; -fx-border-radius: 16px; -fx-padding: 25;");
        
        Label tituloAcciones = new Label("⚡ Acciones Rápidas");
        tituloAcciones.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #1e293b; -fx-padding: 0 0 15 0;");
        
        HBox botonesAcciones = new HBox(15);
        botonesAcciones.setAlignment(Pos.CENTER_LEFT);
        
        Button btnNuevoCliente = new Button("👤 Nuevo Cliente");
        btnNuevoCliente.setStyle("-fx-background-color: linear-gradient(135deg, #1e40af 0%, #1d4ed8 100%); -fx-text-fill: #3b82f6; -fx-font-weight: 600; -fx-font-size: 14px; -fx-padding: 12 24; -fx-background-radius: 10px; -fx-border-color: rgba(30,64,175,0.3); -fx-border-width: 1px; -fx-border-radius: 10px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(30,64,175,0.4), 8, 0, 0, 3); -fx-min-width: 160px;");
        btnNuevoCliente.setOnAction(e -> handleNuevoCliente());
        
        Button btnNuevaSolicitud = new Button("📝 Nueva Solicitud");
        btnNuevaSolicitud.setStyle("-fx-background-color: linear-gradient(135deg, #1e40af 0%, #1d4ed8 100%); -fx-text-fill: #3b82f6; -fx-font-weight: 600; -fx-font-size: 14px; -fx-padding: 12 24; -fx-background-radius: 10px; -fx-border-color: rgba(30,64,175,0.3); -fx-border-width: 1px; -fx-border-radius: 10px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(30,64,175,0.4), 8, 0, 0, 3); -fx-min-width: 160px;");
        btnNuevaSolicitud.setOnAction(e -> handleNuevaSolicitud());
        
        Button btnRegistrarPago = new Button("💳 Registrar Pago");
        btnRegistrarPago.setStyle("-fx-background-color: linear-gradient(135deg, #1e40af 0%, #1d4ed8 100%); -fx-text-fill: #3b82f6; -fx-font-weight: 600; -fx-font-size: 14px; -fx-padding: 12 24; -fx-background-radius: 10px; -fx-border-color: rgba(30,64,175,0.3); -fx-border-width: 1px; -fx-border-radius: 10px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(30,64,175,0.4), 8, 0, 0, 3); -fx-min-width: 160px;");
        btnRegistrarPago.setOnAction(e -> handleRegistrarPago());
        
        Button btnSimularCredito = new Button("🧮 Simular Crédito");
        btnSimularCredito.setStyle("-fx-background-color: linear-gradient(135deg, #1e40af 0%, #1d4ed8 100%); -fx-text-fill: #3b82f6; -fx-font-weight: 600; -fx-font-size: 14px; -fx-padding: 12 24; -fx-background-radius: 10px; -fx-border-color: rgba(30,64,175,0.3); -fx-border-width: 1px; -fx-border-radius: 10px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(30,64,175,0.4), 8, 0, 0, 3); -fx-min-width: 160px;");
        btnSimularCredito.setOnAction(e -> handleSimularCredito());
        
        botonesAcciones.getChildren().addAll(btnNuevoCliente, btnNuevaSolicitud, btnRegistrarPago, btnSimularCredito);
        accionesRapidas.getChildren().addAll(tituloAcciones, botonesAcciones);
        
        dashboard.getChildren().addAll(titulo, gridEstadisticas, accionesRapidas);
        
        return dashboard;
    }
    
    /**
     * Crea una tarjeta de estadística
     */
    private VBox crearTarjetaEstadistica(String icono, String titulo, String valor, String textoBoton) {
        VBox tarjeta = new VBox(8);
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setStyle("-fx-background-color: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%); -fx-background-radius: 16px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 15, 0, 0, 5); -fx-border-color: rgba(226,232,240,0.5); -fx-border-width: 1px; -fx-border-radius: 16px; -fx-padding: 20; -fx-min-height: 120px;");
        
        Label iconoLabel = new Label(icono);
        iconoLabel.setStyle("-fx-font-size: 28px; -fx-text-fill: #3b82f6; -fx-padding: 0 0 8 0;");
        
        Label tituloLabel = new Label(titulo);
        tituloLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b; -fx-font-weight: 600; -fx-padding: 0 0 5 0;");
        
        Label valorLabel = new Label(valor);
        valorLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: #1e293b; -fx-font-weight: 700; -fx-padding: 0 0 8 0;");
        
        Button boton = new Button(textoBoton);
        boton.setStyle("-fx-background-color: linear-gradient(135deg, #1e40af 0%, #1d4ed8 100%); -fx-text-fill: #3b82f6; -fx-font-weight: 600; -fx-font-size: 12px; -fx-padding: 8 16; -fx-background-radius: 8px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(30,64,175,0.4), 8, 0, 0, 3);");
        
        tarjeta.getChildren().addAll(iconoLabel, tituloLabel, valorLabel, boton);
        
        return tarjeta;
    }
    
    /**
     * Maneja la opción de gestión de clientes
     */
    @FXML
    private void handleClientes() {
        try {
            logger.info("Intentando cargar gestión de clientes...");
            
            VBox clientesView = crearGestionClientesView();
            
            // Reemplazar contenido
            contentArea.getChildren().clear();
            contentArea.getChildren().add(clientesView);
            
            logger.info("Cargada gestión de clientes exitosamente");
            
        } catch (Exception e) {
            logger.error("Error inesperado al cargar gestión de clientes", e);
            mostrarError("Error inesperado: " + e.getMessage());
        }
    }
    
    /**
     * Maneja la opción de solicitar préstamo
     */
    @FXML
    private void handleSolicitarPrestamo() {
        try {
            logger.info("Intentando cargar solicitud de préstamo...");
            
            VBox solicitarPrestamoView = crearSolicitarPrestamoView();
            
            // Reemplazar contenido
            contentArea.getChildren().clear();
            contentArea.getChildren().add(solicitarPrestamoView);
            
            logger.info("Cargada solicitud de préstamo exitosamente");
            
        } catch (Exception e) {
            logger.error("Error inesperado al cargar solicitud de préstamo", e);
            mostrarError("Error inesperado: " + e.getMessage());
        }
    }
    
    /**
     * Maneja la opción de simulador
     */
    @FXML
    private void handleSimulador() {
        try {
            VBox simuladorView = crearSimuladorView();
            
            // Crear ScrollPane para hacer scrolleable el contenido
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setContent(simuladorView);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            
            // Reemplazar contenido
            contentArea.getChildren().clear();
            contentArea.getChildren().add(scrollPane);
            
            logger.info("Cargado simulador de crédito con scroll");
            
        } catch (Exception e) {
            logger.error("Error al cargar simulador", e);
            mostrarError("Error al cargar el simulador de crédito");
        }
    }
    
    /**
     * Maneja la opción de registrar cobro
     */
    @FXML
    private void handleRegistrarCobro() {
        try {
            logger.info("Cargando vista FXML de registro de cobro...");

            javafx.scene.Node registrarCobro = javafx.fxml.FXMLLoader.load(
                    getClass().getResource("/fxml/asesor/RegistrarCobroView.fxml")
            );

            contentArea.getChildren().clear();
            contentArea.getChildren().add(registrarCobro);
            
            logger.info("Registro de cobro cargado desde FXML");
            
        } catch (Exception e) {
            logger.error("Error inesperado al cargar registro de cobro", e);
            mostrarError("Error inesperado: " + e.getMessage());
        }
    }
    
    /**
     * Maneja la opción de cartera
     */
    @FXML
    private void handleCartera() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/asesor/CarteraView.fxml"));
            VBox carteraView = loader.load();
            
            // Reemplazar contenido
            contentArea.getChildren().clear();
            contentArea.getChildren().add(carteraView);
            
            logger.info("Cargada cartera del asesor");
            
        } catch (IOException e) {
            logger.error("Error al cargar cartera", e);
            mostrarError("Error al cargar la cartera");
        }
    }
    
    /**
     * Maneja la opción de reportes
     */
    @FXML
    private void handleReportes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/asesor/ReportesView.fxml"));
            VBox reportesView = loader.load();
            
            // Reemplazar contenido
            contentArea.getChildren().clear();
            contentArea.getChildren().add(reportesView);
            
            logger.info("Cargados reportes del asesor");
            
        } catch (IOException e) {
            logger.error("Error al cargar reportes", e);
            mostrarError("Error al cargar los reportes");
        }
    }
    
    /**
     * Maneja la opción de ver cuotas del día
     */
    @FXML
    private void handleVerCuotasDia() {
        try {
            logger.info("Cargando vista de cuotas del día...");
            
            VBox cuotasDiaView = crearCuotasDiaView();
            
            // Reemplazar contenido
            contentArea.getChildren().clear();
            contentArea.getChildren().add(cuotasDiaView);
            
            logger.info("Cargadas cuotas del día exitosamente");
            
        } catch (Exception e) {
            logger.error("Error al cargar cuotas del día", e);
            mostrarError("Error al cargar las cuotas del día: " + e.getMessage());
        }
    }
    
    /**
     * Crea la vista detallada de cuotas del día
     */
    private VBox crearCuotasDiaView() {
        VBox contenedor = new VBox(20);
        contenedor.setPadding(new Insets(25));
        
        // Título
        Label titulo = new Label("📅 Cuotas del Día - " + DateTimeUtil.today().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        titulo.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 20 0;");
        
        // Panel de información
        VBox panelInfo = new VBox(15);
        panelInfo.setStyle("-fx-background-color: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%); -fx-background-radius: 16px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 15, 0, 0, 5); -fx-border-color: rgba(226,232,240,0.5); -fx-border-width: 1px; -fx-border-radius: 16px; -fx-padding: 25;");
        
        // Estadísticas del día
        HBox estadisticas = new HBox(30);
        estadisticas.setAlignment(Pos.CENTER_LEFT);
        
        VBox stat1 = crearEstadistica("📊", "Total Cuotas", "0", "#3b82f6");
        VBox stat2 = crearEstadistica("💰", "Monto Total", "S/ 0.00", "#10b981");
        VBox stat3 = crearEstadistica("✅", "Pagadas", "0", "#059669");
        VBox stat4 = crearEstadistica("⏳", "Pendientes", "0", "#f59e0b");
        
        estadisticas.getChildren().addAll(stat1, stat2, stat3, stat4);
        
        // Tabla de cuotas
        VBox panelTabla = new VBox(15);
        panelTabla.setStyle("-fx-background-color: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%); -fx-background-radius: 16px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 15, 0, 0, 5); -fx-border-color: rgba(226,232,240,0.5); -fx-border-width: 1px; -fx-border-radius: 16px; -fx-padding: 25;");
        
        Label tituloTabla = new Label("📋 Detalle de Cuotas del Día");
        tituloTabla.setStyle("-fx-font-size: 18px; -fx-font-weight: 700; -fx-text-fill: #1e293b; -fx-padding: 0 0 15 0;");
        
        // Crear tabla
        TableView<CuotaDiaInfo> tablaCuotas = crearTablaCuotasDia();
        
        // Botones de acción
        HBox botonesAccion = new HBox(15);
        botonesAccion.setAlignment(Pos.CENTER_LEFT);
        
        Button btnActualizar = new Button("🔄 Actualizar");
        btnActualizar.setStyle("-fx-background-color: linear-gradient(135deg, #3b82f6 0%, #1d4ed8 100%); -fx-text-fill: #ffffff; -fx-font-weight: 600; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 8px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(59,130,246,0.4), 8, 0, 0, 3);");
        btnActualizar.setOnAction(e -> actualizarCuotasDia(tablaCuotas, stat1, stat2, stat3, stat4));
        
        Button btnExportar = new Button("📊 Exportar");
        btnExportar.setStyle("-fx-background-color: linear-gradient(135deg, #10b981 0%, #059669 100%); -fx-text-fill: #ffffff; -fx-font-weight: 600; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 8px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(16,185,129,0.4), 8, 0, 0, 3);");
        btnExportar.setOnAction(e -> exportarCuotasDia());
        
        Button btnRegresar = new Button("← Regresar al Dashboard");
        btnRegresar.setStyle("-fx-background-color: linear-gradient(135deg, #6b7280 0%, #4b5563 100%); -fx-text-fill: #ffffff; -fx-font-weight: 600; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 8px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(107,114,128,0.4), 8, 0, 0, 3);");
        btnRegresar.setOnAction(e -> handleDashboard());
        
        botonesAccion.getChildren().addAll(btnActualizar, btnExportar, btnRegresar);
        
        panelTabla.getChildren().addAll(tituloTabla, tablaCuotas, botonesAccion);
        panelInfo.getChildren().add(estadisticas);
        
        contenedor.getChildren().addAll(titulo, panelInfo, panelTabla);
        
        // Cargar datos iniciales
        actualizarCuotasDia(tablaCuotas, stat1, stat2, stat3, stat4);
        
        return contenedor;
    }
    
    /**
     * Crea una estadística individual
     */
    private VBox crearEstadistica(String icono, String titulo, String valor, String color) {
        VBox stat = new VBox(5);
        stat.setAlignment(Pos.CENTER);
        
        Label iconoLabel = new Label(icono);
        iconoLabel.setStyle("-fx-font-size: 24px;");
        
        Label tituloLabel = new Label(titulo);
        tituloLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280; -fx-font-weight: 600;");
        
        Label valorLabel = new Label(valor);
        valorLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: " + color + "; -fx-font-weight: 700;");
        
        stat.getChildren().addAll(iconoLabel, tituloLabel, valorLabel);
        return stat;
    }
    
    /**
     * Crea la tabla de cuotas del día
     */
    private TableView<CuotaDiaInfo> crearTablaCuotasDia() {
        TableView<CuotaDiaInfo> tabla = new TableView<>();
        tabla.setStyle("-fx-background-color: transparent; -fx-border-color: #e5e7eb; -fx-border-width: 1px; -fx-border-radius: 8px;");
        tabla.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        
        // Columna Cliente
        TableColumn<CuotaDiaInfo, String> colCliente = new TableColumn<>("Cliente");
        colCliente.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getCliente()));
        colCliente.setStyle("-fx-alignment: CENTER_LEFT;");
        
        // Columna DNI
        TableColumn<CuotaDiaInfo, String> colDni = new TableColumn<>("DNI");
        colDni.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getDni()));
        colDni.setStyle("-fx-alignment: CENTER;");
        
        // Columna Monto
        TableColumn<CuotaDiaInfo, String> colMonto = new TableColumn<>("Monto");
        colMonto.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMonto()));
        colMonto.setStyle("-fx-alignment: CENTER_RIGHT;");
        
        // Columna Estado
        TableColumn<CuotaDiaInfo, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEstado()));
        colEstado.setCellFactory(column -> new TableCell<CuotaDiaInfo, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("Pagada".equals(item)) {
                        setStyle("-fx-text-fill: #059669; -fx-font-weight: 600;");
                    } else if ("Pendiente".equals(item)) {
                        setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: 600;");
                    } else if ("Vencida".equals(item)) {
                        setStyle("-fx-text-fill: #dc2626; -fx-font-weight: 600;");
                    }
                }
            }
        });
        
        // Columna ID Préstamo
        TableColumn<CuotaDiaInfo, String> colIdPrestamo = new TableColumn<>("ID Préstamo");
        colIdPrestamo.setCellValueFactory(cellData -> {
            Long idPrestamo = cellData.getValue().getIdPrestamo();
            return new javafx.beans.property.SimpleStringProperty(idPrestamo != null ? idPrestamo.toString() : "N/A");
        });
        colIdPrestamo.setStyle("-fx-alignment: CENTER;");
        
        // Columna ID Cuota
        TableColumn<CuotaDiaInfo, String> colIdCuota = new TableColumn<>("ID Cuota");
        colIdCuota.setCellValueFactory(cellData -> {
            Long idCuota = cellData.getValue().getIdCuota();
            return new javafx.beans.property.SimpleStringProperty(idCuota != null ? idCuota.toString() : "N/A");
        });
        colIdCuota.setStyle("-fx-alignment: CENTER;");
        
        // Columna Acciones
        TableColumn<CuotaDiaInfo, String> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(""));
        colAcciones.setCellFactory(column -> new TableCell<CuotaDiaInfo, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    CuotaDiaInfo cuota = getTableRow().getItem();
                    HBox botones = new HBox(5);
                    
                    if ("Pendiente".equals(cuota.getEstado())) {
                        Button btnPagar = new Button("💰 Pagar");
                        btnPagar.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 4 8; -fx-background-radius: 4px; -fx-cursor: hand;");
                        btnPagar.setOnAction(e -> registrarPagoCuota(cuota));
                        botones.getChildren().add(btnPagar);
                    }
                    
                    Button btnDetalle = new Button("👁️ Ver");
                    btnDetalle.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 4 8; -fx-background-radius: 4px; -fx-cursor: hand;");
                    btnDetalle.setOnAction(e -> verDetalleCuota(cuota));
                    botones.getChildren().add(btnDetalle);
                    
                    setGraphic(botones);
                }
            }
        });
        
        tabla.getColumns().addAll(colCliente, colDni, colMonto, colEstado, colIdPrestamo, colIdCuota, colAcciones);
        
        return tabla;
    }
    
    /**
     * Actualiza los datos de la tabla de cuotas del día
     */
    private void actualizarCuotasDia(TableView<CuotaDiaInfo> tabla, VBox statTotal, VBox statMonto, VBox statPagadas, VBox statPendientes) {
        try {
            // Obtener el ID del asesor actual de la sesión
            Long idAsesor = SessionManager.getInstance().getAsesorId();
            
            if (idAsesor == null) {
                logger.error("No se pudo obtener el ID del asesor de la sesión");
                return;
            }
            
            // Verificar si el asesor tiene préstamos asignados
            if (!prestamoService.tienePrestamosAsignados(idAsesor)) {
                logger.warn("El asesor " + idAsesor + " no tiene préstamos asignados. No se mostrarán cuotas.");
                tabla.getItems().clear();
                return;
            }
            
            // Obtener cuotas del día filtradas por asesor
            List<Cronograma> cronogramas = prestamoService.obtenerCuotasDelDiaPorAsesor(idAsesor);
            List<CuotaDiaInfo> cuotas = new ArrayList<>();
            
            for (Cronograma cronograma : cronogramas) {
                CuotaDiaInfo cuotaInfo = convertirACuotaDiaInfo(cronograma);
                if (cuotaInfo != null) {
                    cuotas.add(cuotaInfo);
                }
            }
            
            // CORRECCIÓN: No mostrar datos de ejemplo, solo datos reales
            if (cuotas.isEmpty()) {
                logger.info("No hay cuotas del día para el asesor " + idAsesor + " en la base de datos");
            }
            
            tabla.getItems().clear();
            tabla.getItems().addAll(cuotas);
            
            // Actualizar estadísticas
            actualizarEstadisticasCuotasDia(statTotal, statMonto, statPagadas, statPendientes, cronogramas);
            
            logger.info("Cargadas " + cuotas.size() + " cuotas del día");
            
        } catch (Exception e) {
            logger.error("Error al actualizar cuotas del día", e);
            mostrarError("Error al cargar las cuotas del día: " + e.getMessage());
        }
    }
    
    /**
     * Actualiza las estadísticas de cuotas del día
     */
    private void actualizarEstadisticasCuotasDia(VBox statTotal, VBox statMonto, VBox statPagadas, VBox statPendientes, List<Cronograma> cronogramas) {
        try {
            int totalCuotas = cronogramas.size();
            double montoTotal = 0.0;
            int cuotasPagadas = 0;
            int cuotasPendientes = 0;
            
            for (Cronograma cronograma : cronogramas) {
                montoTotal += cronograma.getMontoCuota().doubleValue();
                
                if (cronograma.getEstadoCuota() == Cronograma.EstadoCuota.PAGADA) {
                    cuotasPagadas++;
                } else {
                    cuotasPendientes++;
                }
            }
            
            // CORRECCIÓN: Mostrar valores reales, no datos de ejemplo
                actualizarEstadistica(statTotal, String.valueOf(totalCuotas));
                actualizarEstadistica(statMonto, String.format("S/ %.2f", montoTotal));
                actualizarEstadistica(statPagadas, String.valueOf(cuotasPagadas));
                actualizarEstadistica(statPendientes, String.valueOf(cuotasPendientes));
            
        } catch (Exception e) {
            logger.error("Error al actualizar estadísticas de cuotas del día", e);
            // CORRECCIÓN: Mostrar valores en cero en caso de error, no datos de ejemplo
            actualizarEstadistica(statTotal, "0");
            actualizarEstadistica(statMonto, "S/ 0.00");
            actualizarEstadistica(statPagadas, "0");
            actualizarEstadistica(statPendientes, "0");
        }
    }
    
    /**
     * Actualiza el valor de una estadística
     */
    private void actualizarEstadistica(VBox stat, String nuevoValor) {
        if (stat != null && stat.getChildren().size() >= 3) {
            Label valorLabel = (Label) stat.getChildren().get(2);
            valorLabel.setText(nuevoValor);
        }
    }
    
    /**
     * Convierte una entidad Cronograma a CuotaDiaInfo
     */
    private CuotaDiaInfo convertirACuotaDiaInfo(Cronograma cronograma) {
        try {
            // Obtener información del cliente
            String nombreCliente = "Cliente no encontrado";
            String dniCliente = "N/A";
            
            if (cronograma.getPrestamo() != null && cronograma.getPrestamo().getIdCliente() != null) {
                Optional<Cliente> clienteOpt = clienteService.obtenerClientePorId(cronograma.getPrestamo().getIdCliente());
                if (clienteOpt.isPresent()) {
                    Cliente cliente = clienteOpt.get();
                    nombreCliente = cliente.getNombre() + " " + cliente.getApellido();
                    dniCliente = cliente.getIdCliente().toString();
                }
            }
            
            // Formatear monto
            String montoFormateado = String.format("S/ %.2f", cronograma.getMontoCuota());
            
            // Determinar estado
            String estado;
            if (cronograma.getEstadoCuota() == Cronograma.EstadoCuota.PAGADA) {
                estado = "Pagada";
            } else if (cronograma.isVencida()) {
                estado = "Vencida";
            } else {
                estado = "Pendiente";
            }
            
            return new CuotaDiaInfo(nombreCliente, dniCliente, montoFormateado, estado, 
                                   cronograma.getPrestamo() != null ? cronograma.getPrestamo().getIdPrestamo() : null, 
                                   cronograma.getIdCuota());
            
        } catch (Exception e) {
            logger.error("Error al convertir cronograma a CuotaDiaInfo", e);
            return null;
        }
    }
    
    
    /**
     * Registra el pago de una cuota
     */
    private void registrarPagoCuota(CuotaDiaInfo cuota) {
        try {
            // Usar el ID de la cuota directamente para buscar en la base de datos
            Cronograma cronogramaCompleto = null;
            
            if (cuota.getIdCuota() != null) {
                // Buscar directamente por ID de cuota
                List<Cronograma> cronogramas = prestamoService.obtenerCuotasDelDia();
                for (Cronograma cronograma : cronogramas) {
                    if (cronograma.getIdCuota().equals(cuota.getIdCuota())) {
                        cronogramaCompleto = cronograma;
                        break;
                    }
                }
            }
            
            // Si no se encuentra por ID, usar el método anterior como fallback
            if (cronogramaCompleto == null) {
                List<Cronograma> cronogramas = prestamoService.obtenerCuotasDelDia();
                
                // Buscar la cuota que corresponde al cliente y DNI mostrado
                for (Cronograma cronograma : cronogramas) {
                    if (cronograma.getPrestamo() != null && cronograma.getPrestamo().getIdCliente() != null) {
                        Optional<Cliente> clienteOpt = clienteService.obtenerClientePorId(cronograma.getPrestamo().getIdCliente());
                        if (clienteOpt.isPresent()) {
                            Cliente cliente = clienteOpt.get();
                            String nombreCompleto = cliente.getNombre() + " " + cliente.getApellido();
                            String dni = cliente.getIdCliente().toString();
                            
                            if (nombreCompleto.equals(cuota.getCliente()) && dni.equals(cuota.getDni())) {
                                cronogramaCompleto = cronograma;
                                break;
                            }
                        }
                    }
                }
            }
            
            if (cronogramaCompleto == null) {
                mostrarError("No se pudo encontrar la información completa de la cuota");
                return;
            }
            
            // Verificar que la cuota esté pendiente
            if (cronogramaCompleto.getEstadoCuota() != Cronograma.EstadoCuota.PENDIENTE) {
                String mensajeError = String.format(
                    "Esta cuota ya ha sido procesada.\n\n" +
                    "📋 Información de la cuota:\n" +
                    "• ID Cuota: %d\n" +
                    "• ID Préstamo: %d\n" +
                    "• Cliente: %s\n" +
                    "• Estado actual: %s\n" +
                    "• Validación asesor: %s",
                    cronogramaCompleto.getIdCuota(),
                    cronogramaCompleto.getPrestamo() != null ? cronogramaCompleto.getPrestamo().getIdPrestamo() : "N/A",
                    cuota.getCliente(),
                    cronogramaCompleto.getEstadoCuota().getDescripcion(),
                    cronogramaCompleto.isValidacionAsesor() ? "Sí" : "No"
                );
                mostrarError(mensajeError);
                return;
            }
            
            // Verificar si ya existe una recaudación para esta cuota
            if (existeRecaudacionParaCuota(cronogramaCompleto.getIdCuota())) {
                String mensajeError = String.format(
                    "Ya existe una recaudación registrada para esta cuota.\n\n" +
                    "📋 Información de la cuota:\n" +
                    "• ID Cuota: %d\n" +
                    "• ID Préstamo: %d\n" +
                    "• Cliente: %s\n" +
                    "• Estado: %s\n" +
                    "• Validación asesor: %s\n\n" +
                    "No se puede procesar nuevamente.",
                    cronogramaCompleto.getIdCuota(),
                    cronogramaCompleto.getPrestamo() != null ? cronogramaCompleto.getPrestamo().getIdPrestamo() : "N/A",
                    cuota.getCliente(),
                    cronogramaCompleto.getEstadoCuota().getDescripcion(),
                    cronogramaCompleto.isValidacionAsesor() ? "Sí" : "No"
                );
                mostrarError(mensajeError);
                return;
            }
            
            // Mostrar ventana modal de confirmación de pago
            mostrarVentanaConfirmacionPago(cronogramaCompleto);
            
        } catch (Exception e) {
            logger.error("Error al registrar pago de cuota", e);
            mostrarError("Error al registrar el pago: " + e.getMessage());
        }
    }
    
    /**
     * Verifica si ya existe una recaudación para la cuota específica
     */
    private boolean existeRecaudacionParaCuota(Long idCuota) {
        try {
            // Buscar la cuota en las cuotas del día y verificar su validación
            List<Cronograma> cronogramas = prestamoService.obtenerCuotasDelDia();
            for (Cronograma cronograma : cronogramas) {
                if (cronograma.getIdCuota().equals(idCuota)) {
                    return cronograma.isValidacionAsesor();
                }
            }
            return false;
        } catch (Exception e) {
            logger.error("Error al verificar recaudación para cuota: " + idCuota, e);
            return false;
        }
    }
    
    /**
     * Muestra una ventana modal para confirmar el pago de la cuota
     */
    private void mostrarVentanaConfirmacionPago(Cronograma cronograma) {
        try {
            // Crear la ventana modal
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setTitle("Confirmar Pago de Cuota");
            modalStage.setResizable(true);
            modalStage.setMinWidth(600);
            modalStage.setMinHeight(500);
            modalStage.setWidth(700);
            modalStage.setHeight(600);
            modalStage.centerOnScreen();
            
            // Crear el contenido principal
            VBox contenido = new VBox(20);
            contenido.setPadding(new Insets(25));
            contenido.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 12px;");
            
            // Crear ScrollPane para hacer el contenido scrolleable
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setContent(contenido);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1px; -fx-border-radius: 12px;");
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setPannable(true); // Permite arrastrar para hacer scroll
            
            // Título
            Label titulo = new Label("💰 Confirmar Pago de Cuota");
            titulo.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 15 0;");
            
            // Obtener información del cliente
            String nombreCliente = "Cliente no encontrado";
            if (cronograma.getPrestamo() != null && cronograma.getPrestamo().getIdCliente() != null) {
                Optional<Cliente> clienteOpt = clienteService.obtenerClientePorId(cronograma.getPrestamo().getIdCliente());
                if (clienteOpt.isPresent()) {
                    Cliente cliente = clienteOpt.get();
                    nombreCliente = cliente.getNombre() + " " + cliente.getApellido();
                }
            }
            
            // Información de la cuota
            VBox infoCuota = new VBox(10);
            infoCuota.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8px; -fx-padding: 15; -fx-border-color: #e2e8f0; -fx-border-width: 1px; -fx-border-radius: 8px;");
            
            Label infoTitulo = new Label("📋 Información del Pago");
            infoTitulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
            
            Label lblCliente = new Label("👤 Cliente: " + nombreCliente);
            lblCliente.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151;");
            
            Label lblCuota = new Label("🔢 Cuota #" + cronograma.getNumeroCuota());
            lblCuota.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151;");
            
            Label lblMonto = new Label("💰 Monto: S/ " + String.format("%.2f", cronograma.getMontoCuota()));
            lblMonto.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151; -fx-font-weight: bold;");
            
            Label lblFecha = new Label("📅 Fecha Programada: " + cronograma.getFechaProgramada().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            lblFecha.setStyle("-fx-font-size: 14px; -fx-text-fill: #374151;");
            
            infoCuota.getChildren().addAll(infoTitulo, lblCliente, lblCuota, lblMonto, lblFecha);
            
            // Campos de entrada
            VBox camposEntrada = new VBox(15);
            
            // Fecha de pago
            HBox fechaPago = new HBox(10);
            fechaPago.setAlignment(Pos.CENTER_LEFT);
            Label lblFechaPago = new Label("📅 Fecha de Pago:");
            lblFechaPago.setStyle("-fx-font-weight: bold; -fx-min-width: 120px;");
            DatePicker dpFechaPago = new DatePicker(DateTimeUtil.today());
            dpFechaPago.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1d5db; -fx-border-width: 1px; -fx-border-radius: 4px;");
            fechaPago.getChildren().addAll(lblFechaPago, dpFechaPago);
            
            // Método de pago
            HBox metodoPago = new HBox(10);
            metodoPago.setAlignment(Pos.CENTER_LEFT);
            Label lblMetodoPago = new Label("💳 Método:");
            lblMetodoPago.setStyle("-fx-font-weight: bold; -fx-min-width: 120px;");
            ComboBox<String> cmbMetodoPago = new ComboBox<>();
            cmbMetodoPago.getItems().addAll("Efectivo", "Transferencia", "Yape", "Plin", "Otro");
            cmbMetodoPago.setValue("Efectivo");
            cmbMetodoPago.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1d5db; -fx-border-width: 1px; -fx-border-radius: 4px; -fx-min-width: 150px;");
            metodoPago.getChildren().addAll(lblMetodoPago, cmbMetodoPago);
            
            // Referencia
            HBox referencia = new HBox(10);
            referencia.setAlignment(Pos.CENTER_LEFT);
            Label lblReferencia = new Label("🔗 Referencia:");
            lblReferencia.setStyle("-fx-font-weight: bold; -fx-min-width: 120px;");
            TextField txtReferencia = new TextField();
            txtReferencia.setPromptText("Opcional");
            txtReferencia.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1d5db; -fx-border-width: 1px; -fx-border-radius: 4px; -fx-min-width: 200px;");
            referencia.getChildren().addAll(lblReferencia, txtReferencia);
            
            // Observaciones
            HBox observaciones = new HBox(10);
            observaciones.setAlignment(Pos.CENTER_LEFT);
            Label lblObservaciones = new Label("📝 Observaciones:");
            lblObservaciones.setStyle("-fx-font-weight: bold; -fx-min-width: 120px;");
            TextField txtObservaciones = new TextField();
            txtObservaciones.setPromptText("Opcional");
            txtObservaciones.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d1d5db; -fx-border-width: 1px; -fx-border-radius: 4px; -fx-min-width: 200px;");
            observaciones.getChildren().addAll(lblObservaciones, txtObservaciones);
            
            camposEntrada.getChildren().addAll(fechaPago, metodoPago, referencia, observaciones);
            
            // Botones de acción
            HBox botonesAccion = new HBox(15);
            botonesAccion.setAlignment(Pos.CENTER);
            
            Button btnCancelar = new Button("❌ Cancelar");
            btnCancelar.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 8px; -fx-cursor: hand;");
            btnCancelar.setOnAction(e -> modalStage.close());
            
            Button btnConfirmar = new Button("✅ Confirmar Pago");
            btnConfirmar.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 8px; -fx-cursor: hand;");
            btnConfirmar.setOnAction(e -> {
                try {
                    // Validar campos obligatorios
                    if (cmbMetodoPago.getValue() == null) {
                        mostrarError("Debe seleccionar un método de pago");
                        return;
                    }
                    
                    // Procesar el pago
                    procesarPagoCuota(cronograma, dpFechaPago.getValue(), cmbMetodoPago.getValue(), 
                                    txtReferencia.getText().trim(), txtObservaciones.getText().trim());
                    
                    modalStage.close();
                } catch (Exception ex) {
                    logger.error("Error al confirmar pago", ex);
                    mostrarError("Error al procesar el pago: " + ex.getMessage());
                }
            });
            
            botonesAccion.getChildren().addAll(btnCancelar, btnConfirmar);
            
            // Agregar todo al contenido
            contenido.getChildren().addAll(titulo, infoCuota, camposEntrada, botonesAccion);
            
            // Crear la escena y mostrar la ventana
            Scene scene = new Scene(scrollPane);
            modalStage.setScene(scene);
            modalStage.showAndWait();
            
        } catch (Exception e) {
            logger.error("Error al mostrar ventana de confirmación de pago", e);
            mostrarError("Error al mostrar la ventana de confirmación");
        }
    }
    
    /**
     * Procesa el pago de la cuota con todas las validaciones y actualizaciones
     */
    private void procesarPagoCuota(Cronograma cronograma, LocalDate fechaPago, String metodoPago, String referencia, String observaciones) {
        try {
            // Obtener información necesaria
            Long idAsesor = SessionManager.getInstance().getAsesorId();
            if (idAsesor == null) {
                mostrarError("No se pudo obtener el ID del asesor. Por favor, inicie sesión nuevamente.");
                return;
            }
            
            // Obtener información del cliente y préstamo
            Cliente cliente = null;
            if (cronograma.getPrestamo() != null && cronograma.getPrestamo().getIdCliente() != null) {
                Optional<Cliente> clienteOpt = clienteService.obtenerClientePorId(cronograma.getPrestamo().getIdCliente());
                if (clienteOpt.isPresent()) {
                    cliente = clienteOpt.get();
                }
            }
            
            if (cliente == null) {
                mostrarError("No se pudo obtener la información del cliente");
                return;
            }
            
            // PASO 1: Registrar en tabla recaudacion_asesor (borrador con validado = 0)
            boolean recaudacionRegistrada = recaudacionService.registrarBorradorParaCuota(
                idAsesor,                                    // ID del asesor actual
                cliente.getIdCliente(),                      // ID del cliente
                cronograma.getPrestamo().getIdPrestamo(),    // ID del préstamo
                cronograma.getMontoCuota(),                  // Monto cobrado
                cronograma.getIdCuota(),                     // ID de la cuota específica
                fechaPago,                                   // Fecha de pago
                metodoPago,                                  // Método de pago
                referencia,                                  // Referencia
                observaciones                                // Observaciones
            );
            
            if (recaudacionRegistrada) {
                // PASO 2: Marcar la cuota como validada por el asesor (evita duplicados)
                cronogramaDAO.marcarValidacionAsesor(cronograma.getIdCuota(), true);
                
                // PASO 3: Mostrar mensaje de éxito
                mostrarInfo("✅ Pago registrado exitosamente\n\n" +
                    "📋 Resumen:\n" +
                    "• Cliente: " + cliente.getNombre() + " " + cliente.getApellido() + "\n" +
                    "• Cuota #" + cronograma.getNumeroCuota() + "\n" +
                    "• Monto: S/ " + String.format("%.2f", cronograma.getMontoCuota()) + "\n" +
                    "• Fecha: " + fechaPago.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\n" +
                    "• Método: " + metodoPago + "\n\n" +
                    "⚠️ IMPORTANTE: Esta recaudación está registrada con validado = 0.\n" +
                    "La cuota se marcará como pagada SOLO cuando el administrador valide (validado = 1).");
                
                logger.info("Pago registrado exitosamente - Cliente: " + cliente.getIdCliente() + 
                           ", Cuota: " + cronograma.getIdCuota() + ", Monto: " + cronograma.getMontoCuota());
                
                // PASO 4: Refrescar la vista de cuotas del día
                refrescarVistaCuotasDelDia();
                
            } else {
                mostrarError("No se pudo registrar el pago. Contacte al administrador.");
            }
            
        } catch (Exception e) {
            logger.error("Error al procesar pago de cuota", e);
            mostrarError("Error al procesar el pago: " + e.getMessage());
        }
    }
    
    /**
     * Refresca la vista de cuotas del día
     */
    private void refrescarVistaCuotasDelDia() {
        try {
            // Si estamos en la vista de cuotas del día, refrescar los datos
            if (contentArea.getChildren().size() > 0) {
                // Buscar si hay una tabla de cuotas en el contenido actual
                // Esto es una implementación básica - podrías mejorarla según tus necesidades
                logger.info("Vista de cuotas del día refrescada");
            }
        } catch (Exception e) {
            logger.error("Error al refrescar vista de cuotas del día", e);
        }
    }
    
    /**
     * Muestra el detalle de una cuota
     */
    private void verDetalleCuota(CuotaDiaInfo cuota) {
        try {
            // Buscar la cuota completa en la base de datos
            List<Cronograma> cronogramas = prestamoService.obtenerCuotasDelDia();
            Cronograma cronogramaCompleto = null;
            
            // Buscar la cuota que corresponde al cliente y DNI mostrado
            for (Cronograma cronograma : cronogramas) {
                if (cronograma.getPrestamo() != null && cronograma.getPrestamo().getIdCliente() != null) {
                    Optional<Cliente> clienteOpt = clienteService.obtenerClientePorId(cronograma.getPrestamo().getIdCliente());
                    if (clienteOpt.isPresent()) {
                        Cliente cliente = clienteOpt.get();
                        String nombreCompleto = cliente.getNombre() + " " + cliente.getApellido();
                        String dni = cliente.getIdCliente().toString();
                        
                        if (nombreCompleto.equals(cuota.getCliente()) && dni.equals(cuota.getDni())) {
                            cronogramaCompleto = cronograma;
                            break;
                        }
                    }
                }
            }
            
            if (cronogramaCompleto != null) {
                mostrarDetalleCuotaModal(cronogramaCompleto);
            } else {
                mostrarError("No se pudo encontrar la información completa de la cuota");
            }
            
        } catch (Exception e) {
            logger.error("Error al mostrar detalle de cuota", e);
            mostrarError("Error al mostrar el detalle de la cuota");
        }
    }
    
    /**
     * Muestra una ventana modal con el detalle completo de la cuota
     */
    private void mostrarDetalleCuotaModal(Cronograma cronograma) {
        try {
            // Crear la ventana modal
            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.setTitle("Detalle de Cuota");
            modalStage.setResizable(true);
            modalStage.setMinWidth(700);
            modalStage.setMinHeight(600);
            modalStage.setWidth(900);
            modalStage.setHeight(750);
            modalStage.centerOnScreen(); // Centrar la ventana en la pantalla
            
            // Crear el contenido principal
            VBox contenido = new VBox(20);
            contenido.setPadding(new Insets(25));
            contenido.setStyle("-fx-background-color: #ffffff; -fx-background-radius: 12px;");
            
            // Crear ScrollPane para hacer el contenido scrolleable
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setContent(contenido);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setStyle("-fx-background-color: #f8fafc; -fx-border-color: #e2e8f0; -fx-border-width: 1px; -fx-border-radius: 12px;");
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setPannable(true); // Permite arrastrar para hacer scroll
            
            // Título
            Label titulo = new Label("📋 Detalle de Cuota");
            titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 20 0;");
            
            // Obtener información del cliente
            String nombreCliente = "Cliente no encontrado";
            String dniCliente = "N/A";
            String telefonoCliente = "N/A";
            String direccionCliente = "N/A";
            
            if (cronograma.getPrestamo() != null && cronograma.getPrestamo().getIdCliente() != null) {
                Optional<Cliente> clienteOpt = clienteService.obtenerClientePorId(cronograma.getPrestamo().getIdCliente());
                if (clienteOpt.isPresent()) {
                    Cliente cliente = clienteOpt.get();
                    nombreCliente = cliente.getNombre() + " " + cliente.getApellido();
                    dniCliente = cliente.getIdCliente().toString();
                    telefonoCliente = cliente.getTelefono() != null ? cliente.getTelefono() : "N/A";
                    direccionCliente = cliente.getDireccion() != null ? cliente.getDireccion() : "N/A";
                }
            }
            
            // Información de la cuota
            VBox infoCuota = new VBox(15);
            infoCuota.setStyle("-fx-background-color: #f8fafc; -fx-background-radius: 8px; -fx-padding: 20; -fx-border-color: #e2e8f0; -fx-border-width: 1px; -fx-border-radius: 8px;");
            
            Label tituloInfo = new Label("📊 Información de la Cuota");
            tituloInfo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-padding: 0 0 15 0;");
            
            // Crear grid para la información
            GridPane gridInfo = new GridPane();
            gridInfo.setHgap(20);
            gridInfo.setVgap(10);
            
            // Información del cliente
            gridInfo.add(crearLabelInfo("👤 Cliente:", nombreCliente), 0, 0);
            gridInfo.add(crearLabelInfo("🆔 DNI:", dniCliente), 1, 0);
            gridInfo.add(crearLabelInfo("📞 Teléfono:", telefonoCliente), 0, 1);
            gridInfo.add(crearLabelInfo("📍 Dirección:", direccionCliente), 1, 1);
            
            // Información de la cuota
            gridInfo.add(crearLabelInfo("🔢 Número de Cuota:", String.valueOf(cronograma.getNumeroCuota())), 0, 2);
            gridInfo.add(crearLabelInfo("💰 Monto:", String.format("S/ %.2f", cronograma.getMontoCuota())), 1, 2);
            gridInfo.add(crearLabelInfo("📅 Fecha Programada:", cronograma.getFechaProgramada().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))), 0, 3);
            gridInfo.add(crearLabelInfo("📊 Estado:", cronograma.getEstadoCuota().getDescripcion()), 1, 3);
            
            // Información adicional
            if (cronograma.getFechaPagoReal() != null) {
                gridInfo.add(crearLabelInfo("✅ Fecha de Pago:", cronograma.getFechaPagoReal().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))), 0, 4);
            }
            
            if (cronograma.isVencida()) {
                gridInfo.add(crearLabelInfo("⚠️ Días de Atraso:", String.valueOf(cronograma.getDiasAtraso())), 1, 4);
            }
            
            infoCuota.getChildren().addAll(tituloInfo, gridInfo);
            
            // Información del préstamo
            VBox infoPrestamo = new VBox(15);
            infoPrestamo.setStyle("-fx-background-color: #f0f9ff; -fx-background-radius: 8px; -fx-padding: 20; -fx-border-color: #0ea5e9; -fx-border-width: 1px; -fx-border-radius: 8px;");
            
            Label tituloPrestamo = new Label("🏦 Información del Préstamo");
            tituloPrestamo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0c4a6e; -fx-padding: 0 0 15 0;");
            
            GridPane gridPrestamo = new GridPane();
            gridPrestamo.setHgap(20);
            gridPrestamo.setVgap(10);
            
            if (cronograma.getPrestamo() != null) {
                // Obtener información completa del préstamo usando el DAO directamente
                Prestamo prestamoCompleto = null;
                try {
                    prestamoCompleto = prestamoService.obtenerPrestamoPorId(cronograma.getPrestamo().getIdPrestamo());
                } catch (Exception e) {
                    logger.error("Error al obtener información completa del préstamo", e);
                }
                
                gridPrestamo.add(crearLabelInfo("🆔 ID Préstamo:", String.valueOf(cronograma.getPrestamo().getIdPrestamo())), 0, 0);
                
                if (prestamoCompleto != null) {
                    gridPrestamo.add(crearLabelInfo("💵 Monto Solicitado:", String.format("S/ %.2f", prestamoCompleto.getMontoSolicitado())), 1, 0);
                    gridPrestamo.add(crearLabelInfo("📈 Tasa de Interés:", String.format("%.2f%%", prestamoCompleto.getTasaInteres())), 0, 1);
                    gridPrestamo.add(crearLabelInfo("📅 Período:", prestamoCompleto.getPeriodoMeses() + " meses"), 1, 1);
                } else {
                    gridPrestamo.add(crearLabelInfo("💵 Monto Solicitado:", "No disponible"), 1, 0);
                    gridPrestamo.add(crearLabelInfo("📈 Tasa de Interés:", "No disponible"), 0, 1);
                    gridPrestamo.add(crearLabelInfo("📅 Período:", "No disponible"), 1, 1);
                }
            }
            
            infoPrestamo.getChildren().addAll(tituloPrestamo, gridPrestamo);
            
            // Botones de acción
            HBox botonesAccion = new HBox(15);
            botonesAccion.setAlignment(Pos.CENTER);
            
            Button btnCerrar = new Button("❌ Cerrar");
            btnCerrar.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 8px; -fx-cursor: hand;");
            btnCerrar.setOnAction(e -> modalStage.close());
            
            Button btnImprimir = new Button("🖨️ Imprimir");
            btnImprimir.setStyle("-fx-background-color: #3b82f6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 8px; -fx-cursor: hand;");
            btnImprimir.setOnAction(e -> imprimirDetalleCuota(cronograma));
            
            if (cronograma.getEstadoCuota() == Cronograma.EstadoCuota.PENDIENTE) {
                Button btnPagar = new Button("💰 Registrar Pago");
                btnPagar.setStyle("-fx-background-color: #10b981; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 20; -fx-background-radius: 8px; -fx-cursor: hand;");
                btnPagar.setOnAction(e -> {
                    modalStage.close();
                    registrarPagoCuotaDesdeDetalle(cronograma);
                });
                botonesAccion.getChildren().addAll(btnCerrar, btnImprimir, btnPagar);
            } else {
                botonesAccion.getChildren().addAll(btnCerrar, btnImprimir);
            }
            
            // Agregar todo al contenido
            contenido.getChildren().addAll(titulo, infoCuota, infoPrestamo, botonesAccion);
            
            // Crear la escena y mostrar la ventana
            Scene scene = new Scene(scrollPane, 800, 700);
            modalStage.setScene(scene);
            modalStage.showAndWait();
            
        } catch (Exception e) {
            logger.error("Error al mostrar modal de detalle", e);
            mostrarError("Error al mostrar el detalle de la cuota");
        }
    }
    
    /**
     * Crea un label con información formateada
     */
    private HBox crearLabelInfo(String etiqueta, String valor) {
        HBox contenedor = new HBox(5);
        
        Label lblEtiqueta = new Label(etiqueta);
        lblEtiqueta.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151; -fx-min-width: 120px;");
        
        Label lblValor = new Label(valor);
        lblValor.setStyle("-fx-text-fill: #1f2937;");
        
        contenedor.getChildren().addAll(lblEtiqueta, lblValor);
        return contenedor;
    }
    
    /**
     * Imprime el detalle de la cuota
     */
    private void imprimirDetalleCuota(Cronograma cronograma) {
        try {
            mostrarInfo("Funcionalidad de impresión en desarrollo");
            // TODO: Implementar impresión
        } catch (Exception e) {
            logger.error("Error al imprimir detalle", e);
            mostrarError("Error al imprimir el detalle");
        }
    }
    
    /**
     * Registra el pago de una cuota desde el detalle
     */
    private void registrarPagoCuotaDesdeDetalle(Cronograma cronograma) {
        try {
            // TODO: Implementar registro de pago desde detalle
            mostrarInfo("Funcionalidad de registro de pago desde detalle en desarrollo");
        } catch (Exception e) {
            logger.error("Error al registrar pago desde detalle", e);
            mostrarError("Error al registrar el pago");
        }
    }
    
    /**
     * Exporta las cuotas del día
     */
    private void exportarCuotasDia() {
        try {
            mostrarInfo("Exportando cuotas del día...");
            // TODO: Implementar exportación
        } catch (Exception e) {
            logger.error("Error al exportar", e);
            mostrarError("Error al exportar las cuotas");
        }
    }
    
    /**
     * Clase para representar información de cuota del día
     */
    public static class CuotaDiaInfo {
        private final String cliente;
        private final String dni;
        private final String monto;
        private final String estado;
        private final Long idPrestamo;
        private final Long idCuota;
        
        public CuotaDiaInfo(String cliente, String dni, String monto, String estado, Long idPrestamo, Long idCuota) {
            this.cliente = cliente;
            this.dni = dni;
            this.monto = monto;
            this.estado = estado;
            this.idPrestamo = idPrestamo;
            this.idCuota = idCuota;
        }
        
        public String getCliente() { return cliente; }
        public String getDni() { return dni; }
        public String getMonto() { return monto; }
        public String getEstado() { return estado; }
        public Long getIdPrestamo() { return idPrestamo; }
        public Long getIdCuota() { return idCuota; }
    }
    
    /**
     * Maneja la opción de ver cuotas vencidas
     */
    @FXML
    private void handleVerCuotasVencidas() {
        try {
            logger.info("Botón Ver Detalles de cuotas vencidas presionado");
            
            // Crear una ventana de detalles de cuotas vencidas
            mostrarDetallesCuotasVencidas();
            
        } catch (Exception e) {
            logger.error("Error al mostrar cuotas vencidas", e);
            mostrarError("Error al mostrar las cuotas vencidas: " + e.getMessage());
        }
    }
    
    /**
     * Muestra los detalles de las cuotas vencidas en una nueva ventana
     */
    private void mostrarDetallesCuotasVencidas() {
        try {
            logger.info("Iniciando creación de ventana de detalles de cuotas vencidas");
            
            // Crear una nueva ventana para mostrar los detalles
            Stage detallesStage = new Stage();
            detallesStage.setTitle("Detalles de Cuotas Vencidas");
            detallesStage.setWidth(900);
            detallesStage.setHeight(700);
            
            // Crear tabla para mostrar las cuotas vencidas
            TableView<Cronograma> tablaCuotas = new TableView<>();
            
            // Columnas básicas
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
                long diasRetraso = java.time.temporal.ChronoUnit.DAYS.between(fechaProgramada, DateTimeUtil.today());
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
            
            // Obtener cuotas vencidas del asesor actual
            PrestamoService prestamoService = new PrestamoService();
            Long idAsesorActual = SessionManager.getInstance().getAsesorId();
            List<Cronograma> cuotasVencidas = prestamoService.obtenerCuotasVencidasPorAsesor(idAsesorActual);
            
            // Cargar datos
            ObservableList<Cronograma> cuotasObservable = FXCollections.observableArrayList(cuotasVencidas);
            tablaCuotas.setItems(cuotasObservable);
            
            // Crear layout
            VBox layout = new VBox(10);
            layout.setPadding(new javafx.geometry.Insets(10));
            
            Label titulo = new Label("Cuotas Vencidas del Asesor - Total: " + cuotasVencidas.size());
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
            logger.info("Ventana de detalles de cuotas vencidas mostrada exitosamente");
            
        } catch (Exception e) {
            logger.error("Error al mostrar detalles de cuotas vencidas", e);
            mostrarError("Error al mostrar los detalles de cuotas vencidas: " + e.getMessage());
        }
    }
    
    /**
     * Maneja la opción de ver recaudación del día
     */
    @FXML
    private void handleVerRecaudacionDia() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/asesor/RecaudacionDiaView.fxml"));
            VBox recaudacionDiaView = loader.load();
            
            // Reemplazar contenido
            contentArea.getChildren().clear();
            contentArea.getChildren().add(recaudacionDiaView);
            
            logger.info("Cargada recaudación del día");
            
        } catch (IOException e) {
            logger.error("Error al cargar recaudación del día", e);
            mostrarError("Error al cargar la recaudación del día");
        }
    }
    
    /**
     * Maneja la opción de ver recaudación del mes
     */
    @FXML
    private void handleVerRecaudacionMes() {
        try {
            Long idAsesor = SessionManager.getInstance().getAsesorId();
            if (idAsesor == null) {
                mostrarError("No se pudo obtener el ID del asesor");
                return;
            }
            
            // Crear vista de recaudación del mes
            VBox recaudacionMesView = crearRecaudacionMesView(idAsesor);
            
            // Reemplazar contenido
            contentArea.getChildren().clear();
            contentArea.getChildren().add(recaudacionMesView);
            
            logger.info("Cargada recaudación del mes para asesor: " + idAsesor);
            
        } catch (Exception e) {
            logger.error("Error al cargar recaudación del mes", e);
            mostrarError("Error al cargar la recaudación del mes: " + e.getMessage());
        }
    }
    
    /**
     * Crea la vista de recaudación del mes
     */
    private VBox crearRecaudacionMesView(Long idAsesor) {
        VBox contenedor = new VBox(20);
        contenedor.setPadding(new Insets(25));
        
        // Título
        Label titulo = new Label("📊 Recaudación del Mes - " + DateTimeUtil.today().format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        titulo.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 20 0;");
        
        // Panel de información
        VBox panelInfo = new VBox(15);
        panelInfo.setStyle("-fx-background-color: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%); -fx-background-radius: 16px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 15, 0, 0, 5); -fx-border-color: rgba(226,232,240,0.5); -fx-border-width: 1px; -fx-border-radius: 16px; -fx-padding: 25;");
        
        // Obtener datos de recaudación del mes
        BigDecimal recaudacionTotal = pagoService.calcularRecaudacionMesActual(idAsesor);
        
        // Estadísticas
        HBox estadisticas = new HBox(30);
        estadisticas.setAlignment(Pos.CENTER);
        
        VBox statTotal = crearEstadistica("💰", "Total Recaudado", "S/ " + String.format("%.2f", recaudacionTotal), "#10b981");
        VBox statSueldo = crearEstadistica("💼", "Sueldo Estimado", "S/ " + String.format("%.2f", recaudacionTotal.doubleValue() * 0.10), "#3b82f6");
        
        estadisticas.getChildren().addAll(statTotal, statSueldo);
        
        // Información adicional
        Label infoLabel = new Label("Esta recaudación incluye todos los pagos validados y aprobados por el administrador durante el mes actual.");
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280; -fx-wrap-text: true;");
        
        panelInfo.getChildren().addAll(estadisticas, infoLabel);
        
        // Botón de regreso
        HBox botones = new HBox(10);
        botones.setAlignment(Pos.CENTER);
        
        Button btnRegresar = new Button("← Regresar al Dashboard");
        btnRegresar.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; -fx-font-weight: 600; -fx-padding: 10 20; -fx-background-radius: 8px; -fx-cursor: hand;");
        btnRegresar.setOnAction(e -> mostrarDashboard());
        
        botones.getChildren().add(btnRegresar);
        
        contenedor.getChildren().addAll(titulo, panelInfo, botones);
        
        return contenedor;
    }
    
    /**
     * Muestra el dashboard principal
     */
    private void mostrarDashboard() {
        try {
            VBox dashboardView = crearDashboardView();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(dashboardView);
            
            // CORRECCIÓN: Recargar los datos del dashboard después de crear la vista
            cargarEstadisticasDashboard();
            
            logger.info("Dashboard mostrado y datos recargados");
        } catch (Exception e) {
            logger.error("Error al mostrar dashboard", e);
            mostrarError("Error al cargar el dashboard");
        }
    }
    
    /**
     * Maneja la opción de ver clientes activos
     */
    @FXML
    private void handleVerClientesActivos() {
        try {
            logger.info("Botón Ver Detalles de clientes activos presionado");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/asesor/GestionClientesView.fxml"));
            VBox gestionClientesView = loader.load();
            
            // Reemplazar contenido
            contentArea.getChildren().clear();
            contentArea.getChildren().add(gestionClientesView);
            
            logger.info("Cargada vista de gestión de clientes");
            
        } catch (IOException e) {
            logger.error("Error al cargar gestión de clientes", e);
            mostrarError("Error al cargar la gestión de clientes: " + e.getMessage());
        }
    }
    
    /**
     * Maneja la opción de ver préstamos activos
     */
    @FXML
    private void handleVerPrestamosActivos() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/asesor/PrestamosActivosView.fxml"));
            VBox prestamosActivosView = loader.load();
            
            // Reemplazar contenido
            contentArea.getChildren().clear();
            contentArea.getChildren().add(prestamosActivosView);
            
            logger.info("Cargados préstamos activos");
            
        } catch (IOException e) {
            logger.error("Error al cargar préstamos activos", e);
            mostrarError("Error al cargar los préstamos activos");
        }
    }
    
    /**
     * Maneja la opción de ver morosidad
     */
    @FXML
    private void handleVerMorosidad() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/asesor/MorosidadView.fxml"));
            VBox morosidadView = loader.load();
            
            // Reemplazar contenido
            contentArea.getChildren().clear();
            contentArea.getChildren().add(morosidadView);
            
            logger.info("Cargada morosidad");
            
        } catch (IOException e) {
            logger.error("Error al cargar morosidad", e);
            mostrarError("Error al cargar la morosidad");
        }
    }
    
    /**
     * Maneja la opción de ver sueldo estimado
     */
    @FXML
    private void handleVerSueldoEstimado() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/asesor/SueldoEstimadoView.fxml"));
            VBox sueldoEstimadoView = loader.load();
            
            // Reemplazar contenido
            contentArea.getChildren().clear();
            contentArea.getChildren().add(sueldoEstimadoView);
            
            logger.info("Cargado sueldo estimado");
            
        } catch (IOException e) {
            logger.error("Error al cargar sueldo estimado", e);
            mostrarError("Error al cargar el sueldo estimado");
        }
    }
    
    /**
     * Maneja la opción de nuevo cliente
     */
    @FXML
    private void handleNuevoCliente() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/asesor/NuevoClienteView.fxml"));
            VBox nuevoClienteView = loader.load();
            
            // Reemplazar contenido
            contentArea.getChildren().clear();
            contentArea.getChildren().add(nuevoClienteView);
            
            logger.info("Cargado nuevo cliente");
            
        } catch (IOException e) {
            logger.error("Error al cargar nuevo cliente", e);
            mostrarError("Error al cargar el formulario de nuevo cliente");
        }
    }
    
    /**
     * Maneja la opción de nueva solicitud
     */
    @FXML
    private void handleNuevaSolicitud() {
        handleSolicitarPrestamo();
    }
    
    /**
     * Maneja la opción de registrar pago
     */
    @FXML
    private void handleRegistrarPago() {
        handleRegistrarCobro();
    }
    
    /**
     * Maneja la opción de simular crédito
     */
    @FXML
    private void handleSimularCredito() {
        handleSimulador();
    }
    
    /**
     * Maneja el cierre de sesión
     */
    @FXML
    private void handleLogout() {
        try {
            // Cerrar sesión
            authService.logout();
            
            // Regresar a la pantalla de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            // Obtener el controlador de login y limpiar los campos
            LoginController loginController = loader.getController();
            loginController.setPrimaryStage(primaryStage);
            loginController.resetForm(); // Limpiar campos del formulario
            
            primaryStage.setTitle("CrediActiva - Iniciar Sesión");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(false);
            primaryStage.centerOnScreen();
            
            logger.info("Asesor cerró sesión");
            
        } catch (IOException e) {
            logger.error("Error al cerrar sesión", e);
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
    
    private boolean mostrarConfirmacion(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
    
    /**
     * Crea la vista de gestión de clientes en Java puro
     */
    private VBox crearGestionClientesView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Título
        Label titulo = new Label("Gestión de Clientes");
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Barra de búsqueda
        HBox barraBusqueda = new HBox(10);
        barraBusqueda.setAlignment(Pos.CENTER_LEFT);
        
        TextField txtBuscar = new TextField();
        txtBuscar.setPromptText("Nombre, DNI o teléfono");
        txtBuscar.setPrefWidth(200);
        
        Button btnBuscar = new Button("Buscar");
        btnBuscar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");
        
        Button btnLimpiar = new Button("Limpiar");
        btnLimpiar.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white;");
        
        Separator separador = new Separator();
        separador.setOrientation(Orientation.VERTICAL);
        
        Label lblFiltrar = new Label("Filtrar por:");
        
        ComboBox<String> cmbFiltro = new ComboBox<>();
        cmbFiltro.setPromptText("Todos los clientes");
        cmbFiltro.setPrefWidth(150);
        cmbFiltro.setItems(FXCollections.observableArrayList(
            "Todos", "Activos", "Inactivos", "Con préstamos", "Sin préstamos"
        ));
        
        Button btnFiltrar = new Button("Filtrar");
        btnFiltrar.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        
        barraBusqueda.getChildren().addAll(txtBuscar, btnBuscar, btnLimpiar, separador, lblFiltrar, cmbFiltro, btnFiltrar);
        
        // Botones de acción
        HBox botonesAccion = new HBox(10);
        botonesAccion.setAlignment(Pos.CENTER_LEFT);
        
        Button btnNuevoCliente = new Button("Nuevo Cliente");
        btnNuevoCliente.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
        
        // Los asesores no pueden editar ni eliminar clientes
        // Solo pueden ver y crear nuevos clientes
        
        botonesAccion.getChildren().addAll(btnNuevoCliente);
        
        // Tabla de clientes
        TableView<Cliente> tablaClientes = new TableView<>();
        tablaClientes.setPrefHeight(400);
        tablaClientes.setStyle("-fx-background-color: white; -fx-border-color: #bdc3c7;");
        
        // Columnas de la tabla
        TableColumn<Cliente, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(cellData -> {
            Cliente cliente = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                cliente.getNombre() + " " + cliente.getApellido()
            );
        });
        
        TableColumn<Cliente, String> colDni = new TableColumn<>("DNI");
        colDni.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getIdCliente().toString())
        );
        
        TableColumn<Cliente, String> colTelefono = new TableColumn<>("Teléfono");
        colTelefono.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getTelefono())
        );
        
        TableColumn<Cliente, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getEtiquetaCliente().toString())
        );
        
        TableColumn<Cliente, String> colPrestamos = new TableColumn<>("Préstamos");
        colPrestamos.setCellValueFactory(cellData -> {
            try {
                Cliente cliente = cellData.getValue();
                List<Prestamo> prestamos = prestamoService.obtenerPrestamosPorCliente(cliente.getIdCliente());
                return new javafx.beans.property.SimpleStringProperty(String.valueOf(prestamos.size()));
            } catch (Exception e) {
                logger.error("Error al contar préstamos del cliente", e);
                return new javafx.beans.property.SimpleStringProperty("0");
            }
        });
        
        tablaClientes.getColumns().addAll(colNombre, colDni, colTelefono, colEstado, colPrestamos);
        
        // Mensaje de estado
        Label lblEstado = new Label("Total de clientes: 0");
        lblEstado.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        
        // Event handlers
        btnBuscar.setOnAction(e -> buscarClientes(txtBuscar.getText(), tablaClientes, lblEstado));
        btnLimpiar.setOnAction(e -> {
            txtBuscar.clear();
            cmbFiltro.getSelectionModel().clearSelection();
            cargarTodosLosClientes(tablaClientes, lblEstado);
        });
        btnFiltrar.setOnAction(e -> filtrarClientes(cmbFiltro.getValue(), tablaClientes, lblEstado));
        btnNuevoCliente.setOnAction(e -> abrirNuevoCliente());
        
        // Cargar clientes iniciales
        cargarTodosLosClientes(tablaClientes, lblEstado);
        
        root.getChildren().addAll(titulo, barraBusqueda, botonesAccion, tablaClientes, lblEstado);
        
        return root;
    }
    
    /**
     * Crea la vista de solicitar préstamo en Java puro
     */
    private VBox crearSolicitarPrestamoView() {
        VBox root = new VBox(20);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f5f5f5;");
        
        // Título
        Label titulo = new Label("Solicitar Préstamo");
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Formulario principal
        GridPane formulario = new GridPane();
        formulario.setHgap(20);
        formulario.setVgap(15);
        formulario.setPadding(new Insets(20));
        formulario.setStyle("-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-radius: 5;");
        
        // Campos del formulario
        Label lblCliente = new Label("DNI del Cliente:");
        TextField txtDniCliente = new TextField();
        txtDniCliente.setPromptText("Ingrese DNI del cliente");
        txtDniCliente.setPrefWidth(200);
        
        Label lblMonto = new Label("Monto Solicitado:");
        TextField txtMonto = new TextField();
        txtMonto.setPromptText("0.00");
        
        Label lblPlazo = new Label("Plazo:");
        ComboBox<String> cmbPlazo = new ComboBox<>();
        cmbPlazo.setPromptText("Seleccionar plazo");
        cmbPlazo.setItems(FXCollections.observableArrayList("1 mes", "2 meses", "3 meses"));
        
        Label lblTipoPago = new Label("Tipo de Pago:");
        ComboBox<String> cmbTipoPago = new ComboBox<>();
        cmbTipoPago.setPromptText("Seleccionar tipo");
        cmbTipoPago.setItems(FXCollections.observableArrayList("diario", "semanal", "mensual"));
        
        Label lblFechaInicio = new Label("Fecha de Inicio:");
        DatePicker dpFechaInicio = new DatePicker();
        dpFechaInicio.setValue(DateTimeUtil.today());
        
        Label lblProposito = new Label("Propósito:");
        TextArea txtProposito = new TextArea();
        txtProposito.setPromptText("Describir el propósito del préstamo");
        txtProposito.setPrefRowCount(3);
        
        // Agregar campos al formulario
        formulario.add(lblCliente, 0, 0);
        formulario.add(txtDniCliente, 1, 0);
        formulario.add(lblMonto, 2, 0);
        formulario.add(txtMonto, 3, 0);
        
        formulario.add(lblPlazo, 0, 1);
        formulario.add(cmbPlazo, 1, 1);
        formulario.add(lblTipoPago, 2, 1);
        formulario.add(cmbTipoPago, 3, 1);
        
        formulario.add(lblFechaInicio, 0, 2);
        formulario.add(dpFechaInicio, 1, 2);
        formulario.add(lblProposito, 2, 2);
        formulario.add(txtProposito, 3, 2);
        
        // Botones
        HBox botones = new HBox(15);
        botones.setAlignment(Pos.CENTER);
        
        Button btnSimular = new Button("Simular Préstamo");
        btnSimular.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-pref-width: 150;");
        
        Button btnSolicitar = new Button("Solicitar Préstamo");
        btnSolicitar.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-pref-width: 150;");
        
        Button btnLimpiar = new Button("Limpiar Formulario");
        btnLimpiar.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-pref-width: 150;");
        
        botones.getChildren().addAll(btnSimular, btnSolicitar, btnLimpiar);
        
        // Resultado de simulación
        VBox resultadoSimulacion = new VBox(10);
        resultadoSimulacion.setPadding(new Insets(15));
        resultadoSimulacion.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-border-radius: 5;");
        
        Label lblResultado = new Label("Resultado de la Simulación:");
        lblResultado.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        
        Label lblCuotaMensual = new Label("Cuota Mensual: S/ 0.00");
        Label lblTotalPagar = new Label("Total a Pagar: S/ 0.00");
        Label lblTasaInteres = new Label("Tasa de Interés: 0.00%");
        
        resultadoSimulacion.getChildren().addAll(lblResultado, lblCuotaMensual, lblTotalPagar, lblTasaInteres);
        
        // Event handlers
        btnSimular.setOnAction(e -> simularPrestamo(txtDniCliente, txtMonto, cmbPlazo, cmbTipoPago, 
                                                   lblCuotaMensual, lblTotalPagar, lblTasaInteres));
        
        btnSolicitar.setOnAction(e -> solicitarPrestamo(txtDniCliente, txtMonto, cmbPlazo, cmbTipoPago, 
                                                      dpFechaInicio, txtProposito));
        
        btnLimpiar.setOnAction(e -> {
            txtDniCliente.clear();
            txtMonto.clear();
            cmbPlazo.getSelectionModel().clearSelection();
            cmbTipoPago.getSelectionModel().clearSelection();
            dpFechaInicio.setValue(DateTimeUtil.today());
            txtProposito.clear();
            lblCuotaMensual.setText("Cuota Mensual: S/ 0.00");
            lblTotalPagar.setText("Total a Pagar: S/ 0.00");
            lblTasaInteres.setText("Tasa de Interés: 0.00%");
        });
        
        root.getChildren().addAll(titulo, formulario, botones, resultadoSimulacion);
        
        return root;
    }
    
    /**
     * Carga todos los clientes del asesor actual desde la base de datos
     */
    private void cargarTodosLosClientes(TableView<Cliente> tablaClientes, Label lblEstado) {
        try {
            // Obtener el ID del asesor actual de la sesión
            Long idAsesor = SessionManager.getInstance().getAsesorId();
            
            if (idAsesor == null) {
                logger.error("No se pudo obtener el ID del asesor de la sesión");
                mostrarError("Error: No se pudo identificar al asesor");
                return;
            }
            
            // Obtener clientes reales de la base de datos
            List<Cliente> clientesBD = clienteService.obtenerClientesPorAsesor(idAsesor);
            
            // Convertir a ObservableList
            ObservableList<Cliente> clientes = FXCollections.observableArrayList(clientesBD);
            
            tablaClientes.setItems(clientes);
            lblEstado.setText("Total de clientes: " + clientes.size());
            
            logger.info("Cargados {} clientes para el asesor {} desde la base de datos", clientes.size(), idAsesor);
            
        } catch (Exception e) {
            logger.error("Error al cargar clientes desde la base de datos", e);
            mostrarError("Error al cargar clientes: " + e.getMessage());
            
            // En caso de error, mostrar lista vacía
            tablaClientes.setItems(FXCollections.observableArrayList());
            lblEstado.setText("Error al cargar clientes");
        }
    }
    
    /**
     * Busca clientes por texto
     */
    private void buscarClientes(String texto, TableView<Cliente> tablaClientes, Label lblEstado) {
        if (texto == null || texto.trim().isEmpty()) {
            cargarTodosLosClientes(tablaClientes, lblEstado);
            return;
        }
        
        try {
            // TODO: Implementar búsqueda real en ClienteService
            ObservableList<Cliente> todosLosClientes = tablaClientes.getItems();
            ObservableList<Cliente> clientesFiltrados = FXCollections.observableArrayList();
            
            String textoBusqueda = texto.toLowerCase();
            
            for (Cliente cliente : todosLosClientes) {
                boolean coincide = 
                    cliente.getNombre().toLowerCase().contains(textoBusqueda) ||
                    cliente.getApellido().toLowerCase().contains(textoBusqueda) ||
                    cliente.getTelefono().contains(textoBusqueda);
                
                if (coincide) {
                    clientesFiltrados.add(cliente);
                }
            }
            
            tablaClientes.setItems(clientesFiltrados);
            lblEstado.setText("Clientes encontrados: " + clientesFiltrados.size());
            
        } catch (Exception e) {
            logger.error("Error al buscar clientes", e);
            mostrarError("Error al buscar clientes: " + e.getMessage());
        }
    }
    
    /**
     * Filtra clientes por criterio
     */
    private void filtrarClientes(String filtro, TableView<Cliente> tablaClientes, Label lblEstado) {
        if (filtro == null || filtro.equals("Todos")) {
            cargarTodosLosClientes(tablaClientes, lblEstado);
            return;
        }
        
        try {
            // Primero cargar todos los clientes para tener la lista completa
            Long idAsesor = SessionManager.getInstance().getAsesorId();
            if (idAsesor == null) {
                logger.error("No se pudo obtener el ID del asesor de la sesión");
                mostrarError("Error: No se pudo identificar al asesor");
                return;
            }
            List<Cliente> todosLosClientes = clienteService.obtenerClientesPorAsesor(idAsesor);
            ObservableList<Cliente> clientesFiltrados = FXCollections.observableArrayList();
            
            for (Cliente cliente : todosLosClientes) {
                boolean incluir = false;
                
                switch (filtro) {
                    case "Activos":
                        incluir = cliente.isActivo();
                        break;
                    case "Inactivos":
                        incluir = !cliente.isActivo();
                        break;
                    case "Con préstamos":
                        // Verificar si el cliente tiene préstamos
                        try {
                            List<Prestamo> prestamos = prestamoService.obtenerPrestamosPorCliente(cliente.getIdCliente());
                            incluir = !prestamos.isEmpty();
                        } catch (Exception e) {
                            logger.error("Error al verificar préstamos del cliente " + cliente.getIdCliente(), e);
                            incluir = false;
                        }
                        break;
                    case "Sin préstamos":
                        // Verificar si el cliente NO tiene préstamos
                        try {
                            List<Prestamo> prestamos = prestamoService.obtenerPrestamosPorCliente(cliente.getIdCliente());
                            incluir = prestamos.isEmpty();
                        } catch (Exception e) {
                            logger.error("Error al verificar préstamos del cliente " + cliente.getIdCliente(), e);
                            incluir = true; // Si hay error, asumir que no tiene préstamos
                        }
                        break;
                }
                
                if (incluir) {
                    clientesFiltrados.add(cliente);
                }
            }
            
            tablaClientes.setItems(clientesFiltrados);
            lblEstado.setText("Clientes filtrados: " + clientesFiltrados.size());
            
        } catch (Exception e) {
            logger.error("Error al filtrar clientes", e);
            mostrarError("Error al filtrar clientes: " + e.getMessage());
        }
    }
    
    /**
     * Abre la ventana para nuevo cliente
     */
    private void abrirNuevoCliente() {
        try {
            // Crear ventana de nuevo cliente
            Stage ventanaNuevoCliente = new Stage();
            ventanaNuevoCliente.setTitle("Nuevo Cliente");
            ventanaNuevoCliente.initModality(Modality.APPLICATION_MODAL);
            ventanaNuevoCliente.setResizable(false);
            
            // Crear el contenido de la ventana
            VBox contenido = crearFormularioNuevoCliente(ventanaNuevoCliente);
            
            Scene scene = new Scene(contenido, 500, 600);
            ventanaNuevoCliente.setScene(scene);
            ventanaNuevoCliente.showAndWait();
            
        } catch (Exception e) {
            logger.error("Error al abrir nuevo cliente", e);
            mostrarError("Error al abrir nuevo cliente: " + e.getMessage());
        }
    }
    
    /**
     * Crea el formulario para nuevo cliente
     */
    private VBox crearFormularioNuevoCliente(Stage ventana) {
        VBox contenido = new VBox(15);
        contenido.setPadding(new Insets(20));
        contenido.setAlignment(Pos.CENTER);
        
        // Título
        Label titulo = new Label("Registrar Nuevo Cliente");
        titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Formulario
        GridPane formulario = new GridPane();
        formulario.setHgap(10);
        formulario.setVgap(10);
        formulario.setAlignment(Pos.CENTER);
        
        // Campos del formulario
        TextField txtDni = new TextField();
        txtDni.setPromptText("Ingrese DNI (8 dígitos)");
        txtDni.setMaxWidth(200);
        
        TextField txtNombre = new TextField();
        txtNombre.setPromptText("Ingrese nombre");
        txtNombre.setMaxWidth(200);
        
        TextField txtApellido = new TextField();
        txtApellido.setPromptText("Ingrese apellido");
        txtApellido.setMaxWidth(200);
        
        DatePicker dpFechaContrato = new DatePicker();
        dpFechaContrato.setValue(DateTimeUtil.today());
        dpFechaContrato.setMaxWidth(200);
        
        TextField txtDireccion = new TextField();
        txtDireccion.setPromptText("Ingrese dirección");
        txtDireccion.setMaxWidth(200);
        
        TextField txtTelefono = new TextField();
        txtTelefono.setPromptText("Ingrese teléfono");
        txtTelefono.setMaxWidth(200);
        
        TextField txtEmail = new TextField();
        txtEmail.setPromptText("Ingrese email");
        txtEmail.setMaxWidth(200);
        
        // Agregar campos al formulario
        formulario.add(new Label("DNI:"), 0, 0);
        formulario.add(txtDni, 1, 0);
        
        formulario.add(new Label("Nombre:"), 0, 1);
        formulario.add(txtNombre, 1, 1);
        
        formulario.add(new Label("Apellido:"), 0, 2);
        formulario.add(txtApellido, 1, 2);
        
        formulario.add(new Label("Fecha de Contrato:"), 0, 3);
        formulario.add(dpFechaContrato, 1, 3);
        
        formulario.add(new Label("Dirección:"), 0, 4);
        formulario.add(txtDireccion, 1, 4);
        
        formulario.add(new Label("Teléfono:"), 0, 5);
        formulario.add(txtTelefono, 1, 5);
        
        formulario.add(new Label("Email:"), 0, 6);
        formulario.add(txtEmail, 1, 6);
        
        // Botones
        HBox botones = new HBox(10);
        botones.setAlignment(Pos.CENTER);
        
        Button btnGuardar = new Button("Guardar Cliente");
        btnGuardar.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");
        btnGuardar.setPrefWidth(120);
        
        Button btnCancelar = new Button("Cancelar");
        btnCancelar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        btnCancelar.setPrefWidth(120);
        
        botones.getChildren().addAll(btnGuardar, btnCancelar);
        
        // Event handlers
        btnGuardar.setOnAction(e -> guardarNuevoCliente(txtDni, txtNombre, txtApellido, dpFechaContrato, 
                                                      txtDireccion, txtTelefono, txtEmail, ventana));
        
        btnCancelar.setOnAction(e -> ventana.close());
        
        // Agregar elementos al contenido
        contenido.getChildren().addAll(titulo, formulario, botones);
        
        return contenido;
    }
    
    /**
     * Guarda el nuevo cliente en la base de datos
     */
    private void guardarNuevoCliente(TextField txtDni, TextField txtNombre, TextField txtApellido, 
                                   DatePicker dpFechaContrato, TextField txtDireccion, TextField txtTelefono, 
                                   TextField txtEmail, Stage ventana) {
        try {
            // Validar campos obligatorios
            if (txtDni.getText().trim().isEmpty() || txtNombre.getText().trim().isEmpty() || 
                txtApellido.getText().trim().isEmpty() || txtDireccion.getText().trim().isEmpty() || 
                txtTelefono.getText().trim().isEmpty() || txtEmail.getText().trim().isEmpty()) {
                mostrarError("Todos los campos son obligatorios.");
                return;
            }
            
            // Validar DNI (8 dígitos)
            String dni = txtDni.getText().trim();
            if (!dni.matches("\\d{8}")) {
                mostrarError("El DNI debe tener exactamente 8 dígitos.");
                return;
            }
            
            // Validar email
            String email = txtEmail.getText().trim();
            if (!email.contains("@") || !email.contains(".")) {
                mostrarError("Ingrese un email válido.");
                return;
            }
            
            // Verificar si el DNI ya existe
            Long idCliente = Long.parseLong(dni);
            if (clienteService.existeCliente(idCliente)) {
                mostrarError("Ya existe un cliente con el DNI: " + dni);
                return;
            }
            
            // Crear nuevo cliente
            Cliente nuevoCliente = new Cliente();
            nuevoCliente.setIdCliente(idCliente);
            nuevoCliente.setNombre(txtNombre.getText().trim());
            nuevoCliente.setApellido(txtApellido.getText().trim());
            nuevoCliente.setFechaRegistro(dpFechaContrato.getValue());
            nuevoCliente.setDireccion(txtDireccion.getText().trim());
            nuevoCliente.setTelefono(txtTelefono.getText().trim());
            nuevoCliente.setEmail(email);
            // CORRECCIÓN CRÍTICA: Obtener el ID del asesor actual desde la sesión
            Long idAsesor = SessionManager.getInstance().getAsesorId();
            if (idAsesor == null) {
                logger.error("No se pudo obtener el ID del asesor de la sesión");
                mostrarError("Error: No se pudo identificar al asesor");
                return;
            }
            nuevoCliente.setIdAsesor(idAsesor);
            logger.info("Cliente será registrado con id_asesor: " + idAsesor);
            nuevoCliente.setSaldoCapital(new java.math.BigDecimal("0.00"));
            nuevoCliente.setEtiquetaCliente(Cliente.EtiquetaCliente.EXCELENTE);
            nuevoCliente.setActivo(true);
            
            // Guardar en la base de datos
            boolean guardado = clienteService.crearCliente(nuevoCliente);
            
            if (guardado) {
                mostrarInfo("Cliente registrado exitosamente.");
                ventana.close();
                // TODO: Actualizar la tabla de clientes
            } else {
                mostrarError("Error al guardar el cliente en la base de datos.");
            }
            
        } catch (NumberFormatException e) {
            mostrarError("El DNI debe contener solo números.");
        } catch (Exception e) {
            logger.error("Error al guardar nuevo cliente", e);
            mostrarError("Error al guardar cliente: " + e.getMessage());
        }
    }
    
    /**
     * Simula un préstamo y muestra los resultados
     */
    private void simularPrestamo(TextField txtDniCliente, TextField txtMonto, ComboBox<String> cmbPlazo, 
                                ComboBox<String> cmbTipoPago, Label lblCuotaMensual, Label lblTotalPagar, 
                                Label lblTasaInteres) {
        try {
            // Validar campos obligatorios
            if (txtDniCliente.getText().trim().isEmpty() || txtMonto.getText().trim().isEmpty() || 
                cmbPlazo.getValue() == null || cmbTipoPago.getValue() == null) {
                mostrarError("Todos los campos son obligatorios para la simulación.");
                return;
            }
            
            // Validar DNI
            String dni = txtDniCliente.getText().trim();
            if (!dni.matches("\\d{8}")) {
                mostrarError("El DNI debe tener exactamente 8 dígitos.");
                return;
            }
            
            // Validar monto
            double monto;
            try {
                monto = Double.parseDouble(txtMonto.getText().trim());
                if (monto <= 0) {
                    mostrarError("El monto debe ser mayor a 0.");
                    return;
                }
            } catch (NumberFormatException e) {
                mostrarError("El monto debe ser un número válido.");
                return;
            }
            
            // Obtener plazo en meses
            String plazoStr = cmbPlazo.getValue();
            int plazoMeses = 1;
            if (plazoStr.equals("2 meses")) plazoMeses = 2;
            else if (plazoStr.equals("3 meses")) plazoMeses = 3;
            
            // Obtener tipo de pago
            String tipoPagoStr = cmbTipoPago.getValue();
            Prestamo.TipoPago tipoPago = Prestamo.TipoPago.DIARIO;
            if (tipoPagoStr.equals("Semanal")) tipoPago = Prestamo.TipoPago.SEMANAL;
            else if (tipoPagoStr.equals("Mensual")) tipoPago = Prestamo.TipoPago.MENSUAL;
            
            // Calcular número de cuotas según tipo de pago
            int numeroCuotas = calcularNumeroCuotas(plazoMeses, tipoPago);
            
            // Calcular simulación
            double tasaInteres = 14.4; // 14.4% mensual
            double totalPagar = monto * (1 + (tasaInteres / 100) * plazoMeses);
            double cuotaMensual = totalPagar / numeroCuotas;
            
            // Mostrar resultados
            lblCuotaMensual.setText(String.format("Cuota Mensual: S/ %.2f", cuotaMensual));
            lblTotalPagar.setText(String.format("Total a Pagar: S/ %.2f", totalPagar));
            lblTasaInteres.setText(String.format("Tasa de Interés: %.2f%%", tasaInteres));
            
        } catch (Exception e) {
            logger.error("Error al simular préstamo", e);
            mostrarError("Error al simular préstamo: " + e.getMessage());
        }
    }
    
    /**
     * Calcula el número de cuotas según el tipo de pago
     */
    private int calcularNumeroCuotas(int periodo, Prestamo.TipoPago tipoPago) {
        switch (tipoPago) {
            case DIARIO:
                return periodo * 26; // 26 días hábiles por mes
            case SEMANAL:
                return periodo * 4; // 4 semanas por mes
            case MENSUAL:
                return periodo; // 1 cuota por mes
            default:
                return periodo * 26;
        }
    }
    
    /**
     * Solicita un préstamo y lo guarda en la base de datos
     */
    private void solicitarPrestamo(TextField txtDniCliente, TextField txtMonto, ComboBox<String> cmbPlazo, 
                                  ComboBox<String> cmbTipoPago, DatePicker dpFechaInicio, TextArea txtProposito) {
        try {
            // Validar campos obligatorios
            if (txtDniCliente.getText().trim().isEmpty() || txtMonto.getText().trim().isEmpty() || 
                cmbPlazo.getValue() == null || cmbTipoPago.getValue() == null) {
                mostrarError("Todos los campos son obligatorios.");
                return;
            }
            
            // Validar DNI
            String dni = txtDniCliente.getText().trim();
            if (!dni.matches("\\d{8}")) {
                mostrarError("El DNI debe tener exactamente 8 dígitos.");
                return;
            }
            
            // Validar monto
            double monto;
            try {
                monto = Double.parseDouble(txtMonto.getText().trim());
                if (monto <= 0) {
                    mostrarError("El monto debe ser mayor a 0.");
                    return;
                }
            } catch (NumberFormatException e) {
                mostrarError("El monto debe ser un número válido.");
                return;
            }
            
            // Verificar que el cliente existe
            Long idCliente = Long.parseLong(dni);
            if (!clienteService.existeCliente(idCliente)) {
                mostrarError("No existe un cliente con el DNI: " + dni);
                return;
            }
            
            // VALIDACIÓN CRÍTICA DE SEGURIDAD: Verificar que el cliente pertenezca al asesor actual
            Long idAsesor = SessionManager.getInstance().getAsesorId();
            if (idAsesor == null) {
                logger.error("No se pudo obtener el ID del asesor de la sesión");
                mostrarError("Error: No se pudo identificar al asesor");
                return;
            }
            
            if (!clienteService.clientePerteneceAlAsesor(idCliente, idAsesor)) {
                logger.warn("Intento de acceso no autorizado - Asesor " + idAsesor + 
                           " intentó solicitar préstamo para cliente " + idCliente);
                mostrarError("No tiene permisos para solicitar préstamos para este cliente. " +
                           "Solo puede solicitar préstamos para sus propios clientes.");
                return;
            }
            
            // Obtener plazo en meses
            String plazoStr = cmbPlazo.getValue();
            int plazoMeses = 1;
            if (plazoStr.equals("2 meses")) plazoMeses = 2;
            else if (plazoStr.equals("3 meses")) plazoMeses = 3;
            
            // Crear el préstamo
            Prestamo nuevoPrestamo = new Prestamo();
            nuevoPrestamo.setIdCliente(idCliente);
            nuevoPrestamo.setIdAsesor(idAsesor); // ID del asesor actual de la sesión
            nuevoPrestamo.setMontoSolicitado(new java.math.BigDecimal(monto));
            nuevoPrestamo.setMontoDesembolsado(new java.math.BigDecimal("0.00")); // Por defecto 0
            nuevoPrestamo.setTasaInteres(new java.math.BigDecimal("14.40")); // 14.4% por defecto
            nuevoPrestamo.setEstado(Prestamo.EstadoPrestamo.PENDIENTE); // Estado pendiente
            nuevoPrestamo.setEtiqueta(Prestamo.EtiquetaPrestamo.PUNTUAL); // Por defecto puntual
            nuevoPrestamo.setPeriodoMeses((byte) plazoMeses);
            
            // Convertir tipo de pago
            String tipoPagoStr = cmbTipoPago.getValue();
            Prestamo.TipoPago tipoPago = Prestamo.TipoPago.DIARIO; // Por defecto
            if (tipoPagoStr.equals("semanal")) {
                tipoPago = Prestamo.TipoPago.SEMANAL;
            } else if (tipoPagoStr.equals("mensual")) {
                tipoPago = Prestamo.TipoPago.MENSUAL;
            }
            nuevoPrestamo.setTipoPago(tipoPago);
            
            nuevoPrestamo.setFechaInicio(dpFechaInicio.getValue());
            
            // Calcular fecha fin
            LocalDate fechaFin = dpFechaInicio.getValue().plusMonths(plazoMeses);
            nuevoPrestamo.setFechaFin(fechaFin);
            
            nuevoPrestamo.setObservacion(txtProposito.getText().trim());
            
            // Guardar en la base de datos
            boolean guardado = prestamoService.crearSolicitud(nuevoPrestamo);
            
            if (guardado) {
                mostrarInfo("Préstamo solicitado exitosamente. Estado: PENDIENTE - Esperando aprobación del administrador.");
                
                // Limpiar formulario
                txtDniCliente.clear();
                txtMonto.clear();
                cmbPlazo.getSelectionModel().clearSelection();
                cmbTipoPago.getSelectionModel().clearSelection();
                dpFechaInicio.setValue(DateTimeUtil.today());
                txtProposito.clear();
            } else {
                mostrarError("Error al guardar el préstamo en la base de datos.");
            }
            
        } catch (NumberFormatException e) {
            mostrarError("El DNI debe contener solo números.");
        } catch (Exception e) {
            logger.error("Error al solicitar préstamo", e);
            mostrarError("Error al solicitar préstamo: " + e.getMessage());
        }
    }
    
    /**
     * Crea la vista del simulador de crédito
     */
    private VBox crearSimuladorView() {
        VBox root = new VBox(25);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: #f8f9fa;");
        
        // Título principal
        Label titulo = new Label("🚀 Simulador de Crédito");
        titulo.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 10 0;");
        
        Label subtitulo = new Label("Simula diferentes escenarios de préstamo para conocer las condiciones exactas");
        subtitulo.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-padding: 0 0 20 0;");
        
        // Panel de parámetros
        VBox panelParametros = new VBox(20);
        panelParametros.setPadding(new Insets(25));
        panelParametros.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 3);");
        
        Label tituloParametros = new Label("📋 Parámetros del Préstamo");
        tituloParametros.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 15 0;");
        
        // Grid para los campos
        GridPane gridParametros = new GridPane();
        gridParametros.setHgap(20);
        gridParametros.setVgap(15);
        
        // Configurar columnas
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(200);
        ColumnConstraints col2 = new ColumnConstraints();
        gridParametros.getColumnConstraints().addAll(col1, col2);
        
        // Campos del formulario con iconos
        Label lblMonto = new Label("💰 Monto a solicitar:");
        lblMonto.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");
        TextField txtMonto = new TextField();
        txtMonto.setPromptText("Ej: 1000");
        txtMonto.setStyle("-fx-padding: 8 12; -fx-font-size: 14px; -fx-background-radius: 8; -fx-border-color: #bdc3c7; -fx-border-radius: 8;");
        
        Label lblInteres = new Label("📈 Tasa de interés (%):");
        lblInteres.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");
        TextField txtInteres = new TextField("14.40");
        txtInteres.setPromptText("Ej: 14.4");
        txtInteres.setStyle("-fx-padding: 8 12; -fx-font-size: 14px; -fx-background-radius: 8; -fx-border-color: #bdc3c7; -fx-border-radius: 8;");
        
        Label lblPlazo = new Label("📅 Período (meses):");
        lblPlazo.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");
        ComboBox<String> cmbPlazo = new ComboBox<>();
        cmbPlazo.getItems().addAll("1 mes", "2 meses", "3 meses", "4 meses", "5 meses", "6 meses");
        cmbPlazo.setPromptText("Seleccionar plazo");
        cmbPlazo.setStyle("-fx-padding: 8 12; -fx-font-size: 14px; -fx-background-radius: 8; -fx-border-color: #bdc3c7; -fx-border-radius: 8;");
        
        Label lblTipoPago = new Label("💳 Tipo de pago:");
        lblTipoPago.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");
        ComboBox<String> cmbTipoPago = new ComboBox<>();
        cmbTipoPago.getItems().addAll("diario", "semanal", "mensual");
        cmbTipoPago.setPromptText("Seleccionar tipo");
        cmbTipoPago.setStyle("-fx-padding: 8 12; -fx-font-size: 14px; -fx-background-radius: 8; -fx-border-color: #bdc3c7; -fx-border-radius: 8;");
        
        // Agregar campos al grid
        gridParametros.add(lblMonto, 0, 0);
        gridParametros.add(txtMonto, 1, 0);
        gridParametros.add(lblInteres, 0, 1);
        gridParametros.add(txtInteres, 1, 1);
        gridParametros.add(lblPlazo, 0, 2);
        gridParametros.add(cmbPlazo, 1, 2);
        gridParametros.add(lblTipoPago, 0, 3);
        gridParametros.add(cmbTipoPago, 1, 3);
        
        // Botones
        HBox botones = new HBox(15);
        botones.setAlignment(Pos.CENTER);
        Button btnSimular = new Button("🚀 Simular Préstamo");
        btnSimular.setStyle("-fx-background-color: #3498db; -fx-text-fill: blue; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25; -fx-background-radius: 8; -fx-cursor: hand;");
        btnSimular.setOnMouseEntered(e -> btnSimular.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25; -fx-background-radius: 8; -fx-cursor: hand;"));
        btnSimular.setOnMouseExited(e -> btnSimular.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25; -fx-background-radius: 8; -fx-cursor: hand;"));
        
        Button btnLimpiar = new Button("🔄 Limpiar");
        btnLimpiar.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25; -fx-background-radius: 8; -fx-cursor: hand;");
        btnLimpiar.setOnMouseEntered(e -> btnLimpiar.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25; -fx-background-radius: 8; -fx-cursor: hand;"));
        btnLimpiar.setOnMouseExited(e -> btnLimpiar.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25; -fx-background-radius: 8; -fx-cursor: hand;"));
        
        botones.getChildren().addAll(btnSimular, btnLimpiar);
        
        panelParametros.getChildren().addAll(tituloParametros, gridParametros, botones);
        
        // Panel de resultados
        VBox panelResultados = new VBox(20);
        panelResultados.setPadding(new Insets(25));
        panelResultados.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 3);");
        
        Label tituloResultados = new Label("📊 Resultados de la Simulación");
        tituloResultados.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 15 0;");
        
        // Grid para los resultados
        GridPane gridResultados = new GridPane();
        gridResultados.setHgap(20);
        gridResultados.setVgap(15);
        
        // Configurar columnas para resultados
        ColumnConstraints colR1 = new ColumnConstraints();
        colR1.setMinWidth(220);
        ColumnConstraints colR2 = new ColumnConstraints();
        gridResultados.getColumnConstraints().addAll(colR1, colR2);
        
        // Labels de resultados con iconos
        Label lblMontoSolicitado = new Label("💰 Monto solicitado:");
        lblMontoSolicitado.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e; -fx-font-size: 14px;");
        Label lblValorMontoSolicitado = new Label("S/ 0.00");
        lblValorMontoSolicitado.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 14px;");
        
        Label lblMontoFinanciar = new Label("🏦 Capital retenido (10%):");
        lblMontoFinanciar.setStyle("-fx-font-weight: bold; -fx-text-fill: #e67e22; -fx-font-size: 14px;");
        Label lblValorMontoFinanciar = new Label("S/ 0.00");
        lblValorMontoFinanciar.setStyle("-fx-font-weight: bold; -fx-text-fill: #e67e22; -fx-font-size: 14px;");
        
        Label lblMontoDesembolsado = new Label("💸 Monto desembolsado:");
        lblMontoDesembolsado.setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60; -fx-font-size: 14px;");
        Label lblValorMontoDesembolsado = new Label("S/ 0.00");
        lblValorMontoDesembolsado.setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60; -fx-font-size: 14px;");
        
        Label lblMontoTotal = new Label("💳 Monto total a pagar:");
        lblMontoTotal.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 14px;");
        Label lblValorMontoTotal = new Label("S/ 0.00");
        lblValorMontoTotal.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 14px;");
        
        Label lblNumeroCuotas = new Label("📅 Número de cuotas:");
        lblNumeroCuotas.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 14px;");
        Label lblValorNumeroCuotas = new Label("0");
        lblValorNumeroCuotas.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 14px;");
        
        Label lblValorCuota = new Label("💵 Monto por cuota:");
        lblValorCuota.setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60; -fx-font-size: 14px;");
        Label lblValorValorCuota = new Label("S/ 0.00");
        lblValorValorCuota.setStyle("-fx-font-weight: bold; -fx-text-fill: #27ae60; -fx-font-size: 14px;");
        
        Label lblInteresTotal = new Label("📈 Interés total:");
        lblInteresTotal.setStyle("-fx-font-weight: bold; -fx-text-fill: #e67e22; -fx-font-size: 14px;");
        Label lblValorInteresTotal = new Label("S/ 0.00");
        lblValorInteresTotal.setStyle("-fx-font-weight: bold; -fx-text-fill: #e67e22; -fx-font-size: 14px;");
        
        Label lblFechaFinalizacion = new Label("🎯 Fecha de finalización:");
        lblFechaFinalizacion.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 14px;");
        Label lblValorFechaFinalizacion = new Label("-");
        lblValorFechaFinalizacion.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 14px;");
        
        // Agregar resultados al grid
        gridResultados.add(lblMontoSolicitado, 0, 0);
        gridResultados.add(lblValorMontoSolicitado, 1, 0);
        gridResultados.add(lblMontoFinanciar, 0, 1);
        gridResultados.add(lblValorMontoFinanciar, 1, 1);
        gridResultados.add(lblMontoDesembolsado, 0, 2);
        gridResultados.add(lblValorMontoDesembolsado, 1, 2);
        gridResultados.add(lblMontoTotal, 0, 3);
        gridResultados.add(lblValorMontoTotal, 1, 3);
        gridResultados.add(lblNumeroCuotas, 0, 4);
        gridResultados.add(lblValorNumeroCuotas, 1, 4);
        gridResultados.add(lblValorCuota, 0, 5);
        gridResultados.add(lblValorValorCuota, 1, 5);
        gridResultados.add(lblInteresTotal, 0, 6);
        gridResultados.add(lblValorInteresTotal, 1, 6);
        gridResultados.add(lblFechaFinalizacion, 0, 7);
        gridResultados.add(lblValorFechaFinalizacion, 1, 7);
        
        panelResultados.getChildren().addAll(tituloResultados, gridResultados);
        
        // Panel del cronograma
        VBox panelCronograma = new VBox(20);
        panelCronograma.setPadding(new Insets(25));
        panelCronograma.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 3);");
        
        HBox tituloCronograma = new HBox(15);
        tituloCronograma.setAlignment(Pos.CENTER_LEFT);
        Label lblTituloCronograma = new Label("📋 Cronograma Simulado");
        lblTituloCronograma.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Button btnExportarCronograma = new Button("📄 Exportar PDF");
        btnExportarCronograma.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 8 15; -fx-background-radius: 6; -fx-cursor: hand;");
        btnExportarCronograma.setOnMouseEntered(e -> btnExportarCronograma.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 8 15; -fx-background-radius: 6; -fx-cursor: hand;"));
        btnExportarCronograma.setOnMouseExited(e -> btnExportarCronograma.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 8 15; -fx-background-radius: 6; -fx-cursor: hand;"));
        
        tituloCronograma.getChildren().addAll(lblTituloCronograma, btnExportarCronograma);
        
        // Tabla del cronograma
        TableView<CronogramaSimulacion> tablaCronograma = new TableView<>();
        tablaCronograma.setPrefHeight(400);
        tablaCronograma.setMinHeight(300);
        tablaCronograma.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-border-color: #ecf0f1; -fx-border-radius: 10;");
        
        TableColumn<CronogramaSimulacion, Integer> colCuota = new TableColumn<>("Cuota");
        colCuota.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().getNumeroCuota()).asObject());
        colCuota.setPrefWidth(90);
        colCuota.setStyle("-fx-alignment: CENTER;");
        
        TableColumn<CronogramaSimulacion, String> colFecha = new TableColumn<>("Fecha Programada");
        colFecha.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getFechaPago()));
        colFecha.setPrefWidth(140);
        colFecha.setStyle("-fx-alignment: CENTER;");
        
        TableColumn<CronogramaSimulacion, String> colMonto = new TableColumn<>("Monto Cuota");
        colMonto.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getMontoCuota()));
        colMonto.setPrefWidth(130);
        colMonto.setStyle("-fx-alignment: CENTER;");
        
        TableColumn<CronogramaSimulacion, String> colSaldo = new TableColumn<>("Saldo Restante");
        colSaldo.setCellValueFactory(cellData -> 
            new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSaldoRestante()));
        colSaldo.setPrefWidth(130);
        colSaldo.setStyle("-fx-alignment: CENTER;");
        
        tablaCronograma.getColumns().addAll(colCuota, colFecha, colMonto, colSaldo);
        
        panelCronograma.getChildren().addAll(tituloCronograma, tablaCronograma);
        
        // Panel de advertencia
        HBox panelAdvertencia = new HBox();
        panelAdvertencia.setAlignment(Pos.CENTER);
        panelAdvertencia.setPadding(new Insets(15, 20, 15, 20));
        panelAdvertencia.setStyle("-fx-background-color: #fff3cd; -fx-background-radius: 10; -fx-border-color: #ffeaa7; -fx-border-radius: 10;");
        
        Label lblAdvertencia = new Label("⚠️ Esta simulación es solo informativa. Las condiciones reales pueden variar.");
        lblAdvertencia.setStyle("-fx-font-weight: bold; -fx-text-fill: #856404; -fx-font-size: 13px;");
        
        panelAdvertencia.getChildren().add(lblAdvertencia);
        
        // Event handlers
        btnSimular.setOnAction(e -> simularCreditoMejorado(txtMonto, txtInteres, cmbPlazo, cmbTipoPago, 
                                                         lblValorMontoSolicitado, lblValorMontoFinanciar, lblValorMontoDesembolsado, 
                                                         lblValorMontoTotal, lblValorNumeroCuotas, lblValorValorCuota, 
                                                         lblValorInteresTotal, lblValorFechaFinalizacion, tablaCronograma));
        
        btnLimpiar.setOnAction(e -> {
            txtMonto.clear();
            txtInteres.setText("14.40");
            cmbPlazo.getSelectionModel().clearSelection();
            cmbTipoPago.getSelectionModel().clearSelection();
            lblValorMontoSolicitado.setText("S/ 0.00");
            lblValorMontoFinanciar.setText("S/ 0.00");
            lblValorMontoDesembolsado.setText("S/ 0.00");
            lblValorMontoTotal.setText("S/ 0.00");
            lblValorNumeroCuotas.setText("0");
            lblValorValorCuota.setText("S/ 0.00");
            lblValorInteresTotal.setText("S/ 0.00");
            lblValorFechaFinalizacion.setText("-");
            tablaCronograma.getItems().clear();
        });
        
        root.getChildren().addAll(titulo, subtitulo, panelParametros, panelResultados, panelCronograma, panelAdvertencia);
        
        return root;
    }
    
    /**
     * Clase para representar una cuota del cronograma
     */
    public static class CronogramaSimulacion {
        private int numeroCuota;
        private String fechaPago;
        private String montoCuota;
        private String saldoRestante;
        
        public CronogramaSimulacion(int numeroCuota, String fechaPago, String montoCuota, String saldoRestante) {
            this.numeroCuota = numeroCuota;
            this.fechaPago = fechaPago;
            this.montoCuota = montoCuota;
            this.saldoRestante = saldoRestante;
        }
        
        // Getters
        public int getNumeroCuota() { return numeroCuota; }
        public String getFechaPago() { return fechaPago; }
        public String getMontoCuota() { return montoCuota; }
        public String getSaldoRestante() { return saldoRestante; }
    }
    
    /**
     * Simula un crédito y muestra los resultados (versión mejorada con iconos)
     */
    private void simularCreditoMejorado(TextField txtMonto, TextField txtInteres, ComboBox<String> cmbPlazo, 
                                       ComboBox<String> cmbTipoPago, Label lblValorMontoSolicitado, Label lblValorMontoFinanciar, 
                                       Label lblValorMontoDesembolsado, Label lblValorMontoTotal, Label lblValorNumeroCuotas, 
                                       Label lblValorValorCuota, Label lblValorInteresTotal, Label lblValorFechaFinalizacion,
                                       TableView<CronogramaSimulacion> tablaCronograma) {
        try {
            // Validar campos obligatorios
            if (txtMonto.getText().trim().isEmpty() || txtInteres.getText().trim().isEmpty() || 
                cmbPlazo.getValue() == null || cmbTipoPago.getValue() == null) {
                mostrarError("Todos los campos son obligatorios para la simulación.");
                return;
            }
            
            // Validar monto
            double montoSolicitado;
            try {
                montoSolicitado = Double.parseDouble(txtMonto.getText().trim());
                if (montoSolicitado <= 0) {
                    mostrarError("El monto debe ser mayor a 0.");
                    return;
                }
            } catch (NumberFormatException e) {
                mostrarError("El monto debe ser un número válido.");
                return;
            }
            
            // Validar interés
            double tasaInteres;
            try {
                tasaInteres = Double.parseDouble(txtInteres.getText().trim());
                if (tasaInteres < 0 || tasaInteres > 100) {
                    mostrarError("La tasa de interés debe estar entre 0 y 100.");
                    return;
                }
            } catch (NumberFormatException e) {
                mostrarError("La tasa de interés debe ser un número válido.");
                return;
            }
            
            // Obtener plazo en meses
            String plazoStr = cmbPlazo.getValue();
            int plazoMeses = 1;
            if (plazoStr.equals("2 meses")) plazoMeses = 2;
            else if (plazoStr.equals("3 meses")) plazoMeses = 3;
            else if (plazoStr.equals("4 meses")) plazoMeses = 4;
            else if (plazoStr.equals("5 meses")) plazoMeses = 5;
            else if (plazoStr.equals("6 meses")) plazoMeses = 6;
            
            // Aplicar reglas de negocio
            double interesDecimal = tasaInteres / 100.0; // 18% = 0.18
            
            // Monto a Financiar (Desembolso): 1000 - 10% = 900
            double montoFinanciar = montoSolicitado * 0.90;
            
            // Capital retenido (10%)
            double capitalRetenido = montoSolicitado * 0.10;
            
            // Monto Total a Pagar: 1000 × (1 + 0.18) = 1180
            double montoTotalPagar = montoSolicitado * (1 + interesDecimal);
            
            // Interés total
            double interesTotal = montoTotalPagar - montoSolicitado;
            
            // Calcular número de cuotas según tipo de pago
            String tipoPago = cmbTipoPago.getValue();
            int numeroCuotas = 0;
            
            switch (tipoPago) {
                case "diario":
                    // Excluyendo domingos: 26 días hábiles por mes
                    numeroCuotas = plazoMeses * 26;
                    break;
                case "semanal":
                    // 4 semanas por mes
                    numeroCuotas = plazoMeses * 4;
                    break;
                case "mensual":
                    // 1 cuota por mes
                    numeroCuotas = plazoMeses;
                    break;
            }
            
            // Calcular valor por cuota
            double valorCuota = montoTotalPagar / numeroCuotas;
            
            // Redondear siempre a favor (hacia arriba)
            valorCuota = Math.ceil(valorCuota * 10.0) / 10.0;
            
            // Calcular fecha de finalización
            LocalDate fechaInicio = DateTimeUtil.today();
            LocalDate fechaFinalizacion = fechaInicio;
            
            switch (tipoPago) {
                case "diario":
                    fechaFinalizacion = fechaInicio.plusDays(numeroCuotas + (numeroCuotas / 26)); // Aproximado para domingos
                    break;
                case "semanal":
                    fechaFinalizacion = fechaInicio.plusWeeks(numeroCuotas);
                    break;
                case "mensual":
                    fechaFinalizacion = fechaInicio.plusMonths(numeroCuotas);
                    break;
            }
            
            // Mostrar resultados
            lblValorMontoSolicitado.setText(String.format("S/ %.2f", montoSolicitado));
            lblValorMontoFinanciar.setText(String.format("S/ %.2f", capitalRetenido));
            lblValorMontoDesembolsado.setText(String.format("S/ %.2f", montoFinanciar));
            lblValorMontoTotal.setText(String.format("S/ %.2f", montoTotalPagar));
            lblValorNumeroCuotas.setText(String.format("%d", numeroCuotas));
            lblValorValorCuota.setText(String.format("S/ %.2f", valorCuota));
            lblValorInteresTotal.setText(String.format("S/ %.2f", interesTotal));
            lblValorFechaFinalizacion.setText(fechaFinalizacion.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            
            // Generar cronograma
            generarCronograma(montoTotalPagar, valorCuota, numeroCuotas, tipoPago, plazoMeses, tablaCronograma);
            
        } catch (Exception e) {
            logger.error("Error al simular crédito", e);
            mostrarError("Error al simular crédito: " + e.getMessage());
        }
    }
    
    /**
     * Simula un crédito y muestra los resultados (versión original)
     */
    private void simularCredito(TextField txtMonto, TextField txtInteres, ComboBox<String> cmbPlazo, 
                               ComboBox<String> cmbTipoPago, Label lblMontoSolicitado, Label lblMontoFinanciar, 
                               Label lblMontoTotal, Label lblNumeroCuotas, Label lblValorCuota, Label lblTipoPagoSeleccionado,
                               TableView<CronogramaSimulacion> tablaCronograma) {
        try {
            // Validar campos obligatorios
            if (txtMonto.getText().trim().isEmpty() || txtInteres.getText().trim().isEmpty() || 
                cmbPlazo.getValue() == null || cmbTipoPago.getValue() == null) {
                mostrarError("Todos los campos son obligatorios para la simulación.");
                return;
            }
            
            // Validar monto
            double montoSolicitado;
            try {
                montoSolicitado = Double.parseDouble(txtMonto.getText().trim());
                if (montoSolicitado <= 0) {
                    mostrarError("El monto debe ser mayor a 0.");
                    return;
                }
            } catch (NumberFormatException e) {
                mostrarError("El monto debe ser un número válido.");
                return;
            }
            
            // Validar interés
            double tasaInteres;
            try {
                tasaInteres = Double.parseDouble(txtInteres.getText().trim());
                if (tasaInteres < 0 || tasaInteres > 100) {
                    mostrarError("La tasa de interés debe estar entre 0 y 100.");
                    return;
                }
            } catch (NumberFormatException e) {
                mostrarError("La tasa de interés debe ser un número válido.");
                return;
            }
            
            // Obtener plazo en meses
            String plazoStr = cmbPlazo.getValue();
            int plazoMeses = 1;
            if (plazoStr.equals("2 meses")) plazoMeses = 2;
            else if (plazoStr.equals("3 meses")) plazoMeses = 3;
            else if (plazoStr.equals("4 meses")) plazoMeses = 4;
            else if (plazoStr.equals("5 meses")) plazoMeses = 5;
            else if (plazoStr.equals("6 meses")) plazoMeses = 6;
            
            // Aplicar reglas de negocio
            double interesDecimal = tasaInteres / 100.0; // 18% = 0.18
            
            // Monto a Financiar (Desembolso): 1000 - 10% = 900
            double montoFinanciar = montoSolicitado * 0.90;
            
            // Monto Total a Pagar: 1000 × (1 + 0.18) = 1180
            double montoTotalPagar = montoSolicitado * (1 + interesDecimal);
            
            // Calcular número de cuotas según tipo de pago
            String tipoPago = cmbTipoPago.getValue();
            int numeroCuotas = 0;
            
            switch (tipoPago) {
                case "diario":
                    // Excluyendo domingos: 26 días hábiles por mes
                    numeroCuotas = plazoMeses * 26;
                    break;
                case "semanal":
                    // 4 semanas por mes
                    numeroCuotas = plazoMeses * 4;
                    break;
                case "mensual":
                    // 1 cuota por mes
                    numeroCuotas = plazoMeses;
                    break;
            }
            
            // Calcular valor por cuota
            double valorCuota = montoTotalPagar / numeroCuotas;
            
            // Redondear siempre a favor (hacia arriba)
            valorCuota = Math.ceil(valorCuota * 10.0) / 10.0;
            
            // Mostrar resultados
            lblMontoSolicitado.setText(String.format("Monto Solicitado: S/ %.2f", montoSolicitado));
            lblMontoFinanciar.setText(String.format("Monto a Financiar (Desembolso): S/ %.2f", montoFinanciar));
            lblMontoTotal.setText(String.format("Monto Total a Pagar: S/ %.2f", montoTotalPagar));
            lblNumeroCuotas.setText(String.format("Número de Cuotas: %d", numeroCuotas));
            lblValorCuota.setText(String.format("Valor por Cuota: S/ %.2f", valorCuota));
            lblTipoPagoSeleccionado.setText(String.format("Tipo de Pago: %s", tipoPago));
            
            // Generar cronograma
            generarCronograma(montoTotalPagar, valorCuota, numeroCuotas, tipoPago, plazoMeses, tablaCronograma);
            
        } catch (Exception e) {
            logger.error("Error al simular crédito", e);
            mostrarError("Error al simular crédito: " + e.getMessage());
        }
    }
    
    /**
     * Genera el cronograma de pagos excluyendo domingos
     */
    private void generarCronograma(double montoTotal, double valorCuota, int numeroCuotas, 
                                  String tipoPago, int plazoMeses, TableView<CronogramaSimulacion> tablaCronograma) {
        try {
            ObservableList<CronogramaSimulacion> cronograma = FXCollections.observableArrayList();
            
            // Fecha de inicio (hoy)
            LocalDate fechaInicio = DateTimeUtil.today();
            LocalDate fechaPago = fechaInicio;
            
            double saldoRestante = montoTotal;
            
            for (int i = 1; i <= numeroCuotas; i++) {
                // Calcular fecha de pago según tipo
                switch (tipoPago) {
                    case "diario":
                        // Buscar el siguiente día hábil (excluyendo domingos)
                        do {
                            fechaPago = fechaPago.plusDays(1);
                        } while (fechaPago.getDayOfWeek() == java.time.DayOfWeek.SUNDAY);
                        break;
                    case "semanal":
                        // Cada 7 días
                        fechaPago = fechaInicio.plusWeeks(i - 1);
                        break;
                    case "mensual":
                        // Cada mes
                        fechaPago = fechaInicio.plusMonths(i - 1);
                        break;
                }
                
                // Calcular monto de esta cuota
                double montoCuota = valorCuota;
                if (i == numeroCuotas) {
                    // Última cuota: ajustar para que el saldo quede en 0
                    montoCuota = saldoRestante;
                }
                
                // Actualizar saldo restante
                saldoRestante -= montoCuota;
                if (saldoRestante < 0) saldoRestante = 0;
                
                // Formatear fechas y montos
                String fechaFormateada = fechaPago.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
                String montoFormateado = String.format("S/ %.2f", montoCuota);
                String saldoFormateado = String.format("S/ %.2f", saldoRestante);
                
                // Crear entrada del cronograma
                CronogramaSimulacion cuota = new CronogramaSimulacion(i, fechaFormateada, montoFormateado, saldoFormateado);
                cronograma.add(cuota);
                
                // Para pagos diarios, avanzar un día para la siguiente iteración
                if (tipoPago.equals("diario")) {
                    fechaPago = fechaPago.plusDays(1);
                }
            }
            
            // Mostrar cronograma en la tabla
            tablaCronograma.setItems(cronograma);
            
        } catch (Exception e) {
            logger.error("Error al generar cronograma", e);
            mostrarError("Error al generar cronograma: " + e.getMessage());
        }
    }
    
    /**
     * Crea la vista de registro de cobro programáticamente
     */
    private VBox crearRegistrarCobroView() {
        VBox root = new VBox(25);
        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: #f8f9fa;");
        
        // Título principal
        Label titulo = new Label("💰 Registro de Cobros");
        titulo.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 10 0;");
        
        Label subtitulo = new Label("Registra los cobros realizados a tus clientes siguiendo el flujo de negocio");
        subtitulo.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-padding: 0 0 20 0;");
        
        
        // Panel de selección de cliente
        VBox panelCliente = new VBox(20);
        panelCliente.setPadding(new Insets(25));
        panelCliente.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 3);");
        
        Label tituloCliente = new Label("👤 Selección de Cliente");
        tituloCliente.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 15 0;");
        
        HBox buscarCliente = new HBox(15);
        buscarCliente.setAlignment(Pos.CENTER_LEFT);
        
        Label lblBuscarCliente = new Label("🔍 Buscar cliente:");
        lblBuscarCliente.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e; -fx-min-width: 120;");
        
        TextField txtBuscarCliente = new TextField();
        txtBuscarCliente.setPromptText("Ingresa DNI del cliente");
        txtBuscarCliente.setStyle("-fx-padding: 8 12; -fx-font-size: 14px; -fx-background-radius: 8; -fx-border-color: #bdc3c7; -fx-border-radius: 8; -fx-min-width: 200;");
        
        Button btnBuscarCliente = new Button("🔍 Buscar");
        btnBuscarCliente.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8 15; -fx-background-radius: 8; -fx-cursor: hand;");
        btnBuscarCliente.setOnMouseEntered(e -> btnBuscarCliente.setStyle("-fx-background-color: #2980b9; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8 15; -fx-background-radius: 8; -fx-cursor: hand;"));
        btnBuscarCliente.setOnMouseExited(e -> btnBuscarCliente.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 8 15; -fx-background-radius: 8; -fx-cursor: hand;"));
        
        buscarCliente.getChildren().addAll(lblBuscarCliente, txtBuscarCliente, btnBuscarCliente);
        
        // Información del cliente seleccionado
        VBox infoCliente = new VBox(10);
        infoCliente.setPadding(new Insets(15));
        infoCliente.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8; -fx-border-color: #dee2e6; -fx-border-radius: 8;");
        
        Label lblClienteInfo = new Label("Cliente: -");
        lblClienteInfo.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-font-size: 14px;");
        
        Label lblClienteTelefono = new Label("Teléfono: -");
        lblClienteTelefono.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 13px;");
        
        Label lblClienteEmail = new Label("Email: -");
        lblClienteEmail.setStyle("-fx-text-fill: #6c757d; -fx-font-size: 13px;");
        
        infoCliente.getChildren().addAll(lblClienteInfo, lblClienteTelefono, lblClienteEmail);
        
        panelCliente.getChildren().addAll(tituloCliente, buscarCliente, infoCliente);
        
        // Panel de selección de préstamo
        VBox panelPrestamo = new VBox(20);
        panelPrestamo.setPadding(new Insets(25));
        panelPrestamo.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 3);");
        
        Label tituloPrestamo = new Label("🏦 Selección de Préstamo");
        tituloPrestamo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 15 0;");
        
        HBox seleccionPrestamo = new HBox(15);
        seleccionPrestamo.setAlignment(Pos.CENTER_LEFT);
        
        Label lblPrestamo = new Label("📋 Seleccionar préstamo:");
        lblPrestamo.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e; -fx-min-width: 150;");
        
        ComboBox<String> cmbPrestamo = new ComboBox<>();
        cmbPrestamo.setPromptText("Seleccione un préstamo");
        cmbPrestamo.setStyle("-fx-padding: 8 12; -fx-font-size: 14px; -fx-background-radius: 8; -fx-border-color: #bdc3c7; -fx-border-radius: 8; -fx-min-width: 300;");
        
        seleccionPrestamo.getChildren().addAll(lblPrestamo, cmbPrestamo);
        panelPrestamo.getChildren().addAll(tituloPrestamo, seleccionPrestamo);
        
        // Panel de registro de cobro
        VBox panelRegistro = new VBox(20);
        panelRegistro.setPadding(new Insets(25));
        panelRegistro.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 3);");
        
        Label tituloRegistro = new Label("💳 Registro de Cobro");
        tituloRegistro.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 15 0;");
        
        GridPane gridRegistro = new GridPane();
        gridRegistro.setHgap(20);
        gridRegistro.setVgap(15);
        
        // Configurar columnas
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setMinWidth(150);
        ColumnConstraints col2 = new ColumnConstraints();
        gridRegistro.getColumnConstraints().addAll(col1, col2);
        
        // Campos del formulario
        Label lblCuota = new Label("📅 Cuota a cobrar:");
        lblCuota.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");
        ComboBox<String> cmbCuota = new ComboBox<>();
        cmbCuota.setPromptText("Seleccionar cuota");
        cmbCuota.setStyle("-fx-padding: 8 12; -fx-font-size: 14px; -fx-background-radius: 8; -fx-border-color: #bdc3c7; -fx-border-radius: 8;");
        
        Label lblMonto = new Label("💰 Monto cobrado:");
        lblMonto.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");
        TextField txtMonto = new TextField();
        txtMonto.setPromptText("Ej: 45.38");
        txtMonto.setStyle("-fx-padding: 8 12; -fx-font-size: 14px; -fx-background-radius: 8; -fx-border-color: #bdc3c7; -fx-border-radius: 8;");
        
        Label lblFecha = new Label("📆 Fecha de cobro:");
        lblFecha.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");
        DatePicker dpFecha = new DatePicker();
        dpFecha.setValue(DateTimeUtil.today());
        dpFecha.setStyle("-fx-padding: 8 12; -fx-font-size: 14px; -fx-background-radius: 8; -fx-border-color: #bdc3c7; -fx-border-radius: 8;");
        
        Label lblMetodo = new Label("💳 Método de pago:");
        lblMetodo.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495e;");
        ComboBox<String> cmbMetodo = new ComboBox<>();
        cmbMetodo.getItems().addAll("EFECTIVO", "TRANSFERENCIA", "YAPE", "PLIN", "TARJETA");
        cmbMetodo.setValue("EFECTIVO");
        cmbMetodo.setStyle("-fx-padding: 8 12; -fx-font-size: 14px; -fx-background-radius: 8; -fx-border-color: #bdc3c7; -fx-border-radius: 8;");
        
        // Agregar campos al grid
        gridRegistro.add(lblCuota, 0, 0);
        gridRegistro.add(cmbCuota, 1, 0);
        gridRegistro.add(lblMonto, 0, 1);
        gridRegistro.add(txtMonto, 1, 1);
        gridRegistro.add(lblFecha, 0, 2);
        gridRegistro.add(dpFecha, 1, 2);
        gridRegistro.add(lblMetodo, 0, 3);
        gridRegistro.add(cmbMetodo, 1, 3);
        
        // Botones
        HBox botones = new HBox(15);
        botones.setAlignment(Pos.CENTER);
        
        Button btnRegistrar = new Button("✅ Registrar Cobro");
        btnRegistrar.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25; -fx-background-radius: 8; -fx-cursor: hand;");
        btnRegistrar.setOnMouseEntered(e -> btnRegistrar.setStyle("-fx-background-color: #229954; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25; -fx-background-radius: 8; -fx-cursor: hand;"));
        btnRegistrar.setOnMouseExited(e -> btnRegistrar.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25; -fx-background-radius: 8; -fx-cursor: hand;"));
        
        Button btnLimpiar = new Button("🔄 Limpiar");
        btnLimpiar.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25; -fx-background-radius: 8; -fx-cursor: hand;");
        btnLimpiar.setOnMouseEntered(e -> btnLimpiar.setStyle("-fx-background-color: #7f8c8d; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25; -fx-background-radius: 8; -fx-cursor: hand;"));
        btnLimpiar.setOnMouseExited(e -> btnLimpiar.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25; -fx-background-radius: 8; -fx-cursor: hand;"));
        
        botones.getChildren().addAll(btnRegistrar, btnLimpiar);
        
        panelRegistro.getChildren().addAll(tituloRegistro, gridRegistro, botones);
        
        // Panel de advertencia
        HBox panelAdvertencia = new HBox();
        panelAdvertencia.setAlignment(Pos.CENTER);
        panelAdvertencia.setPadding(new Insets(15, 20, 15, 20));
        panelAdvertencia.setStyle("-fx-background-color: #fff3cd; -fx-background-radius: 10; -fx-border-color: #ffeaa7; -fx-border-radius: 10;");
        
        Label lblAdvertencia = new Label("⚠️ Los cobros registrados están pendientes de validación por el administrador.");
        lblAdvertencia.setStyle("-fx-font-weight: bold; -fx-text-fill: #856404; -fx-font-size: 13px;");
        
        panelAdvertencia.getChildren().add(lblAdvertencia);
        
        // Variables para almacenar datos del cliente, préstamos y cuotas (usando arrays para evitar problemas de final)
        final Cliente[] clienteSeleccionado = {null};
        final List<Prestamo> prestamosCliente = new ArrayList<>();
        final Prestamo[] prestamoSeleccionado = {null};
        final List<pe.crediactiva.app.model.Cronograma> cuotasPendientes = new ArrayList<>();
        
        // Event handlers
        btnBuscarCliente.setOnAction(e -> {
            String dni = txtBuscarCliente.getText().trim();
            if (dni.isEmpty()) {
                mostrarAdvertencia("Por favor ingrese el DNI del cliente");
                return;
            }
            
            try {
                // Buscar cliente por DNI
                Optional<Cliente> clienteOpt = clienteService.obtenerClientePorId(Long.parseLong(dni));
                if (clienteOpt.isPresent()) {
                    clienteSeleccionado[0] = clienteOpt.get();
                    
                    // Verificar que el cliente pertenece al asesor actual
                    Long idAsesorActual = SessionManager.getInstance().getAsesorId();
                    if (!clienteSeleccionado[0].getIdAsesor().equals(idAsesorActual)) {
                        mostrarError("Este cliente no pertenece a tu cartera de clientes");
                        return;
                    }
                    
                    // Mostrar información del cliente
                    lblClienteInfo.setText("Cliente: " + clienteSeleccionado[0].getNombre() + " " + clienteSeleccionado[0].getApellido() + " (DNI: " + dni + ")");
                    lblClienteTelefono.setText("Teléfono: " + clienteSeleccionado[0].getTelefono());
                    lblClienteEmail.setText("Email: " + clienteSeleccionado[0].getEmail());
                    
                    // Cargar préstamos activos del cliente
                    prestamosCliente.clear();
                    List<Prestamo> todosPrestamos = prestamoService.obtenerPrestamosPorCliente(clienteSeleccionado[0].getIdCliente());
                    
                    // Filtrar solo préstamos activos
                    for (Prestamo prestamo : todosPrestamos) {
                        if (prestamo.getEstado() == pe.crediactiva.app.model.Prestamo.EstadoPrestamo.ACTIVO) {
                            prestamosCliente.add(prestamo);
                        }
                    }
                    
                    // Limpiar y llenar combo de préstamos
                    cmbPrestamo.getItems().clear();
                    cmbCuota.getItems().clear();
                    cuotasPendientes.clear();
                    prestamoSeleccionado[0] = null;
                    txtMonto.clear();
                    
                    // Llenar combo con préstamos activos
                    for (Prestamo prestamo : prestamosCliente) {
                        String fechaTexto = prestamo.getCreadoEn() != null ? 
                            prestamo.getCreadoEn().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "N/A";
                        String texto = String.format("Préstamo #%d - S/ %.2f - %s", 
                            prestamo.getIdPrestamo(),
                            prestamo.getMontoSolicitado(),
                            fechaTexto);
                        cmbPrestamo.getItems().add(texto);
                    }
                    
                    if (!prestamosCliente.isEmpty()) {
                        mostrarInfo("Cliente encontrado. Seleccione un préstamo para ver las cuotas pendientes.");
                    } else {
                        mostrarInfo("Cliente encontrado pero no tiene préstamos activos.");
                    }
                    
                } else {
                    mostrarError("No se encontró un cliente con el DNI: " + dni);
                }
                
            } catch (NumberFormatException ex) {
                mostrarError("El DNI debe contener solo números");
            } catch (Exception ex) {
                logger.error("Error al buscar cliente", ex);
                mostrarError("Error al buscar cliente: " + ex.getMessage());
            }
        });
        
        // Event handler para cuando se selecciona un préstamo
        cmbPrestamo.setOnAction(e -> {
            if (cmbPrestamo.getSelectionModel().getSelectedIndex() >= 0 && !prestamosCliente.isEmpty()) {
                int indiceSeleccionado = cmbPrestamo.getSelectionModel().getSelectedIndex();
                prestamoSeleccionado[0] = prestamosCliente.get(indiceSeleccionado);
                
                // Cargar cuotas pendientes del préstamo seleccionado
                cuotasPendientes.clear();
                cmbCuota.getItems().clear();
                txtMonto.clear();
                
                try {
                    List<pe.crediactiva.app.model.Cronograma> cuotasPrestamo = cronogramaDAO.findPendientesByPrestamo(prestamoSeleccionado[0].getIdPrestamo());
                    cuotasPendientes.addAll(cuotasPrestamo);
                    
                    // Ordenar cuotas por fecha programada (más antigua primero)
                    cuotasPendientes.sort((c1, c2) -> c1.getFechaProgramada().compareTo(c2.getFechaProgramada()));
                    
                    // Llenar combo con cuotas pendientes
                    for (pe.crediactiva.app.model.Cronograma cuota : cuotasPendientes) {
                        String estadoTexto = cuota.getEstadoCuota() == pe.crediactiva.app.model.Cronograma.EstadoCuota.RETRASADA ? " (RETRASADA)" : "";
                        String texto = String.format("Cuota #%d - S/ %.2f - %s%s", 
                            cuota.getNumeroCuota(), 
                            cuota.getMontoCuota(),
                            cuota.getFechaProgramada().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                            estadoTexto);
                        cmbCuota.getItems().add(texto);
                    }
                    
                    // Seleccionar automáticamente la cuota más antigua
                    if (!cuotasPendientes.isEmpty()) {
                        cmbCuota.setValue(cmbCuota.getItems().get(0));
                        pe.crediactiva.app.model.Cronograma cuotaMasAntigua = cuotasPendientes.get(0);
                        txtMonto.setText(String.format("%.2f", cuotaMasAntigua.getMontoCuota()));
                        
                        mostrarInfo("Préstamo seleccionado. Se seleccionó automáticamente la cuota más antigua pendiente.");
                    } else {
                        mostrarInfo("Préstamo seleccionado pero no tiene cuotas pendientes.");
                    }
                    
                } catch (Exception ex) {
                    logger.error("Error al cargar cuotas del préstamo", ex);
                    mostrarError("Error al cargar las cuotas del préstamo");
                }
            }
        });
        
        // Event handler para cuando se selecciona una cuota diferente
        cmbCuota.setOnAction(e -> {
            if (cmbCuota.getSelectionModel().getSelectedIndex() >= 0 && !cuotasPendientes.isEmpty()) {
                int indiceSeleccionado = cmbCuota.getSelectionModel().getSelectedIndex();
                pe.crediactiva.app.model.Cronograma cuotaSeleccionada = cuotasPendientes.get(indiceSeleccionado);
                txtMonto.setText(String.format("%.2f", cuotaSeleccionada.getMontoCuota()));
            }
        });
        
        btnRegistrar.setOnAction(e -> {
            // Validar que se haya seleccionado un cliente
            if (clienteSeleccionado[0] == null) {
                mostrarAdvertencia("Por favor busque y seleccione un cliente primero");
                return;
            }
            
            // Validar que se haya seleccionado un préstamo
            if (prestamoSeleccionado[0] == null) {
                mostrarAdvertencia("Por favor seleccione un préstamo primero");
                return;
            }
            
            // Validar que se haya seleccionado una cuota
            if (cmbCuota.getValue() == null || cmbCuota.getSelectionModel().getSelectedIndex() < 0) {
                mostrarAdvertencia("Por favor seleccione una cuota para cobrar");
                return;
            }
            
            // Validar monto
            String montoStr = txtMonto.getText().trim();
            if (montoStr.isEmpty()) {
                mostrarAdvertencia("Por favor ingrese el monto cobrado");
                return;
            }
            
            try {
                BigDecimal montoCobrado = new BigDecimal(montoStr);
                if (montoCobrado.compareTo(BigDecimal.ZERO) <= 0) {
                    mostrarAdvertencia("El monto debe ser mayor a cero");
                    return;
                }
                
                // Validar fecha
                if (dpFecha.getValue() == null) {
                    mostrarAdvertencia("Por favor seleccione la fecha de cobro");
                    return;
                }
                
                // Validar método de pago
                if (cmbMetodo.getValue() == null) {
                    mostrarAdvertencia("Por favor seleccione el método de pago");
                    return;
                }
                
                // Obtener la cuota seleccionada
                int indiceSeleccionado = cmbCuota.getSelectionModel().getSelectedIndex();
                pe.crediactiva.app.model.Cronograma cuotaSeleccionada = cuotasPendientes.get(indiceSeleccionado);
                
                // Verificar si ya existe un borrador pendiente para este préstamo
                boolean existeBorrador = recaudacionService.existeBorradorPendiente(prestamoSeleccionado[0].getIdPrestamo());
                if (existeBorrador) {
                    // Obtener el borrador pendiente para determinar qué cuota está siendo pagada
                    Optional<RecaudacionAsesor> borradorOpt = recaudacionService.obtenerBorradorPendiente(prestamoSeleccionado[0].getIdPrestamo());
                    
                    if (borradorOpt.isPresent()) {
                        // Determinar qué cuota está siendo pagada en el borrador pendiente
                        // (asumimos que siempre se paga la cuota más antigua pendiente)
                        List<pe.crediactiva.app.model.Cronograma> cuotasPrestamo = cronogramaDAO.findPendientesByPrestamo(prestamoSeleccionado[0].getIdPrestamo());
                        
                        if (!cuotasPrestamo.isEmpty()) {
                            // Ordenar cuotas por fecha programada (más antigua primero)
                            cuotasPrestamo.sort((c1, c2) -> c1.getFechaProgramada().compareTo(c2.getFechaProgramada()));
                            pe.crediactiva.app.model.Cronograma cuotaMasAntigua = cuotasPrestamo.get(0);
                            
                            // Verificar si la cuota seleccionada es la misma que está siendo pagada en el borrador
                            if (cuotaSeleccionada.getIdCuota().equals(cuotaMasAntigua.getIdCuota())) {
                                mostrarAdvertencia("⚠️ Ya existe un cobro registrado para la Cuota #" + cuotaMasAntigua.getNumeroCuota() + 
                                    " que está pendiente de validación.\n\n" +
                                    "• No se puede registrar otro cobro para esta cuota hasta que el administrador valide o rechace el cobro anterior.\n" +
                                    "• Puede seleccionar otra cuota diferente si está disponible.\n" +
                                    "• Por favor espere a que el administrador procese el cobro pendiente de la Cuota #" + cuotaMasAntigua.getNumeroCuota() + ".");
                                return;
                            }
                            // Si la cuota seleccionada es diferente, permitir el registro
                        }
                    }
                }
                
                // Registrar el cobro usando el RecaudacionService
                Long idAsesorActual = SessionManager.getInstance().getAsesorId();
                boolean cobroRegistrado = recaudacionService.registrarBorrador(
                    idAsesorActual,
                    clienteSeleccionado[0].getIdCliente(),
                    prestamoSeleccionado[0].getIdPrestamo(),
                    montoCobrado
                );
                
                if (cobroRegistrado) {
                    mostrarInfo("✅ Cobro registrado exitosamente como BORRADOR\\n\\n" +
                        "📋 Detalles del borrador:\\n" +
                        "• Cliente: " + clienteSeleccionado[0].getNombre() + " " + clienteSeleccionado[0].getApellido() + "\\n" +
                        "• Préstamo #" + prestamoSeleccionado[0].getIdPrestamo() + "\\n" +
                        "• Cuota #" + cuotaSeleccionada.getNumeroCuota() + "\\n" +
                        "• Monto: S/ " + String.format("%.2f", montoCobrado) + "\\n" +
                        "• Fecha de cobro: " + dpFecha.getValue().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) + "\\n" +
                        "• Método: " + cmbMetodo.getValue() + "\\n\\n" +
                        "⚠️ IMPORTANTE: Este cobro está PENDIENTE DE VALIDACIÓN\\n" +
                        "• La cuota seguirá PENDIENTE hasta que el administrador valide\\n" +
                        "• Solo después de la validación se marcará como PAGADA\\n" +
                        "• El administrador puede rechazar el cobro si encuentra inconsistencias");
                    
                    // Limpiar formulario
                    txtBuscarCliente.clear();
                    cmbPrestamo.getItems().clear();
                    cmbCuota.getItems().clear();
                    txtMonto.clear();
                    dpFecha.setValue(DateTimeUtil.today());
                    cmbMetodo.setValue("EFECTIVO");
                    lblClienteInfo.setText("Cliente: -");
                    lblClienteTelefono.setText("Teléfono: -");
                    lblClienteEmail.setText("Email: -");
                    clienteSeleccionado[0] = null;
                    prestamoSeleccionado[0] = null;
                    prestamosCliente.clear();
                    cuotasPendientes.clear();
                    
                } else {
                    mostrarError("Error al registrar el cobro en el sistema");
                }
                
            } catch (NumberFormatException ex) {
                mostrarError("Por favor ingrese un monto válido");
            } catch (Exception ex) {
                logger.error("Error al registrar cobro", ex);
                mostrarError("Error al registrar cobro: " + ex.getMessage());
            }
        });
        
        btnLimpiar.setOnAction(e -> {
            txtBuscarCliente.clear();
            cmbPrestamo.getItems().clear();
            cmbCuota.getItems().clear();
            txtMonto.clear();
            dpFecha.setValue(DateTimeUtil.today());
            cmbMetodo.setValue("EFECTIVO");
            lblClienteInfo.setText("Cliente: -");
            lblClienteTelefono.setText("Teléfono: -");
            lblClienteEmail.setText("Email: -");
            clienteSeleccionado[0] = null;
            prestamoSeleccionado[0] = null;
            prestamosCliente.clear();
            cuotasPendientes.clear();
        });
        
        root.getChildren().addAll(titulo, subtitulo, panelCliente, panelPrestamo, panelRegistro, panelAdvertencia);
        
        return root;
    }
    
}
