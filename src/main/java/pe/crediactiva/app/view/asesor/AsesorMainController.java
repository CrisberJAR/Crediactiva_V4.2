package pe.crediactiva.app.view.asesor;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.Scene;
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
import pe.crediactiva.app.dao.CronogramaDAO;
import pe.crediactiva.app.dao.impl.CronogramaDAOImpl;
import pe.crediactiva.app.config.SessionManager;
import pe.crediactiva.app.view.LoginController;
import pe.crediactiva.app.model.Cliente;
import pe.crediactiva.app.model.Prestamo;
import pe.crediactiva.app.model.RecaudacionAsesor;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
    private CronogramaDAO cronogramaDAO;
    
    public AsesorMainController() {
        this.authService = new AuthenticationService();
        this.prestamoService = new PrestamoService();
        this.clienteService = new ClienteService();
        this.recaudacionService = new RecaudacionService();
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
            lblFechaHoy.setText("Fecha: " + LocalDate.now().toString());
            
            // Cargar estadísticas del dashboard
            cargarEstadisticasDashboard();
            
            // Configurar última actualización
            lblUltimaActualizacion.setText("Última actualización: " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            
        } catch (Exception e) {
            logger.error("Error al inicializar la pantalla del asesor", e);
        }
    }
    
    /**
     * Carga las estadísticas del dashboard
     */
    private void cargarEstadisticasDashboard() {
        try {
            // Cuotas del día
            int cuotasDia = prestamoService.obtenerCuotasDelDia().size();
            lblCuotasDia.setText(String.valueOf(cuotasDia));
            
            // Cuotas vencidas
            int cuotasVencidas = prestamoService.obtenerCuotasVencidas().size();
            lblCuotasVencidas.setText(String.valueOf(cuotasVencidas));
            
            // Recaudación del día
            // TODO: Implementar recaudación
            lblRecaudacionDia.setText("S/ 0.00");
            lblRecaudacionMes.setText("Recaudación Mes: S/ 0.00");
            lblRecaudacionMesCard.setText("S/ 0.00");
            
            // Sueldo estimado (10% de la recaudación del mes)
            double sueldoEstimado = 0.0;
            lblSueldoEstimado.setText("Sueldo Estimado: S/ 0.00");
            lblSueldoEstimadoCard.setText("S/ " + String.format("%.2f", sueldoEstimado));
            
            // Clientes activos
            int clientesActivos = 0; // TODO: Implementar en ClienteService
            lblClientesActivos.setText(String.valueOf(clientesActivos));
            
            // Préstamos activos
            int prestamosActivos = prestamoService.obtenerPrestamosActivos().size();
            lblPrestamosActivos.setText(String.valueOf(prestamosActivos));
            
            // Morosidad (porcentaje de cuotas vencidas)
            double morosidad = prestamoService.calcularMorosidad();
            lblMorosidad.setText(String.format("%.1f%%", morosidad));
            
        } catch (Exception e) {
            logger.error("Error al cargar estadísticas del dashboard", e);
        }
    }
    
    /**
     * Maneja la opción de dashboard
     */
    @FXML
    private void handleDashboard() {
        try {
            // Recargar estadísticas
            cargarEstadisticasDashboard();
            
            // Mostrar mensaje de actualización
            mostrarInfo("Dashboard actualizado");
            
        } catch (Exception e) {
            logger.error("Error al actualizar dashboard", e);
            mostrarError("Error al actualizar el dashboard");
        }
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
            logger.info("Intentando cargar registro de cobro...");
            
            VBox registrarCobroView = crearRegistrarCobroView();
            
            // Crear ScrollPane para hacer scrolleable el contenido
            ScrollPane scrollPane = new ScrollPane();
            scrollPane.setContent(registrarCobroView);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            
            // Reemplazar contenido
            contentArea.getChildren().clear();
            contentArea.getChildren().add(scrollPane);
            
            logger.info("Cargado registro de cobro exitosamente con scroll");
            
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/asesor/CuotasDiaView.fxml"));
            VBox cuotasDiaView = loader.load();
            
            // Reemplazar contenido
            contentArea.getChildren().clear();
            contentArea.getChildren().add(cuotasDiaView);
            
            logger.info("Cargadas cuotas del día");
            
        } catch (IOException e) {
            logger.error("Error al cargar cuotas del día", e);
            mostrarError("Error al cargar las cuotas del día");
        }
    }
    
    /**
     * Maneja la opción de ver cuotas vencidas
     */
    @FXML
    private void handleVerCuotasVencidas() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/asesor/CuotasVencidasView.fxml"));
            VBox cuotasVencidasView = loader.load();
            
            // Reemplazar contenido
            contentArea.getChildren().clear();
            contentArea.getChildren().add(cuotasVencidasView);
            
            logger.info("Cargadas cuotas vencidas");
            
        } catch (IOException e) {
            logger.error("Error al cargar cuotas vencidas", e);
            mostrarError("Error al cargar las cuotas vencidas");
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/asesor/RecaudacionMesView.fxml"));
            VBox recaudacionMesView = loader.load();
            
            // Reemplazar contenido
            contentArea.getChildren().clear();
            contentArea.getChildren().add(recaudacionMesView);
            
            logger.info("Cargada recaudación del mes");
            
        } catch (IOException e) {
            logger.error("Error al cargar recaudación del mes", e);
            mostrarError("Error al cargar la recaudación del mes");
        }
    }
    
    /**
     * Maneja la opción de ver clientes activos
     */
    @FXML
    private void handleVerClientesActivos() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/asesor/ClientesActivosView.fxml"));
            VBox clientesActivosView = loader.load();
            
            // Reemplazar contenido
            contentArea.getChildren().clear();
            contentArea.getChildren().add(clientesActivosView);
            
            logger.info("Cargados clientes activos");
            
        } catch (IOException e) {
            logger.error("Error al cargar clientes activos", e);
            mostrarError("Error al cargar los clientes activos");
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
        dpFechaInicio.setValue(LocalDate.now());
        
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
            dpFechaInicio.setValue(LocalDate.now());
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
            // Obtener el ID del asesor actual (12345678)
            Long idAsesor = 12345678L;
            
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
            Long idAsesor = 12345678L;
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
        dpFechaContrato.setValue(LocalDate.now());
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
            nuevoCliente.setIdAsesor(12345678L); // ID del asesor actual
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
            double tasaInteres = 18.0; // 18% mensual
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
            
            // Obtener plazo en meses
            String plazoStr = cmbPlazo.getValue();
            int plazoMeses = 1;
            if (plazoStr.equals("2 meses")) plazoMeses = 2;
            else if (plazoStr.equals("3 meses")) plazoMeses = 3;
            
            // Crear el préstamo
            Prestamo nuevoPrestamo = new Prestamo();
            nuevoPrestamo.setIdCliente(idCliente);
            nuevoPrestamo.setIdAsesor(12345678L); // ID del asesor actual
            nuevoPrestamo.setMontoSolicitado(new java.math.BigDecimal(monto));
            nuevoPrestamo.setMontoDesembolsado(new java.math.BigDecimal("0.00")); // Por defecto 0
            nuevoPrestamo.setTasaInteres(new java.math.BigDecimal("18.00")); // 18% por defecto
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
                dpFechaInicio.setValue(LocalDate.now());
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
        TextField txtInteres = new TextField("18.00");
        txtInteres.setPromptText("Ej: 18");
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
        btnSimular.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 12 25; -fx-background-radius: 8; -fx-cursor: hand;");
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
            txtInteres.setText("18.00");
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
            LocalDate fechaInicio = LocalDate.now();
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
            LocalDate fechaInicio = LocalDate.now();
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
        
        // Panel de información del flujo
        VBox panelInfo = new VBox(15);
        panelInfo.setPadding(new Insets(20));
        panelInfo.setStyle("-fx-background-color: #e8f4fd; -fx-background-radius: 10; -fx-border-color: #3498db; -fx-border-radius: 10;");
        
        Label tituloInfo = new Label("📋 Flujo de Registro de Cobros");
        tituloInfo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label info1 = new Label("1️⃣ Selecciona el cliente del cual vas a cobrar");
        info1.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 13px;");
        
        Label info2 = new Label("2️⃣ Selecciona el préstamo y la cuota específica");
        info2.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 13px;");
        
        Label info3 = new Label("3️⃣ Registra el monto cobrado y el método de pago");
        info3.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 13px;");
        
        Label info4 = new Label("4️⃣ El cobro se registra como borrador pendiente de validación");
        info4.setStyle("-fx-text-fill: #2c3e50; -fx-font-size: 13px;");
        
        panelInfo.getChildren().addAll(tituloInfo, info1, info2, info3, info4);
        
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
        dpFecha.setValue(LocalDate.now());
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
                    dpFecha.setValue(LocalDate.now());
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
            dpFecha.setValue(LocalDate.now());
            cmbMetodo.setValue("EFECTIVO");
            lblClienteInfo.setText("Cliente: -");
            lblClienteTelefono.setText("Teléfono: -");
            lblClienteEmail.setText("Email: -");
            clienteSeleccionado[0] = null;
            prestamoSeleccionado[0] = null;
            prestamosCliente.clear();
            cuotasPendientes.clear();
        });
        
        root.getChildren().addAll(titulo, subtitulo, panelInfo, panelCliente, panelPrestamo, panelRegistro, panelAdvertencia);
        
        return root;
    }
    
}
