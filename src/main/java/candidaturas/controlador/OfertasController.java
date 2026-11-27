package candidaturas.controlador;

import candidaturas.dao.EmpresaDAO;
import candidaturas.dao.ImportadorCSV;
import candidaturas.dao.OfertaDAO;
import candidaturas.modelo.Empresa;
import candidaturas.modelo.Oferta;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Controlador de la pantalla principal de candidaturas (listado de ofertas).
 */
public class OfertasController {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private TableView<Oferta> tablaOfertas;
    @FXML private TableColumn<Oferta, String> colTitulo;
    @FXML private TableColumn<Oferta, String> colEmpresa;
    @FXML private TableColumn<Oferta, String> colEstado;
    @FXML private TableColumn<Oferta, LocalDate> colFecha;
    @FXML private TableColumn<Oferta, Double> colSalario;

    @FXML private Label lblTotal;
    @FXML private Label lblPendientes;
    @FXML private Label lblEntrevista;
    @FXML private Label lblAceptadas;

    @FXML private TextField txtBuscar;
    @FXML private ComboBox<Empresa> cbEmpresa;
    @FXML private ComboBox<String> cbEstado;
    @FXML private TextField txtSalarioMin;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;

    @FXML private Button btnEditar;
    @FXML private Button btnBorrar;

    private final OfertaDAO ofertaDAO = new OfertaDAO();
    private final EmpresaDAO empresaDAO = new EmpresaDAO();
    private final ObservableList<Oferta> ofertas = FXCollections.observableArrayList();

    @FXML
    private void initialize() {
        colTitulo.setCellValueFactory(new PropertyValueFactory<>("titulo"));
        colEmpresa.setCellValueFactory(new PropertyValueFactory<>("nombreEmpresa"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaPostulacion"));
        colSalario.setCellValueFactory(new PropertyValueFactory<>("salario"));

        // La fecha se muestra en formato dd/MM/yyyy
        colFecha.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate fecha, boolean vacia) {
                super.updateItem(fecha, vacia);
                setText(vacia || fecha == null ? null : fecha.format(FORMATO_FECHA));
            }
        });

