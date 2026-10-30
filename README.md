# Tracker de Candidaturas Laborales - v1.0

## Descripción Breve
Aplicación Java para la gestión y seguimiento de candidaturas laborales durante la búsqueda de empleo. Esta versión (v1.0) opera mediante una interfaz de línea de comandos (CLI) procesando argumentos directamente, sin menús interactivos, y utiliza un sistema de persistencia basado en archivos de texto (CSV).

## Integrantes del Equipo
* **Juan Antonio Díaz Castro**
* **Adrián Sánchez Muñoz**
  
## Tecnologías Utilizadas
* **Lenguaje:** Java 25 (OpenJDK)
* **Gestor de dependencias:** Maven
* **Librerías principales:**
    * Lombok (v1.18.30) para reducción de código boilerplate (Getters, Setters, Constructores).
    * Jackson Databind & Dataformat CSV (tools.jackson v3.2.4) para el mapeo y persistencia de datos CSV.
* **Prototipado y Diseño:** Figma
* **Control de Versiones:** Git / GitHub

## Archivo de Datos de Prueba
El proyecto incluye un archivo de prueba llamado `candidaturas.csv` ubicado en el directorio raíz. Este archivo contiene 15 registros iniciales generados para probar las funcionalidades de filtrado e importación.

## Prototipo y Diseño (Figma)
El diseño de la aplicación y el prototipo interactivo para las versiones v1.0, v2.0 y v3.0 se encuentran documentados aquí:
* **Enlace al Prototipo de Figma (Solo lectura): https://www.figma.com/design/s8JNiH1ELBpmdLWOnrn1Up/Job-Tracker-Landing-Page---Website-Designs---Free-Website-Templates---Web-Design-Inspiration--Community-?node-id=0-1&t=AePawSa73hcBCvLL-1** 
* **Manual de Estilo:** Disponible en la carpeta `docs/manual_estilo.pdf` del repositorio.
* **Capturas del Prototipo:** Disponibles en la carpeta `docs/prototipo_pantallas.pdf` del repositorio.

## Instrucciones de Compilación y Ejecución

### Compilación (Maven)
Para compilar el proyecto y descargar las dependencias, ejecuta desde la raíz del proyecto:
```bash
mvn clean package
```

### Ejecución (CLI)
El programa se ejecuta pasando la operación y sus parámetros como argumentos directamente.

**Comandos disponibles:**

* **Listar todas las candidaturas:**
  ```bash
  java -jar target/tracker-candidaturas.jar -listar
  ```

* **Listar con filtros (estado o empresa):**
  ```bash
  java -jar target/tracker-candidaturas.jar -listar --estado pendiente --empresa Indra
  ```

* **Añadir una nueva candidatura:**
  ```bash
  java -jar target/tracker-candidaturas.jar -añadir --titulo "Dev Java" --empresa "Indra" --estado pendiente --fecha 15/09/2026
  ```

* **Ver detalles de una candidatura por ID:**
  ```bash
  java -jar target/tracker-candidaturas.jar -ver 13
  ```

* **Borrar una candidatura por ID:**
  ```bash
  java -jar target/tracker-candidaturas.jar -borrar 13
  ```

* **Importar candidaturas desde un archivo CSV externo:**
  ```bash
  java -jar target/tracker-candidaturas.jar -importar candidaturas.csv
  ```