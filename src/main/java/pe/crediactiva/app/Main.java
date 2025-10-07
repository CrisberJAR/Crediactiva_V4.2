package pe.crediactiva.app;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pe.crediactiva.app.config.DatabaseConfig;
import pe.crediactiva.app.view.LoginController;

import java.io.IOException;

/**
 * Clase principal de la aplicación CrediActiva Desktop
 */
public class Main extends Application {
    
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Configurar el Stage principal
            primaryStage.setTitle("CrediActiva - Sistema de Gestión de Préstamos");
            primaryStage.setResizable(false);
            
            // Establecer icono de la aplicación (opcional)
            try {
                var iconStream = getClass().getResourceAsStream("/images/logo.png");
                if (iconStream != null) {
                    primaryStage.getIcons().add(new Image(iconStream));
                } else {
                    logger.info("Icono no encontrado, continuando sin icono");
                }
            } catch (Exception e) {
                logger.warn("No se pudo cargar el icono de la aplicación", e);
            }
            
            // Verificar conexión a la base de datos
            if (!DatabaseConfig.testConnection()) {
                logger.error("No se pudo establecer conexión con la base de datos");
                // Aquí podrías mostrar un diálogo de error
                System.exit(1);
            }
            
            // Cargar la pantalla de login
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Scene scene = new Scene(loader.load());
            
            // Configurar el controlador
            LoginController controller = loader.getController();
            controller.setPrimaryStage(primaryStage);
            
            // Configurar la escena
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            primaryStage.setScene(scene);
            primaryStage.show();
            
            logger.info("Aplicación CrediActiva iniciada correctamente");
            
        } catch (IOException e) {
            logger.error("Error al cargar la interfaz de usuario", e);
            System.exit(1);
        }
    }
    
    @Override
    public void stop() {
        // Cerrar el pool de conexiones al cerrar la aplicación
        DatabaseConfig.shutdown();
        logger.info("Aplicación CrediActiva cerrada");
    }
    
    public static void main(String[] args) {
        logger.info("Iniciando aplicación CrediActiva Desktop v1.0.0");
        launch(args);
    }
}
