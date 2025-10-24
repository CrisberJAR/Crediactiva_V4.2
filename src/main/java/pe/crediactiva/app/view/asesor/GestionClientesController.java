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
import pe.crediactiva.app.model.Pago;
import pe.crediactiva.app.service.ClienteService;
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
 * Controlador para la gestión de clientes del asesor
 */
public class GestionClientesController {
    
    private static final Logger logger = LoggerFactory.getLogger(GestionClientesController.class);
    
    @FXML
    private TextField txtBuscar;
    
    @FXML
    private ComboBox<String> cmbFiltro;
    
    @FXML
    private TableView<Cliente> tblClientes;
    
    @FXML
    private TableColumn<Cliente, Integer> colId;
    
    @FXML
    private TableColumn<Cliente, String> colNombre;
    
    
    @FXML
    private TableColumn<Cliente, String> colTelefono;
    
    @FXML
    private TableColumn<Cliente, String> colEmail;
    
    @FXML
    private TableColumn<Cliente, String> colEstado;
    
    @FXML
    private TableColumn<Cliente, Integer> colPrestamos;
    
    @FXML
    private TableColumn<Cliente, String> colFechaRegistro;
    
    @FXML
    private Label lblInfoPaginacion;
    
    @FXML
    private Button btnAnterior;
    
    @FXML
    private Button btnSiguiente;
    
    private ClienteService clienteService;
    private PrestamoService prestamoService;
    private PagoService pagoService;
    private ObservableList<Cliente> clientes;
    private int paginaActual = 1;
    private int elementosPorPagina = 20;
    private String filtroActual = "";
    private String busquedaActual = "";
    
    public GestionClientesController() {
        this.clienteService = new ClienteService();
        this.prestamoService = new PrestamoService();
        this.pagoService = new PagoService();
        this.clientes = FXCollections.observableArrayList();
    }
    
    @FXML
    private void initialize() {
        try {
            logger.info("Inicializando gestión de clientes...");
            configurarTabla();
            cargarClientes();
            configurarFiltros();
            logger.info("Gestión de clientes inicializada correctamente");
            
        } catch (Exception e) {
            logger.error("Error al inicializar gestión de clientes", e);
            mostrarError("Error al inicializar la gestión de clientes: " + e.getMessage());
        }
    }
    
    /**
     * Configura las columnas de la tabla
     */
    private void configurarTabla() {
        colId.setCellValueFactory(new PropertyValueFactory<>("idCliente"));
        colNombre.setCellValueFactory(cellData -> {
            Cliente cliente = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                cliente.getNombre() + " " + cliente.getApellido()
            );
        });
        colTelefono.setCellValueFactory(new PropertyValueFactory<>("telefono"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colEstado.setCellValueFactory(cellData -> {
            Cliente cliente = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                cliente.isActivo() ? "Activo" : "Inactivo"
            );
        });
        colPrestamos.setCellValueFactory(cellData -> {
            Cliente cliente = cellData.getValue();
            try {
                // Obtener el número de préstamos activos del cliente
                int prestamosActivos = prestamoService.contarPrestamosActivosPorCliente(cliente.getIdCliente());
                return new javafx.beans.property.SimpleObjectProperty<>(prestamosActivos);
            } catch (Exception e) {
                logger.error("Error al obtener préstamos activos para cliente: " + cliente.getIdCliente(), e);
                return new javafx.beans.property.SimpleObjectProperty<>(0);
            }
        });
        colFechaRegistro.setCellValueFactory(cellData -> {
            Cliente cliente = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                FechaUtil.formatearFecha(cliente.getFechaRegistro())
            );
        });
        
