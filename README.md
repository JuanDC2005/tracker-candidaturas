# Tracker de Candidaturas Laborales - v2.0

## Descripción Breve
Aplicación Java de escritorio para gestionar y seguir las candidaturas laborales durante la búsqueda de empleo.

La versión 2.0 sustituye la línea de comandos y el fichero CSV de la v1.0 por:
* una **interfaz gráfica con JavaFX**, con vistas diseñadas en FXML con Scene Builder;
* una **base de datos SQLite** a la que se accede con **JDBC**.

## Integrantes del Equipo
* **Juan Antonio Díaz Castro**
* **Adrián Sánchez Muñoz**

## Tecnologías Utilizadas
* **Lenguaje:** Java 25 (OpenJDK)
* **Gestor de dependencias:** Maven
* **Interfaz gráfica:** JavaFX 21 (controls + fxml), vistas creadas con Scene Builder
* **Base de datos:** SQLite con el driver `sqlite-jdbc` (3.47.1.0)
* **Acceso a datos:** JDBC (`PreparedStatement`, transacciones, consultas con `JOIN`)
* **CSV:** Jackson Dataformat CSV (tools.jackson v3.2.4) para importar el CSV de la v1.0
* **Prototipado y Diseño:** Figma
* **Control de Versiones:** Git / GitHub

## Funcionalidades de la v2.0
* **Candidaturas:**
  * tabla con todas las ofertas y tarjetas de resumen (total, pendientes, en entrevista, aceptadas);
  * filtros combinados por texto, empresa, estado, salario mínimo y rango de fechas;
  * alta y edición en un formulario modal con validación;
  * cambio de estado desde la propia tabla con un desplegable de colores;
  * borrado con confirmación, que también borra sus entrevistas en una transacción.
* **Detalle de candidatura:** todos los datos de la oferta y un panel con sus entrevistas, donde se pueden añadir, editar y borrar.
* **Empresas:** listado con el número de candidaturas de cada empresa (`LEFT JOIN` + `COUNT`), con alta, edición y borrado.
* **Entrevistas:** formulario modal de alta y edición (fecha, tipo, resultado y notas).
* **Importar CSV:** carga en la base de datos el `candidaturas.csv` de la v1.0. Crea las empresas que no existan y lo hace todo en una única transacción.

## Base de Datos
La base de datos se crea sola la primera vez que se ejecuta la aplicación, en el archivo `tracker.db`, a partir del script `src/main/resources/schema.sql`.

| Tabla        | Contenido                                                       |
|--------------|-----------------------------------------------------------------|
| `empresa`    | id, nombre (único), sector, web, fecha de registro              |
| `oferta`     | id, título, empresa (FK), estado, fecha, enlace, salario, notas |
| `entrevista` | id, oferta (FK, borrado en cascada), fecha, tipo, notas, resultado |

La conexión se configura en el archivo `config.properties` de la raíz del proyecto:
```properties
db.url=jdbc:sqlite:tracker.db
```
El archivo `tracker.db` no se sube al repositorio (está en el `.gitignore`).

## Estructura del Proyecto
```
src/main/java/candidaturas/
├── App.java / Lanzador.java   -> arranque de la aplicación JavaFX
├── bd/Conexion.java           -> conexión JDBC y creación de tablas
├── modelo/                    -> Empresa, Oferta, Entrevista
├── dao/                       -> EmpresaDAO, OfertaDAO, EntrevistaDAO, ImportadorCSV
└── controlador/               -> controladores de cada vista FXML
src/main/resources/
├── schema.sql
└── vista/                     -> archivos .fxml y estilos.css
```

## Archivo de Datos de Prueba
El proyecto incluye en la raíz el archivo `candidaturas.csv`, con 15 candidaturas de prueba. Para cargarlas, pulsa **"Importar CSV"** en la pantalla de Candidaturas y elige ese archivo.

## Prototipo y Diseño (Figma)
* **Enlace al Prototipo de Figma (Solo lectura): https://www.figma.com/design/s8JNiH1ELBpmdLWOnrn1Up/Job-Tracker-Landing-Page---Website-Designs---Free-Website-Templates---Web-Design-Inspiration--Community-?node-id=0-1&t=AePawSa73hcBCvLL-1**
* **Manual de Estilo:** `docs/Manual de Estilo - Tracker de Candidaturas.pdf`. La interfaz sigue sus colores (principal `#2563EB`, texto `#1E293B`, fondo `#F8FAFC`) y su menú lateral.

## Instrucciones de Compilación y Ejecución

### Desde IntelliJ
Abre el proyecto y ejecuta la clase `candidaturas.Lanzador`.

### Desde Maven
```bash
mvn clean javafx:run
```

## Reparto del Trabajo (v2.0)

| Integrante | Tareas |
|------------|--------|
| **Juan Antonio Díaz** | Dependencias, `schema.sql` y `config.properties`, clase `Conexion`, modelos, `OfertaDAO`, importación del CSV, ventana principal con menú lateral, pantalla de candidaturas, filtros, cambio de estado, formulario de candidaturas y pantalla de detalle |
| **Adrián Sánchez** | `EmpresaDAO`, `EntrevistaDAO`, pantalla de empresas, formulario de entrevistas, número de candidaturas por empresa, limpieza del código de la v1.0 y README de la v2.0 |
