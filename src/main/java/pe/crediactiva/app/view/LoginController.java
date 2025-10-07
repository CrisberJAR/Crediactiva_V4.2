package pe.crediactiva.app.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import pe.crediactiva.app.service.AuthenticationService;
import pe.crediactiva.app.view.admin.AdminMainController;
import pe.crediactiva.app.view.asesor.AsesorMainController;
import pe.crediactiva.app.view.cliente.ClienteMainController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Controlador para la pantalla de login
 */
public class LoginController {
    
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    
    @FXML
    private TextField txtUsuario;
    
    @FXML
    private PasswordField txtPassword;
    
    @FXML
    private Button btnLogin;
    
    @FXML
    private Label lblError;
    
    private Stage primaryStage;
    private AuthenticationService authService;
    
    public LoginController() {
        this.authService = new AuthenticationService();
    }
    
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    @FXML
    private void initialize() {
        // Configurar eventos
        txtPassword.setOnAction(e -> handleLogin());
        btnLogin.setDefaultButton(true);
        
        // Limpiar mensajes de error cuando el usuario escriba
        txtUsuario.textProperty().addListener((obs, oldText, newText) -> clearError());
        txtPassword.textProperty().addListener((obs, oldText, newText) -> clearError());
        
        // Limpiar campos al inicializar (útil cuando se regresa del logout)
        clearFields();
    }
    
    @FXML
    private void handleLogin() {
        try {
            // Validar campos
            if (txtUsuario.getText().trim().isEmpty()) {
                showError("Por favor ingrese su DNI");
                txtUsuario.requestFocus();
                return;
            }
            
            if (txtPassword.getText().trim().isEmpty()) {
                showError("Por favor ingrese su contraseña");
                txtPassword.requestFocus();
                return;
            }
            
            // Obtener credenciales
            Long idUsuario;
            try {
                idUsuario = Long.parseLong(txtUsuario.getText().trim());
            } catch (NumberFormatException e) {
                showError("El DNI debe ser un número válido");
                txtUsuario.requestFocus();
                return;
            }
            
            String password = txtPassword.getText().trim();
            
            // Deshabilitar botón durante la autenticación
            btnLogin.setDisable(true);
            btnLogin.setText("Autenticando...");
            
            // Intentar autenticación
            boolean authenticated = authService.authenticate(idUsuario, password);
            
            if (authenticated) {
                logger.info("Usuario autenticado exitosamente: " + idUsuario);
                clearError();
                redirectToMainScreen();
            } else {
                showError("Credenciales inválidas. Verifique su DNI y contraseña.");
                txtPassword.clear();
                txtPassword.requestFocus();
            }
            
        } catch (Exception e) {
            logger.error("Error durante la autenticación", e);
            showError("Error interno. Por favor intente nuevamente.");
        } finally {
            // Rehabilitar botón
            btnLogin.setDisable(false);
            btnLogin.setText("Iniciar Sesión");
        }
    }
    
    /**
     * Redirige a la pantalla principal según el rol del usuario
     */
    private void redirectToMainScreen() {
        try {
            String fxmlFile;
            String title;
            
            if (authService.isAdmin()) {
                fxmlFile = "/fxml/admin/AdminMainView.fxml";
                title = "CrediActiva - Administrador";
            } else if (authService.isAsesor()) {
                fxmlFile = "/fxml/asesor/AsesorMainView.fxml";
                title = "CrediActiva - Asesor";
            } else if (authService.isCliente()) {
                fxmlFile = "/fxml/cliente/ClienteMainView.fxml";
                title = "CrediActiva - Cliente";
            } else {
                showError("Rol de usuario no válido");
                return;
            }
            
            // Cargar la pantalla principal
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            // Configurar el controlador según el rol
            if (authService.isAdmin()) {
                AdminMainController controller = loader.getController();
                controller.setPrimaryStage(primaryStage);
            } else if (authService.isAsesor()) {
                AsesorMainController controller = loader.getController();
                controller.setPrimaryStage(primaryStage);
            } else if (authService.isCliente()) {
                ClienteMainController controller = loader.getController();
                controller.setPrimaryStage(primaryStage);
            }
            
            // Configurar el stage
            primaryStage.setTitle(title);
            primaryStage.setScene(scene);
            primaryStage.setMaximized(true);
            
            logger.info("Usuario redirigido a pantalla principal: " + authService.getCurrentUser().getRol().getNombre());
            
        } catch (IOException e) {
            logger.error("Error al cargar la pantalla principal", e);
            showError("Error al cargar la interfaz principal");
        }
    }
    
    /**
     * Muestra un mensaje de error
     */
    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
    }
    
    /**
     * Limpia el mensaje de error
     */
    private void clearError() {
        lblError.setText("");
        lblError.setVisible(false);
    }
    
    /**
     * Limpia todos los campos del formulario
     */
    private void clearFields() {
        txtUsuario.clear();
        txtPassword.clear();
        clearError();
        txtUsuario.requestFocus();
    }
    
    /**
     * Método público para limpiar campos (usado por los controladores de logout)
     */
    public void resetForm() {
        clearFields();
    }
}
