package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class ConexionBD {
    private static final String URL = "jdbc:mysql://localhost:3306/all_ten_db?useSSL=false&serverTimezone=UTC";
    private static final Path ARCHIVO_ENV = Path.of(".env");

    public static Connection obtenerConexion() throws SQLException {
        Map<String, String> configuracion = cargarArchivoEnv();
        String user = obtenerConfiguracion("ALL_TEN_DB_USER", configuracion);
        String password = obtenerConfiguracion("ALL_TEN_DB_PASSWORD", configuracion);
        if (user == null || user.trim().isEmpty()
                || password == null || password.trim().isEmpty()) {
            throw new SQLException(
                "Configura ALL_TEN_DB_USER y ALL_TEN_DB_PASSWORD en el archivo .env "
                    + "o como variables de entorno."
            );
        }

        return DriverManager.getConnection(URL, user, password);
    }

    private static String obtenerConfiguracion(String clave, Map<String, String> archivoEnv) {
        String valorEntorno = System.getenv(clave);
        if (valorEntorno != null && !valorEntorno.trim().isEmpty()) {
            return valorEntorno;
        }
        return archivoEnv.get(clave);
    }

    private static Map<String, String> cargarArchivoEnv() throws SQLException {
        Map<String, String> configuracion = new HashMap<>();
        if (!Files.exists(ARCHIVO_ENV)) {
            return configuracion;
        }

        try {
            int numeroLinea = 0;
            for (String linea : Files.readAllLines(ARCHIVO_ENV)) {
                numeroLinea++;
                String contenido = linea.trim();
                if (contenido.isEmpty() || contenido.startsWith("#")) {
                    continue;
                }
                if (contenido.startsWith("export ")) {
                    contenido = contenido.substring("export ".length()).trim();
                }

                int separador = contenido.indexOf('=');
                if (separador < 1) {
                    throw new SQLException(
                        "Formato inválido en .env, línea " + numeroLinea + ". Usa CLAVE=valor."
                    );
                }

                String clave = contenido.substring(0, separador).trim();
                String valor = contenido.substring(separador + 1).trim();
                if (valor.length() >= 2) {
                    char comillaInicial = valor.charAt(0);
                    char comillaFinal = valor.charAt(valor.length() - 1);
                    if ((comillaInicial == '"' && comillaFinal == '"')
                            || (comillaInicial == '\'' && comillaFinal == '\'')) {
                        valor = valor.substring(1, valor.length() - 1);
                    }
                }
                configuracion.put(clave, valor);
            }
        } catch (IOException e) {
            throw new SQLException("No se pudo leer el archivo .env.", e);
        }
        return configuracion;
    }

    public static void probarConexion() throws SQLException {
        try (Connection conexion = obtenerConexion();
             Statement statement = conexion.createStatement();
             ResultSet resultado = statement.executeQuery("SELECT 1")) {
            if (!resultado.next() || resultado.getInt(1) != 1) {
                throw new SQLException("La base de datos no devolvió el resultado esperado.");
            }
        }
    }
}