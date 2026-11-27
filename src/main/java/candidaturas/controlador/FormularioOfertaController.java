package candidaturas.controlador;

import candidaturas.dao.EmpresaDAO;
import candidaturas.dao.OfertaDAO;
import candidaturas.modelo.Empresa;
import candidaturas.modelo.Oferta;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Controlador del formulario de alta y edicion de candidaturas (ofertas).
 */
public class FormularioOfertaController {

    @FXML private Label lblTitulo;
    @FXML private TextField txtTitulo;
    @FXML private ComboBox<Empresa> cbEmpresa;
    @FXML private TextField txtSalario;
    @FXML private TextField txtEnlace;
    @FXML private TextArea txtDescripcion;
    @FXML private ComboBox<String> cbEstado;
    @FXML private DatePicker dpFecha;
    @FXML private TextArea txtNotas;
    @FXML private Label lblError;

    private final OfertaDAO ofertaDAO = new OfertaDAO();
    private final EmpresaDAO empresaDAO = new EmpresaDAO();

    /** Oferta que se edita, o null si es nueva. */
    private Oferta oferta;
    private boolean guardado = false;

    /**
     * Abre el formulario en una ventana modal.
     *
     * @param padre  ventana desde la que se abre
     * @param oferta oferta a editar, o null para crear una nueva
     * @return true si se ha guardado
     */
    public static boolean abrir(Window padre, Oferta oferta) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    FormularioOfertaController.class.getResource("/vista/formulario-oferta.fxml"));
            Parent raiz = loader.load();
            FormularioOfertaController controlador = loader.getController();
            controlador.setOferta(oferta);

            Scene scene = new Scene(raiz);
            scene.getStylesheets().addAll(padre.getScene().getStylesheets());

            Stage ventana = new Stage();
            ventana.setTitle(oferta == null ? "Nueva candidatura" : "Editar candidatura");
            ventana.initOwner(padre);
            ventana.initModality(Modality.WINDOW_MODAL);
            ventana.setScene(scene);
            ventana.showAndWait();
            return controlador.guardado;
        } catch (IOException e) {
            System.out.println("No se pudo abrir el formulario de oferta: " + e.getMessage());
            return false;
        }
    }

    @FXML
    private void initialize() {
        cbEstado.getItems().addAll("pendiente", "entrevista", "rechazado", "aceptado");
        cbEstado.setValue("pendiente");
        dpFecha.setValue(LocalDate.now());
        try {
            cbEmpresa.getItems().setAll(empresaDAO.listar());
        } catch (SQLException e) {
            lblError.setText("No se pudieron cargar las empresas");
        }
    }

    private void setOferta(Oferta oferta) {
        this.oferta = oferta;
        if (oferta == null) {
            return;
        }
        lblTitulo.setText("Editar candidatura");
        txtTitulo.setText(oferta.getTitulo());
        // Se busca la empresa en la lista por id para que el ComboBox la seleccione
        for (Empresa e : cbEmpresa.getItems()) {
            if (e.getId().equals(oferta.getEmpresa().getId())) {
                cbEmpresa.setValue(e);
            }
        }
        txtSalario.setText(oferta.getSalario() != null ? String.valueOf(oferta.getSalario()) : "");
        txtEnlace.setText(oferta.getEnlace());
        txtDescripcion.setText(oferta.getDescripcion());
        cbEstado.setValue(oferta.getEstado());
        dpFecha.setValue(oferta.getFechaPostulacion());
        txtNotas.setText(oferta.getNotas());
    }

    @FXML
    private void guardar() {
        String titulo = txtTitulo.getText().trim();
        String enlace = txtEnlace.getText() == null ? "" : txtEnlace.getText().trim();
        String textoSalario = txtSalario.getText().trim().replace(",", ".");

        if (titulo.isEmpty()) {
            lblError.setText("El puesto es obligatorio");
            return;
        }
        if (cbEmpresa.getValue() == null) {
            lblError.setText("Elige una empresa (si no existe, créala en la pantalla Empresas)");
            return;
        }
        Double salario = null;
        if (!textoSalario.isEmpty()) {
            try {
                salario = Double.parseDouble(textoSalario);
            } catch (NumberFormatException e) {
                lblError.setText("El salario tiene que ser un número");
                return;
            }
            if (salario < 0) {
                lblError.setText("El salario no puede ser negativo");
                return;
            }
        }
        if (!enlace.isEmpty() && !enlace.startsWith("http://") && !enlace.startsWith("https://")) {
            lblError.setText("El enlace tiene que empezar por http:// o https://");
            return;
        }

        try {
            if (oferta == null) {
                Oferta nueva = new Oferta(null, titulo, cbEmpresa.getValue(), cbEstado.getValue(), dpFecha.getValue(),
                        enlace, txtDescripcion.getText(), salario, txtNotas.getText());
                ofertaDAO.insertar(nueva);
            } else {
                oferta.setTitulo(titulo);
                oferta.setEmpresa(cbEmpresa.getValue());
                oferta.setEstado(cbEstado.getValue());
                oferta.setFechaPostulacion(dpFecha.getValue());
                oferta.setEnlace(enlace);
                oferta.setDescripcion(txtDescripcion.getText());
                oferta.setSalario(salario);
                oferta.setNotas(txtNotas.getText());
                ofertaDAO.actualizar(oferta);
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
        ((Stage) txtTitulo.getScene().getWindow()).close();
    }
}
