package pe.crediactiva.app.view.cliente;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import pe.crediactiva.app.config.SessionManager;
import pe.crediactiva.app.model.Cliente;
import pe.crediactiva.app.service.AuthenticationService;
import pe.crediactiva.app.view.LoginController;
import pe.crediactiva.app.view.cliente.PrestamosClienteController;
import pe.crediactiva.app.view.cliente.CronogramasClienteController;
import pe.crediactiva.app.view.cliente.SimuladorClienteController;
import pe.crediactiva.app.view.cliente.RecibosClienteController;
import pe.crediactiva.app.view.cliente.HistorialClienteController;
import pe.crediactiva.app.service.ClienteService;
import pe.crediactiva.app.service.PrestamoService;
import pe.crediactiva.app.util.FechaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Optional;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controlador principal para la interfaz del cliente
 */
public class ClienteMainController {
    
    private static final Logger logger = LoggerFactory.getLogger(ClienteMainController.class);
    
    @FXML
    private Label lblClienteInfo;
    
    @FXML
    private Label lblFechaHoy;
    
    @FXML
    private Label lblCapitalAcumulado;
    
    @FXML
    private Label lblEtiquetaCliente;
    
    @FXML
    private VBox contentArea;
    
    @FXML
    private Button btnLogout;
    
    @FXML
    private Label lblDni;
    
    @FXML
    private Label lblNombre;
    
    @FXML
    private Label lblApellido;
    
    @FXML
    private Label lblTelefono;
    
    @FXML
    private Label lblEmail;
    
    @FXML
    private Label lblAsesor;
    
    @FXML
    private Label lblDireccion;
    
    @FXML
    private Label lblCapitalAcumuladoCard;
    
    @FXML
    private Label lblPrestamosActivos;
    
    @FXML
    private Label lblCuotasPendientes;
    
    @FXML
    private Label lblCuotasVencidas;
    
    @FXML
    private Label lblTotalPagado;
    
    @FXML
    private Label lblMontoPendiente;
    
    @FXML
    private Label lblEtiquetaClienteCard;
    
    @FXML
    private Label lblUltimoPago;
    
    @FXML
    private Label lblUltimaActualizacion;
    
    private Stage primaryStage;
    private AuthenticationService authService;
    private ClienteService clienteService;
    private PrestamoService prestamoService;
    private Cliente clienteActual;
    
