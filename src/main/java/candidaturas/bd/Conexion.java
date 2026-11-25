package candidaturas.bd;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Clase que se encarga de abrir conexiones con la base de datos SQLite.
 * La URL se lee del archivo config.properties.
 */
public class Conexion {

    private static final String ARCHIVO_CONFIG = "config.properties";
    private static String url;

    /**
     * Devuelve una conexion nueva a la base de datos.
     * Hay que cerrarla despues de usarla (try-with-resources).
     *
     * @return conexion abierta
     * @throws SQLException si no se puede conectar
     */
    public static Connection getConexion() throws SQLException {
        if (url == null) {
            url = leerUrl();
        }
        Connection con = DriverManager.getConnection(url);
        // En SQLite las claves foraneas vienen desactivadas por defecto
        try (Statement st = con.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        return con;
    }

    /**
     * Crea las tablas si no existen ejecutando el script schema.sql.
     */
    public static void crearTablas() {
        try (InputStream in = Conexion.class.getResourceAsStream("/schema.sql");
             Connection con = getConexion();
             Statement st = con.createStatement()) {

            String script = new String(in.readAllBytes());
            for (String sentencia : script.split(";")) {
                if (!sentencia.isBlank()) {
                    st.execute(sentencia);
                }
            }
        } catch (IOException | SQLException e) {
            System.out.println("Error al crear las tablas: " + e.getMessage());
        }
    }

    private static String leerUrl() {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(ARCHIVO_CONFIG)) {
            props.load(fis);
        } catch (IOException e) {
            System.out.println("No se encuentra " + ARCHIVO_CONFIG + ", se usa la base de datos por defecto");
        }
        return props.getProperty("db.url", "jdbc:sqlite:tracker.db");
    }
}
