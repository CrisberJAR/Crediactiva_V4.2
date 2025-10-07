package pe.crediactiva.app.view.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
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
    private Text txtUsuarioInfo;
    
    @FXML
    private Text txtFechaHoy;
    
    @FXML
    private Text txtSolicitudesPendientes;
    
    @FXML
    private Text txtPrestamosActivos;
    
    @FXML
    private Text txtClientesMorosos;
    
    @FXML
    private Text txtRecaudacionHoy;
    
    @FXML
    private VBox contentArea;
    
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
            VBox bandejaView = loader.load();
            
            // Reemplazar contenido
            contentArea.getChildren().clear();
            contentArea.getChildren().add(bandejaView);
            
            logger.info("Cargada bandeja de solicitudes");
            
        } catch (IOException e) {
            logger.error("Error al cargar bandeja de solicitudes", e);
            showError("Error al cargar la bandeja de solicitudes");
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
