package pe.crediactiva.app.view.admin;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.RowConstraints;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import pe.crediactiva.app.service.AuthenticationService;
import pe.crediactiva.app.dao.UsuarioDAO;
import pe.crediactiva.app.dao.impl.UsuarioDAOImpl;
import pe.crediactiva.app.config.DatabaseConfig;
import pe.crediactiva.app.service.AsesorService;
import pe.crediactiva.app.service.ClienteService;
import pe.crediactiva.app.service.MovimientoCapitalService;
import pe.crediactiva.app.view.LoginController;
import pe.crediactiva.app.service.PrestamoService;
import pe.crediactiva.app.model.Usuario;
import pe.crediactiva.app.model.Asesor;
import pe.crediactiva.app.model.Cliente;
import pe.crediactiva.app.model.MovimientoCapital;
import pe.crediactiva.app.model.Rol;
import pe.crediactiva.app.model.Prestamo;
import pe.crediactiva.app.util.FechaUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import pe.crediactiva.app.util.DateTimeUtil;

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
            txtFechaHoy.setText("Fecha: " + FechaUtil.formatearFecha(DateTimeUtil.today()));
            
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
            String fechaActual = DateTimeUtil.formatDateTime(DateTimeUtil.now());
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
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/ValidarCobrosView.fxml"));
            VBox validarCobrosView = loader.load();
            
            // Reemplazar contenido
            contentArea.getChildren().clear();
            contentArea.getChildren().add(validarCobrosView);
            
            logger.info("Cargada validación de cobros");
            
        } catch (IOException e) {
            logger.error("Error al cargar validación de cobros", e);
            showError("Error al cargar la validación de cobros");
        }
    }
    
    /**
     * Maneja la opción de gestionar usuarios
     */
    @FXML
    private void handleGestionarUsuarios() {
        try {
            // Crear vista de gestión de usuarios dinámicamente
            VBox gestionarUsuariosView = crearGestionarUsuariosView();
            
            // Reemplazar contenido
            contentArea.getChildren().clear();
            contentArea.getChildren().add(gestionarUsuariosView);
            
            logger.info("Cargada gestión de usuarios");
            
        } catch (Exception e) {
            logger.error("Error al cargar gestión de usuarios", e);
            showError("Error al cargar la gestión de usuarios");
        }
    }
    
    /**
     * Maneja la opción de gestionar asesores
     */
    @FXML
    private void handleGestionarAsesores() {
        try {
            // Crear vista de gestión de asesores dinámicamente
            VBox gestionarAsesoresView = crearGestionarAsesoresView();
            
            // Reemplazar contenido
            contentArea.getChildren().clear();
            contentArea.getChildren().add(gestionarAsesoresView);
            
            logger.info("Cargada gestión de asesores");
            
        } catch (Exception e) {
            logger.error("Error al cargar gestión de asesores", e);
            showError("Error al cargar la gestión de asesores");
        }
    }
    
    /**
     * Maneja la opción de gestionar clientes
     */
    @FXML
    private void handleGestionarClientes() {
        try {
            // Crear vista de gestión de clientes dinámicamente
            VBox gestionarClientesView = crearGestionarClientesView();
            
            // Reemplazar contenido
            contentArea.getChildren().clear();
            contentArea.getChildren().add(gestionarClientesView);
            
            logger.info("Cargada gestión de clientes");
            
        } catch (Exception e) {
            logger.error("Error al cargar gestión de clientes", e);
            showError("Error al cargar la gestión de clientes");
        }
    }
    
    /**
     * Maneja la opción de movimientos de capital
     */
    @FXML
    private void handleMovimientosCapital() {
        try {
            // Crear vista de movimientos de capital dinámicamente
            VBox movimientosCapitalView = crearMovimientosCapitalView();
            
            // Reemplazar contenido
            contentArea.getChildren().clear();
            contentArea.getChildren().add(movimientosCapitalView);
            
            logger.info("Cargada gestión de movimientos de capital");
            
        } catch (Exception e) {
            logger.error("Error al cargar movimientos de capital", e);
            showError("Error al cargar los movimientos de capital");
        }
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
    
    /**
     * Crea la vista de gestión de usuarios
     */
    private VBox crearGestionarUsuariosView() {
        VBox mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(20));
        
        // Título
        Label titulo = new Label("👤 Gestión de Usuarios");
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Botones de acción
        HBox botonesAccion = new HBox(10);
        botonesAccion.setAlignment(Pos.CENTER_LEFT);
        
        Button btnNuevoUsuario = new Button("➕ Nuevo Usuario");
        btnNuevoUsuario.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 5px;");
        btnNuevoUsuario.setOnAction(e -> mostrarFormularioNuevoUsuario());
        
        Button btnActualizar = new Button("🔄 Actualizar");
        btnActualizar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 5px;");
        
        // Tabla de usuarios
        TableView<Usuario> tablaUsuarios = new TableView<>();
        tablaUsuarios.setStyle("-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-width: 1px; -fx-border-radius: 5px;");
        
        // Columnas de la tabla
        TableColumn<Usuario, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        colId.setPrefWidth(60);
        
        TableColumn<Usuario, String> colRol = new TableColumn<>("Rol");
        colRol.setCellValueFactory(cellData -> {
            Usuario usuario = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                usuario.getRol() != null ? usuario.getRol().getNombre() : "Sin rol"
            );
        });
        colRol.setPrefWidth(100);
        
        TableColumn<Usuario, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(cellData -> {
            Usuario usuario = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                usuario.isActivo() ? "Activo" : "Inactivo"
            );
        });
        colEstado.setPrefWidth(80);
        
        TableColumn<Usuario, String> colFechaCreacion = new TableColumn<>("Fecha Creación");
        colFechaCreacion.setCellValueFactory(cellData -> {
            Usuario usuario = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                usuario.getCreadoEn() != null ? 
                usuario.getCreadoEn().toLocalDate().toString() : "N/A"
            );
        });
        colFechaCreacion.setPrefWidth(120);
        
        TableColumn<Usuario, String> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setPrefWidth(150);
        colAcciones.setCellFactory(column -> new TableCell<Usuario, String>() {
            private final Button btnEditar = new Button("✏️");
            private final Button btnEliminar = new Button("🗑️");
            
            {
                btnEditar.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 4 8; -fx-background-radius: 3px;");
                btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 4 8; -fx-background-radius: 3px;");
                
                btnEditar.setOnAction(e -> editarUsuario(getTableView().getItems().get(getIndex())));
                btnEliminar.setOnAction(e -> eliminarUsuario(getTableView().getItems().get(getIndex())));
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(5);
                    hbox.getChildren().addAll(btnEditar, btnEliminar);
                    setGraphic(hbox);
                }
            }
        });
        
        tablaUsuarios.getColumns().addAll(colId, colRol, colEstado, colFechaCreacion, colAcciones);
        
        // Configurar el botón actualizar para recargar la tabla
        btnActualizar.setOnAction(e -> cargarUsuariosEnTabla(tablaUsuarios));
        
        botonesAccion.getChildren().addAll(btnNuevoUsuario, btnActualizar);
        
        // Cargar datos iniciales
        cargarUsuariosEnTabla(tablaUsuarios);
        
        mainContainer.getChildren().addAll(titulo, botonesAccion, tablaUsuarios);
        
        return mainContainer;
    }
    
    /**
     * Carga los usuarios en la tabla
     */
    private void cargarUsuarios() {
        try {
            UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
            List<Usuario> usuarios = usuarioDAO.findAll();
            
            ObservableList<Usuario> usuariosObservable = FXCollections.observableArrayList(usuarios);
            // Aquí necesitarías obtener la referencia a la tabla desde el contexto
            // tablaUsuarios.setItems(usuariosObservable);
            
            logger.info("Usuarios cargados: " + usuarios.size());
            
        } catch (Exception e) {
            logger.error("Error al cargar usuarios", e);
            showError("Error al cargar los usuarios");
        }
    }
    
    /**
     * Carga los usuarios en la tabla específica
     */
    private void cargarUsuariosEnTabla(TableView<Usuario> tablaUsuarios) {
        try {
            UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
            List<Usuario> usuarios = usuarioDAO.findAll();
            
            ObservableList<Usuario> usuariosObservable = FXCollections.observableArrayList(usuarios);
            tablaUsuarios.setItems(usuariosObservable);
            
            logger.info("Usuarios cargados en tabla: " + usuarios.size());
            
        } catch (Exception e) {
            logger.error("Error al cargar usuarios en tabla", e);
            showError("Error al cargar los usuarios");
        }
    }
    
    /**
     * Muestra el formulario para crear un nuevo usuario
     */
    private void mostrarFormularioNuevoUsuario() {
        try {
            // Crear diálogo para seleccionar tipo de usuario
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Crear Nuevo Usuario");
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.setResizable(false);
            
            VBox dialogContent = new VBox(20);
            dialogContent.setPadding(new Insets(20));
            dialogContent.setAlignment(Pos.CENTER);
            
            Label titulo = new Label("¿Qué tipo de usuario desea crear?");
            titulo.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            
            HBox botonesTipo = new HBox(20);
            botonesTipo.setAlignment(Pos.CENTER);
            
            Button btnAsesor = new Button("🎯 Crear Asesor");
            btnAsesor.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 15 25; -fx-background-radius: 8px; -fx-font-size: 14px;");
            btnAsesor.setOnAction(e -> {
                dialogStage.close();
                mostrarFormularioNuevoAsesor();
            });
            
            Button btnCliente = new Button("👥 Crear Cliente");
            btnCliente.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 15 25; -fx-background-radius: 8px; -fx-font-size: 14px;");
            btnCliente.setOnAction(e -> {
                dialogStage.close();
                mostrarFormularioNuevoClienteUsuario();
            });
            
            Button btnCancelar = new Button("❌ Cancelar");
            btnCancelar.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 15 25; -fx-background-radius: 8px; -fx-font-size: 14px;");
            btnCancelar.setOnAction(e -> dialogStage.close());
            
            botonesTipo.getChildren().addAll(btnAsesor, btnCliente, btnCancelar);
            
            dialogContent.getChildren().addAll(titulo, botonesTipo);
            
            Scene dialogScene = new Scene(dialogContent, 500, 200);
            dialogStage.setScene(dialogScene);
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            logger.error("Error al mostrar formulario de nuevo usuario", e);
            showError("Error al mostrar el formulario de nuevo usuario");
        }
    }
    
    /**
     * Edita un usuario existente
     */
    private void editarUsuario(Usuario usuario) {
        // TODO: Implementar edición de usuario
        showInfo("Editando usuario ID: " + usuario.getIdUsuario());
    }
    
    /**
     * Elimina un usuario
     */
    private void eliminarUsuario(Usuario usuario) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText("¿Está seguro de eliminar el usuario?");
        confirmacion.setContentText("Usuario ID: " + usuario.getIdUsuario());
        
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
                boolean eliminado = usuarioDAO.delete(usuario.getIdUsuario());
                
                if (eliminado) {
                    showInfo("Usuario eliminado exitosamente");
                    cargarUsuarios();
                } else {
                    showError("Error al eliminar el usuario");
                }
                
            } catch (Exception e) {
                logger.error("Error al eliminar usuario", e);
                showError("Error al eliminar el usuario");
            }
        }
    }
    
    /**
     * Crea la vista de gestión de asesores
     */
    private VBox crearGestionarAsesoresView() {
        VBox mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(20));
        
        // Título
        Label titulo = new Label("🎯 Gestión de Asesores");
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Botones de acción
        HBox botonesAccion = new HBox(10);
        botonesAccion.setAlignment(Pos.CENTER_LEFT);
        
        Button btnNuevoAsesor = new Button("➕ Nuevo Asesor");
        btnNuevoAsesor.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 5px;");
        btnNuevoAsesor.setOnAction(e -> mostrarFormularioNuevoAsesor());
        
        Button btnActualizar = new Button("🔄 Actualizar");
        btnActualizar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 5px;");
        
        botonesAccion.getChildren().addAll(btnNuevoAsesor, btnActualizar);
        
        // Tabla de asesores
        TableView<Asesor> tablaAsesores = new TableView<>();
        tablaAsesores.setStyle("-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-width: 1px; -fx-border-radius: 5px;");
        
        // Columnas de la tabla
        TableColumn<Asesor, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idAsesor"));
        colId.setPrefWidth(60);
        
        TableColumn<Asesor, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colNombre.setPrefWidth(200);
        
        TableColumn<Asesor, String> colTelefono = new TableColumn<>("Teléfono");
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colTelefono.setPrefWidth(120);
        
        TableColumn<Asesor, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(150);
        
        TableColumn<Asesor, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(cellData -> {
            Asesor asesor = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                asesor.isActivo() ? "Activo" : "Inactivo"
            );
        });
        colEstado.setPrefWidth(80);
        
        TableColumn<Asesor, String> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setPrefWidth(150);
        colAcciones.setCellFactory(column -> new TableCell<Asesor, String>() {
            private final Button btnEditar = new Button("✏️");
            private final Button btnEliminar = new Button("🗑️");
            
            {
                btnEditar.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 4 8; -fx-background-radius: 3px;");
                btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 4 8; -fx-background-radius: 3px;");
                
                btnEditar.setOnAction(e -> editarAsesor(getTableView().getItems().get(getIndex())));
                btnEliminar.setOnAction(e -> eliminarAsesor(getTableView().getItems().get(getIndex())));
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(5);
                    hbox.getChildren().addAll(btnEditar, btnEliminar);
                    setGraphic(hbox);
                }
            }
        });
        
        tablaAsesores.getColumns().addAll(colId, colNombre, colTelefono, colEmail, colEstado, colAcciones);
        
        // Configurar el botón actualizar para recargar la tabla
        btnActualizar.setOnAction(e -> cargarAsesoresEnTabla(tablaAsesores));
        
        // Cargar datos iniciales
        cargarAsesoresEnTabla(tablaAsesores);
        
        mainContainer.getChildren().addAll(titulo, botonesAccion, tablaAsesores);
        
        return mainContainer;
    }
    
    /**
     * Carga los asesores en la tabla
     */
    private void cargarAsesores() {
        try {
            AsesorService asesorService = new AsesorService();
            List<Asesor> asesores = asesorService.obtenerAsesoresActivos();
            
            ObservableList<Asesor> asesoresObservable = FXCollections.observableArrayList(asesores);
            // Aquí necesitarías obtener la referencia a la tabla desde el contexto
            // tablaAsesores.setItems(asesoresObservable);
            
            logger.info("Asesores cargados: " + asesores.size());
            
        } catch (Exception e) {
            logger.error("Error al cargar asesores", e);
            showError("Error al cargar los asesores");
        }
    }
    
    /**
     * Carga los asesores en la tabla específica
     */
    private void cargarAsesoresEnTabla(TableView<Asesor> tablaAsesores) {
        try {
            AsesorService asesorService = new AsesorService();
            List<Asesor> asesores = asesorService.obtenerAsesoresActivos();
            
            ObservableList<Asesor> asesoresObservable = FXCollections.observableArrayList(asesores);
            tablaAsesores.setItems(asesoresObservable);
            
            logger.info("Asesores cargados en tabla: " + asesores.size());
            
        } catch (Exception e) {
            logger.error("Error al cargar asesores en tabla", e);
            showError("Error al cargar los asesores");
        }
    }
    
    /**
     * Muestra el formulario para crear un nuevo asesor
     */
    private void mostrarFormularioNuevoAsesor() {
        try {
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Crear Nuevo Asesor");
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.setResizable(false);
            
            VBox mainContent = new VBox(15);
            mainContent.setPadding(new Insets(20));
            
            Label titulo = new Label("🎯 Crear Nuevo Asesor");
            titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            
            GridPane formGrid = new GridPane();
            formGrid.setHgap(10);
            formGrid.setVgap(10);
            formGrid.setPadding(new Insets(10));
            
            // Campos del formulario
            Label lblIdAsesor = new Label("ID Asesor:");
            TextField txtIdAsesor = new TextField();
            txtIdAsesor.setPromptText("Ej: 1001");
            txtIdAsesor.setPrefWidth(200);
            
            Label lblNombre = new Label("Nombre:");
            TextField txtNombre = new TextField();
            txtNombre.setPromptText("Nombre del asesor");
            txtNombre.setPrefWidth(200);
            
            Label lblApellido = new Label("Apellido:");
            TextField txtApellido = new TextField();
            txtApellido.setPromptText("Apellido del asesor");
            txtApellido.setPrefWidth(200);
            
            Label lblTelefono = new Label("Teléfono:");
            TextField txtTelefono = new TextField();
            txtTelefono.setPromptText("Ej: 987654321");
            txtTelefono.setPrefWidth(200);
            
            Label lblEmail = new Label("Email:");
            TextField txtEmail = new TextField();
            txtEmail.setPromptText("asesor@empresa.com");
            txtEmail.setPrefWidth(200);
            
            Label lblDireccion = new Label("Dirección:");
            TextField txtDireccion = new TextField();
            txtDireccion.setPromptText("Dirección del asesor");
            txtDireccion.setPrefWidth(200);
            
            Label lblPassword = new Label("Contraseña:");
            PasswordField txtPassword = new PasswordField();
            txtPassword.setPromptText("Contraseña para el usuario");
            txtPassword.setPrefWidth(200);
            
            // Agregar campos al grid
            formGrid.add(lblIdAsesor, 0, 0);
            formGrid.add(txtIdAsesor, 1, 0);
            formGrid.add(lblNombre, 0, 1);
            formGrid.add(txtNombre, 1, 1);
            formGrid.add(lblApellido, 0, 2);
            formGrid.add(txtApellido, 1, 2);
            formGrid.add(lblTelefono, 0, 3);
            formGrid.add(txtTelefono, 1, 3);
            formGrid.add(lblEmail, 0, 4);
            formGrid.add(txtEmail, 1, 4);
            formGrid.add(lblDireccion, 0, 5);
            formGrid.add(txtDireccion, 1, 5);
            formGrid.add(lblPassword, 0, 6);
            formGrid.add(txtPassword, 1, 6);
            
            // Botones
            HBox botones = new HBox(10);
            botones.setAlignment(Pos.CENTER_RIGHT);
            
            Button btnCrear = new Button("✅ Crear Asesor");
            btnCrear.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5px;");
            btnCrear.setOnAction(e -> crearAsesor(txtIdAsesor.getText(), txtNombre.getText(), txtApellido.getText(), 
                                                txtTelefono.getText(), txtEmail.getText(), txtDireccion.getText(), txtPassword.getText(), dialogStage));
            
            Button btnCancelar = new Button("❌ Cancelar");
            btnCancelar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5px;");
            btnCancelar.setOnAction(e -> dialogStage.close());
            
            botones.getChildren().addAll(btnCrear, btnCancelar);
            
            mainContent.getChildren().addAll(titulo, formGrid, botones);
            
            Scene scene = new Scene(mainContent, 400, 450);
            dialogStage.setScene(scene);
            
            // Mostrar la ventana y luego enfocar
            dialogStage.show();
            
            // Enfocar el primer campo usando Platform.runLater para evitar conflictos
            Platform.runLater(() -> {
                txtIdAsesor.requestFocus();
            });
            
            // Esperar a que se cierre la ventana
            dialogStage.setOnCloseRequest(e -> dialogStage.close());
            
        } catch (Exception e) {
            logger.error("Error al mostrar formulario de nuevo asesor", e);
            showError("Error al mostrar el formulario de nuevo asesor");
        }
    }
    
    /**
     * Crea un nuevo asesor
     */
    private void crearAsesor(String idAsesorStr, String nombre, String apellido, String telefono, String email, String direccion, String password, Stage dialogStage) {
        try {
            // Validar campos obligatorios
            if (idAsesorStr.trim().isEmpty() || nombre.trim().isEmpty() || apellido.trim().isEmpty() || 
                telefono.trim().isEmpty() || email.trim().isEmpty() || direccion.trim().isEmpty() || password.trim().isEmpty()) {
                showError("Todos los campos son obligatorios");
                return;
            }
            
            // Validar ID numérico
            Long idAsesor;
            try {
                idAsesor = Long.parseLong(idAsesorStr.trim());
            } catch (NumberFormatException e) {
                showError("El ID del asesor debe ser un número válido");
                return;
            }
            
            // Validar email
            if (!email.contains("@")) {
                showError("El email debe tener un formato válido");
                return;
            }
            
            // Crear el asesor
            Asesor nuevoAsesor = new Asesor();
            nuevoAsesor.setIdAsesor(idAsesor);
            nuevoAsesor.setNombre(nombre.trim());
            nuevoAsesor.setApellido(apellido.trim());
            nuevoAsesor.setTelefono(telefono.trim());
            nuevoAsesor.setEmail(email.trim());
            nuevoAsesor.setDireccion(direccion.trim());
            nuevoAsesor.setActivo(true);
            
            // Crear el usuario
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setIdUsuario(idAsesor);
            nuevoUsuario.setPasswordHash(password);
            nuevoUsuario.setIdRol(2); // Rol de asesor
            nuevoUsuario.setActivo(true);
            nuevoUsuario.setCreadoEn(DateTimeUtil.now());
            
            // Verificar que el usuario se está creando con los valores correctos
            logger.info("Usuario a crear - ID: " + nuevoUsuario.getIdUsuario() + 
                       ", Rol: " + nuevoUsuario.getIdRol() + 
                       ", Activo: " + nuevoUsuario.isActivo() + 
                       ", Password: " + (nuevoUsuario.getPasswordHash() != null ? "***" : "NULL"));
            
            // Crear rol para el usuario
            Rol rolAsesor = new Rol();
            rolAsesor.setIdRol(2);
            rolAsesor.setNombre("Asesor");
            nuevoUsuario.setRol(rolAsesor);
            
            // Guardar en la base de datos
            UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
            
            logger.info("Intentando crear usuario con ID: " + idAsesor + ", Activo: " + nuevoUsuario.isActivo());
            boolean usuarioCreado = usuarioDAO.create(nuevoUsuario);
            logger.info("Resultado de creación de usuario: " + usuarioCreado);
            
            if (usuarioCreado) {
                // Crear asesor en la tabla asesores
                logger.info("Intentando crear asesor en tabla asesores...");
                boolean asesorCreado = crearAsesorEnBaseDatos(nuevoAsesor);
                logger.info("Resultado de creación de asesor: " + asesorCreado);
                
                if (asesorCreado) {
                    showInfo("Asesor creado exitosamente con ID: " + idAsesor);
                    dialogStage.close();
                    
                    logger.info("Nuevo asesor creado: " + nombre + " " + apellido + " (ID: " + idAsesor + ")");
                } else {
                    // Si falla la creación del asesor, eliminar el usuario creado
                    logger.warn("Falló la creación del asesor, eliminando usuario creado...");
                    usuarioDAO.delete(idAsesor);
                    showError("Error al crear el asesor en la base de datos.");
                }
            } else {
                showError("Error al crear el usuario. Verifique que el ID no esté en uso.");
            }
            
        } catch (Exception e) {
            logger.error("Error al crear asesor", e);
            showError("Error al crear el asesor: " + e.getMessage());
        }
    }
    
    /**
     * Crea un asesor en la base de datos
     */
    private boolean crearAsesorEnBaseDatos(Asesor asesor) {
        try {
            // Usar el AsesorService para crear el asesor
            AsesorService asesorService = new AsesorService();
            
            // Crear el asesor usando SQL directo ya que el servicio no tiene método create
            // Incluir fecha_contrato que es obligatorio en la tabla
            String sql = "INSERT INTO asesores (id_asesor, nombre, apellido, telefono, email, direccion, activo, fecha_contrato) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (java.sql.Connection connection = DatabaseConfig.getConnection();
                 java.sql.PreparedStatement statement = connection.prepareStatement(sql)) {
                
                statement.setLong(1, asesor.getIdAsesor());
                statement.setString(2, asesor.getNombre());
                statement.setString(3, asesor.getApellido());
                statement.setString(4, asesor.getTelefono());
                statement.setString(5, asesor.getEmail());
                statement.setString(6, asesor.getDireccion());
                statement.setBoolean(7, asesor.isActivo());
                // Agregar fecha_contrato con la fecha actual
                statement.setTimestamp(8, DateTimeUtil.nowAsTimestamp());
                
                int rowsAffected = statement.executeUpdate();
                
                if (rowsAffected > 0) {
                    logger.info("Asesor creado en base de datos: " + asesor.getNombre() + " " + asesor.getApellido());
                    return true;
                } else {
                    logger.error("No se pudo crear el asesor en la base de datos");
                    return false;
                }
                
            } catch (java.sql.SQLException e) {
                logger.error("Error SQL al crear asesor", e);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Error al crear asesor en base de datos", e);
            return false;
        }
    }
    
    /**
     * Edita un asesor existente
     */
    private void editarAsesor(Asesor asesor) {
        // TODO: Implementar edición de asesor
        showInfo("Editando asesor: " + asesor.getNombreCompleto());
    }
    
    /**
     * Desactiva un asesor
     */
    private void eliminarAsesor(Asesor asesor) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Desactivación");
        confirmacion.setHeaderText("¿Está seguro de desactivar el asesor?");
        confirmacion.setContentText("Asesor: " + asesor.getNombreCompleto());
        
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                // TODO: Implementar método para desactivar asesor
                showInfo("Funcionalidad de desactivación de asesor en desarrollo");
                // cargarAsesores();
                
            } catch (Exception e) {
                logger.error("Error al desactivar asesor", e);
                showError("Error al desactivar el asesor");
            }
        }
    }
    
    /**
     * Crea la vista de gestión de clientes
     */
    private VBox crearGestionarClientesView() {
        VBox mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(20));
        
        // Título
        Label titulo = new Label("👥 Gestión de Clientes");
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Botones de acción
        HBox botonesAccion = new HBox(10);
        botonesAccion.setAlignment(Pos.CENTER_LEFT);
        
        Button btnNuevoCliente = new Button("➕ Nuevo Cliente");
        btnNuevoCliente.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 5px;");
        btnNuevoCliente.setOnAction(e -> mostrarFormularioNuevoCliente());
        
        Button btnActualizar = new Button("🔄 Actualizar");
        btnActualizar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 5px;");
        
        botonesAccion.getChildren().addAll(btnNuevoCliente, btnActualizar);
        
        // Tabla de clientes
        TableView<Cliente> tablaClientes = new TableView<>();
        tablaClientes.setStyle("-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-width: 1px; -fx-border-radius: 5px;");
        
        // Columnas de la tabla
        TableColumn<Cliente, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idCliente"));
        colId.setPrefWidth(60);
        
        TableColumn<Cliente, String> colNombre = new TableColumn<>("Nombre");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colNombre.setPrefWidth(200);
        
        TableColumn<Cliente, String> colDni = new TableColumn<>("DNI");
        colDni.setCellValueFactory(new PropertyValueFactory<>("dni"));
        colDni.setPrefWidth(100);
        
        TableColumn<Cliente, String> colTelefono = new TableColumn<>("Teléfono");
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colTelefono.setPrefWidth(120);
        
        TableColumn<Cliente, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEmail.setPrefWidth(150);
        
        TableColumn<Cliente, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(cellData -> {
            Cliente cliente = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                cliente.isActivo() ? "Activo" : "Inactivo"
            );
        });
        colEstado.setPrefWidth(80);
        
        TableColumn<Cliente, String> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setPrefWidth(150);
        colAcciones.setCellFactory(column -> new TableCell<Cliente, String>() {
            private final Button btnEditar = new Button("✏️");
            private final Button btnEliminar = new Button("🗑️");
            
            {
                btnEditar.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 4 8; -fx-background-radius: 3px;");
                btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 4 8; -fx-background-radius: 3px;");
                
                btnEditar.setOnAction(e -> editarCliente(getTableView().getItems().get(getIndex())));
                btnEliminar.setOnAction(e -> eliminarCliente(getTableView().getItems().get(getIndex())));
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(5);
                    hbox.getChildren().addAll(btnEditar, btnEliminar);
                    setGraphic(hbox);
                }
            }
        });
        
        tablaClientes.getColumns().addAll(colId, colNombre, colDni, colTelefono, colEmail, colEstado, colAcciones);
        
        // Configurar el botón actualizar para recargar la tabla
        btnActualizar.setOnAction(e -> cargarClientesEnTabla(tablaClientes));
        
        // Cargar datos iniciales
        cargarClientesEnTabla(tablaClientes);
        
        mainContainer.getChildren().addAll(titulo, botonesAccion, tablaClientes);
        
        return mainContainer;
    }
    
    /**
     * Carga los clientes en la tabla
     */
    private void cargarClientes() {
        try {
            ClienteService clienteService = new ClienteService();
            List<Cliente> clientes = clienteService.obtenerTodosLosClientes();
            
            ObservableList<Cliente> clientesObservable = FXCollections.observableArrayList(clientes);
            // Aquí necesitarías obtener la referencia a la tabla desde el contexto
            // tablaClientes.setItems(clientesObservable);
            
            logger.info("Clientes cargados: " + clientes.size());
            
        } catch (Exception e) {
            logger.error("Error al cargar clientes", e);
            showError("Error al cargar los clientes");
        }
    }
    
    /**
     * Carga los clientes en la tabla específica
     */
    private void cargarClientesEnTabla(TableView<Cliente> tablaClientes) {
        try {
            ClienteService clienteService = new ClienteService();
            List<Cliente> clientes = clienteService.obtenerTodosLosClientes();
            
            ObservableList<Cliente> clientesObservable = FXCollections.observableArrayList(clientes);
            tablaClientes.setItems(clientesObservable);
            
            logger.info("Clientes cargados en tabla: " + clientes.size());
            
        } catch (Exception e) {
            logger.error("Error al cargar clientes en tabla", e);
            showError("Error al cargar los clientes");
        }
    }
    
    /**
     * Muestra el formulario para crear un nuevo cliente
     */
    private void mostrarFormularioNuevoCliente() {
        // TODO: Implementar formulario de nuevo cliente
        showInfo("Funcionalidad de nuevo cliente en desarrollo");
    }
    
    /**
     * Muestra el formulario para crear usuario de cliente
     */
    private void mostrarFormularioNuevoClienteUsuario() {
        try {
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Crear Usuario para Cliente");
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.setResizable(false);
            
            VBox mainContent = new VBox(15);
            mainContent.setPadding(new Insets(20));
            
            Label titulo = new Label("👥 Crear Usuario para Cliente");
            titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            
            // Obtener clientes que tienen préstamos pero no tienen usuario
            List<Cliente> clientesSinUsuario = obtenerClientesSinUsuario();
            
            if (clientesSinUsuario.isEmpty()) {
                Label mensaje = new Label("No hay clientes con préstamos que necesiten usuario.");
                mensaje.setStyle("-fx-text-fill: #e74c3c; -fx-font-size: 14px;");
                mainContent.getChildren().addAll(titulo, mensaje);
                
                Button btnCerrar = new Button("Cerrar");
                btnCerrar.setStyle("-fx-background-color: #95a5a6; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5px;");
                btnCerrar.setOnAction(e -> dialogStage.close());
                mainContent.getChildren().add(btnCerrar);
            } else {
                Label instruccion = new Label("Seleccione un cliente para crear su usuario:");
                instruccion.setStyle("-fx-font-size: 14px; -fx-text-fill: #2c3e50;");
                
                // Tabla de clientes
                TableView<Cliente> tablaClientes = new TableView<>();
                tablaClientes.setStyle("-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-width: 1px; -fx-border-radius: 5px;");
                tablaClientes.setMaxHeight(300);
                
                // Columnas de la tabla
                TableColumn<Cliente, Long> colId = new TableColumn<>("ID");
                colId.setCellValueFactory(new PropertyValueFactory<>("idCliente"));
                colId.setPrefWidth(60);
                
                TableColumn<Cliente, String> colNombre = new TableColumn<>("Nombre");
                colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
                colNombre.setPrefWidth(200);
                
                TableColumn<Cliente, String> colDni = new TableColumn<>("DNI");
                colDni.setCellValueFactory(new PropertyValueFactory<>("dni"));
                colDni.setPrefWidth(100);
                
                TableColumn<Cliente, String> colTelefono = new TableColumn<>("Teléfono");
                colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
                colTelefono.setPrefWidth(120);
                
                TableColumn<Cliente, String> colAccion = new TableColumn<>("Acción");
                colAccion.setPrefWidth(100);
                colAccion.setCellFactory(column -> new TableCell<Cliente, String>() {
                    private final Button btnCrear = new Button("Crear");
                    
                    {
                        btnCrear.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 4 8; -fx-background-radius: 3px;");
                        btnCrear.setOnAction(e -> {
                            Cliente cliente = getTableView().getItems().get(getIndex());
                            dialogStage.close();
                            mostrarFormularioCrearUsuarioCliente(cliente);
                        });
                    }
                    
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            setGraphic(btnCrear);
                        }
                    }
                });
                
                tablaClientes.getColumns().addAll(colId, colNombre, colDni, colTelefono, colAccion);
                
                // Cargar datos
                ObservableList<Cliente> clientesObservable = FXCollections.observableArrayList(clientesSinUsuario);
                tablaClientes.setItems(clientesObservable);
                
                Button btnCancelar = new Button("❌ Cancelar");
                btnCancelar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5px;");
                btnCancelar.setOnAction(e -> dialogStage.close());
                
                mainContent.getChildren().addAll(titulo, instruccion, tablaClientes, btnCancelar);
            }
            
            Scene scene = new Scene(mainContent, 600, 500);
            dialogStage.setScene(scene);
            dialogStage.showAndWait();
            
        } catch (Exception e) {
            logger.error("Error al mostrar formulario de nuevo cliente usuario", e);
            showError("Error al mostrar el formulario de nuevo cliente usuario");
        }
    }
    
    /**
     * Obtiene clientes que tienen préstamos pero no tienen usuario
     */
    private List<Cliente> obtenerClientesSinUsuario() {
        try {
            // Usar SQL directo para obtener clientes con préstamos pero sin usuario
            String sql = "SELECT DISTINCT c.id_cliente, c.nombre, c.apellido, c.dni, c.telefono, c.email, c.activo " +
                        "FROM clientes c " +
                        "INNER JOIN prestamos p ON c.id_cliente = p.id_cliente " +
                        "LEFT JOIN usuarios u ON c.id_cliente = u.id_usuario " +
                        "WHERE u.id_usuario IS NULL AND c.activo = 1";
            
            List<Cliente> clientesSinUsuario = new java.util.ArrayList<>();
            
            try (java.sql.Connection connection = DatabaseConfig.getConnection();
                 java.sql.PreparedStatement statement = connection.prepareStatement(sql);
                 java.sql.ResultSet resultSet = statement.executeQuery()) {
                
                while (resultSet.next()) {
                    Cliente cliente = new Cliente();
                    cliente.setIdCliente(resultSet.getLong("id_cliente"));
                    cliente.setNombre(resultSet.getString("nombre"));
                    cliente.setApellido(resultSet.getString("apellido"));
                    cliente.setDni(resultSet.getString("dni"));
                    cliente.setTelefono(resultSet.getString("telefono"));
                    cliente.setEmail(resultSet.getString("email"));
                    cliente.setActivo(resultSet.getBoolean("activo"));
                    
                    clientesSinUsuario.add(cliente);
                }
            }
            
            logger.info("Clientes sin usuario encontrados: " + clientesSinUsuario.size());
            return clientesSinUsuario;
            
        } catch (Exception e) {
            logger.error("Error al obtener clientes sin usuario", e);
            return new java.util.ArrayList<>();
        }
    }
    
    /**
     * Verifica si un cliente tiene préstamos activos
     */
    private boolean tienePrestamosActivos(Long idCliente) {
        try {
            // Usar SQL directo para verificar si el cliente tiene préstamos
            String sql = "SELECT COUNT(*) FROM prestamos WHERE id_cliente = ?";
            
            try (java.sql.Connection connection = DatabaseConfig.getConnection();
                 java.sql.PreparedStatement statement = connection.prepareStatement(sql)) {
                
                statement.setLong(1, idCliente);
                
                try (java.sql.ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        int count = resultSet.getInt(1);
                        return count > 0;
                    }
                }
            }
            
            return false;
        } catch (Exception e) {
            logger.error("Error al verificar préstamos del cliente: " + idCliente, e);
            return false;
        }
    }
    
    /**
     * Verifica si un cliente ya tiene usuario activo
     */
    private boolean tieneUsuarioActivo(Long idCliente) {
        try {
            UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
            Optional<Usuario> usuarioOpt = usuarioDAO.findById(idCliente);
            return usuarioOpt.isPresent() && usuarioOpt.get().isActivo();
        } catch (Exception e) {
            logger.error("Error al verificar usuario del cliente: " + idCliente, e);
            return false;
        }
    }
    
    /**
     * Muestra el formulario para crear usuario de un cliente específico
     */
    private void mostrarFormularioCrearUsuarioCliente(Cliente cliente) {
        try {
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Crear Usuario para " + cliente.getNombreCompleto());
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.setResizable(false);
            
            VBox mainContent = new VBox(15);
            mainContent.setPadding(new Insets(20));
            
            Label titulo = new Label("👤 Crear Usuario para Cliente");
            titulo.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            
            // Información del cliente
            VBox infoCliente = new VBox(5);
            infoCliente.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 10; -fx-background-radius: 5px;");
            
            Label lblInfo = new Label("Información del Cliente:");
            lblInfo.setStyle("-fx-font-weight: bold; -fx-text-fill: #2c3e50;");
            
            Label lblNombre = new Label("Nombre: " + cliente.getNombreCompleto());
            Label lblDni = new Label("DNI: " + cliente.getDni());
            Label lblTelefono = new Label("Teléfono: " + cliente.getTelefono());
            
            infoCliente.getChildren().addAll(lblInfo, lblNombre, lblDni, lblTelefono);
            
            // Formulario de usuario
            GridPane formGrid = new GridPane();
            formGrid.setHgap(10);
            formGrid.setVgap(10);
            formGrid.setPadding(new Insets(10));
            
            Label lblIdUsuario = new Label("ID Usuario:");
            TextField txtIdUsuario = new TextField();
            txtIdUsuario.setText(cliente.getIdCliente().toString());
            txtIdUsuario.setEditable(false);
            txtIdUsuario.setStyle("-fx-background-color: #bdc3c7;");
            
            Label lblPassword = new Label("Contraseña:");
            PasswordField txtPassword = new PasswordField();
            txtPassword.setPromptText("Contraseña para el usuario");
            
            formGrid.add(lblIdUsuario, 0, 0);
            formGrid.add(txtIdUsuario, 1, 0);
            formGrid.add(lblPassword, 0, 1);
            formGrid.add(txtPassword, 1, 1);
            
            // Botones
            HBox botones = new HBox(10);
            botones.setAlignment(Pos.CENTER_RIGHT);
            
            Button btnCrear = new Button("✅ Crear Usuario");
            btnCrear.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5px;");
            btnCrear.setOnAction(e -> crearUsuarioCliente(cliente, txtPassword.getText(), dialogStage));
            
            Button btnCancelar = new Button("❌ Cancelar");
            btnCancelar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5px;");
            btnCancelar.setOnAction(e -> dialogStage.close());
            
            botones.getChildren().addAll(btnCrear, btnCancelar);
            
            mainContent.getChildren().addAll(titulo, infoCliente, formGrid, botones);
            
            Scene scene = new Scene(mainContent, 400, 350);
            dialogStage.setScene(scene);
            
            // Mostrar la ventana y luego enfocar
            dialogStage.show();
            
            // Enfocar el campo de contraseña usando Platform.runLater para evitar conflictos
            Platform.runLater(() -> {
                txtPassword.requestFocus();
            });
            
            // Esperar a que se cierre la ventana
            dialogStage.setOnCloseRequest(e -> dialogStage.close());
            
        } catch (Exception e) {
            logger.error("Error al mostrar formulario de crear usuario cliente", e);
            showError("Error al mostrar el formulario de crear usuario cliente");
        }
    }
    
    /**
     * Crea un usuario para un cliente
     */
    private void crearUsuarioCliente(Cliente cliente, String password, Stage dialogStage) {
        try {
            // Validar contraseña
            if (password.trim().isEmpty()) {
                showError("La contraseña es obligatoria");
                return;
            }
            
            if (password.length() < 4) {
                showError("La contraseña debe tener al menos 4 caracteres");
                return;
            }
            
            // Crear el usuario
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setIdUsuario(cliente.getIdCliente());
            nuevoUsuario.setPasswordHash(password);
            nuevoUsuario.setIdRol(3); // Rol de cliente
            nuevoUsuario.setActivo(true);
            nuevoUsuario.setCreadoEn(DateTimeUtil.now());
            
            // Crear rol para el usuario
            Rol rolCliente = new Rol();
            rolCliente.setIdRol(3);
            rolCliente.setNombre("Cliente");
            nuevoUsuario.setRol(rolCliente);
            
            // Guardar en la base de datos
            UsuarioDAO usuarioDAO = new UsuarioDAOImpl();
            boolean usuarioCreado = usuarioDAO.create(nuevoUsuario);
            
            if (usuarioCreado) {
                showInfo("Usuario creado exitosamente para " + cliente.getNombreCompleto());
                dialogStage.close();
                
                logger.info("Nuevo usuario cliente creado: " + cliente.getNombreCompleto() + " (ID: " + cliente.getIdCliente() + ")");
            } else {
                showError("Error al crear el usuario. Verifique que el ID no esté en uso.");
            }
            
        } catch (Exception e) {
            logger.error("Error al crear usuario cliente", e);
            showError("Error al crear el usuario: " + e.getMessage());
        }
    }
    
    /**
     * Edita un cliente existente
     */
    private void editarCliente(Cliente cliente) {
        // TODO: Implementar edición de cliente
        showInfo("Editando cliente: " + cliente.getNombreCompleto());
    }
    
    /**
     * Desactiva un cliente
     */
    private void eliminarCliente(Cliente cliente) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Desactivación");
        confirmacion.setHeaderText("¿Está seguro de desactivar el cliente?");
        confirmacion.setContentText("Cliente: " + cliente.getNombreCompleto());
        
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                // TODO: Implementar método para desactivar cliente
                showInfo("Funcionalidad de desactivación de cliente en desarrollo");
                // cargarClientes();
                
            } catch (Exception e) {
                logger.error("Error al desactivar cliente", e);
                showError("Error al desactivar el cliente");
            }
        }
    }
    
    /**
     * Crea la vista de movimientos de capital
     */
    private VBox crearMovimientosCapitalView() {
        VBox mainContainer = new VBox(15);
        mainContainer.setPadding(new Insets(20));
        
        // Título
        Label titulo = new Label("💼 Movimientos de Capital");
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        // Botones de acción
        HBox botonesAccion = new HBox(10);
        botonesAccion.setAlignment(Pos.CENTER_LEFT);
        
        Button btnNuevoMovimiento = new Button("➕ Nuevo Movimiento");
        btnNuevoMovimiento.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 5px;");
        btnNuevoMovimiento.setOnAction(e -> mostrarFormularioNuevoMovimiento());
        
        Button btnActualizar = new Button("🔄 Actualizar");
        btnActualizar.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 5px;");
        
        botonesAccion.getChildren().addAll(btnNuevoMovimiento, btnActualizar);
        
        // Tabla de movimientos de capital
        TableView<MovimientoCapital> tablaMovimientos = new TableView<>();
        tablaMovimientos.setStyle("-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-width: 1px; -fx-border-radius: 5px;");
        
        // Columnas de la tabla
        TableColumn<MovimientoCapital, Long> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(new PropertyValueFactory<>("idMovimiento"));
        colId.setPrefWidth(60);
        
        TableColumn<MovimientoCapital, String> colTipo = new TableColumn<>("Tipo");
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipoMovimiento"));
        colTipo.setPrefWidth(120);
        
        TableColumn<MovimientoCapital, String> colMonto = new TableColumn<>("Monto");
        colMonto.setCellValueFactory(new PropertyValueFactory<>("monto"));
        colMonto.setPrefWidth(100);
        
        TableColumn<MovimientoCapital, String> colDescripcion = new TableColumn<>("Descripción");
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colDescripcion.setPrefWidth(200);
        
        TableColumn<MovimientoCapital, String> colFecha = new TableColumn<>("Fecha");
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaMovimiento"));
        colFecha.setPrefWidth(120);
        
        TableColumn<MovimientoCapital, String> colAcciones = new TableColumn<>("Acciones");
        colAcciones.setPrefWidth(150);
        colAcciones.setCellFactory(column -> new TableCell<MovimientoCapital, String>() {
            private final Button btnEditar = new Button("✏️");
            private final Button btnEliminar = new Button("🗑️");
            
            {
                btnEditar.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 4 8; -fx-background-radius: 3px;");
                btnEliminar.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10px; -fx-padding: 4 8; -fx-background-radius: 3px;");
                
                btnEditar.setOnAction(e -> editarMovimiento(getTableView().getItems().get(getIndex())));
                btnEliminar.setOnAction(e -> eliminarMovimiento(getTableView().getItems().get(getIndex())));
            }
            
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(5);
                    hbox.getChildren().addAll(btnEditar, btnEliminar);
                    setGraphic(hbox);
                }
            }
        });
        
        tablaMovimientos.getColumns().addAll(colId, colTipo, colMonto, colDescripcion, colFecha, colAcciones);
        
        // Configurar el botón actualizar para recargar la tabla
        btnActualizar.setOnAction(e -> cargarMovimientosCapitalEnTabla(tablaMovimientos));
        
        // Cargar datos iniciales
        cargarMovimientosCapitalEnTabla(tablaMovimientos);
        
        mainContainer.getChildren().addAll(titulo, botonesAccion, tablaMovimientos);
        
        return mainContainer;
    }
    
    /**
     * Carga los movimientos de capital en la tabla
     */
    private void cargarMovimientosCapital() {
        try {
            MovimientoCapitalService movimientoService = new MovimientoCapitalService();
            // Usar el DAO directamente para obtener todos los movimientos
            pe.crediactiva.app.dao.MovimientoCapitalDAO movimientoDAO = new pe.crediactiva.app.dao.impl.MovimientoCapitalDAOImpl();
            List<MovimientoCapital> movimientos = movimientoDAO.findAll();
            
            ObservableList<MovimientoCapital> movimientosObservable = FXCollections.observableArrayList(movimientos);
            // Aquí necesitarías obtener la referencia a la tabla desde el contexto
            // tablaMovimientos.setItems(movimientosObservable);
            
            logger.info("Movimientos de capital cargados: " + movimientos.size());
            
        } catch (Exception e) {
            logger.error("Error al cargar movimientos de capital", e);
            showError("Error al cargar los movimientos de capital");
        }
    }
    
    /**
     * Carga los movimientos de capital en la tabla específica
     */
    private void cargarMovimientosCapitalEnTabla(TableView<MovimientoCapital> tablaMovimientos) {
        try {
            // Usar el DAO directamente para obtener todos los movimientos
            pe.crediactiva.app.dao.MovimientoCapitalDAO movimientoDAO = new pe.crediactiva.app.dao.impl.MovimientoCapitalDAOImpl();
            List<MovimientoCapital> movimientos = movimientoDAO.findAll();
            
            ObservableList<MovimientoCapital> movimientosObservable = FXCollections.observableArrayList(movimientos);
            tablaMovimientos.setItems(movimientosObservable);
            
            logger.info("Movimientos de capital cargados en tabla: " + movimientos.size());
            
        } catch (Exception e) {
            logger.error("Error al cargar movimientos de capital en tabla", e);
            showError("Error al cargar los movimientos de capital");
        }
    }
    
    /**
     * Muestra el formulario para crear un nuevo movimiento
     */
    private void mostrarFormularioNuevoMovimiento() {
        // TODO: Implementar formulario de nuevo movimiento
        showInfo("Funcionalidad de nuevo movimiento en desarrollo");
    }
    
    /**
     * Edita un movimiento existente
     */
    private void editarMovimiento(MovimientoCapital movimiento) {
        // TODO: Implementar edición de movimiento
        showInfo("Editando movimiento ID: " + movimiento.getIdMovimiento());
    }
    
    /**
     * Elimina un movimiento
     */
    private void eliminarMovimiento(MovimientoCapital movimiento) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Eliminación");
        confirmacion.setHeaderText("¿Está seguro de eliminar el movimiento?");
        confirmacion.setContentText("Movimiento ID: " + movimiento.getIdMovimiento());
        
        Optional<ButtonType> resultado = confirmacion.showAndWait();
        if (resultado.isPresent() && resultado.get() == ButtonType.OK) {
            try {
                // TODO: Implementar método para eliminar movimiento
                showInfo("Funcionalidad de eliminación de movimiento en desarrollo");
                // cargarMovimientosCapital();
                
            } catch (Exception e) {
                logger.error("Error al eliminar movimiento", e);
                showError("Error al eliminar el movimiento");
            }
        }
    }
}
