package candidaturas.controlador;

import candidaturas.dao.EntrevistaDAO;
import candidaturas.modelo.Entrevista;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Controlador del formulario de alta y edicion de entrevistas.
 * Se abre como ventana modal desde el detalle de una oferta.
 */
public class FormularioEntrevistaController {

    @FXML private Label lblTitulo;
    @FXML private DatePicker dpFecha;
    @FXML private ComboBox<String> cbTipo;
    @FXML private ComboBox<String> cbResultado;
    @FXML private TextArea txtNotas;
    @FXML private Label lblError;

    private final EntrevistaDAO entrevistaDAO = new EntrevistaDAO();

    private int idOferta;
    /** Entrevista que se edita, o null si es nueva. */
    private Entrevista entrevista;
    private boolean guardado = false;

    /**
     * Abre el formulario en una ventana modal y espera a que se cierre.
     *
     * @param padre      ventana desde la que se abre
     * @param idOferta   oferta a la que pertenece la entrevista
     * @param entrevista entrevista a editar, o null para crear una nueva
     * @return true si se ha guardado algo
     */
    public static boolean abrir(Window padre, int idOferta, Entrevista entrevista) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    FormularioEntrevistaController.class.getResource("/vista/formulario-entrevista.fxml"));
            Parent raiz = loader.load();
            FormularioEntrevistaController controlador = loader.getController();
            controlador.setDatos(idOferta, entrevista);

            Scene scene = new Scene(raiz);
            scene.getStylesheets().addAll(padre.getScene().getStylesheets());

            Stage ventana = new Stage();
            ventana.setTitle(entrevista == null ? "Nueva entrevista" : "Editar entrevista");
            ventana.initOwner(padre);
            ventana.initModality(Modality.WINDOW_MODAL);
            ventana.setScene(scene);
            ventana.showAndWait();
            return controlador.guardado;
        } catch (IOException e) {
            System.out.println("No se pudo abrir el formulario de entrevista: " + e.getMessage());
            return false;
        }
    }

    @FXML
    private void initialize() {
        cbTipo.getItems().addAll("telefónica", "técnica", "presencial", "online");
        cbResultado.getItems().addAll("pendiente", "positivo", "negativo");
        cbResultado.setValue("pendiente");
    }

    private void setDatos(int idOferta, Entrevista entrevista) {
        this.idOferta = idOferta;
        this.entrevista = entrevista;
        if (entrevista != null) {
            lblTitulo.setText("Editar entrevista");
            dpFecha.setValue(entrevista.getFecha());
            cbTipo.setValue(entrevista.getTipo());
            cbResultado.setValue(entrevista.getResultado());
            txtNotas.setText(entrevista.getNotas());
        }
    }

    @FXML
    private void guardar() {
        if (dpFecha.getValue() == null) {
            lblError.setText("La fecha es obligatoria");
            return;
        }
        if (cbTipo.getValue() == null) {
            lblError.setText("Elige el tipo de entrevista");
            return;
        }

        try {
            if (entrevista == null) {
                Entrevista nueva = new Entrevista(null, idOferta, dpFecha.getValue(), cbTipo.getValue(),
                        txtNotas.getText(), cbResultado.getValue());
                entrevistaDAO.insertar(nueva);
            } else {
                entrevista.setFecha(dpFecha.getValue());
                entrevista.setTipo(cbTipo.getValue());
                entrevista.setResultado(cbResultado.getValue());
                entrevista.setNotas(txtNotas.getText());
                entrevistaDAO.actualizar(entrevista);
            }
            guardado = true;
            cerrar();
        } catch (SQLException e) {
            lblError.setText("Error al guardar: " + e.getMessage());
        }
    }

    @FXML
    private void cancelar() {
        cerrar();
    }

    private void cerrar() {
        ((Stage) dpFecha.getScene().getWindow()).close();
    }
}
