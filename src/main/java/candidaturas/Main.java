package candidaturas;

import java.util.List;
import java.util.Objects;

public class Main {
    private static final String ARCHIVO = "datos.txt";

    public static void main(String[] args) {
        if (args.length == 0) return;
        List<Candidatura> lista = GestorArchivos.cargar(ARCHIVO);
        String comando = args[0];

        if (comando.equals("-añadir")) {
            String titulo = extraer(args, "--titulo");
            String empresa = extraer(args, "--empresa");
            String estado = extraer(args, "--estado");
            String fecha = extraer(args, "--fecha");

            if (titulo != null && empresa != null && estado != null && fecha != null) {
                Integer nuevoId = lista.isEmpty() ? 1 : lista.get(lista.size() - 1).getId() + 1;
                lista.add(new Candidatura(nuevoId, titulo, empresa, estado, fecha, "", ""));
                GestorArchivos.guardar(lista, ARCHIVO);
                System.out.println("Añadida candidatura con ID: " + nuevoId);
            }
        }
        else if (comando.equals("-listar")) {
            String filtroEst = extraer(args, "--estado");
            String filtroEmp = extraer(args, "--empresa");

            for (Candidatura c : lista) {
                Boolean coincideEst = (filtroEst == null || c.getEstado().equalsIgnoreCase(filtroEst));
                Boolean coincideEmp = (filtroEmp == null || c.getEmpresa().equalsIgnoreCase(filtroEmp));
                if (coincideEst && coincideEmp) {
                    System.out.println(c.toString());
                }
            }
        }
    }

    private static String extraer(String[] args, String clave) {
        for (Integer i = 0; i < args.length - 1; i++) {
            if (args[i].equalsIgnoreCase(clave)) return args[i + 1];
        }
        return null;
    }
}