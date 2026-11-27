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

        tablaOfertas.setItems(ofertas);

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

    private void mostrarError(String cabecera, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR, mensaje);
        alerta.setHeaderText(cabecera);
        alerta.showAndWait();
    }
}
