package candidaturas;

import candidaturas.bd.Conexion;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Clase principal de la aplicacion JavaFX (v2.0).
 */
public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        Conexion.crearTablas();

        FXMLLoader loader = new FXMLLoader(App.class.getResource("/vista/principal.fxml"));
        Scene scene = new Scene(loader.load(), 1150, 700);
        scene.getStylesheets().add(App.class.getResource("/vista/estilos.css").toExternalForm());

        stage.setTitle("Tracker de Candidaturas");
        stage.setMinWidth(950);
        stage.setMinHeight(600);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
