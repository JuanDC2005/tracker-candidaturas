package candidaturas;

import tools.jackson.databind.MappingIterator;
import tools.jackson.dataformat.csv.CsvMapper;
import tools.jackson.dataformat.csv.CsvSchema;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GestorArchivos {
    private static final CsvMapper mapper = CsvMapper.builder().build();
    // Se indica el orden de las columnas y que la primera linea es la cabecera
    private static final CsvSchema schema = mapper.schemaFor(Candidatura.class)
            .sortedBy("id", "titulo", "empresa", "estado", "fechaPostulacion", "enlace", "notas")
            .withHeader()
            .withColumnSeparator(';')
            .withoutQuoteChar();
    public static List<Candidatura> cargar(String ruta) {
        try {
            File archivo = new File(ruta);
            if (!archivo.exists()) return new ArrayList<>();
            MappingIterator<Candidatura> it = mapper.readerFor(Candidatura.class).with(schema).readValues(archivo);
            return it.readAll();
        } catch (Exception e) {
            System.out.println("Error al leer: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    public static void guardar(List<Candidatura> lista, String ruta) {
        try {
            mapper.writer(schema).writeValue(new File(ruta), lista);
        } catch (Exception e) {
            System.out.println("Error al guardar: " + e.getMessage());
        }
    }
}


