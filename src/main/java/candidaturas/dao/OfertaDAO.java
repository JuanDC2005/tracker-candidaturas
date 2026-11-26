package candidaturas.dao;

import candidaturas.bd.Conexion;
import candidaturas.modelo.Empresa;
import candidaturas.modelo.Oferta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Operaciones CRUD de la tabla oferta usando JDBC.
 * Las consultas hacen JOIN con empresa para traer sus datos.
 */
public class OfertaDAO {

    private static final String SELECT_BASE =
            "SELECT o.*, e.nombre AS e_nombre, e.sector AS e_sector, e.web AS e_web, e.fechaRegistro AS e_fecha " +
            "FROM oferta o JOIN empresa e ON o.id_empresa = e.id";

    /**
     * Devuelve todas las ofertas con su empresa.
     *
     * @return lista de ofertas
     * @throws SQLException si falla la consulta
     */
    public List<Oferta> listar() throws SQLException {
        return filtrar(null, null, null, null, null, null);
    }

    /**
     * Filtra las ofertas. Los parametros que lleguen a null no se tienen en cuenta,
     * asi se pueden combinar como se quiera.
     *
     * @param texto      texto que debe aparecer en el titulo
     * @param idEmpresa  id de la empresa
     * @param estado     estado de la oferta
     * @param salarioMin salario minimo
     * @param desde      fecha de postulacion minima
     * @param hasta      fecha de postulacion maxima
     * @return lista de ofertas que cumplen los filtros
     * @throws SQLException si falla la consulta
     */
    public List<Oferta> filtrar(String texto, Integer idEmpresa, String estado, Double salarioMin,
                                LocalDate desde, LocalDate hasta) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_BASE + " WHERE 1=1");
        List<Object> parametros = new ArrayList<>();

        if (texto != null && !texto.isBlank()) {
            sql.append(" AND LOWER(o.titulo) LIKE ?");
            parametros.add("%" + texto.toLowerCase() + "%");
        }
        if (idEmpresa != null) {
            sql.append(" AND o.id_empresa = ?");
            parametros.add(idEmpresa);
        }
        if (estado != null && !estado.isBlank()) {
            sql.append(" AND o.estado = ?");
            parametros.add(estado);
        }
        if (salarioMin != null) {
            sql.append(" AND o.salario >= ?");
            parametros.add(salarioMin);
        }
        if (desde != null) {
            sql.append(" AND o.fechaPostulacion >= ?");
            parametros.add(desde.toString());
        }
        if (hasta != null) {
            sql.append(" AND o.fechaPostulacion <= ?");
            parametros.add(hasta.toString());
        }
        sql.append(" ORDER BY o.fechaPostulacion DESC");

        List<Oferta> lista = new ArrayList<>();
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lista.add(leerOferta(rs));
                }
            }
        }
        return lista;
    }

    /**
     * Busca una oferta por su id.
     *
     * @param id id de la oferta
     * @return la oferta o null si no existe
     * @throws SQLException si falla la consulta
     */
    public Oferta buscarPorId(int id) throws SQLException {
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(SELECT_BASE + " WHERE o.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return leerOferta(rs);
                }
            }
        }
        return null;
    }

    /**
     * Inserta una oferta nueva y le asigna el id generado.
     *
     * @param o oferta a insertar (debe tener empresa)
     * @throws SQLException si falla la insercion
     */
    public void insertar(Oferta o) throws SQLException {
        try (Connection con = Conexion.getConexion()) {
            insertar(con, o);
        }
    }

    /**
     * Actualiza todos los campos de una oferta.
     *
     * @param o oferta con los datos nuevos
     * @throws SQLException si falla la actualizacion
     */
    public void actualizar(Oferta o) throws SQLException {
        String sql = "UPDATE oferta SET id_empresa = ?, titulo = ?, descripcion = ?, estado = ?, " +
                "fechaPostulacion = ?, enlace = ?, salario = ?, notas = ? WHERE id = ?";
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            rellenarCampos(ps, o);
            ps.setInt(9, o.getId());
            ps.executeUpdate();
        }
    }

    /**
     * Cambia solo el estado de una oferta.
     *
     * @param id     id de la oferta
     * @param estado estado nuevo
     * @throws SQLException si falla la actualizacion
     */
    public void cambiarEstado(int id, String estado) throws SQLException {
        try (Connection con = Conexion.getConexion();
             PreparedStatement ps = con.prepareStatement("UPDATE oferta SET estado = ? WHERE id = ?")) {
            ps.setString(1, estado);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    /**
     * Borra una oferta y sus entrevistas en una transaccion.
     *
     * @param id id de la oferta
     * @throws SQLException si falla el borrado (se deshacen los cambios)
     */
    public void borrar(int id) throws SQLException {
        try (Connection con = Conexion.getConexion()) {
            con.setAutoCommit(false);
            try (PreparedStatement psEnt = con.prepareStatement("DELETE FROM entrevista WHERE id_oferta = ?");
                 PreparedStatement psOf = con.prepareStatement("DELETE FROM oferta WHERE id = ?")) {
                psEnt.setInt(1, id);
                psEnt.executeUpdate();
                psOf.setInt(1, id);
                psOf.executeUpdate();
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            }
        }
    }

    /**
     * Inserta una oferta usando una conexion ya abierta (para usarlo dentro de transacciones).
     */
    void insertar(Connection con, Oferta o) throws SQLException {
        String sql = "INSERT INTO oferta (id_empresa, titulo, descripcion, estado, fechaPostulacion, " +
                "enlace, salario, notas) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            rellenarCampos(ps, o);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    o.setId(rs.getInt(1));
                }
            }
        }
    }

    private void rellenarCampos(PreparedStatement ps, Oferta o) throws SQLException {
        ps.setInt(1, o.getEmpresa().getId());
        ps.setString(2, o.getTitulo());
        ps.setString(3, o.getDescripcion());
        ps.setString(4, o.getEstado());
        ps.setString(5, o.getFechaPostulacion() != null ? o.getFechaPostulacion().toString() : null);
        ps.setString(6, o.getEnlace());
        if (o.getSalario() != null) {
            ps.setDouble(7, o.getSalario());
        } else {
            ps.setNull(7, Types.REAL);
        }
        ps.setString(8, o.getNotas());
    }

    private Oferta leerOferta(ResultSet rs) throws SQLException {
        String fechaEmp = rs.getString("e_fecha");
        Empresa empresa = new Empresa(
                rs.getInt("id_empresa"),
                rs.getString("e_nombre"),
                rs.getString("e_sector"),
                rs.getString("e_web"),
                fechaEmp != null ? LocalDate.parse(fechaEmp) : null
        );

        String fecha = rs.getString("fechaPostulacion");
        double salario = rs.getDouble("salario");
        Double salarioObj = rs.wasNull() ? null : salario;

        return new Oferta(
                rs.getInt("id"),
                rs.getString("titulo"),
                empresa,
                rs.getString("estado"),
                fecha != null ? LocalDate.parse(fecha) : null,
                rs.getString("enlace"),
                rs.getString("descripcion"),
                salarioObj,
                rs.getString("notas")
        );
    }
}
