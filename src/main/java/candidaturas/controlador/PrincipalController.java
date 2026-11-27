package candidaturas.controlador;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

/**
 * Controlador de la ventana principal. Tiene el menu lateral y
 * cambia la vista del centro segun el boton pulsado.
 */
public class PrincipalController {

    @FXML
    private StackPane contenido;

    @FXML
    private VBox menuLateral;

    @FXML
    private Button btnCandidaturas;

    @FXML
    private void mostrarCandidaturas() {
        mostrarVista("/vista/ofertas.fxml", btnCandidaturas);
    }

    /**
     * Carga un FXML y lo pone en el centro de la ventana.
     *
     * @param rutaFxml ruta del FXML dentro de resources
     * @param boton    boton del menu que se marca como activo
     */
    private void mostrarVista(String rutaFxml, Button boton) {
        try {
            Parent vista = FXMLLoader.load(getClass().getResource(rutaFxml));
            contenido.getChildren().setAll(vista);
            marcarActivo(boton);
        } catch (IOException | NullPointerException e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setHeaderText("No se pudo abrir la pantalla");
            alerta.setContentText(rutaFxml);
            alerta.showAndWait();
        }
    }

    private void marcarActivo(Button boton) {
        for (var nodo : menuLateral.getChildren()) {
            nodo.getStyleClass().remove("boton-menu-activo");
        }
        boton.getStyleClass().add("boton-menu-activo");
    }
}
