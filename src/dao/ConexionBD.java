package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    private static final String URL = "jdbc:mysql://localhost:3306/all_ten_db?useSSL=false&serverTimezone=UTC";

    public static Connection obtenerConexion() {
        Connection conexion = null;
        try {
            String user = System.getenv("ALL_TEN_DB_USER");
            String password = System.getenv("ALL_TEN_DB_PASSWORD");
            if (user == null || password == null) {
                throw new IllegalStateException(
                    "Define las variables de entorno ALL_TEN_DB_USER y ALL_TEN_DB_PASSWORD."
                );
            }

            // Cargar el driver JDBC
            Class.forName("com.mysql.cj.jdbc.Driver");
            conexion = DriverManager.getConnection(URL, user, password);
            System.out.println("¡Conexión exitosa a la base de datos All Ten!");
        } catch (ClassNotFoundException e) {
            System.err.println("Error: No se encontró el Driver de MySQL JDBC -> " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Error de SQL al conectar con la base de datos -> " + e.getMessage());
        }
        return conexion;
    }
}