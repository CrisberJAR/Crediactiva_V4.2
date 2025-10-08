package pe.crediactiva.app.view.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import pe.crediactiva.app.service.AuthenticationService;
import pe.crediactiva.app.view.LoginController;
import pe.crediactiva.app.service.PrestamoService;
import pe.crediactiva.app.util.FechaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;

/**
 * Controlador principal para la interfaz del administrador
 */
public class AdminMainController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminMainController.class);
    
    @FXML
    private Label txtUsuarioInfo;
    
    @FXML
    private Label txtFechaHoy;
    
    @FXML
    private Label txtSolicitudesPendientes;
    
    @FXML
    private Label txtPrestamosActivos;
    
    @FXML
    private Label txtClientesMorosos;
    
    @FXML
    private Label txtRecaudacionHoy;
    
    @FXML
    private Label txtTotalClientes;
    
    @FXML
    private Label txtTotalAsesores;
    
    @FXML
    private Label txtCapitalTotal;
    
    @FXML
    private Label txtUltimaActualizacion;
    
    @FXML
    private Label lblUltimaActualizacion;
    
    @FXML
    private VBox contentArea;
    
    @FXML
    private Button btnVolverDashboard;
    
    @FXML
    private Button btnLogout;
    
    private Stage primaryStage;
    private AuthenticationService authService;
    private PrestamoService prestamoService;
    
    public AdminMainController() {
        this.authService = new AuthenticationService();
        this.prestamoService = new PrestamoService();
    }
    
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    @FXML
    private void initialize() {
        try {
            // Configurar información del usuario
            if (authService.getCurrentUser() != null) {
                txtUsuarioInfo.setText("Usuario: " + authService.getCurrentUser().getIdUsuario() + " - Administrador");
            }
            
            // Configurar fecha actual
            txtFechaHoy.setText("Fecha: " + FechaUtil.formatearFecha(LocalDate.now()));
            
            // Cargar estadísticas del dashboard
            loadDashboardStats();
            
        } catch (Exception e) {
            logger.error("Error al inicializar la pantalla del administrador", e);
        }
    }
    
    /**
     * Carga las estadísticas del dashboard
     */
    private void loadDashboardStats() {
        try {
            // Solicitudes pendientes
            int solicitudesPendientes = prestamoService.obtenerPrestamosPendientes().size();
            txtSolicitudesPendientes.setText(String.valueOf(solicitudesPendientes));
            
            // Préstamos activos
            int prestamosActivos = prestamoService.obtenerPrestamosActivos().size();
            txtPrestamosActivos.setText(String.valueOf(prestamosActivos));
            
            // TODO: Implementar clientes morosos y recaudación del día
            txtClientesMorosos.setText("0");
            txtRecaudacionHoy.setText("S/ 0.00");
            
            // Estadísticas adicionales
            txtTotalClientes.setText("0"); // TODO: Implementar
            txtTotalAsesores.setText("0"); // TODO: Implementar
            txtCapitalTotal.setText("S/ 0.00"); // TODO: Implementar
            
            // Última actualización
            String fechaActual = java.time.LocalDateTime.now().format(
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
            txtUltimaActualizacion.setText(fechaActual);
            lblUltimaActualizacion.setText("Última actualización: " + fechaActual);
            
        } catch (Exception e) {
            logger.error("Error al cargar estadísticas del dashboard", e);
        }
    }
    
    /**
     * Maneja la opción de bandeja de solicitudes
     */
    @FXML
    private void handleBandejaSolicitudes() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/BandejaSolicitudesView.fxml"));
            javafx.scene.layout.BorderPane bandejaView = loader.load();
            
            // Reemplazar contenido
            contentArea.getChildren().clear();
            contentArea.getChildren().add(bandejaView);
            
            logger.info("Cargada bandeja de solicitudes");
            
        } catch (IOException e) {
            logger.error("Error al cargar bandeja de solicitudes", e);
            showError("Error al cargar la bandeja de solicitudes: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error inesperado al cargar bandeja de solicitudes", e);
            showError("Error inesperado: " + e.getMessage());
        }
    }
    
    /**
     * Maneja la opción de nueva solicitud
     */
    @FXML
    private void handleNuevaSolicitud() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/NuevaSolicitudView.fxml"));
            VBox nuevaSolicitudView = loader.load();
            
            // Reemplazar contenido
            contentArea.getChildren().clear();
            contentArea.getChildren().add(nuevaSolicitudView);
            
            logger.info("Cargada nueva solicitud");
            
        } catch (IOException e) {
            logger.error("Error al cargar nueva solicitud", e);
            showError("Error al cargar el formulario de nueva solicitud");
        }
    }
    
    /**
     * Maneja la opción de administrar pagos
     */
    @FXML
    private void handleAdministrarPagos() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/AdministrarPagosView.fxml"));
            VBox administrarPagosView = loader.load();
            
            // Reemplazar contenido
            contentArea.getChildren().clear();
            contentArea.getChildren().add(administrarPagosView);
            
            logger.info("Cargada administración de pagos");
            
        } catch (IOException e) {
            logger.error("Error al cargar administración de pagos", e);
            showError("Error al cargar la administración de pagos");
        }
    }
    
    /**
     * Maneja la opción de validar cobros
     */
    @FXML
    private void handleValidarCobros() {
        // TODO: Implementar validación de cobros
        showInfo("Funcionalidad de validación de cobros en desarrollo");
    }
    
    /**
     * Maneja la opción de gestionar usuarios
     */
    @FXML
    private void handleGestionarUsuarios() {
        // TODO: Implementar gestión de usuarios
        showInfo("Funcionalidad de gestión de usuarios en desarrollo");
    }
    
    /**
     * Maneja la opción de gestionar asesores
     */
    @FXML
    private void handleGestionarAsesores() {
        // TODO: Implementar gestión de asesores
        showInfo("Funcionalidad de gestión de asesores en desarrollo");
    }
    
    /**
     * Maneja la opción de gestionar clientes
     */
    @FXML
    private void handleGestionarClientes() {
        // TODO: Implementar gestión de clientes
        showInfo("Funcionalidad de gestión de clientes en desarrollo");
    }
    
    /**
     * Maneja la opción de movimientos de capital
     */
    @FXML
    private void handleMovimientosCapital() {
        // TODO: Implementar movimientos de capital
        showInfo("Funcionalidad de movimientos de capital en desarrollo");
    }
    
    /**
     * Maneja la opción de reporte por cliente
     */
    @FXML
    private void handleReporteClientes() {
        // TODO: Implementar reporte por cliente
        showInfo("Funcionalidad de reporte por cliente en desarrollo");
    }
    
    /**
     * Maneja la opción de reporte por asesor
     */
    @FXML
    private void handleReporteAsesores() {
        // TODO: Implementar reporte por asesor
        showInfo("Funcionalidad de reporte por asesor en desarrollo");
    }
    
    /**
     * Maneja la opción de reporte por préstamo
     */
    @FXML
    private void handleReportePrestamos() {
        // TODO: Implementar reporte por préstamo
        showInfo("Funcionalidad de reporte por préstamo en desarrollo");
    }
    
    /**
     * Maneja la opción de reporte consolidado
     */
    @FXML
    private void handleReporteConsolidado() {
        // TODO: Implementar reporte consolidado
        showInfo("Funcionalidad de reporte consolidado en desarrollo");
    }
    
    /**
     * Maneja la opción de reporte de sueldos
     */
    @FXML
    private void handleReporteSueldos() {
        // TODO: Implementar reporte de sueldos
        showInfo("Funcionalidad de reporte de sueldos en desarrollo");
    }
    
    /**
     * Maneja la opción de auditoría
     */
    @FXML
    private void handleAuditoria() {
        // TODO: Implementar auditoría
        showInfo("Funcionalidad de auditoría en desarrollo");
    }
    
    /**
     * Maneja la opción de configuración
     */
    @FXML
    private void handleConfiguracion() {
        // TODO: Implementar configuración
        showInfo("Funcionalidad de configuración en desarrollo");
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
            
            logger.info("Usuario cerró sesión");
            
        } catch (IOException e) {
            logger.error("Error al cerrar sesión", e);
        }
    }
    
    /**
     * Vuelve al dashboard administrativo
     */
    @FXML
    private void handleVolverDashboard() {
        try {
            // Limpiar el área de contenido
            contentArea.getChildren().clear();
            
            // Cargar el dashboard por defecto
            cargarDashboard();
            
            logger.info("Volviendo al dashboard administrativo");
            
        } catch (Exception e) {
            logger.error("Error al volver al dashboard", e);
        }
    }
    
    /**
     * Carga el dashboard por defecto
     */
    private void cargarDashboard() {
        try {
            // Dashboard compacto
            VBox dashboard = new VBox(15.0);
            dashboard.setStyle("-fx-background-color: transparent; -fx-padding: 15;");
            
            Label titulo = new Label("Dashboard Administrativo");
            titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 10 0;");
            
            // Estadísticas compactas en 2 filas
            GridPane estadisticas = new GridPane();
            estadisticas.setHgap(10.0);
            estadisticas.setVgap(10.0);
            
            // Configurar columnas
            for (int i = 0; i < 4; i++) {
                ColumnConstraints col = new ColumnConstraints();
                col.setHgrow(javafx.scene.layout.Priority.ALWAYS);
                col.setMinWidth(150.0);
                estadisticas.getColumnConstraints().add(col);
            }
            
            // Configurar filas
            for (int i = 0; i < 2; i++) {
                RowConstraints row = new RowConstraints();
                row.setMinHeight(80.0);
                row.setVgrow(javafx.scene.layout.Priority.NEVER);
                estadisticas.getRowConstraints().add(row);
            }
            
            // Fila 1: Métricas principales
            estadisticas.add(crearTarjetaEstadistica("📥", "Solicitudes", txtSolicitudesPendientes.getText()), 0, 0);
            estadisticas.add(crearTarjetaEstadistica("💳", "Préstamos", txtPrestamosActivos.getText()), 1, 0);
            estadisticas.add(crearTarjetaEstadistica("⚠️", "Morosos", txtClientesMorosos.getText()), 2, 0);
            estadisticas.add(crearTarjetaEstadistica("💰", "Recaudación", txtRecaudacionHoy.getText()), 3, 0);
            
            // Fila 2: Métricas secundarias
            estadisticas.add(crearTarjetaEstadistica("👥", "Clientes", txtTotalClientes.getText()), 0, 1);
            estadisticas.add(crearTarjetaEstadistica("🎯", "Asesores", txtTotalAsesores.getText()), 1, 1);
            estadisticas.add(crearTarjetaEstadistica("🏦", "Capital", txtCapitalTotal.getText()), 2, 1);
            estadisticas.add(crearTarjetaEstadistica("🕒", "Actualizado", txtUltimaActualizacion.getText()), 3, 1);
            
            // Acciones rápidas compactas
            VBox acciones = new VBox(10.0);
            Label tituloAcciones = new Label("🚀 Acciones Rápidas");
            tituloAcciones.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 0 0 8 0;");
            
            HBox botonesAcciones = new HBox(10.0);
            botonesAcciones.setAlignment(javafx.geometry.Pos.CENTER);
            
            Button btnSolicitudes = new Button("📥 Solicitudes");
            btnSolicitudes.setOnAction(e -> handleBandejaSolicitudes());
            btnSolicitudes.setStyle("-fx-background-color: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%); -fx-text-fill: #2c3e50; -fx-font-weight: 600; -fx-font-size: 12px; -fx-padding: 8 15; -fx-background-radius: 6px; -fx-cursor: hand;");
            
            Button btnPagos = new Button("💳 Pagos");
            btnPagos.setOnAction(e -> handleAdministrarPagos());
            btnPagos.setStyle("-fx-background-color: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%); -fx-text-fill: #2c3e50; -fx-font-weight: 600; -fx-font-size: 12px; -fx-padding: 8 15; -fx-background-radius: 6px; -fx-cursor: hand;");
            
            Button btnReportes = new Button("📊 Reportes");
            btnReportes.setOnAction(e -> handleReporteConsolidado());
            btnReportes.setStyle("-fx-background-color: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%); -fx-text-fill: #2c3e50; -fx-font-weight: 600; -fx-font-size: 12px; -fx-padding: 8 15; -fx-background-radius: 6px; -fx-cursor: hand;");
            
            Button btnUsuarios = new Button("👥 Usuarios");
            btnUsuarios.setOnAction(e -> handleGestionarUsuarios());
            btnUsuarios.setStyle("-fx-background-color: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%); -fx-text-fill: #2c3e50; -fx-font-weight: 600; -fx-font-size: 12px; -fx-padding: 8 15; -fx-background-radius: 6px; -fx-cursor: hand;");
            
            botonesAcciones.getChildren().addAll(btnSolicitudes, btnPagos, btnReportes, btnUsuarios);
            acciones.getChildren().addAll(tituloAcciones, botonesAcciones);
            
            // Mensaje de bienvenida compacto
            VBox bienvenida = new VBox(10.0);
            bienvenida.setAlignment(javafx.geometry.Pos.CENTER);
            bienvenida.setStyle("-fx-background-color: linear-gradient(135deg, #87CEEB 0%, #4169E1 100%); -fx-background-radius: 10px; -fx-padding: 15;");
            
            Label tituloBienvenida = new Label("👑 Bienvenido al Panel de Administración");
            tituloBienvenida.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");
            
            Label subtituloBienvenida = new Label("Seleccione una opción del menú lateral para comenzar");
            subtituloBienvenida.setStyle("-fx-font-size: 12px; -fx-text-fill: rgba(255,255,255,0.9);");
            
            bienvenida.getChildren().addAll(tituloBienvenida, subtituloBienvenida);
            
            // Agregar todo al dashboard
            dashboard.getChildren().addAll(titulo, estadisticas, acciones, bienvenida);
            
            // Agregar al contenido
            contentArea.getChildren().add(dashboard);
            
        } catch (Exception e) {
            logger.error("Error al cargar dashboard", e);
        }
    }
    
    /**
     * Crea una tarjeta de estadística compacta
     */
    private VBox crearTarjetaEstadistica(String icono, String titulo, String valor) {
        VBox tarjeta = new VBox(5.0);
        tarjeta.setAlignment(javafx.geometry.Pos.CENTER);
        tarjeta.setStyle("-fx-background-color: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%); -fx-background-radius: 8px; -fx-border-color: rgba(135,206,235,0.2); -fx-border-width: 1px; -fx-border-radius: 8px; -fx-padding: 10; -fx-min-height: 70px;");
        
        Label iconoLabel = new Label(icono);
        iconoLabel.setStyle("-fx-font-size: 18px; -fx-padding: 0 0 3 0;");
        
        Label tituloLabel = new Label(titulo);
        tituloLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #7f8c8d; -fx-font-weight: 600; -fx-padding: 0 0 3 0;");
        
        Label valorLabel = new Label(valor);
        valorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");
        
        tarjeta.getChildren().addAll(iconoLabel, tituloLabel, valorLabel);
        return tarjeta;
    }
    
    /**
     * Muestra un mensaje de información
     */
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Información");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Muestra un mensaje de error
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
