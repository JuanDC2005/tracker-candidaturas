package candidaturas.controlador;

import candidaturas.dao.EntrevistaDAO;
import candidaturas.modelo.Entrevista;
import candidaturas.modelo.Oferta;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Controlador de la ventana de detalle de una candidatura.
 * Muestra todos los datos de la oferta y la lista de sus entrevistas.
 */
public class DetalleOfertaController {

    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML private Label lblTitulo;
    @FXML private Label lblEmpresa;
    @FXML private Label lblEstado;
    @FXML private Label lblFecha;
    @FXML private Label lblSalario;
    @FXML private Label lblEnlace;
    @FXML private Label lblSector;
    @FXML private Label lblDescripcion;
    @FXML private Label lblNotas;

    @FXML private TitledPane panelEntrevistas;
    @FXML private TableView<Entrevista> tablaEntrevistas;
    @FXML private TableColumn<Entrevista, LocalDate> colFecha;
    @FXML private TableColumn<Entrevista, String> colTipo;
    @FXML private TableColumn<Entrevista, String> colResultado;
    @FXML private TableColumn<Entrevista, String> colNotas;
    @FXML private Button btnEditarEntrevista;
    @FXML private Button btnBorrarEntrevista;

    private final EntrevistaDAO entrevistaDAO = new EntrevistaDAO();
    private final ObservableList<Entrevista> entrevistas = FXCollections.observableArrayList();

    private Oferta oferta;

    /**
     * Abre el detalle de una oferta en una ventana modal.
     *
     * @param padre  ventana desde la que se abre
     * @param oferta oferta que se quiere ver
     */
    public static void abrir(Window padre, Oferta oferta) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    DetalleOfertaController.class.getResource("/vista/detalle-oferta.fxml"));
            Parent raiz = loader.load();
            DetalleOfertaController controlador = loader.getController();
            controlador.setOferta(oferta);

            Scene scene = new Scene(raiz);
            scene.getStylesheets().addAll(padre.getScene().getStylesheets());

            Stage ventana = new Stage();
            ventana.setTitle("Detalle de la candidatura");
            ventana.initOwner(padre);
            ventana.initModality(Modality.WINDOW_MODAL);
            ventana.setScene(scene);
            ventana.showAndWait();
        } catch (IOException e) {
            System.out.println("No se pudo abrir el detalle: " + e.getMessage());
        }
    }

    @FXML
    private void initialize() {
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colResultado.setCellValueFactory(new PropertyValueFactory<>("resultado"));
        colNotas.setCellValueFactory(new PropertyValueFactory<>("notas"));

        colFecha.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate fecha, boolean vacia) {
                super.updateItem(fecha, vacia);
                setText(vacia || fecha == null ? null : fecha.format(FORMATO_FECHA));
            }
        });

        tablaEntrevistas.setItems(entrevistas);

        // Editar y borrar solo si hay una entrevista seleccionada
        btnEditarEntrevista.disableProperty().bind(tablaEntrevistas.getSelectionModel().selectedItemProperty().isNull());
        btnBorrarEntrevista.disableProperty().bind(tablaEntrevistas.getSelectionModel().selectedItemProperty().isNull());

        // Doble clic en una entrevista para editarla
        tablaEntrevistas.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                editarEntrevista();
            }
        });
    }

    private void setOferta(Oferta oferta) {
        this.oferta = oferta;

        lblTitulo.setText(oferta.getTitulo());
        lblEmpresa.setText(oferta.getNombreEmpresa());
        lblEstado.setText(oferta.getEstado());
        lblEstado.getStyleClass().add("estado-" + oferta.getEstado());
        lblFecha.setText(oferta.getFechaPostulacion() != null ? oferta.getFechaPostulacion().format(FORMATO_FECHA) : "-");
        lblSalario.setText(oferta.getSalario() != null ? String.format("%,.0f €", oferta.getSalario()) : "-");
        lblEnlace.setText(textoOGuion(oferta.getEnlace()));
        lblSector.setText(oferta.getEmpresa() != null ? textoOGuion(oferta.getEmpresa().getSector()) : "-");
        lblDescripcion.setText(textoOGuion(oferta.getDescripcion()));
        lblNotas.setText(textoOGuion(oferta.getNotas()));

        cargarEntrevistas();
    }

    private String textoOGuion(String texto) {
        return texto == null || texto.isBlank() ? "-" : texto;
    }

    private void cargarEntrevistas() {
        try {
            entrevistas.setAll(entrevistaDAO.listarPorOferta(oferta.getId()));
            panelEntrevistas.setText("Entrevistas (" + entrevistas.size() + ")");
        } catch (SQLException e) {
            mostrarError("No se pudieron cargar las entrevistas", e.getMessage());
        }
    }

    @FXML
    private void nuevaEntrevista() {
        if (FormularioEntrevistaController.abrir(ventana(), oferta.getId(), null)) {
            cargarEntrevistas();
        }
    }

    @FXML
    private void editarEntrevista() {
        Entrevista seleccionada = tablaEntrevistas.getSelectionModel().getSelectedItem();
        if (seleccionada != null && FormularioEntrevistaController.abrir(ventana(), oferta.getId(), seleccionada)) {
            cargarEntrevistas();
        }
    }

    @FXML
    private void borrarEntrevista() {
        Entrevista seleccionada = tablaEntrevistas.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            return;
        }
        Alert confirmar = new Alert(Alert.AlertType.CONFIRMATION, "Se borrará la entrevista seleccionada.");
        confirmar.setHeaderText("¿Borrar entrevista?");
        if (confirmar.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        try {
            entrevistaDAO.borrar(seleccionada.getId());
            cargarEntrevistas();
        } catch (SQLException e) {
            mostrarError("No se pudo borrar la entrevista", e.getMessage());
        }
    }

    @FXML
    private void cerrar() {
        ((Stage) ventana()).close();
    }

    private Window ventana() {
        return lblTitulo.getScene().getWindow();
    }

    private void mostrarError(String cabecera, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR, mensaje);
        alerta.setHeaderText(cabecera);
        alerta.showAndWait();
    }
}
