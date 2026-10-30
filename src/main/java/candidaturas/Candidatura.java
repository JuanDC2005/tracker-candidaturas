package candidaturas;

public class Candidatura {
    private Integer id;
    private String titulo;
    private String empresa;
    private String estado;
    private String fechaPostulacion;
    private String enlace;
    private String notas;


    public Candidatura() {}

    public Candidatura(Integer id, String titulo, String empresa, String estado, String fechaPostulacion, String enlace, String notas) {
        this.id = id;
        this.titulo = titulo;
        this.empresa = empresa;
        this.estado = estado;
        this.fechaPostulacion = fechaPostulacion;
        this.enlace = enlace;
        this.notas = notas;
    }

    public Integer getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getEmpresa() { return empresa; }
    public String getEstado() { return estado; }
    public String getFechaPostulacion() { return fechaPostulacion; }
    public String getEnlace() { return enlace; }
    public String getNotas() { return notas; }

    public void setId(Integer id) { this.id = id; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setEmpresa(String empresa) { this.empresa = empresa; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setFechaPostulacion(String fechaPostulacion) { this.fechaPostulacion = fechaPostulacion; }
    public void setEnlace(String enlace) { this.enlace = enlace; }
    public void setNotas(String notas) { this.notas = notas; }

    @Override
    public String toString() {
        return "[" + id + "] " + titulo + " en " + empresa + " | Estado: " + estado + " | Fecha: " + fechaPostulacion;
    }
}