        // Configurar selección de fila
        tblClientes.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    }
    
    /**
     * Configura los filtros disponibles
     */
    private void configurarFiltros() {
        cmbFiltro.getItems().addAll("Todos", "Activos", "Inactivos", "Con préstamos", "Sin préstamos");
        cmbFiltro.setValue("Todos");
    }
    
    /**
     * Carga los clientes del asesor según los filtros actuales
     */
    private void cargarClientes() {
        try {
            // Obtener el ID del asesor actual de la sesión
            Long idAsesor = SessionManager.getInstance().getAsesorId();
            
            if (idAsesor == null) {
                logger.error("No se pudo obtener el ID del asesor de la sesión");
                mostrarError("Error: No se pudo identificar al asesor");
                return;
            }
            
            // Obtener solo los clientes del asesor actual
            List<Cliente> listaClientes = clienteService.obtenerClientesPorAsesor(idAsesor);
            
            // Aplicar búsqueda si hay texto de búsqueda
            if (busquedaActual != null && !busquedaActual.isEmpty()) {
                listaClientes = filtrarPorBusqueda(listaClientes, busquedaActual);
            }
            
            // Aplicar filtro si hay uno seleccionado
            if (filtroActual != null && !filtroActual.isEmpty()) {
                listaClientes = aplicarFiltro(listaClientes, filtroActual);
            }
            
            clientes.clear();
            clientes.addAll(listaClientes);
            tblClientes.setItems(clientes);
            
            actualizarInfoPaginacion();
            
            logger.info("Cargados " + listaClientes.size() + " clientes para el asesor: " + idAsesor + 
                       " (Búsqueda: '" + busquedaActual + "', Filtro: '" + filtroActual + "')");
            
        } catch (Exception e) {
            logger.error("Error al cargar clientes", e);
            mostrarError("Error al cargar la lista de clientes");
        }
    }
    
    /**
     * Filtra la lista de clientes según el texto de búsqueda (DNI o nombre)
     */
    private List<Cliente> filtrarPorBusqueda(List<Cliente> clientes, String textoBusqueda) {
        String busquedaLower = textoBusqueda.toLowerCase().trim();
        
        return clientes.stream()
            .filter(cliente -> {
                // Buscar por DNI (idCliente)
                String dni = String.valueOf(cliente.getIdCliente());
                if (dni.contains(busquedaLower)) {
                    return true;
                }
                
                // Buscar por nombre completo
                String nombreCompleto = (cliente.getNombre() + " " + cliente.getApellido()).toLowerCase();
                if (nombreCompleto.contains(busquedaLower)) {
                    return true;
                }
                
                // Buscar por teléfono
                if (cliente.getTelefono() != null && cliente.getTelefono().contains(busquedaLower)) {
                    return true;
                }
                
                return false;
            })
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Aplica el filtro seleccionado a la lista de clientes
     */
    private List<Cliente> aplicarFiltro(List<Cliente> clientes, String filtro) {
        switch (filtro) {
            case "activos":
                return clientes.stream()
                    .filter(Cliente::isActivo)
                    .collect(java.util.stream.Collectors.toList());
                    
            case "inactivos":
                return clientes.stream()
                    .filter(cliente -> !cliente.isActivo())
                    .collect(java.util.stream.Collectors.toList());
                    
            case "con_prestamos":
                return clientes.stream()
                    .filter(cliente -> {
                        try {
                            int prestamosActivos = prestamoService.contarPrestamosActivosPorCliente(cliente.getIdCliente());
                            return prestamosActivos > 0;
                        } catch (Exception e) {
                            logger.error("Error al contar préstamos del cliente: " + cliente.getIdCliente(), e);
                            return false;
                        }
                    })
                    .collect(java.util.stream.Collectors.toList());
                    
            case "sin_prestamos":
                return clientes.stream()
                    .filter(cliente -> {
                        try {
                            int prestamosActivos = prestamoService.contarPrestamosActivosPorCliente(cliente.getIdCliente());
                            return prestamosActivos == 0;
                        } catch (Exception e) {
                            logger.error("Error al contar préstamos del cliente: " + cliente.getIdCliente(), e);
                            return false;
                        }
                    })
                    .collect(java.util.stream.Collectors.toList());
                    
            default:
                return clientes;
        }
    }
    
    /**
     * Actualiza la información de paginación
     */
    private void actualizarInfoPaginacion() {
        try {
            // TODO: Implementar conteo en ClienteService
            int totalClientes = clientes.size();
            int inicio = (paginaActual - 1) * elementosPorPagina + 1;
            int fin = Math.min(paginaActual * elementosPorPagina, totalClientes);
            
            lblInfoPaginacion.setText(
                String.format("Mostrando %d-%d de %d clientes", inicio, fin, totalClientes)
            );
            
            // Habilitar/deshabilitar botones de paginación
            btnAnterior.setDisable(paginaActual <= 1);
            btnSiguiente.setDisable(fin >= totalClientes);
            
        } catch (Exception e) {
            logger.error("Error al actualizar información de paginación", e);
        }
    }
    
    /**
     * Maneja la búsqueda de clientes por DNI, nombre o teléfono
     */
    @FXML
    private void handleBuscar() {
        busquedaActual = txtBuscar.getText().trim();
        paginaActual = 1;
        cargarClientes();
        
        if (busquedaActual.isEmpty()) {
            logger.info("Búsqueda vacía - mostrando todos los clientes");
        } else {
            logger.info("Búsqueda realizada: '" + busquedaActual + "' - Resultados: " + clientes.size());
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
        cargarClientes();
        logger.info("Filtros limpiados");
    }
    
    /**
     * Maneja el filtrado de clientes
     */
    @FXML
    private void handleFiltrar() {
        String filtroSeleccionado = cmbFiltro.getValue();
        if (filtroSeleccionado != null) {
            switch (filtroSeleccionado) {
                case "Activos":
                    filtroActual = "activos";
                    break;
                case "Inactivos":
                    filtroActual = "inactivos";
                    break;
                case "Con préstamos":
                    filtroActual = "con_prestamos";
                    break;
                case "Sin préstamos":
                    filtroActual = "sin_prestamos";
                    break;
                default:
                    filtroActual = "";
                    break;
            }
        } else {
            filtroActual = "";
        }
        
        paginaActual = 1;
        cargarClientes();
        
        if (filtroActual.isEmpty()) {
            logger.info("Filtro 'Todos' aplicado - Resultados: " + clientes.size());
        } else {
            logger.info("Filtro aplicado: '" + filtroActual + "' - Resultados: " + clientes.size());
        }
    }
    
    /**
     * Maneja la creación de un nuevo cliente
     */
    @FXML
    private void handleNuevoCliente() {
        try {
            // Crear una nueva ventana modal para el nuevo cliente
            Stage nuevoClienteStage = new Stage();
            
            // Cargar la pantalla del nuevo cliente
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/asesor/NuevoClienteView.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            // Configurar la nueva ventana
            nuevoClienteStage.setTitle("CrediActiva - Nuevo Cliente");
            nuevoClienteStage.setScene(scene);
            nuevoClienteStage.setMinWidth(800);
            nuevoClienteStage.setMinHeight(600);
            nuevoClienteStage.setResizable(false);
            
            // Centrar la ventana
            nuevoClienteStage.centerOnScreen();
            
            // Mostrar la ventana modal
            nuevoClienteStage.showAndWait();
            
            // Refrescar la tabla después de cerrar la ventana
            cargarClientes();
            
            logger.info("Ventana de nuevo cliente cerrada, tabla actualizada");
            
        } catch (Exception e) {
            logger.error("Error al abrir formulario de nuevo cliente", e);
            mostrarError("Error al abrir el formulario de nuevo cliente");
        }
    }
    
    
    /**
     * Maneja la visualización de detalles del cliente
     */
    @FXML
    private void handleVerDetalles() {
        Cliente clienteSeleccionado = tblClientes.getSelectionModel().getSelectedItem();
        
        if (clienteSeleccionado == null) {
            mostrarAdvertencia("Por favor seleccione un cliente para ver sus detalles");
            return;
        }
        
        try {
            // Crear una ventana modal para mostrar los detalles del cliente
            Stage detallesStage = new Stage();
            
            // Crear el contenido de la ventana de detalles
            VBox contenido = crearVentanaDetallesCliente(clienteSeleccionado);
            
            // Crear un ScrollPane para hacer el contenido scrolleable
            ScrollPane scrollPane = new ScrollPane(contenido);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setStyle("-fx-background-color: #f8fafc;");
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            
            Scene scene = new Scene(scrollPane, 700, 600);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            // Configurar la ventana
            detallesStage.setTitle("CrediActiva - Detalles del Cliente");
            detallesStage.setScene(scene);
            detallesStage.setResizable(true);
            detallesStage.setMinWidth(600);
            detallesStage.setMinHeight(400);
            detallesStage.centerOnScreen();
            
            // Mostrar la ventana modal
            detallesStage.showAndWait();
            
            logger.info("Ventana de detalles del cliente cerrada para cliente: " + clienteSeleccionado.getIdCliente());
            
        } catch (Exception e) {
            logger.error("Error al mostrar detalles del cliente", e);
            mostrarError("Error al mostrar los detalles del cliente");
        }
    }
    
    /**
     * Crea la ventana de detalles del cliente
     */
    private VBox crearVentanaDetallesCliente(Cliente cliente) {
        VBox contenido = new VBox(20);
        contenido.setPadding(new javafx.geometry.Insets(30));
        contenido.setStyle("-fx-background-color: #f8fafc;");
        contenido.setMinWidth(550);
        
        // Título
        Label titulo = new Label("📋 Detalles del Cliente");
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        // Información personal
        VBox infoPersonal = new VBox(15);
        infoPersonal.setStyle("-fx-background-color: white; -fx-background-radius: 10px; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        
        Label tituloPersonal = new Label("👤 Información Personal");
        tituloPersonal.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        
        // Crear grid para información personal
        GridPane gridPersonal = new GridPane();
        gridPersonal.setHgap(20);
        gridPersonal.setVgap(10);
        
        // ID del cliente (DNI)
        gridPersonal.add(new Label("DNI:"), 0, 0);
        gridPersonal.add(new Label(String.valueOf(cliente.getIdCliente())), 1, 0);
        
        // Nombre completo
        gridPersonal.add(new Label("Nombre:"), 0, 1);
        gridPersonal.add(new Label(cliente.getNombre() + " " + cliente.getApellido()), 1, 1);
        
        // Teléfono
        gridPersonal.add(new Label("Teléfono:"), 0, 2);
        gridPersonal.add(new Label(cliente.getTelefono()), 1, 2);
        
        // Email
        gridPersonal.add(new Label("Email:"), 0, 3);
        gridPersonal.add(new Label(cliente.getEmail()), 1, 3);
        
        // Dirección
        gridPersonal.add(new Label("Dirección:"), 0, 4);
        gridPersonal.add(new Label(cliente.getDireccion()), 1, 4);
        
        // Fecha de registro
        gridPersonal.add(new Label("Fecha Registro:"), 0, 5);
        gridPersonal.add(new Label(FechaUtil.formatearFecha(cliente.getFechaRegistro())), 1, 5);
        
        // Estado
        gridPersonal.add(new Label("Estado:"), 0, 6);
        Label estadoLabel = new Label(cliente.isActivo() ? "Activo" : "Inactivo");
        estadoLabel.setStyle(cliente.isActivo() ? "-fx-text-fill: #10b981; -fx-font-weight: bold;" : "-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        gridPersonal.add(estadoLabel, 1, 6);
        
        infoPersonal.getChildren().addAll(tituloPersonal, gridPersonal);
        
        // Información de préstamos
        VBox infoPrestamos = new VBox(15);
        infoPrestamos.setStyle("-fx-background-color: white; -fx-background-radius: 10px; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        
        Label tituloPrestamos = new Label("💰 Información de Préstamos");
        tituloPrestamos.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        
        try {
            // Obtener información de préstamos del cliente
            int prestamosActivos = prestamoService.contarPrestamosActivosPorCliente(cliente.getIdCliente());
            
            GridPane gridPrestamos = new GridPane();
            gridPrestamos.setHgap(20);
            gridPrestamos.setVgap(10);
            
            gridPrestamos.add(new Label("Préstamos Activos:"), 0, 0);
            gridPrestamos.add(new Label(String.valueOf(prestamosActivos)), 1, 0);
            
            infoPrestamos.getChildren().addAll(tituloPrestamos, gridPrestamos);
            
        } catch (Exception e) {
            logger.error("Error al obtener información de préstamos", e);
            infoPrestamos.getChildren().addAll(tituloPrestamos, new Label("Error al cargar información de préstamos"));
        }
        
        // Botón de cerrar
        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5px;");
        btnCerrar.setOnAction(e -> ((Stage) btnCerrar.getScene().getWindow()).close());
        
        // Contenedor para el botón centrado
        HBox botonContainer = new HBox();
        botonContainer.setAlignment(javafx.geometry.Pos.CENTER);
        botonContainer.getChildren().add(btnCerrar);
        
        contenido.getChildren().addAll(titulo, infoPersonal, infoPrestamos, botonContainer);
        
        return contenido;
    }
    
    /**
     * Maneja la visualización del historial del cliente
     */
    @FXML
    private void handleHistorial() {
        Cliente clienteSeleccionado = tblClientes.getSelectionModel().getSelectedItem();
        
        if (clienteSeleccionado == null) {
            mostrarAdvertencia("Por favor seleccione un cliente para ver su historial");
            return;
        }
        
        try {
            // Crear una ventana modal para mostrar el historial del cliente
            Stage historialStage = new Stage();
            
            // Crear el contenido de la ventana de historial
            VBox contenido = crearVentanaHistorialCliente(clienteSeleccionado);
            
            // Crear un ScrollPane para hacer el contenido scrolleable
            ScrollPane scrollPane = new ScrollPane(contenido);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setStyle("-fx-background-color: #f8fafc;");
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
            
            Scene scene = new Scene(scrollPane, 900, 700);
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            
            // Configurar la ventana
            historialStage.setTitle("CrediActiva - Historial del Cliente");
            historialStage.setScene(scene);
            historialStage.setResizable(true);
            historialStage.setMinWidth(800);
            historialStage.setMinHeight(500);
            historialStage.centerOnScreen();
            
            // Mostrar la ventana modal
            historialStage.showAndWait();
            
            logger.info("Ventana de historial del cliente cerrada para cliente: " + clienteSeleccionado.getIdCliente());
            
        } catch (Exception e) {
            logger.error("Error al mostrar historial del cliente", e);
            mostrarError("Error al mostrar el historial del cliente");
        }
    }
    
    /**
     * Crea la ventana de historial del cliente
     */
    private VBox crearVentanaHistorialCliente(Cliente cliente) {
        VBox contenido = new VBox(20);
        contenido.setPadding(new javafx.geometry.Insets(30));
        contenido.setStyle("-fx-background-color: #f8fafc;");
        contenido.setMinWidth(750);
        
        // Título
        Label titulo = new Label("📜 Historial del Cliente: " + cliente.getNombre() + " " + cliente.getApellido());
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        
        // Sección de resumen
        VBox resumen = crearSeccionResumen(cliente);
        
        // Sección de préstamos
        VBox prestamos = crearSeccionPrestamos(cliente);
        
        // Sección de pagos
        VBox pagos = crearSeccionPagos(cliente);
        
        // Botón de cerrar
        Button btnCerrar = new Button("Cerrar");
        btnCerrar.setStyle("-fx-background-color: #6b7280; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 20; -fx-background-radius: 5px;");
        btnCerrar.setOnAction(e -> ((Stage) btnCerrar.getScene().getWindow()).close());
        
        HBox botonContainer = new HBox();
        botonContainer.setAlignment(javafx.geometry.Pos.CENTER);
        botonContainer.getChildren().add(btnCerrar);
        
        contenido.getChildren().addAll(titulo, resumen, prestamos, pagos, botonContainer);
        
        return contenido;
    }
    
    /**
     * Crea la sección de resumen del historial
     */
    private VBox crearSeccionResumen(Cliente cliente) {
        VBox seccion = new VBox(15);
        seccion.setStyle("-fx-background-color: white; -fx-background-radius: 10px; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        
        Label tituloSeccion = new Label("📊 Resumen General");
        tituloSeccion.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        
        GridPane gridResumen = new GridPane();
        gridResumen.setHgap(30);
        gridResumen.setVgap(10);
        
        try {
            int totalPrestamos = prestamoService.obtenerPrestamosPorCliente(cliente.getIdCliente()).size();
            int prestamosActivos = prestamoService.contarPrestamosActivosPorCliente(cliente.getIdCliente());
            double totalPagado = prestamoService.obtenerTotalPagadoPorCliente(cliente.getIdCliente());
            double montoPendiente = prestamoService.obtenerMontoPendientePorCliente(cliente.getIdCliente());
            
            gridResumen.add(new Label("Total de Préstamos:"), 0, 0);
            Label lblTotalPrestamos = new Label(String.valueOf(totalPrestamos));
            lblTotalPrestamos.setStyle("-fx-font-weight: bold;");
            gridResumen.add(lblTotalPrestamos, 1, 0);
            
            gridResumen.add(new Label("Préstamos Activos:"), 0, 1);
            Label lblPrestamosActivos = new Label(String.valueOf(prestamosActivos));
            lblPrestamosActivos.setStyle("-fx-font-weight: bold; -fx-text-fill: #10b981;");
            gridResumen.add(lblPrestamosActivos, 1, 1);
            
            gridResumen.add(new Label("Total Pagado:"), 2, 0);
            Label lblTotalPagado = new Label(String.format("S/ %.2f", totalPagado));
            lblTotalPagado.setStyle("-fx-font-weight: bold; -fx-text-fill: #10b981;");
            gridResumen.add(lblTotalPagado, 3, 0);
            
            gridResumen.add(new Label("Monto Pendiente:"), 2, 1);
            Label lblMontoPendiente = new Label(String.format("S/ %.2f", montoPendiente));
            lblMontoPendiente.setStyle("-fx-font-weight: bold; -fx-text-fill: #ef4444;");
            gridResumen.add(lblMontoPendiente, 3, 1);
            
        } catch (Exception e) {
            logger.error("Error al obtener resumen del cliente", e);
            gridResumen.add(new Label("Error al cargar resumen"), 0, 0);
        }
        
        seccion.getChildren().addAll(tituloSeccion, gridResumen);
        return seccion;
    }
    
    /**
     * Crea la sección de préstamos del historial
     */
    private VBox crearSeccionPrestamos(Cliente cliente) {
        VBox seccion = new VBox(15);
        seccion.setStyle("-fx-background-color: white; -fx-background-radius: 10px; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        
        Label tituloSeccion = new Label("💼 Historial de Préstamos");
        tituloSeccion.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        
        // Crear tabla de préstamos
        TableView<Prestamo> tablaPrestamos = new TableView<>();
        tablaPrestamos.setPrefHeight(250);
        tablaPrestamos.setMaxHeight(250);
        
        TableColumn<Prestamo, Long> colIdPrestamo = new TableColumn<>("ID");
        colIdPrestamo.setCellValueFactory(new PropertyValueFactory<>("idPrestamo"));
        colIdPrestamo.setPrefWidth(60);
        
        TableColumn<Prestamo, String> colFechaCreacion = new TableColumn<>("Fecha Creación");
        colFechaCreacion.setCellValueFactory(cellData -> {
            Prestamo prestamo = cellData.getValue();
            if (prestamo.getCreadoEn() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    prestamo.getCreadoEn().toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                );
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        colFechaCreacion.setPrefWidth(120);
        
        TableColumn<Prestamo, String> colMonto = new TableColumn<>("Monto");
        colMonto.setCellValueFactory(cellData -> {
            Prestamo prestamo = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                String.format("S/ %.2f", prestamo.getMontoSolicitado())
            );
        });
        colMonto.setPrefWidth(120);
        
        TableColumn<Prestamo, String> colTasa = new TableColumn<>("Tasa");
        colTasa.setCellValueFactory(cellData -> {
            Prestamo prestamo = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                String.format("%.2f%%", prestamo.getTasaInteres())
            );
        });
        colTasa.setPrefWidth(80);
        
        TableColumn<Prestamo, Integer> colCuotas = new TableColumn<>("Cuotas");
        colCuotas.setCellValueFactory(new PropertyValueFactory<>("numeroCuotas"));
        colCuotas.setPrefWidth(80);
        
        TableColumn<Prestamo, String> colEstado = new TableColumn<>("Estado");
        colEstado.setCellValueFactory(cellData -> {
            Prestamo prestamo = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                prestamo.getEstado().toString()
            );
        });
        colEstado.setPrefWidth(100);
        
        TableColumn<Prestamo, String> colFechaInicio = new TableColumn<>("Fecha Inicio");
        colFechaInicio.setCellValueFactory(cellData -> {
            Prestamo prestamo = cellData.getValue();
            if (prestamo.getFechaInicio() != null) {
                return new javafx.beans.property.SimpleStringProperty(
                    FechaUtil.formatearFecha(prestamo.getFechaInicio())
                );
            }
            return new javafx.beans.property.SimpleStringProperty("-");
        });
        colFechaInicio.setPrefWidth(130);
        
        tablaPrestamos.getColumns().addAll(colIdPrestamo, colFechaCreacion, colMonto, colTasa, colCuotas, colEstado, colFechaInicio);
        
        try {
            List<Prestamo> listaPrestamos = prestamoService.obtenerPrestamosPorCliente(cliente.getIdCliente());
            tablaPrestamos.getItems().addAll(listaPrestamos);
            
            if (listaPrestamos.isEmpty()) {
                Label lblSinDatos = new Label("No hay préstamos registrados");
                lblSinDatos.setStyle("-fx-text-fill: #6b7280;");
                seccion.getChildren().addAll(tituloSeccion, lblSinDatos);
            } else {
                seccion.getChildren().addAll(tituloSeccion, tablaPrestamos);
            }
            
        } catch (Exception e) {
            logger.error("Error al cargar préstamos del cliente", e);
            seccion.getChildren().addAll(tituloSeccion, new Label("Error al cargar préstamos"));
        }
        
        return seccion;
    }
    
    /**
     * Crea la sección de pagos del historial
     */
    private VBox crearSeccionPagos(Cliente cliente) {
        VBox seccion = new VBox(15);
        seccion.setStyle("-fx-background-color: white; -fx-background-radius: 10px; -fx-padding: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 3);");
        
        Label tituloSeccion = new Label("💰 Historial de Pagos");
        tituloSeccion.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #374151;");
        
        // Crear tabla de pagos
        TableView<Pago> tablaPagos = new TableView<>();
        tablaPagos.setPrefHeight(250);
        tablaPagos.setMaxHeight(250);
        
        TableColumn<Pago, Long> colIdPago = new TableColumn<>("ID Pago");
        colIdPago.setCellValueFactory(new PropertyValueFactory<>("idPago"));
        colIdPago.setPrefWidth(80);
        
        TableColumn<Pago, Long> colIdCuota = new TableColumn<>("ID Cuota");
        colIdCuota.setCellValueFactory(new PropertyValueFactory<>("idCuota"));
        colIdCuota.setPrefWidth(80);
        
        TableColumn<Pago, String> colFechaPago = new TableColumn<>("Fecha Pago");
        colFechaPago.setCellValueFactory(cellData -> {
            Pago pago = cellData.getValue();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            return new javafx.beans.property.SimpleStringProperty(
                pago.getFechaPago().format(formatter)
            );
        });
        colFechaPago.setPrefWidth(150);
        
        TableColumn<Pago, String> colMontoPagado = new TableColumn<>("Monto Pagado");
        colMontoPagado.setCellValueFactory(cellData -> {
            Pago pago = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(
                String.format("S/ %.2f", pago.getMontoPagado())
            );
        });
        colMontoPagado.setPrefWidth(120);
        
        TableColumn<Pago, String> colValidado = new TableColumn<>("Validado");
        colValidado.setCellValueFactory(cellData -> {
            Pago pago = cellData.getValue();
            String validado = pago.isValidado() ? "Sí" : "No";
            return new javafx.beans.property.SimpleStringProperty(validado);
        });
        colValidado.setPrefWidth(80);
        
        TableColumn<Pago, String> colObservaciones = new TableColumn<>("Observaciones");
        colObservaciones.setCellValueFactory(cellData -> {
            Pago pago = cellData.getValue();
            String obs = pago.getObservaciones() != null ? pago.getObservaciones() : "-";
            return new javafx.beans.property.SimpleStringProperty(obs);
        });
        colObservaciones.setPrefWidth(250);
        
        tablaPagos.getColumns().addAll(colIdPago, colIdCuota, colFechaPago, colMontoPagado, colValidado, colObservaciones);
        
        try {
            List<Pago> listaPagos = pagoService.obtenerPagosPorCliente(cliente.getIdCliente());
            tablaPagos.getItems().addAll(listaPagos);
            
            if (listaPagos.isEmpty()) {
                Label lblSinDatos = new Label("No hay pagos registrados");
                lblSinDatos.setStyle("-fx-text-fill: #6b7280;");
                seccion.getChildren().addAll(tituloSeccion, lblSinDatos);
            } else {
                seccion.getChildren().addAll(tituloSeccion, tablaPagos);
            }
            
        } catch (Exception e) {
            logger.error("Error al cargar pagos del cliente", e);
            seccion.getChildren().addAll(tituloSeccion, new Label("Error al cargar pagos"));
        }
        
        return seccion;
    }
    
    /**
     * Actualiza la lista de clientes
     */
    @FXML
    private void handleActualizar() {
        cargarClientes();
        mostrarInfo("Lista de clientes actualizada");
    }
    
    /**
     * Navega a la página anterior
     */
    @FXML
    private void handleAnterior() {
        if (paginaActual > 1) {
            paginaActual--;
            cargarClientes();
        }
    }
    
    /**
     * Navega a la página siguiente
     */
    @FXML
    private void handleSiguiente() {
        try {
            // TODO: Implementar paginación real
            int totalClientes = clientes.size();
            int totalPaginas = (int) Math.ceil((double) totalClientes / elementosPorPagina);
            
            if (paginaActual < totalPaginas) {
                paginaActual++;
                cargarClientes();
            }
        } catch (Exception e) {
            logger.error("Error al navegar a la página siguiente", e);
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
