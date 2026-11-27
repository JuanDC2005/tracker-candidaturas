package candidaturas.controlador;

import candidaturas.dao.ImportadorCSV;
import candidaturas.dao.OfertaDAO;
import candidaturas.modelo.Oferta;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
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

    private final OfertaDAO ofertaDAO = new OfertaDAO();
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
        cargarOfertas();
    }

    /**
     * Vuelve a leer las ofertas de la base de datos y actualiza la tabla y los contadores.
     */
    private void cargarOfertas() {
        try {
            ofertas.setAll(ofertaDAO.listar());
            actualizarContadores();
        } catch (SQLException e) {
            mostrarError("No se pudieron cargar las candidaturas", e.getMessage());
        }
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
