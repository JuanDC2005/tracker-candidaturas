-- Esquema de la base de datos del Tracker de Candidaturas (v2.0)
-- Base de datos: SQLite

CREATE TABLE IF NOT EXISTS empresa (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL UNIQUE,
    sector TEXT,
    web TEXT,
    fechaRegistro TEXT
);

CREATE TABLE IF NOT EXISTS oferta (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    id_empresa INTEGER NOT NULL,
    titulo TEXT NOT NULL,
    descripcion TEXT,
    estado TEXT NOT NULL CHECK (estado IN ('pendiente', 'entrevista', 'rechazado', 'aceptado')),
    fechaPostulacion TEXT,
    enlace TEXT,
    salario REAL,
    notas TEXT,
    FOREIGN KEY (id_empresa) REFERENCES empresa(id)
);

CREATE TABLE IF NOT EXISTS entrevista (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    id_oferta INTEGER NOT NULL,
    fecha TEXT,
    tipo TEXT,
    notas TEXT,
    resultado TEXT DEFAULT 'pendiente',
    FOREIGN KEY (id_oferta) REFERENCES oferta(id) ON DELETE CASCADE
);
