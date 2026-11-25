package candidaturas.modelo;

import java.time.LocalDate;

/**
 * Candidatura a una oferta de empleo. Desde la v2 la empresa es un objeto.
 */
public class Oferta {
    private Integer id;
    private String titulo;
    private Empresa empresa;
    private String estado;
    private LocalDate fechaPostulacion;
    private String enlace;
    private String descripcion;
    private Double salario;
    private String notas;

    public Oferta() {}

    public Oferta(Integer id, String titulo, Empresa empresa, String estado, LocalDate fechaPostulacion,
                  String enlace, String descripcion, Double salario, String notas) {
        this.id = id;
        this.titulo = titulo;
        this.empresa = empresa;
        this.estado = estado;
        this.fechaPostulacion = fechaPostulacion;
        this.enlace = enlace;
        this.descripcion = descripcion;
        this.salario = salario;
        this.notas = notas;
    }

    public Integer getId() { return id; }
    public String getTitulo() { return titulo; }
    public Empresa getEmpresa() { return empresa; }
    public String getEstado() { return estado; }
    public LocalDate getFechaPostulacion() { return fechaPostulacion; }
    public String getEnlace() { return enlace; }
    public String getDescripcion() { return descripcion; }
    public Double getSalario() { return salario; }
    public String getNotas() { return notas; }

    public void setId(Integer id) { this.id = id; }
    public void setTitulo(String titulo) { this.titulo = titulo; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setFechaPostulacion(LocalDate fechaPostulacion) { this.fechaPostulacion = fechaPostulacion; }
    public void setEnlace(String enlace) { this.enlace = enlace; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setSalario(Double salario) { this.salario = salario; }
    public void setNotas(String notas) { this.notas = notas; }

    /**
     * Nombre de la empresa, para mostrarlo en la tabla.
     *
     * @return nombre de la empresa o cadena vacia
     */
    public String getNombreEmpresa() {
        return empresa != null ? empresa.getNombre() : "";
    }

    @Override
    public String toString() {
        return "[" + id + "] " + titulo + " en " + getNombreEmpresa() + " | Estado: " + estado;
    }
}