        // El salario con el simbolo del euro
        colSalario.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double salario, boolean vacia) {
                super.updateItem(salario, vacia);
                setText(vacia || salario == null ? null : String.format("%,.0f €", salario));
            }
        });

        // En la columna estado se pone un desplegable para cambiarlo desde la tabla
        colEstado.setCellFactory(col -> new CeldaEstado());

        tablaOfertas.setItems(ofertas);

        // Los botones Editar y Borrar solo se activan si hay una fila seleccionada
        btnEditar.disableProperty().bind(tablaOfertas.getSelectionModel().selectedItemProperty().isNull());
        btnBorrar.disableProperty().bind(tablaOfertas.getSelectionModel().selectedItemProperty().isNull());

        prepararFiltros();
        cargarOfertas();
    }

    /**
     * Rellena los desplegables de filtro y hace que la tabla se filtre
     * cada vez que cambia alguno.
     */
    private void prepararFiltros() {
        cbEstado.getItems().addAll("pendiente", "entrevista", "rechazado", "aceptado");
        cargarEmpresasFiltro();

        txtBuscar.textProperty().addListener((obs, antes, ahora) -> cargarOfertas());
        txtSalarioMin.textProperty().addListener((obs, antes, ahora) -> cargarOfertas());
        cbEmpresa.valueProperty().addListener((obs, antes, ahora) -> cargarOfertas());
        cbEstado.valueProperty().addListener((obs, antes, ahora) -> cargarOfertas());
        dpDesde.valueProperty().addListener((obs, antes, ahora) -> cargarOfertas());
        dpHasta.valueProperty().addListener((obs, antes, ahora) -> cargarOfertas());
    }

    private void cargarEmpresasFiltro() {
        try {
            cbEmpresa.getItems().setAll(empresaDAO.listar());
        } catch (SQLException e) {
            mostrarError("No se pudieron cargar las empresas", e.getMessage());
        }
    }

    /**
     * Vuelve a leer las ofertas de la base de datos aplicando los filtros
     * y actualiza la tabla y los contadores.
     */
    private void cargarOfertas() {
        Integer idEmpresa = cbEmpresa.getValue() != null ? cbEmpresa.getValue().getId() : null;
        Double salarioMin = leerSalarioMin();

        try {
            ofertas.setAll(ofertaDAO.filtrar(txtBuscar.getText(), idEmpresa, cbEstado.getValue(),
                    salarioMin, dpDesde.getValue(), dpHasta.getValue()));
            actualizarContadores();
        } catch (SQLException e) {
            mostrarError("No se pudieron cargar las candidaturas", e.getMessage());
        }
    }

    /**
     * Lee el salario minimo del filtro. Si no es un numero se marca en rojo y no se filtra por salario.
     */
    private Double leerSalarioMin() {
        String texto = txtSalarioMin.getText().trim().replace(",", ".");
        txtSalarioMin.getStyleClass().remove("campo-error");
        if (texto.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(texto);
        } catch (NumberFormatException e) {
            txtSalarioMin.getStyleClass().add("campo-error");
            return null;
        }
    }

    @FXML
    private void limpiarFiltros() {
        txtBuscar.clear();
        txtSalarioMin.clear();
        cbEmpresa.setValue(null);
        cbEstado.setValue(null);
        dpDesde.setValue(null);
        dpHasta.setValue(null);
    }

    private void actualizarContadores() {
        lblTotal.setText(String.valueOf(ofertas.size()));
        lblPendientes.setText(String.valueOf(contarEstado("pendiente")));
        lblEntrevista.setText(String.valueOf(contarEstado("entrevista")));
        lblAceptadas.setText(String.valueOf(contarEstado("aceptado")));
    }

    private long contarEstado(String estado) {
        return ofertas.stream().filter(o -> estado.equals(o.getEstado())).count();
    }

    @FXML
    private void importarCsv() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Elige el CSV de candidaturas");
        selector.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos CSV", "*.csv"));
        selector.setInitialDirectory(new File("."));
        File archivo = selector.showOpenDialog(tablaOfertas.getScene().getWindow());
        if (archivo == null) {
            return;
        }

        try {
            int total = new ImportadorCSV().importar(archivo.getAbsolutePath());
            cargarEmpresasFiltro();
            cargarOfertas();
            Alert info = new Alert(Alert.AlertType.INFORMATION, "Se han importado " + total + " candidaturas.");
            info.setHeaderText("Importacion terminada");
            info.showAndWait();
        } catch (SQLException | IllegalArgumentException e) {
            mostrarError("Error al importar el CSV", e.getMessage());
        }
    }

    @FXML
    private void nuevaOferta() {
        if (FormularioOfertaController.abrir(tablaOfertas.getScene().getWindow(), null)) {
            cargarOfertas();
        }
    }

    @FXML
    private void editarOferta() {
        Oferta seleccionada = tablaOfertas.getSelectionModel().getSelectedItem();
        if (seleccionada != null && FormularioOfertaController.abrir(tablaOfertas.getScene().getWindow(), seleccionada)) {
            cargarOfertas();
        }
    }

    @FXML
    private void borrarOferta() {
        Oferta seleccionada = tablaOfertas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            return;
        }
        Alert confirmar = new Alert(Alert.AlertType.CONFIRMATION,
                "Se borrará \"" + seleccionada.getTitulo() + "\" y todas sus entrevistas.");
        confirmar.setHeaderText("¿Borrar candidatura?");
        if (confirmar.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        try {
            ofertaDAO.borrar(seleccionada.getId());
            cargarOfertas();
        } catch (SQLException e) {
            mostrarError("No se pudo borrar la candidatura", e.getMessage());
        }
    }

    /**
     * Cambia el estado de una oferta en la base de datos y actualiza los contadores.
     */
    private void cambiarEstado(Oferta oferta, String nuevoEstado) {
        if (nuevoEstado == null || nuevoEstado.equals(oferta.getEstado())) {
            return;
        }
        try {
            ofertaDAO.cambiarEstado(oferta.getId(), nuevoEstado);
            oferta.setEstado(nuevoEstado);
            actualizarContadores();
        } catch (SQLException e) {
            mostrarError("No se pudo cambiar el estado", e.getMessage());
        }
    }

    /**
     * Celda de la columna estado: un ComboBox con un color segun el estado.
     */
    private class CeldaEstado extends TableCell<Oferta, String> {
        private final ComboBox<String> combo = new ComboBox<>();

        CeldaEstado() {
            combo.getItems().addAll("pendiente", "entrevista", "rechazado", "aceptado");
            combo.setMaxWidth(Double.MAX_VALUE);
            combo.setOnAction(e -> {
                Oferta oferta = getTableRow().getItem();
                if (oferta != null) {
                    cambiarEstado(oferta, combo.getValue());
                    ponerColor(combo.getValue());
                }
            });
        }

        @Override
        protected void updateItem(String estado, boolean vacia) {
            super.updateItem(estado, vacia);
            if (vacia || estado == null) {
                setGraphic(null);
            } else {
                combo.setValue(estado);
                ponerColor(estado);
                setGraphic(combo);
            }
        }

        private void ponerColor(String estado) {
            combo.getStyleClass().removeAll("estado-pendiente", "estado-entrevista", "estado-rechazado", "estado-aceptado");
            combo.getStyleClass().add("estado-" + estado);
        }
    }

    private void mostrarError(String cabecera, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR, mensaje);
        alerta.setHeaderText(cabecera);
        alerta.showAndWait();
    }
}