    public ClienteMainController() {
        this.authService = new AuthenticationService();
        this.clienteService = new ClienteService();
        this.prestamoService = new PrestamoService();
    }
    
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    @FXML
    private void initialize() {
        try {
            // Configurar información del cliente
            try {
                if (authService.getCurrentUser() != null) {
                    lblClienteInfo.setText("Cliente: " + authService.getCurrentUser().getIdUsuario());
                    
                    // Obtener datos del cliente
                    Optional<Cliente> clienteOpt = clienteService.obtenerClientePorDni(authService.getCurrentUser().getIdUsuario());
                    if (clienteOpt.isPresent()) {
                        clienteActual = clienteOpt.get();
                        mostrarDatosCliente(clienteActual);
                    }
                } else {
                    lblClienteInfo.setText("Cliente: Usuario actual");
                }
            } catch (Exception e) {
                logger.warn("No se pudo obtener información del cliente actual", e);
                lblClienteInfo.setText("Cliente: Usuario actual");
            }
            
            // Configurar fecha actual
            lblFechaHoy.setText("Fecha: " + FechaUtil.formatearFecha(LocalDate.now()));
            
            // Cargar estadísticas del resumen
            try {
                cargarEstadisticasResumen();
            } catch (Exception e) {
                logger.warn("No se pudieron cargar las estadísticas del resumen", e);
            }
            
            // Configurar última actualización
            lblUltimaActualizacion.setText("Última actualización: " + 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            
        } catch (Exception e) {
            logger.error("Error al inicializar la pantalla del cliente", e);
        }
    }
    
    /**
     * Muestra los datos del cliente
     */
    private void mostrarDatosCliente(Cliente cliente) {
        lblDni.setText(String.valueOf(cliente.getIdCliente()));
        lblNombre.setText(cliente.getNombre());
        lblApellido.setText(cliente.getApellido());
        lblTelefono.setText(cliente.getTelefono());
        lblEmail.setText(cliente.getEmail());
        lblDireccion.setText(cliente.getDireccion());
        lblAsesor.setText("Asesor " + cliente.getIdAsesor()); // TODO: Obtener nombre del asesor
        
        // Mostrar información financiera
        lblCapitalAcumulado.setText("Capital Acumulado: S/ " + String.format("%.2f", cliente.getSaldoCapital()));
        lblCapitalAcumuladoCard.setText("S/ " + String.format("%.2f", cliente.getSaldoCapital()));
        lblEtiquetaCliente.setText("Etiqueta: " + cliente.getEtiquetaCliente().name());
        lblEtiquetaClienteCard.setText(cliente.getEtiquetaCliente().name());
    }
    
    /**
     * Carga las estadísticas del resumen
     */
    private void cargarEstadisticasResumen() {
        try {
            if (clienteActual == null) {
                // Mostrar valores por defecto si no hay cliente
                lblPrestamosActivos.setText("0");
                lblCuotasPendientes.setText("0");
                lblCuotasVencidas.setText("0");
                lblTotalPagado.setText("S/ 0.00");
                lblMontoPendiente.setText("S/ 0.00");
                lblUltimoPago.setText("N/A");
                return;
            }
            
            // Préstamos activos - con manejo de errores
            try {
                int prestamosActivos = prestamoService.obtenerPrestamosActivosPorCliente(clienteActual.getIdCliente()).size();
                lblPrestamosActivos.setText(String.valueOf(prestamosActivos));
            } catch (Exception e) {
                logger.warn("Error al obtener préstamos activos", e);
                lblPrestamosActivos.setText("0");
            }
            
            // Cuotas pendientes - con manejo de errores
            try {
                int cuotasPendientes = prestamoService.obtenerCuotasPendientesPorCliente(clienteActual.getIdCliente()).size();
                lblCuotasPendientes.setText(String.valueOf(cuotasPendientes));
            } catch (Exception e) {
                logger.warn("Error al obtener cuotas pendientes", e);
                lblCuotasPendientes.setText("0");
            }
            
            // Cuotas vencidas - con manejo de errores
            try {
                int cuotasVencidas = prestamoService.obtenerCuotasVencidasPorCliente(clienteActual.getIdCliente()).size();
                lblCuotasVencidas.setText(String.valueOf(cuotasVencidas));
            } catch (Exception e) {
                logger.warn("Error al obtener cuotas vencidas", e);
                lblCuotasVencidas.setText("0");
            }
            
            // Total pagado - con manejo de errores
            try {
                double totalPagado = prestamoService.obtenerTotalPagadoPorCliente(clienteActual.getIdCliente());
                lblTotalPagado.setText("S/ " + String.format("%.2f", totalPagado));
            } catch (Exception e) {
                logger.warn("Error al obtener total pagado", e);
                lblTotalPagado.setText("S/ 0.00");
            }
            
            // Monto pendiente - con manejo de errores
            try {
                double montoPendiente = prestamoService.obtenerMontoPendientePorCliente(clienteActual.getIdCliente());
                lblMontoPendiente.setText("S/ " + String.format("%.2f", montoPendiente));
            } catch (Exception e) {
                logger.warn("Error al obtener monto pendiente", e);
                lblMontoPendiente.setText("S/ 0.00");
            }
            
            // Último pago - con manejo de errores
            try {
                String ultimoPago = prestamoService.obtenerUltimoPagoPorCliente(clienteActual.getIdCliente());
                lblUltimoPago.setText(ultimoPago != null ? ultimoPago : "--");
            } catch (Exception e) {
                logger.warn("Error al obtener último pago", e);
                lblUltimoPago.setText("--");
            }
            
        } catch (Exception e) {
            logger.error("Error al cargar estadísticas del resumen", e);
        }
    }
    
    /**
     * Maneja la opción de resumen
     */
    @FXML
    private void handleResumen() {
        try {
            // Recargar estadísticas
            cargarEstadisticasResumen();
            
            // Mostrar mensaje de actualización
            mostrarInfo("Resumen actualizado");
            
        } catch (Exception e) {
            logger.error("Error al actualizar resumen", e);
            mostrarError("Error al actualizar el resumen");
        }
    }
    
    /**
     * Maneja la opción de préstamos
     */
    @FXML
    private void handlePrestamos() {
        try {
            // Cargar la pantalla de préstamos del cliente
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cliente/PrestamosClienteView.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            PrestamosClienteController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);
            
            primaryStage.setTitle("CrediActiva - Mis Préstamos");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            
        } catch (Exception e) {
            logger.error("Error al cargar pantalla de préstamos", e);
            mostrarError("Error al cargar la pantalla de préstamos");
        }
    }
    
