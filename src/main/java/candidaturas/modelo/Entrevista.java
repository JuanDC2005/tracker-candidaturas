package candidaturas.modelo;

import java.time.LocalDate;

/**
 * Entrevista asociada a una oferta.
 */
public class Entrevista {
    private Integer id;
    private Integer idOferta;
    private LocalDate fecha;
    private String tipo;
    private String notas;
    private String resultado;

    public Entrevista() {}

    public Entrevista(Integer id, Integer idOferta, LocalDate fecha, String tipo, String notas, String resultado) {
        this.id = id;
        this.idOferta = idOferta;
        this.fecha = fecha;
        this.tipo = tipo;
        this.notas = notas;
        this.resultado = resultado;
    }

    public Integer getId() { return id; }
    public Integer getIdOferta() { return idOferta; }
    public LocalDate getFecha() { return fecha; }
    public String getTipo() { return tipo; }
    public String getNotas() { return notas; }
    public String getResultado() { return resultado; }

    public void setId(Integer id) { this.id = id; }
    public void setIdOferta(Integer idOferta) { this.idOferta = idOferta; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setNotas(String notas) { this.notas = notas; }
    public void setResultado(String resultado) { this.resultado = resultado; }

    @Override
    public String toString() {
        return fecha + " - " + tipo + " (" + resultado + ")";
    }
}
