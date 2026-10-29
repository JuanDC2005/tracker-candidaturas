package candidaturas;

import tools.jackson.databind.MappingIterator;
import tools.jackson.dataformat.csv.CsvMapper;
import tools.jackson.dataformat.csv.CsvSchema;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GestorArchivos {
    private static final CsvMapper mapper = CsvMapper.builder().build();
    private static final CsvSchema schema = mapper.schemaFor(Candidatura.class)
            .withColumnSeparator(';')
            .withoutQuoteChar();
}
