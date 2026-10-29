package candidaturas;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@jsonPropertyOrder({"id", "titulo", "empresa", "estado", "fechaPostulacion", "enlace", "notas"})
public class Candidatura {
    private Integer id;
    private String titulo;
    private String empresa;
    private String estado;
    private String fechaPostulacion;
    private String enlace;
    private String notas;
}
