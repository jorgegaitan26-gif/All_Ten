package servicios;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Administra la conexión a MySQL (patrón Singleton).
 *
 * Las credenciales se leen del archivo .env, que debe estar en la carpeta
 * desde la que se ejecuta la aplicación (la raíz del proyecto):
 *
 * <pre>
 * DB_HOST=servidor
 * DB_PORT=3306
 * DB_NAME=nombre_de_la_base
 * DB_USER=usuario
 * DB_PASSWORD=clave
 * </pre>
 *
 * Opcionalmente se puede definir DB_URL con la URL JDBC completa; si existe,
 * tiene prioridad sobre DB_HOST, DB_PORT y DB_NAME. Cada clave también se
 * puede dar como variable de entorno del sistema.
 *
 * Valores por defecto: localhost, puerto 3306, base all_ten_db, usuario root.
 */
public class ConexionBD {

    private static final String HOST_POR_DEFECTO = "localhost";
    private static final String PUERTO_POR_DEFECTO = "3306";
    private static final String BASE_POR_DEFECTO = "all_ten_db";
    private static final String USUARIO_POR_DEFECTO = "root";

    private static final String PARAMETROS =
            "?sslMode=PREFERRED&serverTimezone=UTC&allowPublicKeyRetrieval=true&connectTimeout=10000";

    private static ConexionBD instancia;
    private Connection conexion;

    private ConexionBD() throws Exception {
        conectar();
    }

    /**
     * Obtiene la única instancia de la conexión.
     *
     * @return instancia de ConexionBD
     * @throws Exception si no se puede establecer la conexión
     */
    public static synchronized ConexionBD getInstancia() throws Exception {
        if (instancia == null) {
            instancia = new ConexionBD();
        }
        return instancia;
    }

    /**
     * Obtiene la conexión activa. Si estaba cerrada o el servidor la tumbó por
     * inactividad (común en bases en la nube), abre una nueva.
     *
     * @return conexión a la base de datos
     * @throws Exception si no se puede establecer la conexión
     */
    public synchronized Connection getConexion() throws Exception {
        if (conexion == null || conexion.isClosed() || !conexion.isValid(2)) {
            conectar();
        }
        return conexion;
    }

    /**
     * Cierra la conexión solo si ya había sido creada (no abre una nueva).
     */
    public static synchronized void cerrarSiExiste() {
        if (instancia != null) {
            instancia.cerrar();
        }
    }

    /**
     * Cierra la conexión (por ejemplo, al cerrar la aplicación). Si se vuelve a
     * pedir con getConexion(), se abre una nueva.
     */
    public synchronized void cerrar() {
        if (conexion != null) {
            try {
                conexion.close();
            } catch (Exception e) {
                System.err.println("No se pudo cerrar la conexión: " + e.getMessage());
            }
            conexion = null;
        }
    }

    private void conectar() throws Exception {
        Map<String, String> env = leerEnv();

        String url = valor(env, "DB_URL", null);
        if (url == null) {
            String host = valor(env, "DB_HOST", HOST_POR_DEFECTO);
            String puerto = valor(env, "DB_PORT", PUERTO_POR_DEFECTO);
            String base = valor(env, "DB_NAME", BASE_POR_DEFECTO);
            url = "jdbc:mysql://" + host + ":" + puerto + "/" + base + PARAMETROS;
        }

        String user = valor(env, "DB_USER", USUARIO_POR_DEFECTO);
        String password = valor(env, "DB_PASSWORD", null);

        if (password == null) {
            password = System.getenv("ALL_TEN_DB_PASSWORD");
        }
        if (password == null) {
            throw new IllegalStateException(
                    "No se encontró DB_PASSWORD. Crea el archivo .env en la raíz del proyecto (ver .env.example).");
        }

        Class.forName("com.mysql.cj.jdbc.Driver");
        conexion = DriverManager.getConnection(url, user, password);
    }

    /** Busca la clave en el .env y, si no está, en las variables de entorno. */
    private static String valor(Map<String, String> env, String clave, String porDefecto) {
        String v = env.get(clave);
        if (v == null || v.isBlank()) {
            v = System.getenv(clave);
        }
        return (v == null || v.isBlank()) ? porDefecto : v;
    }

    /**
     * Lee el archivo .env (líneas CLAVE=valor) sin necesidad de librerías externas.
     */
    private static Map<String, String> leerEnv() {
        Map<String, String> mapa = new HashMap<>();
        Path ruta = Paths.get(".env");

        if (!Files.exists(ruta)) {
            return mapa;
        }

        try {
            List<String> lineas = Files.readAllLines(ruta, StandardCharsets.UTF_8);
            for (String linea : lineas) {
                String l = linea.trim();
                if (l.isEmpty() || l.startsWith("#") || !l.contains("=")) {
                    continue;
                }
                int i = l.indexOf('=');
                String clave = l.substring(0, i).trim();
                String val = l.substring(i + 1).trim();
                if (val.length() >= 2 && (val.startsWith("\"") && val.endsWith("\"")
                        || val.startsWith("'") && val.endsWith("'"))) {
                    val = val.substring(1, val.length() - 1);
                }
                mapa.put(clave, val);
            }
        } catch (IOException e) {
            System.err.println("No se pudo leer el archivo .env: " + e.getMessage());
        }
        return mapa;
    }
}