    /**
     * Maneja la opción de cronogramas
     */
    @FXML
    private void handleCronogramas() {
        try {
            // Cargar la pantalla de cronogramas del cliente
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cliente/CronogramasClienteView.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            CronogramasClienteController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);
            
            primaryStage.setTitle("CrediActiva - Mis Cronogramas");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            
        } catch (Exception e) {
            logger.error("Error al cargar pantalla de cronogramas", e);
            mostrarError("Error al cargar la pantalla de cronogramas");
        }
    }
    
    /**
     * Maneja la opción de recibos
     */
    @FXML
    private void handleRecibos() {
        try {
            // Cargar la pantalla de recibos del cliente
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cliente/RecibosClienteView.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            RecibosClienteController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);
            
            primaryStage.setTitle("CrediActiva - Descargar Recibos");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            
        } catch (Exception e) {
            logger.error("Error al cargar pantalla de recibos", e);
            mostrarError("Error al cargar la pantalla de recibos");
        }
    }
    
    /**
     * Maneja la opción de simulador
     */
    @FXML
    private void handleSimulador() {
        try {
            // Crear una nueva ventana para el simulador
            Stage simuladorStage = new Stage();
            
            // Cargar la pantalla del simulador del cliente
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cliente/SimuladorClienteView.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            SimuladorClienteController controller = loader.getController();
            controller.setPrimaryStage(simuladorStage);
            
            // Configurar la nueva ventana
            simuladorStage.setTitle("CrediActiva - Simulador de Crédito");
            simuladorStage.setScene(scene);
            simuladorStage.setMaximized(true);
            simuladorStage.setMinWidth(1200);
            simuladorStage.setMinHeight(800);
            
            // Centrar la ventana
            simuladorStage.centerOnScreen();
            
            // Mostrar la ventana
            simuladorStage.show();
            
            // Traer al frente
            simuladorStage.toFront();
            
        } catch (Exception e) {
            logger.error("Error al cargar pantalla del simulador", e);
            mostrarError("Error al cargar la pantalla del simulador");
        }
    }
    
    /**
     * Maneja la opción de historial
     */
    @FXML
    private void handleHistorial() {
        try {
            // Cargar la pantalla de historial del cliente
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cliente/HistorialClienteView.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            HistorialClienteController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);
            
            primaryStage.setTitle("CrediActiva - Mi Historial");
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            
        } catch (Exception e) {
            logger.error("Error al cargar pantalla de historial", e);
            mostrarError("Error al cargar la pantalla de historial");
        }
    }
    
    /**
     * Maneja la opción de ver préstamos
     */
    @FXML
    private void handleVerPrestamos() {
        handlePrestamos();
    }
    
    /**
     * Maneja la opción de ver cronogramas
     */
    @FXML
    private void handleVerCronogramas() {
        handleCronogramas();
    }
    
    /**
     * Maneja la opción de simular crédito
     */
    @FXML
    private void handleSimularCredito() {
        handleSimulador();
    }
    
    /**
     * Maneja la opción de descargar recibos
     */
    @FXML
    private void handleDescargarRecibos() {
        handleRecibos();
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
            
            logger.info("Cliente cerró sesión");
            
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
}
