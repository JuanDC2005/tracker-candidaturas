package candidaturas.dao;

import candidaturas.bd.Conexion;
import candidaturas.modelo.Empresa;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Operaciones CRUD de la tabla empresa usando JDBC.
 */
public class EmpresaDAO {

    /**
     * Devuelve todas las empresas ordenadas por nombre.
     *
     * @return lista de empresas
     * @throws SQLException si falla la consulta
     */
    public List<Empresa> listar() throws SQLException {
        List<Empresa> lista = new ArrayList<>();
        String sql = "SELECT * FROM empresa ORDER BY nombre";
        try (Connection con = Conexion.getConexion();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(leerEmpresa(rs));
            }
        }
        return lista;
    }

    /**
     * Busca una empresa por su id.
     *
     * @param id id de la empresa
     * @return la empresa o null si no existe
     * @throws SQLException si falla la consulta
     */
    public Empresa buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM empresa WHERE id = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return leerEmpresa(rs);
                }
            }
        }
        return null;
    }

    /**
     * Inserta una empresa nueva y le asigna el id generado.
     *
     * @param e empresa a insertar
     * @throws SQLException si falla la insercion (por ejemplo, nombre repetido)
     */
    public void insertar(Empresa e) throws SQLException {
        String sql = "INSERT INTO empresa (nombre, sector, web, fechaRegistro) VALUES (?, ?, ?, ?)";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getSector());
            ps.setString(3, e.getWeb());
            ps.setString(4, e.getFechaRegistro() != null ? e.getFechaRegistro().toString() : LocalDate.now().toString());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    e.setId(rs.getInt(1));
                }
            }
        }
    }

    /**
     * Actualiza los datos de una empresa existente.
     *
     * @param e empresa con los datos nuevos
     * @throws SQLException si falla la actualizacion
     */
    public void actualizar(Empresa e) throws SQLException {
        String sql = "UPDATE empresa SET nombre = ?, sector = ?, web = ? WHERE id = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, e.getNombre());
            ps.setString(2, e.getSector());
            ps.setString(3, e.getWeb());
            ps.setInt(4, e.getId());
            ps.executeUpdate();
        }
    }

    /**
     * Borra una empresa. Si tiene ofertas asociadas SQLite no deja borrarla
     * por la clave foranea y salta una SQLException.
     *
     * @param id id de la empresa
     * @throws SQLException si falla el borrado
     */
    public void borrar(int id) throws SQLException {
        String sql = "DELETE FROM empresa WHERE id = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    private Empresa leerEmpresa(ResultSet rs) throws SQLException {
        String fecha = rs.getString("fechaRegistro");
        return new Empresa(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("sector"),
                rs.getString("web"),
                fecha != null ? LocalDate.parse(fecha) : null
        );
    }
}
