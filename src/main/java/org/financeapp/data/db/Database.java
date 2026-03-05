package org.financeapp.data.db;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class Database {

    // Carpeta donde se va a alojar la base de datos del proyecto localmente
    private static final Path DB_FOLDER = Path.of(System.getProperty("user.home"), ".financeapp");

    // Archivo físico de la base de datos real.
    private static final Path DB_FILE = DB_FOLDER.resolve("finance.db");

    // URlde JDBC correcta, para que DriveManager pueda manejarlo.
    private static final String JDBC_URL = "jdbc:sqlite:" + DB_FILE.toAbsolutePath();

    // Conexión a la base de datos de SQLite.
    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(JDBC_URL);
    }

    // Inicializar la base de datos: crear carpeta, abrir conexión, activar FK y ejecutar schema.
    public static void initialize() throws Exception {

        // Crear carpeta,createDirectories NO falla si ya existe.
        Files.createDirectories(DB_FOLDER);

        // Abrir conexión
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Activar claves foráneas
            stmt.execute("PRAGMA foreign_keys = ON;");

            // Ejecutar schema, es decir, tablas y seeds de SQLite.
            runSchema(stmt);
        }
    }

    // Ejecutar el schema.sql que creamos en resources/db/schema.sql
    private static void runSchema(Statement stmt) throws Exception {
        try (InputStream is = Database.class.getResourceAsStream("/db/schema.sql")) {
            if (is == null) {
                throw new RuntimeException(
                        "No se encontró /db/schema.sql en el classpath"
                );
            }

            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append('\n');
                }
            }

            // Ejecutar sentencia por sentencia (separadas por ;)
            for (String raw : content.toString().split(";")) {
                String sql = raw.trim();
                if (!sql.isEmpty()) {
                    stmt.execute(sql);
                }
            }
        }
    }


}