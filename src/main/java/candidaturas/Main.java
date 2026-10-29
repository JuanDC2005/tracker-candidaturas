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
        else if (comando.equals("-ver")) {
            if (args.length > 1) {
                Integer idBuscado = Integer.valueOf(args[1]);
                lista.stream()
                        .filter(c -> Objects.equals(c.getId(), idBuscado))
                        .findFirst()
                        .ifPresentOrElse(
                                c -> System.out.println(c.toString()),
                                () -> System.out.println("ID no encontrado.")
                        );
            }
        }
        else if (comando.equals("-borrar")) {
            if (args.length > 1) {
                Integer idBorrar = Integer.valueOf(args[1]);
                Boolean eliminado = lista.removeIf(c -> Objects.equals(c.getId(), idBorrar));
                if (eliminado) {
                    GestorArchivos.guardar(lista, ARCHIVO);
                    System.out.println("Eliminada candidatura: " + idBorrar);
                } else {
                    System.out.println("Error: ID inexistente.");
                }
            }
        }
        else if (comando.equals("-importar")) {
            if (args.length > 1) {
                List<Candidatura> importadas = GestorArchivos.cargar(args[1]);
                lista.addAll(importadas);
                GestorArchivos.guardar(lista, ARCHIVO);
                System.out.println("Importadas candidaturas desde " + args[1]);
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