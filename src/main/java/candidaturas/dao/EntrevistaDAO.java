package candidaturas.dao;

import candidaturas.bd.Conexion;
import candidaturas.modelo.Entrevista;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Operaciones CRUD de la tabla entrevista usando JDBC.
 */
public class EntrevistaDAO {

    /**
     * Devuelve las entrevistas de una oferta ordenadas por fecha.
     *
     * @param idOferta id de la oferta
     * @return lista de entrevistas
     * @throws SQLException si falla la consulta
     */
    public List<Entrevista> listarPorOferta(int idOferta) throws SQLException {
        List<Entrevista> lista = new ArrayList<>();
        String sql = "SELECT * FROM entrevista WHERE id_oferta = ? ORDER BY fecha";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idOferta);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(leerEntrevista(rs));
                }
            }
        }
        return lista;
    }

    /**
     * Inserta una entrevista nueva y le asigna el id generado.
     *
     * @param e entrevista a insertar
     * @throws SQLException si falla la insercion
     */
    public void insertar(Entrevista e) throws SQLException {
        String sql = "INSERT INTO entrevista (id_oferta, fecha, tipo, notas, resultado) VALUES (?, ?, ?, ?, ?)";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, e.getIdOferta());
            ps.setString(2, e.getFecha() != null ? e.getFecha().toString() : null);
            ps.setString(3, e.getTipo());
            ps.setString(4, e.getNotas());
            ps.setString(5, e.getResultado() != null ? e.getResultado() : "pendiente");
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    e.setId(rs.getInt(1));
                }
            }
        }
    }

    /**
     * Actualiza una entrevista existente.
     *
     * @param e entrevista con los datos nuevos
     * @throws SQLException si falla la actualizacion
     */
    public void actualizar(Entrevista e) throws SQLException {
        String sql = "UPDATE entrevista SET fecha = ?, tipo = ?, notas = ?, resultado = ? WHERE id = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getFecha() != null ? e.getFecha().toString() : null);
            ps.setString(2, e.getTipo());
            ps.setString(3, e.getNotas());
            ps.setString(4, e.getResultado());
            ps.setInt(5, e.getId());
            ps.executeUpdate();
        }
    }

    /**
     * Borra una entrevista.
     *
     * @param id id de la entrevista
     * @throws SQLException si falla el borrado
     */
    public void borrar(int id) throws SQLException {
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement("DELETE FROM entrevista WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Entrevista leerEntrevista(ResultSet rs) throws SQLException {
        String fecha = rs.getString("fecha");
        return new Entrevista(
                rs.getInt("id"),
                rs.getInt("id_oferta"),
                fecha != null ? LocalDate.parse(fecha) : null,
                rs.getString("tipo"),
                rs.getString("notas"),
                rs.getString("resultado")
        );
    }
}
