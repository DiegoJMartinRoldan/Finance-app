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

    // URl de JDBC correcta, para que DriveManager pueda manejarlo.
    private static final String JDBC_URL = "jdbc:sqlite:" + DB_FILE.toAbsolutePath();

    // Conexión a la base de datos de SQLite.
    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(JDBC_URL);
    }


    // Ejecutar el schema.sql que creamos en resources/db/schema.sql
    private static void runSchema(Statement statement) throws Exception {
        try (InputStream inputStream = Database.class.getResourceAsStream("/db/schema.sql")) {
            if (inputStream == null) {
                throw new RuntimeException(
                        "No se encontró /db/schema.sql en la ruta de la aplicación"
                );
            }

            StringBuilder content = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append('\n');
                }
            }

            // Ejecutar sentencia por sentencia (separadas por ;)
            for (String raw : content.toString().split(";")) {
                String sql = raw.trim();
                if (!sql.isEmpty()) {
                    statement.execute(sql);
                }
            }
        }
    }





    // Inicializar la base de datos: crear carpeta, abrir conexión, activar FK y ejecutar schema.
    public static void initialize() throws Exception {

        // Crear carpeta,createDirectories, si ya existe no falla.
        Files.createDirectories(DB_FOLDER);

        // Abrir conexión
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            // Activar claves foráneas
            statement.execute("PRAGMA foreign_keys = ON;");

            // Ejecutar schema, es decir, tablas y seeds de SQLite.
            runSchema(statement);
        }
    }



}