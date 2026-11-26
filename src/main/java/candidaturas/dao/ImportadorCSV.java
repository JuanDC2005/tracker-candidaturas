package candidaturas.dao;

import candidaturas.Candidatura;
import candidaturas.GestorArchivos;
import candidaturas.bd.Conexion;
import candidaturas.modelo.Empresa;
import candidaturas.modelo.Oferta;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Importa las candidaturas del CSV de la v1 a la base de datos.
 * Todo se hace en una transaccion: o se importan todas o ninguna.
 */
public class ImportadorCSV {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final OfertaDAO ofertaDAO = new OfertaDAO();

    /**
     * Lee el CSV e inserta cada candidatura como oferta. Si la empresa no existe se crea.
     *
     * @param ruta ruta del archivo CSV
     * @return numero de ofertas importadas
     * @throws SQLException si falla algo en la base de datos (se hace rollback)
     */
    public int importar(String ruta) throws SQLException {
        if (!new File(ruta).exists()) {
            throw new IllegalArgumentException("No se encuentra el archivo " + ruta);
        }
        List<Candidatura> candidaturas = GestorArchivos.cargar(ruta);

        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);
            try {
                for (Candidatura c : candidaturas) {
                    Empresa empresa = buscarOCrearEmpresa(con, c.getEmpresa());
                    Oferta oferta = new Oferta(null, c.getTitulo(), empresa, c.getEstado(),
                            convertirFecha(c.getFechaPostulacion()), c.getEnlace(), null, null, c.getNotas());
                    ofertaDAO.insertar(con, oferta);
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
        return candidaturas.size();
    }

    private Empresa buscarOCrearEmpresa(Connection con, String nombre) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement("SELECT id FROM empresa WHERE nombre = ?")) {
            ps.setString(1, nombre);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Empresa(rs.getInt("id"), nombre, null, null, null);
                }
            }
        }

        Empresa nueva = new Empresa(null, nombre, null, null, LocalDate.now());
        String sql = "INSERT INTO empresa (nombre, fechaRegistro) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setString(2, nueva.getFechaRegistro().toString());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    nueva.setId(rs.getInt(1));
                }
            }
        }
        return nueva;
    }

    private LocalDate convertirFecha(String texto) {
        if (texto == null || texto.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(texto, FORMATO);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
