package candidaturas.controlador;

import candidaturas.dao.EmpresaDAO;
import candidaturas.modelo.Empresa;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Controlador de la pantalla de gestion de empresas (tabla + formulario).
 */
public class EmpresasController {

    @FXML private TableView<Empresa> tablaEmpresas;
    @FXML private TableColumn<Empresa, String> colNombre;
    @FXML private TableColumn<Empresa, String> colSector;
    @FXML private TableColumn<Empresa, String> colWeb;
    @FXML private TableColumn<Empresa, LocalDate> colFecha;

    @FXML private Label lblFormulario;
    @FXML private TextField txtNombre;
    @FXML private ComboBox<String> cbSector;
    @FXML private TextField txtWeb;
    @FXML private Label lblError;
    @FXML private Button btnBorrar;

    private final EmpresaDAO empresaDAO = new EmpresaDAO();
    private final ObservableList<Empresa> empresas = FXCollections.observableArrayList();

    /** Empresa que se esta editando, o null si es una nueva. */
    private Empresa seleccionada;

    @FXML
    private void initialize() {
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colSector.setCellValueFactory(new PropertyValueFactory<>("sector"));
        colWeb.setCellValueFactory(new PropertyValueFactory<>("web"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fechaRegistro"));
        tablaEmpresas.setItems(empresas);

        cbSector.getItems().addAll("Tecnología", "Consultoría", "Banca", "Telecomunicaciones", "Otro");

        // Al seleccionar una fila se cargan sus datos en el formulario
        tablaEmpresas.getSelectionModel().selectedItemProperty().addListener((obs, antigua, nueva) -> {
            if (nueva != null) {
                cargarFormulario(nueva);
            }
        });

        cargarEmpresas();
    }

    private void cargarEmpresas() {
        try {
            empresas.setAll(empresaDAO.listar());
        } catch (SQLException e) {
            mostrarError("No se pudieron cargar las empresas", e.getMessage());
        }
    }

    private void cargarFormulario(Empresa e) {
        seleccionada = e;
        lblFormulario.setText("Editar empresa");
        txtNombre.setText(e.getNombre());
        cbSector.setValue(e.getSector());
        txtWeb.setText(e.getWeb());
        lblError.setText("");
        btnBorrar.setDisable(false);
    }

    @FXML
    private void nuevo() {
        seleccionada = null;
        tablaEmpresas.getSelectionModel().clearSelection();
        lblFormulario.setText("Nueva empresa");
        txtNombre.clear();
        cbSector.setValue(null);
        txtWeb.clear();
        lblError.setText("");
        btnBorrar.setDisable(true);
        txtNombre.requestFocus();
    }

    @FXML
    private void guardar() {
        String nombre = txtNombre.getText().trim();
        String sector = cbSector.getEditor().getText().trim();
        String web = txtWeb.getText().trim();

        String error = validar(nombre, web);
        if (error != null) {
            lblError.setText(error);
            return;
        }

        try {
            if (seleccionada == null) {
                Empresa nueva = new Empresa(null, nombre, sector, web, LocalDate.now());
                empresaDAO.insertar(nueva);
            } else {
                seleccionada.setNombre(nombre);
                seleccionada.setSector(sector);
                seleccionada.setWeb(web);
                empresaDAO.actualizar(seleccionada);
            }
            cargarEmpresas();
            nuevo();
        } catch (SQLException e) {
            lblError.setText("Ya existe una empresa con ese nombre o ha fallado la base de datos");
        }
    }

    @FXML
    private void borrar() {
        if (seleccionada == null) {
            return;
        }
        Alert confirmar = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Seguro que quieres borrar " + seleccionada.getNombre() + "?");
        confirmar.setHeaderText("Borrar empresa");
        Optional<ButtonType> respuesta = confirmar.showAndWait();
        if (respuesta.isEmpty() || respuesta.get() != ButtonType.OK) {
            return;
        }

        try {
            empresaDAO.borrar(seleccionada.getId());
            cargarEmpresas();
            nuevo();
        } catch (SQLException e) {
            mostrarError("No se puede borrar la empresa",
                    "Tiene candidaturas asociadas. Borra primero sus candidaturas.");
        }
    }

    /**
     * Comprueba los campos del formulario.
     *
     * @return mensaje de error o null si todo esta bien
     */
    private String validar(String nombre, String web) {
        if (nombre.isEmpty()) {
            return "El nombre es obligatorio";
        }
        if (nombre.length() > 100) {
            return "El nombre es demasiado largo";
        }
        if (!web.isEmpty() && !web.startsWith("http://") && !web.startsWith("https://")) {
            return "La web tiene que empezar por http:// o https://";
        }
        return null;
    }

    private void mostrarError(String cabecera, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR, mensaje);
        alerta.setHeaderText(cabecera);
        alerta.showAndWait();
    }
}
