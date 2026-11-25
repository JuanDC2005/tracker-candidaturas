package candidaturas.modelo;

import java.time.LocalDate;

/**
 * Empresa a la que se han enviado candidaturas.
 */
public class Empresa {
    private Integer id;
    private String nombre;
    private String sector;
    private String web;
    private LocalDate fechaRegistro;

    public Empresa() {}

    public Empresa(Integer id, String nombre, String sector, String web, LocalDate fechaRegistro) {
        this.id = id;
        this.nombre = nombre;
        this.sector = sector;
        this.web = web;
        this.fechaRegistro = fechaRegistro;
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public String getSector() { return sector; }
    public String getWeb() { return web; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }

    public void setId(Integer id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setSector(String sector) { this.sector = sector; }
    public void setWeb(String web) { this.web = web; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    /**
     * Se usa para mostrar la empresa en los ComboBox.
     */
    @Override
    public String toString() {
        return nombre;
    }
}
